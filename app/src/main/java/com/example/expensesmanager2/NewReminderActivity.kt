package com.example.expensesmanager2

import android.annotation.SuppressLint
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.math.min

class NewReminderActivity : AppCompatActivity() {

    private lateinit var etTitle : EditText
    private lateinit var etDesc : EditText
    private lateinit var rgType : RadioGroup
    private lateinit var rbRegularly : RadioButton
    private lateinit var rbOnce : RadioButton
    private lateinit var spPeriod : Spinner
    private lateinit var spDay : Spinner
    private lateinit var llPeriod : LinearLayout
    private lateinit var llDay : LinearLayout
    private lateinit var dpDate : DatePicker
    private lateinit var tpTime : TimePicker
    private lateinit var btnSet : Button
    private lateinit var sql: SQLiteHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder)

        initView()
        sql = SQLiteHelper(this)

        rgType.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = findViewById(checkedId)
            val isRegular = radioButton.id == R.id.rbRegularly
            dpDate.isVisible = !isRegular
            llPeriod.isVisible = isRegular
            llDay.isVisible = isRegular
        }

        btnSet.setOnClickListener {
            setReminder()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setReminder() {
        val title = etTitle.text.toString()
        val desc = etTitle.text.toString()
        val type = getTypeID()
        val time: Long
        val periodId : Int
        if(type == 0) {
            periodId = spPeriod.selectedItemPosition
            if (periodId == 1 || periodId == 0) {
                time = setNextDayOfWeek()
            } else {
                time = setNextDayOfMonth()
            }

        } else {
            periodId = -1
            val calendar = Calendar.getInstance()
            calendar.set(dpDate.year, dpDate.month, dpDate.dayOfMonth, tpTime.hour, tpTime.minute)
            time = calendar.timeInMillis
        }

        val f = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val c = Calendar.getInstance()
        c.timeInMillis = time
        Log.e("EEE", f.format(c.time))

        val rmd = ReminderModel(0, title, desc, type, time, periodId)
//        sql.insertReminder(rmd)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNextDayOfMonth(): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val monthsCount : Long = when(spPeriod.selectedItemPosition){
            2 -> 1
            3 -> 2
            else -> 3
        }
        val thisMonthDate = prepareThisMonthDay()
        val localDate = LocalDateTime.parse(thisMonthDate, formatter)
        val thisMonthMillis = localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
        val time = when(thisMonthMillis > System.currentTimeMillis()){
            true -> thisMonthMillis
            else -> localDate.plusMonths(monthsCount)
                .atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
        }

        return time
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun prepareThisMonthDay(): String {
        val day = when(spDay.selectedItem.toString().length == 1){
            true -> "0${spDay.selectedItem}"
            else -> spDay.selectedItem.toString()
        }
        val hour = when(tpTime.hour.toString().length == 1){
            true -> "0${tpTime.hour}"
            else -> tpTime.hour.toString()
        }
        val minute = when(tpTime.minute.toString().length == 1){
            true -> "0${tpTime.minute}"
            else -> tpTime.minute.toString()
        }
        val thisMonthDate = LocalDate.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM")).toString() +
            "-" + day + " " + hour + ":" + minute

        return thisMonthDate
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNextDayOfWeek(): Long {
        val date = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val nextReminderDay = date.with(TemporalAdjusters.next(
            DayOfWeek.valueOf(spDay.selectedItem.toString().uppercase()))
        ).toString()
        val nextReminderTime = tpTime.hour.toString() + ":" + tpTime.minute.toString()
        val nextReminderString = "$nextReminderDay $nextReminderTime"
        val localDate = LocalDateTime.parse(nextReminderString, formatter)
        val time = localDate.atOffset(ZoneOffset.UTC).toInstant().toEpochMilli()
        return time
    }

    private fun getTypeID(): Int {
        val selectedOption: Int = rgType.checkedRadioButtonId
        val radioButton: RadioButton = findViewById(selectedOption)
        val type = when (radioButton.id){
            R.id.rbRegularly -> 0
            R.id.rbOnce -> 1
            else -> 0
        }

        return type
    }

    private fun initView() {
        etTitle = findViewById(R.id.etTitle)
        etDesc = findViewById(R.id.etDesc)
        rgType = findViewById(R.id.rgType)
        rbRegularly = findViewById(R.id.rbRegularly)
        rbOnce = findViewById(R.id.rbOnce)
        spPeriod = findViewById(R.id.spPeriod)
        spDay = findViewById(R.id.spDay)
        llPeriod = findViewById(R.id.llPeriod)
        llDay = findViewById(R.id.llDay)
        dpDate = findViewById(R.id.dpDate)
        tpTime = findViewById(R.id.tpTime)
        btnSet = findViewById(R.id.btnSet)

        tpTime.setIs24HourView(true)

        val periodsArray = arrayListOf(
            "Every week",
            "Every 2 weeks",
            "Every month",
            "Every 2 months",
            "Every 3 months"
        )

        spPeriod.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, periodsArray)
        spPeriod.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                initDaySpinner(position != 0 && position != 1)

            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun initDaySpinner(numbers: Boolean){
        var daysArray = arrayListOf<String>()
        if(numbers){
            var nr = 1
            do{
                daysArray.add(nr.toString())
                nr++
            }while (nr < 29)
            daysArray.add("Last")
        } else {
            daysArray = arrayListOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        }

        spDay.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, daysArray)
        spDay.onItemSelectedListener = object : OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}