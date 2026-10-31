package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_config")
data class AppConfigEntity(
    @PrimaryKey val id: Int = 1,
    val adminPin: String = "1234",
    val storeName: String = "Quick Café & Bistro",
    val taxRate: Double = 5.0
)
