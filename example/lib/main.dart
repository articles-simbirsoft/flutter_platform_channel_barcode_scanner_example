import 'package:example_barcode_scanner/example_barcode_scanner_widget.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(
      home: MainScreen(),
    );
  }
}

class MainScreen extends StatelessWidget {
  const MainScreen({
    Key? key,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Center(
        child: ExampleBarcodeScannerWidget(
          key: const ValueKey('barcodeScanner'),
          onScan: (data) async {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('Прочтен штрихкод $data'),
              ),
            );
          },
          onError: (error) async {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('Произошла ошибка $error'),
              ),
            );
          },
        ),
      ),
    );
  }
}
