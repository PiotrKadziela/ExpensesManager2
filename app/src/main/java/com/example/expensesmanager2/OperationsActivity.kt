package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OperationsActivity : AppCompatActivity() {
    private lateinit var sql: SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private var adapter: OperationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operations)

        recyclerView = findViewById(R.id.rvOperations)
        initRecyclerView()
        sql = SQLiteHelper(this)
        getOperations()

        adapter?.setOnClickItem {
            val intent = Intent(this, EditOperationActivity::class.java)
            intent.putExtra("oprId", it.id)
            intent.putExtra("oprTitle", it.title)
            intent.putExtra("oprCost", it.cost)
            intent.putExtra("oprCategory", it.category)
            startActivity(intent)
            finish()
        }

        adapter?.setOnClickDeleteItem { deleteOperation(it.id)}
    }

    private fun initRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OperationAdapter()
        recyclerView.adapter = adapter
    }

    private fun deleteOperation(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES"){dialog, _ ->
            sql.deleteOperation(id)
            getOperations()
            Toast.makeText(this, "Operation deleted!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("NO"){dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }
    private fun getOperations() {
        val oprList = sql.getAllOperations()

        adapter?.addItems(oprList)
    }

}