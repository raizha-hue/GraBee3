// RiderDashboardViewModel.kt
package com.example.grabee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RiderDashboardViewModel : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _orders.value = _orders.value.map { order ->
                if (order.id == orderId) order.copy(status = newStatus) else order
            }
        }
    }

    fun completeOrderDelivery(orderId: String, customerId: String) {
        viewModelScope.launch {
            try {
                // 1. Update order status in Firestore
                Firebase.firestore.collection("orders")
                    .document(orderId)
                    .update("status", "Delivered")

                // 2. Update customer's ecoPoints
                Firebase.firestore.collection("customers")
                    .document(customerId)
                    .update("ecoPoints", FieldValue.increment(10.0))

                // 3. Update local state
                _orders.value = _orders.value.map { order ->
                    if (order.id == orderId) order.copy(status = "Delivered") else order
                }

                _toastMessage.value = "Delivery completed! +10 EcoPoints awarded"
            } catch (e: Exception) {
                _toastMessage.value = "Error updating delivery: ${e.message}"
            }
        }
    }

    // Add this function to clear toast messages
    fun clearToastMessage() {
        _toastMessage.value = null
    }

    init {
        // Initialize with sample data
        _orders.value = listOf(
            Order(
                id = "order1",
                customerId = "customer123",
                customerName = "John Doe",
                deliveryAddress = "123 Main St",
                items = listOf("Burger", "Fries"),
                status = "Pending"
            )
        )
        _isLoading.value = false
    }
}