package com.example.expensesmanager2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager2.interfaces.ListProdListener
import com.example.expensesmanager2.models.ListProdModel
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.ProductModel

class ListProdAdapter(
    val context: Context,
    val arrayList: ArrayList<Int>,
    val listProdListener: ListProdListener
) : RecyclerView.Adapter<ListProdAdapter.ListProdViewHolder>() {
    private var prodList: ArrayList<ListProdModel> = ArrayList()
    private var onClickItem: ((ListProdModel) -> Unit)? = null
    private var onClickDeleteItem: ((ListProdModel) -> Unit)? = null
    var arrayList0 = ArrayList<Int>()

    fun setOnClickDeleteItem(callback: (ListProdModel) -> Unit) {
        this.onClickDeleteItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ListProdViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_item_list_prod, parent, false)
    )

    override fun onBindViewHolder(holder: ListProdViewHolder, position: Int) {
        val prod = prodList[position]
        holder.bindView(prod)
        holder.itemView.setOnClickListener { onClickItem?.invoke(prod) }
        holder.btnDelete.setOnClickListener { onClickDeleteItem?.invoke(prod) }
        holder.cbBought.setOnClickListener {
            if (arrayList.size > 0) {
                if (holder.cbBought.isChecked) {
                    arrayList0.add(arrayList[position])
                } else {
                    arrayList0.remove(arrayList[position])
                }
                listProdListener.onListProdChange(arrayList0)
            }
        }
    }

    override fun getItemCount(): Int {
        return prodList.size
    }

    fun addItems(prodList: ArrayList<ListProdModel>) {
        this.prodList = prodList
    }

    class ListProdViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var name = view.findViewById<TextView>(R.id.txtName)
        var amount = view.findViewById<TextView>(R.id.txtAmount)
        var btnDelete = view.findViewById<Button>(R.id.btnDelete)
        var cbBought = view.findViewById<CheckBox>(R.id.cbBought)


        fun bindView(prod: ListProdModel) {
            name.text = ProductModel(view.context).get("_id = ${prod.prod_id}")[0].name
            amount.text = prod.amount.toString()
        }
    }
}