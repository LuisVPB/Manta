package com.blautic.sonda.viewModel


import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blautic.sonda.R
import com.blautic.sonda.ble.device.BleManager
import com.blautic.sonda.utils.Util
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class MainViewModel(
    context: Context
): ViewModel() {

    private var bleManager = BleManager(context = context)

    var conected = false
        get() =  bleManager.isConnected

    var arrayDatosExp = mutableListOf<Array<String>>()

    fun connectionState() = bleManager.connectionStateFlow.asLiveData()
    fun statusFlow() = bleManager.statusFlow
    fun presionFlow() = bleManager.presionFlow
    fun mpuFlow() = bleManager.mpuFlow
    fun anglesFlow() = bleManager.anglesFlow

    fun connect(mac: String) {
        bleManager.connectToDevice(mac)
    }

    fun collectDataExp(){
        viewModelScope.launch {
            presionFlow().collect {

                arrayDatosExp.add(
                    arrayOf(
                        String.format("%.1f", it?.get(0)?: 0F),
                        String.format("%.1f", it?.get(1)?: 0F),
                        String.format("%.1f", it?.get(2)?: 0F),
                        String.format("%.1f", it?.get(3)?: 0F),
                        String.format("%.1f", it?.get(4)?: 0F),
                        String.format("%.1f", it?.get(5)?: 0F)
                    )
                )
                Log.d("info", arrayDatosExp.toString())

            }
        }
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

    fun startExport(arl: ActivityResultLauncher<Intent>){
        exportExcelActivityResult = arl
        val timeStamp = SimpleDateFormat("MMdd_HHmmss").format(Date())
        saveFile("monitor_$timeStamp.xls")
    }
    private fun saveFile(name: String?) {
        val exportIntent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        exportIntent.addCategory(Intent.CATEGORY_OPENABLE)
        val myMime = MimeTypeMap.getSingleton()
        val mimeType = myMime.getMimeTypeFromExtension("xls")
        exportIntent.type = mimeType
        exportIntent.putExtra(Intent.EXTRA_TITLE, name)
        exportExcelActivityResult.launch(exportIntent)
    }

    private lateinit var exportExcelActivityResult: ActivityResultLauncher<Intent>


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
