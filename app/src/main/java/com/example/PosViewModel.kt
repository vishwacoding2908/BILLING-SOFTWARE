package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppConfigEntity
import com.example.data.InvoiceEntity
import com.example.data.MenuItemEntity
import com.example.data.PosRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PosRepository

    init {
        val dao = AppDatabase.getDatabase(application, viewModelScope).posDao()
        repository = PosRepository(dao)
    }

    val menuItems: StateFlow<List<MenuItemEntity>> = repository.menuItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val invoices: StateFlow<List<InvoiceEntity>> = repository.invoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow("pos")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _staffName = MutableStateFlow("Staff Member 1")
    val staffName: StateFlow<String> = _staffName.asStateFlow()

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminPinInput = MutableStateFlow("")
    val adminPinInput: StateFlow<String> = _adminPinInput.asStateFlow()

    private val _adminError = MutableStateFlow<String?>(null)
    val adminError: StateFlow<String?> = _adminError.asStateFlow()

    private val _config = MutableStateFlow(AppConfigEntity())
    val config: StateFlow<AppConfigEntity> = _config.asStateFlow()

    // Cart state: map of MenuItemEntity to quantity
    private val _cartItems = MutableStateFlow<Map<MenuItemEntity, Int>>(emptyMap())
    val cartItems: StateFlow<Map<MenuItemEntity, Int>> = _cartItems.asStateFlow()

    init {
        viewModelScope.launch {
            val cfg = repository.getConfig()
            if (cfg != null) {
                _config.value = cfg
            }
        }
    }

    fun setScreen(screen: String) {
        _currentScreen.value = screen
    }

    fun setStaffName(name: String) {
        _staffName.value = name
    }

    fun addToCart(item: MenuItemEntity) {
        val current = _cartItems.value.toMutableMap()
        val count = current[item] ?: 0
        current[item] = count + 1
        _cartItems.value = current
    }

    fun removeFromCart(item: MenuItemEntity) {
        val current = _cartItems.value.toMutableMap()
        val count = current[item] ?: 0
        if (count > 1) {
            current[item] = count - 1
        } else {
            current.remove(item)
        }
        _cartItems.value = current
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }

    fun proceedCheckout(onComplete: (String) -> Unit) {
        val cart = _cartItems.value
        if (cart.isEmpty()) return

        val totalAmount = cart.entries.sumOf { it.key.price * it.value }
        val itemsSummary = cart.entries.joinToString(", ") { "${it.value}x ${it.key.name}" }

        viewModelScope.launch {
            val invNumber = "INV-${Random.nextInt(1000, 9999)}"
            val invoice = InvoiceEntity(
                invoiceNumber = invNumber,
                customerName = "Walk-in Guest",
                staffName = _staffName.value,
                itemsSummary = itemsSummary,
                totalAmount = totalAmount,
                status = "Paid"
            )
            repository.insertInvoice(invoice)
            clearCart()
            onComplete(invNumber)
        }
    }

    fun setAdminPinInput(pin: String) {
        _adminPinInput.value = pin
        _adminError.value = null
    }

    fun verifyAdminPin() {
        val currentPin = _config.value.adminPin
        if (_adminPinInput.value == currentPin) {
            _isAdminLoggedIn.value = true
            _adminError.value = null
            _adminPinInput.value = ""
        } else {
            _adminError.value = "Incorrect Admin PIN. Default is 1234."
        }
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        _adminPinInput.value = ""
    }

    fun updateAdminPin(newPin: String) {
        viewModelScope.launch {
            val updated = _config.value.copy(adminPin = newPin)
            repository.saveConfig(updated)
            _config.value = updated
        }
    }

    fun updateStoreName(name: String) {
        viewModelScope.launch {
            val updated = _config.value.copy(storeName = name)
            repository.saveConfig(updated)
            _config.value = updated
        }
    }

    fun createInvoice(customerName: String, itemsSummary: String, totalAmount: Double, status: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val invNumber = "INV-${Random.nextInt(1000, 9999)}"
            val invoice = InvoiceEntity(
                invoiceNumber = invNumber,
                customerName = customerName.ifBlank { "Walk-in Guest" },
                staffName = _staffName.value,
                itemsSummary = itemsSummary,
                totalAmount = totalAmount,
                status = status
            )
            repository.insertInvoice(invoice)
            onComplete()
        }
    }

    fun updateInvoiceStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateInvoiceStatus(id, status)
        }
    }

    fun deleteInvoice(id: Long) {
        viewModelScope.launch {
            repository.deleteInvoice(id)
        }
    }

    fun addMenuItem(name: String, category: String, price: Double, imageUrl: String, description: String) {
        viewModelScope.launch {
            val item = MenuItemEntity(
                name = name,
                category = category,
                price = price,
                imageUrl = imageUrl.ifBlank { "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500" },
                description = description
            )
            repository.insertMenuItem(item)
        }
    }

    fun updateMenuItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
        }
    }

    fun deleteMenuItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMenuItem(id)
        }
    }
}
