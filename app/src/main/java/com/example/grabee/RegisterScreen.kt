package com.example.grabee

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.clickable

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToVendorRegistration: (email: String) -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = Firebase.firestore

    // State variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Customer") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("COD") }
    var dietaryPreference by remember { mutableStateOf("Halal") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val roles = listOf("Customer", "Vendor", "Rider")
    val dietaryOptions = listOf("Halal", "Non-Halal", "Show Both")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Register", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 16.dp))

        // Error message display
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = errorMessage != null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Role selection
        Text("Select Role:", style = MaterialTheme.typography.bodyLarge)
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            roles.forEach { roleOption ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { role = roleOption }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = role == roleOption,
                        onClick = { role = roleOption }
                    )
                    Text(roleOption, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Conditional fields based on role
        if (role == "Customer") {
            // Dietary Preference Selection
            Text("Dietary Preference:", style = MaterialTheme.typography.bodyLarge)
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                dietaryOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dietaryPreference = option }
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = dietaryPreference == option,
                            onClick = { dietaryPreference = option }
                        )
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Payment method selection
            Text("Payment Method:", style = MaterialTheme.typography.bodyLarge)
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPaymentMethod = "COD" }
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == "COD",
                        onClick = { selectedPaymentMethod = "COD" }
                    )
                    Text("Cash on Delivery (COD)", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.5f)
                        .padding(vertical = 4.dp)
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == "GCash",
                        onClick = { /* Disabled */ },
                        enabled = false
                    )
                    Text("GCash (Coming Soon)", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register button
        Button(
            onClick = {
                if (validateInputs(
                        email = email,
                        password = password,
                        confirmPassword = confirmPassword,
                        role = role,
                        onError = { error -> errorMessage = error }
                    )) {
                    isLoading = true
                    errorMessage = null

                    handleRegistration(
                        auth = auth,
                        db = db,
                        email = email,
                        password = password,
                        role = role,
                        selectedPaymentMethod = selectedPaymentMethod,
                        dietaryPreference = dietaryPreference,
                        onSuccess = {
                            isLoading = false
                            if (role == "Vendor") {
                                onNavigateToVendorRegistration(email)
                            } else {
                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                onNavigateToLogin()
                            }
                        },
                        onError = { error ->
                            isLoading = false
                            errorMessage = error
                            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = if (role == "Vendor") "Continue Vendor Setup" else "Register",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login navigation
        TextButton(
            onClick = onNavigateToLogin,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Already have an account? Login")
        }
    }
}

private fun validateInputs(
    email: String,
    password: String,
    confirmPassword: String,
    role: String,
    onError: (String) -> Unit
): Boolean {
    return when {
        email.isEmpty() -> "Please enter your email"
        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Please enter a valid email"
        password.isEmpty() -> "Please enter a password"
        password.length < 6 -> "Password must be at least 6 characters"
        password != confirmPassword -> "Passwords do not match"
        role.isEmpty() -> "Please select a role"
        else -> null
    }?.let {
        onError(it)
        false
    } ?: true
}

private fun handleRegistration(
    auth: FirebaseAuth,
    db: com.google.firebase.firestore.FirebaseFirestore,
    email: String,
    password: String,
    role: String,
    selectedPaymentMethod: String,
    dietaryPreference: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                val userId = auth.currentUser?.uid ?: run {
                    onError("User creation failed")
                    return@addOnCompleteListener
                }

                val userData = hashMapOf(
                    "email" to email,
                    "role" to role,
                    "registrationComplete" to (role != "Vendor"),
                    "paymentMethod" to selectedPaymentMethod.takeIf { role == "Customer" },
                    "dietaryPreference" to dietaryPreference.takeIf { role == "Customer" }
                )

                db.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { e ->
                        onError("Failed to save user data: ${e.message}")
                        auth.currentUser?.delete()
                    }
            } else {
                onError("Registration failed: ${authTask.exception?.message ?: "Unknown error"}")
            }
        }
}