package com.example.data

import kotlinx.coroutines.flow.Flow

class PosRepository(private val dao: PosDao) {
    val menuItems: Flow<List<MenuItemEntity>> = dao.getAllMenuItems()
    val invoices: Flow<List<InvoiceEntity>> = dao.getAllInvoices()

    suspend fun insertMenuItem(item: MenuItemEntity) = dao.insertMenuItem(item)
    suspend fun updateMenuItem(item: MenuItemEntity) = dao.updateMenuItem(item)
    suspend fun deleteMenuItem(id: Long) = dao.deleteMenuItem(id)

    suspend fun insertInvoice(invoice: InvoiceEntity) = dao.insertInvoice(invoice)
    suspend fun updateInvoiceStatus(id: Long, status: String) = dao.updateInvoiceStatus(id, status)
    suspend fun deleteInvoice(id: Long) = dao.deleteInvoice(id)

    suspend fun getConfig(): AppConfigEntity? = dao.getConfig()
    suspend fun saveConfig(config: AppConfigEntity) = dao.saveConfig(config)
}
