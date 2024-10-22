package com.example.bookrentapp.Book

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookrentapp.Chat.ChatScreen
import com.example.bookrentapp.PaymentScreen
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class BookDetails : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var sellerId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var chatWithSellerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_details)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        chatWithSellerButton = findViewById(R.id.chat_with_seller_button)

        // Retrieve the bookId from the Intent
        val bookId = intent.getStringExtra("BOOK_ID") ?: ""

        findViewById<Button>(R.id.buy_button).setOnClickListener {

            val intent=Intent(this@BookDetails,PaymentScreen::class.java)
            intent.putExtra("BOOK_ID",bookId)
            startActivity(intent)
        }

        // Fetch book details and update the UI
        getBookDetails(bookId)
    }

    private fun getBookDetails(bookId: String) {
        database.child("books").child(bookId).get().addOnSuccessListener {
            if (it.exists()) {
                val title = it.child("title").value.toString()
                val author = it.child("author").value.toString()
                val price = it.child("price").value.toString()
                val imageUrl = it.child("imgUrl").value.toString()
                sellerId = it.child("sellerId").value.toString() // Retrieve sellerId

                // Update UI with book details
                findViewById<TextView>(R.id.book_title).text = title
                findViewById<TextView>(R.id.book_author).text = author
                findViewById<TextView>(R.id.book_price).text = "Rent Price: ₹$price"
                val imageView = findViewById<ImageView>(R.id.book_cover)
                Glide.with(this).load(imageUrl).into(imageView)

                // Now that sellerId is available, check if current user is the seller
                if (sellerId == auth.currentUser?.uid) {
                    // If the current user is the seller, hide the chat button
                    chatWithSellerButton.visibility = Button.GONE
                }

                // Set the click listener for the chat button (after sellerId is fetched)
                chatWithSellerButton.setOnClickListener {
                    // Start ChatScreen and pass the sellerId
                    val intent = Intent(this, ChatScreen::class.java)
                    intent.putExtra("SELLER_ID", sellerId) // Pass the sellerId
                    intent.putExtra("BOOK_ID", bookId)
                    intent.putExtra("BOOK_TITLE", title) // Pass the book title as well
                    startActivity(intent)
                }
            }
        }.addOnFailureListener {
            // Handle errors
            Toast.makeText(this, "Failed to fetch book details", Toast.LENGTH_SHORT).show()
        }
    }
}
