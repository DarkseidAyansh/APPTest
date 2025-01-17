package com.example.bookrentapp.Chat

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookrentapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ChatScreen : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var auth = FirebaseAuth.getInstance()
    private lateinit var chatRecyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: ImageButton
    private lateinit var sellerId: String
    private lateinit var bookId: String
    private lateinit var buyerId: String
    private lateinit var  profilePicRef: DatabaseReference
    private val chatMessages = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatMessageAdapter
    private var currentUserId = auth.currentUser?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        database = FirebaseDatabase.getInstance().reference
        chatRecyclerView = findViewById(R.id.chat_recycler_view)
        messageInput = findViewById(R.id.message_input)
        sendButton = findViewById(R.id.send_button)

        // Get the seller ID and book ID from the Intent
        sellerId = intent.getStringExtra("SELLER_ID") ?: ""
        bookId = intent.getStringExtra("BOOK_ID") ?: ""
        buyerId=intent.getStringExtra("BUYER_ID")?:""

        val profileImage: ImageView = findViewById(R.id.seller_profile_image)

        if (sellerId == currentUserId) {
            currentUserId=buyerId
            val sellerNameTextView: TextView = findViewById(R.id.seller_name)
            val tagTextView: TextView = findViewById(R.id.seller_tag)
            fetchUsername(buyerId) { username ->
                sellerNameTextView.text = "$username "
                tagTextView.text="BUYER"
                tagTextView.setBackgroundResource(R.color.message_background_sent)// Show buyer's name
            }
            loadProfileImage(profileImage,buyerId)

        } else {
            // Buyer is chatting, so display the seller's name
            val sellerNameTextView: TextView = findViewById(R.id.seller_name)
            val tagTextView: TextView = findViewById(R.id.seller_tag)
            fetchUsername(sellerId) { username ->
                sellerNameTextView.text = "$username "
                tagTextView.text = "SELLER"
            }
            loadProfileImage(profileImage,sellerId)
        }



        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatAdapter = ChatMessageAdapter(chatMessages, currentUserId)
        chatRecyclerView.adapter = chatAdapter

        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(message)
            }
        }


        listenForMessages()
    }

    private fun sendMessage(message: String) {
        // Use a combination of buyerId (currentUserId) and sellerId for the Firebase path
        val chatPath = "$currentUserId $sellerId"
        val messageId = database.child("chats").child(chatPath).push().key ?: return
        val chatMessage = ChatMessage(message, currentUserId, bookId)

        database.child("chats").child(chatPath).child(messageId).setValue(chatMessage)
            .addOnSuccessListener {
                // Update last message in the chat fragment
                updateLastMessage(sellerId, bookId,message)
                messageInput.text = null
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateLastMessage(sellerId: String, bookId: String, lastMessage: String) {
        val lastMessageData = mapOf("bookId" to bookId, "message" to lastMessage)

        // Update the last message in the chats node for the current user
        val chatSummaryPath = "chats/$currentUserId/$sellerId"
        database.child(chatSummaryPath).setValue(lastMessageData)
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update last message", Toast.LENGTH_SHORT).show()
            }
    }

    private fun listenForMessages() {

        val chatPath = "$currentUserId $sellerId"
        database.child("chats").child(chatPath).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatMessages.clear() // Clear the current list
                for (data in snapshot.children) {
                    val chatMessage = data.getValue(ChatMessage::class.java)
                    chatMessage?.let { chatMessages.add(it) } // Add each message to the list
                }
                chatAdapter= ChatMessageAdapter(chatMessages, currentUserId)
                chatRecyclerView.adapter = chatAdapter
                chatRecyclerView.scrollToPosition(chatMessages.size - 1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatScreen, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchUsername(userId: String, callback: (String) -> Unit) {
        val userRef = database.child("users").child(userId).child("username")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java) ?: userId
                callback(username)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatScreen, "Error fetching username: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(userId) // Fallback to sellerId if there's an error
            }
        })
    }

    private fun loadProfileImage(profileImage: ImageView,id:String) {
        profilePicRef = database.child("users").child(id).child("profile_pic")

        profilePicRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.getValue(String::class.java)
                if (imageUrl != null) {
                    // Use Glide to load the profile image
                    Glide.with(this@ChatScreen).load(imageUrl).into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatScreen, "Error loading profile image: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

}
