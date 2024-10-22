package com.example.bookrentapp.Dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R

class UserDashboardFragment : Fragment() {

    private lateinit var organizationRecyclerView: RecyclerView
    private lateinit var organizationAdapter: OrganizationAdapter
    private lateinit var organizationList: MutableList<Organization>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_dashboard, container, false)

        organizationRecyclerView = view.findViewById(R.id.organization_recycler_view)
        organizationRecyclerView.layoutManager = LinearLayoutManager(context)

        // Sample data
        organizationList = mutableListOf(
            Organization("Food Shop", "Fresh groceries and more"),
            Organization("General Store", "Daily essentials at your service"),
            Organization("Merchandise Co.", "Quality products at great prices")
        )

        organizationAdapter = OrganizationAdapter(organizationList)
        organizationRecyclerView.adapter = organizationAdapter

        return view
    }
}



