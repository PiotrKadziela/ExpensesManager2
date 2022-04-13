package com.example.expensesmanager2.activities

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
import androidx.core.view.isVisible
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.ConfigModel
import com.example.expensesmanager2.models.OperationModel
import com.example.expensesmanager2.utils.SQLiteHelper
import kotlin.math.floor

class MainActivity : AppCompatActivity() {
    private lateinit var btnAdd: Button
    private lateinit var btnShow: Button
    private lateinit var btnShoppingList: Button
    private lateinit var txtBalance: TextView
    private lateinit var txtCurrency: TextView
    private lateinit var ibMenu: ImageButton
    private lateinit var lvMenu: ListView
    private lateinit var sql: SQLiteHelper

    override fun onResume() {
        super.onResume()

        val string = ConfigModel(this).get("balance").split('.')
        txtBalance.text = if (string[1].length > 1)
            ConfigModel(this).get("balance") else
            ConfigModel(this).get("balance") + "0"
        txtCurrency.text = ConfigModel(this).get("currency")
        lvMenu.isVisible = false
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sql = SQLiteHelper(this)
        initView()

        btnAdd.setOnClickListener {
            val intent = Intent(this, AddOperationActivity::class.java)
            intent.putExtra("edit", "false")
            startActivity(intent)
        }

        btnShow.setOnClickListener {
            val intent = Intent(this, OperationsActivity::class.java)
            startActivity(intent)
        }

        btnShoppingList.setOnClickListener {
            val intent = Intent(this, ShoppingListActivity::class.java)
            startActivity(intent)
        }

        val balance = ConfigModel(this).get("balance").toDouble()

        if (balance == 0.0 && OperationModel(this).get().size == 0) {
            setStartingBalance()
        }

        txtBalance.text = balance.toString()

        ibMenu.setOnClickListener {
            lvMenu.isVisible = !lvMenu.isVisible
        }

        lvMenu.setOnItemClickListener { _, view, position, _ ->
            when (position) {
                0 -> {
                    val intent = Intent(this, ManageProductsActivity::class.java)
                    startActivity(intent)
                }
                1 -> {
                    val intent = Intent(this, RemindersActivity::class.java)
                    startActivity(intent)
                }
                2 -> {
                    val intent = Intent(this, StatisticsActivity::class.java)
                    startActivity(intent)
                }
                else -> {

                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun setStartingBalance() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.add_starting_balance_dialog, null)
        val etStartingBalance = dialogLayout.findViewById<EditText>(R.id.etStartingBalance)
        val etCurrency = dialogLayout.findViewById<EditText>(R.id.etCurrency)

        with(builder) {
            setTitle("Hello!")
            setPositiveButton("OK") { _, _ ->
                if (ConfigModel(
                        this@MainActivity,
                        "balance",
                        etStartingBalance.text.toString()
                    ).insert() > -1 &&
                    ConfigModel(
                        this@MainActivity,
                        "currency",
                        etCurrency.text.toString()
                    ).insert() > -1
                ) {
                    txtBalance.text = ConfigModel(this@MainActivity).get("balance")
                    txtCurrency.text = ConfigModel(this@MainActivity).get("currency")
                }
            }
            setView(dialogLayout)
            show()
        }

        etStartingBalance.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val balanceString = etStartingBalance.text.toString()
                val splitedBalance = balanceString.split('.')
                val balanceDecimals = splitedBalance.last()
                if (balanceDecimals.length > 2 && splitedBalance.size > 1) {
                    val decimalCost: Double = balanceString.toDouble()
                    val roundedCost: String = (floor(decimalCost * 100) / 100).toString()
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
        txtCurrency = findViewById(R.id.txtCurrency)
        ibMenu = findViewById(R.id.ibMenu)
        lvMenu = findViewById(R.id.lvMenu)

        val menuArray = arrayListOf<String>("Products", "Reminders", "Statistics", "Settings")
        val arrayAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuArray)

        lvMenu.adapter = arrayAdapter
    }
}