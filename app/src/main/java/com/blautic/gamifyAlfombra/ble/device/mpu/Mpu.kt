package com.blautic.gamifyAlfombra.ble.device.mpu

import android.os.Parcelable

import com.diegulog.ble.BleBytesParser
import com.diegulog.ble.BleBytesParser.FORMAT_SINT16
import kotlinx.parcelize.Parcelize

@Parcelize
data class Mpu(
    var id:Long = 0,
    var ownerID: Long = 0,
    var sample: Long = 0,
    var accX: Float = 0f,
    var accY: Float = 0f,
    var accZ: Float = 0f,
    var gyrX: Float = 0f,
    var gyrY: Float = 0f,
    var gyrZ: Float = 0f
) : Parcelable {

    @Transient
    var angles: Angles = Angles(false)

    @Transient
    private val accScale = AccScale.ACC_SCALE_2G.ratio

    @Transient
    private val gyrScale: Float = GyrScale.GYR_SCALE_1000.ratio

    fun setData(parse: BleBytesParser) {
        if (parse.value.size >= 6) { //inicialmente en 8, no entraba
            //sample = parse.getIntValue(FORMAT_UINT16).toLong() //No requiere sample
            accX = parse.getIntValue(FORMAT_SINT16) * accScale;
            accY = parse.getIntValue(FORMAT_SINT16) * accScale;
            accZ = parse.getIntValue(FORMAT_SINT16) * accScale;
            angles.setValues(/*sample.toInt(),*/ accX, accY, accZ)
        }
    }


}
