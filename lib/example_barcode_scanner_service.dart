library barcode_scanner;

import 'dart:async';

import 'package:example_barcode_scanner/scan_api.dart';

enum DeviceType {
  camera,
  tsd,
}

abstract class ExampleBarcodeScannerService extends ScanFlutterApi {
  static ExampleBarcodeScannerService? _instance;

  static ExampleBarcodeScannerService get instance {
    if (_instance != null) {
      return _instance!;
    }
    _instance = ExampleBarcodeScannerServiceImpl();
    return _instance!;
  }

  /// поток результатов сканирования
  Stream<String> get scanResultStream;

  /// метод запуска сервиса сканирования
  /// в [onScan] передается результат успешного сканирования
  ///
  /// возвращает [StartScanResult] с информацией о сканере
  Future<StartScanResult> startScan();

  /// метод остановки сервиса сканирования
  Future<void> stopScan();
}

class ExampleBarcodeScannerServiceImpl implements ExampleBarcodeScannerService {
  ExampleBarcodeScannerServiceImpl() {
    // важно вызвать чтобы зарегистрировать экземпляр
    // плагина для получения сообщений по PlatformChannel
    ScanFlutterApi.setup(this);
  }

  final ScanHostApi _scanHostApi = ScanHostApi();

  final StreamController<String> _scanResultStreamController =
      StreamController<String>.broadcast();

  @override
  Stream<String> get scanResultStream => _scanResultStreamController.stream;

  @override
  void onScan(String data) {
    _scanResultStreamController.add(data);
  }

  @override
  Future<StartScanResult> startScan() {
    // вызываем метод платформы для запуска сканера
    return _scanHostApi.startScan();
  }

  @override
  Future<void> stopScan() {
    // вызываем метод платформы для остановки сканера
    return _scanHostApi.stopScan();
  }
}
