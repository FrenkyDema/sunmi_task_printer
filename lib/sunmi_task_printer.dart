import 'dart:async';

import 'package:flutter/services.dart';

import 'column_maker.dart';
import 'enums.dart';
import 'sunmi_style.dart';

/// **SunmiTaskPrinter**
///
/// The orchestration core engine of the system layout layer.
/// Directs method commands through to the low-level platform architecture interface.
class SunmiTaskPrinter {
  static final Map<String, String> _printerStatus = {
    'ERROR': 'Something went wrong.',
    'NORMAL': 'Works normally',
    'ABNORMAL_COMMUNICATION': 'Abnormal communication',
    'OUT_OF_PAPER': 'Out of paper',
    'PREPARING': 'Preparing printer',
    'OVERHEATED': 'Overheated',
    'OPEN_THE_LID': 'Open the lid',
    'PAPER_CUTTER_ABNORMAL': 'The paper cutter is abnormal',
    'PAPER_CUTTER_RECOVERED': 'The paper cutter has been recovered',
    'NO_BLACK_MARK': 'No black mark had been detected',
    'NO_PRINTER_DETECTED': 'No printer had been detected',
    'FAILED_TO_UPGRADE_FIRMWARE': 'Failed to upgrade firmware',
    'EXCEPTION': 'Unknown Error code',
  };

  /// Structural map index values representing physical device line width specifications.
  static final List<int> _paperSize = [80, 58];

  /// Platform Channel link reference tracking target namespace instance.
  static const MethodChannel _channel = MethodChannel('sunmi_task_printer');

  /// Retrieves the host runtime OS version string name.
  Future<String?> getPlatformVersion() async {
    return await _channel.invokeMethod<String>('getPlatformVersion');
  }

  /// Establishes an explicit background interface connection with the remote service wrapper.
  /// This must execute before dispatching hardware layout configuration instructions.
  static Future<bool?> bindingService() async {
    return await _channel.invokeMethod<bool>('BIND_SERVICE');
  }

  /// Disconnects and releases the background active driver hook connection context framework safely.
  static Future<bool?> unbindingService() async {
    return await _channel.invokeMethod<bool>('UNBIND_SERVICE');
  }

  /// Resets printing configurations, parameters, weights, and text styling states back to base defaults.
  static Future<bool?> initPrinter() async {
    return await _channel.invokeMethod<bool>('INIT_PRINTER');
  }

  /// Evaluates and yields the exact real-time structural hardware health state code.
  static Future<PrinterStatus> getPrinterStatus() async {
    final String? status = await _channel.invokeMethod<String>(
      'GET_UPDATE_PRINTER',
    );
    switch (status) {
      case 'ERROR':
        return PrinterStatus.ERROR;
      case 'NORMAL':
        return PrinterStatus.NORMAL;
      case 'ABNORMAL_COMMUNICATION':
        return PrinterStatus.ABNORMAL_COMMUNICATION;
      case 'OUT_OF_PAPER':
        return PrinterStatus.OUT_OF_PAPER;
      case 'PREPARING':
        return PrinterStatus.PREPARING;
      case 'OVERHEATED':
        return PrinterStatus.OVERHEATED;
      case 'OPEN_THE_LID':
        return PrinterStatus.OPEN_THE_LID;
      case 'PAPER_CUTTER_ABNORMAL':
        return PrinterStatus.PAPER_CUTTER_ABNORMAL;
      case 'PAPER_CUTTER_RECOVERED':
        return PrinterStatus.PAPER_CUTTER_RECOVERED;
      case 'NO_BLACK_MARK':
        return PrinterStatus.NO_BLACK_MARK;
      case 'NO_PRINTER_DETECTED':
        return PrinterStatus.NO_PRINTER_DETECTED;
      case 'FAILED_TO_UPGRADE_FIRMWARE':
        return PrinterStatus.FAILED_TO_UPGRADE_FIRMWARE;
      case 'EXCEPTION':
        return PrinterStatus.EXCEPTION;
      default:
        return PrinterStatus.UNKNOWN;
    }
  }

  /// Almost the same as [getPrinterStatus], but returns a human-readable verbose string description.
  static Future<String?> getPrinterStatusWithVerbose() async {
    final String? status = await _channel.invokeMethod<String>(
      'GET_UPDATE_PRINTER',
    );
    return _printerStatus[status ?? 'EXCEPTION'];
  }

  /// Evaluates the printing paper run mode setup configuration context state.
  static Future<PrinterMode> getPrinterMode() async {
    final String? mode = await _channel.invokeMethod<String>(
      'GET_PRINTER_MODE',
    );
    switch (mode) {
      case 'NORMAL_MODE':
        return PrinterMode.NORMAL_MODE;
      case 'BLACK_LABEL_MODE':
        return PrinterMode.BLACK_LABEL_MODE;
      case 'LABEL_MODE':
        return PrinterMode.LABEL_MODE;
      default:
        return PrinterMode.UNKNOWN;
    }
  }

