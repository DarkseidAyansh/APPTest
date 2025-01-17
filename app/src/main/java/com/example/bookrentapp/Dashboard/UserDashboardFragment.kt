package com.example.bookrentapp.Dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDashboardFragment : Fragment() {

    private lateinit var organizationRecyclerView: RecyclerView
    private lateinit var organizationAdapter: OrganizationAdapter
    private val organizationList= mutableListOf<Organization>()

    private lateinit var database: DatabaseReference

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_dashboard, container, false)

        organizationRecyclerView = view.findViewById(R.id.organization_recycler_view)
        organizationRecyclerView.layoutManager = LinearLayoutManager(context)
        organizationAdapter = OrganizationAdapter(organizationList)
        organizationRecyclerView.adapter = organizationAdapter

        database = FirebaseDatabase.getInstance().reference

        database.child("Shops").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                organizationList.clear()
                for (data in snapshot.children) {
                    val organization = data.getValue(Organization::class.java)
                    if (organization != null) {
                        organizationList.add(0,organization)
                    }
                }
                organizationAdapter = OrganizationAdapter(organizationList)
                organizationRecyclerView.adapter = organizationAdapter
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

        return view
    }
}



