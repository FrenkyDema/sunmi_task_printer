import 'dart:async';

import 'package:esc_pos_utils/esc_pos_utils.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:sunmi_task_printer/column_maker.dart';
import 'package:sunmi_task_printer/enums.dart';
import 'package:sunmi_task_printer/sunmi_style.dart';
import 'package:sunmi_task_printer/sunmi_task_printer.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  await SystemChrome.setPreferredOrientations(
      [DeviceOrientation.landscapeRight, DeviceOrientation.landscapeRight]);
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
        title: 'Sunmi Printer',
        theme: ThemeData(
          primaryColor: Colors.black,
        ),
        debugShowCheckedModeBanner: false,
        home: const Home());
  }
}

class Home extends StatefulWidget {
  const Home({Key? key}) : super(key: key);

  @override
  State<Home> createState() => _HomeState();
}

class _HomeState extends State<Home> {
  bool printBinded = false;
  int paperSize = 0;
  String serialNumber = "";
  String printerVersion = "";

  @override
  void initState() {
    super.initState();

    _bindingPrinter().then((bool? isBind) async {
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

      setState(() {
        printBinded = isBind!;
      });
    });
  }

  /// must binding ur printer at first init in app
  Future<bool?> _bindingPrinter() async {
    final bool? result = await SunmiTaskPrinter.bindingService();
    return result;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: const Text('Sunmi printer Example'),
        ),
        body: SingleChildScrollView(
          child: Column(
            children: [
              Padding(
                padding: const EdgeInsets.only(
                  top: 10,
                ),
                child: Text("Print bound: $printBinded"),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 2.0),
                child: Text("Paper size: $paperSize"),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 2.0),
                child: Text("Serial number: $serialNumber"),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 2.0),
                child: Text("Printer version: $printerVersion"),
              ),
              const Divider(),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printQRCode('https://github.com/brasizza/sunmi_printer');
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Print qrCode')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printBarCode(
                            '1234567890',
                            barcodeType: SunmiBarcodeType.CODE128,
                            textPosition: SunmiBarcodeTextPos.TEXT_UNDER,
                            height: 20,
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Print barCode')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.line();
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Print line')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.lineWrap(2);
                        },
                        child: const Text('Wrap line')),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printText(
                            'Hello I\'m bold',
                            style: SunmiStyle(bold: true),
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Bold Text')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printText(
                            'Very small!',
                            style: SunmiStyle(fontSize: SunmiFontSize.XS),
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Very small font')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printText(
                            'Very small!',
                            style: SunmiStyle(fontSize: SunmiFontSize.SM),
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Small font')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printText(
                            'Normal font',
                            style: SunmiStyle(fontSize: SunmiFontSize.MD),
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Normal font')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.printText(
                            'Large font',
                            style: SunmiStyle(fontSize: SunmiFontSize.LG),
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Large font')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.setFontSize(SunmiFontSize.XL);
                          SunmiTaskPrinter.printText('Very Large font!');
                          SunmiTaskPrinter.resetFontSize();
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Very large font')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.setCustomFontSize(13);
                          SunmiTaskPrinter.printText('Very Large font!');
                          SunmiTaskPrinter.resetFontSize();
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Custom size font')),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printText(
                            'Align right',
                            style: SunmiStyle(align: SunmiPrintAlign.RIGHT),
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Align right')),
                    ElevatedButton(
                        onPressed: () {
                          SunmiTaskPrinter.initPrinter();
                          SunmiTaskPrinter.startTransactionPrint(true);
                          SunmiTaskPrinter.printText(
                            'Align left',
                            style: SunmiStyle(align: SunmiPrintAlign.LEFT),
                          );
                          SunmiTaskPrinter.lineWrap(2);
                          SunmiTaskPrinter.exitTransactionPrint(true);
                        },
                        child: const Text('Align left')),
                    ElevatedButton(
                      onPressed: () {
                        SunmiTaskPrinter.initPrinter();
                        SunmiTaskPrinter.startTransactionPrint(true);
                        SunmiTaskPrinter.printText(
                          'Align center/ LARGE TEXT AND BOLD',
                          style: SunmiStyle(
                              align: SunmiPrintAlign.CENTER,
                              bold: true,
                              fontSize: SunmiFontSize.LG),
                        );
                        SunmiTaskPrinter.lineWrap(2);
                        SunmiTaskPrinter.exitTransactionPrint(true);
                      },
                      child: const Text('Align center'),
                    ),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    GestureDetector(
                      onTap: ()  async {
                        SunmiTaskPrinter.initPrinter();
                        Uint8List byte = await _getImageFromAsset('assets/images/dash.jpeg');
                        SunmiTaskPrinter.setAlignment(SunmiPrintAlign.CENTER);
                        SunmiTaskPrinter.startTransactionPrint(true);
                        SunmiTaskPrinter.printImage(byte);
                        SunmiTaskPrinter.lineWrap(2);
                        SunmiTaskPrinter.exitTransactionPrint(true);
                      },
                      child: Column(
                        children: [
                          Image.asset(
                            'assets/images/dash.jpeg',
                            width: 100,
                          ),
                          const Text('Print this image from asset!')
                        ],
                      ),
                    ),
                    GestureDetector(
                      onTap: () async {
                        await SunmiTaskPrinter.initPrinter();

                        String url = 'https://avatars.githubusercontent.com/u/14101776?s=100';
                        // convert image to Uint8List format
                        Uint8List byte = (await NetworkAssetBundle(Uri.parse(url)).load(url))
                            .buffer
                            .asUint8List();
                        SunmiTaskPrinter.setAlignment(SunmiPrintAlign.CENTER);
                        SunmiTaskPrinter.startTransactionPrint(true);
                        SunmiTaskPrinter.printImage(byte);
                        SunmiTaskPrinter.lineWrap(2);
                        SunmiTaskPrinter.exitTransactionPrint(true);
                      },
                      child: Column(
                        children: [
                          Image.network('https://avatars.githubusercontent.com/u/14101776?s=100'),
                          const Text('Print this image from WEB!')
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              const Divider(),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(mainAxisAlignment: MainAxisAlignment.spaceAround, children: [
                  ElevatedButton(
                      onPressed: () async {
                        await SunmiTaskPrinter.cut();
                      },
                      child: const Text('CUT PAPER')),
                ]),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(mainAxisAlignment: MainAxisAlignment.spaceAround, children: [
                  ElevatedButton(
                      onPressed: () async {
                        SunmiTaskPrinter.initPrinter();
                        SunmiTaskPrinter.startTransactionPrint(true);
                        SunmiTaskPrinter.setAlignment(SunmiPrintAlign.CENTER);
                        SunmiTaskPrinter.line();
                        SunmiTaskPrinter.printText('Payment receipt');
                        SunmiTaskPrinter.printText('Using the old way to bold!');
                        SunmiTaskPrinter.line();

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'Name', width: 12, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: 'Qty', width: 6, align: SunmiPrintAlign.CENTER),
                          ColumnMaker(text: 'UN', width: 6, align: SunmiPrintAlign.RIGHT),
                          ColumnMaker(text: 'TOT', width: 6, align: SunmiPrintAlign.RIGHT),
                        ]);

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'Fries', width: 12, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: '4x', width: 6, align: SunmiPrintAlign.CENTER),
                          ColumnMaker(text: '3.00', width: 6, align: SunmiPrintAlign.RIGHT),
                          ColumnMaker(text: '12.00', width: 6, align: SunmiPrintAlign.RIGHT),
                        ]);

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'Strawberry', width: 12, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: '1x', width: 6, align: SunmiPrintAlign.CENTER),
                          ColumnMaker(text: '24.44', width: 6, align: SunmiPrintAlign.RIGHT),
                          ColumnMaker(text: '24.44', width: 6, align: SunmiPrintAlign.RIGHT),
                        ]);

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'Soda', width: 12, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: '1x', width: 6, align: SunmiPrintAlign.CENTER),
                          ColumnMaker(text: '1.99', width: 6, align: SunmiPrintAlign.RIGHT),
                          ColumnMaker(text: '1.99', width: 6, align: SunmiPrintAlign.RIGHT),
                        ]);

                        SunmiTaskPrinter.line();
                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'TOTAL', width: 25, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: '38.43', width: 5, align: SunmiPrintAlign.RIGHT),
                        ]);

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'ARABIC TEXT', width: 15, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: 'اسم المشترك', width: 15, align: SunmiPrintAlign.LEFT),
                        ]);

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'اسم المشترك', width: 15, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: 'اسم المشترك', width: 15, align: SunmiPrintAlign.LEFT),
                        ]);

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'RUSSIAN TEXT', width: 15, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(
                              text: 'Санкт-Петербу́рг', width: 15, align: SunmiPrintAlign.LEFT),
                        ]);
                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(
                              text: 'Санкт-Петербу́рг', width: 15, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(
                              text: 'Санкт-Петербу́рг', width: 15, align: SunmiPrintAlign.LEFT),
                        ]);

                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: 'CHINESE TEXT', width: 15, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: '風俗通義', width: 15, align: SunmiPrintAlign.LEFT),
                        ]);
                        SunmiTaskPrinter.printRow(cols: [
                          ColumnMaker(text: '風俗通義', width: 15, align: SunmiPrintAlign.LEFT),
                          ColumnMaker(text: '風俗通義', width: 15, align: SunmiPrintAlign.LEFT),
                        ]);

                        SunmiTaskPrinter.setAlignment(SunmiPrintAlign.CENTER);
                        SunmiTaskPrinter.line();
                        SunmiTaskPrinter.bold();
                        SunmiTaskPrinter.printText('Transaction\'s Qrcode');
                        SunmiTaskPrinter.resetBold();
                        SunmiTaskPrinter.printQRCode(
                            'https://github.com/brasizza/sunmi_printer');
                        SunmiTaskPrinter.lineWrap(2);
                        SunmiTaskPrinter.exitTransactionPrint(true);
                      },
                      child: const Text('TICKET EXAMPLE')),
                ]),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(mainAxisAlignment: MainAxisAlignment.spaceAround, children: [
                  ElevatedButton(
                      onPressed: () async {
                        final List<int> escPos = await _customEscPos();
                        SunmiTaskPrinter.initPrinter();
                        SunmiTaskPrinter.startTransactionPrint(true);
                        SunmiTaskPrinter.printRawData(Uint8List.fromList(escPos));
                        SunmiTaskPrinter.exitTransactionPrint(true);
                      },
                      child: const Text('Custom ESC/POS to print')),
                ]),
              ),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 20),
                child: Row(mainAxisAlignment: MainAxisAlignment.spaceAround, children: [
                  ElevatedButton(
                      onPressed: () {
                        SunmiTaskPrinter.openDrawer();
                      },
                      child: const Text('Open Drawer')),
                ]),
              ),
            ],
          ),
        ));
  }
}

