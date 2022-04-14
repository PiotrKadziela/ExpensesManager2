package com.example.expensesmanager2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager2.adapters.OperationAdapter
import com.example.expensesmanager2.R
import com.example.expensesmanager2.interfaces.ActivityInterface
import com.example.expensesmanager2.models.OperationModel
import com.example.expensesmanager2.utils.SQLiteHelper

class OperationsActivity : AppCompatActivity(), ActivityInterface {
    private lateinit var sql: SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private var adapter: OperationAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_operations)
        sql = SQLiteHelper(this)

        initView()
        loadView()
        setListeners()

    }

    override fun initView() {
        recyclerView = findViewById(R.id.rvOperations)
    }

    override fun loadView() {
        loadRecyclerView()
        getOperations()
    }

    override fun setListeners() {
        setOperationAdapterListeners()
    }

    private fun loadRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OperationAdapter()
        recyclerView.adapter = adapter
        for (item in recyclerView.children) {
            item
        }
    }

    private fun deleteOperation(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES") { dialog, _ ->
            OperationModel(this).delete("_id=$id")
            getOperations()
            Toast.makeText(this, "Operation deleted!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        builder.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    private fun getOperations() {
        val oprList = OperationModel(this).get()

        adapter?.addItems(oprList)
    }

    private fun setOperationAdapterListeners() {
        adapter?.setOnClickItem {
            val intent = Intent(this, AddOperationActivity::class.java)
            intent.putExtra("oprId", it.id)
            intent.putExtra("oprTitle", it.title)
            intent.putExtra("oprCost", it.cost)
            intent.putExtra("oprCategory", it.category)
            intent.putExtra("edit", true)
            intent.putExtra("oprType", it.type)
            intent.putExtra("oprList", it.listId)
            intent.putExtra("oprDate", it.date)
            startActivity(intent)
            finish()
        }

        adapter?.setOnClickDeleteItem { deleteOperation(it.id) }
    }
}