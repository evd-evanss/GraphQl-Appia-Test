package com.sayhitoiot.graphqlappiatest.ui.activityes.dashboard

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toDeferred
import com.example.graphql.MeasurementsQuery
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import java.io.IOException


class DashBoardPresenter(
    val view: DashBoardContract.View, val token: String):DashBoardContract.Presenter{
    private val BASE_URL = "https://appia-api-candidates.herokuapp.com/graphql"
    lateinit var values: MutableList<Entry>
    lateinit var dataMeasures: MutableList<MeasurementsQuery.Measurement>

    // Apollo Client
    override suspend fun configDataEntries() {
        values = queryMeasures() // <== coroutine
        Log.i("tamanho", values.size.toString())
        configDataSetValues(values)
    }
    // Busco dados a serem apresentados
    override suspend fun queryMeasures(): MutableList<Entry> {
        val apolloClient: ApolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(this.createOkHttpWithValidToken()!!)
            .build()
        val queryMea = MeasurementsQuery.builder().build()
        val measuresData =
            apolloClient.query(queryMea).toDeferred().await()
        // Instancio classe measurements gerada pelo Apollo
        dataMeasures = arrayListOf()
        values = arrayListOf()
        dataMeasures = measuresData.data()?.measurements()!!
        // Converto classe measurements em Entry para o MPAndroid chart
        var pos = 0f
        dataMeasures.forEach{
            if(pos<7){
                values.add(Entry(pos++, it.measurement().toFloat()))
            }
        }
        Log.i("Medidas", dataMeasures.toString())
        return values
    }

    override suspend fun getMeasures(): MutableList<MeasurementsQuery.Measurement>{
        val apolloClient: ApolloClient = ApolloClient.builder()
            .serverUrl(BASE_URL)
            .okHttpClient(this.createOkHttpWithValidToken()!!)
            .build()
        val queryMea = MeasurementsQuery.builder().build()
        val measuresData =
            apolloClient.query(queryMea).toDeferred().await()
        // Instancio classe measurements gerada pelo Apollo
        dataMeasures = arrayListOf()
        values = arrayListOf()
        dataMeasures = measuresData.data()?.measurements()!!
        // Converto classe measurements em Entry para o MPAndroid chart
        var pos = 0f
        dataMeasures.forEach{
            if(pos<7){
                values.add(Entry(pos++, it.measurement().toFloat()))
            }
        }
        return dataMeasures
    }
    // Configuro valores e cores
    override fun configDataSetValues(values: MutableList<Entry>){
        // Configuro os dados recebidos e atribuo as cores de acordo com os valores
        val measuresDataset = LineDataSet(values, "")
        val colors_dataset :MutableList<Int>
        colors_dataset = ArrayList()
        var pos = 0

        values.forEach{
            val entry: Entry = values.get(pos)
            Log.i("color", entry.y.toString())
            // valor <= 70 Hipoglicemia => pinta circulos de vermelho
            if(entry.y <= 70f) colors_dataset.add(pos, ColorTemplate.rgb("#5120DD"))
            // valor > 70 e < 180 Normal => pinta circulos de verde
            if((entry.y >= 70f) and (entry.y <= 180f)) colors_dataset.add(pos, ColorTemplate.rgb("#039500")) // VERDE
            // valor > 180 Hiperglicemia => pinta circulos de azul
            if(entry.y > 180f) colors_dataset.add(pos, ColorTemplate.rgb("#C60000"))
            pos++
        }
        measuresDataset.setCircleColors(colors_dataset)
        view.configAxisX()
        view.configAxisY()
        measuresDataset.lineWidth = 3f
        measuresDataset.circleRadius = 5f
        measuresDataset.circleHoleRadius = 0f
        measuresDataset.setColors(colors_dataset)
        measuresDataset.setDrawFilled(true)
        view.plotterGraph(measuresDataset)

    }
    // Configuro header através do token
    override fun createOkHttpWithValidToken(): OkHttpClient? {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                    return chain.proceed(
                        chain.request().newBuilder().header(
                            "token", token
                        ).build()
                    )
                }
            }).build()
    }
}