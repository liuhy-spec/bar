package com.example.custommenu

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MenuListAdapter(
    private val context: Context,
    private val menus: List<MenuConfig>,
    private val onConfigClick: (MenuConfig) -> Unit,
    private val onEditClick: (MenuConfig) -> Unit,
    private val onRemoveClick: (Int) -> Unit
) : RecyclerView.Adapter<MenuListAdapter.ViewHolder>() {
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMenuId: TextView = itemView.findViewById(R.id.tv_menu_id)
        val tvMenuInfo: TextView = itemView.findViewById(R.id.tv_menu_info)
        val btnConfig: Button = itemView.findViewById(R.id.btn_config)
        val btnEdit: Button = itemView.findViewById(R.id.btn_edit)
        val btnRemove: Button = itemView.findViewById(R.id.btn_remove)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.menu_item, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val menu = menus[position]
        holder.tvMenuId.text = "菜单栏 #${menu.id}"
        holder.tvMenuInfo.text = "位置: (${menu.x}, ${menu.y}) | 尺寸: ${menu.width}x${menu.height} | 置顶: ${menu.isTop} | 应用数: ${menu.appPackageNames.size}"
        
        holder.btnConfig.setOnClickListener {
            onConfigClick(menu)
        }
        
        holder.btnEdit.setOnClickListener {
            onEditClick(menu)
        }
        
        holder.btnRemove.setOnClickListener {
            onRemoveClick(menu.id)
        }
    }
    
    override fun getItemCount(): Int {
        return menus.size
    }
}