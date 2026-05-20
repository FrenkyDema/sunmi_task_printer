package dev.francescodema.sunmi_task_printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * The main Flutter plugin class that handles communication between Dart and native Android.
 * It intercepts method calls from the MethodChannel and routes them to the SunmiTaskPrinterMethod instance.
 */
public class SunmiTaskPrinterPlugin implements FlutterPlugin, MethodCallHandler {
    private MethodChannel channel;
    private SunmiTaskPrinterMethod sunmiTaskPrinterMethod;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "sunmi_task_printer");
        sunmiTaskPrinterMethod = new SunmiTaskPrinterMethod(flutterPluginBinding.getApplicationContext());
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        switch (call.method) {
            case "getPlatformVersion" ->
                    result.success("Android " + android.os.Build.VERSION.RELEASE);
            case "BIND_SERVICE" -> sunmiTaskPrinterMethod.bindService(result);
            case "UNBIND_SERVICE" -> sunmiTaskPrinterMethod.unbindService(result);
            case "INIT_PRINTER" -> sunmiTaskPrinterMethod.initPrinter(result);
            case "GET_UPDATE_PRINTER" -> sunmiTaskPrinterMethod.updatePrinter(result);
            case "PRINT_TEXT" -> {
                String text = call.argument("text");
                sunmiTaskPrinterMethod.printText(text, result);
            }
            case "RAW_DATA" -> {
                byte[] data = call.argument("data");
                sunmiTaskPrinterMethod.sendRaw(data, result);
            }
            case "PRINT_QRCODE" -> {
                String data = call.argument("data");
                Integer moduleSize = call.argument("modulesize");
                Integer errorLevel = call.argument("errorlevel");
                sunmiTaskPrinterMethod.printQRCode(data, moduleSize != null ? moduleSize : 4, errorLevel != null ? errorLevel : 0, result);
            }
            case "PRINT_BARCODE" -> {
                String data = call.argument("data");
                Integer barcodeType = call.argument("barcodeType");
                Integer textPosition = call.argument("textPosition");
                Integer width = call.argument("width");
                Integer height = call.argument("height");
                sunmiTaskPrinterMethod.printBarCode(data, barcodeType != null ? barcodeType : 0, textPosition != null ? textPosition : 0, width != null ? width : 2, height != null ? height : 162, result);
            }
            case "LINE_WRAP" -> {
                Integer lines = call.argument("lines");
                sunmiTaskPrinterMethod.lineWrap(lines != null ? lines : 1, result);
            }
            case "FONT_SIZE" -> {
                Integer size = call.argument("size");
                sunmiTaskPrinterMethod.setFontSize(size != null ? size : 24, result);
            }
            case "SET_ALIGNMENT" -> {
                Integer alignment = call.argument("alignment");
                sunmiTaskPrinterMethod.setAlignment(alignment != null ? alignment : 0, result);
            }
            case "PRINT_IMAGE" -> {
                byte[] bytes = call.argument("bitmap");
                if (bytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    sunmiTaskPrinterMethod.printImage(bitmap, result);
                } else {
                    result.error("INVALID_ARGUMENT", "Bitmap data cannot be null", null);
                }
            }
            case "GET_PRINTER_MODE" -> sunmiTaskPrinterMethod.getPrinterMode(result);
            case "ENTER_PRINTER_BUFFER" -> {
                Boolean clearEnter = call.argument("clearEnter");
                sunmiTaskPrinterMethod.enterPrinterBuffer(clearEnter != null ? clearEnter : false, result);
            }
            case "COMMIT_PRINTER_BUFFER" -> sunmiTaskPrinterMethod.commitPrinterBuffer(result);
            case "EXIT_PRINTER_BUFFER" -> {
                Boolean clearExit = call.argument("clearExit");
                sunmiTaskPrinterMethod.exitPrinterBuffer(clearExit != null ? clearExit : false, result);
            }
            case "CUT_PAPER" -> sunmiTaskPrinterMethod.cutPaper(result);
            case "OPEN_DRAWER" -> sunmiTaskPrinterMethod.openDrawer(result);
            case "DRAWER_OPENED" -> sunmiTaskPrinterMethod.timesOpened(result);
            case "DRAWER_STATUS" -> sunmiTaskPrinterMethod.drawerIsConnected(result);
            case "PRINT_ROW" -> {
                List<Map<String, Object>> cols = call.argument("cols");
                if (cols != null) {
                    String[] colsText = new String[cols.size()];
                    int[] colsWidth = new int[cols.size()];
                    int[] colsAlign = new int[cols.size()];

                    for (int i = 0; i < cols.size(); i++) {
                        Map<String, Object> col = cols.get(i);
                        colsText[i] = (String) col.get("text");

                        // Safely unbox integers to prevent NullPointerExceptions
                        Object widthObj = col.get("width");
                        Object alignObj = col.get("align");
                        colsWidth[i] = widthObj instanceof Integer ? (Integer) widthObj : 0;
                        colsAlign[i] = alignObj instanceof Integer ? (Integer) alignObj : 0;
                    }
                    sunmiTaskPrinterMethod.printColumn(colsText, colsWidth, colsAlign, result);
                } else {
                    result.error("INVALID_ARGUMENT", "Columns list cannot be null", null);
                }
            }
            case "PRINTER_SERIAL_NUMBER" -> sunmiTaskPrinterMethod.getPrinterSerialNumber(result);
            case "PRINTER_VERSION" -> sunmiTaskPrinterMethod.getPrinterVersion(result);
            case "PAPER_SIZE" -> sunmiTaskPrinterMethod.getPrinterPaperSize(result);
            case "LCD_COMMAND" -> {
                Integer flag = call.argument("flag");
                sunmiTaskPrinterMethod.sendLCDCommand(flag != null ? flag : 1, result);
            }
            case "LCD_STRING" -> {
                String string = call.argument("string");
                sunmiTaskPrinterMethod.sendLCDString(string, result);
            }
            case "LCD_BITMAP" -> {
                byte[] lcdBitmapData = call.argument("bitmap");
                if (lcdBitmapData != null) {
                    Bitmap lcdBitmap = BitmapFactory.decodeByteArray(lcdBitmapData, 0, lcdBitmapData.length);
                    sunmiTaskPrinterMethod.sendLCDBitmap(lcdBitmap, result);
                } else {
                    result.error("INVALID_ARGUMENT", "LCD Bitmap data cannot be null", null);
                }
            }
            case "LCD_DOUBLE_STRING" -> {
                String topText = call.argument("topText");
                String bottomText = call.argument("bottomText");
                sunmiTaskPrinterMethod.sendLCDDoubleString(topText, bottomText, result);
            }
            case "LCD_FILL_STRING" -> {
                String string = call.argument("string");
                Integer size = call.argument("size");
                Boolean fill = call.argument("fill");
                sunmiTaskPrinterMethod.sendLCDFillString(string, size != null ? size : 16, fill != null ? fill : false, result);
            }
            case "LCD_MULTI_STRING" -> {
                ArrayList<String> lcdTextAL = call.argument("text");
                ArrayList<Integer> lcdAlignAL = call.argument("align");
                if (lcdTextAL != null && lcdAlignAL != null) {
                    String[] lcdText = Utilities.arrayListToString(lcdTextAL);
                    int[] lcdAlign = Utilities.arrayListToIntList(lcdAlignAL);
                    sunmiTaskPrinterMethod.sendLCDMultiString(lcdText, lcdAlign, result);
                } else {
                    result.error("INVALID_ARGUMENT", "LCD text or alignment array cannot be null", null);
                }
            }
            default -> result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }
}