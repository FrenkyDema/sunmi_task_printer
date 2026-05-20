import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sunmi_task_printer/column_maker.dart';
import 'package:sunmi_task_printer/enums.dart';
import 'package:sunmi_task_printer/sunmi_style.dart';
import 'package:sunmi_task_printer/sunmi_task_printer.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  debugPrint(
      "[SunmiDebug] Initializing WidgetsFlutterBinding and binding service...");
  try {
    final bool? bound = await SunmiTaskPrinter.bindingService();
    debugPrint("[SunmiDebug] Service binding returned: $bound");
  } catch (e) {
    debugPrint("[SunmiDebug] CRITICAL: Service binding failed: $e");
  }

  await SystemChrome.setPreferredOrientations(
      [DeviceOrientation.landscapeRight, DeviceOrientation.landscapeRight]);

  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Sunmi Printer Example',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepOrange),
        useMaterial3: true,
      ),
      debugShowCheckedModeBanner: false,
      home: const Home(),
    );
  }
}

class Home extends StatefulWidget {
  const Home({super.key});

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  int paperSize = 0;
  String serialNumber = "Loading...";
  String printerVersion = "Loading...";

  @override
  void initState() {
    super.initState();
    _fetchPrinterMetadata();
  }

  Future<void> _fetchPrinterMetadata() async {
    debugPrint("[SunmiDebug] Fetching printer hardware metadata...");
    try {
      final results = await Future.wait([
        SunmiTaskPrinter.paperSize(),
        SunmiTaskPrinter.printerVersion(),
        SunmiTaskPrinter.serialNumber(),
      ]);

      debugPrint(
          "[SunmiDebug] Metadata fetched successfully. Size: ${results[0]}, Version: ${results[1]}, Serial: ${results[2]}");

      if (mounted) {
        setState(() {
          paperSize = results[0] as int;
          printerVersion = results[1] as String;
          serialNumber = results[2] as String;
        });
      }
    } catch (e) {
      debugPrint("[SunmiDebug] Error fetching metadata properties: $e");
    }
  }

