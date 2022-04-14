package com.example.expensesmanager2.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.text.isDigitsOnly
import androidx.core.view.isVisible
import com.example.expensesmanager2.R
import com.example.expensesmanager2.interfaces.ActivityInterface
import com.example.expensesmanager2.models.*
import com.example.expensesmanager2.utils.SQLiteHelper
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class AddOperationActivity : AppCompatActivity(), ActivityInterface {
    private lateinit var etTitle: EditText
    private lateinit var etCost: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnEdit: Button
    private lateinit var txtHeader: TextView
    private lateinit var txtError: TextView
    private lateinit var txtDate: TextView
    private lateinit var txtShoppingList: TextView
    private lateinit var txtCurrency: TextView
    private lateinit var spCategory: Spinner
    private lateinit var rgType: RadioGroup
    private lateinit var rbExpense: RadioButton
    private lateinit var rbIncome: RadioButton
    private lateinit var sql: SQLiteHelper
    private var isEdit = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_operation)
        sql = SQLiteHelper(this)

        initView()
        loadView()
        setListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun initView() {
        etTitle = findViewById(R.id.etTitle)
        etCost = findViewById(R.id.etCost)
        btnAdd = findViewById(R.id.btnAdd)
        btnEdit = findViewById(R.id.btnEdit)
        spCategory = findViewById(R.id.spCategory)
        txtHeader = findViewById(R.id.txtHeader)
        txtError = findViewById(R.id.txtError)
        txtShoppingList = findViewById(R.id.txtShoppingList)
        txtDate = findViewById(R.id.txtDate)
        txtCurrency = findViewById(R.id.txtCurrency)
        rgType = findViewById(R.id.rgType)
        rbIncome = findViewById(R.id.rbIncome)
        rbExpense = findViewById(R.id.rbExpense)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun loadView() {
        if (intent.getBooleanExtra("edit", false)) isEdit = true
        if (isEdit) loadEditView() else loadAddView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun setListeners() {
        setRgTypeListeners()
        setEtCostListeners()
        setBtnAddListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBtnAddListeners() {
        btnAdd.setOnClickListener {
            if (etTitle.text.toString().length < 3) {
                txtError.text = "Too short title!"
            } else if (etTitle.text.toString().isDigitsOnly()) {
                txtError.text = "Title cannot be a number!"
            } else if (etCost.text.toString().toDouble() <= 0) {
                txtError.text = "Cost has to be bigger than 0!"
            } else {
                if (isEdit) {
                    updateOperation()
                } else {
                    addOperation()
                }
            }
        }
    }

    private fun setEtCostListeners() {
        etCost.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val costString = etCost.text.toString()
                val splitedCost = costString.split('.')
                val costDecimals = splitedCost.last()
                if (costDecimals.length > 2 && splitedCost.size > 1) {
                    val decimalCost: Double = costString.toDouble()
                    val roundedCost: String = (floor(decimalCost * 100) / 100).toString()
                    etCost.setText(roundedCost)
                    etCost.setSelection(etCost.text.length)
                }
            }
        })
    }

    private fun setRgTypeListeners() {
        val options = ArrayList<String>()
        rgType.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = findViewById(checkedId)

            if (radioButton.id == R.id.rbIncome) {
                spCategory.isEnabled = false
                etCost.hint = "Value"
                options.add("Income")
            } else {
                spCategory.isEnabled = true
                etCost.hint = "Cost"
                for (cat in CategoryModel(this).get("_id != 0")) {
                    options.add(cat.name)
                }
            }

            spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    private fun loadAddView() {
        rbExpense.isChecked = true

        if (intent.getIntExtra("oprList", 0) != 0) {
            rbExpense.isEnabled = false
            rbIncome.isEnabled = false
        }

        btnEdit.isVisible = false
        val date = SimpleDateFormat("MM/dd/yyyy")
        txtDate.text = date.format(System.currentTimeMillis())
        val options = ArrayList<String>()

        for (cat in CategoryModel(this).get("_id != 0")) {
            options.add(cat.name)
        }

        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
    }

    @SuppressLint("SimpleDateFormat")
    private fun loadEditView() {
        if (intent.getIntExtra("oprList", 0) != 0) {
            rbExpense.isEnabled = false
            rbIncome.isEnabled = false
        }

        btnAdd.isVisible = false
        txtHeader.setText(resources.getString(R.string.edit_activity_header))
        setFieldsValues()

        val date = SimpleDateFormat("MM/dd/yyyy")
        txtDate.text = date.format(Date(intent.getLongExtra("oprDate", 0)))
    }

    private fun setFieldsValues() {
        var cost = intent.getDoubleExtra("oprCost", 0.0)
        val type = intent.getIntExtra("oprType", 0)
        val options = ArrayList<String>()

        if (type == 0) {
            cost *= -1
            for (cat in CategoryModel(this).get("_id != 0")) {
                options.add(cat.name)
            }
            rbExpense.isChecked = true
        } else {
            options.add("Income")
            rbIncome.isChecked = true
        }

        etCost.setText(cost.toString())
        spCategory.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, options)
        etTitle.setText(intent.getStringExtra("oprTitle"))
        spCategory.setSelection(options.indexOf(intent.getStringExtra("oprCategory")))
        val list = intent.getIntExtra("oprList", 0)

        if (list != 0) {
            var listString = ""
            listString += "SHOPPING LIST:\n\n"
            val prodList = ListProdModel(this).get("list_id = $list")

            if (prodList.size > 0) {
                for (prod in prodList) {
                    val name = ProductModel(this).get("_id = ${prod.prod_id}")[0].name
                    listString += "- $name ${prod.amount}\n"
                }
            } else {
                listString += "All products from this\nlist have been deleted\nform database"
            }

            txtShoppingList.text = listString
        }

        txtCurrency.text = ConfigModel(this).get("currency")
    }

    private fun updateOperation() {
        val title = etTitle.text.toString()
        var cost = etCost.text.toString().toDouble()
        val id = intent.getIntExtra("oprId", 0)
        val type = if (rgType.checkedRadioButtonId == R.id.rbIncome) 1 else 0
        val date = intent.getLongExtra("oprDate", 0)
        val list = OperationModel(this).get("_id = $id")[0].listId
        val category = when (type) {
            0 -> spCategory.selectedItemPosition + 1
            else -> 0
        }

        if (type == 0 && cost > 0 || type == 1 && cost < 0) {
            cost *= -1
        }

        val opr = OperationModel(this, id, title, cost, category, type, list, date)

        if (opr.update() > -1) {
            Toast.makeText(this, "Operation edited!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Operation edit failed!", Toast.LENGTH_SHORT).show()
        }

        val intentList = Intent(this, OperationsActivity::class.java)
        startActivity(intentList)
        finish()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addOperation() {
        val title = etTitle.text.toString()
        var cost = etCost.text.toString().toDouble()
        var category = spCategory.selectedItemPosition + 1
        val type = if (rgType.checkedRadioButtonId == R.id.rbIncome) 1 else 0
        val date = System.currentTimeMillis()

        if (type == 0) {
            cost *= -1
        } else {
            category = 0
        }

        val list = intent.getIntExtra("oprList", 0)
        val opr = OperationModel(this, 0, title, cost, category, type, list, date)
        val status = opr.insert()

        if (status > -1) {
            Toast.makeText(this, "Operation added!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Insert failed!", Toast.LENGTH_SHORT).show()
        }

        if (list != 0) {
            ListModel(this).startNewList()
            intent.getIntegerArrayListExtra("oprProds")
                ?.let { ListProdModel(this).passUnbuyedProducts(it) }
        }

        val intent = Intent(this, OperationsActivity::class.java)
        startActivity(intent)
        finish()
    }
}