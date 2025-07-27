package com.example.grabee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDashboardScreen(
    onLogout: () -> Unit,          // Navigation callback
    onNavigateToDetails: () -> Unit // Navigation callback
) {
    var foodList by remember { mutableStateOf<List<Food>>(emptyList()) }
    var ecoPoints by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }

    // Real-time eco points listener
    LaunchedEffect(Unit) {
        Firebase.auth.currentUser?.uid?.let { userId ->
            Firebase.firestore.collection("customers")
                .document(userId)
                .addSnapshotListener { snapshot, _ ->
                    snapshot?.getLong("ecoPoints")?.let { points ->
                        ecoPoints = points.toInt()
                    }
                }
        }
    }

    // In CustomerDashboardScreen.kt - replace the document mapping code
    LaunchedEffect(retryCount) {
        try {
            val querySnapshot = Firebase.firestore.collection("foods")
                .get()
                .await()

            foodList = querySnapshot.documents.map { document ->
                Food(
                    id = document.id,
                    name = document.getString("name") ?: "",
                    description = document.getString("description") ?: "",
                    price = document.getDouble("price") ?: 0.0,
                    imageURL = document.getString("imageURL") ?: ""
                )
            }
        } catch (e: Exception) {
            error = "Failed to load menu: ${e.localizedMessage}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Dashboard") },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Eco Points",
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$ecoPoints",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        Spacer(Modifier.width(16.dp))
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(
                            onClick = { retryCount++ },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
                foodList.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No food items available")
                            Text(
                                "You have $ecoPoints Eco Points",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Eco Points Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFE8F5E9),
                                contentColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        "Your Eco Points",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "$ecoPoints pts",
                                        style = MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = "Eco Points",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Personal Details Button
                        Button(
                            onClick = onNavigateToDetails,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            )
                        ) {
                            Text("Edit Personal Details", color = Color.White)
                        }

                        Text(
                            text = "Our Menu",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(foodList) { food ->
                                FoodItemCard(food)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemCard(food: Food) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            AsyncImage(
                model = food.imageURL,
                contentDescription = food.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(food.name, style = MaterialTheme.typography.titleLarge)
            Text("₱${"%.2f".format(food.price)}", style = MaterialTheme.typography.titleMedium)
            Text(food.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}