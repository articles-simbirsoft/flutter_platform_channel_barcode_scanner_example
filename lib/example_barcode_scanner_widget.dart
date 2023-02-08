import 'dart:async';

import 'package:example_barcode_scanner/example_barcode_scanner_service.dart';
import 'package:example_barcode_scanner/scan_api.dart';
import 'package:flutter/material.dart';

/// виджет сканера, [onScan] вызывается каждый раз
/// как сканер распознает 1-д/2-д код
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
  // экземпляр сервиса для взаимодействия с сканером
  final _barcodeScannerService = ExampleBarcodeScannerService.instance;
  StreamSubscription<String>? _onScanSubscription;
  // результат запуска сканера
  StartScanResult? _startScanResult;

  @override
  void initState() {
    super.initState();
    // вызываем запуск сканера платформы при старте
    _startScan().then((startScanResult) {
      setState(() {
        _startScanResult = startScanResult;
      });
    });
  }

  Future<StartScanResult> _startScan() {
    // подписываемся на поток результатов сканирования
    _onScanSubscription = _barcodeScannerService.scanResultStream.listen(
      widget.onScan,
    );

    // запускаем сканер
    return _barcodeScannerService.startScan();
  }

  Future<void> _stopScan() {
    // отменяем подписку
    _onScanSubscription?.cancel();

    //
    return _barcodeScannerService.stopScan();
  }

  @override
  void didUpdateWidget(covariant ExampleBarcodeScannerWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.onScan != widget.onScan) {
      // обновляем назначенный callback
      // нет необходимости перезапускать сервис сканера
      _onScanSubscription?.cancel();
      _onScanSubscription = _barcodeScannerService.scanResultStream.listen(
        widget.onScan,
      );
    }
  }

  @override
  void dispose() {
    // убираем за собой, останавливаем сканер
    _stopScan();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final startScanResult = _startScanResult;

    // пока ожидаем результата сканирования - отображаем индикатор загрузки
    if (startScanResult == null) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (startScanResult.scannerType == DeviceType.tsd) {
      // если девайс ТСД - отображем текст о том что это тсд
      return const Center(
        child: Text('tsd'),
      );
    }

    // получаем свойства камеры и отображаем текстуру с нужным AspectRatio
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
