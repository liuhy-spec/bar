package com.example.custommenu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppListAdapter(
    private val context: Context,
    apps: List<AppInfo>,
    private val selectedPackages: List<String>,
    private val onItemClick: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.ViewHolder>() {
    
    private var apps: MutableList<AppInfo> = apps.toMutableList()
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.iv_app_icon)
        val tvName: TextView = itemView.findViewById(R.id.tv_app_name)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cb_select)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.app_item, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.tvName.text = app.appName
        holder.ivIcon.setImageDrawable(app.icon)
        holder.cbSelect.isChecked = selectedPackages.contains(app.packageName)
        
        holder.itemView.setOnClickListener {
            val isChecked = !holder.cbSelect.isChecked
            holder.cbSelect.isChecked = isChecked
            onItemClick(app, isChecked)
        }
        
        holder.cbSelect.setOnClickListener {
            onItemClick(app, holder.cbSelect.isChecked)
        }
    }
    
    override fun getItemCount(): Int {
        return apps.size
    }
    
    fun updateApps(newApps: List<AppInfo>) {
        apps.clear()
        apps.addAll(newApps)
        notifyDataSetChanged()
    }
}