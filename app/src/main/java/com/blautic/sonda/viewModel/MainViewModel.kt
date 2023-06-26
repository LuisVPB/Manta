package com.blautic.sonda.viewModel


import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.blautic.sonda.ble.device.BleManager

class MainViewModel(
    context: Context
): ViewModel() {

    private var bleManager = BleManager(context = context)

    fun connectionState() = bleManager.connectionStateFlow.asLiveData()
    fun statusFlow() = bleManager.statusFlow
    fun presionFlow() = bleManager.presionFlow
    fun mpuFlow() = bleManager.mpuFlow

    fun connect(mac: String) {
        bleManager.connectToDevice(mac)
    }

    fun startScan(){
        bleManager.startBleScan()
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
