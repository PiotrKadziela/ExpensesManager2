package com.example.expensesmanager2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class AddOperationActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnEdit: Button
    private lateinit var txtHeader: TextView
    private lateinit var txtError: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtShoppingList: TextView
    private lateinit var txtCurrency: TextView
    private lateinit var spCategory: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var rbExpense: RadioButton
    private lateinit var rbIncome: RadioButton
    private lateinit var sql: SQLiteHelper

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_operation)

        initView()
        sql = SQLiteHelper(this)

        val isEdit = intent.getStringExtra("edit")

        if(isEdit == "true"){
            loadEditView()
        }
        else {
            loadAddView()
        }

        var options = ArrayList<String>()
        rgType.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = findViewById(checkedId)
            if(radioButton.id == R.id.rbIncome){
                spCategory.setEnabled(false)
                etCost.hint = "Value"
                options.add("Income")
            } else {
                spCategory.isEnabled = true
                etCost.hint = "Cost"
                for(cat in sql.getAllCategories()){
                    options.add(cat.name)
                }
            }
            loadCategoriesSpinner(options)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun loadAddView() {
        rbExpense.isChecked = true
        if(intent.getIntExtra("oprList", 0) != 0){
            rbExpense.isEnabled = false
            rbIncome.isEnabled = false
        }
        btnEdit.isVisible = false
        etCost.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val costString = etCost.text.toString()
                val splitedCost = costString.split('.')
                val costDecimals = splitedCost.last()
                if(costDecimals.length > 2 && splitedCost.size > 1){
                    val decimalCost: Double = costString.toDouble()
                    val roundedCost: String = (floor(decimalCost * 100 ) / 100).toString()
                    etCost.setText(roundedCost)
                    etCost.setSelection(etCost.text.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}

        })
        btnAdd.setOnClickListener {
            if(etTitle.text.toString().length < 3){
                txtError.setText("Too short title!")
            }
            else if(etTitle.text.toString().isDigitsOnly()) {
                txtError.setText("Title cannot be a number!")
            }
            else if(etCost.text.toString().toDouble() <= 0) {
                txtError.setText("Cost has to be bigger than 0!")
            }
            else {
                addOperation()
            }
        }
        val date = SimpleDateFormat("MM/dd/yyyy")
        txtDate.text = date.format(System.currentTimeMillis())
        val options = ArrayList<String>()
        for (cat in sql.getAllCategories()){
            options.add(cat.name)
        }
        loadCategoriesSpinner(options)
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun loadEditView() {
        if(intent.getIntExtra("oprList", 0) != 0){
            rbExpense.isEnabled = false
            rbIncome.isEnabled = false
        }
        btnAdd.isVisible = false
        txtHeader.setText(resources.getString(R.string.edit_activity_header))
        setFieldsValues()
        etCost.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val costString = etCost.text.toString()
                val splitedCost = costString.split('.')
                val costDecimals = splitedCost.last()
                if(costDecimals.length > 2 && splitedCost.size > 1){
                    val decimalCost: Double = costString.toDouble()
                    val roundedCost: String = (floor(decimalCost * 100 ) / 100).toString()
                    etCost.setText(roundedCost)
                    etCost.setSelection(etCost.text.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}

        })
        btnEdit.setOnClickListener {
            if(etTitle.text.toString().length < 3){
                txtError.text = "Too short title!"
            }
            else if(etTitle.text.toString().isDigitsOnly()) {
                txtError.setText("Title cannot be a number!")
            }
            else if(etCost.text.toString().toDouble() <= 0) {
                txtError.setText("Cost has to be bigger than 0!")
            }
            else {
                updateOperation()
            }
        }
        val date = SimpleDateFormat("MM/dd/yyyy")
        txtDate.text = date.format(Date(intent.getLongExtra("oprDate", 0)))
    }

    private fun setFieldsValues() {
        val title = intent.getStringExtra("oprTitle")
        var cost = intent.getDoubleExtra("oprCost", 0.0)
        if(cost < 0) {
            cost *= -1
        }
        val category = intent.getStringExtra("oprCategory")
        val type = intent.getIntExtra("oprType", 0)

        val options = ArrayList<String>()

        when (type){
            1 -> options.add("Income")
            else -> {
                for(cat in sql.getAllCategories()){
                    options.add(cat.name)
                }
            }
        }
        loadCategoriesSpinner(options)

        if(type == 0){
            rbExpense.isChecked = true
        }
        else{
            rbIncome.isChecked = true
        }
        etTitle.setText(title)
        etCost.setText(cost.toString())
        spCategory.setSelection(options.indexOf(category))
        val list = intent.getIntExtra("oprList", 0)
        var listString = ""
        if(list != 0){
            listString += "SHOPPING LIST:\n\n"
            val prodList = sql.getAllListProd(list)

            if (prodList.size > 0) {
                for (prod in prodList) {
                    listString += "- " + prod.name + " " + prod.amount + "\n"
                }
            }

            else{
                listString += "All products from this\nlist have been deleted\nform database"
            }
        }

        txtShoppingList.text = listString
        txtCurrency.text = sql.getConfig()["currency"]
    }

    private fun loadCategoriesSpinner(options: ArrayList<String>) {
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateOperation() {
        val title = etTitle.text.toString()
        var cost = etCost.text.toString().toDouble()
        val id = intent.getIntExtra("oprId", 0)
        val type = getTypeID()
        val date = intent.getLongExtra("oprDate", 0)
        val list = sql.getOne("operations", "_id = $id")["list_id"]?.toInt()
        val category = when (type){
            0 -> sql.getOne("categories", "_id = " + (spCategory.selectedItemPosition + 1).toString())["name"]!!
            else -> "Income"
        }

        if(type == 0 && cost > 0 || type == 1 && cost < 0){
            cost *= -1
        }

        val opr = OperationModel(id, title, cost, category, type, list, date)
        sql.updateOperation(opr)
        val intentList = Intent(this, OperationsActivity::class.java)
        startActivity(intentList)
        finish()
        Toast.makeText(this, "Operation edited!", Toast.LENGTH_SHORT).show()
    }

    private fun getTypeID(): Int {
        val selectedOption: Int = rgType.checkedRadioButtonId
        val radioButton: RadioButton = findViewById(selectedOption)
        val type = when (radioButton.text){
            "Expense" -> 0
            "Income" -> 1
            else -> 0
        }

        return type
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addOperation() {
        val title = etTitle.text.toString()
        var cost = etCost.text.toString().toDouble()
        val category = spCategory.selectedItem.toString()
        val type = getTypeID()
        val date = System.currentTimeMillis()

        if(type == 0){
            cost *= -1
        }

        val list = intent.getIntExtra("oprList", 0)

        val opr = OperationModel(0, title, cost, category, type, list, date)
        val status = sql.insertOperation(opr)

        if (status > -1){
            Toast.makeText(this, "Operation added!", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(this, "Insert failed!", Toast.LENGTH_SHORT).show()
        }

        sql.startNewList()
        intent.getIntegerArrayListExtra("oprProds", )?.let { sql.updateListProd(it) }
        val intent = Intent(this, OperationsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initView() {
        etTitle = findViewById(R.id.etTitle)
        etCost = findViewById(R.id.etCost)
        btnAdd = findViewById(R.id.btnAdd)
        btnEdit = findViewById(R.id.btnEdit)
        spCategory = findViewById(R.id.spCategory)
        txtHeader = findViewById(R.id.txtHeader)
        txtError = findViewById(R.id.txtError)
        txtShoppingList = findViewById(R.id.txtShoppingList)
        txtDate = findViewById(R.id.txtDate)
        txtCurrency = findViewById(R.id.txtCurrency)
        rgType = findViewById(R.id.rgType)
        rbIncome = findViewById(R.id.rbIncome)
        rbExpense = findViewById(R.id.rbExpense)
    }
}