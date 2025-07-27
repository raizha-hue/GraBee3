package com.example.grabee

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CustomerDetailsViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    private val _customer = MutableStateFlow(Customer())
    val customer = _customer.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        loadCustomerData()
    }

    fun loadCustomerData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val document = db.collection("customers").document(userId).get().await()

                document.toObject(Customer::class.java)?.let { customerData ->
                    _customer.value = customerData.copy(userId = userId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCustomerDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                db.collection("customers").document(userId)
                    .set(_customer.value.copy(userId = userId))
                    .await()
                _isSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Save failed: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateField(
        fullName: String? = null,
        phone: String? = null,
        birthDate: String? = null,
        address: String? = null
    ) {
        _customer.value = _customer.value.copy(
            fullName = fullName ?: _customer.value.fullName,
            phoneNumber = phone ?: _customer.value.phoneNumber,
            birthDate = birthDate ?: _customer.value.birthDate,
            address = address ?: _customer.value.address
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccess() {
        _isSuccess.value = false
    }
}