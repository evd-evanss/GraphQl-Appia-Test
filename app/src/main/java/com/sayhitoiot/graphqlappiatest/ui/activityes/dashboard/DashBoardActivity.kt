package com.sayhitoiot.graphqlappiatest.ui.activityes.dashboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.sayhitoiot.graphqlappiatest.R
import com.sayhitoiot.graphqlappiatest.util.MyValueFormatter
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.bottom_sheet_list.*
import kotlinx.android.synthetic.main.bottom_appbar.*
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
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    lateinit var adapter: MeasuresAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        bottomSheetBehavior = BottomSheetBehavior.from<ConstraintLayout>(heroislist)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        val sharedPref = this.getSharedPreferences("my_token_data",Context.MODE_PRIVATE) ?: return
        token = sharedPref.getString("token", "vazio")!!
        chart_mpa.setNoDataText("")
        chart_mpa.setOnChartValueSelectedListener(this)
        presenter = token.let { DashBoardPresenter(this, it) }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_item, menu)
        return true
    }


    override fun onStart() {
        super.onStart()
        val sharedPref = this.getSharedPreferences("my_token_data", Context.MODE_PRIVATE) ?: return
        token = sharedPref.getString("token", "").toString()

        launch {
            presenter.configDataEntries()
            initializeAdapter()

            recycler_view.itemAnimator = DefaultItemAnimator()
            recycler_view.adapter = adapter
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    fun logoutClick(view: View){
        val layout = LayoutInflater.from(this).inflate(R.layout.dialog_signout, null, false)
        val dialog = AlertDialog.Builder(this).setView(layout)

        layout.bt_logout.setOnClickListener {
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
        xAxis.textSize = 20f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            xAxis.typeface =  resources.getFont(R.font.amatic_bold)
        }

        val limite_normal = LimitLine(180f, "")
        limite_normal.lineColor = Color.GREEN
        limite_normal.lineWidth = 3f
        val limite_hipoglicemia = LimitLine(70f, "")
        limite_hipoglicemia.lineColor = Color.RED
        limite_hipoglicemia.lineWidth = 3f
        val offset_line = LimitLine(-11.4f, "")
        offset_line.lineColor = ContextCompat.getColor(this, R.color.colorAccent)
        offset_line.lineWidth = 3f

        chart_mpa.axisLeft .addLimitLine(offset_line)
        chart_mpa.axisLeft .addLimitLine(limite_hipoglicemia)
        chart_mpa.axisLeft.addLimitLine(limite_normal)
        chart_mpa.axisLeft.axisLineWidth = 3f
        chart_mpa.axisLeft.axisLineColor = ContextCompat.getColor(this, R.color.colorAccent)
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
            yAxisLeft.typeface =  resources.getFont(R.font.amatic_bold)
        }
        yAxisLeft.setDrawGridLines(false)
        yAxisLeft.textColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
        yAxisLeft.textSize = 20f
        yAxisRight.isEnabled = false // Desabilita inhas Y a direita

        yAxisRight.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        chart_mpa.axisLeft .mAxisMaximum = 250f
        chart_mpa.axisLeft .mAxisMinimum = 0f
        chart_mpa.axisLeft .mAxisRange = chart_mpa.axisLeft .mAxisMaximum - chart_mpa.axisLeft .mAxisMinimum
    }

    override suspend fun initializeAdapter() {
        adapter =
            MeasuresAdapter(
                presenter.getMeasures()
            )
    }

    override fun onNothingSelected() {

    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onValueSelected(e: Entry?, h: Highlight?) {
        tv_Entry.text = e?.y.toString() + " mg/dl"
    }

    fun onClick(view: View) {
        when(view){

            fab ->{
                launch {
                    presenter.configDataEntries()
                    initializeAdapter()
                }
                if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                    //buttonSlideUp.text = "Deslize para baixo"
                } else {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED;
                    //buttonSlideUp.text = "Deslize para cima"
                }
            }
            iv_linear ->{
                iv_linear.animate()
                measuresDataset.mode = LineDataSet.Mode.LINEAR
                chart_mpa.invalidate()
            }
            iv_bezier ->{
                measuresDataset.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                chart_mpa.invalidate()
            }
            iv_cubic ->{
                measuresDataset.mode = LineDataSet.Mode.STEPPED
                chart_mpa.invalidate()
            }
            iv_tips ->{
                val layout = LayoutInflater.from(this).inflate(R.layout.dialog_tips, null, false)
                val dialog = AlertDialog.Builder(this).setView(layout)
                dialog.setNegativeButton("Voltar", null)
                dialog.create().show()            }
        }

    }
}
