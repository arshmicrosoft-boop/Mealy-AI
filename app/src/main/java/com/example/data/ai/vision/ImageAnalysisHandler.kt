package com.example.data.ai.vision

import com.example.data.model.DetectedFoodItem
import com.example.data.model.IngredientCategory
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class ScanTargetMode(val label: String, val description: String) {
    BARCODE("Barcode (UPC/EAN)", "Scan package barcodes for instant product & shelf-life lookup"),
    LABEL_OCR("Ingredient & Expiry Label", "Analyze printed packaging text, expiry dates, and nutrition"),
    SMART_FRIDGE_VISION("Multi-Item Shelf Vision", "Detect multiple produce and pantry items simultaneously")
}

data class ScannedBarcodeProduct(
    val barcode: String,
    val format: String = "UPC-A",
    val productName: String,
    val brand: String,
    val category: IngredientCategory,
    val estimatedShelfLifeDays: Int,
    val suggestedQuantity: Double,
    val unit: String,
    val calories: Int? = null,
    val netWeight: String? = null,
    val notes: String = "",
    val confidence: Float = 0.99f
)

data class ScannedLabelOcrResult(
    val rawText: String,
    val detectedProductName: String,
    val detectedBrand: String?,
    val detectedExpiryDate: String?,
    val calculatedShelfLifeDays: Int,
    val category: IngredientCategory,
    val detectedAllergens: List<String> = emptyList(),
    val confidence: Float = 0.94f
)

data class VisionImageAnalysisResult(
    val scanMode: ScanTargetMode,
    val detectedItems: List<DetectedFoodItem>,
    val scannedBarcode: ScannedBarcodeProduct? = null,
    val scannedLabel: ScannedLabelOcrResult? = null,
    val rawOcrText: String = "",
    val analysisNotes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val processingLatencyMs: Long = 450L,
    val isSuccessful: Boolean = true
)

/**
 * Interface for AI Image & Barcode analysis.
 * Serves as the extensible foundation for Google ML Kit / Gemini Vision / CameraX pipelines.
 */
interface ImageAnalysisHandler {
    suspend fun analyzeImageFrame(
        imageBytes: ByteArray? = null,
        mode: ScanTargetMode = ScanTargetMode.BARCODE,
        barcodeInput: String? = null
    ): VisionImageAnalysisResult

    suspend fun lookupBarcode(barcode: String): ScannedBarcodeProduct?
    suspend fun extractLabelData(simulatedOcrText: String): ScannedLabelOcrResult
}

/**
 * Robust mock implementation of ImageAnalysisHandler for future AI Vision integration.
 * Provides rich barcode database, label OCR heuristics, expiry date parsing, and item categorization.
 */
class MockImageAnalysisHandler : ImageAnalysisHandler {

