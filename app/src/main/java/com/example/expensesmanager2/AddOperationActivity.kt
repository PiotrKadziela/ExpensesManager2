package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.strictmode.SqliteObjectLeakedViolation
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class AddOperationActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnAdd: Button
    private lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_operation)

        initView()
        sql = SQLiteHelper(this)

        btnAdd.setOnClickListener {
            addOperation()
        }
    }

    private fun addOperation() {
        val title = etTitle.text.toString()
        val cost = etCost.text.toString()

        if(title.isEmpty() || cost.isEmpty()){
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
        } else{
            val opr = OperationModel(0, title, cost)
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
    }
}