package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AdminRole
import com.example.model.AdminUser
import com.example.ui.theme.PolishMaroonDark
import com.example.ui.theme.PolishPrimaryRed
import com.example.ui.theme.PolishTextDark
import com.example.ui.theme.PolishTextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartnerManagementDialog(
    adminUser: AdminUser?,
    onDismiss: () -> Unit,
    onSave: (AdminUser) -> Unit
) {
    val isNew = (adminUser == null || adminUser.id.isBlank())
    var name by remember { mutableStateOf(adminUser?.name ?: "") }
    var username by remember { mutableStateOf(adminUser?.username ?: "") }
    var phone by remember { mutableStateOf(adminUser?.phone ?: "0303-") }
    var pin by remember { mutableStateOf(adminUser?.pin ?: "") }
    var selectedRole by remember { mutableStateOf(adminUser?.role ?: AdminRole.PARTNER) }
    var isActive by remember { mutableStateOf(adminUser?.isActive ?: true) }

    // Granular Permissions
    var canManageMenu by remember { mutableStateOf(adminUser?.canManageMenu ?: true) }
    var canManageOrders by remember { mutableStateOf(adminUser?.canManageOrders ?: true) }
    var canViewReports by remember { mutableStateOf(adminUser?.canViewReports ?: true) }
    var canManageRiders by remember { mutableStateOf(adminUser?.canManageRiders ?: true) }
    var canManagePartners by remember { mutableStateOf(adminUser?.canManagePartners ?: false) }
    var canManagePayments by remember { mutableStateOf(adminUser?.canManagePayments ?: false) }
    var canManageDeals by remember { mutableStateOf(adminUser?.canManageDeals ?: true) }

    var roleDropdownExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = PolishPrimaryRed,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Column {
                        Text(
                            text = if (isNew) "Register New Partner / Admin" else "Edit Partner Account",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = PolishMaroonDark,
                                fontSize = 18.sp
                            )
                        )
                        Text(
                            text = "Multi-admin ID, custom password & role permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishTextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text("Partner Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PolishPrimaryRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Username / Login ID
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; errorMessage = null },
                    label = { Text("Unique Login ID / Username") },
                    placeholder = { Text("e.g. partner_ahmad or ali@slice") },
                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, tint = PolishPrimaryRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_username_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Phone
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it; errorMessage = null },
                    label = { Text("Phone Number (WhatsApp)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PolishPrimaryRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Password / PIN
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it; errorMessage = null },
                    label = { Text("Login Password / PIN") },
                    placeholder = { Text("Secret password for partner") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = PolishPrimaryRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PolishPrimaryRed,
                        focusedLabelColor = PolishPrimaryRed
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_pin_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = roleDropdownExpanded,
                    onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = "${selectedRole.displayName} (${selectedRole.name})",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Admin Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PolishPrimaryRed,
                            focusedLabelColor = PolishPrimaryRed
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roleDropdownExpanded,
                        onDismissRequest = { roleDropdownExpanded = false }
                    ) {
                        AdminRole.entries.forEach { role ->
                            DropdownMenuItem(
                                text = { Text("${role.displayName} - ${role.name}") },
                                onClick = {
                                    selectedRole = role
                                    roleDropdownExpanded = false
                                    // Set default permissions based on role
                                    when (role) {
                                        AdminRole.SUPER_ADMIN -> {
                                            canManageMenu = true; canManageOrders = true; canViewReports = true
                                            canManageRiders = true; canManagePartners = true; canManagePayments = true; canManageDeals = true
                                        }
                                        AdminRole.PARTNER -> {
                                            canManageMenu = true; canManageOrders = true; canViewReports = true
                                            canManageRiders = true; canManagePartners = false; canManagePayments = false; canManageDeals = true
                                        }
                                        AdminRole.MANAGER -> {
                                            canManageMenu = true; canManageOrders = true; canViewReports = true
                                            canManageRiders = true; canManagePartners = false; canManagePayments = false; canManageDeals = false
                                        }
                                        AdminRole.DISPATCHER -> {
                                            canManageMenu = false; canManageOrders = true; canViewReports = false
                                            canManageRiders = true; canManagePartners = false; canManagePayments = false; canManageDeals = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Granular Permissions Checklist
                Text(
                    text = "Granular Access Permissions (اختیارات)",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = PolishMaroonDark
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                PermissionCheckbox(
                    title = "Order Management (Accept, Kitchen, Dispatched)",
                    checked = canManageOrders,
                    onCheckedChange = { canManageOrders = it }
                )

                PermissionCheckbox(
                    title = "Menu Rates & Stock Control (Pizzas, Burgers, Deals)",
                    checked = canManageMenu,
                    onCheckedChange = { canManageMenu = it }
                )

                PermissionCheckbox(
                    title = "Analytics, Daily/Monthly Sales & PDF Reports",
                    checked = canViewReports,
                    onCheckedChange = { canViewReports = it }
                )

                PermissionCheckbox(
                    title = "Rider Fleet Management & Assigning Deliveries",
                    checked = canManageRiders,
                    onCheckedChange = { canManageRiders = it }
                )

                PermissionCheckbox(
                    title = "Banner Deals & Discounts Management",
                    checked = canManageDeals,
                    onCheckedChange = { canManageDeals = it }
                )

                PermissionCheckbox(
                    title = "Easypaisa / Payment Settings Configuration",
                    checked = canManagePayments,
                    onCheckedChange = { canManagePayments = it }
                )

                PermissionCheckbox(
                    title = "Create & Manage Other Admin/Partner Accounts",
                    checked = canManagePartners,
                    onCheckedChange = { canManagePartners = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isActive) "Account Status: Active 🟢" else "Account Status: Disabled 🔴",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PolishPrimaryRed
                        )
                    )
                }

                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = PolishPrimaryRed,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel", color = PolishTextMuted)
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || username.isBlank() || pin.isBlank()) {
                                errorMessage = "Please provide Name, Login ID, and Password."
                            } else {
                                val savedUser = AdminUser(
                                    id = adminUser?.id ?: "admin_${System.currentTimeMillis() % 100000}",
                                    username = username.trim().lowercase(),
                                    name = name.trim(),
                                    phone = phone.trim(),
                                    pin = pin.trim(),
                                    role = selectedRole,
                                    isActive = isActive,
                                    canManageMenu = canManageMenu,
                                    canManageOrders = canManageOrders,
                                    canViewReports = canViewReports,
                                    canManageRiders = canManageRiders,
                                    canManagePartners = canManagePartners,
                                    canManagePayments = canManagePayments,
                                    canManageDeals = canManageDeals
                                )
                                onSave(savedUser)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PolishPrimaryRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("partner_save_submit_btn")
                    ) {
                        Text("Save Partner 🤝", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCheckbox(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PolishPrimaryRed,
                checkmarkColor = Color.White
            )
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal,
                color = if (checked) PolishTextDark else PolishTextMuted
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
