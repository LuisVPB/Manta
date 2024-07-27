package com.blautic.gamifyAlfombra.ble.device.mpu

/**
 * This class describes the Acc scale, Send the base frame with AccScale configured with one of the following values::
 */
enum class AccScale(val value: Float) {
    /**
     * 2g: 0x00
     */
    ACC_SCALE_2G(2.0f),

    /**
     * 4g: 0x08
     */
    ACC_SCALE_4G(4.0f),

    /**
     * 8g: 0x10
     */
    ACC_SCALE_8G(8.0f),

    /**
     * 16g: 0x18
     */
    ACC_SCALE_16G(16.0f);

    val ratio: Float = value / 32767

    companion object {
        fun fromValue(value: Int): AccScale {
            for (type in values()) {
                if (type.value == value.toFloat()) return type
            }
            return ACC_SCALE_4G
        }
    }

}

