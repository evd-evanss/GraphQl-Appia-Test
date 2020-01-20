package com.sayhitoiot.graphqlappiatest.util

import android.util.Log
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.ArrayList

class MyValueFormatter() : ValueFormatter() {

    private val mValues = ArrayList<String>()

    init {
        mValues.add("Seg")
        mValues.add("Ter")
        mValues.add("Qua")
        mValues.add("Qui")
        mValues.add("Sex")
        mValues.add("Sab")
        mValues.add("Dom")

    }
    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        Log.d("Teste", mValues[value.toInt()])
        if (value.toInt() >= 0 && value.toInt() <= mValues.size - 1) {
            return mValues[value.toInt()]
        } else {
            return ("").toString()
        }
//        return mValues[value.toInt()]
    }
    override fun getFormattedValue(value: Float): String {
        return value.toString()
    }

    override fun getAxisLabel(value: Float, axis: AxisBase): String {
//        Log.d("Teste", mValues[value.toInt()])
        if (value.toInt() >= 0 && value.toInt() <= mValues.size - 1) {
            return mValues[value.toInt()]
        } else {
            return ("").toString()
        }
    }
}