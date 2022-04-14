package com.example.expensesmanager2.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.annotation.RequiresApi
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.CategoryModel
import com.example.expensesmanager2.models.ConfigModel
import com.example.expensesmanager2.models.OperationModel
import com.example.expensesmanager2.utils.SQLiteHelper
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.collections.ArrayList

class StatisticsActivity : AppCompatActivity() {
    private lateinit var btnWeek: Button
    private lateinit var btnMonth: Button
    private lateinit var btnAll: Button
    private lateinit var pieChart: PieChart
    private lateinit var sql: SQLiteHelper
    private val activeButtonColor = Color.parseColor("#FF000B41")
    private val inactiveButtonColor = Color.parseColor("#FF112BBD")

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ResourceAsColor", "PrivateResource")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        sql = SQLiteHelper(this)
        btnWeek = findViewById(R.id.btnWeek)
        btnMonth = findViewById(R.id.btnMonth)
        btnAll = findViewById(R.id.btnAll)
        pieChart = findViewById(R.id.pieChart)

        setupPieChart()
        showMonthChart()

        btnMonth.setBackgroundColor(activeButtonColor)
        btnAll.setBackgroundColor(inactiveButtonColor)
        btnWeek.setBackgroundColor(inactiveButtonColor)


        btnAll.setOnClickListener {
            loadPieChartData(0)
            it.setBackgroundColor(activeButtonColor)
            btnMonth.setBackgroundColor(inactiveButtonColor)
            btnWeek.setBackgroundColor(inactiveButtonColor)
        }
        btnMonth.setOnClickListener {
            showMonthChart()
            it.setBackgroundColor(activeButtonColor)
            btnAll.setBackgroundColor(inactiveButtonColor)
            btnWeek.setBackgroundColor(inactiveButtonColor)
        }
        btnWeek.setOnClickListener {
            showWeekChart()
            it.setBackgroundColor(activeButtonColor)
            btnMonth.setBackgroundColor(inactiveButtonColor)
            btnAll.setBackgroundColor(inactiveButtonColor)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showWeekChart() {
        val today = LocalDate.now(ZoneId.systemDefault())
        val previousMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY))
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, previousMonday.dayOfMonth)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        loadPieChartData(calendar.timeInMillis)
    }

    private fun showMonthChart() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        loadPieChartData(calendar.timeInMillis)
    }

    private fun setupPieChart() {

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setCenterTextSize(20F)
        val string = OperationModel(this).getExpensesSum(0).toString().split('.')
        pieChart.centerText = if (string[1].length > 1)
            OperationModel(this).getExpensesSum(0)
                .toString() + " " + ConfigModel(this).get("currency") else
            OperationModel(this).getExpensesSum(0)
                .toString() + "0" + " " + ConfigModel(this).get("currency")


        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.setDrawInside(false)
        legend.isEnabled = true
        legend.textSize = 15F
    }

    private fun loadPieChartData(since: Long) {
        val entries = ArrayList<PieEntry>()

        for (cat in CategoryModel(this).get()) {
            if (cat.getSum(since) > 0 && cat.id != 0)
                entries.add(PieEntry(cat.getSum(since), cat.name))
        }

        val colors = ArrayList<Int>()

        for (color in ColorTemplate.MATERIAL_COLORS) {
            colors.add(color)
        }

        for (color in ColorTemplate.VORDIPLOM_COLORS) {
            colors.add(color)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors

        val data = PieData(dataSet)
        data.setDrawValues(true)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(12F)
        data.setValueTextColor(Color.BLACK)

        pieChart.data = data
        pieChart.invalidate()
    }
}