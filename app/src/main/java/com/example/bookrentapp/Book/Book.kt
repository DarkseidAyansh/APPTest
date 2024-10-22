package com.example.bookrentapp.Book

data class Book(
    val bookId: String = "",   // Unique ID for each book
    val title: String = "",
    val author: String = "",
    val price: String = "",
    val sellerId: String = "",
    var imgUrl: String = "",  // Change to var to allow updates
    val category: String = ""    // Add this line for the category
)


