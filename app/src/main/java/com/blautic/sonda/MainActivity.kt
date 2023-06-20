package com.blautic.sonda

import android.Manifest
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.blautic.sonda.ble.device.DeviceManager
import com.blautic.sonda.databinding.ActivityMainBinding
import com.blautic.sonda.viewModel.MainViewModel
import com.blautic.sonda.viewModel.MainViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var mainViewModelFactory: MainViewModelFactory

    //Permisos:
    var PERMISSIONS_REQUIRED = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        //Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
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

        val arg1 = "Valor "
        val arg2 = 10 //por el momento hay dos argumentos genéricos
        mainViewModelFactory = MainViewModelFactory(DeviceManager(this), arg2)
        viewModel = ViewModelProvider(this, mainViewModelFactory).get(MainViewModel::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
    }
}