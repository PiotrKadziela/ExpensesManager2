package com.example.expensesmanager2

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import kotlin.math.floor

class AddOperationActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnEdit: Button
    private lateinit var txtHeader: TextView
    private lateinit var txtError: TextView
    private lateinit var txtShoppingList: TextView
    private lateinit var spCategory: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var rbExpense: RadioButton
    private lateinit var rbIncome: RadioButton
    private lateinit var sql: SQLiteHelper

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

        var options: Array<String>
        rgType.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = findViewById(checkedId)
            if(radioButton.text == "Income"){
                spCategory.setEnabled(false)
                options =  arrayOf("Income")
            } else {
                spCategory.isEnabled = true
                options = sql.getAllCategories()
            }
            loadCategoriesSpinner(options)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun loadAddView() {
        rbExpense.isChecked = true
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

        val options = sql.getAllCategories()
        loadCategoriesSpinner(options)
    }

    @SuppressLint("SetTextI18n")
    private fun loadEditView() {
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
    }

    private fun setFieldsValues() {
        val title = intent.getStringExtra("oprTitle")
        var cost = intent.getDoubleExtra("oprCost", 0.0)
        if(cost < 0) {
            cost *= -1
        }
        val category = intent.getStringExtra("oprCategory")
        val type = intent.getIntExtra("oprType", 0)

        val options = when (type){
            1 -> arrayOf("Income")
            else -> sql.getAllCategories()
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

            for(prod in prodList){
                listString += "- " + prod.name + " " + prod.amount + "\n"
            }
        }

        txtShoppingList.text = listString
    }

    private fun loadCategoriesSpinner(options: Array<String>) {
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
        val list = sql.getOne("operations", "_id = $id")["list_id"]?.toInt()
        val category = when (type){
            0 -> sql.getOne("categories", "_id = " + (spCategory.selectedItemPosition + 1).toString())["name"]!!
            else -> "Income"
        }

        if(type == 0 && cost > 0 || type == 1 && cost < 0){
            cost *= -1
        }

        val opr = OperationModel(id, title, cost, category, type, list)
        sql.updateOperation(opr)
        val intentList = Intent(this, OperationsActivity::class.java)
        startActivity(intentList)
        finish()
        Toast.makeText(this, "Operation edited!", Toast.LENGTH_SHORT).show()
    }

    private fun checkIfListAttached(): Boolean {
        TODO("Not yet implemented")
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

    private fun addOperation() {
        val title = etTitle.text.toString()
        var cost = etCost.text.toString().toDouble()
        val category = spCategory.selectedItem.toString()
        val type = getTypeID()

        if(type == 0){
            cost = cost * -1
        }

        val list = intent.getIntExtra("oprList", 0)

        val opr = OperationModel(0, title, cost, category, type, list)
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
        rgType = findViewById(R.id.rgType)
        rbIncome = findViewById(R.id.rbIncome)
        rbExpense = findViewById(R.id.rbExpense)
    }
}