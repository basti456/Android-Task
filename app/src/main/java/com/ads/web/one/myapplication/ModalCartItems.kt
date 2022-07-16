package com.ads.web.one.myapplication

//modal class for cart items
data class ModalCartItems(
    val id: String? = null,
    val finalPrice: String? = null,
    val finalQuantity: String? = null,
    val priceEach: String? = null,
    val productImgUrl: String? = null,
    val productDesc: String? = null,
    val productName: String? = null
)
