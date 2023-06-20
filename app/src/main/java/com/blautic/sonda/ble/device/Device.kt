package com.blautic.sonda.ble.device

import com.blautic.sonda.ble.device.DeviceManager.Companion.UUID_SERVICE
import com.blautic.sonda.ble.device.mpu.Mpu
import com.blautic.sonda.utils.toUUID
import com.diegulog.ble.gatt.BlePeripheral
import com.diegulog.ble.gatt.BlePeripheralCallback
import com.diegulog.ble.gatt.ConnectionPriority
import com.diegulog.ble.gatt.ConnectionState
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

    }

    fun setConnectionState(connectionSate: ConnectionState) {
        _connectionStateFlow.value = connectionSate
    }



}
