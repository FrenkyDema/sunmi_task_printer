package dev.francescodema.sunmi_task_printer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import io.flutter.plugin.common.MethodChannel.Result;
import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.ILcdCallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

/**
 * Handles all direct interactions with the Sunmi Printer AIDL Service.
 * Uses a single-threaded background executor to prevent UI blocking,
 * but resolves Flutter Results immediately upon successful IPC dispatch to prevent Transaction Buffer Deadlocks.
 */
public class SunmiTaskPrinterMethod {
    private final Context _context;
    private IWoyouService _printerService;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Pending BIND_SERVICE result, resolved exactly once by whichever callback
     * fires first. Touched only from the main thread.
     */
    private Result bindResultPending;

    /**
     * True once bindService() has been accepted by the system, regardless of
     * whether onServiceConnected has fired yet. Needed so unbindService() can
     * release a connection that never completed.
     */
    private boolean isBound;

    /**
     * Milliseconds to wait for a printer callback before giving up. The AIDL
     * service is normally prompt; this only guards against a callback that the
     * firmware never delivers, so Dart is never left awaiting forever.
     */
    private static final long CALLBACK_TIMEOUT_MS = 5000;

    /**
     * Strong references to callbacks currently in flight. A Binder stub passed
     * to a remote process is otherwise eligible for garbage collection before
     * the reply arrives, which would silently drop the result.
     */
    private final Set<ICallback> pendingCallbacks = Collections.synchronizedSet(new HashSet<>());

    private final ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _printerService = IWoyouService.Stub.asInterface(service);
            resolveBindResult(true, null, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // The process died but the binding survives: Android calls
            // onServiceConnected again once the service is back.
            _printerService = null;
        }

        @Override
        public void onNullBinding(ComponentName name) {
            // The service exists but refused to return a binder: never leave Dart awaiting.
            resolveBindResult(false, "NULL_BINDING", "Printer service returned a null binding");
        }

