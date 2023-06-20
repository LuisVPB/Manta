package com.blautic.sonda.ble.device

enum class TypeDevice(
    val samplingRateInHz: Double,
    val numElectrodes: Int,
    val groupedData: Int,
    val UUID_MPU_CHARACTERISTIC: String,
    val UUID_ECG_CHARACTERISTIC: String,
    val UUID_STATUS_CHARACTERISTIC: String,
    val UUID_ORDER :String,
    val UUID_FW_VER_CHARACTERISTIC:String,
    val MAC: String,
    val UUID_CH_FR : String
) {
    PRESION(125.0,5, 9,
        "0000ff04-0000-1000-8000-00805f9b34fb", "0000ff05-0000-1000-8000-00805f9b34fb",
        "0000ff03-0000-1000-8000-00805f9b34fb", "0000ff02-0000-1000-8000-00805f9b34fb",
        "0000ff01-0000-1000-8000-00805f9b34fb","12:12:12:12:12:12", "0000ff09-0000-1000-8000-00805f9b34fb"
    )
}
