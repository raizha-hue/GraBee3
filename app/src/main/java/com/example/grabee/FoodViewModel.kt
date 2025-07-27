package com.example.grabee.viewmodels
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class FoodViewModel : ViewModel() {
    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uploadProgress = MutableStateFlow(0)
    val uploadProgress: StateFlow<Int> = _uploadProgress.asStateFlow()

    sealed class UploadResult {
        data class Success(val documentId: String) : UploadResult()
        data class Error(val message: String) : UploadResult()
    }

    fun uploadFoodItem(
        name: String,
        price: Double,
        description: String,
        imageUri: Uri?,
        onComplete: (UploadResult) -> Unit
    ) = viewModelScope.launch {
        try {
            _uploadProgress.value = 0

            val imageUrl = imageUri?.let { uri ->
                val filename = "food_images/${UUID.randomUUID()}"
                val ref = storage.reference.child(filename)

                val uploadTask = ref.putFile(uri)
                    .addOnProgressListener { snapshot ->
                        val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                        _uploadProgress.value = progress
                    }

                uploadTask.await()
                ref.downloadUrl.await().toString()
            }

            val documentReference = db.collection("food_items")
                .add(hashMapOf(
                    "name" to name,
                    "price" to price,
                    "description" to description,
                    "imageUrl" to imageUrl,
                    "createdAt" to System.currentTimeMillis()
                ))
                .await()

            onComplete(UploadResult.Success(documentReference.id))
        } catch (e: Exception) {
            onComplete(UploadResult.Error(e.message ?: "Upload failed"))
        } finally {
            _uploadProgress.value = 0
        }
    }
}