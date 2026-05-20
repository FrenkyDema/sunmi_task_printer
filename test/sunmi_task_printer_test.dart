import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sunmi_task_printer/column_maker.dart';
import 'package:sunmi_task_printer/enums.dart';
import 'package:sunmi_task_printer/sunmi_task_printer.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const MethodChannel channel = MethodChannel('sunmi_task_printer');

  // This list will keep track of every method called during the test
  final List<MethodCall> log = <MethodCall>[];

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
          log.add(methodCall);

          // Return stub data based on the method called
          switch (methodCall.method) {
            case 'BIND_SERVICE':
              return true;
            case 'PAPER_SIZE':
              return 1; // Corresponds to 58mm in your list
            case 'PRINTER_SERIAL_NUMBER':
              return 'TEST_SERIAL_123';
            case 'GET_UPDATE_PRINTER':
              return 'NORMAL';
            default:
              return null;
          }
        });
  });

  tearDown(() {
    log.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('bindingService triggers BIND_SERVICE', () async {
    final result = await SunmiTaskPrinter.bindingService();
    expect(result, true);
    expect(log, <Matcher>[isMethodCall('BIND_SERVICE', arguments: null)]);
  });

  test('paperSize returns correctly mapped value', () async {
    final result = await SunmiTaskPrinter.paperSize();
    expect(result, 58); // Because our mock returned index 1
    expect(log, <Matcher>[isMethodCall('PAPER_SIZE', arguments: null)]);
  });

  test('printText formats arguments correctly', () async {
    await SunmiTaskPrinter.printText('Hello World');

    // It should call PRINT_TEXT and then INIT_PRINTER based on your code
    expect(log[0].method, 'PRINT_TEXT');
    expect(log[0].arguments, {'text': 'Hello World\n'});
    expect(log[1].method, 'INIT_PRINTER');
  });

  test('printRow maps columns correctly', () async {
    await SunmiTaskPrinter.printRow(
      cols: [
        ColumnMaker(text: 'Item', width: 2, align: SunmiPrintAlign.CENTER),
      ],
    );

    expect(log[0].method, 'PRINT_ROW');

    // Verify the Dart object was correctly converted to the primitive map list
    final args = log[0].arguments as Map<dynamic, dynamic>;
    final cols = args['cols'] as List;
    expect(cols.length, 1);
    expect(cols[0]['text'], 'Item');
    expect(cols[0]['width'], 2);
    expect(cols[0]['align'], 1); // CENTER maps to 1
  });
}
