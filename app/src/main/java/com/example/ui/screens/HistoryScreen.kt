package com.example.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.PosViewModel
import com.example.data.InvoiceEntity
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: PosViewModel) {
    val context = LocalContext.current
    val invoices by viewModel.invoices.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") } // "All", "Paid", "Outstanding"
    var selectedInvoiceForDetails by remember { mutableStateOf<InvoiceEntity?>(null) }
    var thermalReceiptInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }

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
                        Button(onClick = {
                            val target = inv
                            selectedInvoiceForDetails = null
                            thermalReceiptInvoice = target
                        }, modifier = Modifier.testTag("print_receipt_btn")) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Print Receipt")
                        }
                        if (inv.status.equals("Outstanding", ignoreCase = true)) {
                            OutlinedButton(onClick = {
                                viewModel.updateInvoiceStatus(inv.id, "Paid")
                                selectedInvoiceForDetails = null
                            }) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mark Paid")
                            }
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

        // Thermal Printer Receipt Preview & Nearby Printer Connection Dialog
        thermalReceiptInvoice?.let { inv ->
            AlertDialog(
                onDismissRequest = { thermalReceiptInvoice = null },
                title = { Text("Thermal Receipt & Printer Connection") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Previewing layout for thermal printer. Click 'Scan & Print to Nearby Printer' to discover network/Bluetooth printers.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFFDF9)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("QUICK CAFÉ & BISTRO", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                                Text("POS Terminal #1", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("----------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text("Invoice #: ${inv.invoiceNumber}", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                                    Text("Date: ${dateFormatter.format(Date(inv.timestamp))}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                                    Text("Staff: ${inv.staffName}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                                    Text("Customer: ${inv.customerName}", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.Black)
                                    Text("----------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Gray)
                                    Text("Items:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                                    Text(inv.itemsSummary, fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.Black)
                                    Text("----------------------------------------", fontFamily = FontFamily.Monospace, fontSize = 9.sp, color = Color.Gray)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("TOTAL:", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                        Text("₹${String.format("%.2f", inv.totalAmount)}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Thank You! Visit Again", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color.DarkGray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
                            val jobName = "QuickBill Receipt ${inv.invoiceNumber}"
                            printManager.print(
                                jobName,
                                ReceiptPrintAdapter(context, inv, dateFormatter, "QUICK CAFÉ & BISTRO"),
                                null
                            )
                            thermalReceiptInvoice = null
                        },
                        modifier = Modifier.testTag("scan_and_print_btn")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan & Print to Nearby Printer")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { thermalReceiptInvoice = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

class ReceiptPrintAdapter(
    private val context: Context,
    private val invoice: InvoiceEntity,
    private val dateFormatter: SimpleDateFormat,
    private val storeName: String
) : PrintDocumentAdapter() {

    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }
        val builder = PrintDocumentInfo.Builder("Receipt-${invoice.invoiceNumber}.pdf")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(1)
        callback?.onLayoutFinished(builder.build(), true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(400, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        val boldPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }

        var y = 30f
        canvas.drawText(storeName, 30f, y, boldPaint)
        y += 20f
        canvas.drawText("POS Terminal #1", 30f, y, paint)
        y += 20f
        canvas.drawText("------------------------------------------------", 30f, y, paint)
        y += 20f
        canvas.drawText("Invoice #: ${invoice.invoiceNumber}", 30f, y, paint)
        y += 20f
        canvas.drawText("Date: ${dateFormatter.format(Date(invoice.timestamp))}", 30f, y, paint)
        y += 20f
        canvas.drawText("Staff: ${invoice.staffName}", 30f, y, paint)
        y += 20f
        canvas.drawText("Customer: ${invoice.customerName}", 30f, y, paint)
        y += 20f
        canvas.drawText("------------------------------------------------", 30f, y, paint)
        y += 20f
        canvas.drawText("Items Purchased:", 30f, y, boldPaint)
        y += 20f
        canvas.drawText(invoice.itemsSummary, 30f, y, paint)
        y += 30f
        canvas.drawText("------------------------------------------------", 30f, y, paint)
        y += 20f
        canvas.drawText("TOTAL AMOUNT: ₹${String.format("%.2f", invoice.totalAmount)}", 30f, y, boldPaint)
        y += 30f
        canvas.drawText("Thank You! Please Visit Again", 30f, y, paint)

        pdfDocument.finishPage(page)

        try {
            destination?.let {
                pdfDocument.writeTo(FileOutputStream(it.fileDescriptor))
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.localizedMessage)
        } finally {
            pdfDocument.close()
        }
    }
}
