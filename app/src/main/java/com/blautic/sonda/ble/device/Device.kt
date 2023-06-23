package com.blautic.sonda.ble.device

import android.bluetooth.BluetoothGattCharacteristic
import android.util.Log
import com.blautic.sonda.ble.device.DeviceManager.Companion.UUID_SERVICE
import com.blautic.sonda.ble.device.mpu.Mpu
import com.blautic.sonda.utils.toUUID
import com.diegulog.ble.BleBytesParser
import com.diegulog.ble.gatt.BlePeripheral
import com.diegulog.ble.gatt.BlePeripheralCallback
import com.diegulog.ble.gatt.ConnectionPriority
import com.diegulog.ble.gatt.ConnectionState
import com.diegulog.ble.gatt.GattStatus
import com.diegulog.ble.gatt.PhyType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class Device(val numDevice: Int, val address: String, val typeDevice: TypeDevice) : BlePeripheralCallback() {
    private val _connectionStateFlow = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionStateFlow: StateFlow<ConnectionState> get() = _connectionStateFlow

    private val deviceStatus = DeviceStatus()
    private val _deviceStatusFlow = MutableStateFlow<DeviceStatus>(deviceStatus)
    val deviceStatusFlow: StateFlow<DeviceStatus> get() = _deviceStatusFlow

    private val accelerometer = Mpu(nDevice = numDevice)
    private val _mpuFlow = MutableStateFlow(Mpu(nDevice =numDevice))
    val mpuFlow: StateFlow<Mpu> get() = _mpuFlow

    private val _firmwareVersionFlow = MutableStateFlow(0)
    val firmwareVersionFlow: StateFlow<Int> get() = _firmwareVersionFlow

    var autoReconnect = true

    override fun onServicesDiscovered(peripheral: BlePeripheral) {
        Timber.d("onServicesDiscovered")
        peripheral.setNotify(UUID_SERVICE, typeDevice.UUID_STATUS_CHARACTERISTIC.toUUID(), true)
        _connectionStateFlow.value = ConnectionState.CONNECTED
        peripheral.requestConnectionPriority(ConnectionPriority.HIGH)
        peripheral.notifyingCharacteristics//
    }

    override fun onPhyUpdate(peripheral: BlePeripheral, txPhy: PhyType, rxPhy: PhyType, status: GattStatus) {
        super.onPhyUpdate(peripheral, txPhy, rxPhy, status)
        Timber.d("%s %s %s", txPhy, rxPhy, status)
    }

    override fun onCharacteristicWrite(
        peripheral: BlePeripheral,
        value: ByteArray,
        characteristic: BluetoothGattCharacteristic,
        status: GattStatus
    ) {
        if (status == GattStatus.SUCCESS) {
            Timber.i("SUCCESS: Writing <%s> to <%s>", BleBytesParser.bytes2String(value), characteristic.uuid)
        } else {
            Timber.i("ERROR: Failed writing <%s> to <%s> (%s)", BleBytesParser.bytes2String(value), characteristic.uuid, status)
        }
    }

    override fun onCharacteristicUpdate(
        peripheral: BlePeripheral,
        value: ByteArray,
        characteristic: BluetoothGattCharacteristic,
        status: GattStatus
    ) {
        val uuid = characteristic.uuid.toString()
        val parse = BleBytesParser(value)
        Timber.d("datos a mostrar: ${BleBytesParser.bytes2String(value)} / ${if (uuid.contains("ff05")) uuid else ""}")
        when (uuid) {
            typeDevice.UUID_STATUS_CHARACTERISTIC -> {
                Timber.d("Tipo de sensor STATUS: ${typeDevice}")
                Log.d("status", "bateria: ${deviceStatus.battery}")
                deviceStatus.setData(parse)
                _deviceStatusFlow.value = deviceStatus.copy()
            }

            /*typeDevice.UUID_MPU_CHARACTERISTIC -> {
                Log.d("recuento", "->")
                //Timber.d("Tipo de sensor MPU: ${typeSensor}")
                accelerometer.setData(parse)
                _mpuFlow.value = accelerometer.copy().apply {
                    this.angles.xy = accelerometer.angles.xy
                    this.angles.zy = accelerometer.angles.zy
                    this.angles.xz = accelerometer.angles.xz
                }
            }

             */
        }
    }

    fun setConnectionState(connectionSate: ConnectionState) {
        _connectionStateFlow.value = connectionSate
    }

}