    private val barcodeCatalog = listOf(
        ScannedBarcodeProduct(
            barcode = "011110038331",
            format = "UPC-A",
            productName = "Organic Greek Yogurt (Plain 2%)",
            brand = "Chobani",
            category = IngredientCategory.DAIRY_EGGS,
            estimatedShelfLifeDays = 14,
            suggestedQuantity = 1.0,
            unit = "tub (32 oz)",
            calories = 130,
            netWeight = "32 oz",
            notes = "Rich in probiotics and 16g protein per serving",
            confidence = 0.99f
        ),
        ScannedBarcodeProduct(
            barcode = "070847811169",
            format = "UPC-A",
            productName = "Baby Spinach Clamshell",
            brand = "Organic Girl",
            category = IngredientCategory.PRODUCE,
            estimatedShelfLifeDays = 5,
            suggestedQuantity = 1.0,
            unit = "box (5 oz)",
            calories = 20,
            netWeight = "5 oz",
            notes = "Pre-washed, tender baby leaves. Best used within 5 days.",
            confidence = 0.98f
        ),
        ScannedBarcodeProduct(
            barcode = "041220194883",
            format = "UPC-A",
            productName = "Unsweetened Almond Milk",
            brand = "Califia Farms",
            category = IngredientCategory.DAIRY_EGGS,
            estimatedShelfLifeDays = 10,
            suggestedQuantity = 1.0,
            unit = "bottle (48 fl oz)",
            calories = 35,
            netWeight = "48 fl oz",
            notes = "Plant-based, calcium-fortified, zero sugar.",
            confidence = 0.99f
        ),
        ScannedBarcodeProduct(
            barcode = "021000615967",
            format = "UPC-A",
            productName = "Sharp Cheddar Cheese Block",
            brand = "Cabot",
            category = IngredientCategory.DAIRY_EGGS,
            estimatedShelfLifeDays = 28,
            suggestedQuantity = 8.0,
            unit = "oz",
            calories = 110,
            netWeight = "8 oz",
            notes = "Aged naturally, lactose-free.",
            confidence = 0.97f
        ),
        ScannedBarcodeProduct(
            barcode = "072220002863",
            format = "UPC-A",
            productName = "Hot Chili Sriracha Sauce",
            brand = "Huy Fong",
            category = IngredientCategory.CONDIMENTS_SPICES,
            estimatedShelfLifeDays = 180,
            suggestedQuantity = 1.0,
            unit = "bottle (17 oz)",
            calories = 5,
            netWeight = "17 oz",
            notes = "Sun-ripened red jalapeños and garlic.",
            confidence = 0.99f
        ),
        ScannedBarcodeProduct(
            barcode = "011110874526",
            format = "UPC-A",
            productName = "Organic Free-Range Large Eggs",
            brand = "Vital Farms",
            category = IngredientCategory.DAIRY_EGGS,
            estimatedShelfLifeDays = 21,
            suggestedQuantity = 12.0,
            unit = "count",
            calories = 70,
            netWeight = "1 carton",
            notes = "Pasture-raised grade A eggs.",
            confidence = 0.99f
        ),
        ScannedBarcodeProduct(
            barcode = "024600010918",
            format = "UPC-A",
            productName = "Boneless Skinless Chicken Breast",
            brand = "Bell & Evans",
            category = IngredientCategory.PROTEINS,
            estimatedShelfLifeDays = 3,
            suggestedQuantity = 1.5,
            unit = "lbs",
            calories = 165,
            netWeight = "1.5 lbs",
            notes = "Air chilled, all vegetarian diet, no antibiotics.",
            confidence = 0.96f
        ),
        ScannedBarcodeProduct(
            barcode = "041789001201",
            format = "UPC-A",
            productName = "Organic Black Beans (Low Sodium)",
            brand = "Eden Foods",
            category = IngredientCategory.PANTRY_GRAINS,
            estimatedShelfLifeDays = 365,
            suggestedQuantity = 1.0,
            unit = "can (15 oz)",
            calories = 110,
            netWeight = "15 oz",
            notes = "Cooked with kombu seaweed for easy digestion.",
            confidence = 0.98f
        ),
        ScannedBarcodeProduct(
            barcode = "071514000312",
            format = "UPC-A",
            productName = "Extra Virgin Cold-Pressed Olive Oil",
            brand = "California Olive Ranch",
            category = IngredientCategory.CONDIMENTS_SPICES,
            estimatedShelfLifeDays = 240,
            suggestedQuantity = 1.0,
            unit = "bottle (500 ml)",
            calories = 120,
            netWeight = "500 ml",
            notes = "First cold press, fresh floral and peppery finish.",
            confidence = 0.99f
        ),
        ScannedBarcodeProduct(
            barcode = "073420000145",
            format = "UPC-A",
            productName = "Organic Jasmine Rice",
            brand = "Lundberg",
            category = IngredientCategory.PANTRY_GRAINS,
            estimatedShelfLifeDays = 180,
            suggestedQuantity = 2.0,
            unit = "lbs",
            calories = 160,
            netWeight = "2 lbs",
            notes = "Fragrant long-grain whole grain.",
            confidence = 0.97f
        )
    )

