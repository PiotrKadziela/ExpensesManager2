package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.isDigitsOnly
import kotlin.math.floor

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

        etStartingSaldo.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val saldoString = etStartingSaldo.text.toString()
                val splitedSaldo = saldoString.split('.')
                val saldoDecimals = splitedSaldo.last()
                if(saldoDecimals.length > 2 && splitedSaldo.size > 1){
                    val decimalCost: Double = saldoString.toDouble()
                    val roundedCost: String = (floor(decimalCost * 100 ) / 100).toString()
                    etStartingSaldo.setText(roundedCost)
                    etStartingSaldo.setSelection(etStartingSaldo.text.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun initView() {
        btnAdd = findViewById(R.id.btnAdd)
        btnShow = findViewById(R.id.btnShow)
        txtSaldo = findViewById(R.id.txtSaldo)
    }
}