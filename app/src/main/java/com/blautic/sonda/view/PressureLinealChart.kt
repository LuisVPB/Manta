package com.blautic.sonda.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.blautic.sonda.R
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class PressureLinealChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(
    context,
    attrs,
    defStyleAttr
) {

    private var labelSensorName: TextView? = null
    private var chart: LineChart? = null
    private var scale = 50

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.pressure_lineal_chart, this)
        labelSensorName = findViewById<TextView>(R.id.label_sensor)
        chart = findViewById<LineChart>(R.id.chartSensor)

        initLineChar()
    }

    private fun initLineChar() {
        chart?.let {
            it.apply {

                animateY(1400, Easing.EaseInOutQuad)

                description.isEnabled = false

                // enable touch gestures
                setTouchEnabled(true)

                // enable scaling and dragging
                isDragEnabled = true
                setScaleEnabled(true)

                // sin lineas de eje
                setDrawGridBackground(false)

                // if disabled, scaling can be done on x- and y-axis separately
                setPinchZoom(false)

                // set background color TRANSPARENT
                setBackgroundColor(Color.TRANSPARENT)

                // add empty data
                val datos = LineData()
                datos.setValueTextColor(Color.WHITE)
                data = datos

                // disable legend (only possible after setting data)
                legend.isEnabled = false

                // no mostrar eje x
                xAxis.isEnabled = false

                // configurar eje y
                axisLeft.axisMaximum = scale.toFloat()
                axisLeft.axisMinimum = (-scale).toFloat()
                axisLeft.setDrawGridLines(true)
                axisLeft.textSize = 9f
                //disable axis right
                axisRight.isEnabled = false

            }
        }
    }

    fun clean() {
        chart?.let{
            it.clear()
            it.invalidate()
        }
    }

    fun setScale(scale: Float) {
        this.scale = scale.toInt()
        initLineChar()
    }

    fun setMinScale(scale: Int) {
        chart?.axisLeft?.axisMinimum = scale.toFloat()
    }

    fun setMaxScale(scale: Int) {
        chart?.axisLeft?.axisMaximum = scale.toFloat()
    }

    fun setLabelSensorName(name: String?) {
        labelSensorName?.text = name
    }

    fun addEntryLineChart(entry: Float) {
        chart?.data?.let { lineData ->
            Log.d("grafico", entry.toString())
            var set = lineData.getDataSetByIndex(0)
            set = createSetLineChart()
            lineData.addDataSet(set)
            for (i in 1..359) {
                lineData.addEntry(Entry(set.entryCount.toFloat(), 0f), 0)
            }
            lineData.addEntry(Entry(set.entryCount.toFloat(), entry), 0)
            lineData.notifyDataChanged()
            // let the chart know it's data has changed
            chart?.notifyDataSetChanged()
            // limit the number of visible entries
            chart?.setVisibleXRangeMaximum(360f)
            // move to the latest entry
            chart?.moveViewToX(lineData.entryCount.toFloat())
        }

    }

    private fun createSetLineChart(): LineDataSet? {
        val set = LineDataSet(null, "")
        set.axisDependency = YAxis.AxisDependency.LEFT
        set.lineWidth = 2f
        set.isHighlightEnabled = false
        set.color = ContextCompat.getColor(context, R.color.teal_700)
        set.setDrawValues(false)
        set.setDrawCircles(false)
        return set
    }



}