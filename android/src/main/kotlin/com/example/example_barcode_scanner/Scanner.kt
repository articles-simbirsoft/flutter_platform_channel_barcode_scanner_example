package com.example.example_barcode_scanner

import android.app.Activity

interface Scanner {

    fun getDeviceType(): Pigeon.ScannerType

    fun onActivityAttach(activity: Activity)

    fun onActivityDetach(activity: Activity)

    fun startScan(
        onData: (String) -> Unit,
        onComplete: (Pigeon.StartScanResult) -> Unit,
    )

    fun stopScan(activity: Activity?)
}