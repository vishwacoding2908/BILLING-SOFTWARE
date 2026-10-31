package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PosDao {
    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity)

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: Long)

    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Query("UPDATE invoices SET status = :status WHERE id = :id")
    suspend fun updateInvoiceStatus(id: Long, status: String)

    @Query("DELETE FROM invoices WHERE id = :id")
    suspend fun deleteInvoice(id: Long)

    @Query("SELECT * FROM app_config WHERE id = 1")
    suspend fun getConfig(): AppConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AppConfigEntity)
}
