package com.example.expensesmanager2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(){
    private var prodList: ArrayList<ProductModel> = ArrayList()
    private var onClickItem: ((ProductModel) -> Unit)? = null
    private var onClickDeleteItem: ((ProductModel) -> Unit)? = null

    fun addItems(items: ArrayList<ProductModel>){
        this.prodList = items
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (ProductModel) -> Unit){
        this.onClickItem = callback
    }

    fun setOnClickDeleteItem(callback: (ProductModel) -> Unit){
        this.onClickDeleteItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProductViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_item_prod, parent, false)
    )

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val prod = prodList[position]
        holder.bindView(prod)
        holder.itemView.setOnClickListener { onClickItem?.invoke(prod) }
        holder.delete.setOnClickListener { onClickDeleteItem?.invoke(prod) }
    }

    override fun getItemCount(): Int {
        return prodList.size
    }

    class ProductViewHolder(var view: View) : RecyclerView.ViewHolder(view){
        var name = view.findViewById<TextView>(R.id.txtName)
        var unit = view.findViewById<TextView>(R.id.txtUnit)
        var regular = view.findViewById<TextView>(R.id.txtRegular)
        var delete = view.findViewById<TextView>(R.id.txtDelete)

        fun bindView(prod: ProductModel){
            name.text = prod.name
            unit.text = prod.unit
            if(prod.isBoughtRegularly == 0){
                regular.text = "NO"
            } else{
                regular.text = "YES"
            }
        }
    }
}