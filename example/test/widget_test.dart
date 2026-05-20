import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sunmi_task_printer_example/main.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const MethodChannel channel = MethodChannel('sunmi_task_printer');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      switch (methodCall.method) {
        case 'PAPER_SIZE':
          return 0; // 80mm
        case 'PRINTER_VERSION':
          return 'MOCK_V_1.0.5';
        case 'PRINTER_SERIAL_NUMBER':
          return 'SN_TEST_12345';
        case 'BIND_SERVICE':
          return true;
        default:
          return null;
      }
    });
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  testWidgets('Verify Metadata Loads in Example App',
      (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const MyApp());

    // Wait for the Future.wait in initState to resolve
    await tester.pumpAndSettle();

    // Verify that the UI updated with the mock data from the MethodChannel
    expect(
        find.textContaining('Hardware Serial: SN_TEST_12345'), findsOneWidget);
    expect(find.textContaining('Service Firmware Release Tag: MOCK_V_1.0.5'),
        findsOneWidget);
    expect(find.textContaining('Width: 80 mm'), findsOneWidget);
  });
}
