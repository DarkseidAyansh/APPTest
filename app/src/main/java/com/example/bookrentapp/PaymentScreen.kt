package com.example.bookrentapp

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text

class PaymentScreen : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_payment_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database=FirebaseDatabase.getInstance().reference

        val bookId = intent.getStringExtra("BOOK_ID") ?: ""

        database.child("books").child(bookId).get().addOnSuccessListener {
            if (it.exists()) {
                val title = it.child("title").value.toString()
                val author = it.child("author").value.toString()
                val price = it.child("price").value.toString()

                findViewById<TextView>(R.id.book_title_payment).text=title
                findViewById<TextView>(R.id.book_price_payment).text="Amount to pay: ₹$price"

            }
        }
    }
}