package com.example.example_barcode_scanner

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias SuccessListener = (String) -> Unit

/**
 * класс анализатора изображения для распознования EAN-13 и EAN-8 штрихкодов
 */
class MlKitCodeAnalyzer(
    private val barcodeListener: SuccessListener,
) : ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient(
        defaultOptions()
    )


    private fun defaultOptions() = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
        )
        .build()

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage != null) {
            val mlImage = InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees)
            val currentTimestamp = System.currentTimeMillis()
            scanner.process(mlImage)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty() && barcodes[0].rawValue != null) barcodes[0].rawValue?.let {
                        barcodeListener(
                            it
                        )
                    }
                }
                .addOnCompleteListener {
                    // Позволяет производить сканирование раз в секунду
                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000 - (System.currentTimeMillis() - currentTimestamp))
                        image.close()
                    }
                }
        }
    }
}