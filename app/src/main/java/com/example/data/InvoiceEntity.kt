package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val invoiceNumber: String,
    val customerName: String,
    val staffName: String,
    val itemsSummary: String,
    val totalAmount: Double,
    val status: String, // "Paid" or "Outstanding"
    val timestamp: Long = System.currentTimeMillis()
)
