package com.example.bookrentapp.Book

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookrentapp.R

class BookAdapter(private val books: List<Book>) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.titleTextView.text = book.title
        holder.authorTextView.text = book.author
        holder.priceTextView.text = "₹${book.price}"
        Glide.with(holder.itemView.context).load(book.imgUrl).into(holder.imageView)


        // Handle click on the item to open BookDetailsActivity
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, BookDetails::class.java)
            intent.putExtra("BOOK_ID", book.bookId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = books.size

    class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.book_title)
        val authorTextView: TextView = itemView.findViewById(R.id.book_author)
        val priceTextView: TextView = itemView.findViewById(R.id.book_price)
        val imageView: ImageView = itemView.findViewById(R.id.book_image)
    }
}
