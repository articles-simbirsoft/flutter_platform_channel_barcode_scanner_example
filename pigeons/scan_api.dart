import 'package:pigeon/pigeon.dart';

enum ScannerType { tsd, camera }

/// модель свойств камеры
class CameraProperties {
  const CameraProperties(
    this.textureId,
    this.aspectRatio,
    this.width,
    this.height,
  );

  /// id текстуры для передачи изображения
  final int textureId;

  /// соотношение сторон
  final double aspectRatio;
  final int width;
  final int height;
}

/// модель результата запуска сканера
class StartScanResult {
  const StartScanResult(
    this.scannerType,
    this.cameraProperties,
  );

  /// тип сканера
  final ScannerType scannerType;

  /// свойства камеры
  /// согласно контракту, если тип сканера [ScannerType.tsd], то данное поле буде null.
  final CameraProperties? cameraProperties;
}

/// описание апи генерируемого pigeon для платформы
@HostApi()
abstract class ScanHostApi {
  /// запуск сканера
  @async
  StartScanResult startScan();

  /// остановка сканера
  @async
  void stopScan();
}

/// описание апи геренрируемого pigeon для Flutter приложения
@FlutterApi()
abstract class ScanFlutterApi {
  /// метод вызываемый платформой для передачи результата сканирования
  void onScan(String data);
}
