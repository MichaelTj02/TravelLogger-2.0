package com.example.iat359_final_project.feature.logs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.iat359_final_project.R
import com.example.iat359_final_project.domain.model.LogEntry

class CustomAdapter(
    private val list: MutableList<LogEntry>
) : RecyclerView.Adapter<CustomAdapter.MyViewHolder>() {

    private var listener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.log_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = list[position]
        holder.sessionTitle.text = item.sessionTitle
        holder.locationTextView.text = item.location
        holder.stepsTextView.text = item.steps
    }

    override fun getItemCount(): Int = list.size

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val locationTextView: TextView = itemView.findViewById(R.id.locationEntry)
        val stepsTextView: TextView = itemView.findViewById(R.id.stepsEntry)
        val sessionTitle: TextView = itemView.findViewById(R.id.sessionTitleEntry)
        private val deleteItemButton: Button = itemView.findViewById(R.id.delete_button)

        init {
            deleteItemButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener?.onDeleteItemClick(list[position])
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onDeleteItemClick(item: LogEntry)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateDataSet(newList: List<LogEntry>) {
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }
}
