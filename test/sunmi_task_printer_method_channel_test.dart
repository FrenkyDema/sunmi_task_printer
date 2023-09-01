import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sunmi_task_printer/sunmi_task_printer_method_channel.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  MethodChannelSunmiTaskPrinter platform = MethodChannelSunmiTaskPrinter();
  const MethodChannel channel = MethodChannel('sunmi_task_printer');

  setUp(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(
      channel,
      (MethodCall methodCall) async {
        return '42';
      },
    );
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
  });

  test('getPlatformVersion', () async {
    expect(await platform.getPlatformVersion(), '42');
  });
}
