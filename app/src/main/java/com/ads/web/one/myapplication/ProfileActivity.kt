package com.ads.web.one.myapplication

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    private lateinit var bottomNavProfile: BottomNavigationView
    private lateinit var profileImg: ShapeableImageView
    private lateinit var profileName: TextView
    private lateinit var profileEmail: TextView
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        bottomNavProfile = findViewById(R.id.btmNavProfile);
        profileImg = findViewById(R.id.profileImage)
        profileName = findViewById(R.id.username)
        profileEmail = findViewById(R.id.email)
        bottomNavProfile.selectedItemId = R.id.nav_account
        bottomNavProfile.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_account -> {
                    return@setOnNavigationItemSelectedListener true
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val ref = db.collection("User").document(userId)
        ref.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val name = snapshot.data?.get("name").toString()
                val email = snapshot.data?.get("email").toString()
                val imgUrl=snapshot.data?.get("imgUrl").toString()
                profileEmail.text = email
                profileName.text = name
                Picasso.get().load(imgUrl).placeholder(R.drawable.dummy_image).into(profileImg)
            }
        }
    }
}