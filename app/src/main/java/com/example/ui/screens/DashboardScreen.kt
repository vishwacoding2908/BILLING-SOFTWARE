package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: PosViewModel) {
    val invoices by viewModel.invoices.collectAsState()

    val totalRevenue = invoices.filter { it.status.equals("Paid", ignoreCase = true) }.sumOf { it.totalAmount }
    val outstandingInvoices = invoices.filter { it.status.equals("Outstanding", ignoreCase = true) }
    val totalOutstandingAmount = outstandingInvoices.sumOf { it.totalAmount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Billing Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingVals ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Top Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        title = "Total Revenue",
                        value = "₹${String.format("%.2f", totalRevenue)}",
                        icon = Icons.Default.AttachMoney,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f).testTag("card_total_revenue")
                    )
                    MetricCard(
                        title = "Outstanding",
                        value = "₹${String.format("%.2f", totalOutstandingAmount)}",
                        icon = Icons.Default.ErrorOutline,
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.weight(1f).testTag("card_outstanding")
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Business Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Total Invoices Generated: ${invoices.size}", style = MaterialTheme.typography.bodyMedium)
                        Text("Paid Invoices: ${invoices.count { it.status.equals("Paid", ignoreCase = true) }}", style = MaterialTheme.typography.bodyMedium)
                        Text("Outstanding Invoices: ${outstandingInvoices.size}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (invoices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No invoices recorded yet. Start billing from the POS tab!", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(invoices.take(10)) { invoice ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(invoice.invoiceNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Customer: ${invoice.customerName} • Staff: ${invoice.staffName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.itemsSummary, style = MaterialTheme.typography.bodyMedium)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("₹${String.format("%.2f", invoice.totalAmount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (invoice.status.equals("Paid", ignoreCase = true)) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = invoice.status,
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
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
