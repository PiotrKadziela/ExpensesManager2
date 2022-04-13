package com.example.expensesmanager2.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager2.utils.AlertReceiver
import com.example.expensesmanager2.R
import com.example.expensesmanager2.adapters.ReminderAdapter
import com.example.expensesmanager2.models.ReminderModel
import com.example.expensesmanager2.utils.SQLiteHelper

class RemindersActivity : AppCompatActivity() {
    private lateinit var btnNewReminder : Button
    private lateinit var recyclerView: RecyclerView
    private var adapter: ReminderAdapter? = null
    private lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        btnNewReminder = findViewById(R.id.btnNewReminder)

        btnNewReminder.setOnClickListener {
            val intent = Intent(this, NewReminderActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView = findViewById(R.id.rvReminders)
        initRecyclerView()
        sql = SQLiteHelper(this)
        getReminders()

        adapter?.setOnClickDeleteItem { deleteReminder(it.id)}

        ReminderModel(this).delete("type = 1 AND time < ${System.currentTimeMillis()}")
    }

    private fun deleteReminder(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES"){dialog, _ ->
            ReminderModel(this).delete("_id=$id")
            unsetReminder(id)
            getReminders()
            Toast.makeText(this, "Reminder deleted!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("NO"){dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    private fun unsetReminder(id: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlertReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0)
        alarmManager.cancel(pendingIntent)
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ReminderAdapter()
        recyclerView.adapter = adapter
    }

    private fun getReminders() {
        val rmdList = ReminderModel(this).get()

        adapter?.addItems(rmdList)
    }
}