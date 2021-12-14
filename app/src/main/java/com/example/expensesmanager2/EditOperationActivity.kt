package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class EditOperationActivity : AppCompatActivity() {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnEdit: Button
    private lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_operation)

        sql = SQLiteHelper(this)

        initView()

        val title = intent.getStringExtra("oprTitle")
        val cost = intent.getStringExtra("oprCost")

        etTitle.setText(title)
        etCost.setText(cost)

        btnEdit.setOnClickListener { updateOperation() }

    }

    private fun updateOperation() {
        val title = etTitle.text.toString()
        val cost = etCost.text.toString()
        val id = intent.getIntExtra("oprId", 0)
        val opr = OperationModel(id, title, cost)
        sql.updateOperation(opr)
        val intentList = Intent(this, OperationsActivity::class.java)
        startActivity(intentList)
        Toast.makeText(this, "Operation edited!", Toast.LENGTH_SHORT).show()
    }

    private fun initView() {
        etTitle = findViewById(R.id.etTitle)
        etCost = findViewById(R.id.etCost)
        btnEdit = findViewById(R.id.btnEdit)
    }
}