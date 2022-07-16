package com.ads.web.one.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var loginEmail: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        //link XML components
        loginEmail = findViewById(R.id.userLoginEmail)
        loginPassword = findViewById(R.id.userLoginPassword)
        loginButton = findViewById(R.id.buttonLogIn)
        firebaseAuth = FirebaseAuth.getInstance()
        loginButton.setOnClickListener {
            val email = loginEmail.text.trim().toString()
            val pass = loginPassword.text.trim().toString()
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                //check whether user exists with entered email and password
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Wrong Credentials", Toast.LENGTH_SHORT).show()

                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
