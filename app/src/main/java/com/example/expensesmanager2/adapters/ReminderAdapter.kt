package com.example.expensesmanager2.adapters

import android.annotation.SuppressLint
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.expensesmanager2.R
import com.example.expensesmanager2.models.ReminderModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ReminderAdapter : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {
    private var rmdList: ArrayList<ReminderModel> = ArrayList()
    private var onClickItem: ((ReminderModel) -> Unit)? = null
    private var onClickDeleteItem: ((ReminderModel) -> Unit)? = null

    fun addItems(items: ArrayList<ReminderModel>) {
        this.rmdList = items
        notifyDataSetChanged()
    }

    fun setOnClickDeleteItem(callback: (ReminderModel) -> Unit) {
        this.onClickDeleteItem = callback
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ReminderViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.card_item_reminder, parent, false)
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val prod = rmdList[position]
        holder.bindView(prod)
        holder.itemView.setOnClickListener { onClickItem?.invoke(prod) }
        holder.delete.setOnClickListener { onClickDeleteItem?.invoke(prod) }
    }

    override fun getItemCount(): Int {
        return rmdList.size
    }

    class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.tvTitle)
        val period = view.findViewById<TextView>(R.id.tvPeriod)
        val next = view.findViewById<TextView>(R.id.tvNext)
        var delete = view.findViewById<TextView>(R.id.tvDelete)

        @SuppressLint("SimpleDateFormat")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bindView(rmd: ReminderModel) {
            title.text = rmd.title
            period.text = when (rmd.period_id) {
                0 -> "Weekly"
                1 -> "Every 2 weeks"
                2 -> "Monthly"
                3 -> "Every 2 months"
                4 -> "Every 3 months"
                else -> "Once"
            }
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm")
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = rmd.time
            next.text = if (rmd.type == 0)
                "Next: " + formatter.format(calendar.time) else formatter.format(calendar.time)
        }
    }
}