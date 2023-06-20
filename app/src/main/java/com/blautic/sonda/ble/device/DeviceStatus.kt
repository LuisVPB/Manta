package com.blautic.sonda.ble.device

import com.diegulog.ble.BleBytesParser


data class DeviceStatus(var battery:Int = 0, var operationMode:Int = 0) {
    private var lasAdcBat = 0
    private val avgBat: MutableList<Int> = ArrayList()

    fun setData(parse: BleBytesParser) {
        operationMode = parse.getIntValue(BleBytesParser.FORMAT_UINT8)
        val adc = parse.getIntValue(BleBytesParser.FORMAT_UINT16)
        setPowerVal(adc)
    }

    private fun setPowerVal(adc: Int) {
        if (adc > 0 && adc != lasAdcBat) {
            lasAdcBat = adc
            var bat = 0
            //Calculamos el valor de batería desde adc
            //12 bits: 1400 100%   1365:4.08V 1333:4.0   1309:3.9   1283:3.8 1252:3.7  1204:3.6 1160:3.5  1131:3.4 1110:3.3
            if (adc > powerAdc.first()) bat = 100 else if (adc < powerAdc.last()) bat = 0 else {
                for (i in powerAdc.indices) {
                    //Log.d("BAT",key.intValue()+":"+powerMap.get(key) +" < "+adc);
                    if (powerAdc[i] < adc) {
                        bat = powerPerc[i]
                        break
                    }
                }
            }
            if (avgBat.size >= NSAMPLES_AVG_BATT) avgBat.removeAt(0)

            avgBat.add(bat)
            battery = avgBat.average().toInt()
        }
    }

    override fun toString(): String {
        return "DeviceStatus{" +
                "battery=" + battery +
                ", operationMode=" + operationMode +
                '}'
    }

    override fun equals(other: Any?): Boolean {
        return false
    }

    companion object {
        private const val NSAMPLES_AVG_BATT = 10
        private val powerAdc = intArrayOf(
            1417, 1407, 1396, 1386, 1375, 1365, 1354, 1344, 1334, 1323,
            1313, 1302, 1292, 1281, 1271, 1260, 1250, 1194, 1180, 1166, 1093
        )
        private val powerPerc = intArrayOf(
            100, 95, 90, 85, 80, 75, 70, 65, 60, 55,
            50, 45, 40, 35, 30, 25, 20, 15, 10, 5, 0
        )
    }
}
