# sunmi_task_printer

[![Analyze & Test](https://github.com/FrenkyDema/sunmi_task_printer/actions/workflows/analyze_and_test.yml/badge.svg)](https://github.com/FrenkyDema/sunmi_task_printer/actions/workflows/analyze_and_test.yml)
[![Coverage](https://raw.githubusercontent.com/FrenkyDema/sunmi_task_printer/coverage/.github/badges/coverage-badge.svg)](http://francescodema.dev/sunmi_task_printer/)
[![Pub Version](https://img.shields.io/pub/v/sunmi_task_printer.svg)](https://pub.dev/packages/sunmi_task_printer)

## Important

**THIS PACKAGE WILL WORK ONLY IN ANDROID!**

It supports Sunmi devices and Null Safety. I developed this Flutter plugin based on the following:
[Official Sunmi Inner Printer Doc](https://file.cdn.sunmi.com/SUNMIDOCS/%E5%95%86%E7%B1%B3%E5%86%85%E7%BD%AE%E6%89%93%E5%8D%B0%E6%9C%BA%E5%BC%80%E5%8F%91%E8%80%85%E6%96%87%E6%A1%A3EN-0224.pdf).

However, not all methods from the documentation were included in this package because I don't have
access to all the equipment. If you have the equipment and can assist, please contact me on GitHub!

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
- [x] LCD: Print an image
- [x] LCD: Print a string with multiple lines (double lines)
- [x] Open the cash drawer
- [x] Check if the cash drawer is connected
- [x] Get the number of times the cash drawer was opened
- [ ] **Real-time device status listener for hardware failures (Out of paper, overheating, etc.)**

## Tested Devices

```text
Sunmi V2 Pro
Sunmi T2 mini
Sunmi V2S
```

## Setup & Initialization

> **Note:** As of `v0.1.0`, this package utilizes a robust, non-blocking asynchronous queue. You *
*must** `await` your commands to ensure the hardware processes the transaction buffer correctly.

```dart
import 'package:sunmi_task_printer/sunmi_task_printer.dart';

void main() async {
WidgetsFlutterBinding.ensureInitialized();

// Bind the hardware printer layer immediately upon startup
await SunmiTaskPrinter.bindingService();

runApp(const MyApp());
}
```

## Fetching Device Info

```dart
// Fetching hardware metadata
final int paperSize = await SunmiTaskPrinter.paperSize();
final String version = await SunmiTaskPrinter.printerVersion();
final String serial = await SunmiTaskPrinter.serialNumber();

print('Size: $paperSize, Version: $version, Serial: $serial');
```

## Printing a Transaction (Receipt)

To prevent the printer from pausing between commands, wrap your layout inside a transaction buffer:

```dart
await SunmiTaskPrinter.initPrinter();
await SunmiTaskPrinter.startTransactionPrint(true);

await SunmiTaskPrinter.setAlignment(SunmiPrintAlign.CENTER);
await SunmiTaskPrinter.printText('CUSTOMER RECEIPT', style: const SunmiStyle(bold: true));
await SunmiTaskPrinter.line();

await SunmiTaskPrinter.printRow(cols: [
ColumnMaker(text: 'Item', width: 12, align: SunmiPrintAlign.LEFT),
ColumnMaker(text: 'Qty', width: 6, align: SunmiPrintAlign.CENTER),
ColumnMaker(text: 'Total', width: 6, align: SunmiPrintAlign.RIGHT),
]);

await SunmiTaskPrinter.lineWrap(2);

// Force the printer to commit and push the paper out
await SunmiTaskPrinter.submitTransactionPrint();
```

## Listening to Hardware Errors (EventChannel) (WIP)

You can listen to real-time mechanical failures (e.g., printer ran out of paper mid-receipt) by
subscribing to the hardware stream:

```dart
late StreamSubscription _hardwareErrorSubscription;

@override
void initState() {
    super.initState();
    
    _hardwareErrorSubscription = SunmiTaskPrinter.hardwareErrorStream.listen((error) {
        final errorType = error['type'];
        final errorMessage = error['message'];
    
        print("[PRINTER FAULT]: $errorType - $errorMessage");
        // Show an alert dialog to the cashier to fix the paper roll!

    });
}

@override
void dispose() {
    _hardwareErrorSubscription.cancel();
    super.dispose();
}
```

## ESC/POS Compatibility

You can also combine this package with the [esc_pos_utils](https://pub.dev/packages/esc_pos_utils)
package.

*With this package, you **can** create custom ESC/POS commands, eliminating the need for additional
commands. This is beneficial if you already have code that is compatible with other printers, as you
can reuse that code effortlessly.*

```dart
final Uint8List escPosData = // ... generate your ESC/POS bytes
await SunmiTaskPrinter.printRawData(escPosData);
```

## Inspiration

This package is a modernized fork
of [sunmi_printer_plus](https://pub.dev/packages/sunmi_printer_plus).
