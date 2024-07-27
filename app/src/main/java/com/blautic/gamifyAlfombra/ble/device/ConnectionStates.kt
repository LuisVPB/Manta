package com.blautic.gamifyAlfombra.ble.device

enum class ConnectionStates(var label: String) {
    STATE_CONNECTED("Device Connected"), STATE_CONNECTING(
        "Device Connecting"),
    STATE_DISCONNECTED("Device Disconnected"), STATE_DISCONNECTING(
        "Device Disconnecting")
}