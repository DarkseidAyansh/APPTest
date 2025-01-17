package com.example.bookrentapp.Vendor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookrentapp.R
import com.example.bookrentapp.databinding.ActivityCompleteProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CompleteProfile : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_complete_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        auth=FirebaseAuth.getInstance()
        database= FirebaseDatabase.getInstance().reference

        val ownerId=auth.currentUser?.uid

        val shopNameInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.shop_name_input)
        val ownerNameInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.owner_name_input)
        val shopTypeInput = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.shop_type_input)

        val saveButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.save_button)

        saveButton.setOnClickListener {
            val shopName = shopNameInput.text.toString()
            val ownerName = ownerNameInput.text.toString()
            val shopType = shopTypeInput.text.toString()
            if (ownerId != null) {
                uploadToDatabase(shopName, ownerName, shopType, ownerId)
            }else{
                Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun uploadToDatabase(shopName: String, ownerName: String, shopType: String, ownerId: String) {
        if (shopName.isNotEmpty() && ownerName.isNotEmpty() && shopType.isNotEmpty() && ownerId.isNotEmpty()) {

            val shop = mapOf(
                "shopName" to shopName,
                "ownerName" to ownerName,
                "shopType" to shopType,
                "ownerId" to ownerId,
                "status" to "Closed"
            )

            val database = FirebaseDatabase.getInstance()
            val shopRef = database.reference.child("Shops").child(ownerId)

            shopRef.setValue(shop).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile uploaded successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {

                    Toast.makeText(this, "Failed to upload profile", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
        }
    }

}