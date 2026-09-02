package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.ai.vision.MockImageAnalysisHandler
import com.example.data.ai.vision.ScanTargetMode
import com.example.data.ai.vision.ScannedBarcodeProduct
import com.example.data.ai.vision.ScannedLabelOcrResult
import com.example.data.ai.vision.VisionImageAnalysisResult
import com.example.data.model.IngredientCategory
import com.example.ui.theme.*
import com.example.ui.viewmodel.KitchenViewModel
import com.example.util.rememberCameraPermissionState

/**
 * Camera Scanner Dialog for scanning ingredient barcodes (UPC/EAN) or product labels.
 * Integrates directly with MockImageAnalysisHandler for future AI Vision pipelines.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeAndLabelScannerSheet(
    viewModel: KitchenViewModel,
    onDismiss: () -> Unit,
    onItemAdded: (String) -> Unit = {}
) {
    val cameraPermissionState = rememberCameraPermissionState()
    val isAnalyzing by viewModel.isAnalyzingImage.collectAsState()
    val analysisResult by viewModel.imageAnalysisResult.collectAsState()
    val activeMode by viewModel.activeScanTargetMode.collectAsState()

    var customBarcodeInput by remember { mutableStateOf("") }
    var flashEnabled by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf("Fridge") }
    var customQuantity by remember { mutableStateOf(1.0) }

    // Quick Sample Barcodes from Mock Handler
    val sampleProducts = remember {
        MockImageAnalysisHandler().getAllSampleBarcodes()
    }

    // Laser Animation for Camera Viewfinder
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    // Trigger initial scan on open
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.hasPermission) {
            cameraPermissionState.requestPermission()
        }
        if (analysisResult == null) {
            viewModel.scanBarcodeOrLabel(activeMode)
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.clearImageAnalysisResult()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("barcode_scanner_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.clearImageAnalysisResult()
                            onDismiss()
                        },
                        modifier = Modifier.testTag("close_scanner_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Smart Camera Scanner",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Barcodes & Ingredient Labels",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    IconButton(
                        onClick = { flashEnabled = !flashEnabled },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (flashEnabled) SageContainer else Color.Transparent)
                            .testTag("toggle_flash_button")
                    ) {
                        Icon(
                            imageVector = if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (flashEnabled) SageGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    // Mode Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeMode == ScanTargetMode.BARCODE,
                            onClick = {
                                viewModel.setScanTargetMode(ScanTargetMode.BARCODE)
                                viewModel.scanBarcodeOrLabel(ScanTargetMode.BARCODE)
                            },
                            label = { Text("Barcode (UPC/EAN)") },
                            leadingIcon = {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageContainer,
                                selectedLabelColor = OnSageContainer
                            ),
                            modifier = Modifier.weight(1f).testTag("select_mode_barcode")
                        )

                        FilterChip(
                            selected = activeMode == ScanTargetMode.LABEL_OCR,
                            onClick = {
                                viewModel.setScanTargetMode(ScanTargetMode.LABEL_OCR)
                                viewModel.scanBarcodeOrLabel(ScanTargetMode.LABEL_OCR)
                            },
                            label = { Text("Label & Expiry OCR") },
                            leadingIcon = {
                                Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SageContainer,
                                selectedLabelColor = OnSageContainer
                            ),
                            modifier = Modifier.weight(1f).testTag("select_mode_label_ocr")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Camera Viewfinder Box
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .testTag("camera_viewfinder_box"),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.fridge_scan_hero_1787938924629),
                                contentDescription = "Camera Viewfinder Feed",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )

                            // Dark Tint Overlay
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.35f))
                            )

                            // Viewfinder Reticle Frame
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(width = 250.dp, height = if (activeMode == ScanTargetMode.BARCODE) 130.dp else 170.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (isAnalyzing) SageGreenPrimary else Color.White.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .background(Color.White.copy(alpha = 0.05f))
                            ) {
                                // Animated Laser Bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.5.dp)
                                        .padding(horizontal = 8.dp)
                                        .align(Alignment.TopCenter)
                                        .offset(y = ((if (activeMode == ScanTargetMode.BARCODE) 130 else 170) * laserPosition).dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color.Transparent,
                                                    SageGreenPrimary,
                                                    Color.White,
                                                    SageGreenPrimary,
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                Text(
                                    text = if (activeMode == ScanTargetMode.BARCODE) "Align Barcode in Frame" else "Align Ingredient / Expiry Label",
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 6.dp)
                                )
                            }

                            // Viewfinder Header Status Badge
                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (isAnalyzing) Color.Yellow else FreshGreen,
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        text = if (isAnalyzing) "Mock AI Vision Processing..." else "Live Sensor Ready",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }

                            // Retake / Snapshot Button on bottom center of camera
                            Button(
                                onClick = { viewModel.scanBarcodeOrLabel(activeMode) },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp)
                                    .testTag("snap_and_analyze_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Capture Frame",
                                    color = SageGreenPrimary,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick-Test Barcode Demo Selector (Simulates real barcode scanner triggers)
                    if (activeMode == ScanTargetMode.BARCODE) {
                        Text(
                            text = "Tap a Sample Barcode to Test Vision Pipeline:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sampleProducts) { product ->
                                val isSelected = analysisResult?.scannedBarcode?.barcode == product.barcode
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) SageContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, SageGreenPrimary) else null,
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.scanBarcodeOrLabel(ScanTargetMode.BARCODE, barcodeInput = product.barcode)
                                        }
                                        .testTag("sample_barcode_${product.barcode}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QrCode,
                                            contentDescription = null,
                                            tint = if (isSelected) SageGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Column {
                                            Text(
                                                text = product.productName.take(18) + if (product.productName.length > 18) "…" else "",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) OnSageContainer else MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = product.barcode,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp, fontFamily = FontFamily.Monospace),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Custom Barcode input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customBarcodeInput,
                                onValueChange = { customBarcodeInput = it },
                                placeholder = { Text("Enter UPC/EAN (e.g., 011110038331)", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("custom_barcode_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                            Button(
                                onClick = {
                                    if (customBarcodeInput.isNotBlank()) {
                                        viewModel.scanBarcodeOrLabel(ScanTargetMode.BARCODE, barcodeInput = customBarcodeInput)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary),
                                modifier = Modifier.testTag("lookup_barcode_button")
                            ) {
                                Text("Lookup")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Analysis Result Presentation Card
                    val barcode = analysisResult?.scannedBarcode
                    val label = analysisResult?.scannedLabel

                    if (isAnalyzing) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = SageGreenPrimary,
                                    strokeWidth = 3.dp
                                )
                                Column {
                                    Text(
                                        text = "AI Image Analysis Handler Running...",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Extracting OCR text, UPC tokens & shelf-life predictions",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    } else if (barcode != null && activeMode == ScanTargetMode.BARCODE) {
                        ScannedBarcodeCard(
                            barcode = barcode,
                            selectedLocation = selectedLocation,
                            onLocationChanged = { selectedLocation = it },
                            onAdd = {
                                viewModel.addScannedBarcodeToInventory(
                                    barcodeProduct = barcode,
                                    customQuantity = customQuantity,
                                    location = selectedLocation
                                ) {
                                    onItemAdded(barcode.productName)
                                    onDismiss()
                                }
                            }
                        )
                    } else if (label != null && activeMode == ScanTargetMode.LABEL_OCR) {
                        ScannedLabelCard(
                            label = label,
                            selectedLocation = selectedLocation,
                            onLocationChanged = { selectedLocation = it },
                            onAdd = {
                                viewModel.addScannedLabelToInventory(
                                    labelResult = label,
                                    quantity = 1.0,
                                    unit = "pkg",
                                    location = selectedLocation
                                ) {
                                    onItemAdded(label.detectedProductName)
                                    onDismiss()
                                }
                            }
                        )
                    } else {
                        // Empty / Ready state
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = SageGreenPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Ready for Barcode or Label Scan",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Point camera at food packaging, barcode, or expiration text.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun ScannedBarcodeCard(
    barcode: ScannedBarcodeProduct,
    selectedLocation: String,
    onLocationChanged: (String) -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scanned_barcode_result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SageGreenPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = SageContainer
                ) {
                    Text(
                        text = "✓ Barcode Match (${(barcode.confidence * 100).toInt()}%)",
                        color = OnSageContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Text(
                    text = "UPC: ${barcode.barcode}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = barcode.productName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Brand: ${barcode.brand} • ${barcode.unit}",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Metadata Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberContainer
                ) {
                    Text(
                        text = "⏳ Shelf Life: ~${barcode.estimatedShelfLifeDays} Days",
                        color = OnAmberContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "📦 ${barcode.category.name}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (barcode.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = barcode.notes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Location Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Storage Location:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Fridge", "Pantry", "Freezer").forEach { loc ->
                        val isSel = selectedLocation == loc
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SageGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onLocationChanged(loc) }
                        ) {
                            Text(
                                text = loc,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Button
            Button(
                onClick = onAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_scanned_barcode_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add to $selectedLocation",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun ScannedLabelCard(
    label: ScannedLabelOcrResult,
    selectedLocation: String,
    onLocationChanged: (String) -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scanned_label_result_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, SageGreenPrimary.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SageContainer
            ) {
                Text(
                    text = "✓ OCR Label Extracted (${(label.confidence * 100).toInt()}%)",
                    color = OnSageContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = label.detectedProductName,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            if (!label.detectedExpiryDate.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ExpiringRedContainer
                ) {
                    Text(
                        text = "📅 ${label.detectedExpiryDate} (est. ${label.calculatedShelfLifeDays} days)",
                        color = ExpiringRed,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            if (label.detectedAllergens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Allergens: ${label.detectedAllergens.joinToString()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Raw OCR Output snippet
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label.rawText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Location Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Storage Location:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Fridge", "Pantry", "Freezer").forEach { loc ->
                        val isSel = selectedLocation == loc
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) SageGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onLocationChanged(loc) }
                        ) {
                            Text(
                                text = loc,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add Button
            Button(
                onClick = onAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("add_scanned_label_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SageGreenPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add to $selectedLocation",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
