package com.example.bookrentapp.Dashboard

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R

class OrganizationAdapter(private val organizationList: List<Organization>) : RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder>() {

    private var sortedOrganizationList: List<Organization> = organizationList.sortedBy { it.status != "Open" }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_organization, parent, false)
        return OrganizationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrganizationViewHolder, position: Int) {
        val organization = sortedOrganizationList[position]
        holder.shopName.text = organization.shopName
        holder.shopType.text = organization.shopType
        holder.status.text = organization.status

        if (holder.status.text == "Open") {
            holder.status.setTextColor(Color.parseColor("#00FF00"))  // Green for Open
        } else {
            holder.status.setTextColor(Color.parseColor("#FF0000"))  // Red for Closed
            // Apply a dull effect (change alpha for dimming)
            holder.itemView.alpha = 0.5f
            holder.itemView.isClickable = false
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ItemFoodsActivity::class.java)
            intent.putExtra("ORGANIZATION_ID", organization.ownerId)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int = sortedOrganizationList.size

    class OrganizationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shopName: TextView = itemView.findViewById(R.id.org_name)
        val shopType: TextView = itemView.findViewById(R.id.org_description)
        val status: TextView = itemView.findViewById(R.id.org_status)
    }
}
