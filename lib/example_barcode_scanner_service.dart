library barcode_scanner;

import 'dart:async';

import 'package:flutter/services.dart';

enum DeviceType {
  camera,
  tsd,
}

abstract class ExampleBarcodeScannerService {
  Future<DeviceType> getDeviceType();

  /// метод запуска сервиса сканирования
  /// в [onScan] передается результат успешного сканирования
  /// в [onError] передается ошибка, возникшая в результате сканирования
  /// если данный метод вызывается когда процесс сканирования
  /// запущен - обновляются назначенные колбеки
  ///
  /// возвращает id текстуры в gl буффере
  Future<int?> startScan({
    required final Future<void> Function(String data) onScan,
    required final Future<void> Function(String? errorMessage) onError,
  });

  /// метод остановки сервиса сканирования
  Future<int?> stopScan();

  void setCallback(
    Function(String barcode) onScanned,
    Function(String? error) onError,
  );
}

class ExampleBarcodeScannerServiceImpl implements ExampleBarcodeScannerService {
  static const MethodChannel _methodChannel =
      MethodChannel('example_barcode_scanner');

  @override
  Future<int?> startScan({
    required Future<void> Function(String data) onScan,
    required Future<void> Function(String? errorMessage) onError,
  }) async {
    final textureId = await _methodChannel.invokeMethod<int>('startScan');

    setCallback(onScan, onError);

    return textureId;
  }

  @override
  Future<int?> stopScan() {
    return _methodChannel.invokeMethod<int>('stopScan');
  }

  @override
  Future<DeviceType> getDeviceType() async {
    final deviceTypeString = await _methodChannel.invokeMethod<String>(
      'getDeviceType',
    );
    return _getDeviceTypeFromString(deviceTypeString);
  }

  DeviceType _getDeviceTypeFromString(String? string) {
    switch (string) {
      case 'tsd':
        return DeviceType.tsd;
      case 'camera':
        return DeviceType.camera;
    }

    throw Exception("Неизвестный тип устройства!");
  }

  @override
  void setCallback(
    Function(String barcode) onScanned,
    Function(String? error) onError,
  ) {
    _methodChannel.setMethodCallHandler((final MethodCall call) async {
      if (call.method == 'onScan') {
        if (call.arguments != null) {
          onScanned(call.arguments as String);
        } else {
          onError('barcode is null');
        }
      }
      if (call.method == 'onError') {
        if (call.arguments != null) {
          onError(call.arguments as String);
        } else {
          onError(null);
        }
      }
    });
  }
}
