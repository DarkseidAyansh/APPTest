package com.example.bookrentapp.Book

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.bookrentapp.Login
import com.example.bookrentapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UploadBook : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null
    private lateinit var bookId: String // Store the bookId for use in image uploading

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_book)

        // Initialize Firebase references
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("item_images")

        val editTextTitle = findViewById<EditText>(R.id.item_title_input)
        val editTextAuthor = findViewById<EditText>(R.id.item_author_input)
        val editTextPrice = findViewById<EditText>(R.id.item_price_input)
        val categorySpinner = findViewById<Spinner>(R.id.category_spinner)
        val buttonUpload = findViewById<Button>(R.id.upload_button)
        val image = findViewById<ImageView>(R.id.item_image_view)

        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = ProgressBar.GONE // Hide progress bar initially

        // Handle image selection
        image.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            imageLauncher.launch(intent)
        }

        // Check if user is authenticated
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please sign in first", Toast.LENGTH_SHORT).show()
            val signInIntent = Intent(this, Login::class.java)
            startActivity(signInIntent)
            finish()
        } else {
            buttonUpload.setOnClickListener {
                val title = editTextTitle.text.toString()
                val author = editTextAuthor.text.toString()
                val price = editTextPrice.text.toString()
                val category = categorySpinner.selectedItem.toString()

                // Ensure all fields are filled
                if (title.isNotEmpty() && author.isNotEmpty() && price.isNotEmpty() && category.isNotEmpty()) {
                    val sellerId = currentUser.uid // Use the current authenticated user ID
                    bookId = database.child("books").push().key ?: "" // Generate a unique bookId

                    val book = Book(bookId, title, author, price, sellerId, "", category)

                    // Upload the image and then upload the book details
                    if (selectedImageUri != null) {
                        progressBar.visibility = ProgressBar.VISIBLE // Show progress bar
                        uploadImageToFirebase(selectedImageUri!!, book)
                    } else {
                        Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Upload image to Firebase Storage and save URL to book details
    private fun uploadImageToFirebase(imageUri: Uri, book: Book) {
        val itemRef = storageReference.child("${book.bookId}.jpg")

        val uploadTask = itemRef.putFile(imageUri)
        uploadTask.addOnSuccessListener {
            itemRef.downloadUrl.addOnSuccessListener { uri ->
                book.imgUrl = uri.toString() // Set image URL in the book object
                uploadBook(book) // Now upload the book with the image URL
            }
        }.addOnFailureListener {
            progressBar.visibility = ProgressBar.GONE // Hide progress bar on failure
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
        }
    }

    // Upload book details to Firebase Realtime Database
    private fun uploadBook(book: Book) {
        if (book.bookId.isNotEmpty()) {
            database.child("books").child(book.bookId).setValue(book)
                .addOnSuccessListener {
                    progressBar.visibility = ProgressBar.GONE // Hide progress bar on success
                    Toast.makeText(this, "Book uploaded successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    progressBar.visibility = ProgressBar.GONE // Hide progress bar on failure
                    Toast.makeText(this, "Failed to upload book", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Handle image picker result
    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            selectedImageUri = it.data?.data // Save selected image URI for upload
            val imageView = findViewById<ImageView>(R.id.item_image_view)
            imageView.setImageURI(selectedImageUri) // Display selected image
        }
    }
}
