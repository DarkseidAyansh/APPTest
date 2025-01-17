package com.example.bookrentapp.Chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import pl.droidsonroids.gif.GifImageView

class ChatFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private var auth = FirebaseAuth.getInstance()
    private lateinit var chatSummaryRecyclerView: RecyclerView
    private lateinit var chatSummaryAdapter: ChatSummaryAdapter
    private val chatSummaries = mutableListOf<ChatSummary>()
    private var currentUserId = auth.currentUser?.uid.toString()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        chatSummaryRecyclerView = view.findViewById(R.id.chats_recycler_view)
        chatSummaryRecyclerView.layoutManager = LinearLayoutManager(context)
        chatSummaryAdapter = ChatSummaryAdapter(chatSummaries)
        chatSummaryRecyclerView.adapter = chatSummaryAdapter

        database = FirebaseDatabase.getInstance().reference

        // Load chat summaries
        val loadingGif = view.findViewById<ImageView>(R.id.loading_gif)
        loadingGif.visibility = View.VISIBLE
        loadChatSummaries()
        loadingGif.visibility = View.GONE

        return view
    }

    private fun loadChatSummaries() {
        val sellerChatSummaries = mutableListOf<ChatSummary>()
        val buyerChatSummaries = mutableListOf<ChatSummary>()
        chatSummaries.clear() // Clear global chat summaries before reloading

        // Load seller chat summaries
        database.child("chats").child(currentUserId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                sellerChatSummaries.clear()
                for (data in snapshot.children) {
                    val sellerId = data.key ?: ""
                    val lastMessageData = data.child("message").getValue(String::class.java) ?: ""
                    val bookId = data.child("bookId").getValue(String::class.java) ?: ""

                    val chatSummary = ChatSummary(sellerId, currentUserId, bookId, lastMessageData)
                    sellerChatSummaries.add(0,chatSummary)
                }

                // Merge with buyer summaries
                updateGlobalChatSummaries(sellerChatSummaries, buyerChatSummaries)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Load buyer chat summaries
        database.child("chats").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                buyerChatSummaries.clear()
                for (chatSessionSnapshot in snapshot.children) {
                    if (chatSessionSnapshot.key == currentUserId) continue
                    for (userSnapshot in chatSessionSnapshot.children) {
                        val userId = userSnapshot.key ?: ""
                        if (userId == currentUserId) {
                            val lastMessageData = userSnapshot.child("message").getValue(String::class.java) ?: ""
                            val bookId = userSnapshot.child("bookId").getValue(String::class.java) ?: ""

                            val chatSummary = ChatSummary(userId, chatSessionSnapshot.key ?: "", bookId, lastMessageData)
                            buyerChatSummaries.add(0,chatSummary)
                        }
                    }
                }

                // Merge with seller summaries
                updateGlobalChatSummaries(sellerChatSummaries, buyerChatSummaries)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Helper function to update global chat summaries and refresh the adapter
    private fun updateGlobalChatSummaries(
        sellerChatSummaries: List<ChatSummary>,
        buyerChatSummaries: List<ChatSummary>
    ) {
        chatSummaries.clear() // Clear global chat summaries
        chatSummaries.addAll(sellerChatSummaries)
        chatSummaries.addAll(buyerChatSummaries)

        // Notify adapter of the updated data
        chatSummaryRecyclerView.adapter=ChatSummaryAdapter(chatSummaries)
    }


}