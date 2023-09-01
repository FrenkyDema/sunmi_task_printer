package dev.francescodema.sunmi_task_printer;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.ILcdCallback;
import woyou.aidlservice.jiuiv5.IWoyouService;


/**
 * The type Sunmi task printer method.
 */
public class SunmiTaskPrinterMethod {
    private IWoyouService _printerService;
    private final Context _context;

    /**
     * Instantiates a new Sunmi task printer method.
     *
     * @param context the context
     */
    public SunmiTaskPrinterMethod(Context context) {
        this._context = context;
    }

    private final ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                _printerService = IWoyouService.Stub.asInterface(service);
                String serviceVersion = _printerService.getServiceVersion();
                Toast.makeText(_context, "Sunmi Printer Service Connected. Version :" + serviceVersion, Toast.LENGTH_LONG).show();


            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

                Toast.makeText(_context, "Sunmi Printer Service Not Found", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Toast.makeText(_context, "Sunmi Printer Service Disconnected", Toast.LENGTH_LONG).show();
        }
    };

    /**
     * Bind printer service.
     */
    public void bindService() {
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        _context.bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind printer service.
     */
    public void unbindService() {
        _context.unbindService(connService);
    }


    /**
     * Init printer boolean.
     *
     * @return the outcome of the operation
     */
    public boolean initPrinter() {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.printerInit(this._callback()));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }


    /**
     * Get the latest status return value of the printer:
     * <p>
     * 1: Printer is normal
     * <p>
     * 2: Printer update status
     * <p>
     * 3: Get state abnormalities
     * <p>
     * 4: Lamdable paper
     * <p>
     * 5: overheated
     * <p>
     * 6: Open cover
     * <p>
     * 7: cut knife abnormal
     * <p>
     * 8: cut knife recovery
     * <p>
     * 505: The printer is not detected
     *
     * @return the last status int
     */
    @SuppressLint("NewApi")
    public int updatePrinter() {
        try {
            return TaskProvider.runFunctionWithException(() -> _printerService.updatePrinterState()).orElse(0);
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    /**
     * Print text.
     *
     * @param text the text
     * @return the outcome of the operation
     */
    Boolean printText(String text) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.printText(text, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Setting alignment mode, which has an impact on the post-printing,
     * <p>
     * unless the initialization alignment:
     * <p>
     * 0: to the left,
     * <p>
     * 1: center,
     * <p>
     * 2: to the right
     *
     * @param alignment the alignment int
     * @return the outcome of the operation
     */
    public Boolean setAlignment(Integer alignment) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.setAlignment(alignment, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Sets font size.
     *
     * @param fontSize the font size
     * @return the outcome of the operation
     */
    public Boolean setFontSize(int fontSize) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.setFontSize(fontSize, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Print column.
     *
     * @param stringColumns   the string columns
     * @param columnWidth     the column width
     * @param columnAlignment the column alignment
     * @return the outcome of the operation
     */
    public Boolean printColumn(String[] stringColumns, int[] columnWidth, int[] columnAlignment) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.printColumnsText(stringColumns, columnWidth, columnAlignment, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Print image.
     *
     * @param bitmap the bitmap
     * @return the outcome of the operation
     */
    public Boolean printImage(Bitmap bitmap) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.printBitmap(bitmap, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Cut paper boolean.
     *
     * @return the outcome of the operation
     */
    public Boolean cutPaper() {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.cutPaper(this._callback()));
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Get the printing machine board serial number.
     *
     * @return the printer serial number, "DEFAULT" if is null, "NOT FOUND" if error
     */
    @SuppressLint("NewApi")
    public String getPrinterSerialNumber() {
        try {
            return TaskProvider.runFunctionWithException(() -> _printerService.getPrinterSerialNo()).orElse("DEFAULT");
        } catch (RemoteException | NullPointerException e) {
            return "NOT FOUND";
        }
    }

    /**
     * Gets printer version.
     * <p>
     * The desktop printer can switch the print paper type.
     * <p>
     * 0: 80mm
     * <p>
     * 1: 58mm
     * <p>
     * Support version: T1-V2.4.0 or above T2, S2-V1.0.5 or above.
     *
     * @return the printer serial number, "DEFAULT" if is null, "NOT FOUND" if error
     */
    @SuppressLint("NewApi")
    public String getPrinterVersion() {
        try {
            return TaskProvider.runFunctionWithException(() -> _printerService.getPrinterVersion()).orElse("DEFAULT");
        } catch (RemoteException | NullPointerException e) {
            return "NOT FOUND";
        }
    }

    /**
     * Gets printer paper size.
     * <p>
     * 0: 80mm
     * <p>
     * 1: 58mm
     *
     * @return the printer paper size int
     */
    @SuppressLint("NewApi")
    public int getPrinterPaperSize() {
        try {
            return TaskProvider.runFunctionWithException(() -> _printerService.getPrinterPaper()).orElse(1);
        } catch (RemoteException e) {
            return 1; // error;
        } catch (NullPointerException e) {
            return 1;
        }
    }

    /**
     * Get the current printer mode.
     *
     * @return 0 common mode 1 black label mode, else 3
     */
    @SuppressLint("NewApi")
    public int getPrinterMode() {
        try {
            return TaskProvider.runFunctionWithException(() -> _printerService.getPrinterMode()).orElse(3);
        } catch (RemoteException e) {
            return 3; // error;
        } catch (NullPointerException e) {
            return 3;
        }
    }

    /**
     * Open the money cabinet.
     *
     * @return the outcome of the operation
     */
    public Boolean openDrawer() {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.openDrawer(this._callback()));
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Drawer status.
     *
     * @return is drawer connected: true, disconnected: false
     */
    @SuppressLint("NewApi")
    public Boolean drawerIsConnected() {
        try {
            return TaskProvider.runFunctionWithException(() -> _printerService.getDrawerStatus()).orElse(false);
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Cumulative number of times the money cabinet is opened.
     *
     * @return int open number of times
     */
    @SuppressLint("NewApi")
    public int timesOpened() {
        try {
            return TaskProvider.runFunctionWithException(() -> _printerService.getOpenDrawerTimes()).orElse(0);
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    /**
     * Line wrap.
     *
     * @param lines the number of paper line
     * @return the outcome of the operation
     */
    public boolean lineWrap(int lines) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.lineWrap(lines, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Send raw data.
     *
     * @param bytes the bytes to send
     * @return the outcome of the operation
     */
    public boolean sendRaw(byte[] bytes) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.sendRAWData(bytes, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Entering the transaction mode, all printing calls will cache.
     *
     * @param clear the clear
     * @return the outcome of the operation
     */
    public boolean enterPrinterBuffer(Boolean clear) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.enterPrinterBuffer(clear));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Printing buffer content.
     *
     * @return the outcome of the operation
     */
    public boolean commitPrinterBuffer() {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.commitPrinterBuffer());
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Exit buffer mode.
     *
     * @param clear whether to print the buffer content
     * @return the outcome of the operation
     */
    public boolean exitPrinterBuffer(Boolean clear) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.exitPrinterBuffer(clear));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }


    /**
     * Print qr code.
     *
     * @param data       the data
     * @param moduleSize the moduleSize
     * @param errorLevel the errorLevel
     * @return the outcome of the operation
     */
    public boolean printQRCode(String data, int moduleSize, int errorLevel) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.printQRCode(data, moduleSize, errorLevel, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Print bar code.
     *
     * @param data         the data
     * @param barcodeType  the barcode type
     * @param textPosition the text position
     * @param width        the width
     * @param height       the height
     * @return the outcome of the operation
     */
    public boolean printBarCode(String data, int barcodeType, int textPosition, int width, int height) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.printBarCode(data, barcodeType, height, width, textPosition, this._callback()));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    private ICallback _callback() {
        return new ICallback() {
            @Override
            public void onRunResult(boolean isSuccess) {
            }

            @Override
            public void onReturnString(String result) {
            }

            @Override
            public void onRaiseException(int code, String msg) {
            }

            @Override
            public void onPrintResult(int code, String msg) {
            }

            @Override
            public IBinder asBinder() {
                return null;
            }
        };
    }

    // LCD METHODS

    /**
     * Send lcd command.
     *
     * @param flag the flag
     * @return the outcome of the operation
     */
    public boolean sendLCDCommand(int flag) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.sendLCDCommand(flag));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Send lcd string.
     *
     * @param string the string
     * @return the outcome of the operation
     */
    public boolean sendLCDString(String string) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.sendLCDString(string, this._lcdCallback()));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Send lcd bitmap.
     *
     * @param bitmap the bitmap
     * @return the outcome of the operation
     */
    public boolean sendLCDBitmap(android.graphics.Bitmap bitmap) {
        try {
            TaskProvider.runFunctionWithException(() -> _printerService.sendLCDBitmap(bitmap, this._lcdCallback()));
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Send lcd double string.
     *
     * @param topText    the top text
     * @param bottomText the bottom text
     * @return the outcome of the operation
     */
    public boolean sendLCDDoubleString(String topText, String bottomText) {
        try {
            _printerService.sendLCDDoubleString(topText, bottomText, this._lcdCallback());
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Send lcd fill string.
     *
     * @param string the string
     * @param size   the size
     * @param fill   the fill
     * @return the outcome of the operation
     */
    public boolean sendLCDFillString(String string, int size, boolean fill) {
        try {
            _printerService.sendLCDFillString(string, size, fill, this._lcdCallback());
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    /**
     * Show multi lines text on LCD.
     *
     * @param text  Text lines.
     * @param align The weight of the solid content of each line. Like flex.
     * @return the outcome of the operation
     */
    public boolean sendLCDMultiString(String[] text, int[] align) {
        try {
            _printerService.sendLCDMultiString(text, align, this._lcdCallback());
            return true;
        } catch (RemoteException | NullPointerException ignored) {
            return false;
        }
    }

    private ILcdCallback _lcdCallback() {
        return new ILcdCallback() {
            @Override
            public IBinder asBinder() {
                return null;
            }

            @Override
            public void onRunResult(boolean show) {
            }
        };
    }
}
