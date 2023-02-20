package com.example.example_barcode_scanner

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.Surface
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import io.flutter.view.TextureRegistry
import io.flutter.view.TextureRegistry.SurfaceTextureEntry
import java.util.concurrent.Executors

private const val CAMERA_PERMISSION_REQUEST_CODE = 1001

class CameraScanner(private val textureRegistry: TextureRegistry) : Scanner {

    // Экзекьютор анализатора изображений
    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var camera: Camera? = null
    private var processCameraProvider: ProcessCameraProvider? = null
    private var activity: Activity? = null

    private var textureEntry: SurfaceTextureEntry? = null

    private fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkCameraPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityAttach(activity: Activity) {
        this.activity = activity
    }

    override fun onActivityDetach(activity: Activity) {
        this.activity = null
        release()
    }

    override fun startScan(
        onData: (String) -> Unit,
        onComplete: (Pigeon.StartScanResult) -> Unit,
    ) {
        val activity = this.activity ?: return

        if (!checkCameraPermission(activity)) {
            requestCameraPermission(activity)
        }

        val textureEntry: SurfaceTextureEntry =
            if (this.textureEntry == null) textureRegistry.createSurfaceTexture() else this.textureEntry!!

        this.textureEntry = textureEntry

        val mainThreadExecutor = ContextCompat.getMainExecutor(activity.baseContext)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)
        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                processCameraProvider = cameraProvider
                val preview = Preview.Builder().build()
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                val analyzer: ImageAnalysis.Analyzer = MlKitCodeAnalyzer(
                    barcodeListener = onData,
                )
                imageAnalysis.setAnalyzer(analysisExecutor, analyzer)
                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        activity as LifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                    preview.setSurfaceProvider { request ->
                        val reqRes = request.resolution

                        // связываем текстуру и превью
                        val surfaceTexture = textureEntry.surfaceTexture()
                        surfaceTexture.setDefaultBufferSize(reqRes.width, reqRes.height)
                        request.provideSurface(Surface(surfaceTexture), mainThreadExecutor) {}

                        // формируем результат
                        val cameraProperties = Pigeon.CameraProperties.Builder()
                            .setAspectRatio(reqRes.height.toDouble() / reqRes.width.toDouble())
                            .setHeight(reqRes.height.toLong()).setWidth(reqRes.width.toLong())
                            .setTextureId(textureEntry.id()).build()
                        val startScanResult = Pigeon.StartScanResult.Builder()
                            .setScannerType(Pigeon.ScannerType.CAMERA)
                            .setCameraProperties(cameraProperties).build()

                        // отправляем результат через вызов onComplete
                        onComplete(startScanResult)
                    }


                } catch (e: Exception) {
                    Log.e("ExampleBarcodeScanner", "Binding failed! :(", e)
                    //TODO Handler error
                }
            },
            ContextCompat.getMainExecutor(activity),
        )

    }

    override fun stopScan() {
        release()
    }

    private fun release() {
        processCameraProvider?.unbindAll()
        textureEntry?.release()
        processCameraProvider = null
        textureEntry = null
    }
}