    override suspend fun analyzeImageFrame(
        imageBytes: ByteArray?,
        mode: ScanTargetMode,
        barcodeInput: String?
    ): VisionImageAnalysisResult {
        // Simulate real camera frame processing latency (e.g. inference on NPU/MLKit/Gemini)
        val startTime = System.currentTimeMillis()
        delay(400)

        return when (mode) {
            ScanTargetMode.BARCODE -> {
                val barcodeToQuery = barcodeInput?.trim() ?: barcodeCatalog.random().barcode
                val matchedProduct = lookupBarcode(barcodeToQuery)
                    ?: ScannedBarcodeProduct(
                        barcode = barcodeToQuery,
                        productName = "Packaged Grocery Item ($barcodeToQuery)",
                        brand = "Store Brand",
                        category = IngredientCategory.OTHER,
                        estimatedShelfLifeDays = 7,
                        suggestedQuantity = 1.0,
                        unit = "pcs",
                        notes = "Detected via camera barcode scanner",
                        confidence = 0.91f
                    )

                val detectedItem = DetectedFoodItem(
                    name = matchedProduct.productName,
                    category = matchedProduct.category,
                    suggestedQuantity = matchedProduct.suggestedQuantity,
                    unit = matchedProduct.unit,
                    estimatedShelfLifeDays = matchedProduct.estimatedShelfLifeDays,
                    confidence = matchedProduct.confidence,
                    isSelected = true
                )

                VisionImageAnalysisResult(
                    scanMode = ScanTargetMode.BARCODE,
                    detectedItems = listOf(detectedItem),
                    scannedBarcode = matchedProduct,
                    analysisNotes = "Barcode recognized: ${matchedProduct.format} [${matchedProduct.barcode}] -> ${matchedProduct.brand} ${matchedProduct.productName}",
                    processingLatencyMs = System.currentTimeMillis() - startTime,
                    isSuccessful = true
                )
            }

            ScanTargetMode.LABEL_OCR -> {
                val sampleTexts = listOf(
                    "ORGANIC VALLEY WHOLE MILK\nPASTEURIZED GRADE A\nBEST BY: 18 SEP\nNET WT 64 FL OZ (1.89L)\nKEEP REFRIGERATED",
                    "ORGANIC BABY ARUGULA\nTRIPLE WASHED & READY TO EAT\nUSE BY: 5 DAYS FROM OPENING\nPACK DATE: 08/28",
                    "WILD ALASKAN SOCKEYE SALMON FILLET\nFRESH CATCH - SUSTAINABLY SOURCED\nCOOK OR FREEZE BY: 3 DAYS\nKEEP AT 38°F OR BELOW",
                    "MUTTI SAN MARZANO WHOLE PEELED TOMATOES\nPRODUCT OF ITALY\nEXP: 12/2027\nNET WT 28 OZ (800g)"
                )
                val rawText = sampleTexts.random()
                val labelResult = extractLabelData(rawText)

                val detectedItem = DetectedFoodItem(
                    name = labelResult.detectedProductName,
                    category = labelResult.category,
                    suggestedQuantity = 1.0,
                    unit = "package",
                    estimatedShelfLifeDays = labelResult.calculatedShelfLifeDays,
                    confidence = labelResult.confidence,
                    isSelected = true
                )

                VisionImageAnalysisResult(
                    scanMode = ScanTargetMode.LABEL_OCR,
                    detectedItems = listOf(detectedItem),
                    scannedLabel = labelResult,
                    rawOcrText = rawText,
                    analysisNotes = "Label OCR extracted product '${labelResult.detectedProductName}', shelf life: ${labelResult.calculatedShelfLifeDays}d",
                    processingLatencyMs = System.currentTimeMillis() - startTime,
                    isSuccessful = true
                )
            }

            ScanTargetMode.SMART_FRIDGE_VISION -> {
                val items = listOf(
                    DetectedFoodItem("Fresh Roma Tomatoes", IngredientCategory.PRODUCE, 4.0, "count", 4, 0.96f),
                    DetectedFoodItem("Organic Whole Milk", IngredientCategory.DAIRY_EGGS, 1.0, "gallon", 5, 0.94f),
                    DetectedFoodItem("Boneless Chicken Breast", IngredientCategory.PROTEINS, 1.5, "lbs", 2, 0.92f),
                    DetectedFoodItem("Baby Spinach", IngredientCategory.PRODUCE, 1.0, "box", 3, 0.95f),
                    DetectedFoodItem("Eggs (Large)", IngredientCategory.DAIRY_EGGS, 12.0, "count", 12, 0.98f)
                )
                VisionImageAnalysisResult(
                    scanMode = ScanTargetMode.SMART_FRIDGE_VISION,
                    detectedItems = items,
                    analysisNotes = "Multi-item shelf vision classified 5 distinct grocery targets",
                    processingLatencyMs = System.currentTimeMillis() - startTime,
                    isSuccessful = true
                )
            }
        }
    }

