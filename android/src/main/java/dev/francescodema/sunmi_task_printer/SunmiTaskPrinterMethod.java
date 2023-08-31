package dev.francescodema.sunmi_task_printer;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import java.util.ArrayList;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.ILcdCallback;
import woyou.aidlservice.jiuiv5.IWoyouService;


/**
 * The type Sunmi task printer method.
 */
public class SunmiTaskPrinterMethod {


    private static final String SERVICE_PACKAGE = "woyou.aidlservice.jiuiv5";
    private static final String SERVICE_ACTION = "woyou.aidlservice.jiuiv5.IWoyouService";
    private final String TAG = SunmiTaskPrinterMethod.class.getSimpleName();
    private final ArrayList<Boolean> _printingText = new ArrayList<>();
    private IWoyouService printerService;
    private final ServiceConnection connService;
    private final Context _context;

    /**
     * Instantiates a new Sunmi task printer method.
     *
     * @param context the context
     */
    public SunmiTaskPrinterMethod(Context context) {
        this._context = context;

        connService = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                try {
                    printerService = IWoyouService.Stub.asInterface(service);
                    String serviceVersion = printerService.getServiceVersion();
                    Toast.makeText(
                            _context,
                            "Sunmi Printer Service Connected. Version :" + serviceVersion,
                            Toast.LENGTH_LONG
                    ).show();


                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {

                    Toast.makeText(
                            _context,
                            "Sunmi Printer Service Not Found",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(_context, "Sunmi Printer Service Disconnected", Toast.LENGTH_LONG).show();
            }
        };


    }


    /**
     * Bind printer service.
     */
    public void bindService() {
        Intent intent = new Intent();
        intent.setPackage(SERVICE_PACKAGE);
        intent.setAction(SERVICE_ACTION);
        _context.getApplicationContext().bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind printer service.
     */
    public void unbindService() {
        if (printerService != null) {
            _context.getApplicationContext().unbindService(connService);
            printerService = null;
        }
    }

    /**
     * Init printer.
     */
    public void initPrinter() {
        try {
            printerService.printerInit(this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Update printer int.
     *
     * @return the int
     */
    public int updatePrinter() {
        try {
            return printerService.updatePrinterState();
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    /**
     * Print text.
     *
     * @param text the text
     */
    public void printText(String text) {
        this._printingText.add(this._printText(text));
    }

    private Boolean _printText(String text) {
        try {
            printerService.printText(text, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Sets alignment.
     *
     * @param alignment the alignment
     * @return the alignment
     */
    public Boolean setAlignment(Integer alignment) {
        try {
            printerService.setAlignment(alignment, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Sets font size.
     *
     * @param fontSize the font size
     * @return the font size
     */
    public Boolean setFontSize(int fontSize) {
        try {
            printerService.setFontSize(fontSize, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Sets font bold.
     *
     * @param bold the bold
     * @return the font bold
     */
    public Boolean setFontBold(Boolean bold) {
        if (bold == null) {
            bold = false;
        }

        byte[] command = new byte[]{0x1B, 0x45, 0x1};

        if (!bold) {
            command = new byte[]{0x1B, 0x45, 0x0};
        }

        try {
            printerService.sendRAWData(command, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Print column boolean.
     *
     * @param stringColumns   the string columns
     * @param columnWidth     the column width
     * @param columnAlignment the column alignment
     * @return the boolean
     */
    public Boolean printColumn(String[] stringColumns, int[] columnWidth, int[] columnAlignment) {


        try {

            printerService.printColumnsText(stringColumns, columnWidth, columnAlignment, this._callback());

            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Print image boolean.
     *
     * @param bitmap the bitmap
     * @return the boolean
     */
    public Boolean printImage(Bitmap bitmap) {
        try {
            printerService.printBitmap(bitmap, this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Cut paper boolean.
     *
     * @return the boolean
     */
    public Boolean cutPaper() {
        try {
            printerService.cutPaper(this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Gets printer serial no.
     *
     * @return the printer serial no
     */
    public String getPrinterSerialNo() {

        try {
            return printerService.getPrinterSerialNo();
        } catch (RemoteException e) {
            return "";
        } catch (NullPointerException e) {
            return "NOT FOUND";
        }
    }

    /**
     * Gets printer version.
     *
     * @return the printer version
     */
    public String getPrinterVersion() {
        try {
            return printerService.getPrinterVersion();
        } catch (RemoteException e) {
            return "";// error;
        } catch (NullPointerException e) {
            return "NOT FOUND";
        }
    }

    /**
     * Gets printer paper.
     *
     * @return the printer paper
     */
    public int getPrinterPaper() {
        try {
            return printerService.getPrinterPaper();
        } catch (RemoteException e) {
            return 1; // error;
        } catch (NullPointerException e) {
            return 1;
        }
    }

    /**
     * Gets printer mode.
     *
     * @return the printer mode
     */
    public int getPrinterMode() {
        try {
            return printerService.getPrinterMode();
        } catch (RemoteException e) {
            return 3; // error;
        } catch (NullPointerException e) {
            return 3;
        }
    }

    /**
     * Open drawer boolean.
     *
     * @return the boolean
     */
    public Boolean openDrawer() {
        try {
            printerService.openDrawer(this._callback());
            return true;
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }


    /**
     * Drawer status boolean.
     *
     * @return the boolean
     */
    public Boolean drawerStatus() {
        try {
            return printerService.getDrawerStatus();
        } catch (RemoteException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Times opened int.
     *
     * @return the int
     */
    public int timesOpened() {
        try {
            return printerService.getOpenDrawerTimes();
        } catch (RemoteException | NullPointerException e) {
            return 0;
        }
    }

    /**
     * Line wrap.
     *
     * @param lines the lines
     */
    public void lineWrap(int lines) {
        try {
            printerService.lineWrap(lines, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Send raw.
     *
     * @param bytes the bytes
     */
    public void sendRaw(byte[] bytes) {
        try {
            this.printerService.sendRAWData(bytes, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Enter printer buffer.
     *
     * @param clear the clear
     */
    public void enterPrinterBuffer(Boolean clear) {
        try {
            this.printerService.enterPrinterBuffer(clear);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Commit printer buffer.
     */
    public void commitPrinterBuffer() {
        try {
            this.printerService.commitPrinterBuffer();
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Exit printer buffer.
     *
     * @param clear the clear
     */
    public void exitPrinterBuffer(Boolean clear) {
        try {
            this.printerService.exitPrinterBuffer(clear);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Sets alignment.
     *
     * @param alignment the alignment
     */
    public void setAlignment(int alignment) {
        try {
            printerService.setAlignment(alignment, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Print qr code.
     *
     * @param data       the data
     * @param modulesize the modulesize
     * @param errorlevel the errorlevel
     */
    public void printQRCode(String data, int modulesize, int errorlevel) {
        try {
            printerService.printQRCode(data, modulesize, errorlevel, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
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
     */
    public void printBarCode(String data, int barcodeType, int textPosition, int width, int height) {
        try {
            printerService.printBarCode(data, barcodeType, height, width, textPosition, this._callback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    private ICallback _callback() {
        return new ICallback() {
            @Override
            public void onRunResult(boolean isSuccess) throws RemoteException {
            }

            @Override
            public void onReturnString(String result) throws RemoteException {
            }

            @Override
            public void onRaiseException(int code, String msg) throws RemoteException {
            }

            @Override
            public void onPrintResult(int code, String msg) throws RemoteException {
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
     */
    public void sendLCDCommand(int flag) {
        try {
            printerService.sendLCDCommand(flag);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Send lcd string.
     *
     * @param string the string
     */
    public void sendLCDString(String string) {
        try {
            printerService.sendLCDString(string, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Send lcd bitmap.
     *
     * @param bitmap the bitmap
     */
    public void sendLCDBitmap(android.graphics.Bitmap bitmap) {
        try {
            printerService.sendLCDBitmap(bitmap, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Send lcd double string.
     *
     * @param topText    the top text
     * @param bottomText the bottom text
     */
    public void sendLCDDoubleString(String topText, String bottomText) {
        try {
            printerService.sendLCDDoubleString(topText, bottomText, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Send lcd fill string.
     *
     * @param string the string
     * @param size   the size
     * @param fill   the fill
     */
    public void sendLCDFillString(String string, int size, boolean fill) {
        try {
            printerService.sendLCDFillString(string, size, fill, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Show multi lines text on LCD.
     *
     * @param text  Text lines.
     * @param align The weight of the solid content of each line. Like flex.
     */
    public void sendLCDMultiString(String[] text, int[] align) {
        try {
            printerService.sendLCDMultiString(text, align, this._lcdCallback());
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    private ILcdCallback _lcdCallback() {
        return new ILcdCallback() {
            @Override
            public IBinder asBinder() {
                return null;
            }

            @Override
            public void onRunResult(boolean show) throws RemoteException {
            }
        };
    }
}
