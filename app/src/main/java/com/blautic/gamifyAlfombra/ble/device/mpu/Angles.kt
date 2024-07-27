package com.blautic.gamifyAlfombra.ble.device.mpu

import android.util.Log
import uk.me.berndporr.iirj.Butterworth
import kotlin.math.*

data class Angles(
    var enableFilter: Boolean,
    var xy: Float = 0f,
    var zy: Float = 0f,
    var xz: Float = 0f,
    var minAngleXY: Float = 0f,
    var minAngleZY: Float = 0f,
    var minAngleXZ: Float = 0f,
    var maxAngleXY: Float = 0f,
    var maxAngleZY: Float = 0f,
    var maxAngleXZ: Float = 0f
) {


    private val avgXY = mutableListOf<Float>()
    private val avgZY = mutableListOf<Float>()
    private val avgXZ = mutableListOf<Float>()
    private val lowpassA = Butterworth()
    private val lowpassB = Butterworth()
    private val lowpassC = Butterworth()
    private val MAX_AVG = 200
    private var base = 0
    private var offsetXy = 0f
    private var offsetZy = 0f
    private var offsetXz = 0f
    private var lastSample = 0

    fun setValues(x: Float, y: Float, z: Float) {
        val accX: Float = if (enableFilter) lowpassA.filter(x.toDouble()).toFloat() else x
        val accY: Float = if (enableFilter) lowpassB.filter(y.toDouble()).toFloat() else y
        val accZ: Float = if (enableFilter) lowpassC.filter(z.toDouble()).toFloat() else z
        xy = getAngles(accX, accY) - offsetXy
        zy = getAngles(accY, accZ) - offsetZy
        xz = getAngles(accX, accZ) - offsetXz
        setValueAvg()
        setValueMin()
        setValueMax()
    }

    fun setValues(sample: Int, x: Float, y: Float, z: Float) {
        if (sample < lastSample + 5) return
        lastSample = sample
        val accX: Float = if (enableFilter) lowpassA.filter(x.toDouble()).toFloat() else x
        val accY: Float = if (enableFilter) lowpassB.filter(y.toDouble()).toFloat() else y
        val accZ: Float = if (enableFilter) lowpassC.filter(z.toDouble()).toFloat() else z
        xy = (getAngles(accX, accY) - offsetXy)
        zy = (getAngles(accY, accZ) - offsetZy)
        xz = (getAngles(accX, accZ) - offsetXz)
        setValueAvg()
        setValueMin()
        setValueMax()
    }

    private fun getAngles(a: Float, b: Float): Float {



        return if (abs(a) < 0.1 && abs(b) < 0.1) 0f
        else {
            val rad3 = atan2(a, b)
            val result = ((rad3 * 180) / PI).toFloat()
            Log.d("valAnglos", "$a / $b / $rad3")
            return result
        }

    }

    fun setOffset(enable: Boolean) {
        if (enable) {
            offsetXy = xy - base
            offsetXz = xz - base
            offsetZy = zy - base
        } else {
            offsetXy = 0f
        }
    }

    private fun setValueAvg() {
        //Media acc
        avgXY.add(xy)
        if (avgXY.size > MAX_AVG) avgXY.removeAt(0)
        avgZY.add(zy)
        if (avgZY.size > MAX_AVG) avgZY.removeAt(0)
        avgXZ.add(xz)
        if (avgXZ.size > MAX_AVG) avgXZ.removeAt(0)
    }

    private fun setValueMin() {
        if (xy < minAngleXY || minAngleXY == 0f) {
            minAngleXY = xy
        }
        if (zy < minAngleZY || minAngleZY == 0f) {
            minAngleZY = zy
        }
        if (xz < minAngleXZ || minAngleXZ == 0f) {
            minAngleXZ = xz
        }
    }

    private fun setValueMax() {
        if (xy > maxAngleXY || maxAngleXY == 0f) {
            maxAngleXY = xy
        }
        if (zy > maxAngleZY || maxAngleZY == 0f) {
            maxAngleZY = zy
        }
        if (xz > maxAngleXZ || maxAngleXZ == 0f) {
            maxAngleXZ = xz
        }
    }

    fun clean() {
        avgZY.clear()
        avgXY.clear()
        avgXZ.clear()
        minAngleXY = 0f
        minAngleZY = 0f
        minAngleXZ = 0f
        maxAngleXY = 0f
        maxAngleZY = 0f
        maxAngleXZ = 0f
    }

    fun setBase(base: Int) {
        this.base = base
    }

    init {
        if (enableFilter) {
            lowpassA.lowPass(1, 20.0, 1.0) //order,Samplingfreq,Cutoff frequ);
            lowpassB.lowPass(1, 20.0, 1.0) //order,Samplingfreq,Cutoff frequ);
            lowpassC.lowPass(1, 20.0, 1.0) //order,Samplingfreq,Cutoff frequ);
        }
    }
}
