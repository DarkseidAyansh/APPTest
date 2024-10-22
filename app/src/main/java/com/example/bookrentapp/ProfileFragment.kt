package com.example.bookrentapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import android.app.Dialog
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference

    private lateinit var progressBar: ProgressBar
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference.child("profile_images")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        val logoutButton: TextView = view.findViewById(R.id.logout)
        logoutButton.setOnClickListener {
            logout()
        }

        val profileNameTextView: TextView = view.findViewById(R.id.profile_name)
        progressBar = view.findViewById(R.id.progressBar)

        bottomNavigationView = requireActivity().findViewById(R.id.bottom_navigation)

        fetchUsername(auth.currentUser?.uid.toString()) { username ->
            profileNameTextView.text = username
        }

        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        loadProfileImage(profileImage)

        profileImage.setOnClickListener {
            fetchImageFromGallery()
        }

        val editProfileButton: Button = view.findViewById(R.id.button_edit_profile)
        editProfileButton.setOnClickListener {
            showEditUsernameDialog(profileNameTextView)
        }

        return view
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.GONE
    }

    private fun fetchImageFromGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.type = "image/*"
        imageLauncher.launch(intent)
    }

    private fun showEditUsernameDialog(profileNameTextView: TextView) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_edit_username)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        dialog.window?.attributes = layoutParams

        val editText: EditText = dialog.findViewById(R.id.edit_username)
        val saveButton: Button = dialog.findViewById(R.id.save_username_button)

        saveButton.setOnClickListener {
            val newUsername = editText.text.toString().trim()
            if (newUsername.isNotEmpty()) {
                showProgressBar()
                saveUsername(auth.currentUser?.uid.toString(), newUsername) {
                    profileNameTextView.text = newUsername
                    dialog.dismiss()
                    hideProgressBar()
                }
            } else {
                Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun loadProfileImage(profileImage: ImageView) {
        val userId = auth.currentUser?.uid.toString()
        val profilePicRef = database.child("users").child(userId).child("profile_pic")

        showProgressBar()

        profilePicRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrl = snapshot.getValue(String::class.java)
                if (imageUrl != null) {
                    // Use Glide to load the profile image
                    Glide.with(this@ProfileFragment).load(imageUrl).into(profileImage)
                }
                hideProgressBar()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error loading profile image: ${error.message}", Toast.LENGTH_SHORT).show()
                hideProgressBar()

            }
        })

    }

    private val imageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null) {
            val selectedImageUri: Uri? = it.data?.data
            if (selectedImageUri != null) {
                val profileImage: ImageView = requireView().findViewById(R.id.profile_image)
                profileImage.setImageURI(selectedImageUri)
                uploadImageToFirebase(selectedImageUri)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val userId = auth.currentUser?.uid.toString()
        val profilePicRef = storageReference.child("$userId.jpg")

        showProgressBar()

        profilePicRef.putFile(imageUri).addOnSuccessListener {
            profilePicRef.downloadUrl.addOnSuccessListener { uri ->
                saveImageUrlToDatabase(uri.toString())
                Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show()
                hideProgressBar()
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to upload image", Toast.LENGTH_SHORT).show()
            hideProgressBar()
        }
    }

    private fun saveImageUrlToDatabase(imageUrl: String) {
        val userId = auth.currentUser?.uid.toString()
        val userRef = database.child("users").child(userId).child("profile_pic")

        showProgressBar()

        userRef.setValue(imageUrl).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Image URL saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to save image URL", Toast.LENGTH_SHORT).show()
            }
            hideProgressBar()
        }
    }

    private fun logout() {
        showProgressBar()
        auth.signOut()
        hideProgressBar()
        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
        val loginIntent = Intent(activity, Login::class.java)
        loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(loginIntent)
    }

    private fun saveUsername(userId: String, username: String, onComplete: () -> Unit) {
        val userRef = database.child("users").child(userId).child("username")

        showProgressBar()

        userRef.setValue(username).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(requireContext(), "Username updated", Toast.LENGTH_SHORT).show()
                onComplete()
            } else {
                Toast.makeText(requireContext(), "Error updating username", Toast.LENGTH_SHORT).show()
            }
            hideProgressBar()
        }
    }

    private fun fetchUsername(userId: String, callback: (String) -> Unit) {
        val userRef = database.child("users").child(userId).child("username")

        showProgressBar()

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val username = snapshot.getValue(String::class.java) ?: userId
                callback(username)
                hideProgressBar()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Error fetching username: ${error.message}", Toast.LENGTH_SHORT).show()
                callback(userId)
                hideProgressBar()
            }
        })
    }
}