  /// Dispatches plain text layout print strings over to the native core pipeline safely.
  static Future<void> printText(String text, {SunmiStyle? style}) async {
    if (style != null) {
      if (style.align != null) {
        await setAlignment(style.align!);
      }
      if (style.fontSize != null) {
        await setFontSize(style.fontSize!);
      }
      if (style.bold == true) {
        await bold();
      }
    }
    await _channel.invokeMethod("PRINT_TEXT", {"text": '$text\n'});
    await initPrinter();
  }

  /// Spits out dynamic complex tables using raw list maps instead of structural string JSON conversions.
  static Future<void> printRow({required List<ColumnMaker> cols}) async {
    final List<Map<String, dynamic>> rawColumnsList = cols
        .map((col) => col.toJson())
        .toList();
    await _channel.invokeMethod("PRINT_ROW", {"cols": rawColumnsList});
  }

  /// Passes explicit positional ESC/POS binary buffers natively over the system bridge safely.
  static Future<void> printRawData(Uint8List data) async {
    await _channel.invokeMethod("RAW_DATA", {"data": data});
  }

  /// Builds a standard format vector QR Code image sequence payload pattern.
  static Future<void> printQRCode(
    String data, {
    int size = 5,
    SunmiQrcodeLevel errorLevel = SunmiQrcodeLevel.LEVEL_H,
  }) async {
    int errorCode;
    switch (errorLevel) {
      case SunmiQrcodeLevel.LEVEL_L:
        errorCode = 0;
        break;
      case SunmiQrcodeLevel.LEVEL_M:
        errorCode = 1;
        break;
      case SunmiQrcodeLevel.LEVEL_Q:
        errorCode = 2;
        break;
      case SunmiQrcodeLevel.LEVEL_H:
        errorCode = 3;
        break;
    }
    await _channel.invokeMethod("PRINT_QRCODE", {
      "data": data,
      'modulesize': size,
      'errorlevel': errorCode,
    });
  }

  /// Builds standard linear barcode configurations directly into raw layout channels.
  static Future<void> printBarCode(
    String data, {
    SunmiBarcodeType barcodeType = SunmiBarcodeType.CODE128,
    int height = 162,
    int width = 2,
    SunmiBarcodeTextPos textPosition = SunmiBarcodeTextPos.TEXT_ABOVE,
  }) async {
    int codeType = 8;
    int textPositionIndex = 1;

    switch (barcodeType) {
      case SunmiBarcodeType.UPCA:
        codeType = 0;
        break;
      case SunmiBarcodeType.UPCE:
        codeType = 1;
        break;
      case SunmiBarcodeType.JAN13:
        codeType = 2;
        break;
      case SunmiBarcodeType.JAN8:
        codeType = 3;
        break;
      case SunmiBarcodeType.CODE39:
        codeType = 4;
        break;
      case SunmiBarcodeType.ITF:
        codeType = 5;
        break;
      case SunmiBarcodeType.CODABAR:
        codeType = 6;
        break;
      case SunmiBarcodeType.CODE93:
        codeType = 7;
        break;
      case SunmiBarcodeType.CODE128:
        codeType = 8;
        break;
    }

    switch (textPosition) {
      case SunmiBarcodeTextPos.NO_TEXT:
        textPositionIndex = 0;
        break;
      case SunmiBarcodeTextPos.TEXT_ABOVE:
        textPositionIndex = 1;
        break;
      case SunmiBarcodeTextPos.TEXT_UNDER:
        textPositionIndex = 2;
        break;
      case SunmiBarcodeTextPos.BOTH:
        textPositionIndex = 3;
        break;
    }

    await _channel.invokeMethod("PRINT_BARCODE", {
      "data": data,
      'barcodeType': codeType,
      'textPosition': textPositionIndex,
      'width': width,
      'height': height,
    });
  }

  /// Feeds the printer paper mechanically by an explicit index tracking lines spacing layout.
  static Future<void> lineWrap(int lines) async {
    await _channel.invokeMethod("LINE_WRAP", {"lines": lines});
  }

  /// Utility draw line command helper for separating operational blocks cleanly.
  static Future<void> line({String ch = '-', int len = 31}) async {
    await resetFontSize();
    await printText(List.filled(len, ch[0]).join());
  }

  /// Appends bold typeface printing parameters directly to downstream streams.
  static Future<void> bold() async {
    await printRawData(Uint8List.fromList([27, 69, 1]));
  }

  /// Strips bold printing metrics from active character parameters.
  static Future<void> resetBold() async {
    await printRawData(Uint8List.fromList([27, 69, 0]));
  }

  /// Modifies horizontal alignment rules for downstream font buffers.
  static Future<void> setAlignment(SunmiPrintAlign alignment) async {
    int value;
    switch (alignment) {
      case SunmiPrintAlign.LEFT:
        value = 0;
        break;
      case SunmiPrintAlign.CENTER:
        value = 1;
        break;
      case SunmiPrintAlign.RIGHT:
        value = 2;
        break;
    }
    await _channel.invokeMethod("SET_ALIGNMENT", {"alignment": value});
  }

