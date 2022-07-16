package com.ads.web.one.myapplication

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import kotlin.math.roundToInt

class CartActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var bottomNavigationCart: BottomNavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var cartList: ArrayList<ModalCartItems>
    private lateinit var addItems: Button
    private var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var emptyCarts: ConstraintLayout
    private lateinit var paymentDetails: ConstraintLayout
    private lateinit var totalPayment: TextView
    private lateinit var payNow: Button
    private var totalCost: Int = 0
    private var totalItems: Int = 0
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)
        //link XML components
        bottomNavigationCart = findViewById(R.id.btmNavCart)
        recyclerView = findViewById(R.id.recycler_Cart_Items)
        emptyCarts = findViewById(R.id.cl_empty_cart)
        payNow = findViewById(R.id.btnPayNow)
        paymentDetails = findViewById(R.id.cl_details)
        totalPayment = findViewById(R.id.totalPayment)
        addItems = findViewById(R.id.btn_add_items)
        firebaseAuth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)
        val userId = firebaseAuth.currentUser!!.uid
        recyclerView.layoutManager = LinearLayoutManager(this)
        cartList = arrayListOf()
        bottomNavigationCart.selectedItemId = R.id.nav_cart
        bottomNavigationCart.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
                R.id.nav_cart -> {
                    return@setOnNavigationItemSelectedListener true
                }
                R.id.nav_account -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    overridePendingTransition(0, 0)
                    finish()
                }
            }
            return@setOnNavigationItemSelectedListener false
        }
        db.collection("User").document(userId).collection("Cart")
            .addSnapshotListener { DocumentSnapshot, e ->
                if (e != null) {
                    Log.e(ContentValues.TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }
                if (DocumentSnapshot != null) {
                    cartList.clear()
                    //fetching all the items in the cart
                    for (snapshot in DocumentSnapshot.documents) {
                        val cartItemDetail: ModalCartItems? =
                            snapshot.toObject(ModalCartItems::class.java)
                        if (cartItemDetail != null) {
                            cartList.add(cartItemDetail)
                        }
                    }
                    if (cartList.isEmpty()) {
                        recyclerView.visibility = View.GONE
                        paymentDetails.visibility = View.GONE
                        emptyCarts.visibility = View.VISIBLE
                        addItems.setOnClickListener {
                            val intent = Intent(this, MainActivity::class.java);
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        emptyCarts.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        paymentDetails.visibility = View.VISIBLE
                        for (i in 1..minOf(cartList.size, 10)) {
                            totalCost += cartList[i - 1].finalPrice?.toInt() ?: 0
                        }
                        totalItems = minOf(cartList.size, 10)
                        recyclerView.adapter = AdapterCartItem(cartList)
                        totalPayment.text = "Rs. " + totalCost + "($totalItems/items)"
                    }
                }
            }
        payNow.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val dialog = builder.show()
            builder.setTitle("Confirmation")
            builder.setMessage("Are you sure to place your order")
            builder.setPositiveButton("Confirm") { text, listener ->
                dialog.dismiss()
                //Initialise razorpay checkout
                val co = Checkout()
                //set key_id
                co.setKeyID("rzp_test_Xfyxwn1v9tlKgE")
                //set image
                co.setImage(com.razorpay.R.drawable.rzp_logo)
                try {
                    val options = JSONObject()
                    options.put("name", "Admin")
                    options.put("description", "Order Charges")
                    //You can omit the image option to fetch the image from dashboard
                    options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
                    options.put("theme.color", "#3399cc")
                    options.put("currency", "INR")
                    options.put("amount", "${totalCost*100}")//pass amount in currency subunits
                    val prefill = JSONObject()
                    prefill.put("email", "akagraagarwal89@gmail.com")
                    prefill.put("contact", "7317544896")
                    options.put("prefill", prefill)
                    co.open(this, options)

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            builder.setNegativeButton("Exit") { text, listener ->
                dialog.dismiss()
            }
            builder.create().show()
        }

    }

    override fun onPaymentSuccess(p0: String?) {
        Toast.makeText(this, "Payment success", Toast.LENGTH_SHORT).show()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser!!.uid
        val newRef = db.collection("User").document(userId).collection("Cart")
        newRef.get().addOnCompleteListener { task ->
            for (snapshot in task.result) {
                //deleting cart elements after payment success
                newRef.document(snapshot.id).delete().addOnSuccessListener {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    override fun onPaymentError(p0: Int, p1: String?) {
        try {
            Toast.makeText(this, "Payment error please try again", Toast.LENGTH_SHORT).show()
        } catch (e: java.lang.Exception) {
            Log.e("OnPaymentError", "Exception in onPaymentError", e)
        }
    }
}