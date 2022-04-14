package com.example.expensesmanager2.adapters

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager2.models.OperationModel
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.CategoryModel
import com.example.expensesmanager2.models.ConfigModel
import com.example.expensesmanager2.utils.SQLiteHelper

class OperationAdapter : RecyclerView.Adapter<OperationAdapter.OperationViewHolder>() {
    private var oprList: ArrayList<OperationModel> = ArrayList()
    private var onClickItem: ((OperationModel) -> Unit)? = null
    private var onClickDeleteItem: ((OperationModel) -> Unit)? = null

    fun addItems(items: ArrayList<OperationModel>) {
        this.oprList = items
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (OperationModel) -> Unit) {
        this.onClickItem = callback
    }

    fun setOnClickDeleteItem(callback: (OperationModel) -> Unit) {
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

    class OperationViewHolder(var view: View) : RecyclerView.ViewHolder(view) {
        var title = view.findViewById<TextView>(R.id.txtTitle)
        var cost = view.findViewById<TextView>(R.id.txtCost)
        var category = view.findViewById<TextView>(R.id.txtCategory)
        var btnDelete = view.findViewById<Button>(R.id.btnDelete)
        val sql = SQLiteHelper(view.context)

        @SuppressLint("SetTextI18n")
        fun bindView(opr: OperationModel) {
            title.text = opr.title

            val string = ConfigModel(view.context).get("balance").split('.')
            cost.text = if (string[1].length > 1)
                opr.cost.toString() + " " + ConfigModel(view.context).get("currency") else
                opr.cost.toString() + "0" + " " + ConfigModel(view.context).get("currency")
            if (opr.cost.toInt() < 0) {
                cost.setTextColor(Color.RED)
            } else {
                cost.setTextColor(Color.GREEN)
            }
            category.text = CategoryModel(view.context, opr.category).name
        }
    }
}