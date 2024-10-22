package com.example.bookrentapp.Chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth


class ChatMessageAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String// Pass current user's ID to distinguish messages
) : RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder>() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.chat_message_item, parent, false)
        return ChatMessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser?.uid.toString()

        val message = messages[position]
        holder.messageTextView.text = "${message.message}"

        // Determine message alignment based on senderId
        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        if (currentUserId == currentUser) {
            // Message from user (right aligned)
            params.marginEnd = 0
            params.marginStart = 80 // adjust as needed
        } else {
            // Message from seller or other users (left aligned)
            params.marginEnd = 80 // adjust as needed
            params.marginStart = 0
        }
        holder.itemView.layoutParams = params
    }

    override fun getItemCount(): Int = messages.size

    class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.chat_message)
    }
}
