import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'sunmi_task_printer_method_channel.dart';

abstract class SunmiTaskPrinterPlatform extends PlatformInterface {
  /// Constructs a SunmiTaskPrinterPlatform.
  SunmiTaskPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static SunmiTaskPrinterPlatform _instance = MethodChannelSunmiTaskPrinter();

  /// The default instance of [SunmiTaskPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelSunmiTaskPrinter].
  static SunmiTaskPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [SunmiTaskPrinterPlatform] when
  /// they register themselves.
  static set instance(SunmiTaskPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
