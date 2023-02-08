import 'dart:async';

import 'package:example_barcode_scanner/example_barcode_scanner_service.dart';
import 'package:example_barcode_scanner/scan_api.dart';
import 'package:flutter/material.dart';

class ExampleBarcodeScannerWidget extends StatefulWidget {
  const ExampleBarcodeScannerWidget({
    Key? key,
    required this.onScan,
  }) : super(key: key);

  final Future<void> Function(String data) onScan;

  @override
  State<ExampleBarcodeScannerWidget> createState() =>
      _ExampleBarcodeScannerWidgetState();
}

class _ExampleBarcodeScannerWidgetState
    extends State<ExampleBarcodeScannerWidget> {
  final _barcodeScannerService = ExampleBarcodeScannerService.instance;
  StreamSubscription<String>? _onScanSubscription;
  StartScanResult? _startScanResult;

  @override
  void initState() {
    super.initState();
    _startScan().then((startScanResult) {
      setState(() {
        _startScanResult = startScanResult;
      });
    });
  }

  Future<StartScanResult> _startScan() {
    _onScanSubscription = _barcodeScannerService.scanResultStream.listen(
      widget.onScan,
    );

    return _barcodeScannerService.startScan();
  }

  Future<void> _stopScan() {
    _onScanSubscription?.cancel();

    return _barcodeScannerService.stopScan();
  }

  @override
  void didUpdateWidget(covariant ExampleBarcodeScannerWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.onScan != widget.onScan) {
      _onScanSubscription?.cancel();
      _onScanSubscription = _barcodeScannerService.scanResultStream.listen(
        widget.onScan,
      );
    }
  }

  @override
  void dispose() {
    _stopScan();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final startScanResult = _startScanResult;

    if (startScanResult == null) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (startScanResult.scannerType == DeviceType.tsd) {
      return const Center(
        child: Text('tsd'),
      );
    }

    final cameraProperties = startScanResult.cameraProperties!;
    return AspectRatio(
      aspectRatio: cameraProperties.aspectRatio,
      child: SizedBox(
        height: cameraProperties.height.toDouble(),
        width: cameraProperties.width.toDouble(),
        child: Texture(
          textureId: cameraProperties.textureId,
        ),
      ),
    );
  }
}
