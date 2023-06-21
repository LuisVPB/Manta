package com.blautic.sonda.viewModel


import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blautic.sonda.ble.device.DeviceManager

class MainViewModel(
    val deviceManager: DeviceManager,
    arg2: Int
): ViewModel() {

    val isBluetoothOn = deviceManager.isBluetoothOn()

    fun enableBluetooth(activity: Activity, requestCode: Int) {
        if (requestCode >= 0) {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null && !adapter.isEnabled) {
                val intent = Intent("android.bluetooth.adapter.action.REQUEST_ENABLE")
                activity.startActivityForResult(intent, requestCode)
            }
        }
    }

    fun checkGPSIsEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun hasPermissions(context: Context, PERMISSIONS_REQUIRED: Array<String>): Boolean {
        return PERMISSIONS_REQUIRED.any { permission: String ->
            ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}
