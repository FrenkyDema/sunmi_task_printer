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
    private Result bindResultPending;

    private final ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _printerService = IWoyouService.Stub.asInterface(service);
            if (bindResultPending != null) {
                bindResultPending.success(true);
                bindResultPending = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            _printerService = null;
        }
    };

    public SunmiTaskPrinterMethod(Context context) {
        this._context = context;
    }

    public void bindService(Result result) {
        if (_printerService != null) {
            result.success(true);
            return;
        }
        bindResultPending = result;
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        _context.bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    public void unbindService(Result result) {
        if (_printerService != null) {
            _context.unbindService(connService);
            _printerService = null;
        }
        result.success(true);
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
                    return;
                }
                _printerService.openDrawer(createDummyCallback());
                sendSuccess(result, true);
            } catch (RemoteException e) {
                sendError(result, "REMOTE_EXCEPTION", e.getMessage());
            }
        });
    }

    public void drawerIsConnected(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, false);
                    return;
                }
                sendSuccess(result, _printerService.getDrawerStatus());
            } catch (RemoteException e) {
                sendSuccess(result, false);
            }
        });
    }

    public void timesOpened(Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendSuccess(result, 0);
                    return;
                }
                sendSuccess(result, _printerService.getOpenDrawerTimes());
            } catch (RemoteException e) {
                sendSuccess(result, 0);
            }
        });
    }

    public void lineWrap(int lines, Result result) {
        runOnBackground(() -> {
            try {
                if (_printerService == null) {
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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
                    sendError(result, "UNAVAILABLE", "Service disconnected");
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