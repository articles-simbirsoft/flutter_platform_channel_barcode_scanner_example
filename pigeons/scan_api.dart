import 'package:pigeon/pigeon.dart';

enum ScannerType { tsd, camera }

class CameraProperties {
  const CameraProperties(
    this.textureId,
    this.aspectRatio,
    this.width,
    this.height,
  );

  final int textureId;
  final double aspectRatio;
  final int width;
  final int height;
}

class StartScanResult {
  const StartScanResult(
    this.scannerType,
    this.cameraProperties,
  );

  final ScannerType scannerType;
  final CameraProperties? cameraProperties;
}

@HostApi()
abstract class ScanHostApi {
  @async
  StartScanResult startScan();

  @async
  void stopScan();
}

@FlutterApi()
abstract class ScanFlutterApi {
  void onScan(String data);
}
