package com.blautic.sonda

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blautic.sonda.databinding.ActivityMainBinding
import com.blautic.sonda.viewModel.MainViewModel
import com.blautic.sonda.viewModel.MainViewModelFactory
import com.diegulog.ble.gatt.ConnectionState
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import com.blautic.sonda.utils.Util

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

    private val exportExcelActivityResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            data?.let {
                Util.generatedExcel(
                    this,
                    it.data!!,
                    "resultados"
                )
            }
        }
    }

    //Permisos:
    var PERMISSIONS_REQUIRED = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    var PERMISSIONS_REQUEST_CODE = 10

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModelFactory = MainViewModelFactory(this)
        viewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        binding.btCaptura.setOnClickListener {

            if (viewModel.capturandoDatos){
                viewModel.capturandoDatos = false

                // Activo el guardado en excel:
                viewModel.startExport(exportExcelActivityResult)
                binding.btCaptura.setImageResource(R.drawable.ic_collect)
                viewModel.arrayDatosExp.clear()
            } else {
                viewModel.capturandoDatos = true
                viewModel.collectDataExp()
                binding.btCaptura.setImageResource(R.drawable.ic_xls)
            }




            //

        }

        binding.btConnect.setOnClickListener {
            var estadoConex = viewModel.conected
            if(estadoConex) {
                viewModel.disconnect("77:77:77:77:77:77")
            } else {
                viewModel.connect("77:77:77:77:77:77")
            }
        }

        // Mostrar estado de batería:
        lifecycleScope.launch {
            viewModel.statusFlow().collect {
                binding.ivBattery.setImageResource(viewModel.getBatteryLevelDrawable(it))
                binding.tvBattery.text = "${it?:""}"
            }

        }

        // Mostrar valores de sensores de presión:
        lifecycleScope.launch {

            viewModel.presionFlow().collect {
                binding.apply {
                    presSensor1.text= "P1: ${String.format("%.1f", it?.get(0)?: 0F)} %"
                    presSensor2.text= "P2: ${String.format("%.1f", it?.get(1)?: 0F)} %"
                    presSensor3.text= "P3: ${String.format("%.1f", it?.get(2)?: 0F)} %"
                    presSensor4.text= "P4: ${String.format("%.1f", it?.get(3)?: 0F)} %"
                    presSensor5.text= "P5: ${String.format("%.1f", it?.get(4)?: 0F)} %"
                    presSensor6.text= "P6: ${String.format("%.1f", it?.get(5)?: 0F)} %"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.mpuFlow().collect {

            }
        }

        // Mostrar valores de angulos:
        lifecycleScope.launch {
            viewModel.anglesFlow().collect {

                binding.percentFlex.text = "${(it?.getOrNull(0)?.roundToInt()?.absoluteValue ?: 0)}º"
                binding.percentIncl.text = "${(it?.getOrNull(1)?.roundToInt()?.absoluteValue ?: 0)}º"

                binding.progressFlex.setPolarProgress(it?.getOrNull(0)?.roundToInt() ?: 0)
                binding.progressIncl.setPolarProgress(it?.getOrNull(1)?.roundToInt() ?: 0)
            }
        }

        // Mostrar estado de conexión:
        viewModel.connectionState().observeForever {
            var estado = it
            binding.progressConnection.isVisible = it == ConnectionState.CONNECTING
            binding.tvConectando.isVisible = it == ConnectionState.CONNECTING

            when(it){
                ConnectionState.DISCONNECTED -> {
                    binding.tvConexion.text = "disconnected"
                    binding.ivConexion.setColorFilter(Color.parseColor("#CF1313"))
                    binding.ivBattery.setImageResource(viewModel.getBatteryLevelDrawable(null))
                    binding.btConnect.text = "Conectar"
                    binding.tvBattery.text = ""
                }
                ConnectionState.CONNECTING -> {
                    Log.i("conexion","conectando......")
                }
                ConnectionState.CONNECTED -> {
                    binding.tvConexion.text = "connected"
                    binding.ivConexion.setColorFilter(Color.parseColor("#4CAF50"))
                    binding.btConnect.text = "Desconectar"
                }
                ConnectionState.DISCONNECTING -> {

                }
                ConnectionState.FAILED -> {
                    binding.tvConexion.text = "disconnected"
                    binding.ivConexion.setColorFilter(Color.parseColor("#CF1313"))
                    binding.btConnect.text = "Conectar"
                    binding.tvBattery.text = ""
                    Toast.makeText(this, "se ha perdido la conexión con el dispositivo", Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    //////////////////////////////////

    /*
    private fun checkBlePermissions(): Boolean {
        return if (viewModel.hasPermissions(this, PERMISSIONS_REQUIRED)) {
            isBleAndGpsEnable()
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
            false
        }
    }
     */

    /*
    private fun isBleAndGpsEnable(): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !viewModel.checkGPSIsEnable(this)) {
            MaterialAlertDialogBuilder(this)
                .setMessage(R.string.enable_gps)
                .setNegativeButton(
                    android.R.string.cancel
                ) { _, _ -> finish() }
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(intent, PERMISSIONS_REQUEST_CODE)
                }
                .setCancelable(false)
                .show()
            return false
        }
        if (!viewModel.isBluetoothOn) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.enable_bluetooth)
                .setNegativeButton(
                    android.R.string.cancel
                ) { _, _ -> finish() }
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ -> viewModel.enableBluetooth(this, 102) }
                .setCancelable(false)
                .show()
            return false
        }
        return true
    }

     */

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED != grantResults[0]) {
                finish()
            }
        }
    }

}

    //////////////////////////////////////

private fun CircularProgressIndicator.setPolarProgress(value: Int) {
    if (value < 0) {
        indicatorDirection = CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE
    } else {
        indicatorDirection = CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE
    }
    progress =  (value.absoluteValue * 100) / 180
}

