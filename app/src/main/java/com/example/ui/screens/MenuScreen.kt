package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.PosViewModel
import com.example.data.MenuItemEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.menuItems.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<MenuItemEntity?>(null) }

    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf("Sandwich") }
    var priceInput by remember { mutableStateOf("") }
    var imageInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Menu Card Management", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    if (isAdminLoggedIn) {
                        Button(
                            onClick = {
                                editingItem = null
                                nameInput = ""
                                categoryInput = "Sandwich"
                                priceInput = ""
                                imageInput = ""
                                descInput = ""
                                showAddDialog = true
                            },
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .testTag("add_menu_item_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Item")
                        }
                    }
                }
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(16.dp)
        ) {
            if (!isAdminLoggedIn) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "🔒 Admin login required in the Settings tab to add or edit menu items.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (menuItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No menu items available.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(menuItems) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("menu_item_row_${item.name.replace(" ", "_")}"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("₹${String.format("%.2f", item.price)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                                }

                                if (isAdminLoggedIn) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = {
                                            editingItem = item
                                            nameInput = item.name
                                            categoryInput = item.category
                                            priceInput = item.price.toString()
                                            imageInput = item.imageUrl
                                            descInput = item.description
                                            showAddDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = {
                                            viewModel.deleteMenuItem(item.id)
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(if (editingItem == null) "Add Menu Item" else "Edit Menu Item") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Item Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = categoryInput,
                            onValueChange = { categoryInput = it },
                            label = { Text("Category (e.g. Sandwich, Toast, Beverage)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = priceInput,
                            onValueChange = { priceInput = it },
                            label = { Text("Price ($)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = imageInput,
                            onValueChange = { imageInput = it },
                            label = { Text("Image URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            label = { Text("Description") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        val price = priceInput.toDoubleOrNull() ?: 0.0
                        if (nameInput.isNotBlank()) {
                            if (editingItem == null) {
                                viewModel.addMenuItem(nameInput, categoryInput, price, imageInput, descInput)
                            } else {
                                viewModel.updateMenuItem(
                                    editingItem!!.copy(
                                        name = nameInput,
                                        category = categoryInput,
                                        price = price,
                                        imageUrl = imageInput.ifBlank { editingItem!!.imageUrl },
                                        description = descInput
                                    )
                                )
                            }
                            showAddDialog = false
                        }
                    }) {
                        Text("Save Item")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
                }
            )
        }
    }
}
