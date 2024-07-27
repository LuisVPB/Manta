package com.blautic.gamifyAlfombra.ble.device.mpu

/**
 * This class describes the Gyroscope scale, send the base frame with GyroScale configured with one of the following values:
 */
enum class GyrScale(val value: Float) {
    /**
     * 2000dp:
     */
    GYR_SCALE_2000(2000.0f),

    /**
     * 1000dp:
     */
    GYR_SCALE_1000(1000.0f),

    /**
     * 500dp:
     */
    GYR_SCALE_500(500.0f),

    /**
     * 250dp:
     */
    GYR_SCALE_250(250.0f);

    val ratio: Float = value / 32767

    companion object {
        fun fromValue(value: Int): GyrScale {
            for (type in values()) {
                if (type.value == value.toFloat()) return type
            }
            return GYR_SCALE_1000
        }
    }
}
