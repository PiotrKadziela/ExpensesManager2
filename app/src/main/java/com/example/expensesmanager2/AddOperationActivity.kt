package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.strictmode.SqliteObjectLeakedViolation
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener

class AddOperationActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnAdd: Button
    private lateinit var spCategory: Spinner
    private lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_operation)

        initView()
        sql = SQLiteHelper(this)

        btnAdd.setOnClickListener {
            addOperation()
        }

        val options = resources.getStringArray(R.array.Categories)
        spCategory.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, options)

        spCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {

            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Toast.makeText(getParent(), "Operation added!", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun addOperation() {
        val title = etTitle.text.toString()
        val cost = etCost.text.toString()
        val category = spCategory.selectedItem.toString()

        if(title.isEmpty() || cost.isEmpty()){
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
        } else{
            val opr = OperationModel(0, title, cost, category)
            val status = sql.insertOperation(opr)

            if (status > -1){
                Toast.makeText(this, "Operation added!", Toast.LENGTH_SHORT).show()
                clearEditTexts()
            } else{
                Toast.makeText(this, "Insert failed!", Toast.LENGTH_SHORT).show()
            }
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
        spCategory = findViewById(R.id.spCategory)
    }
}