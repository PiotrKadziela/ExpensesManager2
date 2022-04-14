package com.example.expensesmanager2.activities

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.example.expensesmanager2.utils.AlertReceiver
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.ReminderModel
import com.example.expensesmanager2.utils.SQLiteHelper
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
import java.util.*
import kotlin.random.Random

class NewReminderActivity : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDesc: EditText
    private lateinit var rgType: RadioGroup
    private lateinit var rbPeriodically: RadioButton
    private lateinit var rbOnce: RadioButton
    private lateinit var spPeriod: Spinner
    private lateinit var spDay: Spinner
    private lateinit var llPeriod: LinearLayout
    private lateinit var llDay: LinearLayout
    private lateinit var dpDate: DatePicker
    private lateinit var tpTime: TimePicker
    private lateinit var btnSet: Button
    private lateinit var sql: SQLiteHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_reminder)

        initView()
        sql = SQLiteHelper(this)

        rgType.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = findViewById(checkedId)
            val isPeriodic = radioButton.id == R.id.rbPeriodically
            dpDate.isVisible = !isPeriodic
            llPeriod.isVisible = isPeriodic
            llDay.isVisible = isPeriodic
        }

        btnSet.setOnClickListener {
            setReminder()
            finish()
        }
    }

    @SuppressLint("SimpleDateFormat")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setReminder() {
        val title = etTitle.text.toString()
        val desc = etDesc.text.toString()
        val type = getTypeID()
        val time: Long
        val periodId: Int
        val id = Random.nextInt()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        intent.putExtra("title", title)
        intent.putExtra("desc", desc)
        intent.putExtra("id", id)

        if (type == 0) {
            periodId = spPeriod.selectedItemPosition
            intent.putExtra("period", periodId)
            time = when (periodId) {
                0, 1 -> setNextDayOfWeek()
                else -> setNextDayOfMonth()
            }
            val pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        } else {
            periodId = -1
            val calendar = Calendar.getInstance()
            calendar.set(dpDate.year, dpDate.month, dpDate.dayOfMonth, tpTime.hour, tpTime.minute)
            time = calendar.timeInMillis
            val pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }

        Toast.makeText(this, "Reminder set", Toast.LENGTH_SHORT).show()
        val rmd = ReminderModel(this, id, title, desc, type, time, periodId)
        rmd.insert()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNextDayOfMonth(): Long {
        val monthsCount = when (spPeriod.selectedItemPosition) {
            2 -> 1
            3 -> 2
            else -> 3
        }
        val calendar = Calendar.getInstance()
        val day = when (spDay.selectedItemPosition) {
            28 -> calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            else -> spDay.selectedItemPosition + 1
        }
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, tpTime.hour)
        calendar.set(Calendar.MINUTE, tpTime.minute)
        if (calendar.timeInMillis > System.currentTimeMillis()) {
            return calendar.timeInMillis

        } else {
            calendar.add(Calendar.MONTH, monthsCount)
            return calendar.timeInMillis
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setNextDayOfWeek(): Long {
        val date = LocalDate.now()
        val nextReminderDate =
            if (date.dayOfWeek == DayOfWeek.valueOf(spDay.selectedItem.toString().uppercase())) date
            else date.with(
                TemporalAdjusters.next(
                    DayOfWeek.valueOf(spDay.selectedItem.toString().uppercase())
                )
            )

        val calendar = Calendar.getInstance()
        calendar.set(
            nextReminderDate.year,
            nextReminderDate.monthValue - 1,
            nextReminderDate.dayOfMonth,
            tpTime.hour,
            tpTime.minute
        )

        return calendar.timeInMillis
    }

    private fun getTypeID(): Int {
        val selectedOption: Int = rgType.checkedRadioButtonId
        val radioButton: RadioButton = findViewById(selectedOption)
        val type = when (radioButton.id) {
            R.id.rbPeriodically -> 0
            R.id.rbOnce -> 1
            else -> 0
        }

        return type
    }

    private fun initView() {
        etTitle = findViewById(R.id.etTitle)
        etDesc = findViewById(R.id.etDesc)
        rgType = findViewById(R.id.rgType)
        rbPeriodically = findViewById(R.id.rbPeriodically)
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
        spPeriod.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                initDaySpinner(position != 0 && position != 1)

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

    }

    private fun initDaySpinner(numbers: Boolean) {
        var daysArray = arrayListOf<String>()
        if (numbers) {
            var nr = 1
            do {
                daysArray.add(nr.toString())
                nr++
            } while (nr < 29)
            daysArray.add("Last")
        } else {
            daysArray = arrayListOf(
                "Monday",
                "Tuesday",
                "Wednesday",
                "Thursday",
                "Friday",
                "Saturday",
                "Sunday"
            )
        }

        spDay.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, daysArray)
        spDay.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

}