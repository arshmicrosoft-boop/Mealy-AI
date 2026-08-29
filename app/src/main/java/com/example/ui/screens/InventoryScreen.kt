package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodState
import com.example.data.model.IngredientCategory
import com.example.data.model.InventoryItem
import com.example.ui.components.CategoryIcon
import com.example.ui.components.ExpiryUrgencyChip
import com.example.ui.components.FoodStateBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.MainTab

@Composable
fun InventoryScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.activeInventory.collectAsState()
    val filter by viewModel.inventoryFilter.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Expiring Soon", "Fridge", "Pantry", "Opened")

    val filteredItems = items.filter { item ->
        when (filter) {
            "Expiring Soon" -> item.isExpiringSoon()
            "Fridge" -> item.location.equals("Fridge", ignoreCase = true)
            "Pantry" -> item.location.equals("Pantry", ignoreCase = true)
            "Opened" -> item.state == FoodState.OPENED
            else -> true
        }
    }

    val expiringCount = items.count { it.isExpiringSoon() }
    val totalValue = items.sumOf { it.estimatedPriceDollars }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SageGreenPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .testTag("add_inventory_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Header & Summary Stats
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kitchen Inventory",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Food Waste Intelligence & Lifecycle Tracking",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        IconButton(onClick = { viewModel.selectTab(MainTab.SCAN) }) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan",
                                tint = SageGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    // Summary Banner Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${items.size} Active Items",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Estimated value: $${String.format("%.2f", totalValue)}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            if (expiringCount > 0) {
                                Surface(
                                    color = ExpiringRedContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.WarningAmber,
                                            contentDescription = null,
                                            tint = ExpiringRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "$expiringCount Expiring Soon",
                                            color = ExpiringRed,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Filter Chips
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filterOptions) { opt ->
                        val isSelected = filter == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setInventoryFilter(opt) },
                            label = { Text(opt) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageContainer,
                                selectedLabelColor = OnSageContainer
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 3. Inventory Items List
            if (filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Kitchen,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No items found in this view",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Scan your fridge or tap '+' to add items",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredItems, key = { it.id }) { item ->
                    InventoryItemCard(
                        item = item,
                        onStateChange = { newState -> viewModel.updateItemState(item.id, newState) },
                        onCookWithThis = { viewModel.selectTab(MainTab.RECIPES) },
                        onDelete = { viewModel.deleteInventoryItem(item.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddInventoryDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, cat, qty, unit, shelf, loc ->
                viewModel.addManualInventoryItem(name, cat, qty, unit, shelf, loc)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: InventoryItem,
    onStateChange: (FoodState) -> Unit,
    onCookWithThis: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .testTag("inventory_item_${item.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (item.isExpiringSoon()) ExpiringRedContainer else SageContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CategoryIcon(
                            category = item.category,
                            tint = if (item.isExpiringSoon()) ExpiringRed else SageGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${if (item.quantity % 1.0 == 0.0) item.quantity.toInt().toString() else item.quantity} ${item.unit} • ${item.location}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    FoodStateBadge(state = item.state)
                    Spacer(modifier = Modifier.height(4.dp))
                    ExpiryUrgencyChip(daysLeft = item.daysUntilExpiry())
                }

                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Cook with this") },
                            onClick = {
                                expandedMenu = false
                                onCookWithThis()
                            },
                            leadingIcon = { Icon(Icons.Default.OutdoorGrill, contentDescription = null) }
                        )
                        if (item.state != FoodState.OPENED) {
                            DropdownMenuItem(
                                text = { Text("Mark as Opened") },
                                onClick = {
                                    expandedMenu = false
                                    onStateChange(FoodState.OPENED)
                                },
                                leadingIcon = { Icon(Icons.Default.LockOpen, contentDescription = null) }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Mark as Used") },
                            onClick = {
                                expandedMenu = false
                                onStateChange(FoodState.USED)
                            },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark as Discarded", color = ExpiringRed) },
                            onClick = {
                                expandedMenu = false
                                onStateChange(FoodState.DISCARDED)
                            },
                            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = ExpiringRed) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expandedMenu = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }

            if (item.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.notes,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onAdd: (name: String, cat: IngredientCategory, qty: Double, unit: String, shelf: Int, loc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(IngredientCategory.PRODUCE) }
    var quantityText by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pcs") }
    var shelfLifeDays by remember { mutableStateOf(5) }
    var location by remember { mutableStateOf("Fridge") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Ingredient to Inventory", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient Name") },
                    placeholder = { Text("e.g. Greek Yogurt, Spinach") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (g, pcs, cups)") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = shelfLifeDays.toString(),
                        onValueChange = { shelfLifeDays = it.toIntOrNull() ?: 5 },
                        label = { Text("Shelf Life (Days)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (Fridge/Pantry)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val q = quantityText.toDoubleOrNull() ?: 1.0
                        onAdd(name, selectedCategory, q, unit, shelfLifeDays, location)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Text("Add to Inventory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
