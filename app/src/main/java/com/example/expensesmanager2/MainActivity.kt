package com.example.expensesmanager2

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import kotlin.math.floor

class MainActivity : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var btnShow: Button
    private lateinit var btnShoppingList: Button
    private lateinit var txtBalance: TextView
    private lateinit var ibMenu: ImageButton
    private lateinit var lvMenu: ListView
    private lateinit var sql: SQLiteHelper

    override fun onResume() {
        super.onResume()

        val balance = sql.getBalance()
        txtBalance.text = balance.toString()
    }

    @RequiresApi(Build.VERSION_CODES.M)
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

        btnShoppingList.setOnClickListener{
            val intent = Intent(this, ShoppingListActivity::class.java)
            startActivity(intent)
        }

        val balance = sql.getBalance()

        if(balance == 0.0 && sql.getAllOperations().size == 0){
            setStartingBalance()
        }

        txtBalance.text = balance.toString()

        ibMenu.setOnClickListener {
            lvMenu.isVisible = !lvMenu.isVisible
        }

        lvMenu.setOnItemClickListener { _, view, position, _ ->
            when(position){
                0 -> {
                    val intent = Intent(this, ManageProductsActivity::class.java)
                    startActivity(intent)
                }
                1 -> {
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    Toast.makeText(this, view.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setStartingBalance() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.add_starting_balance_dialog, null)
        val etStartingBalance = dialogLayout.findViewById<EditText>(R.id.etStartingBalance)

        with(builder){
            setTitle("Hello!")
            setPositiveButton("OK"){_, _ ->
                if(sql.insertBalance(etStartingBalance.text.toString().toDouble()) > -1){
                    Toast.makeText(this@MainActivity, "Balance set!", Toast.LENGTH_SHORT).show()
                    val balance = sql.getBalance()
                    txtBalance.text = balance.toString()
                }
            }
            setView(dialogLayout)
            show()
        }

        etStartingBalance.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val balanceString = etStartingBalance.text.toString()
                val splitedBalance = balanceString.split('.')
                val balanceDecimals = splitedBalance.last()
                if(balanceDecimals.length > 2 && splitedBalance.size > 1){
                    val decimalCost: Double = balanceString.toDouble()
                    val roundedCost: String = (floor(decimalCost * 100 ) / 100).toString()
                    etStartingBalance.setText(roundedCost)
                    etStartingBalance.setSelection(etStartingBalance.text.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}

        })
    }

    private fun initView() {
        btnAdd = findViewById(R.id.btnAdd)
        btnShow = findViewById(R.id.btnShow)
        btnShoppingList = findViewById(R.id.btnShoppingList)
        txtBalance = findViewById(R.id.txtBalance)
        ibMenu = findViewById(R.id.ibMenu)
        lvMenu = findViewById(R.id.lvMenu)

        val menuArray = arrayListOf<String>("Products", "Reminders", "Statistics")
        val arrayAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuArray)

        lvMenu.adapter = arrayAdapter
    }
}