    override suspend fun lookupBarcode(barcode: String): ScannedBarcodeProduct? {
        val cleanBarcode = barcode.trim()
        return barcodeCatalog.find { it.barcode.equals(cleanBarcode, ignoreCase = true) }
            ?: barcodeCatalog.find { cleanBarcode.contains(it.barcode) || it.barcode.contains(cleanBarcode) }
    }

    override suspend fun extractLabelData(simulatedOcrText: String): ScannedLabelOcrResult {
        val lines = simulatedOcrText.lines().map { it.trim() }.filter { it.isNotBlank() }
        val firstLine = lines.firstOrNull() ?: "Packaged Ingredient"

        val category = when {
            simulatedOcrText.contains("MILK", true) || simulatedOcrText.contains("YOGURT", true) || simulatedOcrText.contains("CHEESE", true) || simulatedOcrText.contains("EGG", true) -> IngredientCategory.DAIRY_EGGS
            simulatedOcrText.contains("SPINACH", true) || simulatedOcrText.contains("ARUGULA", true) || simulatedOcrText.contains("TOMATO", true) || simulatedOcrText.contains("BERRIES", true) -> IngredientCategory.PRODUCE
            simulatedOcrText.contains("CHICKEN", true) || simulatedOcrText.contains("SALMON", true) || simulatedOcrText.contains("BEEF", true) || simulatedOcrText.contains("TOFU", true) -> IngredientCategory.PROTEINS
            simulatedOcrText.contains("RICE", true) || simulatedOcrText.contains("PASTA", true) || simulatedOcrText.contains("BEANS", true) -> IngredientCategory.PANTRY_GRAINS
            else -> IngredientCategory.OTHER
        }

        val shelfDays = when {
            simulatedOcrText.contains("SALMON", true) || simulatedOcrText.contains("CHICKEN", true) -> 3
            simulatedOcrText.contains("ARUGULA", true) || simulatedOcrText.contains("SPINACH", true) -> 5
            simulatedOcrText.contains("MILK", true) -> 7
            simulatedOcrText.contains("TOMATOES", true) || simulatedOcrText.contains("CAN", true) -> 180
            else -> 10
        }

        val allergens = mutableListOf<String>()
        if (category == IngredientCategory.DAIRY_EGGS) allergens.add("Dairy / Lactose")
        if (simulatedOcrText.contains("PEANUT", true) || simulatedOcrText.contains("NUT", true)) allergens.add("Tree Nuts")
        if (simulatedOcrText.contains("WHEAT", true) || simulatedOcrText.contains("GLUTEN", true)) allergens.add("Gluten")

        return ScannedLabelOcrResult(
            rawText = simulatedOcrText,
            detectedProductName = firstLine,
            detectedBrand = lines.getOrNull(1)?.take(20),
            detectedExpiryDate = lines.find { it.contains("BY", true) || it.contains("EXP", true) } ?: "Best within $shelfDays days",
            calculatedShelfLifeDays = shelfDays,
            category = category,
            detectedAllergens = allergens,
            confidence = 0.95f
        )
    }

    fun getAllSampleBarcodes(): List<ScannedBarcodeProduct> = barcodeCatalog
}
