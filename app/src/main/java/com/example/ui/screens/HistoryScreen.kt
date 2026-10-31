package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.PosViewModel
import com.example.data.InvoiceEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: PosViewModel) {
    val invoices by viewModel.invoices.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Paid", "Outstanding"
    var selectedInvoiceForDetails by remember { mutableStateOf<InvoiceEntity?>(null) }

    val filteredInvoices = invoices.filter { inv ->
        val matchesSearch = inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.customerName.contains(searchQuery, ignoreCase = true) ||
                inv.itemsSummary.contains(searchQuery, ignoreCase = true)
        val matchesStatus = statusFilter == "All" || inv.status.equals(statusFilter, ignoreCase = true)
        matchesSearch && matchesStatus
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Invoice History", fontWeight = FontWeight.Bold)
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by invoice # or customer...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_history_input"),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Paid", "Outstanding").forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = status },
                        label = { Text(status) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredInvoices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No matching invoices found.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredInvoices) { inv ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedInvoiceForDetails = inv }
                                .testTag("invoice_card_${inv.invoiceNumber}"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(inv.invoiceNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("₹${String.format("%.2f", inv.totalAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Customer: ${inv.customerName}", style = MaterialTheme.typography.bodyMedium)
                                Text("Items: ${inv.itemsSummary}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(dateFormatter.format(Date(inv.timestamp)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (inv.status.equals("Paid", ignoreCase = true)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = inv.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Invoice Detail Dialog
        selectedInvoiceForDetails?.let { inv ->
            AlertDialog(
                onDismissRequest = { selectedInvoiceForDetails = null },
                title = { Text("Invoice Details: ${inv.invoiceNumber}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Customer: ${inv.customerName}", style = MaterialTheme.typography.bodyLarge)
                        Text("Staff: ${inv.staffName}", style = MaterialTheme.typography.bodyMedium)
                        Text("Items: ${inv.itemsSummary}", style = MaterialTheme.typography.bodyMedium)
                        Text("Total: ₹${String.format("%.2f", inv.totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Status: ${inv.status}", style = MaterialTheme.typography.bodyMedium)
                        Text("Date: ${dateFormatter.format(Date(inv.timestamp))}", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (inv.status.equals("Outstanding", ignoreCase = true)) {
                            Button(onClick = {
                                viewModel.updateInvoiceStatus(inv.id, "Paid")
                                selectedInvoiceForDetails = null
                            }) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark as Paid")
                            }
                        }
                        OutlinedButton(onClick = {
                            viewModel.deleteInvoice(inv.id)
                            selectedInvoiceForDetails = null
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedInvoiceForDetails = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