  /// Utility helper to print logging state diagnostics onto the device screen
  void _showLogFeedback(String msg, {bool isError = false}) {
    debugPrint("${isError ? '[SunmiError]' : '[SunmiLog]'} $msg");
    if (!mounted) return;
    ScaffoldMessenger.of(context).hideCurrentSnackBar();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(msg),
        backgroundColor: isError ? Colors.red : Colors.green,
        duration: const Duration(seconds: 2),
      ),
    );
  }

  Future<Uint8List> _getImageFromAsset(String iconPath) async {
    final ByteData fileData = await rootBundle.load(iconPath);
    return fileData.buffer
        .asUint8List(fileData.offsetInBytes, fileData.lengthInBytes);
  }

  /// Standard operational loop encapsulating printer commands safely
  Future<void> _executePrintJob(
      String jobName, Future<void> Function() actions) async {
    _showLogFeedback("Starting print job: $jobName...");
    try {
      debugPrint("[SunmiDebug] Resetting styles with initPrinter()");
      await SunmiTaskPrinter.initPrinter();

      debugPrint("[SunmiDebug] Entering transaction print buffer (clear=true)");
      await SunmiTaskPrinter.startTransactionPrint(true);

      // Execute custom print blocks
      await actions();

      debugPrint(
          "[SunmiDebug] Exiting transaction buffer mode and committing content to layout...");
      // This sends the exit signal AND commits the transaction queue to the printing head
      await SunmiTaskPrinter.exitTransactionPrint(true);

      _showLogFeedback("$jobName sent successfully!");
    } catch (e) {
      _showLogFeedback("Failed executing $jobName: $e", isError: true);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Sunmi Task Printer Testing Suite'),
        elevation: 2,
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Text("Paper Configuration Profile Width: $paperSize mm",
                        style: const TextStyle(fontWeight: FontWeight.bold)),
                    const SizedBox(height: 4),
                    Text("Hardware Serial: $serialNumber"),
                    const SizedBox(height: 4),
                    Text("Service Firmware Release Tag: $printerVersion"),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            _buildSectionTitle('Barcodes & Base Commands'),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton.icon(
                  onPressed: () => _executePrintJob('QR Code Print', () async {
                    await SunmiTaskPrinter.printQRCode(
                        'https://github.com/francescodema');
                    await SunmiTaskPrinter.lineWrap(2);
                  }),
                  icon: const Icon(Icons.qr_code_2),
                  label: const Text('Print QR'),
                ),
                ElevatedButton.icon(
                  onPressed: () => _executePrintJob('Barcode Print', () async {
                    await SunmiTaskPrinter.printBarCode(
                      '1234567890',
                      barcodeType: SunmiBarcodeType.CODE128,
                      textPosition: SunmiBarcodeTextPos.TEXT_UNDER,
                      height: 40,
                    );
                    await SunmiTaskPrinter.lineWrap(2);
                  }),
                  icon: const Icon(Icons.line_weight),
                  label: const Text('Print Barcode'),
                ),
                ElevatedButton(
                  onPressed: () =>
                      _executePrintJob('Divider Line Print', () async {
                    await SunmiTaskPrinter.line();
                    await SunmiTaskPrinter.lineWrap(1);
                  }),
                  child: const Text('Print Divider Line'),
                ),
                ElevatedButton(
                  onPressed: () async {
                    _showLogFeedback("Sending raw wrap lines command...");
                    try {
                      await SunmiTaskPrinter.lineWrap(2);
                    } catch (e) {
                      _showLogFeedback("Wrap error: $e", isError: true);
                    }
                  },
                  child: const Text('Wrap 2 Lines (Direct)'),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildSectionTitle('Text Styling Variations'),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: [
                ElevatedButton(
                  onPressed: () => _executePrintJob('Bold Text', () async {
                    await SunmiTaskPrinter.printText(
                        'Hello I\'m bold copy line',
                        style: const SunmiStyle(bold: true));
                    await SunmiTaskPrinter.lineWrap(1);
                  }),
                  child: const Text('Bold'),
                ),
                ElevatedButton(
                  onPressed: () => _executePrintJob('Size XS Text', () async {
                    await SunmiTaskPrinter.printText('XS Font Size Text',
                        style: const SunmiStyle(fontSize: SunmiFontSize.XS));
                    await SunmiTaskPrinter.lineWrap(1);
                  }),
                  child: const Text('Size XS'),
                ),
                ElevatedButton(
                  onPressed: () => _executePrintJob('Size MD Text', () async {
                    await SunmiTaskPrinter.printText('MD Font Size Text',
                        style: const SunmiStyle(fontSize: SunmiFontSize.MD));
                    await SunmiTaskPrinter.lineWrap(1);
                  }),
                  child: const Text('Size MD'),
                ),
                ElevatedButton(
                  onPressed: () => _executePrintJob('Size LG Text', () async {
                    await SunmiTaskPrinter.printText('LG Font Size Text',
                        style: const SunmiStyle(fontSize: SunmiFontSize.LG));
                    await SunmiTaskPrinter.lineWrap(1);
                  }),
                  child: const Text('Size LG'),
                ),
                ElevatedButton(
                  onPressed: () =>
                      _executePrintJob('Centered Block text', () async {
                    await SunmiTaskPrinter.printText('Centered Bold Large',
                        style: const SunmiStyle(
                            align: SunmiPrintAlign.CENTER,
                            bold: true,
                            fontSize: SunmiFontSize.LG));
                    await SunmiTaskPrinter.lineWrap(1);
                  }),
                  child: const Text('Center Block Alignment'),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildSectionTitle('Document Printing Profiles'),
            Card(
              color: Colors.amber.shade50,
              child: Padding(
                padding: const EdgeInsets.all(8.0),
                child: ListTile(
                  title: const Text('Generate Full Demo Bill Receipt',
                      style: TextStyle(fontWeight: FontWeight.bold)),
                  subtitle: const Text(
                      'Prints header, alignment tests, tabular columns rows, and summary block data.'),
                  trailing: const Icon(Icons.print, color: Colors.amber),
                  onTap: _printDemoTicketProfile,
                ),
              ),
            ),
            const SizedBox(height: 16),
            _buildSectionTitle('Hardware Control Relays'),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () async {
                      _showLogFeedback("Triggering cutter relay...");
                      try {
                        await SunmiTaskPrinter.cut();
                      } catch (e) {
                        _showLogFeedback("Cutter error: $e", isError: true);
                      }
                    },
                    icon: const Icon(Icons.content_cut),
                    label: const Text('Trigger Knife Cut'),
                    style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.red.shade50),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: ElevatedButton.icon(
                    onPressed: () async {
                      _showLogFeedback("Triggering drawer kick...");
                      try {
                        await SunmiTaskPrinter.openDrawer();
                      } catch (e) {
                        _showLogFeedback("Drawer error: $e", isError: true);
                      }
                    },
                    icon: const Icon(Icons.sensor_window),
                    label: const Text('Open Cash Box'),
                    style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.green.shade50),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 24),
            _buildSectionTitle('Raster Image Pipelines'),
            Row(
              children: [
                Expanded(
                  child: Card(
                    child: InkWell(
                      onTap: () =>
                          _executePrintJob('Asset Image Print', () async {
                        final Uint8List bytes =
                            await _getImageFromAsset('assets/images/dash.jpeg');
                        await SunmiTaskPrinter.setAlignment(
                            SunmiPrintAlign.CENTER);
                        await SunmiTaskPrinter.printImage(bytes);
                        await SunmiTaskPrinter.lineWrap(2);
                      }),
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Column(
                          children: [
                            Image.asset('assets/images/dash.jpeg',
                                height: 60,
                                width: 60,
                                errorBuilder: (_, __, ___) =>
                                    const Icon(Icons.image, size: 60)),
                            const SizedBox(height: 8),
                            const Text('Print Local Asset',
                                textAlign: TextAlign.center,
                                style: TextStyle(fontSize: 12)),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Card(
                    child: InkWell(
                      onTap: () =>
                          _executePrintJob('Network Image Print', () async {
                        const String url =
                            'https://avatars.githubusercontent.com/u/14101776?s=100';
                        final Uint8List bytes =
                            (await NetworkAssetBundle(Uri.parse(url)).load(url))
                                .buffer
                                .asUint8List();
                        await SunmiTaskPrinter.setAlignment(
                            SunmiPrintAlign.CENTER);
                        await SunmiTaskPrinter.printImage(bytes);
                        await SunmiTaskPrinter.lineWrap(2);
                      }),
                      child: Padding(
                        padding: const EdgeInsets.all(8.0),
                        child: Column(
                          children: [
                            Image.network(
                                'https://avatars.githubusercontent.com/u/14101776?s=100',
                                height: 60,
                                width: 60,
                                errorBuilder: (_, __, ___) =>
                                    const Icon(Icons.cloud_off, size: 60)),
                            const SizedBox(height: 8),
                            const Text('Print Network URL',
                                textAlign: TextAlign.center,
                                style: TextStyle(fontSize: 12)),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
              ],
            )
          ],
        ),
      ),
    );
  }

  Widget _buildSectionTitle(String title) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0, top: 8.0),
      child: Text(title,
          style: const TextStyle(
              fontSize: 14, fontWeight: FontWeight.bold, color: Colors.grey)),
    );
  }

  Future<void> _printDemoTicketProfile() async {
    _showLogFeedback("Preparing full ticket layout...");
    try {
      await SunmiTaskPrinter.initPrinter();
      await SunmiTaskPrinter.startTransactionPrint(true);
      await SunmiTaskPrinter.setAlignment(SunmiPrintAlign.CENTER);
      await SunmiTaskPrinter.line();
      await SunmiTaskPrinter.printText('CUSTOMER RECEIPT');
      await SunmiTaskPrinter.line();

      await SunmiTaskPrinter.printRow(cols: [
        ColumnMaker(text: 'Item Desc', width: 12, align: SunmiPrintAlign.LEFT),
        ColumnMaker(text: 'Qty', width: 6, align: SunmiPrintAlign.CENTER),
        ColumnMaker(text: 'Price', width: 6, align: SunmiPrintAlign.RIGHT),
        ColumnMaker(text: 'Total', width: 6, align: SunmiPrintAlign.RIGHT),
      ]);

      await SunmiTaskPrinter.printRow(cols: [
        ColumnMaker(
            text: 'Fries Crinkle', width: 12, align: SunmiPrintAlign.LEFT),
        ColumnMaker(text: '4x', width: 6, align: SunmiPrintAlign.CENTER),
        ColumnMaker(text: '3.00', width: 6, align: SunmiPrintAlign.RIGHT),
        ColumnMaker(text: '12.00', width: 6, align: SunmiPrintAlign.RIGHT),
      ]);

      await SunmiTaskPrinter.printRow(cols: [
        ColumnMaker(
            text: 'Strawberry Shk', width: 12, align: SunmiPrintAlign.LEFT),
        ColumnMaker(text: '1x', width: 6, align: SunmiPrintAlign.CENTER),
        ColumnMaker(text: '24.44', width: 6, align: SunmiPrintAlign.RIGHT),
        ColumnMaker(text: '24.44', width: 6, align: SunmiPrintAlign.RIGHT),
      ]);

      await SunmiTaskPrinter.line();

      await SunmiTaskPrinter.printRow(cols: [
        ColumnMaker(text: 'TOTAL DUE', width: 20, align: SunmiPrintAlign.LEFT),
        ColumnMaker(text: '36.44', width: 10, align: SunmiPrintAlign.RIGHT),
      ]);

      await SunmiTaskPrinter.line();
      await SunmiTaskPrinter.printQRCode('https://github.com/francescodema');
      await SunmiTaskPrinter.lineWrap(3);

      // Flush and empty transaction buffer to release the feed gears
      debugPrint(
          "[SunmiDebug] Completing ticket profile via exitTransactionPrint(true)...");
      await SunmiTaskPrinter.exitTransactionPrint(true);

      _showLogFeedback("Full receipt profile printed!");
    } catch (e) {
      _showLogFeedback("Receipt profile build failed: $e", isError: true);
    }
  }
}
