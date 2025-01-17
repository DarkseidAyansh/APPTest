package com.example.bookrentapp.Vendor

data class FoodItem(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val category: String = "",
    val cookTime: String = "",
    val vendorId: String = ""
) {
    // Firebase requires a no-argument constructor, so we ensure default values are provided.
    constructor() : this("", "", "", "", "")
}
