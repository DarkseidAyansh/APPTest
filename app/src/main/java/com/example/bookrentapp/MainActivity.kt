

package com.example.bookrentapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.bookrentapp.Book.UploadBook
import com.example.bookrentapp.Chat.ChatFragment
import com.example.bookrentapp.Dashboard.UserDashboardFragment
import com.example.bookrentapp.databinding.ActivityMainBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Load the initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
            binding.bottomNavigation.selectedItemId = R.id.nav_home // Set initial selected item
        }

        fab = findViewById(R.id.fab)

        fab.setOnClickListener {
            showBottomSheet()
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_chat -> ChatFragment()
                R.id.nav_dashboard -> UserDashboardFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment()
            }

            // Get the current fragment
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

            // Prevent re-adding the same fragment
            if (currentFragment != null && currentFragment::class == selectedFragment::class) {
                return@setOnItemSelectedListener true
            }

            // Set FAB visibility based on selected fragment
            fab.visibility = if (selectedFragment is UserDashboardFragment || selectedFragment is ChatFragment) {
                View.GONE
            } else {
                View.VISIBLE
            }

            // Begin transaction and replace the fragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .addToBackStack(null)
                .commit()

            true
        }

        // Listen for back stack changes
        supportFragmentManager.addOnBackStackChangedListener {
            updateBottomNavigation()
        }
    }

    private fun showBottomSheet() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        val cancelButton: ImageView = bottomSheetView.findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val uploadText: TextView = bottomSheetView.findViewById(R.id.upload)
        uploadText.setOnClickListener {
            val intent = Intent(this, UploadBook::class.java)
            startActivity(intent)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun updateBottomNavigation() {
        // Get the current fragment and update the bottom navigation selection
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        val selectedItemId = when (currentFragment) {
            is HomeFragment -> R.id.nav_home
            is ChatFragment -> R.id.nav_chat
            is UserDashboardFragment -> R.id.nav_dashboard
            is ProfileFragment -> R.id.nav_profile
            else -> R.id.nav_home // Default selection
        }
        binding.bottomNavigation.selectedItemId = selectedItemId
    }
}
