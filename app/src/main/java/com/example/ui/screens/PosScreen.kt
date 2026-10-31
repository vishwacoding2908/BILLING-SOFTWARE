package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
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
fun PosScreen(viewModel: PosViewModel) {
    val menuItems by viewModel.menuItems.collectAsState()
    val staffName by viewModel.staffName.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var staffEditing by remember { mutableStateOf(false) }
    var tempStaffName by remember { mutableStateOf(staffName) }
    var showCartDialog by remember { mutableStateOf(false) }
    var billSuccessMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("All", "Sandwich", "Toast", "Beverage", "Snack")

    val filteredItems = menuItems.filter { item ->
        val matchesCategory = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || item.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    val totalCartItemsCount = cartItems.values.sum()
    val totalCartAmount = cartItems.entries.sumOf { it.key.price * it.value }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fastfood, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Quick POS & Staff Entry", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { staffEditing = true }
                            .testTag("staff_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(staffName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (totalCartItemsCount > 0) {
                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showCartDialog = true }
                        ) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("$totalCartItemsCount Items in Order", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text("Total: ₹${String.format("%.2f", totalCartAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.proceedCheckout { invNum ->
                                    billSuccessMessage = "Invoice $invNum billed successfully!"
                                }
                            },
                            modifier = Modifier.testTag("proceed_checkout_btn")
                        ) {
                            Text("Proceed & Bill")
                        }
                    }
                }
            }
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search menu items...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_menu_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categories
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        modifier = Modifier.testTag("chip_$cat")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No menu items found. Add items in the Menu tab.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems) { item ->
                        val qty = cartItems[item] ?: 0
                        MenuItemCard(
                            item = item,
                            quantity = qty,
                            onItemClick = {
                                viewModel.addToCart(item)
                            }
                        )
                    }
                }
            }
        }

        // Staff Name Edit Dialog
        if (staffEditing) {
            AlertDialog(
                onDismissRequest = { staffEditing = false },
                title = { Text("Change Staff Member") },
                text = {
                    OutlinedTextField(
                        value = tempStaffName,
                        onValueChange = { tempStaffName = it },
                        label = { Text("Staff Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (tempStaffName.isNotBlank()) {
                            viewModel.setStaffName(tempStaffName)
                        }
                        staffEditing = false
                    }) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { staffEditing = false }) { Text("Cancel") }
                }
            )
        }

        // Cart Review Dialog (when tapping cart summary)
        if (showCartDialog) {
            AlertDialog(
                onDismissRequest = { showCartDialog = false },
                title = { Text("Current Order Summary") },
                text = {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        items(cartItems.toList()) { (item, qty) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, fontWeight = FontWeight.Bold)
                                    Text("₹${String.format("%.2f", item.price)} each", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.removeFromCart(item) }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                    }
                                    Text("$qty", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { viewModel.addToCart(item) }) {
                                        Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showCartDialog = false
                            viewModel.proceedCheckout { invNum ->
                                billSuccessMessage = "Invoice $invNum billed successfully!"
                            }
                        }
                    ) {
                        Text("Proceed & Bill (₹${String.format("%.2f", totalCartAmount)})")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCartDialog = false }) { Text("Close") }
                }
            )
        }

        // Success Dialog
        billSuccessMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { billSuccessMessage = null },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Instant Billing Saved") },
                text = { Text(msg) },
                confirmButton = {
                    Button(onClick = { billSuccessMessage = null }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun MenuItemCard(item: MenuItemEntity, quantity: Int, onItemClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
            .testTag("menu_card_${item.name.replace(" ", "_")}")
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(item.category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("₹${String.format("%.2f", item.price)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                }
            }

            if (quantity > 0) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = "$quantity",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