Future<Uint8List> readFileBytes(String path) async {
  ByteData fileData = await rootBundle.load(path);
  Uint8List fileUnit8List =
      fileData.buffer.asUint8List(fileData.offsetInBytes, fileData.lengthInBytes);
  return fileUnit8List;
}

Future<Uint8List> _getImageFromAsset(String iconPath) async {
  return await readFileBytes(iconPath);
}

Future<List<int>> _customEscPos() async {
  final profile = await CapabilityProfile.load();
  final generator = Generator(PaperSize.mm58, profile);
  List<int> bytes = [];

  bytes += generator.text(
      'Regular: aA bB cC dD eE fF gG hH iI jJ kK lL mM nN oO pP qQ rR sS tT uU vV wW xX yY zZ');
  bytes += generator.text('Special 1: àÀ èÈ éÉ ûÛ üÜ çÇ ôÔ',
      styles: const PosStyles(codeTable: 'CP1252'));
  bytes += generator.text('Special 2: blåbærgrød', styles: const PosStyles(codeTable: 'CP1252'));

  bytes += generator.text('Bold text', styles: const PosStyles(bold: true));
  bytes += generator.text('Reverse text', styles: const PosStyles(reverse: true));
  bytes +=
      generator.text('Underlined text', styles: const PosStyles(underline: true), linesAfter: 1);
  bytes += generator.text('Align left', styles: const PosStyles(align: PosAlign.left));
  bytes += generator.text('Align center', styles: const PosStyles(align: PosAlign.center));
  bytes +=
      generator.text('Align right', styles: const PosStyles(align: PosAlign.right), linesAfter: 1);
  bytes += generator.qrcode('Barcode by escpos', size: QRSize.Size4, cor: QRCorrection.H);
  bytes += generator.feed(2);

  bytes += generator.row([
    PosColumn(
      text: 'col3',
      width: 3,
      styles: const PosStyles(align: PosAlign.center, underline: true),
    ),
    PosColumn(
      text: 'col6',
      width: 6,
      styles: const PosStyles(align: PosAlign.center, underline: true),
    ),
    PosColumn(
      text: 'col3',
      width: 3,
      styles: const PosStyles(align: PosAlign.center, underline: true),
    ),
  ]);

  bytes += generator.text('Text size 200%',
      styles: const PosStyles(
        height: PosTextSize.size2,
        width: PosTextSize.size2,
      ));

  bytes += generator.reset();
  bytes += generator.cut();

  return bytes;
}
