@file:Suppress("DEPRECATION")

package com.example.bookrentapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bookrentapp.Vendor.CampusVendorsActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class Login : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val rcSignIn = 100
    private lateinit var sharedPreferences: SharedPreferences
    private var userType: String = "user"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Check if user is already signed in
        if (auth.currentUser != null) {
            val userType = sharedPreferences.getString("userType", "user")
            Log.d("Login", "User type retrieved: $userType")

            val nextActivity = if (userType == "user") {
                MainActivity::class.java
            } else {
                CampusVendorsActivity::class.java
            }

            startActivity(Intent(this, nextActivity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finish()
            return
        }

        configureGoogleSignIn()

        findViewById<MaterialButton>(R.id.google_sign_in_user_button).setOnClickListener {
            userType = "user"
            signIn()
        }

        findViewById<MaterialButton>(R.id.google_sign_in_vendor_button).setOnClickListener {
            userType = "vendor"
            signIn()
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, rcSignIn)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API...")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rcSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(Exception::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Google Sign-In Failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserType(userType)
                    navigateToNextActivity()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserType(userType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("userType", userType)
        editor.apply()
    }

    private fun navigateToNextActivity() {
        val userType = sharedPreferences.getString("userType", "user")
        Log.d("Login", "Navigating to user type: $userType")

        val nextActivity = if (userType == "user") {
            MainActivity::class.java
        } else {
            CampusVendorsActivity::class.java
        }

        startActivity(Intent(this, nextActivity))
        finish()
    }
}
