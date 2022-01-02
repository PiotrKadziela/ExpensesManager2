package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShoppingListActivity : AppCompatActivity() {
    lateinit var btnAdd: Button
    lateinit var rvProducts: RecyclerView
    lateinit var sql: SQLiteHelper
    private var adapter: ListProdAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        initView()
        initRecyclerView()
        sql = SQLiteHelper(this)
        getProducts()
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getProducts() {
        val prodList = sql.getAllListProd()
        adapter?.addItems(prodList)
    }

    private fun initRecyclerView() {
        rvProducts.layoutManager = LinearLayoutManager(this)
        adapter = ListProdAdapter()
        rvProducts.adapter = adapter
    }

    private fun initView() {
        btnAdd = findViewById(R.id.btnAdd)
        rvProducts = findViewById(R.id.rvProducts)
    }
}