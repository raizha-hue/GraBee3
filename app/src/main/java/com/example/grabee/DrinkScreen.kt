package com.example.grabee

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase



@Composable
fun DrinkListScreen() {
    val drinks = remember { mutableStateListOf<Drink>() }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("drinks")
            .get()
            .addOnSuccessListener { result ->
                drinks.clear()
                for (document in result) {
                    val drink = document.toObject(Drink::class.java)
                    drinks.add(drink)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting drinks", exception)
            }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(drinks) { drink ->
            DrinkCard(drink)
        }
    }
}

@Composable
fun DrinkCard(drink: Drink) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(drink.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(drink.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Category: ${drink.category}", style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(8.dp))

            AsyncImage(
                model = drink.imageUrl,
                contentDescription = drink.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
    }
}
