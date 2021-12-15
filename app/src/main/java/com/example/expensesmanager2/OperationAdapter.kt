package com.example.expensesmanager2

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ScrollCaptureCallback
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OperationAdapter : RecyclerView.Adapter<OperationAdapter.OperationViewHolder>() {
    private var oprList: ArrayList<OperationModel> = ArrayList()
    private var onClickItem: ((OperationModel) -> Unit)? = null
    private var onClickDeleteItem: ((OperationModel) -> Unit)? = null

    fun addItems(items: ArrayList<OperationModel>){
        this.oprList = items
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (OperationModel) -> Unit){
        this.onClickItem = callback
    }

    fun setOnClickDeleteItem(callback: (OperationModel) -> Unit){
        this.onClickDeleteItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OperationViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_items_opr, parent, false)
    )

    override fun onBindViewHolder(holder: OperationViewHolder, position: Int) {
        val opr = oprList[position]
        holder.bindView(opr)
        holder.itemView.setOnClickListener { onClickItem?.invoke(opr) }
        holder.btnDelete.setOnClickListener { onClickDeleteItem?.invoke(opr) }
    }

    override fun getItemCount(): Int {
        return oprList.size
    }

    class OperationViewHolder(var view: View): RecyclerView.ViewHolder(view){
        var title = view.findViewById<TextView>(R.id.txtTitle)
        var cost = view.findViewById<TextView>(R.id.txtCost)
        var category = view.findViewById<TextView>(R.id.txtCategory)
        var btnDelete = view.findViewById<Button>(R.id.btnDelete)

        fun bindView(opr: OperationModel){
            title.text = opr.title
            cost.text = opr.cost.toString()
            if(opr.cost.toInt() < 0){
                cost.setTextColor(Color.RED)
            } else{
                cost.setTextColor(Color.GREEN)
            }
            category.text = opr.category
        }
    }
}