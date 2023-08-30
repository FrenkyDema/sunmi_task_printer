import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'sunmi_task_printer_platform_interface.dart';

/// An implementation of [SunmiTaskPrinterPlatform] that uses method channels.
class MethodChannelSunmiTaskPrinter extends SunmiTaskPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('sunmi_task_printer');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
