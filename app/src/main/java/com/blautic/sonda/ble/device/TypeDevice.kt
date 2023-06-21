package com.blautic.sonda.ble.device

enum class TypeDevice(
    val samplingRateInHz: Double,
    val numElectrodes: Int,
    val groupedData: Int,
    val UUID_MPU_CHARACTERISTIC: String,
    val UUID_STATUS_CHARACTERISTIC: String,
    val UUID_PRESION_CHARACTERISTIC: String,
    val MAC: String
) {
    PRESION(
        125.0, 5, 9,
        "0000ff3b-0000-1000-8000-00805f9b34fb", "0000ff3a-0000-1000-8000-00805f9b34fb",
        "0000ff37-0000-1000-8000-00805f9b34fb", "77:77:77:77:77:77"
    )
}
