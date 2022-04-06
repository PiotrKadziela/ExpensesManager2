package com.example.expensesmanager2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText

    class SettingsActivity : AppCompatActivity() {
        private lateinit var etCurrency : EditText
        private lateinit var etBalance : EditText
        private lateinit var btnSave : Button
        private lateinit var sql : SQLiteHelper

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_settings)

            initView()
            sql = SQLiteHelper(this)

            etCurrency.setText(sql.getConfig()["currency"])
            etBalance.setText(sql.getBalance().toString())

            btnSave.setOnClickListener {
                sql.setConfig(etBalance.text.toString(), etCurrency.text.toString())
                finish()
            }
        }

        private fun initView() {
            etCurrency = findViewById(R.id.etCurrency)
            etBalance = findViewById(R.id.etBalance)
            btnSave = findViewById(R.id.btnSave)
        }
    }