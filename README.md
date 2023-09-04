# sunmi_task_printer

## Important

**THIS PACKAGE WILL WORK ONLY IN ANDROID!**

It supports Sunmi and Null Safety. I developed this Flutter plugin based on the following:
[Official Sunmi Inner Printer Doc](https://file.cdn.sunmi.com/SUNMIDOCS/%E5%95%86%E7%B1%B3%E5%86%85%E7%BD%AE%E6%89%93%E5%8D%B0%E6%9C%BA%E5%BC%80%E5%8F%91%E8%80%85%E6%96%87%E6%A1%A3EN-0224.pdf).
However, not all methods from the documentation were included in this package because I don't have access to the equipment. If you have the equipment and can assist, please contact me on GitHub!

## Installation

```bash
flutter pub add sunmi_task_printer
```

## What this package does

- [x] Write text (with or without style)
- [x] Change font size
- [x] Insert line breaks
- [x] Draw horizontal lines
- [x] Enable or disable bold mode
- [x] Print various types of barcodes (see enum below)
- [x] Print QR codes with custom width and error level
- [x] Print images from assets or the web (example demonstrates both)
- [x] Print rows like receipts with custom width and alignment
- [x] Combine with existing ESC/POS code
- [x] Cut paper - Dedicated method for cutting paper
- [x] Get the printer's serial number
- [x] Get the printer's version
- [x] Get the paper size (0: 80mm, 1: 58mm)
- [x] LCD: Print an image [ytyng](https://github.com/ytyng)
- [x] LCD: Print a string with multiple lines (double lines) Thanks to [ytyng](https://github.com/ytyng)
- [x] Open the cash drawer Thanks to [ZheruiL](https://github.com/ZheruiL)
- [x] Check if the cash drawer is connected
- [x] Get the number of times the cash drawer was opened
- [ ] Implement device status listener (TODO: Add methods for managing device states via a listener)

## Tested Devices

```
Sunmi V2 Pro
Sunmi T2 mini
Sunmi V2S
```

## \*\*You can also combine this package with the

package [esc_pos_utils](https://pub.dev/packages/esc_pos_utils)\*\*

_With this package, you **can** create custom ESC/POS commands, eliminating the need for additional commands.
This is beneficial if you already have code that is compatible with other printers, as you can reuse that code as well._

# Just see the example folder!

```dart
// Import packages
import 'package:sunmi_task_printer/sunmi_task_printer.dart';

// All methods from Sunmi Task Printer do not require asynchronous calls.
SunmiTaskPrinter.bindingPrinter(); // The plugin handles asynchronous operations automatically.
```

## Get device info

```dart
// No need to use async/await for these operations
SunmiTaskPrinter.paperSize().then((int size) {
  setState(() {
    paperSize = size;
  });
});

SunmiTaskPrinter.printerVersion().then((String version) {
  setState(() {
    printerVersion = version;
  });
});

SunmiTaskPrinter.serialNumber().then((String serial) {
  setState(() {
    serialNumber = serial;
  });
});
```

## Inspiration

This package is a fork of [sunmi_printer_plus](https://pub.dev/packages/sunmi_printer_plus).

