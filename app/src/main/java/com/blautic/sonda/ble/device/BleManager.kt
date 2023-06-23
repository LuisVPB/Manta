package com.blautic.sonda.ble.device

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.util.Log
import com.diegulog.ble.gatt.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.*

class BleManager(private var context: Context) {

    var x = 0

    private val bluetoothAdapter: BluetoothAdapter by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    //SCAN RESULT
    private val _scanResultFlow = MutableStateFlow<ScanResult?>(null)
    val scanResultFlow get() = _scanResultFlow.asStateFlow()

    // Conexión
    private val _connectionStateFlow = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionStateFlow: StateFlow<ConnectionState> get() = _connectionStateFlow

    // Valores de estatus
    private val _statusFlow = MutableStateFlow<Int?>(null)
    val statusFlow get() = _statusFlow.asStateFlow()

    // Valores Presion
    private val _presionFlow = MutableStateFlow<MutableList<Int?>?>(null)
    val presionFlow get() = _presionFlow.asStateFlow()

    private var isConnected = false
    private var isConnecting = false

    private var gattCharacteristic: BluetoothGatt? = null

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                Log.i("ScanCallback",
                    "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address, RSSI: ${result.rssi}")

                if (this.address == "77:77:77:77:77:77") {
                    _scanResultFlow.value = result
                    connectToDevice(this.address)
                }

            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Log.e("ScanCallBack", "OnScanFailed: code $errorCode")
        }
    }


    /*
    * Conecta Con un dispositivo de electroestimulación pasándole la dirección MAC
    */
    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        val device: BluetoothDevice? = bluetoothAdapter.getRemoteDevice(address)
        if (bleAdapterIsEnable() && !isConnected && !isConnecting) {
            isConnecting = true
            gattCharacteristic =
                device?.connectGatt(context, false, gattCallBack, BluetoothDevice.TRANSPORT_LE)
        }
    }

    /*
    * Desconecta el dispositivo de electroestimulación conectado
    */
    @SuppressLint("MissingPermission")
    fun disconnect() {
        gattCharacteristic?.disconnect()
        isConnecting = false
    }

    //Callback para la gestión de la conexión
    private val gattCallBack = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")

        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.w("BluetoothGattCallback", "onConnectionStateChange $status")
            super.onConnectionStateChange(gatt, status, newState)

            val deviceAddress = gatt?.device?.address
            _scanResultFlow.value = null

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {

                        Timber.tag("BluetoothGattCallback")
                            .w("Successfully connected to %s", deviceAddress)

                        _connectionStateFlow.value = ConnectionState.CONNECTED

                        isConnecting = false
                        isConnected = true
                        gatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        Timber.tag("BluetoothGattCallback")
                            .w("Successfully disconnected from %s", deviceAddress)

                        _connectionStateFlow.value = ConnectionState.DISCONNECTED

                        isConnected = false
                        isConnecting = false
                        gatt?.close()
                    }
                    BluetoothProfile.STATE_CONNECTING -> {
                        Timber.tag("BluetoothGattCallback")
                            .w("Successfully STATE_DISCONNECTED to %s", deviceAddress)

                        _connectionStateFlow.value = ConnectionState.CONNECTING

                        isConnected = false
                        isConnecting = true
                    }
                    BluetoothProfile.STATE_DISCONNECTING -> {
                        Timber.tag("BluetoothGattCallback")
                            .w("Successfully STATE_DISCONNECTING to %s", deviceAddress)

                        _connectionStateFlow.value = ConnectionState.DISCONNECTING

                        isConnected = false
                        isConnecting = false
                    }
                }
            } else {
                Timber.tag("BluetoothGattCallback").w("%s! Disconnecting...",
                    "Error " + status + " encountered for " + deviceAddress)

                _connectionStateFlow.value = ConnectionState.FAILED

                isConnected = false
                isConnecting = false
                gatt?.close()
            }
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
        ) {
            super.onCharacteristicChanged(gatt, characteristic)

            when (characteristic?.uuid) {

                BleUUID.UUID_STATUS_CHARACTERISTIC -> {
                    Log.d("NOTIFICATION ${characteristic?.uuid}",
                        "batería = ${characteristic?.value.contentToString()}")
                    x++

                    characteristic?.let {
                        if (it.value.size >= 2) {
                            it.value.apply {
                                val combinedValue = ((this[1].toInt() and 0xFF) shl 8) or (this[0].toInt() and 0xFF)

                                // Actualizar la variable entera con el valor combinado
                                _statusFlow.value = combinedValue
                            }
                        }
                    }

                //ACTUALIZAR EL FLOW DEL ESTATUS
                }

                BleUUID.UUID_PRESION_CHARACTERISTIC -> {
                    Log.d("NOTIFICATION ${characteristic?.uuid}",
                        "presión = ${characteristic?.value.contentToString()}")

                    characteristic?.let {

                            val integers: MutableList<Int?> = mutableListOf()
                            for (i in 0 until it.value.size step 2) {
                                val byte1 = it.value[i]
                                val byte2 = it.value[i + 1]
                                val combinedValue = ((byte1.toInt() and 0xFF) shl 8) or (byte2.toInt() and 0xFF)
                                integers.add(combinedValue)
                            }
                            _presionFlow.value = integers

                        // Actualizar la variable entera con el valor combinado

                    }


                    //ACTUALIZAR EL FLOW DE PRESIÓN
                }

            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            Log.d("Main", "onServicesDiscovered:")

            gattCharacteristic = gatt

            enableNotifications(gatt?.getService(BleUUID.UUID_SERVICE)
                ?.getCharacteristic(BleUUID.UUID_STATUS_CHARACTERISTIC)!!, true)

            enableNotifications(gatt?.getService(BleUUID.UUID_SERVICE)
                ?.getCharacteristic(BleUUID.UUID_PRESION_CHARACTERISTIC)!!, true)

          /*  enableNotifications(gattCharacteristic?.getService(BleUUID.UUID_SERVICE)
                ?.getCharacteristic(BleUUID.UUID_SYSSTATE)!!, true)

            enableNotifications(gattCharacteristic?.getService(BleUUID.UUID_SERVICE)
                ?.getCharacteristic(BleUUID.UUID_BATT)!!, true)

            enableNotifications(gattCharacteristic?.getService(BleUUID.UUID_SERVICE)
                ?.getCharacteristic(BleUUID.UUID_STATUS)!!, true)*/

            isConnected = true

            stopBleScan()
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("CHARACTERISTIC_WRITE",
                    "Characteristic: " + "${characteristic?.getUuid()}" + " SUCCESS")
            }else {
                Log.d("CHARACTERISTIC_WRITE",
                    "Characteristic: " + "${characteristic?.getUuid()}" + " FAILED")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int,
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
        }
    }

    @SuppressLint("MissingPermission")
    fun startBleScan() {
        bleScanner.startScan(null, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
    }

    private fun bleAdapterIsEnable(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(characteristic: BluetoothGattCharacteristic, enable: Boolean) {
        gattCharacteristic?.setCharacteristicNotification(characteristic, enable)
    }

    private fun getPeriodo(frecuencia: Double): Double {
        return 1 / frecuencia
    }

}