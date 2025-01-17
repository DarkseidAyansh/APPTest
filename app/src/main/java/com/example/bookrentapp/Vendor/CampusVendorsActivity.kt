package com.example.bookrentapp.Vendor

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.bookrentapp.Login
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CampusVendorsActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_campus_vendors)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.items_provided).setOnClickListener {
            val intent = Intent(this, ItemsProvided::class.java)
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val ownerId = auth.currentUser?.uid

        if (ownerId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return
        }

        val shopName = findViewById<TextView>(R.id.shop_name)
        val switch1 = findViewById<Switch>(R.id.switch1)

        database.child("Shops").child(ownerId).get().addOnSuccessListener {
            if (it.exists()) {
                shopName.text = it.child("shopName").value.toString()
                val status = it.child("status").value.toString()
                switch1.isChecked = status == "Open" // Set switch state based on the "status" value
                updateSwitchColors(switch1.isChecked) // Update colors based on the initial state
            } else {
                Toast.makeText(this, "Shop does not exist", Toast.LENGTH_SHORT).show()
            }
        }

        switch1.setOnCheckedChangeListener { _, isChecked ->
            val newStatus = if (isChecked) "Open" else "Close"

            database.child("Shops").child(ownerId).child("status").setValue(newStatus)
                .addOnSuccessListener {
                    Toast.makeText(this, "Shop is $newStatus", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show()
                }

            switch1.text = newStatus
            updateSwitchColors(isChecked)
        }

        findViewById<ImageView>(R.id.profile_icon).setOnClickListener {
            val intent = Intent(this, VendorProfile::class.java)
            startActivity(intent)
        }
    }


    private fun updateSwitchColors(isChecked: Boolean) {
        val textColor = if (isChecked) Color.parseColor("#00FF00") else Color.parseColor("#FF0000")
        val thumbColor = ColorStateList.valueOf(if (isChecked) Color.parseColor("#00FF00") else Color.parseColor("#FF0000"))
        val trackColor = ColorStateList.valueOf(if (isChecked) Color.parseColor("#90EE90") else Color.parseColor("#FF7F7F"))

        val switch1 = findViewById<Switch>(R.id.switch1)
        switch1.setTextColor(textColor)
        switch1.thumbTintList = thumbColor
        switch1.trackTintList = trackColor
    }
}
