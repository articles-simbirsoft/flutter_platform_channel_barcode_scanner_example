package com.example.example_barcode_scanner

import android.app.Activity

interface Scanner {

    fun getDeviceType(): DeviceType

    fun onActivityAttach(activity: Activity)

    fun onActivityDetach(activity: Activity)

    fun startScan(
        activity: Activity, onData: (String) -> Unit, onError: (String?) -> Unit
    )

    fun stopScan(activity: Activity?)
}