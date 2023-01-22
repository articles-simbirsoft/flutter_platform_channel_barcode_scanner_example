import 'package:example_barcode_scanner/example_barcode_scanner_service.dart';
import 'package:flutter/material.dart';

class ExampleBarcodeScannerWidget extends StatefulWidget {
  const ExampleBarcodeScannerWidget({
    Key? key,
    required this.onScan,
    required this.onError,
  }) : super(key: key);

  final Future<void> Function(String data) onScan;

  final Future<void> Function(String? errorMessage) onError;

  @override
  State<ExampleBarcodeScannerWidget> createState() =>
      _ExampleBarcodeScannerWidgetState();
}

class _ExampleBarcodeScannerWidgetState
    extends State<ExampleBarcodeScannerWidget> {
  final _barcodeScannerService = ExampleBarcodeScannerServiceImpl();
  int? _textureId;
  DeviceType? _deviceType;

  @override
  void initState() {
    super.initState();
    _startScan().then((textureId) {
      setState(() {
        _textureId = textureId;
      });
    });
    _barcodeScannerService.getDeviceType().then(
      (deviceType) {
        setState(() {
          _deviceType = deviceType;
        });
      },
    );
  }

  Future<int?> _startScan() {
    return _barcodeScannerService.startScan(
      onScan: widget.onScan,
      onError: widget.onError,
    );
  }

  @override
  void didUpdateWidget(covariant ExampleBarcodeScannerWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.onScan != widget.onScan ||
        oldWidget.onError != widget.onError) {
      _barcodeScannerService.setCallback(widget.onScan, widget.onError);
    }
  }

  @override
  void dispose() {
    _barcodeScannerService.stopScan();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final textureId = _textureId;
    final deviceType = _deviceType;
    if (textureId == null || deviceType == null) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (deviceType == DeviceType.tsd) {
      return const Center(
        child: Text('tsd'),
      );
    }

    return AspectRatio(
      aspectRatio: 3 / 4,
      child: Texture(
        textureId: textureId,
      ),
    );
  }
}
