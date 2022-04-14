package com.example.expensesmanager2.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.ConfigModel
import com.example.expensesmanager2.models.OperationModel
import com.example.expensesmanager2.utils.SQLiteHelper

class SettingsActivity : AppCompatActivity() {
    private lateinit var etCurrency: EditText
    private lateinit var etBalance: EditText
    private lateinit var btnSave: Button
    private lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initView()
        sql = SQLiteHelper(this)

        etCurrency.setText(ConfigModel(this).get("currency"))
        etBalance.setText(ConfigModel(this).get("balance"))

        btnSave.setOnClickListener {
            ConfigModel(this, "currency", etCurrency.text.toString()).update()
            var balance = etBalance.text.toString().toDouble()
            for (operation in OperationModel(this).get()) {
                balance -= operation.cost
            }
            ConfigModel(this, "balance", balance.toString()).update()
            finish()
        }
    }

    private fun initView() {
        etCurrency = findViewById(R.id.etCurrency)
        etBalance = findViewById(R.id.etBalance)
        btnSave = findViewById(R.id.btnSave)
    }
}