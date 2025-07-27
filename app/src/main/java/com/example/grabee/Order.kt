// Order.kt
package com.example.grabee

data class Order(
    var id: String = "",
    val customerId: String, // Required for ecoPoints updates
    val customerName: String = "",
    val deliveryAddress: String = "",
    val items: List<String> = emptyList(),
    var status: String = "Pending", // Pending → Accepted → PickedUp → Delivered
    val timestamp: Long = System.currentTimeMillis()
){
    enum class Status {
        PENDING, ACCEPTED, DELIVERED
    }
}