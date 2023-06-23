package com.blautic.sonda.ble.device

import java.util.*

object BleUUID {

    val UUID_SERVICE = UUID.fromString("0000ff30-0000-1000-8000-00805f9b34fb")

    val UUID_STATUS_CHARACTERISTIC = UUID.fromString("0000ff3A-0000-1000-8000-00805f9b34fb")

    val UUID_PRESION_CHARACTERISTIC = UUID.fromString("0000ff37-0000-1000-8000-00805f9b34fb")

    val UUID_MPU_CHARACTERISTIC = UUID.fromString("0000ff3b-0000-1000-8000-00805f9b34fb")

    /////////////////////////////////////////////////////////////////////////////////

    val UUID_SYSSTATE = UUID.fromString("0000ff32-0000-1000-8000-00805f9b34fb")
    val UUID_JOIN = UUID.fromString("0000fff7-0000-1000-8000-00805f9b34fb")
    val UUID_SYS_CONFIG_DESCRIP = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    val UUID_OPER = UUID.fromString("0000ff37-0000-1000-8000-00805f9b34fb")



    val UUID_BATT = UUID.fromString("0000ff39-0000-1000-8000-00805f9b34fb")
    val UUID_CHANNEL = UUID.fromString("0000ff41-0000-1000-8000-00805f9b34fb")

    val UUID_PROGRAM_CH1 = UUID.fromString("0000ff3C-0000-1000-8000-00805f9b34fb")

    const val TAG_ID_DEVICE = 0xE5.toByte()
    const val TAG_ID_DEVICE2 = 0x01.toByte()
}
