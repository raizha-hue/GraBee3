package com.example.grabee

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantListScreen() {
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth
    val currentUser = auth.currentUser

    var restaurants by remember { mutableStateOf<List<Restaurant>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var userDietaryPreference by remember { mutableStateOf("Show Both") }

    // Fetch user preference and restaurants
    LaunchedEffect(Unit) {
        currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    document.getString("dietaryPreference")?.let { preference ->
                        userDietaryPreference = preference
                    }
                    loadRestaurants(db, userDietaryPreference,
                        onSuccess = { restaurants = it },
                        onError = { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
        } ?: loadRestaurants(db, "Show Both",
            onSuccess = { restaurants = it },
            onError = { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Restaurants",
            style = MaterialTheme.typography.headlineMedium
        )

        // Using standard RadioButtons for preference selection
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            listOf("Halal", "Non-Halal", "Show Both").forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = (userDietaryPreference == option),
                            onValueChange = {
                                userDietaryPreference = option
                                loadRestaurants(db, option,
                                    onSuccess = { restaurants = it },
                                    onError = { e ->
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            role = Role.RadioButton
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (userDietaryPreference == option),
                        onClick = null
                    )
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            restaurants.isEmpty() -> Text(
                text = "No matching restaurants found",
                style = MaterialTheme.typography.bodyMedium
            )
            else -> LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(restaurants) { restaurant ->
                    RestaurantCard(restaurant = restaurant)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantCard(restaurant: Restaurant) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = restaurant.address,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (restaurant.isHalalCertified) {
                Text(
                    text = "✅ Halal Certified",
                    color = Color(0xFF388E3C),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

data class Restaurant(
    val id: String = "",
    val name: String = "",
    val address: String = "",
    val isHalalCertified: Boolean = false
)

fun loadRestaurants(
    db: com.google.firebase.firestore.FirebaseFirestore,
    preference: String,
    onSuccess: (List<Restaurant>) -> Unit,
    onError: (Exception) -> Unit
) {
    val query = when (preference) {
        "Halal" -> db.collection("restaurants").whereEqualTo("isHalalCertified", true)
        "Non-Halal" -> db.collection("restaurants").whereEqualTo("isHalalCertified", false)
        else -> db.collection("restaurants")
    }

    query.get()
        .addOnSuccessListener { result ->
            val restaurants = result.documents.map { doc ->
                Restaurant(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    address = doc.getString("address") ?: "",
                    isHalalCertified = doc.getBoolean("isHalalCertified") ?: false
                )
            }
            onSuccess(restaurants)
        }
        .addOnFailureListener { exception ->
            onError(exception)
        }
}