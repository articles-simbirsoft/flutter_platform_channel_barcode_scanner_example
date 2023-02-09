package com.example.example_barcode_scanner

import android.app.Activity
import android.os.Build
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import io.flutter.Log

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.view.TextureRegistry


/** Класс плагина сканера
 * реализует [Pigeon.ScanHostApi] для получения событий по PlatformChannel
 * @property [flutterApi] - экземпляр [Pigeon.ScanFlutterApi] для вызова api Flutter приложения
 * */
class ExampleBarcodeScannerPlugin : FlutterPlugin, ActivityAware, Pigeon.ScanHostApi {
    private var activity: Activity? = null
    private var scanner: Scanner? = null
    private var textureRegistry: TextureRegistry? = null
    private var flutterApi: Pigeon.ScanFlutterApi? = null


    override fun onAttachedToEngine(
        @NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
    ) {
        // важно вызвать чтобы зарегистрировать экземпляр
        // плагина для получения сообщений по PlatformChannel
        Pigeon.ScanHostApi.setup(flutterPluginBinding.binaryMessenger, this)
        textureRegistry = flutterPluginBinding.textureRegistry
        flutterApi = Pigeon.ScanFlutterApi(flutterPluginBinding.binaryMessenger)
    }

    override fun onDetachedFromEngine(
        @NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding
    ) {
        textureRegistry = null
    }

    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
        activity = activityBinding.activity
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} ${Build.DEVICE}"
        android.util.Log.i("ExampleBarcodeScanner", deviceInfo)
        scanner = when {
            // TODO здесь можно добавить создание объекта реализации [Scanner] под конкретный ТСД
            else -> CameraScanner(textureRegistry!!)
        }
        try {
            scanner?.onActivityAttach(activity!!)
        } catch (e: Throwable) {
            android.util.Log.e("ExampleBarcodeScanner", deviceInfo, e)
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(
        activityBindingScanner: ActivityPluginBinding
    ) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
        scanner?.onActivityDetach(activity!!)
        scanner = null
        activity = null
    }

    override fun startScan(result: Pigeon.Result<Pigeon.StartScanResult>?) {
        val scanner = this.scanner
        if (scanner == null) {
            result?.error(Exception("Scanner not running"))
            return
        }

        scanner.startScan(
            onData = { data ->
                ContextCompat.getMainExecutor(activity).execute {
                    android.util.Log.i("ExampleBarcodeScanner", "data: $data")
                    flutterApi?.onScan(data) {}
                }
            },
            onComplete = {
                result?.success(it)
            }
        )
    }

    override fun stopScan(result: Pigeon.Result<Void>?) {
        scanner?.stopScan()
        result?.success(null)
    }


}