        @Override
        public void onBindingDied(ComponentName name) {
            // Terminal for this connection: without an explicit rebind every
            // later call would fail with "Service disconnected" until the host
            // app is restarted.
            _printerService = null;
            resolveBindResult(false, "BINDING_DIED", "Printer service binding died");
            rebind();
        }
    };

    public SunmiTaskPrinterMethod(Context context) {
        this._context = context;
    }

    private Intent printerServiceIntent() {
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        return intent;
    }

    /**
     * Drops the current connection and asks for a fresh one. Used after the
     * binding dies, and lazily when a command finds the service missing.
     */
    private void rebind() {
        try {
            _context.unbindService(connService);
        } catch (IllegalArgumentException ignored) {
            // Nothing was registered; nothing to release.
        }
        isBound = false;
        try {
            isBound = _context.bindService(printerServiceIntent(), connService, Context.BIND_AUTO_CREATE);
        } catch (SecurityException ignored) {
            isBound = false;
        }
    }

    /**
     * Reports the service as unavailable and schedules a reconnect attempt, so
     * the next command has a working binding even though this one fails.
     */
    private void reportUnavailable(Result result) {
        sendError(result, "UNAVAILABLE", "Printer service is not connected");
        if (!isBound) {
            mainHandler.post(this::rebind);
        }
    }

    /**
     * Completes the outstanding BIND_SERVICE call, if any. Every path out of
     * bindService() must funnel through here so the Dart future always settles.
     */
    private void resolveBindResult(boolean success, String errorCode, String errorMessage) {
        mainHandler.post(() -> {
            Result pending = bindResultPending;
            if (pending == null) {
                return;
            }
            bindResultPending = null;
            if (success) {
                pending.success(true);
            } else {
                pending.error(errorCode, errorMessage, null);
            }
        });
    }

    public void bindService(Result result) {
        if (_printerService != null) {
            result.success(true);
            return;
        }
        if (bindResultPending != null) {
            result.error("BIND_IN_PROGRESS", "A bind request is already pending", null);
            return;
        }

        bindResultPending = result;

        boolean accepted;
        try {
            accepted = _context.bindService(printerServiceIntent(), connService, Context.BIND_AUTO_CREATE);
        } catch (SecurityException e) {
            accepted = false;
        }

        if (accepted) {
            isBound = true;
        } else {
            // Service missing, or not visible to this app because the host manifest
            // lacks a <queries> entry for woyou.aidlservice.jiuiv5.
            try {
                _context.unbindService(connService);
            } catch (IllegalArgumentException ignored) {
                // Nothing was registered; nothing to release.
            }
            resolveBindResult(false, "SERVICE_UNAVAILABLE",
                    "Sunmi printer service is not available on this device");
        }
    }

    public void unbindService(Result result) {
        if (isBound) {
            try {
                _context.unbindService(connService);
            } catch (IllegalArgumentException ignored) {
                // Already unbound.
            }
            isBound = false;
        }
        _printerService = null;
        resolveBindResult(false, "UNBOUND", "Service was unbound before binding completed");
        result.success(true);
    }

    /**
     * Releases the service connection when the Flutter engine goes away, so a
     * detached engine never leaks a bound connection.
     */
    public void dispose() {
        if (isBound) {
            try {
                _context.unbindService(connService);
            } catch (IllegalArgumentException ignored) {
                // Already unbound.
            }
            isBound = false;
        }
        _printerService = null;
        pendingCallbacks.clear();
        resolveBindResult(false, "UNBOUND", "Flutter engine detached before binding completed");
    }

    private void runOnBackground(Runnable task) {
        TaskProvider.executor.execute(task);
    }

    private void sendSuccess(Result result, Object value) {
        mainHandler.post(() -> result.success(value));
    }

    private void sendError(Result result, String code, String message) {
        mainHandler.post(() -> result.error(code, message, null));
    }

    /**
     * Dummy callback to satisfy Sunmi AIDL requirements without blocking Flutter.
     * In buffer mode, physical callbacks do not fire until commit.
     */
    /**
     * Bridges a Sunmi AIDL callback to a Flutter Result, settling it exactly
     * once with the outcome the firmware actually reports. Use this for
     * commands whose success cannot be assumed from a successful dispatch,
     * such as kicking the cash drawer.
     */
    private ICallback createResultCallback(Result result, String operation) {
        AtomicBoolean settled = new AtomicBoolean(false);
        ICallback[] holder = new ICallback[1];

        Runnable onTimeout = () -> {
            if (settled.compareAndSet(false, true)) {
                pendingCallbacks.remove(holder[0]);
                result.error("TIMEOUT", operation + " reported no result within "
                        + CALLBACK_TIMEOUT_MS + "ms", null);
            }
        };

        ICallback callback = new ICallback.Stub() {
            @Override
            public void onRunResult(boolean isSuccess) {
                settle(isSuccess, isSuccess ? null : "OPERATION_FAILED",
                        operation + " was rejected by the printer");
            }

            @Override
            public void onReturnString(String resultStr) {
            }

            @Override
            public void onRaiseException(int code, String msg) {
                settle(false, "PRINTER_EXCEPTION", operation + " failed (code " + code + "): " + msg);
            }

            @Override
            public void onPrintResult(int code, String msg) {
            }

            private void settle(boolean success, String errorCode, String errorMessage) {
                if (!settled.compareAndSet(false, true)) {
                    return;
                }
                pendingCallbacks.remove(holder[0]);
                mainHandler.removeCallbacks(onTimeout);
                mainHandler.post(() -> {
                    if (success) {
                        result.success(true);
                    } else {
                        result.error(errorCode, errorMessage, null);
                    }
                });
            }
        };

        holder[0] = callback;
        pendingCallbacks.add(callback);
        mainHandler.postDelayed(onTimeout, CALLBACK_TIMEOUT_MS);
        return callback;
    }

    private ICallback createDummyCallback() {
        return new ICallback.Stub() {
            @Override
            public void onRunResult(boolean isSuccess) {
            }

            @Override
            public void onReturnString(String resultStr) {
            }

            @Override
            public void onRaiseException(int code, String msg) {
            }

            @Override
            public void onPrintResult(int code, String msg) {
            }
        };
    }

    private ILcdCallback createDummyLcdCallback() {
        return new ILcdCallback.Stub() {
            @Override
            public void onRunResult(boolean show) {
            }
        };
    }

    public void initPrinter(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.printerInit(createDummyCallback());
                sendSuccess(result, true); // Resolve immediately so Dart can continue queuing
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void updatePrinter(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, "ERROR");
                    return;
                }
                int status = _printerService.updatePrinterState();
                String statusMsg = switch (status) {
                    case 0 -> "ERROR";
                    case 1 -> "NORMAL";
                    case 2 -> "ABNORMAL_COMMUNICATION";
                    case 3 -> "OUT_OF_PAPER";
                    case 4 -> "PREPARING";
                    case 5 -> "OVERHEATED";
                    case 6 -> "OPEN_THE_LID";
                    case 7 -> "PAPER_CUTTER_ABNORMAL";
                    case 8 -> "PAPER_CUTTER_RECOVERED";
                    case 9 -> "NO_BLACK_MARK";
                    case 505 -> "NO_PRINTER_DETECTED";
                    case 507 -> "FAILED_TO_UPGRADE_FIRMWARE";
                    default -> "EXCEPTION";
                };
                sendSuccess(result, statusMsg);
            } catch (RemoteException e) {
                sendSuccess(result, "EXCEPTION");
            }
        });
    }

    public void printText(String text, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.printText(text, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void setAlignment(int alignment, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.setAlignment(alignment, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void setFontSize(int fontSize, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.setFontSize(fontSize, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void printColumn(String[] stringColumns, int[] columnWidth, int[] columnAlignment, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.printColumnsText(stringColumns, columnWidth, columnAlignment, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void printImage(Bitmap bitmap, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.printBitmap(bitmap, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void cutPaper(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.cutPaper(createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void getPrinterSerialNumber(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, "NOT FOUND");
                    return;
                }
                String serial = _printerService.getPrinterSerialNo();
                sendSuccess(result, serial != null ? serial : "DEFAULT");
            } catch (RemoteException e) {
                sendSuccess(result, "NOT FOUND");
            }
        });
    }

    public void getPrinterVersion(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, "NOT FOUND");
                    return;
                }
                String version = _printerService.getPrinterVersion();
                sendSuccess(result, version != null ? version : "DEFAULT");
            } catch (RemoteException e) {
                sendSuccess(result, "NOT FOUND");
            }
        });
    }

    public void getPrinterPaperSize(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, 1);
                    return;
                }
                sendSuccess(result, _printerService.getPrinterPaper());
            } catch (RemoteException e) {
                sendSuccess(result, 1);
            }
        });
    }

    public void getPrinterMode(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, 3);
                    return;
                }
                int mode = _printerService.getPrinterMode();
                String modeDesc = switch (mode) {
                    case 0 -> "NORMAL_MODE";
                    case 1 -> "BLACK_LABEL_MODE";
                    case 2 -> "LABEL_MODE";
                    default -> "ERROR";
                };
                sendSuccess(result, modeDesc);
            } catch (RemoteException e) {
                sendSuccess(result, "ERROR");
            }
        });
    }

    public void openDrawer(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                // Resolved by the callback: a dispatched kick is not an opened drawer.
                _printerService.openDrawer(createResultCallback(result, "Opening the cash drawer"));
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void drawerIsConnected(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                sendSuccess(result, _printerService.getDrawerStatus());
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void timesOpened(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                sendSuccess(result, _printerService.getOpenDrawerTimes());
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void lineWrap(int lines, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.lineWrap(lines, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void sendRaw(byte[] bytes, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.sendRAWData(bytes, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void enterPrinterBuffer(boolean clear, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, false);
                    return;
                }
                _printerService.enterPrinterBuffer(clear);
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendSuccess(result, false);
            }
        });
    }

    public void commitPrinterBuffer(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, false);
                    return;
                }
                _printerService.commitPrinterBuffer();
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendSuccess(result, false);
            }
        });
    }

    public void exitPrinterBuffer(boolean clear, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, false);
                    return;
                }
                _printerService.exitPrinterBuffer(clear);
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendSuccess(result, false);
            }
        });
    }

    public void printQRCode(String data, int moduleSize, int errorLevel, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.printQRCode(data, moduleSize, errorLevel, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void printBarCode(String data, int barcodeType, int textPosition, int width, int height, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.printBarCode(data, barcodeType, height, width, textPosition, createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void sendLCDCommand(int flag, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, false);
                    return;
                }
                _printerService.sendLCDCommand(flag);
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendSuccess(result, false);
            }
        });
    }

    public void sendLCDString(String string, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.sendLCDString(string, createDummyLcdCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void sendLCDBitmap(Bitmap bitmap, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.sendLCDBitmap(bitmap, createDummyLcdCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void sendLCDDoubleString(String topText, String bottomText, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.sendLCDDoubleString(topText, bottomText, createDummyLcdCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void sendLCDFillString(String string, int size, boolean fill, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.sendLCDFillString(string, size, fill, createDummyLcdCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void sendLCDMultiString(String[] text, int[] align, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    reportUnavailable(result);
                    return;
                }
                _printerService.sendLCDMultiString(text, align, createDummyLcdCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }
}