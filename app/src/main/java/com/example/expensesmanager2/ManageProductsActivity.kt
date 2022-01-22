package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ManageProductsActivity : AppCompatActivity() {
    private lateinit var sql: SQLiteHelper
    private lateinit var recyclerView: RecyclerView
    private var adapter: ProductAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_products)

        recyclerView = findViewById(R.id.rvProducts)
        initRecyclerView()
        sql = SQLiteHelper(this)

        getProducts()

        adapter?.setOnClickItem {
            openEditProdDialog(it)
        }

        adapter?.setOnClickDeleteItem { deleteProduct(it.id) }
    }

    private fun initRecyclerView() {
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
        with(builder){
            setTitle("Edit product")
            setPositiveButton("Save"){_, _ ->
                val name = etName.text.toString()
                val unit = etUnit.text.toString()

                regularlyBoughtDialog(prod.id, name, unit)
            }
            setNegativeButton("Cancel"){dialog, _ ->
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
        builder.setPositiveButton("YES"){dialog, _ ->
            val prod = ProductModel(id, name, unit, 1)
            sql.updateProduct(prod)
            getProducts()
            dialog.dismiss()
        }
        builder.setNegativeButton("NO"){dialog, _ ->
            val prod = ProductModel(id, name, unit, 0)
            sql.updateProduct(prod)
            getProducts()
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()

    }

    private fun getProducts() {
        val prodList = sql.getProducts()
        adapter?.addItems(prodList)
    }

    private fun deleteProduct(id: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are You sure?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES"){dialog, _ ->
            sql.deleteProduct(id)
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
}