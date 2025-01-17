package com.example.bookrentapp.Vendor

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.content.Intent
import android.widget.Button
import android.widget.ImageView
import com.example.bookrentapp.Login

class VendorProfile : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vendor_profile)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val ownerId=auth.currentUser?.uid

        // Check if ownerId is null and handle accordingly
        if (ownerId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()  // Close current activity
            return  // Exit the method
        }

        val shopName = findViewById<TextView>(R.id.shop_name)
        val ownerName = findViewById<TextView>(R.id.shop_status)

        setFields(ownerId, shopName, ownerName)

        // Logout Button
        val logoutButton = findViewById<TextView>(R.id.logout_text)
        logoutButton.setOnClickListener {
            logout()
        }

        findViewById<ImageView>(R.id.edit_profile).setOnClickListener {
            val intent = Intent(this, CompleteProfile::class.java)
            startActivity(intent)
        }
    }

    private fun setFields(ownerId: String, shopName: TextView, ownerName: TextView) {
        // Use ownerId to fetch the data from the database
        database.child("Shops").child(ownerId).get().addOnSuccessListener {
            if (it.exists()) {
                shopName.text = it.child("shopName").value.toString()
                ownerName.text = it.child("ownerName").value.toString()
            } else {
                Toast.makeText(this, "Shop does not exist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logout() {
        auth.signOut()  // Signs out the user
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}
