import 'package:flutter_test/flutter_test.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:sunmi_task_printer/sunmi_task_printer.dart';
import 'package:sunmi_task_printer/sunmi_task_printer_method_channel.dart';
import 'package:sunmi_task_printer/sunmi_task_printer_platform_interface.dart';

class MockSunmiTaskPrinterPlatform
    with MockPlatformInterfaceMixin
    implements SunmiTaskPrinterPlatform {
  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final SunmiTaskPrinterPlatform initialPlatform =
      SunmiTaskPrinterPlatform.instance;

  test('$MethodChannelSunmiTaskPrinter is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelSunmiTaskPrinter>());
  });

  test('getPlatformVersion', () async {
    SunmiTaskPrinter sunmiTaskPrinterPlugin = SunmiTaskPrinter();
    MockSunmiTaskPrinterPlatform fakePlatform = MockSunmiTaskPrinterPlatform();
    SunmiTaskPrinterPlatform.instance = fakePlatform;

    expect(await sunmiTaskPrinterPlugin.getPlatformVersion(), '42');
  });
}
