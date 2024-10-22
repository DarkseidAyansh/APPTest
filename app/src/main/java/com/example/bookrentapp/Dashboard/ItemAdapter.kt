package com.example.bookrentapp.Dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R

class OrganizationAdapter(private val organizationList: List<Organization>) :
    RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrganizationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_organization, parent, false)
        return OrganizationViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrganizationViewHolder, position: Int) {
        val organization = organizationList[position]
        holder.bind(organization)
    }

    override fun getItemCount(): Int = organizationList.size

    inner class OrganizationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val orgName: TextView = itemView.findViewById(R.id.org_name)
        private val orgDescription: TextView = itemView.findViewById(R.id.org_description)

        fun bind(organization: Organization) {
            orgName.text = organization.name
            orgDescription.text = organization.description
        }
    }
}
