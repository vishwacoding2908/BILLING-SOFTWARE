package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.PosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: PosViewModel) {
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()
    val adminPinInput by viewModel.adminPinInput.collectAsState()
    val adminError by viewModel.adminError.collectAsState()
    val config by viewModel.config.collectAsState()

    var newPinInput by remember { mutableStateOf("") }
    var storeNameInput by remember { mutableStateOf(config.storeName) }
    var updateSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Keep storeNameInput synced
    LaunchedEffect(config.storeName) {
        storeNameInput = config.storeName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("System Settings & Admin", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingVals)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!isAdminLoggedIn) {
                // Admin Lockout Screen / PIN Entry
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Secure Admin Access", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(
                            "Enter the admin PIN to manage system settings, store name, and menu items.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = adminPinInput,
                            onValueChange = { viewModel.setAdminPinInput(it) },
                            label = { Text("Admin PIN (Default: 1234)") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_pin_input"),
                            singleLine = true
                        )

                        if (adminError != null) {
                            Text(adminError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        Button(
                            onClick = { viewModel.verifyAdminPin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("admin_login_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Admin Panel")
                        }
                    }
                }
            } else {
                // Unlocked Admin Panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔒 Admin Panel Unlocked", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            OutlinedButton(onClick = { viewModel.logoutAdmin() }) {
                                Text("Lock / Logout")
                            }
                        }

                        Divider()

                        Text("Store Settings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = storeNameInput,
                            onValueChange = { storeNameInput = it },
                            label = { Text("Store Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                viewModel.updateStoreName(storeNameInput)
                                updateSuccessMessage = "Store name updated successfully!"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Store Name")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Security & PIN", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { newPinInput = it },
                            label = { Text("New Admin PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (newPinInput.isNotBlank()) {
                                    viewModel.updateAdminPin(newPinInput)
                                    newPinInput = ""
                                    updateSuccessMessage = "Admin PIN updated successfully!"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Update Admin PIN")
                        }
                    }
                }
            }
        }

        updateSuccessMessage?.let { msg ->
            AlertDialog(
                onDismissRequest = { updateSuccessMessage = null },
                title = { Text("Settings Updated") },
                text = { Text(msg) },
                confirmButton = {
                    Button(onClick = { updateSuccessMessage = null }) { Text("OK") }
                }
            )
        }
    }
}
