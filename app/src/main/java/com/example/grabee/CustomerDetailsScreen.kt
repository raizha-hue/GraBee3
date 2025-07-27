package com.example.grabee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailsScreen(
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit = {},
    viewModel: CustomerDetailsViewModel = viewModel()
) {
    val customer by viewModel.customer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    // Add this block right after state collection
    val auth = Firebase.auth
    if (auth.currentUser == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Please login", color = MaterialTheme.colorScheme.error)
            Button(onClick = onBack) {
                Text("Go to Login")
            }
        }
        return  // This exits the composable early
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            viewModel.clearSuccess()
            onSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personal Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> CustomerForm(
                    customer = customer,
                    onUpdate = viewModel::updateField,
                    onSave = viewModel::saveCustomerDetails,
                    isSuccess = isSuccess,
                    error = error,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun CustomerForm(
    customer: Customer,
    onUpdate: (String?, String?, String?, String?) -> Unit,
    onSave: () -> Unit,
    isSuccess: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    // Track phone validation state
    val isPhoneValid by remember(customer.phoneNumber) {
        derivedStateOf {
            customer.phoneNumber.length >= 10  // Minimum 10 digits
        }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = customer.fullName,
            onValueChange = { onUpdate(it, null, null, null) },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        // Phone Number Field
        OutlinedTextField(
            value = customer.phoneNumber,
            onValueChange = { newPhone ->
                if (newPhone.all { it.isDigit() }) {  // Only allow numbers
                    onUpdate(null, newPhone, null, null)
                }
            },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = !isPhoneValid && customer.phoneNumber.isNotBlank(),
            supportingText = {
                if (!isPhoneValid && customer.phoneNumber.isNotBlank()) {
                    Text("Minimum 10 digits required")
                }
            }
        )

        OutlinedTextField(
            value = customer.birthDate,
            onValueChange = { onUpdate(null, null, it, null) },
            label = { Text("Birth Date (DD/MM/YYYY)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = customer.address,
            onValueChange = { onUpdate(null, null, null, it) },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = customer.fullName.isNotBlank() && isPhoneValid  // Updated validation
        ) {
            Text("Save Details")
        }

        if (isSuccess) {
            Text(
                "Saved successfully!",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}