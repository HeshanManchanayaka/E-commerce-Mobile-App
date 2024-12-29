package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class Register : AppCompatActivity() {


       private lateinit var binding: ActivityRegisterBinding
        private lateinit var firebaseAuth: FirebaseAuth
        private lateinit var firebaseDatabase: FirebaseDatabase

      override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityRegisterBinding.inflate(layoutInflater)
           setContentView(binding.root)


         firebaseAuth = FirebaseAuth.getInstance()
           firebaseDatabase = FirebaseDatabase.getInstance()


            binding.buttonSignUp.setOnClickListener() {
                  val name = binding.editTextName.text.toString()
                val email = binding.editTextEmail.text.toString()
                val password = binding.editTextPassword.text.toString()
                val confirmPassword = binding.editTextConfirmPassword.text.toString()

               if (name.isEmpty()) {
                    binding.editTextName.error = "Name is required"
                    return@setOnClickListener
                }
                if (email.isEmpty()) {
                    binding.editTextEmail.error = "Email is required"
                   return@setOnClickListener
                }
                if (password.isEmpty()) {
                    binding.editTextPassword.error = "Password is required"
                    return@setOnClickListener
                }

                if (confirmPassword.isEmpty()) {
                binding.editTextConfirmPassword.error = "Confirm Password is required"
                return@setOnClickListener
            }
                if (password != confirmPassword) {
                    binding.editTextConfirmPassword.error = "Passwords do not match"
                    return@setOnClickListener
                }


                signUpUser(name, email, password)
            }
        }

    fun onRegisterClick(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
    fun ontextView2(view: View) {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
    }
        private fun signUpUser(name: String, email: String, password: String) {

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val user = firebaseAuth.currentUser
                        user?.let {

                            val userData = UserData(name, email, password)
                            firebaseDatabase.reference.child("users").child(user.uid)
                                .setValue(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@Register,
                                        "User signed up successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                   val intent = Intent(this@Register, Login::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this@Register,
                                        "Failed to sign up user: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {

                        Toast.makeText(
                            this@Register,
                            "Failed to sign up user: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
}



