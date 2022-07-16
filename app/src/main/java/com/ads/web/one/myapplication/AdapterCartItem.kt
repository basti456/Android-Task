package com.ads.web.one.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso

class AdapterCartItem(private val cartList: ArrayList<ModalCartItems>) :
    RecyclerView.Adapter<AdapterCartItem.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //link XML components
        val productImg: ImageView = itemView.findViewById(R.id.product_image)
        val productPr: TextView = itemView.findViewById(R.id.product_price_each)
        val productNm: TextView = itemView.findViewById(R.id.product_name)
        val productDesc: TextView = itemView.findViewById(R.id.product_desc)
        val finalQuantity: TextView = itemView.findViewById(R.id.product_total_quantity)
        val finalPrice: TextView = itemView.findViewById(R.id.product_total_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.single_cart_item, parent, false);
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        //fetching image using picasso library
        Picasso.get().load(cartList[position].productImgUrl.toString()).into(holder.productImg)
        holder.productPr.text = cartList[position].priceEach
        holder.productDesc.text = cartList[position].productDesc
        holder.productNm.text = cartList[position].productName
        holder.finalQuantity.text = "Quantity:-" + cartList[position].finalQuantity
        holder.finalPrice.text = "Rs. " + cartList[position].finalPrice
    }

    override fun getItemCount(): Int {
        return minOf(cartList.size, 10)
    }
}