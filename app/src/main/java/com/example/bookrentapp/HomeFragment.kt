package com.example.bookrentapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookrentapp.Book.Book
import com.example.bookrentapp.Book.BookAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeFragment : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private val books = mutableListOf<Book>()

    private lateinit var navDrawerIcon: ImageView
    private lateinit var drawerLayout: DrawerLayout

    private lateinit var searchView: SearchView
    private lateinit var nothingFoundTextView: TextView
    private val searchList = mutableListOf<Book>()

    private var selectedCategory: String? = null
    private lateinit var usernameTextView: TextView

    private var auth = FirebaseAuth.getInstance()
    private var currentUserId = auth.currentUser?.uid.toString()

    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance().reference

        recyclerView = view.findViewById(R.id.book_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        bookAdapter = BookAdapter(books)
        recyclerView.adapter = bookAdapter

        nothingFoundTextView = view.findViewById(R.id.nothing)
        nothingFoundTextView.visibility = View.GONE

        // Check for the selected category from arguments
        selectedCategory = arguments?.getString("SELECTED_CATEGORY")
        selectedCategory?.let {
            updateCategoryBackground(it)
        }

        searchView = view.findViewById(R.id.search)
        setupSearchView()

        navDrawerIcon = view.findViewById(R.id.nav_drawer_icon)
        drawerLayout = requireActivity().findViewById(R.id.drawer_layout)

        navDrawerIcon.setOnClickListener {
            drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        usernameTextView = view.findViewById(R.id.username_home)

        fetchUsername(currentUserId) { username ->
            usernameTextView.text = username
        }

        progressBar = view.findViewById(R.id.progressBar)

        loadBooks()

        // Setup category click listeners (Assuming you have TextViews or Buttons for categories)
        setupCategoryListeners(view)
    }

    private fun updateCategoryBackground(it: String) {

        val categoryItems = mapOf(
            "Book" to view?.findViewById<ImageView>(R.id.books),
            "Cooler" to view?.findViewById<ImageView>(R.id.cooler),
            "Cycle" to view?.findViewById<ImageView>(R.id.cycle),
            "Laptop" to view?.findViewById<ImageView>(R.id.laptop),
            "Furniture" to view?.findViewById<ImageView>(R.id.furniture)
        )

        // Set the background of the selected category
        categoryItems[selectedCategory]?.setBackgroundColor(0xFF3DDC84.toInt())

    }

    private fun setupCategoryListeners(view: View) {
        // List of categories with their corresponding ImageView IDs
        val categoryItems = listOf(
            Pair(view.findViewById(R.id.books), "Book"),
            Pair(view.findViewById(R.id.cooler), "Cooler"),
            Pair(view.findViewById(R.id.cycle), "Cycle"),
            Pair(view.findViewById<ImageView>(R.id.laptop), "Laptop"),
            Pair(view.findViewById(R.id.furniture), "Furniture")
        )

        // Setting click listeners for each ImageView
        categoryItems.forEach { (imageView, category) ->
            imageView.setOnClickListener {
                navigateToHomeFragmentWithCategory(category)
            }
        }
    }

    private fun navigateToHomeFragmentWithCategory(selectedCategory: String) {
        val homeFragment = HomeFragment().apply {
            arguments = Bundle().apply {
                putString("SELECTED_CATEGORY", selectedCategory)
            }
        }

        parentFragmentManager.popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .addToBackStack("HomeFragment")
            .commit()
    }


    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val searchText = newText?.lowercase() ?: ""
                if (searchText.isNotEmpty()) {
                    hideMainViewElements()
                    searchList.clear()
                    books.forEach {
                        if (it.author.lowercase().contains(searchText) ||
                            it.title.lowercase().contains(searchText) ||
                            it.price.lowercase().contains(searchText)) {
                            searchList.add(it)
                        }
                    }

                    nothingFoundTextView.visibility = if (searchList.isEmpty()) View.VISIBLE else View.GONE
                    bookAdapter = BookAdapter(searchList)
                } else {
                    resetViewVisibility()
                    bookAdapter = BookAdapter(books)
                }
                recyclerView.adapter = bookAdapter
                return false
            }
        })
    }

    private fun loadBooks() {

        database.child("books").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {


                books.clear()

                for (data in snapshot.children) {
                    val book = data.getValue(Book::class.java)
                    if (book != null) {
                        books.add(0, book)
                    }
                }
                progressBar.visibility = View.VISIBLE
                if (selectedCategory != null) {
                    resetViewVisibility()
                    val filteredBooks = books.filter { it.category == selectedCategory }
                    bookAdapter = BookAdapter(filteredBooks)
                    nothingFoundTextView.visibility = if (filteredBooks.isEmpty()) View.VISIBLE else View.GONE
                } else {
                    nothingFoundTextView.visibility = View.GONE
                    bookAdapter = BookAdapter(books)
                }



                recyclerView.adapter = bookAdapter

            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load books: ${error.message}", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
            }
        })
    }

    private fun hideMainViewElements() {
        navDrawerIcon.visibility = View.GONE
        usernameTextView.visibility = View.GONE
        view?.findViewById<TextView>(R.id.textView)?.visibility = View.GONE
        view?.findViewById<TextView>(R.id.textView3)?.visibility = View.GONE
    }

    private fun resetViewVisibility() {
        navDrawerIcon.visibility = View.VISIBLE
        usernameTextView.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.textView)?.visibility = View.VISIBLE
        view?.findViewById<TextView>(R.id.textView3)?.visibility = View.VISIBLE
        nothingFoundTextView.visibility = View.GONE
    }

    private fun fetchUsername(userId: String, callback: (String) -> Unit) {
        val userRef = database.child("users").child(userId).child("username")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java) ?: userId
                callback(username)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching username: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(userId)
            }
        })
    }
}
