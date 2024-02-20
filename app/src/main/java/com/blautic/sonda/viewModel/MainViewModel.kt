package com.blautic.sonda.viewModel


import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blautic.sonda.R
import com.blautic.sonda.ble.device.BleManager
import com.blautic.sonda.utils.ArrayToExcel
import com.blautic.sonda.utils.Util
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


class MainViewModel(
    context: Context
): ViewModel() {

    private var bleManager = BleManager(context = context)

    var conected = false
        get() =  bleManager.isConnected

    var capturandoDatos = false
     var numFase = 0


    fun subirFase(){
        numFase++
    }

    fun resetFase(){
        numFase = 0
    }

    val arrayDatosExp = mutableListOf<Array<String>>()

    var userCode = "no_name"

    fun connectionState() = bleManager.connectionStateFlow.asLiveData()
    fun statusFlow() = bleManager.statusFlow
    fun presionFlow() = bleManager.presionFlow
    fun mpuFlow() = bleManager.mpuFlow
    fun anglesFlow() = bleManager.anglesFlow

    fun connect(mac: String) {
        bleManager.connectToDevice(mac)
    }

    fun collectDataExp(context: Context){
        Util.showMessage(context, "almacenamiento de datos iniciado")
        val filteredPresionFlow = presionFlow().takeWhile { capturandoDatos == true }
        val filteredAnglesFlow = anglesFlow().takeWhile { capturandoDatos == true }
        // variable para pruebas:
        //val controlFlow = createControlFlow().takeWhile { capturandoDatos == true }

        val combinedExpFlow = combine(filteredPresionFlow, filteredAnglesFlow){ presFlowValue, angleFlowValue ->//controlFlow){ presFlowValue, angleFlowValue, periodicTestFlow ->
            Log.d("info", "$presFlowValue $angleFlowValue")
            Pair(presFlowValue, angleFlowValue)
        }

        viewModelScope.launch {
            combinedExpFlow.collect {

                arrayDatosExp.add(
                    arrayOf(
                        SimpleDateFormat("HH:mm:ss").format(Date()),
                        numFase.toString(),
                        String.format("%.1f", it.first?.get(0)?: 0F),
                        String.format("%.1f", it.first?.get(1)?: 0F),
                        String.format("%.1f", it.first?.get(2)?: 0F),
                        String.format("%.1f", it.first?.get(3)?: 0F),
                        String.format("%.1f", it.first?.get(4)?: 0F),
                        String.format("%.1f", it.first?.get(5)?: 0F),
                        String.format("%.1f", it.first?.get(6)?: 0F),
                        String.format("%.1f", it.second?.get(0)?: 0F),
                        String.format("%.1f", it.second?.get(1)?: 0F)
                    )
                )
                Log.d("info", "nueva fila: ${arrayDatosExp.last()[0]}")

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
        saveFile("${userCode}_$timeStamp.xls")
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

    /////////////////////////////////////
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

    //////////////////////////

    fun generatedExcel(context: Context, uri: Uri, tables: String?) {
        tables?.let {
            generarExcel(context, uri, tables)
        }
    }

    private fun generarExcel(
        context: Context,
        uri: Uri,
        tables: String?
    ) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Exporting data, please wait..")
        progressDialog.show()
        arrayDatosExp.addAll(0,
            listOf(
                arrayOf("Paciente: ", userCode),
                arrayOf(""),
                arrayOf("time", "fase", "% pres1", "% pres2", "% pres3", "% pres4", "% pres5", "% pres6", "% pres7", "% flex", "% inclin")
            )
        )
        //arrayDatosExp.add(0,arrayOf("data_time", "pres1", "pres2", "pres3", "pres4", "pres5", "pres6", "flexion", "inclinacion"))
        val builder = ArrayToExcel.Builder(context, arrayDatosExp.toTypedArray(), userCode)
        builder.setTables(tables)
        builder.setOutputPath(context.filesDir.path)
        builder.setOutputFileName("monitor.xls")
        builder.start(object : ArrayToExcel.ExportListener {

            override fun onStart() {
                progressDialog.show()
                Timber.d("onStart")
            }

            override fun onCompleted(filePath: String?) {
                Timber.d("onCompleted %s", filePath)
                progressDialog.dismiss()
                try {
                    val outputStream = context.contentResolver.openOutputStream(uri)
                    outputStream?.let {
                        Util.copy(File(filePath), outputStream)
                        Util.showMessage(context, "Datos guardados en: $filePath")//uri.path)
                    }
                    //arrayDatosExp.clear()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(e: Exception?) {
                progressDialog.dismiss()
                Timber.e(e)
            }
        })
    }

    fun createControlFlow(): Flow<Unit> = flow {
        while (true) {
            // Emitir un valor de control (puede ser cualquier valor, usaremos Unit aquí)
            emit(Unit)

            // Esperar dos segundos antes de emitir el siguiente valor
            delay(2000)
        }
    }

    fun getAppVersion(context: Context): String{
        var version:String? = null

        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            /*val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                //VERSION.SDK_INT < P
                packageInfo.versionCode
            }*/
            val versionName = packageInfo.versionName
            Log.d("info", packageInfo.versionName)
            version = versionName

        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return version?: "--"

    }
}

