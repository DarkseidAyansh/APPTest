package com.example.bookrentapp.Dashboard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R
import com.example.bookrentapp.Vendor.AddFoodActivity
import com.example.bookrentapp.Vendor.FoodAdapter
import com.example.bookrentapp.Vendor.FoodItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ItemFoodsActivity : AppCompatActivity() {
    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var foodItemList: MutableList<FoodItem>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var vendorId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_foods)

        vendorId = intent.getStringExtra("ORGANIZATION_ID").toString()

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        foodRecyclerView = findViewById(R.id.foodRecyclerView)
        foodRecyclerView.layoutManager = LinearLayoutManager(this)

        foodItemList = mutableListOf()
        fetchFoodItemsFromDatabase()

        foodRecyclerView.adapter = FoodAdapter(foodItemList)
    }

    private fun fetchFoodItemsFromDatabase() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            val foodItemsRef = FirebaseDatabase.getInstance().getReference("FoodItems").child(vendorId)

            foodItemsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    foodItemList.clear()
                    Log.d("ItemFoodsActivity", "DataSnapshot received: ${snapshot.childrenCount} items")

                    if (snapshot.exists()) {
                        for (foodSnapshot in snapshot.children) {
                            try {
                                val foodItem = foodSnapshot.getValue(FoodItem::class.java)
                                if (foodItem != null) {
                                    foodItemList.add(foodItem)
                                    Log.d("ItemFoodsActivity", "Food item added: ${foodItem.name}")
                                }
                            } catch (e: Exception) {
                                Log.e("ItemFoodsActivity", "Error parsing food item: ${e.message}")
                            }
                        }
                        foodRecyclerView.adapter= FoodAdapter(foodItemList)
                    } else {
                        Log.d("ItemFoodsActivity", "No food items found")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ItemFoodsActivity, "Failed to load food items", Toast.LENGTH_SHORT).show()
                    Log.e("ItemFoodsActivity", "DatabaseError: ${error.message}")
                }
            })
        } else {
            Log.w("ItemFoodsActivity", "User is not authenticated")
        }
    }
}