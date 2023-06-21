package com.blautic.sonda.ble.device

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.blautic.sonda.utils.toUUID
import com.diegulog.ble.BleManager
import com.diegulog.ble.BleManagerCallback
import com.diegulog.ble.gatt.BlePeripheral
import com.diegulog.ble.gatt.ConnectionState
import com.diegulog.ble.gatt.HciStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import kotlin.concurrent.scheduleAtFixedRate


class DeviceManager(private val context: Context, var typeDevice: TypeDevice = TypeDevice.PRESION): BleManagerCallback() {

    companion object {

        val UUID_SERVICE = UUID.fromString("0000ff00-0000-1000-8000-00805f9b34fb")
        const val ORDER_IDLE: Byte = 0
        const val ORDER_MPU: Byte = 12
        const val ORDER_OFF: Byte = 20

    }

    private val bleManager: BleManager = BleManager(context, this, Handler(Looper.getMainLooper()))
    private val devices: MutableMap<String, Device> = HashMap()

    private val _rssiFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    val rssiFlow: StateFlow<Int> get() = _rssiFlow

    private var timerAutoConnect: TimerTask? = null

    ///////////////////////////////////////////////////////////////////////////////////////////////

    override fun onDiscoveredPeripheral(peripheral: BlePeripheral, scanResult: ScanResult) {
        Log.d("status", "Descubierto el dispositivo: ${peripheral.name}, con mac: ${peripheral.address}")
    }

    override fun onConnectionFailed(peripheral: BlePeripheral, status: HciStatus) {
        Log.d("estado", "la conexión falló con disp. de mac: ${peripheral.address}")
        devices[peripheral.address]?.setConnectionState(ConnectionState.FAILED)
        autoConnect()
    }

    override fun onConnectedPeripheral(peripheral: BlePeripheral) {
        Log.d("estado", "se ha conectado el dispositivo con mac: ${peripheral.address}")
        autoConnect()
    }

    override fun onDisconnectedPeripheral(peripheral: BlePeripheral, status: HciStatus) {
        Log.d("estado", "onDisconnected to ${peripheral.address}")
        devices[peripheral.address]?.setConnectionState(ConnectionState.DISCONNECTED)
        autoConnect()
    }


    override fun onBluetoothAdapterStateChanged(state: Int) {
        Log.d("estado", "adaptador bluetooth cambia su state a: $state")
        if (state == BluetoothAdapter.STATE_ON) {
            autoConnect()
        }
    }

    fun getDevices(): List<Device> {
        return devices.values.sortedBy { it.numDevice }
    }

    fun getDevices(numDevice: Int): Device? {
        return devices.filterValues { it.numDevice == numDevice }.firstNotNullOfOrNull { it.value }
    }

    fun getDevices(address: String): Device? {
        return devices[address]
    }        

    fun connect() {
        devices.values.forEach {
            connect(it)
        }
    }

    fun connect(numDevice:Int, address: String, typeDevice: TypeDevice = TypeDevice.PRESION) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw IllegalArgumentException("$address is not a valid bluetooth address. Make sure all alphabetic characters are uppercase.")
        }
        Log.d("datos", "$address")
        devices[address] = Device(numDevice = numDevice, address = address, typeDevice = typeDevice)


        getDevices(address)?.let {
            connect(it)
        }
    }

    private fun connect(device: Device) {
        if (isConnected(device)) {
            device.setConnectionState(ConnectionState.CONNECTED)
        } else {
            val peripheral = bleManager.getPeripheral(device.address)
            bleManager.connectPeripheral(peripheral, device)
            device.setConnectionState(ConnectionState.CONNECTING)
        }
    }

    private fun autoConnect() {
        timerAutoConnect?.cancel()
        timerAutoConnect = Timer().scheduleAtFixedRate(10000, 10000) {
            devices.values.forEach { device ->
                if (!isConnected(device) && device.autoReconnect) {
                    connect(device)
                }
            }
        }
    }

    private fun isConnected(device: Device): Boolean {
        return bleManager.connectedPeripherals.any { it.address == device.address }
    }
    
    fun enableMpu(enable: Boolean) {
        val order = byteArrayOf(if (enable) ORDER_MPU else ORDER_IDLE)
        notify(typeDevice.UUID_MPU_CHARACTERISTIC, enable)
        write(typeDevice.UUID_PRESION_CHARACTERISTIC, order)
    }       

    fun powerOff() {
        val order = byteArrayOf(ORDER_OFF)
        write(typeDevice.UUID_PRESION_CHARACTERISTIC, order)
    }

    fun enableDeviceStatus(enable: Boolean) {
        notify(typeDevice.UUID_STATUS_CHARACTERISTIC, enable)
    }

    private fun notify(uuid: String, enable: Boolean) {
        for (peripheral in bleManager.connectedPeripherals) {
            peripheral.setNotify(UUID_SERVICE, uuid.toUUID(), enable)
        }
    }

    private fun write(uuid: String, value: ByteArray) {
        for (peripheral in bleManager.connectedPeripherals) {
            peripheral.writeCharacteristic(UUID_SERVICE, uuid.toUUID(), value)
        }
    }       

    private fun read(uuid: String) {
        for (peripheral in bleManager.connectedPeripherals) {
            peripheral.readCharacteristic(UUID_SERVICE, uuid.toUUID())
        }
    }

    fun disconnectAll() {
        devices.values.forEach {
            disconnect(it)
        }
    }

    fun disconnectByNumDevice(numDevice: Int) {
        getDevices(numDevice)?.let {
            disconnect(it)
            devices.remove(it.address)
        }
    }

    fun disconnectByAddress(address: String) {
        getDevices(address)?.let {
            disconnect(it)
        }
    }

    private fun disconnect(device: Device) {
        device.autoReconnect = false
        val peripheral = bleManager.getPeripheral(device.address)
        peripheral.cancelConnection()
    }

    fun destroy() {
        Timber.d("Destroy Ble")
        timerAutoConnect?.cancel()
        disconnectAll()
        try {
            bleManager.close()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    fun isBluetoothOn(): Boolean {
        return bleManager.isBluetoothEnabled
    }    

    private fun checkIfTarget(scan: ByteArray): Boolean {
        return (scan[5] == 0XBC.toByte() && (scan[6] == 0X25.toByte() || scan[6] == 0X26.toByte())) //Comprobar que es un dispositivo tipo HEALTH y que es un Bio1/Bio2
    }

}
