package com.example.bookrentapp.Chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatSummaryAdapter(
    private val chatSummaries: List<ChatSummary>,
) : RecyclerView.Adapter<ChatSummaryAdapter.ChatSummaryViewHolder>() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    class ChatSummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sellerNameTextView: TextView = itemView.findViewById(R.id.seller_name)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.last_message)
        val tagTextView: TextView = itemView.findViewById(R.id.seller_tag)
        val profileImageView: ImageView = itemView.findViewById(R.id.seller_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_summary, parent, false)
        return ChatSummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatSummaryViewHolder, position: Int) {
        val chatSummary = chatSummaries[position]
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        val currentUserId = auth.currentUser?.uid.toString()

        // Display the correct username and profile image
        if (chatSummary.sellerId != currentUserId) {
            fetchUsername(chatSummary.sellerId) { username ->
                holder.sellerNameTextView.text = "$username "
                loadProfileImage(holder.profileImageView, chatSummary.sellerId, holder.itemView.context)
            }
        } else {
            fetchUsername(chatSummary.buyerId) { username ->
                holder.sellerNameTextView.text = "$username "
                holder.tagTextView.text = "BUYER"
                holder.tagTextView.setBackgroundResource(R.color.message_background_sent)
                loadProfileImage(holder.profileImageView, chatSummary.buyerId, holder.itemView.context)
            }
        }

        holder.lastMessageTextView.text = chatSummary.lastMessage

        // Navigate to the chat screen when an item is clicked
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatScreen::class.java)
            intent.putExtra("SELLER_ID", chatSummary.sellerId)
            intent.putExtra("BUYER_ID", chatSummary.buyerId)
            intent.putExtra("BOOK_ID", chatSummary.bookId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return chatSummaries.size
    }

    private fun fetchUsername(userId: String, callback: (String) -> Unit) {
        val userRef = database.child("users").child(userId).child("username")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java) ?: userId
                callback(username)
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun loadProfileImage(profileImage: ImageView, userId: String, context: android.content.Context) {
        val profilePicRef = database.child("users").child(userId).child("profile_pic")
        profilePicRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.getValue(String::class.java)
                if (imageUrl != null) {
                    // Load the profile image using Glide
                    Glide.with(context)
                        .load(imageUrl)
                        .into(profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error loading profile image: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
