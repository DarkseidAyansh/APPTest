package com.example.bookrentapp.Vendor

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth

class FoodAdapter(
    private val foodItems: List<FoodItem>
) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {
    private val auth = FirebaseAuth.getInstance().currentUser?.uid
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodItem = foodItems[position]
        holder.foodName.text = foodItem.name
        if(auth!=foodItem.vendorId){
            holder.addIcon.visibility=View.VISIBLE
        }
    }

    override fun getItemCount(): Int = foodItems.size

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodName: TextView = itemView.findViewById(R.id.foodName)
        val addIcon: ImageView = itemView.findViewById(R.id.addIcon)
    }
}
