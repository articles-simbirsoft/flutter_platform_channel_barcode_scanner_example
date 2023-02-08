package com.example.example_barcode_scanner

import android.app.Activity

/**
 * Интерфейс сканера
 * */
interface Scanner {

    fun onActivityAttach(activity: Activity)

    fun onActivityDetach(activity: Activity)

    /**
     * метод запуска сканера
     * @param onData - callback вызываемый при распозновании штрихкода
     * @param onComplete - callback вызываемый при завершении запуска зканера
     * */
    fun startScan(
        onData: (String) -> Unit,
        onComplete: (Pigeon.StartScanResult) -> Unit,
    )

    /**
     * Метод остановки сканера
     * */
    fun stopScan()
}