  /// Compiles a byte collection array sequence directly into standard raster device outputs.
  static Future<void> printImage(Uint8List img) async {
    await _channel.invokeMethod("PRINT_IMAGE", {"bitmap": img});
  }

  /// Activates transactional execution grouping rules on device memory caches.
  static Future<void> startTransactionPrint([bool clear = false]) async {
    await _channel.invokeMethod("ENTER_PRINTER_BUFFER", {"clearEnter": clear});
  }

  /// Forces execution flush operations on buffered layout transactional blocks.
  static Future<void> submitTransactionPrint() async {
    await _channel.invokeMethod("COMMIT_PRINTER_BUFFER");
  }

  /// Triggers the automatic cutting knife layer (if supported on the hardware).
  static Future<void> cut() async {
    await _channel.invokeMethod("CUT_PAPER");
  }

  /// Sends electrical pulse currents to trip standard outer connection cash drawer relays.
  static Future<void> openDrawer() async {
    await _channel.invokeMethod("OPEN_DRAWER");
  }

  /// Verifies if a valid link loop context is established with the terminal drawer mechanism.
  static Future<bool> drawerStatus() async {
    return await _channel.invokeMethod<bool>("DRAWER_STATUS") ?? false;
  }

  /// Returns total tracked lifetime open counts recorded directly from local peripheral memory blocks.
  static Future<int> drawerTimesOpen() async {
    return await _channel.invokeMethod<int>("DRAWER_OPENED") ?? 0;
  }

  /// Deactivates and exits the transaction buffer mode safely.
  static Future<void> exitTransactionPrint([bool clear = true]) async {
    await _channel.invokeMethod("EXIT_PRINTER_BUFFER", {"clearExit": clear});
  }

  /// Modifies active character rendering heights using structured enum configuration parameters.
  static Future<void> setFontSize(SunmiFontSize size) async {
    int fontSize;
    switch (size) {
      case SunmiFontSize.XS:
        fontSize = 14;
        break;
      case SunmiFontSize.SM:
        fontSize = 18;
        break;
      case SunmiFontSize.MD:
        fontSize = 24;
        break;
      case SunmiFontSize.LG:
        fontSize = 36;
        break;
      case SunmiFontSize.XL:
        fontSize = 42;
        break;
    }
    await _channel.invokeMethod("FONT_SIZE", {"size": fontSize});
  }

  /// Allows setting raw custom character scale sizes.
  static Future<void> setCustomFontSize(int size) async {
    await _channel.invokeMethod("FONT_SIZE", {"size": size});
  }

  /// Restores active text printing parameters back to base medium level scaling metrics.
  static Future<void> resetFontSize() async {
    await _channel.invokeMethod("FONT_SIZE", {"size": 24});
  }

  /// Evaluates and isolates loaded thermal role line width constraints (usually 80 or 58).
  static Future<int> paperSize() async {
    final int? sizeIndex = await _channel.invokeMethod<int>("PAPER_SIZE");
    if (sizeIndex == null || sizeIndex < 0 || sizeIndex >= _paperSize.length) {
      return _paperSize[0];
    }
    return _paperSize[sizeIndex];
  }

  /// Returns the hardware tracking component serial identification reference string.
  static Future<String> serialNumber() async {
    return await _channel.invokeMethod<String>("PRINTER_SERIAL_NUMBER") ??
        "UNKNOWN";
  }

  /// Returns internal manufacturer package compilation software version tags.
  static Future<String> printerVersion() async {
    return await _channel.invokeMethod<String>("PRINTER_VERSION") ?? "UNKNOWN";
  }

  /// Secondary mini LCD Customer Display configuration methods.
  static Future<void> lcdInitialize() async {
    await _channel.invokeMethod("LCD_COMMAND", {"flag": 1});
  }

  static Future<void> lcdWakeup() async {
    await _channel.invokeMethod("LCD_COMMAND", {"flag": 2});
  }

  static Future<void> lcdSleep() async {
    await _channel.invokeMethod("LCD_COMMAND", {"flag": 3});
  }

  static Future<void> lcdClear() async {
    await _channel.invokeMethod("LCD_COMMAND", {"flag": 4});
  }

  static Future<void> lcdString(String text) async {
    await _channel.invokeMethod("LCD_STRING", {"string": text});
  }

  static Future<void> lcdImage(Uint8List img) async {
    await _channel.invokeMethod("LCD_BITMAP", {"bitmap": img});
  }

  static Future<void> lcdDoubleString(String topText, String bottomText) async {
    await _channel.invokeMethod("LCD_DOUBLE_STRING", {
      "topText": topText,
      "bottomText": bottomText,
    });
  }

  static Future<void> lcdFillString(
    String text, {
    int size = 32,
    bool fill = false,
  }) async {
    await _channel.invokeMethod("LCD_FILL_STRING", {
      "string": text,
      "size": size,
      "fill": fill,
    });
  }

  static Future<void> lcdMultiString(
    List<String> texts,
    List<int> aligns,
  ) async {
    await _channel.invokeMethod("LCD_MULTI_STRING", {
      "text": texts,
      "align": aligns,
    });
  }
}
