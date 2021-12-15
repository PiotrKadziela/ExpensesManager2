package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isVisible

class AddOperationActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnEdit: Button
    private lateinit var txtHeader: TextView
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

        val options = resources.getStringArray(R.array.Categories)
        loadCategoriesSpinner(options)
    }

    private fun loadAddView() {
        rbExpense.isChecked = true
        btnEdit.isVisible = false
        btnAdd.setOnClickListener {
            addOperation()
        }
    }

    private fun loadEditView() {
        btnAdd.isVisible = false
        txtHeader.setText(resources.getString(R.string.edit_activity_header))
        setFieldsValues()
        btnEdit.setOnClickListener { updateOperation() }
    }

    private fun setFieldsValues() {
        val title = intent.getStringExtra("oprTitle")
        var cost = intent.getDoubleExtra("oprCost", 0.0)
        if(cost < 0) {
            cost *= -1
        }
        val category = intent.getStringExtra("oprCategory")
        val type = intent.getIntExtra("oprType", 0)

        val options = resources.getStringArray(R.array.Categories)
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
        val category = spCategory.selectedItem.toString()
        val id = intent.getIntExtra("oprId", 0)
        val type = getTypeID()

        if(type == 0 && cost > 0 || type == 1 && cost < 0){
            cost = cost * -1
        }

        val opr = OperationModel(id, title, cost, category, type)
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

    private fun addOperation() {
        val title = etTitle.text.toString()
        var cost = etCost.text.toString().toDouble()
        val category = spCategory.selectedItem.toString()
        val type = getTypeID()

        if(type == 0){
            cost = cost * -1
        }

        val opr = OperationModel(0, title, cost, category, type)
        val status = sql.insertOperation(opr)

        if (status > -1){
            Toast.makeText(this, "Operation added!", Toast.LENGTH_SHORT).show()
            clearEditTexts()
        } else{
            Toast.makeText(this, "Insert failed!", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this, OperationsActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun clearEditTexts() {
        etTitle.setText("")
        etCost.setText("")
        etCost.requestFocus()
    }

    private fun initView() {
        etTitle = findViewById(R.id.etTitle)
        etCost = findViewById(R.id.etCost)
        btnAdd = findViewById(R.id.btnAdd)
        btnEdit = findViewById(R.id.btnEdit)
        spCategory = findViewById(R.id.spCategory)
        txtHeader = findViewById(R.id.txtHeader)
        rgType = findViewById(R.id.rgType)
        rbIncome = findViewById(R.id.rbIncome)
        rbExpense = findViewById(R.id.rbExpense)
    }
}