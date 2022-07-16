package com.ads.web.one.myapplication

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class SignUpActivity : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var btnSignUp: Button
    private lateinit var signIn: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var userImage: ImageView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var uri: Uri
    private var storageRef = Firebase.storage

    //Initializing  database
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        //link XML components
        name = findViewById(R.id.userName)
        email = findViewById(R.id.userEmail)
        password = findViewById(R.id.userPassword)
        confirmPassword = findViewById(R.id.userConfirmPassword)
        btnSignUp = findViewById(R.id.buttonSignUp)
        signIn = findViewById(R.id.textViewSignIn)
        userImage = findViewById(R.id.profImg)
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading File....")
        progressDialog.setCancelable(false)
        //initialize firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        signIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        //For picking image from gallery
        val galleryImage = registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                userImage.setImageURI(it)
                uri = it
            })
        userImage.setOnClickListener {
            galleryImage.launch("image/*")
        }
        btnSignUp.setOnClickListener {
            val usersName = name.text.trim().toString()
            val usersEmail = email.text.trim().toString()
            val usersPassword = password.text.trim().toString()
            val usersConfirmPassword = confirmPassword.text.trim().toString()
            if (usersName.isNotEmpty() && usersEmail.isNotEmpty() && usersPassword.isNotEmpty() && usersConfirmPassword.isNotEmpty()) {
                if (usersPassword == usersConfirmPassword && uri != null) {
                    //creating user with the given email and password
                    firebaseAuth.createUserWithEmailAndPassword(usersEmail, usersPassword)
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful) {
                                //progress dialog starts
                                progressDialog.show()
                                var imgUrl: String? = null
                                val t = System.currentTimeMillis().toString()
                                val ref = storageRef.getReference("images").child(t)
                                val uploadTask = ref.putFile(uri)
                                //getting url of the stored image
                                val urlTask = uploadTask.continueWithTask { task ->
                                    ref.downloadUrl
                                }.addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        val downloadUri = it.result
                                        imgUrl = downloadUri.toString()
                                        val userId = FirebaseAuth.getInstance().currentUser!!.uid
                                        //create a hash map for storing data of a user
                                        val userMap = hashMapOf(
                                            "name" to usersName,
                                            "email" to usersEmail,
                                            "imgUrl" to imgUrl
                                        )
                                        db.collection("User").document(userId).set(userMap)
                                            .addOnSuccessListener {
                                                if (progressDialog.isShowing) {
                                                    progressDialog.dismiss()
                                                }
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(this, "Failed", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                    }
                                }


                            } else {
                                Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }

            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }

    }


    override fun onStart() {
        super.onStart()
        //checks whether user is logged in or not
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}