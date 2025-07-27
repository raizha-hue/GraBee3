package com.example.grabee

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VendorRegistrationViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun registerVendor(
        email: String,
        restaurantName: String,
        address: String,
        phone: String,
        description: String,
        hasHalalCert: Boolean,
        halalCertUri: Uri?,
        onSuccess: () -> Unit
    ) {
        try {
            _isLoading.value = true
            _errorMessage.value = null

            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            val halalCertUrl = if (hasHalalCert && halalCertUri != null) {
                val certFileName = "halal_certs/$userId.jpg"
                storage.reference.child(certFileName)
                    .putFile(halalCertUri)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
                    .toString()
            } else null

            val vendorData = hashMapOf(
                "email" to email,
                "restaurantName" to restaurantName,
                "address" to address,
                "phone" to phone,
                "description" to description,
                "hasHalalCert" to hasHalalCert,
                "halalCertUrl" to halalCertUrl,
                "registrationComplete" to true,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            val batch = db.batch()
            batch.set(db.collection("vendors").document(userId), vendorData, SetOptions.merge())
            batch.update(db.collection("users").document(userId), mapOf("registrationComplete" to true))
            batch.commit().await()

            onSuccess()
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Registration failed"
        } finally {
            _isLoading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorRegistrationScreen(
    email: String,
    onRegistrationComplete: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: VendorRegistrationViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // ViewModel states
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Form states
    var restaurantName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hasHalalCert by remember { mutableStateOf(false) }
    var halalCertImageUri by remember { mutableStateOf<Uri?>(null) }
    var acceptedTerms by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> halalCertImageUri = uri }

    // Validation
    val isRestaurantNameValid = restaurantName.length >= 3
    val isAddressValid = address.length >= 5
    val isPhoneValid = phone.matches(Regex("^[0-9]{10,15}\$"))
    val isFormValid = isRestaurantNameValid &&
            isAddressValid &&
            isPhoneValid &&
            acceptedTerms &&
            (!hasHalalCert || halalCertImageUri != null)

    // Show error message if any
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Complete Vendor Registration") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = restaurantName,
                onValueChange = { restaurantName = it },
                label = { Text("Restaurant Name*") },
                isError = restaurantName.isNotEmpty() && !isRestaurantNameValid,
                supportingText = {
                    if (restaurantName.isNotEmpty() && !isRestaurantNameValid) {
                        Text("Minimum 3 characters required")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address*") },
                isError = address.isNotEmpty() && !isAddressValid,
                supportingText = {
                    if (address.isNotEmpty() && !isAddressValid) {
                        Text("Minimum 5 characters required")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone*") },
                isError = phone.isNotEmpty() && !isPhoneValid,
                supportingText = {
                    if (phone.isNotEmpty() && !isPhoneValid) {
                        Text("10-15 digits required")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                singleLine = false
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = hasHalalCert,
                    onCheckedChange = { hasHalalCert = it }
                )
                Text("Has Halal Certification")
            }

            if (hasHalalCert) {
                Button(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload Certificate")
                }

                halalCertImageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Halal Certificate",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = acceptedTerms,
                    onCheckedChange = { acceptedTerms = it }
                )
                Text("I accept the terms and conditions")
            }

            Button(
                onClick = {
                    scope.launch {
                        viewModel.registerVendor(
                            email = email,
                            restaurantName = restaurantName,
                            address = address,
                            phone = phone,
                            description = description,
                            hasHalalCert = hasHalalCert,
                            halalCertUri = halalCertImageUri,
                            onSuccess = onRegistrationComplete
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = isFormValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        "Complete Registration",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}