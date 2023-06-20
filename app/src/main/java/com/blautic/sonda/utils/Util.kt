package com.blautic.sonda.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.blautic.sonda.ble.device.mpu.Mpu

import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import uk.me.berndporr.iirj.Cascade
import java.text.ParseException
import java.text.SimpleDateFormat

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.math.sqrt

fun convertUtcToLocalTime(date: String?): String {
    if (date == null) return ""
    val utcInstant = "${date}Z".toInstant().toLocalDateTime(TimeZone.currentSystemDefault())
    return "${utcInstant.dayOfMonth.addZero()}-${utcInstant.monthNumber.addZero()}-${utcInstant.year} ${utcInstant.hour.addZero()}:${utcInstant.minute.addZero()}"
}

fun Int.addZero():String{
    return if(this < 10) "0${this}" else "$this"
}

fun Cascade.filter(value:Float):Float{
    return this.filter(value.toDouble()).toFloat()
}

fun calcAi(accList: List<Mpu>): Int {
    if (accList.isEmpty()) {
        return 0
    }
    val it: List<Mpu> = ArrayList(accList)
    val error = 1e-06
    val avgX = it.map { acc -> acc.accX }.average()
    val avgY = it.map { acc -> acc.accY }.average()
    val avgZ = accList.map { acc -> acc.accZ }.average()
    val diX = sqrt(it.map { acc -> (acc.accX - avgX).pow(2.0) }.sum() / (it.size - 1))
    val diY = sqrt(it.map { acc -> (acc.accY - avgY).pow(2.0) }.sum() / (it.size - 1))
    val diZ = sqrt(it.map { acc -> (acc.accZ - avgZ).pow(2.0) }.sum() / (it.size - 1))
    val sum =
        ((diX * diX - error) / error) + ((diY * diY - error) / error) + ((diZ * diZ - error) / error)
    return sqrt((sum / 3).coerceAtLeast(0.0)).toInt()
}

fun sd(values: List<Float>): Double {
    val avg = values.average()
    return sqrt(values.sumOf { (it - avg).pow(2.0) } / (values.size - 1))
}

fun secondOfDay(): Long {
    return Clock.System.now().toLocalDateTime(TimeZone.UTC).date.atStartOfDayIn(TimeZone.UTC)
        .until(
            Clock.System.now().toLocalDateTime(TimeZone.UTC).toInstant(TimeZone.UTC),
            DateTimeUnit.SECOND
        )
}

fun String.toUUID(): UUID{
    return UUID.fromString(this)
}

fun Long.formatSeconds(): String {
    var rest = this
    val days = (rest / 86400).toInt()
    rest %= 86400
    val hours = rest / 3600
    rest %= 3600
    val min = rest / 60
    return "${days}d:${hours}h:${min}m"

}


fun getVersion(context: Context): String? {
    return try {
        val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        "v" + pInfo.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        ""
    }
}

fun String.showMessage(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun dp(context: Context, value: Float): Int {
    return ceil(context.resources.displayMetrics.density * value).toInt()
}

fun String.getTimeLocal(utcTime: String): String {
    val formattedDate = try {
        var df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT)
        df.timeZone = java.util.TimeZone.getTimeZone("UTC")
        val date = df.parse(utcTime)

        df = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ROOT)
        df.timeZone = java.util.TimeZone.getDefault()
        df.format(date)
    } catch (e: ParseException) {
        e.printStackTrace()
        ""
    }
    return formattedDate
}

fun Long.formatSecondsDay(): String {
    var rest = this
    val days = (rest / 86400).toInt()
    rest %= 86400
    val hours = rest / 3600
    rest %= 3600
    val min = rest / 60
    return "${hours}h:${min}m"
}


