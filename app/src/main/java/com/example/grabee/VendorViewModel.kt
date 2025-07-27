package com.example.grabee

import android.net.Uri
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class VendorViewModel @Inject constructor() : ViewModel() {
    val supabase: SupabaseClient = createSupabaseClient(
        supabaseUrl = "YOUR_SUPABASE_URL",
        supabaseKey = "YOUR_SUPABASE_KEY"
    ) {
        install(Postgrest)
        install(Storage)
    }

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun addFoodItem(
        vendorId: String,
        name: String,
        description: String,
        price: Double,
        category: String,
        isHalal: Boolean,
        isAvailable: Boolean,
        imageUri: Uri,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true

                // 1. Upload image to Supabase Storage
                val imageUrl = supabase.storage["food-images"]
                    .upload(
                        fileName = "${vendorId}_${System.currentTimeMillis()}.jpg",
                        file = imageUri
                    )
                    .getPublicUrl()

                // 2. Insert food data to Supabase
                supabase.postgrest["food_items"].insert(
                    mapOf(
                        "vendor_id" to vendorId,
                        "name" to name,
                        "description" to description,
                        "price" to price,
                        "category" to category,
                        "is_halal" to isHalal,
                        "is_available" to isAvailable,
                        "image_url" to imageUrl
                    )
                )

                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add food: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}