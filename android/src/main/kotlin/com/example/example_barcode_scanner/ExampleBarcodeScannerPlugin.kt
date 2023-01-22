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

private const val START_SCAN = "startScan"
private const val GET_DEVICE_TYPE = "getDeviceType"
private const val STOP_SCAN = "stopScan"

/** ExampleBarcodeScannerPlugin */
class ExampleBarcodeScannerPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private var activity: Activity? = null

    // Hardware scan
    private var scanner: Scanner? = null
    private var textureRegistry: TextureRegistry? = null;


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "example_barcode_scanner")
        channel.setMethodCallHandler(this)

        textureRegistry = flutterPluginBinding.textureRegistry
    }

    override fun onDetachedFromEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        textureRegistry = null
    }


    override fun onAttachedToActivity(activityBinding: ActivityPluginBinding) {
        activity = activityBinding.activity
        val deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} ${Build.DEVICE}"
        android.util.Log.i("SCANNED", deviceInfo)
        scanner = when {
            else -> CameraScanner(textureRegistry!!)
        }
        try {
            scanner?.onActivityAttach(activity!!)
        } catch (e: Throwable) {
            android.util.Log.e("SCANNED", deviceInfo, e)
        }
    }

    override fun onDetachedFromActivityForConfigChanges() {
        TODO("Not yet implemented")
    }

    override fun onReattachedToActivityForConfigChanges(activityBindingScanner: ActivityPluginBinding) {
        TODO("Not yet implemented")
    }

    override fun onDetachedFromActivity() {
        Log.i("ExampleBarcodeScannerPlugin", "onDetachedFromActivity")

        scanner?.onActivityDetach(activity!!)
        scanner = null
        activity = null
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        Log.i("ExampleBarcodeScannerPlugin", "onMethodCall")

        val scanner = this.scanner
        when (call.method) {
            START_SCAN -> {
                activity?.let {
                    scanner?.startScan(
                        it,
                        onData = { data ->
                            ContextCompat.getMainExecutor(activity).execute {
                                android.util.Log.i("SCANNED", data)
                                channel.invokeMethod("onScan", data)
                            }
                        },
                        onError = { error ->
                            ContextCompat.getMainExecutor(activity).execute {
                                android.util.Log.i("SCAN ERROR", error.toString())
                                channel.invokeMethod("onError", error)
                            }
                        }
                    )
                }
                if(scanner is CameraScanner){
                    result.success(scanner.textureId)
                } else {
                    result.success(null)
                }
            }
            STOP_SCAN -> {
                scanner?.stopScan(activity);
                result.success(null)
            }
            GET_DEVICE_TYPE -> {
                result.success(scanner?.getDeviceType()?.name?.lowercase())
            }
            else -> result.notImplemented()
        }
    }


}
