package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ShoppingListActivity : AppCompatActivity(), ListProdListener{
    lateinit var btnAdd: Button
    lateinit var btnExecute: Button
    lateinit var rvProducts: RecyclerView
    val sql = SQLiteHelper(this)
    private var adapter: ListProdAdapter? = null
    private var selectedProducts = ArrayList<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        initView()
        initRecyclerView()
        getProducts()
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            startActivity(intent)
            finish()
        }
        btnExecute.setOnClickListener {
            if(selectedProducts.isEmpty()){
                Toast.makeText(this, "No product selected!", Toast.LENGTH_SHORT).show()
            } else {
                TODO("Open adding expense with shopping list attached")
            }
        }

        adapter?.setOnClickDeleteItem { deleteProduct(it.id) }
    }

    private fun getProducts(): ArrayList<ListProdModel> {
        val prodList = sql.getAllListProd()
        adapter?.addItems(prodList)

        return prodList
    }

    private fun deleteProduct(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES"){dialog, _ ->
            sql.deleteListProd(id)
            getProducts()
            Toast.makeText(this, "Product deleted!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("NO"){dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    private fun initRecyclerView() {
        rvProducts.layoutManager = LinearLayoutManager(this)
        val productArray = ArrayList<Int>()
        for (product in getProducts()){
            productArray.add(product.id)
        }
        adapter = ListProdAdapter(this, productArray, this)
        rvProducts.adapter = adapter
    }

    private fun initView() {
        btnAdd = findViewById(R.id.btnAdd)
        btnExecute = findViewById(R.id.btnExecute)
        rvProducts = findViewById(R.id.rvProducts)
    }

    override fun onListProdChange(arrayList: ArrayList<Int>) {
        this.selectedProducts = arrayList
    }
}