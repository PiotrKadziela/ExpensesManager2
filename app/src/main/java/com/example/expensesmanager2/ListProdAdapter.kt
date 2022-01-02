package com.example.expensesmanager2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ListProdAdapter : RecyclerView.Adapter<ListProdAdapter.ListProdViewHolder>() {
    private var prodList: ArrayList<ListProdModel> = ArrayList()
    private var onClickItem: ((ListProdModel) -> Unit)? = null
    private var onClickDeleteItem: ((ListProdModel) -> Unit)? = null

    fun setOnClickItem(callback: (ListProdModel) -> Unit){
        this.onClickItem = callback
    }

    fun setOnClickDeleteItem(callback: (ListProdModel) -> Unit){
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
    }

    override fun getItemCount(): Int {
        return prodList.size
    }

    fun addItems(prodList: ArrayList<ListProdModel>) {
        this.prodList = prodList
        notifyDataSetChanged()
    }

    class ListProdViewHolder(var view: View): RecyclerView.ViewHolder(view){
        var name = view.findViewById<TextView>(R.id.txtName)
        var amount = view.findViewById<TextView>(R.id.txtAmount)
        var btnDelete = view.findViewById<Button>(R.id.btnDelete)


        fun bindView(prod: ListProdModel){
            name.text = prod.name
            amount.text = prod.amount
        }
    }
}