import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sunmi_task_printer/column_maker.dart';
import 'package:sunmi_task_printer/enums.dart';
import 'package:sunmi_task_printer/sunmi_style.dart';
import 'package:sunmi_task_printer/sunmi_task_printer.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  const MethodChannel channel = MethodChannel('sunmi_task_printer');
  final List<MethodCall> log = <MethodCall>[];

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
          log.add(methodCall);
          // Return dummy values based on method
          return null;
        });
  });

  tearDown(() {
    log.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  group('Lifecycle & Metadata', () {
    test('Service Methods', () async {
      await SunmiTaskPrinter.bindingService();
      await SunmiTaskPrinter.unbindingService();
      await SunmiTaskPrinter.initPrinter();

      expect(log[0].method, 'BIND_SERVICE');
      expect(log[1].method, 'UNBIND_SERVICE');
      expect(log[2].method, 'INIT_PRINTER');
    });

    test('Metadata getters', () async {
      await SunmiTaskPrinter.paperSize();
      await SunmiTaskPrinter.serialNumber();
      await SunmiTaskPrinter.printerVersion();

      expect(log[0].method, 'PAPER_SIZE');
      expect(log[1].method, 'PRINTER_SERIAL_NUMBER');
      expect(log[2].method, 'PRINTER_VERSION');
    });
  });

  group('Printing Commands', () {
    test('printText with style', () async {
      await SunmiTaskPrinter.printText(
        'Test',
        style: const SunmiStyle(
          bold: true,
          fontSize: SunmiFontSize.LG,
          align: SunmiPrintAlign.CENTER,
        ),
      );

      // Should trigger alignment -> fontSize -> bold -> printText -> init
      expect(log[0].method, 'SET_ALIGNMENT');
      expect(log[1].method, 'FONT_SIZE');
      expect(log[2].method, 'RAW_DATA'); // Bold
      expect(log[3].method, 'PRINT_TEXT');
    });

    test('printQRCode branches', () async {
      await SunmiTaskPrinter.printQRCode(
        'data',
        errorLevel: SunmiQrcodeLevel.LEVEL_L,
      );
      expect(log[0].arguments['errorlevel'], 0);

      await SunmiTaskPrinter.printQRCode(
        'data',
        errorLevel: SunmiQrcodeLevel.LEVEL_H,
      );
      expect(log[1].arguments['errorlevel'], 3);
    });

    test('printBarCode branches', () async {
      await SunmiTaskPrinter.printBarCode(
        '123',
        barcodeType: SunmiBarcodeType.CODE128,
      );
      expect(log[0].arguments['barcodeType'], 8);
    });
  });

  group('LCD and Hardware Actions', () {
    test('LCD methods', () async {
      await SunmiTaskPrinter.lcdInitialize();
      await SunmiTaskPrinter.lcdString('Test');
      await SunmiTaskPrinter.lcdDoubleString('Top', 'Bottom');

      expect(log[0].method, 'LCD_COMMAND');
      expect(log[1].method, 'LCD_STRING');
      expect(log[2].method, 'LCD_DOUBLE_STRING');
    });

    test('Hardware actions', () async {
      await SunmiTaskPrinter.cut();
      await SunmiTaskPrinter.openDrawer();
      await SunmiTaskPrinter.drawerStatus();

      expect(log[0].method, 'CUT_PAPER');
      expect(log[1].method, 'OPEN_DRAWER');
      expect(log[2].method, 'DRAWER_STATUS');
    });
  });

  group('Utility & Models', () {
    test('ColumnMaker toJson test', () {
      final col = ColumnMaker(
        text: 'Test',
        width: 5,
        align: SunmiPrintAlign.RIGHT,
      );
      final json = col.toJson();
      expect(json['text'], 'Test');
      expect(json['align'], 2); // Right = 2
    });
  });
}
