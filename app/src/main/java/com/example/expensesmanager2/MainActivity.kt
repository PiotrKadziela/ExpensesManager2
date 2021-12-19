package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var btnShow: Button
    private lateinit var txtSaldo: TextView
    private lateinit var sql: SQLiteHelper

    override fun onResume() {
        super.onResume()

        val saldo = sql.getSaldo()
        txtSaldo.text = saldo.toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sql = SQLiteHelper(this)
        initView()

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

        if(saldo == 0.0 && sql.getAllOperations().size == 0){
            setStartingSaldo()
        }

        txtSaldo.text = saldo.toString()
    }

    private fun setStartingSaldo() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.add_starting_saldo_dialog, null)
        val etStartingSaldo = dialogLayout.findViewById<EditText>(R.id.etStartingSaldo)

        with(builder){
            setTitle("Hello!")
            setPositiveButton("OK"){dialog, which ->
                if(sql.insertSaldo(etStartingSaldo.text.toString().toDouble()) > -1){
                    Toast.makeText(this@MainActivity, "Saldo set!", Toast.LENGTH_SHORT).show()
                    val saldo = sql.getSaldo()
                    txtSaldo.text = saldo.toString()
                }
            }
            setView(dialogLayout)
            show()
        }


    }

    private fun initView() {
        btnAdd = findViewById(R.id.btnAdd)
        btnShow = findViewById(R.id.btnShow)
        txtSaldo = findViewById(R.id.txtSaldo)
    }
}