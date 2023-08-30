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

    private final String TAG = SunmiTaskPrinterMethod.class.getSimpleName();
    private final ArrayList<Boolean> _printingText = new ArrayList<>();
    private IWoyouService _woyouService;
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
                _woyouService = IWoyouService.Stub.asInterface(service);
                String serviceVersion = _woyouService.getServiceVersion();
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
    public void bindPrinterService() {
        Intent intent = new Intent();
        intent.setPackage("woyou.aidlservice.jiuiv5");
        intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
        _context.bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind printer service.
     */
    public void unbindPrinterService() {
        _context.unbindService(connService);
    }

    /**
     * Init printer.
     */
    public void initPrinter() {
        try {
            _woyouService.printerInit(this._callback());
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
            return _woyouService.updatePrinterState();
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
            _woyouService.printText(text, this._callback());
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
            _woyouService.setAlignment(alignment, this._callback());
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
            _woyouService.setFontSize(fontSize, this._callback());
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
            _woyouService.sendRAWData(command, this._callback());
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

            _woyouService.printColumnsText(stringColumns, columnWidth, columnAlignment, this._callback());

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
            _woyouService.printBitmap(bitmap, this._callback());
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
            _woyouService.cutPaper(this._callback());
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
            return _woyouService.getPrinterSerialNo();
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
            return _woyouService.getPrinterVersion();
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
            return _woyouService.getPrinterPaper();
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
            return _woyouService.getPrinterMode();
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
            _woyouService.openDrawer(this._callback());
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
            return _woyouService.getDrawerStatus();
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
            return _woyouService.getOpenDrawerTimes();
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
            _woyouService.lineWrap(lines, this._callback());
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
            this._woyouService.sendRAWData(bytes, this._callback());
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
            this._woyouService.enterPrinterBuffer(clear);
        } catch (RemoteException | NullPointerException ignored) {
        }
    }

    /**
     * Commit printer buffer.
     */
    public void commitPrinterBuffer() {
        try {
            this._woyouService.commitPrinterBuffer();
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
            this._woyouService.exitPrinterBuffer(clear);
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
            _woyouService.setAlignment(alignment, this._callback());
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
            _woyouService.printQRCode(data, modulesize, errorlevel, this._callback());
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
            _woyouService.printBarCode(data, barcodeType, height, width, textPosition, this._callback());
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
            _woyouService.sendLCDCommand(flag);
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
            _woyouService.sendLCDString(string, this._lcdCallback());
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
            _woyouService.sendLCDBitmap(bitmap, this._lcdCallback());
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
            _woyouService.sendLCDDoubleString(topText, bottomText, this._lcdCallback());
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
            _woyouService.sendLCDFillString(string, size, fill, this._lcdCallback());
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
            _woyouService.sendLCDMultiString(text, align, this._lcdCallback());
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
