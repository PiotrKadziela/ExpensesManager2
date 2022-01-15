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
        addProducts()
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
                val newestListId = sql.getNewestListId()
                val listProdArray = sql.getAllListProd(newestListId)
                val listProdIds = ArrayList<Int>()

                for(prod in listProdArray){
                    if(!selectedProducts.contains(prod.id)){
                        listProdIds.add(prod.id)
                    }
                }
                val intent = Intent(this, AddOperationActivity::class.java)
                intent.putExtra("edit", "false")
                intent.putExtra("oprList", newestListId)
                intent.putExtra("oprProds", listProdIds)
                startActivity(intent)
                finish()
            }
        }

        adapter?.setOnClickDeleteItem { deleteProduct(it.id) }
    }

    private fun addProducts() {
        val products = sql.getProducts()
        val currentListProd = sql.getAllListProd(sql.getNewestListId())

        for(prod in products){
            val avg = sql.getAverageProdBuy(prod.id)
            val lastBuy = sql.getLastBuyDate(prod.id)
            val diff = (System.currentTimeMillis() - lastBuy) / 1000 / 3600 / 24
            var isOnList = false
            for (product in currentListProd){
                if(product.name == prod.name){
                    isOnList = true
                }
            }
            if(diff +1 >= avg["time"]!! && !isOnList){
                val lp = ListProdModel(0, sql.getNewestListId(), prod.name, avg["amount"]!!.toString())
                sql.insertListProd(lp)
            }
        }
    }

    private fun getProducts(): ArrayList<ListProdModel> {
        val prodList = sql.getAllListProd(sql.getNewestListId())
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
            initRecyclerView()
            getProducts()
            adapter?.setOnClickDeleteItem { deleteProduct(it.id) }
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