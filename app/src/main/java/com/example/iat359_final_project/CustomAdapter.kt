package com.example.iat359_final_project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(
    val list: ArrayList<String>,
    private val db: Database
) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {

    private var listener: OnItemClickListener? = null
    var deleteItemButton: Button? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val results = list[position].split(",")
        holder.sessionTitle.text = results[1]
        holder.locationTextView.text = results[2]
        holder.stepsTextView.text = results[0]
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationTextView: TextView = itemView.findViewById(R.id.locationEntry)
        val stepsTextView: TextView = itemView.findViewById(R.id.stepsEntry)
        val sessionTitle: TextView = itemView.findViewById(R.id.sessionTitleEntry)
        val myLayout: LinearLayout = itemView as LinearLayout

        init {
            deleteItemButton = itemView.findViewById(R.id.delete_button)
            deleteItemButton?.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onDeleteItemClick(position)
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onDeleteItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun deleteItem(position: Int) {
        val item = list[position]
        val results = item.split(",")
        val location = results[0]
        list.removeAt(position)
        notifyItemRemoved(position)
        db.deleteData(location)
    }

    fun updateDataSet(newList: ArrayList<String>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
