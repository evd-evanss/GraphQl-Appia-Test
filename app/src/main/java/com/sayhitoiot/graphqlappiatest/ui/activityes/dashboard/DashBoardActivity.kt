package com.sayhitoiot.graphqlappiatest.ui.activityes.dashboard

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.sayhitoiot.graphqlappiatest.R
import com.sayhitoiot.graphqlappiatest.util.MyValueFormatter
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.dialog_signout.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class DashBoardActivity : AppCompatActivity(), CoroutineScope, DashBoardContract.View,
    OnChartValueSelectedListener {
    private var job: Job = Job()
    lateinit var measuresDataset: LineDataSet
    lateinit var presenter: DashBoardPresenter
    var token = ""
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val sharedPref = this.getSharedPreferences("my_token_data",Context.MODE_PRIVATE) ?: return
        token = sharedPref.getString("token", "vazio")!!
        chart_mpa.setNoDataText("")
        chart_mpa.setOnChartValueSelectedListener(this)
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        presenter = token.let { DashBoardPresenter(this, it) }
    }

    override fun onStart() {
        super.onStart()
        val sharedPref = this.getSharedPreferences("my_token_data", Context.MODE_PRIVATE) ?: return
        token = sharedPref.getString("token", "").toString()

        launch {
            presenter.configDataEntries()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun logoutClick(view: View){
        val layout = LayoutInflater.from(this).inflate(R.layout.dialog_signout, null, false)
        val dialog = AlertDialog.Builder(this).setView(layout)
        dialog.setNegativeButton("Cancelar", null)
        dialog.setPositiveButton("Sim") { d, i ->
            val prefs = this.getSharedPreferences("my_token_data", Context.MODE_PRIVATE)
            with (prefs.edit()) {
                putString("token", "")
                commit()
            }
            finish()
        }
        dialog.create().show()
    }

    override fun plotterGraph(lineDataSet: LineDataSet) {
        measuresDataset = lineDataSet
        chart_mpa.setNoDataText("")
        chart_mpa.setPinchZoom(true)
        //measuresDataset.color = ContextCompat.getColor(this, R.color.colorAccent)
        val dataSet = java.util.ArrayList<ILineDataSet>()
        dataSet.add(0,measuresDataset)
        val lineData = LineData(dataSet)
        chart_mpa.data = lineData
        chart_mpa.description.text = ""
        chart_mpa.legend.isEnabled = false
        chart_mpa.animate()
        chart_mpa.invalidate()
    }

    override fun configAxisX() {
        val xAxis = chart_mpa.xAxis
        xAxis.axisLineColor = Color.BLACK
        xAxis.textColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        xAxis.setDrawAxisLine(false) // apaga linha X
        xAxis.setDrawGridLines(false) // apaga as linhas grids X
        xAxis.labelCount = 7
        xAxis.axisMinimum = -0.3f // altera offset de X
        //xAxis.spaceMax = 3f // define o comprimento da linha X
        //xAxis.textColor = Color.BLACK
        xAxis.textSize = 15f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            xAxis.typeface =  resources.getFont(R.font.cursive_bold)
        }

        val limite_normal = LimitLine(180f, "")
        limite_normal.lineColor = Color.GREEN
        limite_normal.lineWidth = 1f
        val limite_hipoglicemia = LimitLine(70f, "")
        limite_hipoglicemia.lineColor = Color.RED
        limite_hipoglicemia.lineWidth = 1f

        chart_mpa.axisLeft .addLimitLine(limite_normal)
        chart_mpa.axisLeft .addLimitLine(limite_hipoglicemia)
        chart_mpa.axisLeft.addLimitLine(limite_normal)
        xAxis.setValueFormatter(MyValueFormatter())
        xAxis.position = XAxis.XAxisPosition.TOP_INSIDE
        // Evita a duplicação dos dias na linha X
        xAxis.isGranularityEnabled = true
        xAxis.granularity = 1f

    }

    override fun configAxisY() {
        val yAxisLeft = chart_mpa.axisLeft
        val yAxisRight = chart_mpa.axisRight
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            yAxisLeft.typeface =  resources.getFont(R.font.cursive_bold)
        }
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.textColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        yAxisRight.isEnabled = false // Desabilita inhas Y a direita

        yAxisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        chart_mpa.axisLeft .mAxisMaximum = 250f
        chart_mpa.axisLeft .mAxisMinimum = 0f
        chart_mpa.axisLeft .mAxisRange = chart_mpa.axisLeft .mAxisMaximum - chart_mpa.axisLeft .mAxisMinimum
    }

    override fun onNothingSelected() {

    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onValueSelected(e: Entry?, h: Highlight?) {

        if(e?.y!! <= 70f){
            tv_Entry.setTextColor(
                ContextCompat.getColor(this, R.color.colorHipoglicemia))
        }
        if((e.y >= 70f)and(e.y <=180f)){
            tv_Entry.setTextColor(
                ContextCompat.getColor(this, R.color.colorNormal))
        }
        if(e.y >= 180f){
            tv_Entry.setTextColor(
                ContextCompat.getColor(this,
                    R.color.colorHiperglicemia
                ))
        }
        tv_Entry.text = e.y.toString() + " mg/dl"
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
        when (menuItem.itemId) {
            R.id.nav_linear -> {
                measuresDataset.mode = LineDataSet.Mode.LINEAR
                chart_mpa.invalidate()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_horizontal -> {
                measuresDataset.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                chart_mpa.invalidate()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_cubic -> {
                measuresDataset.mode = LineDataSet.Mode.CUBIC_BEZIER
                chart_mpa.invalidate()
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_est -> {
                measuresDataset.mode = LineDataSet.Mode.STEPPED
                chart_mpa.invalidate()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }
}
