package com.example.grabee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.compose.ui.Alignment

@Composable
fun AdminApprovalScreen() {
    val db = Firebase.firestore
    var pendingVendors by remember { mutableStateOf<List<Vendor>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // Fetch pending vendors
    LaunchedEffect(Unit) {
        db.collection("vendors")
            .whereEqualTo("isHalalApproved", false)
            .get()
            .addOnSuccessListener { result ->
                pendingVendors = result.documents.map { doc ->
                    Vendor(
                        id = doc.id,
                        restaurantName = doc.getString("restaurantName") ?: "",
                        address = doc.getString("address") ?: "",
                        halalCertUrl = doc.getString("halalCertUrl") ?: "",
                        ownerId = doc.getString("ownerId") ?: ""
                    )
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pendingVendors) { vendor ->
                VendorApprovalCard(vendor = vendor)
                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorApprovalCard(vendor: Vendor) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = vendor.restaurantName,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = vendor.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Halal Certificate:",
                style = MaterialTheme.typography.titleMedium
            )
            AsyncImage(
                model = vendor.halalCertUrl,
                contentDescription = "Halal Certificate",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { approveVendor(vendor.id, false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Text("Reject")
                }

                Button(
                    onClick = { approveVendor(vendor.id, true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Approve")
                }
            }
        }
    }
}

private fun approveVendor(vendorId: String, approved: Boolean) {
    Firebase.firestore.collection("vendors").document(vendorId)
        .update("isHalalApproved", approved)
        .addOnSuccessListener {
            // Optional: Notify vendor via FCM or email
        }
}

data class Vendor(
    val id: String,
    val restaurantName: String,
    val address: String,
    val halalCertUrl: String,
    val ownerId: String
)