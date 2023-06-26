package com.blautic.sonda

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blautic.sonda.ble.device.DeviceManager
import com.blautic.sonda.databinding.ActivityMainBinding
import com.blautic.sonda.viewModel.MainViewModel
import com.blautic.sonda.viewModel.MainViewModelFactory
import com.diegulog.ble.gatt.ConnectionState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

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

        mainViewModelFactory = MainViewModelFactory(DeviceManager(this), this)
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

        //checkBlePermissions()

        binding.btConnect.setOnClickListener {

            viewModel.connect("77:77:77:77:77:77")

        }

        lifecycleScope.launch {
            viewModel.statusFlow().collect {
                binding.battery.text = "${it ?: 0}"
            }

        }

        lifecycleScope.launch {
            viewModel.presionFlow().collect {
                binding.apply {
                    presSensor1.text= "P1:${it?.get(0) ?: "sin valores"} %"
                    presSensor2.text= "P2:${it?.get(1) ?: "sin valores"} %"
                    presSensor3.text= "P3:${it?.get(2) ?: "sin valores"} %"
                    presSensor4.text= "P4:${it?.get(3) ?: "sin valores"} %"
                    presSensor5.text= "P5:${it?.get(4) ?: "sin valores"} %"
                    presSensor6.text= "P6:${it?.get(5) ?: "sin valores"} %"
                }
            }
        }

        lifecycleScope.launch {
            viewModel.mpuFlow().collect {

            }
        }


        viewModel.connectionState().observeForever {
            when(it){
                ConnectionState.DISCONNECTED -> {
                    binding.tvConexion.text = "disconnected"
                    binding.ivConexion.setColorFilter(Color.parseColor("#CF1313"))
                }
                ConnectionState.CONNECTING -> {

                }
                ConnectionState.CONNECTED -> {
                    binding.tvConexion.text = "connected"
                    binding.ivConexion.setColorFilter(Color.parseColor("#4CAF50"))

                }
                ConnectionState.DISCONNECTING -> {

                }
                ConnectionState.FAILED -> {}

            }
        }
    }

    //////////////////////////////////

    private fun checkBlePermissions(): Boolean {
        return if (viewModel.hasPermissions(this, PERMISSIONS_REQUIRED)) {
            isBleAndGpsEnable()
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
            false
        }
    }

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