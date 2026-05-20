import 'enums.dart';

/// **ColumnMaker**
///
/// With this class you can build a column with a text payload and explicit weight layout alignments.
/// This is an ideal configuration when constructing multi-column layouts like continuous POS receipts.
///
/// Example:
/// Name  Qtd Value
/// XXX   2   2.33
class ColumnMaker {
  /// The string content to render inside the target cell column.
  String text;

  /// The comparative layout width allocation weight.
  int width;

  /// The layout horizontal alignment strategy rule.
  SunmiPrintAlign align;

  /// Constructs a fresh blueprint instance of a row column block.
  ColumnMaker({
    this.text = '',
    this.width = 2,
    this.align = SunmiPrintAlign.LEFT,
  });

  /// Converts the column metadata into a raw structured Map.
  /// Modified to pass values as primitive native types to support modern AGP channel boundaries.
  Map<String, dynamic> toJson() {
    int alignmentValueValue;
    switch (align) {
      case SunmiPrintAlign.LEFT:
        alignmentValueValue = 0;
        break;
      case SunmiPrintAlign.CENTER:
        alignmentValueValue = 1;
        break;
      case SunmiPrintAlign.RIGHT:
        alignmentValueValue = 2;
        break;
    }
    return {"text": text, "width": width, "align": alignmentValueValue};
  }
}
