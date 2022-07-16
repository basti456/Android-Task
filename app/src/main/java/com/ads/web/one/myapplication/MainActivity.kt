package com.ads.web.one.myapplication

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationHome: BottomNavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<ModalProducts>
    private var db = Firebase.firestore
    private lateinit var rl_Cart: RelativeLayout
    private lateinit var items: TextView
    private lateinit var viewCart: TextView
    //initialize firebase Auth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var totalPay: TextView
    //initializing total items and cost as 0
    private var totalCost: Int = 0
    private var totalItems: Int = 0
    private lateinit var cartList: ArrayList<ModalCartItems>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //link XML components
        bottomNavigationHome = findViewById(R.id.btmNavHome)
        recyclerView = findViewById(R.id.recyclerProducts)
        rl_Cart = findViewById(R.id.rl_cart_details)
        items = findViewById(R.id.quantity_item)
        viewCart = findViewById(R.id.txtViewCart)
        totalPay = findViewById(R.id.total_cost_cart)
        firebaseAuth = FirebaseAuth.getInstance()
        //fetching uid of current user
        val userId = firebaseAuth.currentUser!!.uid
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = arrayListOf()
        cartList = arrayListOf()
        bottomNavigationHome.selectedItemId = R.id.nav_home
        bottomNavigationHome.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_cart -> {
                    startActivity(Intent(this, CartActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_account -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
       //listening changes made in database
        db.collection("Products").addSnapshotListener { DocumentSnapshot, e ->
            if (e != null) {
                Log.e(ContentValues.TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (DocumentSnapshot != null) {
                productList.clear()
                //fetching all the products
                for (snapshot in DocumentSnapshot.documents) {
                    val productDetail: ModalProducts? = snapshot.toObject(ModalProducts::class.java)
                    if (productDetail != null) {
                        productList.add(productDetail)
                    }
                }
                recyclerView.adapter = AdapterProducts(productList)
            }
        }
        val ref = db.collection("User").document(userId).collection("Cart")
        ref.addSnapshotListener { DocumentSnapshot, e ->
            if (e != null) {
                Log.e(TAG, "Listen failed.", e)
                return@addSnapshotListener
            }
            if (DocumentSnapshot != null) {
                cartList.clear()
                totalCost = 0
                totalItems = 0
                for (snapshot in DocumentSnapshot.documents) {
                    val cartItemDetail: ModalCartItems? =
                        snapshot.toObject(ModalCartItems::class.java)
                    if (cartItemDetail != null) {
                        cartList.add(cartItemDetail)
                    }
                }
                //checking whether cart list is empty or not
                if (cartList.isEmpty()) {
                    rl_Cart.visibility = View.GONE
                } else {
                    rl_Cart.visibility = View.VISIBLE
                    for (i in 1..minOf(cartList.size, 10)) {
                        totalCost += cartList[i - 1].finalPrice?.toInt() ?: 0
                    }
                    totalItems = minOf(cartList.size, 10)
                    items.text = totalItems.toString()
                    totalPay.text = totalCost.toString()
                }
            }
        }
        viewCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}