package com.example.grabee
import androidx.annotation.Keep

@Keep

data class Food(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageURL: String = "",
    val category: String = ""
)

{
    // Add this no-argument constructor for Firestore
    constructor() : this("", "", "", 0.0, "")
}