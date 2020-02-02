package com.sayhitoiot.graphqlappiatest.ui.activityes.dashboard

import com.example.graphql.MeasurementsQuery
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import okhttp3.OkHttpClient

interface DashBoardContract {
    interface  View{
        fun plotterGraph(lineDataSet: LineDataSet)
        fun configAxisX()
        fun configAxisY()
        suspend fun initializeAdapter()
    }

    interface  Presenter{
        suspend fun queryMeasures(): MutableList<Entry>
        suspend fun configDataEntries()
        fun createOkHttpWithValidToken(): OkHttpClient?
        fun configDataSetValues(values: MutableList<Entry>)
        suspend fun getMeasures(): MutableList<MeasurementsQuery.Measurement>
    }
}