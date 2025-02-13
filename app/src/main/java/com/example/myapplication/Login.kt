package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import android.view.View


class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        // Set click listener for the login button
        binding.loginButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            // Validation (Optional but recommended)
            if (email.isEmpty()) {
                binding.emailLayout.error = "Email is required"
                return@setOnClickListener
            } else {
                binding.emailLayout.error = null
            }
            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password is required"
                return@setOnClickListener
            } else {
                binding.passwordLayout.error = null
            }

            // Call the loginUser method with the user inputs
            loginUser(email, password)
        }
    }

    fun onRegisterClick(view: View) {
        val intent = Intent(this, Register::class.java)
        startActivity(intent)
    }

    private fun loginUser(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login successful
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    // Navigate to another activity (e.g., ViewPost)
                    val intent = Intent(this, ViewPost::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Login failed
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to login: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
