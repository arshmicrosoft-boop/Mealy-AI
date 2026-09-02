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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodState
import com.example.data.model.IngredientCategory
import com.example.data.model.InventoryItem
import com.example.ui.components.BarcodeAndLabelScannerSheet
import com.example.ui.components.CategoryIcon
import com.example.ui.components.ExpiryUrgencyChip
import com.example.ui.components.FoodStateBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel
import com.example.ui.viewmodel.MainTab
import com.example.util.rememberCameraPermissionState
import kotlinx.coroutines.launch

@Composable
fun InventoryScreen(
    viewModel: KitchenViewModel,
    modifier: Modifier = Modifier
) {
    val items by viewModel.activeInventory.collectAsState()
    val filter by viewModel.inventoryFilter.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showScannerSheet by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberCameraPermissionState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

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

    if (cameraPermissionState.showRationaleDialog) {
        AlertDialog(
            onDismissRequest = cameraPermissionState.dismissRationale,
            title = { Text("Camera Permission Needed") },
            text = { Text("Mealio needs camera access to scan barcodes and ingredient packaging labels.") },
            confirmButton = {
                TextButton(onClick = {
                    cameraPermissionState.dismissRationale()
                    cameraPermissionState.requestPermission()
                }) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = cameraPermissionState.dismissRationale) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("inventory_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                // Primary: Camera Barcode & Label Scanner FAB
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!cameraPermissionState.hasPermission) {
                            cameraPermissionState.requestPermission()
                        }
                        showScannerSheet = true
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan Barcode or Label",
                            tint = Color.White
                        )
                    },
                    text = {
                        Text(
                            text = "Scan Barcode / Label",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    containerColor = SageGreenPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("scan_barcode_fab")
                )

                // Secondary: Manual Add Item FAB
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("add_inventory_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Item Manually",
                        modifier = Modifier.size(22.dp)
                    )
                }
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

            // 3. Inventory Items List or Empty State
            if (items.isEmpty()) {
                item {
                    InventoryEmptyState(
                        onScanClick = { viewModel.selectTab(MainTab.SCAN) },
                        onAddManualClick = { showAddDialog = true },
                        onLoadStarterPantry = { viewModel.loadStarterPantry() }
                    )
                }
            } else if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 24.dp)
                            .testTag("filter_empty_state"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterListOff,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No items matching \"$filter\"",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try changing the filter or clearing it to see all ${items.size} tracked items.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            FilledTonalButton(
                                onClick = { viewModel.setInventoryFilter("All") },
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = SageContainer)
                            ) {
                                Text("Show All Items", color = OnSageContainer, fontWeight = FontWeight.Bold)
                            }
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

    if (showScannerSheet) {
        BarcodeAndLabelScannerSheet(
            viewModel = viewModel,
            onDismiss = { showScannerSheet = false },
            onItemAdded = { itemName ->
                showScannerSheet = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Added '$itemName' to inventory")
                }
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

@Composable
fun InventoryEmptyState(
    onScanClick: () -> Unit,
    onAddManualClick: () -> Unit,
    onLoadStarterPantry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("inventory_empty_state"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Scanner Visual Badge
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SageContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan to get started",
                    tint = SageGreenPrimary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Your Kitchen Inventory is Empty",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Mealio tracks shelf life, predicts food waste, and calculates real-time recipe matches based on what's in your fridge and pantry.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Primary CTA: Scan Items
            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("scan_to_get_started_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Scan Items with Smart Vision",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Action Row: Manual Add & Demo Pantry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onAddManualClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("add_item_manual_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Manually", style = MaterialTheme.typography.labelLarge)
                }

                FilledTonalButton(
                    onClick = onLoadStarterPantry,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("load_starter_pantry_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = SageContainer)
                ) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        tint = OnSageContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Demo Pantry", color = OnSageContainer, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(16.dp))

            // Value Props
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ValuePropositionRow(
                    icon = Icons.Default.QrCodeScanner,
                    title = "Instant Camera Detection",
                    subtitle = "Snap a photo of your shelves or receipt for fast stocking."
                )
                ValuePropositionRow(
                    icon = Icons.Default.Timer,
                    title = "Dynamic Expiry Intelligence",
                    subtitle = "Get alerted before ingredients spoil to cut grocery waste."
                )
                ValuePropositionRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Personalized Gemini AI Recipes",
                    subtitle = "Generate customized dishes matching your active pantry stock."
                )
            }
        }
    }
}

@Composable
private fun ValuePropositionRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SageContainer.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SageGreenPrimary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            )
        }
    }
}

