package com.example.expensesmanager2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager2.*
import com.example.expensesmanager2.adapters.ListProdAdapter
import com.example.expensesmanager2.interfaces.ListProdListener
import com.example.expensesmanager2.models.ListModel
import com.example.expensesmanager2.models.ListProdModel
import com.example.expensesmanager2.models.ProductModel
import com.example.expensesmanager2.utils.SQLiteHelper

class ShoppingListActivity : AppCompatActivity(), ListProdListener {
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
            if (selectedProducts.isEmpty()) {
                Toast.makeText(this, "No product selected!", Toast.LENGTH_SHORT).show()
            } else {
                val newestListId = ListModel(this).getNewestListId()
                val listProdArray = ListProdModel(this).get("list_id = $newestListId")
                val listProdIds = ArrayList<Int>()

                for (prod in listProdArray) {
                    if (!selectedProducts.contains(prod.id)) {
                        listProdIds.add(prod.id)
                    }
                }
                val intent = Intent(this, AddOperationActivity::class.java)
                intent.putExtra("edit", false)
                intent.putExtra("oprList", newestListId)
                intent.putExtra("oprProds", listProdIds)
                startActivity(intent)
                finish()
            }
        }

        adapter?.setOnClickDeleteItem { deleteProduct(it.id) }
    }

    private fun addProducts() {
        val products = ProductModel(this).get()
        val currentListProd = ListProdModel(this).get(
            "list_id = ${ListModel(this).getNewestListId()}"
        )

        for (prod in products) {
            if (prod.isBoughtRegularly == 0) {
                continue
            }
            val avg = prod.getAverageValues()
            if (avg["time"]!!.toInt() < 1 || avg["amount"]!!.toInt() < 1) {
                continue
            }
            val lastBuy = prod.getLastBuyDate()
            val diff = (System.currentTimeMillis() - lastBuy) / 1000 / 3600 / 24
            var isOnList = false
            for (product in currentListProd) {
                if (product.prod_id == prod.id) {
                    isOnList = true
                }
            }
            if (diff + 1 >= avg["time"]!! && !isOnList && prod.isBoughtRegularly == 1) {
                val lp = ListProdModel(
                    this,
                    0,
                    ListModel(this).getNewestListId(),
                    prod.id,
                    avg["amount"]!!.toDouble()
                )
                lp.insert()
            }
        }
    }

    private fun getProducts(): ArrayList<ListProdModel> {
        val prodList = ListProdModel(this).get(
            "list_id = ${ListModel(this).getNewestListId()}"
        )
        adapter?.addItems(prodList)

        return prodList
    }

    private fun deleteProduct(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES") { dialog, _ ->
            ListProdModel(this).delete("_id = $id")
            getProducts()
            Toast.makeText(this, "Product deleted!", Toast.LENGTH_SHORT).show()
            initRecyclerView()
            getProducts()
            adapter?.setOnClickDeleteItem { deleteProduct(it.id) }
            dialog.dismiss()
        }
        builder.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }

    private fun initRecyclerView() {
        rvProducts.layoutManager = LinearLayoutManager(this)
        val productArray = ArrayList<Int>()
        for (product in getProducts()) {
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