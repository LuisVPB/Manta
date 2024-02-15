package com.blautic.sonda.ble.device

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat.startActivityForResult
import com.blautic.sonda.ble.device.mpu.Mpu
import com.diegulog.ble.BleBytesParser
import com.diegulog.ble.gatt.ConnectionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.util.*

class BleManager(private var context: Context) {

    private val accelerometer = Mpu()

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val scanSettings =
        ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()

    private val bleScanner by lazy {
        bluetoothAdapter?.bluetoothLeScanner
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
    private val _presionFlow = MutableStateFlow<MutableList<Float?>?>(null)
    val presionFlow get() = _presionFlow.asStateFlow()

    // Valores de MPU
    private val _mpuFlow = MutableStateFlow<MutableList<Int?>?>(null)
    val mpuFlow get() = _mpuFlow.asStateFlow()

    // Ángulos asociados al MPU
    private val _anglesFlow = MutableStateFlow<MutableList<Float?>?>(null)
    val anglesFlow get() = _anglesFlow.asStateFlow()

    var isConnected = false
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
    * Conecta Con el dispositivo pasándole la dirección MAC
    */
    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        val device: BluetoothDevice? = bluetoothAdapter?.getRemoteDevice(address)
        if (bleAdapterIsEnable() && !isConnected && !isConnecting) {
            isConnecting = true
            _connectionStateFlow.value = ConnectionState.CONNECTING
            gattCharacteristic =
                device?.connectGatt(context, true, gattCallBack, BluetoothDevice.TRANSPORT_LE)
        } else if (!bleAdapterIsEnable()){
            /*
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            bluetoothAdapter?.takeIf {
                !it.isEnabled }?.apply {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }

             */
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

            Log.i("BluetoothGattCallback", "onConnectionStateChange status: $status")
            Log.i("BluetoothGattCallback", "onConnectionStateChange newState: $newState")

            val deviceAddress = gatt?.device?.address
            _scanResultFlow.value = null

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
                        .w("Successfully CONNECTING to %s", deviceAddress)

                    _connectionStateFlow.value = ConnectionState.CONNECTING

                    isConnected = false
                    isConnecting = true
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Timber.tag("BluetoothGattCallback")
                        .w("Successfully DISCONNECTING to %s", deviceAddress)

                    _connectionStateFlow.value = ConnectionState.DISCONNECTING

                    isConnected = false
                    isConnecting = false
                }
            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                /*
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
                            .w("Successfully CONNECTING to %s", deviceAddress)

                        _connectionStateFlow.value = ConnectionState.CONNECTING

                        isConnected = false
                        isConnecting = true
                    }
                    BluetoothProfile.STATE_DISCONNECTING -> {
                        Timber.tag("BluetoothGattCallback")
                            .w("Successfully DISCONNECTING to %s", deviceAddress)

                        _connectionStateFlow.value = ConnectionState.DISCONNECTING

                        isConnected = false
                        isConnecting = false
                    }
                }

                 */

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


            val parse = BleBytesParser(characteristic?.value)


            when (characteristic?.uuid) {

                BleUUID.UUID_STATUS_CHARACTERISTIC -> {

                    Log.d("NOTIFICATION ${characteristic?.uuid}",
                        "status = ${characteristic?.value.contentToString()}")

                    characteristic?.let {
                        if (it.value.size >= 2) {
                            it.value.apply {
                                val combinedValue = ((this[1].toInt() and 0xFF) shl 8) or (this[0].toInt() and 0xFF)

                                setPowerVal(combinedValue)

                                // Actualizar la variable entera con el valor combinado
                                _statusFlow.value = battery

                                Log.d("NOTIFICATION ${characteristic?.uuid}",
                                    "batería transformada= $battery")
                                Log.d("NOTIFICATION ${characteristic?.uuid}",
                                    "batería bruto = $combinedValue")
                            }
                        }
                    }

                //ACTUALIZAR EL FLOW DEL ESTATUS
                }

                BleUUID.UUID_PRESION_CHARACTERISTIC -> {
                    Log.d("NOTIFICATION ${characteristic?.uuid}",
                        "presión crudo = ${characteristic?.value.contentToString()}")

                    characteristic?.let {

                            val floats: MutableList<Float?> = mutableListOf()
                            for (i in 0 until it.value.size step 2) {
                                val byte1 = it.value[i]
                                val byte2 = it.value[i + 1]
                                val combinedValue = ((byte2.toInt() and 0xFF) shl 8) or (byte1.toInt() and 0xFF)

                                // Normaliza el valor raw recibido:
                                val normVal = normalization(combinedValue)

                                // Actualizar la variable entera con el valor combinado
                                floats.add(normVal)
                            }

                        Log.d("NOTIFICATION ${characteristic?.uuid}",
                            "presión normal = ${floats.toString()}")

                            //ACTUALIZAR EL FLOW DE PRESIÓN
                            _presionFlow.value = floats
                    }
                }

                BleUUID.UUID_MPU_CHARACTERISTIC -> {
                    Log.d("NOTIFICATION ${characteristic?.uuid}",
                        "mpu = ${characteristic?.value.contentToString()}")

                    characteristic?.let {
                        val valoresBytes: ByteArray = ByteArray(12)
                        val mpuIntegers: MutableList<Int?> = mutableListOf()
                        val anglesIntegers: MutableList<Float?> = mutableListOf()

                        for (i in 0 until it.value.size step 2) {
                            val bytePar = it.value[i]
                            val byteImpar = it.value[i + 1]
                            val combinedValue = ((byteImpar.toInt() and 0xFF) shl 8) or (bytePar.toInt() and 0xFF)
                            mpuIntegers.add(combinedValue)

                            valoresBytes.set(i ,byteImpar)
                            valoresBytes.set(i+1 ,bytePar)
                        }

                        Log.d("NOTIFICATION mpu", valoresBytes.toString())

                        // Actualizar la variable entera con el valor combinado
                        _mpuFlow.value = mpuIntegers

                        val parse = BleBytesParser(it.value)
                        accelerometer.setData(parse)
                        Log.d("angulos_todos","angles: xy: ${Integer.toHexString(accelerometer.angles.xy.toInt())} ,xz: ${Integer.toHexString(accelerometer.angles.xz.toInt())} , zy: ${Integer.toHexString(accelerometer.angles.zy.toInt()) }")
                        anglesIntegers.apply {
                            add(accelerometer.angles.xz)
                            add(accelerometer.angles.zy)
                        }

                        // Actualiza la lista de angulos:
                        _anglesFlow.value = anglesIntegers

                        Log.d("NOTIFICATION ${characteristic?.uuid}",
                            "angles = ${anglesIntegers}")

                    }

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

            enableNotifications(gatt?.getService(BleUUID.UUID_SERVICE)
                ?.getCharacteristic(BleUUID.UUID_MPU_CHARACTERISTIC)!!, true)


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

    private fun normalization(combinedValue: Int): Float {
        Log.d("NOTIFICATION", "val presion combinado $combinedValue")
        val min = 1950
        val result: Float = ((min-combinedValue)/min.toFloat())
        return  if(result >=0) result * 100 else 0F
    }

    @SuppressLint("MissingPermission")
    fun startBleScan() {
        bleScanner?.startScan(null, scanSettings, scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopBleScan() {
        bleScanner?.stopScan(scanCallback)
    }

    private fun bleAdapterIsEnable(): Boolean {
        return bluetoothAdapter?.isEnabled ?: false
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(characteristic: BluetoothGattCharacteristic, enable: Boolean) {
        gattCharacteristic?.setCharacteristicNotification(characteristic, enable)
    }

    private fun getPeriodo(frecuencia: Double): Double {
        return 1 / frecuencia
    }

    private var lasAdcBat = 0
    private val avgBat: MutableList<Int> = ArrayList()
    private var battery = 0

    // Ya se obtiene el valorde adc directamente, no hace falta para la función setPowerVal()
    /*fun setData(parse: BleBytesParser) {
        val adc = parse.getIntValue(BleBytesParser.FORMAT_UINT16)
        setPowerVal(adc)
    }*/

    private fun setPowerVal(adc: Int) {
        if (adc > 0 && adc != lasAdcBat) {
            lasAdcBat = adc
            var bat = 0
            //Calculamos el valor de batería desde adc
            //12 bits: 1400 100%   1365:4.08V 1333:4.0   1309:3.9   1283:3.8 1252:3.7  1204:3.6 1160:3.5  1131:3.4 1110:3.3
            if (adc > powerAdc.first()) bat = 100 else if (adc < powerAdc.last()) bat = 0 else {
                for (i in powerAdc.indices) {
                    //Log.d("BAT",key.intValue()+":"+powerMap.get(key) +" < "+adc);
                    if (powerAdc[i] < adc) {
                        bat = powerPerc[i]
                        break
                    }
                }
            }
            if (avgBat.size >= NSAMPLES_AVG_BATT) avgBat.removeAt(0)

            avgBat.add(bat)
            battery = avgBat.average().toInt()
        }
    }

    companion object {
        private const val NSAMPLES_AVG_BATT = 10
        private val powerAdc = intArrayOf(
            1417, 1407, 1396, 1386, 1375, 1365, 1354, 1344, 1334, 1323,
            1313, 1302, 1292, 1281, 1271, 1260, 1250, 1194, 1180, 1166, 1093
        )
        private val powerPerc = intArrayOf(
            100, 95, 90, 85, 80, 75, 70, 65, 60, 55,
            50, 45, 40, 35, 30, 25, 20, 15, 10, 5, 0
        )
    }

}