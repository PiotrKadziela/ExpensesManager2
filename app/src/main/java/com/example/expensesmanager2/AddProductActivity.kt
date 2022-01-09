package com.example.expensesmanager2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog

class AddProductActivity : AppCompatActivity() {
    lateinit var spProduct: Spinner
    lateinit var btnNewProd: Button
    lateinit var btnAdd: Button
    lateinit var etAmount: EditText
    lateinit var sql: SQLiteHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        sql = SQLiteHelper(this)
        initView()

        val products = sql.getAllProducts("name")
        spProduct.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, products)
        spProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnNewProd.setOnClickListener {
            openNewProdDialog()
        }

        btnAdd.setOnClickListener {
            addListProd()
        }
    }

    private fun addListProd() {
        val product = spProduct.selectedItem.toString()
        val amount = etAmount.text.toString()
        val list = sql.getNewestListId()
        val lp = ListProdModel(0, list, product, amount)
        val status = sql.insertListProd(lp)

        if (status > -1){
            Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Insert failed!", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this, ShoppingListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initView() {
        btnNewProd = findViewById(R.id.btnNewProd)
        btnAdd = findViewById(R.id.btnAdd)
        spProduct = findViewById(R.id.spProduct)
        etAmount = findViewById(R.id.etAmount)
    }

    private fun openNewProdDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.new_product, null)
        val etName = dialogLayout.findViewById<EditText>(R.id.etName)
        val etUnit = dialogLayout.findViewById<EditText>(R.id.etUnit)

        with(builder){
            setTitle("New product")
            setPositiveButton("Add"){_, _ ->
                val name = etName.text.toString()
                val unit = etUnit.text.toString()

                val prod = ProductModel(0, name, unit)
                val status = sql.insertProduct(prod)

                if (status > -1){
                    Toast.makeText(this@AddProductActivity, "Product added!", Toast.LENGTH_SHORT).show()
                    val products = sql.getAllProducts("name")
                    spProduct.adapter = ArrayAdapter(this@AddProductActivity, android.R.layout.simple_list_item_1, products)
                    spProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {}
                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }
                } else{
                    Toast.makeText(this@AddProductActivity, "Insert failed!", Toast.LENGTH_SHORT).show()
                }


            }
            setView(dialogLayout)
            show()
        }
    }
}