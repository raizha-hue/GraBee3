package com.example.grabee

import android.net.Uri
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    onItemAdded: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: VendorViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    // Form states
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var isHalal by remember { mutableStateOf(true) }
    var isAvailable by remember { mutableStateOf(true) }
    var foodImageUri by remember { mutableStateOf<Uri?>(null) }
    var showCategories by remember { mutableStateOf(false) }

    // ViewModel states
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> foodImageUri = uri }

    val foodCategories = listOf(
        "Main Course", "Appetizer", "Dessert",
        "Beverage", "Side Dish", "Specialty"
    )

    // Handle errors
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Food Item") },
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
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Food Image Section
            Text("Food Image", style = MaterialTheme.typography.headlineSmall)

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Upload Food Image")
            }

            foodImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Food Image Preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Food Details
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Food Name*") },
                modifier = Modifier.fillMaxWidth(),
                isError = name.isNotEmpty() && name.length < 3,
                supportingText = {
                    if (name.isNotEmpty() && name.length < 3) {
                        Text("Minimum 3 characters")
                    }
                }
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

            // Category Dropdown
            Box {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategories = true },
                    readOnly = true
                )
                DropdownMenu(
                    expanded = showCategories,
                    onDismissRequest = { showCategories = false }
                ) {
                    foodCategories.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                category = item
                                showCategories = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price*") },
                modifier = Modifier.fillMaxWidth(),
                isError = price.isNotEmpty() && price.toDoubleOrNull() == null,
                supportingText = {
                    if (price.isNotEmpty() && price.toDoubleOrNull() == null) {
                        Text("Enter valid price (e.g. 9.99)")
                    }
                }
            )

            // Food Attributes
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isHalal,
                    onCheckedChange = { isHalal = it }
                )
                Text("Halal Certified")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = isAvailable,
                    onCheckedChange = { isAvailable = it }
                )
                Text("Available Now")
            }

            // Submit Button
            Button(
                onClick = {
                    firebaseUser?.uid?.let { userId ->
                        viewModel.addFoodItem(
                            vendorId = userId,
                            name = name,
                            description = description,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = category,
                            isHalal = isHalal,
                            isAvailable = isAvailable,
                            imageUri = foodImageUri ?: return@Button,
                            onSuccess = onItemAdded
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = name.length >= 3 &&
                        price.toDoubleOrNull() != null &&
                        foodImageUri != null &&
                        firebaseUser != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text("Add Food Item", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (firebaseUser == null) {
                Text(
                    "Please login as a vendor to add food items",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}