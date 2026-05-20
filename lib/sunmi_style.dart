import 'enums.dart';

/// **SunmiStyle**
///
/// Blueprint class to cluster print parameters, avoiding the need to execute 3 distinct statements to modify layout metrics.
class SunmiStyle {
  /// Font scaling dimension metric flag configuration.
  final SunmiFontSize? fontSize;

  /// Layout horizon align direction policy indicator.
  final SunmiPrintAlign? align;

  /// Text formatting configuration weight option parameter.
  final bool? bold;

  /// Instantiates a structural layout style instance template.
  const SunmiStyle({this.fontSize, this.align, this.bold});
}
