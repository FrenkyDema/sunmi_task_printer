package dev.francescodema.sunmi_task_printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * SunmiTaskPrinterPlugin
 */
public class SunmiTaskPrinterPlugin implements FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private SunmiTaskPrinterMethod sunmiTaskPrinterMethod;


    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "sunmi_task_printer");
        sunmiTaskPrinterMethod =
                new SunmiTaskPrinterMethod(flutterPluginBinding.getApplicationContext());
        channel.setMethodCallHandler(this);
    }

    /**
     * @noinspection DataFlowIssue
     */
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        try {
            switch (call.method) {
                case "getPlatformVersion":
                    result.success("Android " + android.os.Build.VERSION.RELEASE);
                    break;
                case "BIND_PRINTER_SERVICE":
                    sunmiTaskPrinterMethod.bindPrinterService();
                    result.success(true);

                    break;
                case "UNBIND_PRINTER_SERVICE":
                    sunmiTaskPrinterMethod.unbindPrinterService();
                    result.success(true);

                    break;
                case "INIT_PRINTER":
                    // ICallback callback = call.argument("callback");
                    sunmiTaskPrinterMethod.initPrinter();
                    result.success(true);
                    break;
                case "GET_UPDATE_PRINTER":
                    final int status_code = sunmiTaskPrinterMethod.updatePrinter();

                    String status_msg;

                    // response printer status
                    switch (status_code) {
                        case 0:
                            status_msg = "ERROR";
                            break;
                        case 1:
                            status_msg = "NORMAL";
                            break;
                        case 2:
                            status_msg = "ABNORMAL_COMMUNICATION";
                            break;
                        case 3:
                            status_msg = "OUT_OF_PAPER";
                            break;
                        case 4:
                            status_msg = "PREPARING";
                            break;
                        case 5:
                            status_msg = "OVERHEATED";
                            break;
                        case 6:
                            status_msg = "OPEN_THE_LID";
                            break;
                        case 7:
                            status_msg = "PAPER_CUTTER_ABNORMAL";
                            break;
                        case 8:
                            status_msg = "PAPER_CUTTER_RECOVERED";
                            break;
                        case 9:
                            status_msg = "NO_BLACK_MARK";
                            break;
                        case 505:
                            status_msg = "NO_PRINTER_DETECTED";
                            break;
                        case 507:
                            status_msg = "FAILED_TO_UPGRADE_FIRMWARE";
                            break;
                        default:
                            status_msg = "EXCEPTION";
                    }

                    result.success(status_msg);
                    break;
                case "PRINT_TEXT":
                    String text = call.argument("text");

                    sunmiTaskPrinterMethod.printText(text);
                    result.success(true);

                    break;
                case "RAW_DATA":
                    sunmiTaskPrinterMethod.sendRaw((byte[]) call.argument("data"));
                    result.success(true);
                    break;
                case "PRINT_QRCODE":
                    String data = call.argument("data");
                    int moduleSize = call.argument("modulesize");
                    int errorLevel = call.argument("errorlevel");
                    sunmiTaskPrinterMethod.printQRCode(data, moduleSize, errorLevel);
                    result.success(true);
                    break;
                case "PRINT_BARCODE":
                    String barCodeData = call.argument("data");
                    int barcodeType = call.argument("barcodeType");
                    int textPosition = call.argument("textPosition");
                    int width = call.argument("width");
                    int height = call.argument("height");
                    sunmiTaskPrinterMethod.printBarCode(
                            barCodeData,
                            barcodeType,
                            textPosition,
                            width,
                            height
                    );
                    sunmiTaskPrinterMethod.lineWrap(1);

                    result.success(true);
                    break;
                // void printBarCode(String data, int symbology, int height, int width, int textposition,  in ICallback callback);

                case "LINE_WRAP":
                    int lines = call.argument("lines");
                    sunmiTaskPrinterMethod.lineWrap(lines);
                    result.success(true);
                    break;
                case "FONT_SIZE":
                    int fontSize = call.argument("size");
                    result.success(sunmiTaskPrinterMethod.setFontSize(fontSize));
                    break;
                case "SET_ALIGNMENT":
                    int alignment = call.argument("alignment");
                    sunmiTaskPrinterMethod.setAlignment(alignment);
                    result.success(true);
                    break;
                case "PRINT_IMAGE":
                    byte[] bytes = call.argument("bitmap");
                    assert bytes != null;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    result.success(sunmiTaskPrinterMethod.printImage(bitmap));
                    break;
                case "GET_PRINTER_MODE":
                    final int mode_code = sunmiTaskPrinterMethod.getPrinterMode();

                    String mode_desc;

                    // response printer status
                    switch (mode_code) {
                        case 0:
                            mode_desc = "NORMAL_MODE";
                            break;
                        case 1:
                            mode_desc = "BLACK_LABEL_MODE";
                            break;
                        case 2:
                            mode_desc = "LABEL_MODE";
                            break;
                        case 3:
                            mode_desc = "ERROR";
                            break;
                        default:
                            mode_desc = "EXCEPTION";
                    }

                    result.success(mode_desc);
                    break;
                case "ENTER_PRINTER_BUFFER":
                    Boolean clearEnter = call.argument("clearEnter");
                    sunmiTaskPrinterMethod.enterPrinterBuffer(clearEnter);
                    result.success(true);

                    break;
                case "COMMIT_PRINTER_BUFFER":
                    sunmiTaskPrinterMethod.commitPrinterBuffer();
                    result.success(true);
                    break;
                case "CUT_PAPER":
                    result.success(sunmiTaskPrinterMethod.cutPaper());
                    break;
                case "OPEN_DRAWER":
                    result.success(sunmiTaskPrinterMethod.openDrawer());
                    break;

                case "DRAWER_OPENED":
                    result.success(sunmiTaskPrinterMethod.timesOpened());
                    break;

                case "DRAWER_STATUS":
                    result.success(sunmiTaskPrinterMethod.drawerStatus());
                    break;
                case "PRINT_ROW":
                    String colsStr = call.argument("cols");

                    try {
                        JSONArray cols = new JSONArray(colsStr);
                        String[] colsText = new String[cols.length()];
                        int[] colsWidth = new int[cols.length()];
                        int[] colsAlign = new int[cols.length()];
                        for (int i = 0; i < cols.length(); i++) {
                            JSONObject col = cols.getJSONObject(i);
                            String textColumn = col.getString("text");
                            int widthColumn = col.getInt("width");
                            int alignColumn = col.getInt("align");
                            colsText[i] = textColumn;
                            colsWidth[i] = widthColumn;
                            colsAlign[i] = alignColumn;
                        }

                        result.success(sunmiTaskPrinterMethod.printColumn(colsText, colsWidth, colsAlign));
                    } catch (Exception err) {
                        Log.d("SunmiPrinter", Objects.requireNonNull(err.getMessage()));
                    }
                    break;
                case "EXIT_PRINTER_BUFFER":
                    Boolean clearExit = call.argument("clearExit");
                    sunmiTaskPrinterMethod.exitPrinterBuffer(clearExit);
                    result.success(true);
                    break;
                case "PRINTER_SERIAL_NUMBER":
                    final String serial = sunmiTaskPrinterMethod.getPrinterSerialNo();
                    result.success(serial);
                    break;
                case "PRINTER_VERSION":
                    final String printer_version = sunmiTaskPrinterMethod.getPrinterVersion();
                    result.success(printer_version);
                    break;
                case "PAPER_SIZE":
                    final int paper = sunmiTaskPrinterMethod.getPrinterPaper();
                    result.success(paper);
                    break;

                // LCD METHODS
                case "LCD_COMMAND":
                    int flag = call.argument("flag");
                    sunmiTaskPrinterMethod.sendLCDCommand(flag);
                    result.success(true);
                    break;
                case "LCD_STRING":
                    String lcdString = call.argument("string");
                    sunmiTaskPrinterMethod.sendLCDString(lcdString);
                    result.success(true);
                    break;
                case "LCD_BITMAP":
                    byte[] lcdBitmapData = call.argument("bitmap");
                    Bitmap lcdBitmap = BitmapFactory.decodeByteArray(
                            lcdBitmapData, 0, lcdBitmapData.length);
                    sunmiTaskPrinterMethod.sendLCDBitmap(lcdBitmap);
                    result.success(true);
                    break;
                case "LCD_DOUBLE_STRING":
                    String topText = call.argument("topText");
                    String bottomText = call.argument("bottomText");
                    sunmiTaskPrinterMethod.sendLCDDoubleString(topText, bottomText);
                    result.success(true);
                    break;
                case "LCD_FILL_STRING":
                    String lcdFillString = call.argument("string");
                    int lcdFillSize = call.argument("size");
                    boolean lcdFill = call.argument("fill");
                    sunmiTaskPrinterMethod.sendLCDFillString(lcdFillString, lcdFillSize, lcdFill);
                    result.success(true);
                    break;
                case "LCD_MULTI_STRING":
                    ArrayList<String> lcdTextAL = call.argument("text");
                    assert lcdTextAL != null;
                    String[] lcdText = Utilities.arrayListToString(lcdTextAL);
                    ArrayList<Integer> lcdAlignAL = call.argument("align");
                    assert lcdAlignAL != null;
                    int[] lcdAlign = Utilities.arrayListToIntList(lcdAlignAL);
                    sunmiTaskPrinterMethod.sendLCDMultiString(lcdText, lcdAlign);
                    result.success(true);
                    break;

                default:
                    result.notImplemented();
                    break;
            }
        } catch (NullPointerException e) {
            result.error(e.toString(), e.getMessage(), e);
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}
