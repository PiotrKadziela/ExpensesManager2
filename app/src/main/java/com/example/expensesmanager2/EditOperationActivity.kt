package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*

class EditOperationActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnEdit: Button
    private lateinit var spCategory: Spinner
    private lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_operation)

        sql = SQLiteHelper(this)

        initView()

        val title = intent.getStringExtra("oprTitle")
        val cost = intent.getStringExtra("oprCost")
        val category = intent.getStringExtra("oprCategory")

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

        etTitle.setText(title)
        etCost.setText(cost)
        spCategory.setSelection(options.indexOf(category))


        btnEdit.setOnClickListener { updateOperation() }
    }

    private fun updateOperation() {
        val title = etTitle.text.toString()
        val cost = etCost.text.toString()
        val category = spCategory.selectedItem.toString()
        val id = intent.getIntExtra("oprId", 0)
        val opr = OperationModel(id, title, cost, category)
        sql.updateOperation(opr)
        val intentList = Intent(this, OperationsActivity::class.java)
        startActivity(intentList)
        finish()
        Toast.makeText(this, "Operation edited!", Toast.LENGTH_SHORT).show()
    }

    private fun initView() {
        etTitle = findViewById(R.id.etTitle)
        etCost = findViewById(R.id.etCost)
        spCategory = findViewById(R.id.spCategory)
        btnEdit = findViewById(R.id.btnEdit)
    }
}