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


    private val analysisExecutor = Executors.newSingleThreadExecutor()
    private var camera: Camera? = null
    private var processCameraProvider: ProcessCameraProvider? = null

    private var textureEntry: SurfaceTextureEntry? = null
    val textureId: Long? get() = textureEntry?.id()

    override fun getDeviceType(): DeviceType {
        return DeviceType.CAMERA
    }

    private fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    private fun checkCameraPermission(activity: Activity): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityAttach(activity: Activity) {
    }

    override fun onActivityDetach(activity: Activity) {
        release()
    }

    override fun startScan(
        activity: Activity,
        onData: (String) -> Unit,
        onError: (String?) -> Unit
    ) {
        if (!checkCameraPermission(activity)) {
            requestCameraPermission(activity)
        }

        val textureEntry: SurfaceTextureEntry =
            if (this.textureEntry == null) textureRegistry.createSurfaceTexture() else this.textureEntry!!

        this.textureEntry = textureEntry

        val surfaceTexture = textureEntry.surfaceTexture()
        val mainThreadExecutor = ContextCompat.getMainExecutor(activity.baseContext)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity)


        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                processCameraProvider = cameraProvider
                val preview = Preview.Builder()
                    .build()
                preview.setSurfaceProvider { request ->
                    val reqRes = request.resolution
                    surfaceTexture.setDefaultBufferSize(reqRes.width, reqRes.height)
                    request.provideSurface(Surface(surfaceTexture), mainThreadExecutor) {}
                }
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                val analyzer: ImageAnalysis.Analyzer = MlKitCodeAnalyzer(
                    barcodeListener = onData,
                    errorListener = onError,
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
                } catch (e: Exception) {
                    Log.e("PreviewUseCase", "Binding failed! :(", e)
                    //TODO Handler error
                }
            },
            ContextCompat.getMainExecutor(activity),
        )

    }

    override fun stopScan(activity: Activity?) {
        release()
    }

    private fun release() {
        processCameraProvider?.unbindAll()
        textureEntry?.release()
        processCameraProvider = null
        textureEntry = null
    }
}