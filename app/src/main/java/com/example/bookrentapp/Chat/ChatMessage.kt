package com.example.bookrentapp.Chat

data class ChatMessage(
    val message: String = "",
    val senderId: String = "",
    val bookId: String = "" // Include bookId to identify which book the message refers to
)
