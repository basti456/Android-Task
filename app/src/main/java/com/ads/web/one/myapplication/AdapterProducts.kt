package com.ads.web.one.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class AdapterProducts(private val productList: ArrayList<ModalProducts>) :
    RecyclerView.Adapter<AdapterProducts.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImg: ShapeableImageView = itemView.findViewById(R.id.productImage)
        val productPr: TextView = itemView.findViewById(R.id.productPrice)
        val productNm: TextView = itemView.findViewById(R.id.productName)
        val productDesc: TextView = itemView.findViewById(R.id.productDescription)
        val btnAdd: Button = itemView.findViewById(R.id.buttonAdd)
        val btnDecrease: Button = itemView.findViewById(R.id.counter_decrease)
        val btnIncrease: Button = itemView.findViewById(R.id.counter_increase)
        val productCount: TextView = itemView.findViewById(R.id.txtCounter)
        val ll: LinearLayout = itemView.findViewById(R.id.ll_counter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.singleproduct, parent, false);
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val db = Firebase.firestore
        val fa = FirebaseAuth.getInstance()
        val userId = fa.currentUser!!.uid
        holder.productPr.text = "Rs. "+productList[position].price
        holder.productDesc.text = productList[position].description
        holder.productNm.text = productList[position].name
        Picasso.get().load(productList[position].imgUrl.toString()).into(holder.productImg)
        val ref = db.collection("User").document(userId)
        val otherRef = db.collection("User").document(userId).collection("Cart")
        otherRef.addSnapshotListener { DataSnapshot, e ->
            otherRef.document(productList[position].id.toString())
                .addSnapshotListener { Snapshot, e ->
                    if (Snapshot != null) {
                        //checks whether item is available in cart or not
                        if (Snapshot.exists()) {
                            holder.btnAdd.visibility = View.GONE
                            holder.ll.visibility = View.VISIBLE
                            val quantity = Snapshot.data?.get("finalQuantity")
                            holder.productCount.text = (quantity.toString())
                            addToCart1(
                                holder,
                                otherRef.document(productList[position].id.toString()),
                                productList[position],
                                quantity.toString()
                            )
                        } else {
                            holder.btnAdd.visibility = View.VISIBLE
                            holder.ll.visibility = View.GONE
                            holder.btnAdd.setOnClickListener {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "Adding..",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                addToCart(
                                    productList[position],
                                    productList[position].id.toString(),
                                    ref.collection("Cart"),
                                    holder.ll,
                                    holder.itemView,
                                    holder.btnAdd
                                )
                            }
                        }
                    }
                }
        }
    }

    //function to increase/decrese quantity of items when itm is already existed
    private fun addToCart1(
        holder: MyViewHolder,
        newRef: DocumentReference,
        modalProducts: ModalProducts,
        quantity: String
    ) {
        var total = quantity.toInt()
        holder.btnIncrease.setOnClickListener {
            total += 1
            holder.productCount.text = total.toString()
            val price = modalProducts.price.toString().toInt()
            val cartMap = mapOf(
                "id" to modalProducts.id,
                "finalPrice" to (total * price).toString(),
                "finalQuantity" to total.toString(),
                "priceEach" to modalProducts.price.toString(),
                "productImgUrl" to modalProducts.imgUrl.toString(),
                "productDesc" to modalProducts.description.toString(),
                "productName" to modalProducts.name.toString()
            )
            newRef.update(cartMap).addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Updated", Toast.LENGTH_SHORT).show()
            }
        }
        holder.btnDecrease.setOnClickListener {
            val price = modalProducts.price.toString().toInt()
            if (total == 1) {
                holder.ll.visibility = View.GONE
                holder.btnAdd.visibility = View.VISIBLE

                newRef.delete().addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Updated In", Toast.LENGTH_SHORT).show()
                }
            } else if (total > 1) {
                total -= 1
                val cartMap = mapOf(
                    "id" to modalProducts.id,
                    "finalPrice" to (total * price).toString(),
                    "finalQuantity" to total.toString(),
                    "priceEach" to modalProducts.price.toString(),
                    "productImgUrl" to modalProducts.imgUrl.toString(),
                    "productDesc" to modalProducts.description.toString(),
                    "productName" to modalProducts.name.toString()
                )
                newRef.update(cartMap).addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Updated De", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // function to add item
    private fun addToCart(
        modalProducts: ModalProducts,
        id: String,
        ref: CollectionReference,
        ll: LinearLayout,
        itemView: View,
        btnAdd: Button
    ) {

        val cartMap = hashMapOf(
            "id" to id,
            "finalPrice" to modalProducts.price.toString(),
            "finalQuantity" to "1",
            "priceEach" to modalProducts.price.toString(),
            "productImgUrl" to modalProducts.imgUrl.toString(),
            "productDesc" to modalProducts.description.toString(),
            "productName" to modalProducts.name.toString()
        )
        ref.document(id).set(cartMap).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(itemView.context, "Item Added to Cart", Toast.LENGTH_SHORT).show()
                btnAdd.visibility = View.GONE
                ll.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}