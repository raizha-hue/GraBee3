package com.example.grabee

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class Customer(
    val userId: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val birthDate: String = "",
    val address: String = "",
    val ecoPoints: Int = 0
) {
    // Add a no-arg constructor for Firestore
    constructor() : this("", "", "", "", "", 0)
}