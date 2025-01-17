package com.example.bookrentapp.Vendor

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ItemsProvided : AppCompatActivity() {

    private lateinit var foodRecyclerView: RecyclerView
    private lateinit var foodItemList: MutableList<FoodItem>
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_provided)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val intent = Intent(this, AddFoodActivity::class.java)
            startActivity(intent)
        }

        foodRecyclerView = findViewById(R.id.foodRecyclerView)
        foodRecyclerView.layoutManager = LinearLayoutManager(this)

        foodItemList = mutableListOf()
        fetchFoodItemsFromDatabase()

        foodRecyclerView.adapter = FoodAdapter(foodItemList)
    }

    private fun fetchFoodItemsFromDatabase() {
        val currentUserId = auth.currentUser?.uid

        if (currentUserId != null) {
            val foodItemsRef = FirebaseDatabase.getInstance().getReference("FoodItems").child(currentUserId)

            foodItemsRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    foodItemList.clear()

                    for (foodSnapshot in snapshot.children) {
                        try {
                            val foodItem = foodSnapshot.getValue(FoodItem::class.java)
                            if (foodItem != null) {
                                foodItemList.add(foodItem)
                            }
                        } catch (e: Exception) {
                        }
                    }

                    foodRecyclerView.adapter = FoodAdapter(foodItemList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@ItemsProvided, "Failed to load food items", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
