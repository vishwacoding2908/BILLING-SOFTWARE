package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val category: String, // e.g. "Sandwich", "Toast", "Beverage", "Snack"
    val price: Double,
    val imageUrl: String,
    val description: String,
    val isAvailable: Boolean = true
)
