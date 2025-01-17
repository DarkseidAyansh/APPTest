package com.example.bookrentapp.Vendor

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddFoodActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var editTextName: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextCategory: EditText
    private lateinit var editTextCookTime: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_food)

        // Initialize Firebase authentication and database reference
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Initialize views
        editTextName = findViewById(R.id.editTextName)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextPrice = findViewById(R.id.editTextPrice)
        editTextCategory = findViewById(R.id.editTextCategory)
        editTextCookTime = findViewById(R.id.editTextCookTime)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            val name = editTextName.text.toString()
            val description = editTextDescription.text.toString()
            val priceString = editTextPrice.text.toString()
            val category = editTextCategory.text.toString()
            val cookTimeString = editTextCookTime.text.toString()

            // Validate input fields
            if (name.isEmpty() || description.isEmpty() || priceString.isEmpty() || category.isEmpty() || cookTimeString.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Get the current user ID from Firebase Authentication
            val currentUserId = auth.currentUser?.uid

            if (currentUserId != null) {
                val foodItemId = database.child("FoodItems").child(currentUserId).push().key // Generate a unique key for each food item
                val foodItem = FoodItem(
                    name = name,
                    description = description,
                    price = priceString,
                    category = category,
                    cookTime = cookTimeString,
                    vendorId = currentUserId
                )

                // Save food item to Firebase
                if (foodItemId != null) {
                    database.child("FoodItems").child(currentUserId).child(foodItemId).setValue(foodItem)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Food item added successfully", Toast.LENGTH_SHORT).show()
                            finish() // Close the activity
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add food item", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
