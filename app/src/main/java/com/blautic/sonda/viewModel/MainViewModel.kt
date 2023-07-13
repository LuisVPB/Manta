package com.blautic.sonda.viewModel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.blautic.sonda.R
import com.blautic.sonda.ble.device.BleManager

class MainViewModel(
    context: Context
): ViewModel() {

    private var bleManager = BleManager(context = context)

    var conected = false
        get() =  bleManager.isConnected

    fun connectionState() = bleManager.connectionStateFlow.asLiveData()
    fun statusFlow() = bleManager.statusFlow
    fun presionFlow() = bleManager.presionFlow
    fun mpuFlow() = bleManager.mpuFlow
    fun anglesFlow() = bleManager.anglesFlow

    fun connect(mac: String) {
        bleManager.connectToDevice(mac)
    }

    fun disconnect(mac: String) {
        bleManager.disconnect()
    }

    fun startScan(){
        bleManager.startBleScan()
    }

    fun getBatteryLevelDrawable(level: Int?): Int {
        return when {
            level == null -> R.drawable.ic_battery_unknown
            level in 0..25 -> R.drawable.ic_battery_1
            level in 26..50 -> R.drawable.ic_battery_3
            level in 51..75 -> R.drawable.ic_battery_5
            level in 76..100 -> R.drawable.ic_battery_full
            else -> R.drawable.ic_battery_alert
        }
    }


    // funciones de comprobación de permisos
    /*fun enableBluetooth(activity: Activity, requestCode: Int) {
        if (requestCode >= 0) {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter != null && !adapter.isEnabled) {
                val intent = Intent("android.bluetooth.adapter.action.REQUEST_ENABLE")
                activity.startActivityForResult(intent, requestCode)
            }
        }
    }*/


    /*fun checkGPSIsEnable(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

     */

    /*fun hasPermissions(context: Context, PERMISSIONS_REQUIRED: Array<String>): Boolean {
        return PERMISSIONS_REQUIRED.any { permission: String ->
            ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

     */

    //val isBluetoothOn = deviceManager.isBluetoothOn()
}
