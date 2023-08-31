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
        sunmiTaskPrinterMethod = new SunmiTaskPrinterMethod(flutterPluginBinding.getApplicationContext());
        channel.setMethodCallHandler(this);
    }

    /**
     * @noinspection DataFlowIssue
     */
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        try {
            switch (call.method) {
                case "getPlatformVersion" ->
                        result.success("Android " + android.os.Build.VERSION.RELEASE);
                case "BIND_SERVICE" -> {
                    sunmiTaskPrinterMethod.bindService();
                    result.success(true);
                }
                case "UNBIND_SERVICE" -> {
                    sunmiTaskPrinterMethod.unbindService();
                    result.success(true);
                }
                case "INIT_PRINTER" -> {
                    // ICallback callback = call.argument("callback");
                    sunmiTaskPrinterMethod.initPrinter();
                    result.success(true);
                }
                case "GET_UPDATE_PRINTER" -> {
                    final int status_code = sunmiTaskPrinterMethod.updatePrinter();
                    String status_msg = switch (status_code) {
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

                    // response printer status
                    result.success(status_msg);
                }
                case "PRINT_TEXT" -> {
                    String text = call.argument("text");
                    sunmiTaskPrinterMethod.printText(text);
                    result.success(true);
                }
                case "RAW_DATA" -> {
                    sunmiTaskPrinterMethod.sendRaw(call.argument("data"));
                    result.success(true);
                }
                case "PRINT_QRCODE" -> {
                    String data = call.argument("data");
                    int moduleSize = call.argument("modulesize");
                    int errorLevel = call.argument("errorlevel");
                    sunmiTaskPrinterMethod.printQRCode(data, moduleSize, errorLevel);
                    result.success(true);
                }
                case "PRINT_BARCODE" -> {
                    String barCodeData = call.argument("data");
                    int barcodeType = call.argument("barcodeType");
                    int textPosition = call.argument("textPosition");
                    int width = call.argument("width");
                    int height = call.argument("height");
                    sunmiTaskPrinterMethod.printBarCode(barCodeData, barcodeType, textPosition, width, height);
                    sunmiTaskPrinterMethod.lineWrap(1);
                    result.success(true);
                }
                // void printBarCode(String data, int symbology, int height, int width, int textposition,  in ICallback callback);

                case "LINE_WRAP" -> {
                    int lines = call.argument("lines");
                    sunmiTaskPrinterMethod.lineWrap(lines);
                    result.success(true);
                }
                case "FONT_SIZE" -> {
                    int fontSize = call.argument("size");
                    result.success(sunmiTaskPrinterMethod.setFontSize(fontSize));
                }
                case "SET_ALIGNMENT" -> {
                    int alignment = call.argument("alignment");
                    sunmiTaskPrinterMethod.setAlignment(alignment);
                    result.success(true);
                }
                case "PRINT_IMAGE" -> {
                    byte[] bytes = call.argument("bitmap");
                    assert bytes != null;
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    result.success(sunmiTaskPrinterMethod.printImage(bitmap));
                }
                case "GET_PRINTER_MODE" -> {
                    final int mode_code = sunmiTaskPrinterMethod.getPrinterMode();
                    String mode_desc = switch (mode_code) {
                        case 0 -> "NORMAL_MODE";
                        case 1 -> "BLACK_LABEL_MODE";
                        case 2 -> "LABEL_MODE";
                        case 3 -> "ERROR";
                        default -> "EXCEPTION";
                    };

                    // response printer status
                    result.success(mode_desc);
                }
                case "ENTER_PRINTER_BUFFER" -> {
                    Boolean clearEnter = call.argument("clearEnter");
                    sunmiTaskPrinterMethod.enterPrinterBuffer(clearEnter);
                    result.success(true);
                }
                case "COMMIT_PRINTER_BUFFER" -> {
                    sunmiTaskPrinterMethod.commitPrinterBuffer();
                    result.success(true);
                }
                case "CUT_PAPER" -> result.success(sunmiTaskPrinterMethod.cutPaper());
                case "OPEN_DRAWER" -> result.success(sunmiTaskPrinterMethod.openDrawer());
                case "DRAWER_OPENED" -> result.success(sunmiTaskPrinterMethod.timesOpened());
                case "DRAWER_STATUS" -> result.success(sunmiTaskPrinterMethod.drawerStatus());
                case "PRINT_ROW" -> {
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
                }
                case "EXIT_PRINTER_BUFFER" -> {
                    Boolean clearExit = call.argument("clearExit");
                    sunmiTaskPrinterMethod.exitPrinterBuffer(clearExit);
                    result.success(true);
                }
                case "PRINTER_SERIAL_NUMBER" -> {
                    final String serial = sunmiTaskPrinterMethod.getPrinterSerialNo();
                    result.success(serial);
                }
                case "PRINTER_VERSION" -> {
                    final String printer_version = sunmiTaskPrinterMethod.getPrinterVersion();
                    result.success(printer_version);
                }
                case "PAPER_SIZE" -> {
                    final int paper = sunmiTaskPrinterMethod.getPrinterPaper();
                    result.success(paper);
                }

                // LCD METHODS
                case "LCD_COMMAND" -> {
                    int flag = call.argument("flag");
                    sunmiTaskPrinterMethod.sendLCDCommand(flag);
                    result.success(true);
                }
                case "LCD_STRING" -> {
                    String lcdString = call.argument("string");
                    sunmiTaskPrinterMethod.sendLCDString(lcdString);
                    result.success(true);
                }
                case "LCD_BITMAP" -> {
                    byte[] lcdBitmapData = call.argument("bitmap");
                    Bitmap lcdBitmap = BitmapFactory.decodeByteArray(lcdBitmapData, 0, lcdBitmapData.length);
                    sunmiTaskPrinterMethod.sendLCDBitmap(lcdBitmap);
                    result.success(true);
                }
                case "LCD_DOUBLE_STRING" -> {
                    String topText = call.argument("topText");
                    String bottomText = call.argument("bottomText");
                    sunmiTaskPrinterMethod.sendLCDDoubleString(topText, bottomText);
                    result.success(true);
                }
                case "LCD_FILL_STRING" -> {
                    String lcdFillString = call.argument("string");
                    int lcdFillSize = call.argument("size");
                    boolean lcdFill = call.argument("fill");
                    sunmiTaskPrinterMethod.sendLCDFillString(lcdFillString, lcdFillSize, lcdFill);
                    result.success(true);
                }
                case "LCD_MULTI_STRING" -> {
                    ArrayList<String> lcdTextAL = call.argument("text");
                    assert lcdTextAL != null;
                    String[] lcdText = Utilities.arrayListToString(lcdTextAL);
                    ArrayList<Integer> lcdAlignAL = call.argument("align");
                    assert lcdAlignAL != null;
                    int[] lcdAlign = Utilities.arrayListToIntList(lcdAlignAL);
                    sunmiTaskPrinterMethod.sendLCDMultiString(lcdText, lcdAlign);
                    result.success(true);
                }
                default -> result.notImplemented();
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
