import 'package:flutter_test/flutter_test.dart';
import 'package:integration_test/integration_test.dart';
import 'package:sunmi_task_printer/sunmi_task_printer.dart';

void main() {
  IntegrationTestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('Plugin degrades gracefully on non-Sunmi device',
      (WidgetTester tester) async {
    // 1. Test Platform Version
    final SunmiTaskPrinter plugin = SunmiTaskPrinter();
    final String? version = await plugin.getPlatformVersion();
    expect(version?.isNotEmpty, true);

    // 2. Test binding to a missing service
    // On a generic emulator, this should execute without crashing,
    // but the internal Java service variable will remain null.
    final bool? isBound = await SunmiTaskPrinter.bindingService();
    // It might return true just for the intent binding passing, or false.
    // The important part is it doesn't throw a PlatformException.
    expect(isBound != null, true);

    // 3. Test a print command gracefully failing
    // Since _printerService is null in Java, our Java code catches it and returns an error code
    final status = await SunmiTaskPrinter.getPrinterStatus();

    // In our Java code, if the service is null, it returns "ERROR" which maps to PrinterStatus.ERROR
    expect(status, isNotNull);

    // 4. Test Serial Number fallback
    final serial = await SunmiTaskPrinter.serialNumber();
    expect(serial, isNotNull);
  });
}
