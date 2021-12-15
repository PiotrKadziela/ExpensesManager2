package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var btnShow: Button
    private lateinit var txtSaldo: TextView
    private lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
        sql = SQLiteHelper(this)

        btnAdd.setOnClickListener{
            val intent = Intent(this, AddOperationActivity::class.java)
            intent.putExtra("edit", "false")
            startActivity(intent)
        }

        btnShow.setOnClickListener{
            val intent = Intent(this, OperationsActivity::class.java)
            startActivity(intent)
        }

        val saldo = sql.getSaldo()

//        if(saldo == 0.0 && sql.getAllOperations().size == 0){
//            TODO: SHOW POPUP TO INSERT SALDO
//        }

        txtSaldo.text = saldo.toString()
    }

    private fun initView() {
        btnAdd = findViewById(R.id.btnAdd)
        btnShow = findViewById(R.id.btnShow)
        txtSaldo = findViewById(R.id.txtSaldo)
    }
}