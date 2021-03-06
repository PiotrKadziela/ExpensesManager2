package com.example.expensesmanager2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.example.expensesmanager2.models.ListProdModel
import com.example.expensesmanager2.models.ProductModel
import com.example.expensesmanager2.R
import com.example.expensesmanager2.interfaces.ActivityInterface
import com.example.expensesmanager2.models.ListModel
import com.example.expensesmanager2.utils.SQLiteHelper

class AddProductActivity : AppCompatActivity(), ActivityInterface {
    lateinit var spProduct: Spinner
    lateinit var btnNewProd: Button
    lateinit var btnAdd: Button
    lateinit var etAmount: EditText
    lateinit var txtManageProducts: TextView
    lateinit var txtUnit: TextView
    lateinit var sql: SQLiteHelper

    override fun onResume() {
        super.onResume()
        loadView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)
        sql = SQLiteHelper(this)

        initView()
        loadView()
        setListeners()
    }

    override fun initView() {
        btnNewProd = findViewById(R.id.btnNewProd)
        btnAdd = findViewById(R.id.btnAdd)
        spProduct = findViewById(R.id.spProduct)
        etAmount = findViewById(R.id.etAmount)
        txtManageProducts = findViewById(R.id.txtManageProducts)
        txtUnit = findViewById(R.id.txtUnit)
    }

    override fun loadView() {
        loadProductsSpinner()
        txtUnit.text = if (ProductModel(this).get().size > 0) {
            ProductModel(this).get()[0].unit
        } else {
            ""
        }
    }

    override fun setListeners() {
        setButtonsListeners()
    }

    private fun loadProductsSpinner() {
        val products = ProductModel(this).get()
        val productsNames: MutableList<String> = mutableListOf()
        for (prod in products) {
            productsNames.add(prod.name)
        }
        spProduct.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, productsNames)
        spProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                txtUnit.text =
                    ProductModel(this@AddProductActivity).get(
                        "name = \"${spProduct.selectedItem}\""
                    )[0].unit
            }
        }
    }

    private fun setButtonsListeners() {
        btnNewProd.setOnClickListener {
            openNewProdDialog()
        }
        btnAdd.setOnClickListener {
            addListProd()
        }
        txtManageProducts.setOnClickListener {
            val intent = Intent(this, ManageProductsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun addListProd() {
        val product = ProductModel(this).get("name = \"${spProduct.selectedItem}\"")[0]
        val amount = etAmount.text.toString()
        val list = ListModel(this).getNewestListId()
        val lp = ListProdModel(this, 0, list, product.id, amount.toDouble())
        val status = lp.insert()

        if (status > -1) {
            Toast.makeText(this, "Product added!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Insert failed!", Toast.LENGTH_SHORT).show()
        }

        val intent = Intent(this, ShoppingListActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun openNewProdDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.new_product_dialog, null)
        val etName = dialogLayout.findViewById<EditText>(R.id.etName)
        val etUnit = dialogLayout.findViewById<EditText>(R.id.etUnit)
        with(builder) {
            setTitle("New product")
            setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val unit = etUnit.text.toString()

                regularlyBoughtDialog(name, unit)
            }

            setView(dialogLayout)
            show()
        }
    }

    private fun regularlyBoughtDialog(name: String, unit: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Is the product bought regularly?")
        builder.setCancelable(true)
        builder.setPositiveButton("YES") { dialog, _ ->
            addProduct(name, unit, 1)
            dialog.dismiss()
        }

        builder.setNegativeButton("NO") { dialog, _ ->
            addProduct(name, unit, 0)
            dialog.dismiss()
        }

        val alert = builder.create()
        alert.show()

    }

    private fun addProduct(name: String, unit: String, regular: Int) {
        val prod = ProductModel(this, 0, name, unit, regular)
        val status = prod.insert()

        if (status > -1) {
            Toast.makeText(this@AddProductActivity, "Product added!", Toast.LENGTH_SHORT).show()
            loadView()
        } else {
            Toast.makeText(this@AddProductActivity, "Insert failed!", Toast.LENGTH_SHORT).show()
        }
    }
}