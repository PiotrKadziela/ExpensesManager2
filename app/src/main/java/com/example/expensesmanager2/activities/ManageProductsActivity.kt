package com.example.expensesmanager2.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager2.adapters.ProductAdapter
import com.example.expensesmanager2.models.ProductModel
import com.example.expensesmanager2.R
import com.example.expensesmanager2.interfaces.ActivityInterface
import com.example.expensesmanager2.utils.SQLiteHelper

class ManageProductsActivity : AppCompatActivity(), ActivityInterface {
    private lateinit var sql: SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private var adapter: ProductAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_products)
        sql = SQLiteHelper(this)

        initView()
        loadView()
        setListeners()
    }

    override fun initView() {
        recyclerView = findViewById(R.id.rvProducts)
    }

    override fun loadView() {
        loadRecyclerView()
        getProducts()
    }

    override fun setListeners() {
        adapter?.setOnClickItem { openEditProdDialog(it) }
        adapter?.setOnClickDeleteItem { deleteProduct(it.id) }
    }

    private fun loadRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ProductAdapter()
        recyclerView.adapter = adapter
    }

    private fun openEditProdDialog(prod: ProductModel) {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.new_product_dialog, null)
        val etName = dialogLayout.findViewById<EditText>(R.id.etName)
        val etUnit = dialogLayout.findViewById<EditText>(R.id.etUnit)
        etUnit.setText(prod.unit)
        etName.setText(prod.name)
        with(builder) {
            setTitle("Edit product")
            setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString()
                val unit = etUnit.text.toString()

                regularlyBoughtDialog(prod.id, name, unit)
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            setView(dialogLayout)
            show()
        }
    }

    private fun regularlyBoughtDialog(id: Int, name: String, unit: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Is the product bought regularly?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES") { dialog, _ ->
            val prod = ProductModel(this, id, name, unit, 1)
            prod.update()
            getProducts()
            dialog.dismiss()
        }
        builder.setNegativeButton("NO") { dialog, _ ->
            val prod = ProductModel(this, id, name, unit, 0)
            prod.update()
            getProducts()
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()

    }

    private fun getProducts() {
        val prodList = ProductModel(this).get()
        adapter?.addItems(prodList)
    }

    private fun deleteProduct(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES") { dialog, _ ->
            ProductModel(this).delete("_id=$id")
            getProducts()
            Toast.makeText(this, "Product deleted!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        builder.setNegativeButton("NO") { dialog, _ ->
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()
    }
}