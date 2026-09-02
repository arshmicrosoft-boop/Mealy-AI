package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthManager
import com.example.data.ai.vision.*
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.KitchenRepository
import com.example.util.SpeechRecognitionHelper
import com.example.util.SpeechState
import com.example.util.TextToSpeechHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,
    SCAN,
    INVENTORY,
    RECIPES,
    COOKING,
    ASK_MEALY,
    PROFILE,
    SETTINGS
}

data class TimerState(
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

class KitchenViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = KitchenRepository(database)
    val ttsHelper = TextToSpeechHelper(application)
    val speechHelper = SpeechRecognitionHelper(application)
    val authManager = AuthManager(application)

    // Auth State
    val userProfile: StateFlow<UserProfile> = authManager.userProfile
    val isAuthenticating: StateFlow<Boolean> = authManager.isAuthenticating
    val authStateMessage: StateFlow<String?> = authManager.authStateMessage

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            authManager.signInWithGoogle(activity)
        }
    }

    fun continueAsGuest(customName: String = "Chef Gourmet") {
        authManager.continueAsGuest(customName)
    }

    fun signOut() {
        authManager.signOut()
    }

    // Speech & Interactive Voice State
    val speechState: StateFlow<SpeechState> = speechHelper.state

    private val _voiceFeedbackMessage = MutableStateFlow<String?>(null)
    val voiceFeedbackMessage: StateFlow<String?> = _voiceFeedbackMessage.asStateFlow()

    private val _isVoiceAiProcessing = MutableStateFlow(false)
    val isVoiceAiProcessing: StateFlow<Boolean> = _isVoiceAiProcessing.asStateFlow()

    private val _aiVoiceResponse = MutableStateFlow<String?>(null)
    val aiVoiceResponse: StateFlow<String?> = _aiVoiceResponse.asStateFlow()

    private val _showCompletionDialogRequested = MutableStateFlow(false)
    val showCompletionDialogRequested: StateFlow<Boolean> = _showCompletionDialogRequested.asStateFlow()

    // Current Tab
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Human Psychology & Habit Formation State (Streaks, XP, Quests, Mystery Drops)
    private val _psychologyState = MutableStateFlow(KitchenPsychologyState())
    val psychologyState: StateFlow<KitchenPsychologyState> = _psychologyState.asStateFlow()

    private val _dopamineCelebrationMessage = MutableStateFlow<String?>(null)
    val dopamineCelebrationMessage: StateFlow<String?> = _dopamineCelebrationMessage.asStateFlow()

    private val _showMysteryDropDialog = MutableStateFlow(false)
    val showMysteryDropDialog: StateFlow<Boolean> = _showMysteryDropDialog.asStateFlow()

    // Inventory & Data Flows
    val activeInventory: StateFlow<List<InventoryItem>> = repository.activeInventory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecipes: StateFlow<List<Recipe>> = repository.allRecipes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferences> = repository.userPreferences
        .map { it ?: UserPreferences() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    val cookingSessions: StateFlow<List<CookingSession>> = repository.cookingSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kitchenMemories: StateFlow<List<KitchenMemoryLog>> = repository.kitchenMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Recipe Detail
    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    // Active Cooking Mode Session
    private val _activeCookingRecipe = MutableStateFlow<Recipe?>(null)
    val activeCookingRecipe: StateFlow<Recipe?> = _activeCookingRecipe.asStateFlow()

    private val _currentCookingStepIndex = MutableStateFlow(0)
    val currentCookingStepIndex: StateFlow<Int> = _currentCookingStepIndex.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()

    private var timerJob: Job? = null

    // Scan & Vision Image Analysis State
    private val _scanPreset = MutableStateFlow("Full Fridge Scan")
    val scanPreset: StateFlow<String> = _scanPreset.asStateFlow()

    private val _detectedFoodItems = MutableStateFlow<List<DetectedFoodItem>>(emptyList())
    val detectedFoodItems: StateFlow<List<DetectedFoodItem>> = _detectedFoodItems.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _activeScanTargetMode = MutableStateFlow(ScanTargetMode.BARCODE)
    val activeScanTargetMode: StateFlow<ScanTargetMode> = _activeScanTargetMode.asStateFlow()

    private val _imageAnalysisResult = MutableStateFlow<VisionImageAnalysisResult?>(null)
    val imageAnalysisResult: StateFlow<VisionImageAnalysisResult?> = _imageAnalysisResult.asStateFlow()

    private val _isAnalyzingImage = MutableStateFlow(false)
    val isAnalyzingImage: StateFlow<Boolean> = _isAnalyzingImage.asStateFlow()

    // Ask Mealio Chat State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Welcome to Mealio AI! I autonomously track your kitchen inventory, prevent food waste, and plan your meals based on your personal taste preferences. Ask me what to cook, request substitutions, or ask for meal plan adjustments.",
                isUser = false,
                suggestedActions = listOf(
                    "Plan my meals for today",
                    "What can I cook right now?",
                    "How do I substitute butter with ghee?",
                    "What items are expiring soon?"
                )
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    private val _isGeneratingRecipes = MutableStateFlow(false)
    val isGeneratingRecipes: StateFlow<Boolean> = _isGeneratingRecipes.asStateFlow()

    private val _showRecipeSuggestionDialog = MutableStateFlow(false)
    val showRecipeSuggestionDialog: StateFlow<Boolean> = _showRecipeSuggestionDialog.asStateFlow()

    private val _lastGeneratedAiRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val lastGeneratedAiRecipes: StateFlow<List<Recipe>> = _lastGeneratedAiRecipes.asStateFlow()

    // Autonomous Meal Plan State
    private val _autonomousMealPlan = MutableStateFlow<AutonomousMealPlan?>(null)
    val autonomousMealPlan: StateFlow<AutonomousMealPlan?> = _autonomousMealPlan.asStateFlow()

    private val _isPlanningMeals = MutableStateFlow(false)
    val isPlanningMeals: StateFlow<Boolean> = _isPlanningMeals.asStateFlow()

    // Inventory Filter
    private val _inventoryFilter = MutableStateFlow("All")
    val inventoryFilter: StateFlow<String> = _inventoryFilter.asStateFlow()

    // Recipe Filter
    private val _recipeCuisineFilter = MutableStateFlow("All")
    val recipeCuisineFilter: StateFlow<String> = _recipeCuisineFilter.asStateFlow()

    init {
        // Run initial scan simulation so user sees detected ingredients ready
        runFridgeScan("Full Fridge Scan")
        refreshAutonomousMealPlan()

        speechHelper.setOnCommandRecognizedListener { spokenText ->
            processVoiceCommand(spokenText)
        }
    }

    fun startVoiceListening() {
        _voiceFeedbackMessage.value = "Listening for hands-free command..."
        speechHelper.startListening()
    }

    fun stopVoiceListening() {
        speechHelper.stopListening()
    }

    fun toggleVoiceListening() {
        if (speechHelper.state.value.isListening) {
            stopVoiceListening()
        } else {
            startVoiceListening()
        }
    }

    fun clearAiVoiceResponse() {
        _aiVoiceResponse.value = null
    }

    fun dismissCompletionDialog() {
        _showCompletionDialogRequested.value = false
    }

    fun processVoiceCommand(spokenText: String) {
        val raw = spokenText.trim()
        val text = raw.lowercase()
        val recipe = _activeCookingRecipe.value
        val stepIdx = _currentCookingStepIndex.value
        val currentStep = recipe?.steps?.getOrNull(stepIdx)

        _voiceFeedbackMessage.value = "Heard: \"$raw\""

        when {
            // Next Step
            text.contains("next") || text.contains("continue") || text.contains("forward") || text.contains("done with this step") -> {
                if (recipe != null && stepIdx == recipe.steps.lastIndex) {
                    _showCompletionDialogRequested.value = true
                    ttsHelper.speak("You've reached the final step! Ready to finish cooking?")
                } else {
                    nextCookingStep()
                }
            }

            // Previous Step
            text.contains("previous") || text.contains("back") || text.contains("last step") || text.contains("go back") -> {
                previousCookingStep()
            }

            // Repeat Step
            text.contains("repeat") || text.contains("read again") || text.contains("say again") || text.contains("what do i do") || text.contains("read step") -> {
                speakCurrentStep()
            }

            // Ingredients for step
            text.contains("ingredient") || text.contains("what do i need") || text.contains("what goes in") -> {
                if (currentStep != null && currentStep.ingredientsNeeded.isNotEmpty()) {
                    val ingText = currentStep.ingredientsNeeded.joinToString(", ")
                    _aiVoiceResponse.value = "Step ${currentStep.stepNumber} ingredients: $ingText"
                    ttsHelper.speak("For this step, you need: $ingText")
                } else if (recipe != null) {
                    val allIng = recipe.ingredients.take(5).joinToString(", ") { it.name }
                    _aiVoiceResponse.value = "Recipe ingredients: $allIng"
                    ttsHelper.speak("Key ingredients are: $allIng")
                }
            }

            // Start Timer
            (text.contains("start") || text.contains("begin") || text.contains("play") || text.contains("resume") || text.contains("set")) && text.contains("timer") -> {
                startTimer()
                ttsHelper.speak("Step timer started.")
            }

            // Pause Timer
            (text.contains("pause") || text.contains("stop") || text.contains("hold")) && text.contains("timer") -> {
                pauseTimer()
                ttsHelper.speak("Step timer paused.")
            }

            // Reset Timer
            (text.contains("reset") || text.contains("restart") || text.contains("clear")) && text.contains("timer") -> {
                resetTimer()
                ttsHelper.speak("Step timer reset.")
            }

            // Time Remaining Check
            text.contains("how much time") || text.contains("time left") || text.contains("remaining time") || text.contains("timer status") -> {
                val rem = _timerState.value.remainingSeconds
                val mins = rem / 60
                val secs = rem % 60
                val msg = if (rem <= 0) "No active timer is currently running." else "$mins minutes and $secs seconds remaining."
                _aiVoiceResponse.value = "Timer: $msg"
                ttsHelper.speak(msg)
            }

            // Chef Science / Tip
            text.contains("tip") || text.contains("science") || text.contains("why") || text.contains("chef advice") -> {
                if (currentStep != null && (!currentStep.chefTip.isNullOrBlank() || !currentStep.scienceExplanation.isNullOrBlank())) {
                    val tip = currentStep.chefTip ?: ""
                    val science = currentStep.scienceExplanation ?: ""
                    val combo = "$tip $science".trim()
                    _aiVoiceResponse.value = "Chef Science: $combo"
                    ttsHelper.speak(combo)
                } else {
                    ttsHelper.speak("Keep a moderate heat and let the pan preheat properly for the best sear.")
                }
            }

            // Finish Cooking
            text.contains("finish") || text.contains("complete") || text.contains("all done") -> {
                _showCompletionDialogRequested.value = true
                ttsHelper.speak("Great cooking! Wrapping up your session.")
            }

            // Silence / Stop Voice
            text.contains("stop talking") || text.contains("be quiet") || text.contains("silence") || text.contains("shh") -> {
                stopSpeaking()
            }

            // Ask Interactive Chef AI Question
            else -> {
                if (recipe != null && currentStep != null) {
                    askAiDuringCooking(raw, recipe, currentStep)
                } else {
                    ttsHelper.speak("I heard: $raw. You can say 'next step', 'repeat', 'start timer', or ask any cooking question.")
                }
            }
        }
    }

    private fun askAiDuringCooking(question: String, recipe: Recipe, step: RecipeStep) {
        viewModelScope.launch {
            _isVoiceAiProcessing.value = true
            _aiVoiceResponse.value = "Chef AI thinking: \"$question\"..."
            try {
                val answer = repository.askCookingQuestion(question, recipe, step)
                _aiVoiceResponse.value = answer
                ttsHelper.speak(answer)
            } catch (e: Exception) {
                val fallback = "Adjust temperature moderately, taste for seasoning, and watch texture closely."
                _aiVoiceResponse.value = fallback
                ttsHelper.speak(fallback)
            } finally {
                _isVoiceAiProcessing.value = false
            }
        }
    }

    fun refreshAutonomousMealPlan() {
        viewModelScope.launch {
            _isPlanningMeals.value = true
            try {
                _autonomousMealPlan.value = repository.generateAutonomousMealPlan()
            } catch (e: Exception) {
                // Fallback handled safely
            } finally {
                _isPlanningMeals.value = false
            }
        }
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun setInventoryFilter(filter: String) {
        _inventoryFilter.value = filter
    }

    fun setRecipeCuisineFilter(cuisine: String) {
        _recipeCuisineFilter.value = cuisine
    }

    // --- Vision & Scan Actions ---
    fun setScanPreset(preset: String) {
        _scanPreset.value = preset
        runFridgeScan(preset)
    }

    fun setScanTargetMode(mode: ScanTargetMode) {
        _activeScanTargetMode.value = mode
    }

    fun scanBarcodeOrLabel(
        mode: ScanTargetMode = _activeScanTargetMode.value,
        barcodeInput: String? = null,
        onResult: (VisionImageAnalysisResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            _isAnalyzingImage.value = true
            _activeScanTargetMode.value = mode
            try {
                val result = repository.analyzeImageWithVisionHandler(
                    mode = mode,
                    barcodeInput = barcodeInput
                )
                _imageAnalysisResult.value = result
                onResult(result)
            } catch (e: Exception) {
                // Fallback handling
            } finally {
                _isAnalyzingImage.value = false
            }
        }
    }

    fun addScannedBarcodeToInventory(
        barcodeProduct: ScannedBarcodeProduct,
        customQuantity: Double? = null,
        location: String = if (barcodeProduct.category == IngredientCategory.PANTRY_GRAINS || barcodeProduct.category == IngredientCategory.CONDIMENTS_SPICES) "Pantry" else "Fridge",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val item = InventoryItem(
                name = barcodeProduct.productName,
                category = barcodeProduct.category,
                quantity = customQuantity ?: barcodeProduct.suggestedQuantity,
                unit = barcodeProduct.unit,
                state = FoodState.STORED,
                expiryTimestamp = now + (barcodeProduct.estimatedShelfLifeDays * dayMs),
                location = location,
                notes = "Scanned Barcode: ${barcodeProduct.barcode} (${barcodeProduct.brand})",
                estimatedPriceDollars = when (barcodeProduct.category) {
                    IngredientCategory.PROTEINS -> 8.50
                    IngredientCategory.DAIRY_EGGS -> 4.50
                    IngredientCategory.PRODUCE -> 3.20
                    else -> 3.99
                }
            )
            repository.addInventoryItem(item)
            _imageAnalysisResult.value = null
            refreshAutonomousMealPlan()
            onSuccess()
        }
    }

    fun addScannedLabelToInventory(
        labelResult: ScannedLabelOcrResult,
        quantity: Double = 1.0,
        unit: String = "pcs",
        location: String = if (labelResult.category == IngredientCategory.PANTRY_GRAINS || labelResult.category == IngredientCategory.CONDIMENTS_SPICES) "Pantry" else "Fridge",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val item = InventoryItem(
                name = labelResult.detectedProductName,
                category = labelResult.category,
                quantity = quantity,
                unit = unit,
                state = FoodState.STORED,
                expiryTimestamp = now + (labelResult.calculatedShelfLifeDays * dayMs),
                location = location,
                notes = "OCR Label Scan (${labelResult.detectedExpiryDate ?: "est. ${labelResult.calculatedShelfLifeDays}d"})",
                estimatedPriceDollars = 3.50
            )
            repository.addInventoryItem(item)
            _imageAnalysisResult.value = null
            refreshAutonomousMealPlan()
            onSuccess()
        }
    }

    fun clearImageAnalysisResult() {
        _imageAnalysisResult.value = null
    }

    fun runFridgeScan(query: String) {
        viewModelScope.launch {
            _isScanning.value = true
            delay(600) // Realistic AI vision processing indicator
            val items = repository.scanFridgeVision(query)
            _detectedFoodItems.value = items
            _isScanning.value = false
        }
    }

    fun toggleDetectedItemSelection(index: Int) {
        val current = _detectedFoodItems.value.toMutableList()
        if (index in current.indices) {
            val item = current[index]
            current[index] = item.copy(isSelected = !item.isSelected)
            _detectedFoodItems.value = current
        }
    }

    fun updateDetectedItemQuantity(index: Int, newQty: Double) {
        val current = _detectedFoodItems.value.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(suggestedQuantity = newQty.coerceAtLeast(0.5))
            _detectedFoodItems.value = current
        }
    }

    fun confirmDetectedItemsToInventory(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val selected = _detectedFoodItems.value.filter { it.isSelected }
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L

            val newInventory = selected.map { detected ->
                InventoryItem(
                    name = detected.name,
                    category = detected.category,
                    quantity = detected.suggestedQuantity,
                    unit = detected.unit,
                    state = FoodState.STORED,
                    expiryTimestamp = now + (detected.estimatedShelfLifeDays * dayMs),
                    location = if (detected.category == IngredientCategory.PANTRY_GRAINS || detected.category == IngredientCategory.CONDIMENTS_SPICES) "Pantry" else "Fridge",
                    notes = "Auto-scanned by Mealio Fridge Vision (${(detected.confidence * 100).toInt()}% confidence)",
                    estimatedPriceDollars = 3.50
                )
            }
            repository.addInventoryItems(newInventory)
            _detectedFoodItems.value = emptyList()
            refreshAutonomousMealPlan()
            onSuccess()
        }
    }

    // --- Inventory Operations ---
    fun addManualInventoryItem(
        name: String,
        category: IngredientCategory,
        quantity: Double,
        unit: String,
        shelfLifeDays: Int,
        location: String
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val item = InventoryItem(
                name = name.trim(),
                category = category,
                quantity = quantity,
                unit = unit.trim(),
                state = FoodState.STORED,
                expiryTimestamp = now + (shelfLifeDays * dayMs),
                location = location,
                notes = "Manually added",
                estimatedPriceDollars = 3.00
            )
            repository.addInventoryItem(item)
        }
    }

    fun updateItemState(id: Long, newState: FoodState) {
        viewModelScope.launch {
            repository.updateItemState(id, newState)
        }
    }

    fun deleteInventoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteInventoryItem(id)
        }
    }

    fun loadStarterPantry() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dayMs = 24 * 60 * 60 * 1000L
            val starterItems = listOf(
                InventoryItem(name = "Organic Whole Milk", category = IngredientCategory.DAIRY_EGGS, quantity = 1.0, unit = "gallon", state = FoodState.STORED, expiryTimestamp = now + (3 * dayMs), location = "Fridge", notes = "Fresh dairy", estimatedPriceDollars = 4.29),
                InventoryItem(name = "Baby Spinach", category = IngredientCategory.PRODUCE, quantity = 1.0, unit = "box", state = FoodState.STORED, expiryTimestamp = now + (2 * dayMs), location = "Fridge", notes = "Expiring soon", estimatedPriceDollars = 3.99),
                InventoryItem(name = "Free-Range Large Eggs", category = IngredientCategory.DAIRY_EGGS, quantity = 12.0, unit = "count", state = FoodState.STORED, expiryTimestamp = now + (8 * dayMs), location = "Fridge", notes = "Fresh", estimatedPriceDollars = 4.50),
                InventoryItem(name = "Boneless Chicken Breast", category = IngredientCategory.PROTEINS, quantity = 1.5, unit = "lbs", state = FoodState.STORED, expiryTimestamp = now + (2 * dayMs), location = "Fridge", notes = "Use soon", estimatedPriceDollars = 8.50),
                InventoryItem(name = "Jasmine Rice", category = IngredientCategory.PANTRY_GRAINS, quantity = 2.0, unit = "lbs", state = FoodState.STORED, expiryTimestamp = now + (90 * dayMs), location = "Pantry", notes = "Staple grain", estimatedPriceDollars = 3.20),
                InventoryItem(name = "Extra Virgin Olive Oil", category = IngredientCategory.CONDIMENTS_SPICES, quantity = 1.0, unit = "bottle", state = FoodState.STORED, expiryTimestamp = now + (180 * dayMs), location = "Pantry", notes = "Cooking base", estimatedPriceDollars = 9.99),
                InventoryItem(name = "Roma Tomatoes", category = IngredientCategory.PRODUCE, quantity = 4.0, unit = "count", state = FoodState.STORED, expiryTimestamp = now + (4 * dayMs), location = "Fridge", notes = "Fresh", estimatedPriceDollars = 2.80),
                InventoryItem(name = "Fresh Garlic", category = IngredientCategory.PRODUCE, quantity = 1.0, unit = "bulb", state = FoodState.STORED, expiryTimestamp = now + (20 * dayMs), location = "Pantry", notes = "Aromatic", estimatedPriceDollars = 0.99)
            )
            repository.addInventoryItems(starterItems)
            refreshAutonomousMealPlan()
        }
    }

    // --- Recipe Details & Cooking Companion ---
    fun openRecipeSuggestionDialog() {
        _showRecipeSuggestionDialog.value = true
        if (_lastGeneratedAiRecipes.value.isEmpty()) {
            generateAiRecipes(null)
        }
    }

    fun closeRecipeSuggestionDialog() {
        _showRecipeSuggestionDialog.value = false
    }

    fun generateAiRecipes(customQuery: String? = null) {
        viewModelScope.launch {
            _isGeneratingRecipes.value = true
            try {
                val results = repository.getAdaptiveRecipes(customQuery)
                _lastGeneratedAiRecipes.value = results
            } catch (e: Exception) {
                // Handled gracefully in repository fallback
            } finally {
                _isGeneratingRecipes.value = false
            }
        }
    }

    fun viewRecipeDetails(recipe: Recipe) {
        _selectedRecipe.value = recipe
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
    }

    fun startCookingSession(recipe: Recipe) {
        _activeCookingRecipe.value = recipe
        _currentCookingStepIndex.value = 0
        _currentTab.value = MainTab.COOKING
        initStepTimer(recipe.steps.firstOrNull())

        if (userPreferences.value.autoVoiceOnStepChange) {
            val firstStep = recipe.steps.firstOrNull()
            if (firstStep != null) {
                ttsHelper.speak("Starting ${recipe.title}. Step 1: ${firstStep.title}. ${firstStep.instruction}")
            }
        }
    }

    fun nextCookingStep() {
        val recipe = _activeCookingRecipe.value ?: return
        val currentIdx = _currentCookingStepIndex.value
        if (currentIdx < recipe.steps.lastIndex) {
            val nextIdx = currentIdx + 1
            _currentCookingStepIndex.value = nextIdx
            val step = recipe.steps[nextIdx]
            initStepTimer(step)

            if (userPreferences.value.autoVoiceOnStepChange) {
                ttsHelper.speak("Step ${step.stepNumber}: ${step.title}. ${step.instruction}")
            }
        }
    }

    fun previousCookingStep() {
        val currentIdx = _currentCookingStepIndex.value
        if (currentIdx > 0) {
            val prevIdx = currentIdx - 1
            _currentCookingStepIndex.value = prevIdx
            val recipe = _activeCookingRecipe.value ?: return
            val step = recipe.steps[prevIdx]
            initStepTimer(step)
        }
    }

    fun speakCurrentStep() {
        val recipe = _activeCookingRecipe.value ?: return
        val currentIdx = _currentCookingStepIndex.value
        val step = recipe.steps.getOrNull(currentIdx) ?: return
        val tip = if (!step.chefTip.isNullOrBlank()) " Chef tip: ${step.chefTip}" else ""
        ttsHelper.speak("Step ${step.stepNumber}: ${step.title}. ${step.instruction}$tip")
    }

    fun stopSpeaking() {
        ttsHelper.stop()
    }

    private fun initStepTimer(step: RecipeStep?) {
        timerJob?.cancel()
        if (step != null && step.durationMinutes > 0) {
            val totalSec = step.durationMinutes * 60
            _timerState.value = TimerState(
                totalSeconds = totalSec,
                remainingSeconds = totalSec,
                isRunning = false,
                isFinished = false
            )
        } else {
            _timerState.value = TimerState()
        }
    }

    fun toggleTimer() {
        val current = _timerState.value
        if (current.isRunning) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        val current = _timerState.value
        if (current.remainingSeconds <= 0) return
        _timerState.value = current.copy(isRunning = true, isFinished = false)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.remainingSeconds > 0 && _timerState.value.isRunning) {
                delay(1000)
                val remaining = _timerState.value.remainingSeconds - 1
                if (remaining <= 0) {
                    _timerState.value = _timerState.value.copy(
                        remainingSeconds = 0,
                        isRunning = false,
                        isFinished = true
                    )
                    ttsHelper.speak("Timer completed! Check your food.")
                    break
                } else {
                    _timerState.value = _timerState.value.copy(remainingSeconds = remaining)
                }
            }
        }
    }

    private fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        val current = _timerState.value
        timerJob?.cancel()
        _timerState.value = current.copy(
            remainingSeconds = current.totalSeconds,
            isRunning = false,
            isFinished = false
        )
    }

    fun finishCookingSession(rating: Int, feedback: String) {
        val recipe = _activeCookingRecipe.value ?: return
        viewModelScope.launch {
            val dollarsSaved = (recipe.wasteReductionScore * 0.8).coerceAtLeast(3.50)
            val session = CookingSession(
                recipeId = recipe.id,
                recipeTitle = recipe.title,
                rating = rating,
                feedbackNote = feedback,
                substitutionsUsed = recipe.substitutions.map { "${it.originalIngredient} -> ${it.substituteIngredient}" },
                wasteSavedDollars = dollarsSaved
            )
            repository.completeCookingSession(session)
            
            // Psychology Reward: Award XP, update Dollars Saved, update streak, complete Quest
            awardXp(120, "Cooked ${recipe.title}")
            recordSavings(dollarsSaved, 0.6)
            completeQuest("quest_rescue")
            triggerDopamineCelebration("🎉 +120 XP! You rescued \$$dollarsSaved of food!")

            ttsHelper.speak("Cooking session finished! Amazing job Chef. Saved to your Kitchen Memory.")
            _activeCookingRecipe.value = null
            _currentTab.value = MainTab.HOME
        }
    }

    // --- Psychology & Habit Triggers (Streaks, Quests, Mystery Drops, XP, Savings) ---
    fun openMysteryDropDialog() {
        _showMysteryDropDialog.value = true
    }

    fun dismissMysteryDropDialog() {
        _showMysteryDropDialog.value = false
    }

    fun claimMysteryDrop() {
        val current = _psychologyState.value
        val drop = current.mysteryDrop
        if (!drop.isOpened) {
            val updatedDrop = drop.copy(isOpened = true)
            val newXp = current.currentXp + drop.bonusXp
            val newQuests = current.dailyQuests.map {
                if (it.id == "quest_mystery") it.copy(isCompleted = true, progress = 1.0f) else it
            }
            _psychologyState.value = current.copy(
                mysteryDrop = updatedDrop,
                currentXp = newXp,
                dailyQuests = newQuests,
                kitchenIqScore = (current.kitchenIqScore + 3).coerceAtMost(100)
            )
            triggerDopamineCelebration("🎁 Mystery Unlocked! +${drop.bonusXp} XP & Secret Culinary Hack!")
        }
    }

    fun completeQuest(questId: String) {
        val current = _psychologyState.value
        val quest = current.dailyQuests.find { it.id == questId }
        if (quest != null && !quest.isCompleted) {
            val updatedQuests = current.dailyQuests.map {
                if (it.id == questId) it.copy(isCompleted = true, progress = 1.0f) else it
            }
            val newXp = current.currentXp + quest.xpReward
            val newIq = (current.kitchenIqScore + 4).coerceAtMost(100)
            _psychologyState.value = current.copy(
                dailyQuests = updatedQuests,
                currentXp = newXp,
                kitchenIqScore = newIq
            )
            triggerDopamineCelebration("⚡ Quest Complete: ${quest.title}! +${quest.xpReward} XP")
        }
    }

    fun awardXp(amount: Int, reason: String) {
        val current = _psychologyState.value
        var newXp = current.currentXp + amount
        var tier = current.chefTierName
        var tierLevel = current.chefTierLevel
        var maxForNext = current.xpForNextLevel

        if (newXp >= current.xpForNextLevel) {
            tierLevel += 1
            tier = when (tierLevel) {
                1 -> "Level 1: Kitchen Novice"
                2 -> "Level 2: Pantry Tactician"
                3 -> "Level 3: Zero-Waste Master"
                4 -> "Level 4: Culinary Alchemist"
                else -> "Level 5: Executive AI Maestro"
            }
            maxForNext = tierLevel * 500
            triggerDopamineCelebration("👑 LEVEL UP! You are now $tier!")
        }

        _psychologyState.value = current.copy(
            currentXp = newXp,
            chefTierName = tier,
            chefTierLevel = tierLevel,
            xpForNextLevel = maxForNext,
            kitchenIqScore = (current.kitchenIqScore + 2).coerceAtMost(100)
        )
    }

    fun recordSavings(dollars: Double, kg: Double) {
        val current = _psychologyState.value
        _psychologyState.value = current.copy(
            totalMoneySavedDollars = current.totalMoneySavedDollars + dollars,
            totalFoodRescuedKg = current.totalFoodRescuedKg + kg,
            mealsCookedTotal = current.mealsCookedTotal + 1
        )
    }

    fun protectStreak() {
        val current = _psychologyState.value
        if (current.streakShieldsRemaining > 0) {
            _psychologyState.value = current.copy(
                streakShieldsRemaining = current.streakShieldsRemaining - 1
            )
            triggerDopamineCelebration("🛡️ Streak Freeze Activated! Your 5-day streak is safely protected.")
        }
    }

    fun triggerDopamineCelebration(message: String) {
        _dopamineCelebrationMessage.value = message
    }

    fun dismissCelebration() {
        _dopamineCelebrationMessage.value = null
    }

    fun exitCookingSession() {
        timerJob?.cancel()
        ttsHelper.stop()
        _activeCookingRecipe.value = null
        _currentTab.value = MainTab.RECIPES
    }

    // --- Ask Mealy AI Chat ---
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(text = text.trim(), isUser = true)
        val current = _chatMessages.value + userMsg
        _chatMessages.value = current
        _isChatGenerating.value = true

        viewModelScope.launch {
            try {
                val response = repository.chatWithChef(userMsg.text, current)
                val aiMsg = ChatMessage(
                    text = response,
                    isUser = false,
                    suggestedActions = when {
                        text.contains("cook", ignoreCase = true) -> listOf("Start Cooking Mode", "Show recipe details", "What else can I make?")
                        text.contains("substitute", ignoreCase = true) -> listOf("How does this affect cooking time?", "Check other pantry swaps")
                        else -> listOf("What can I cook?", "Check expiring food", "Help with timers")
                    }
                )
                _chatMessages.value = _chatMessages.value + aiMsg
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    text = "I'm always ready to help! Try asking: 'What can I cook with my chicken and spinach?' or 'How do I substitute butter with ghee?'",
                    isUser = false
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isChatGenerating.value = false
            }
        }
    }

    // --- Profile & Preferences ---
    fun updatePreferences(prefs: UserPreferences) {
        viewModelScope.launch {
            repository.savePreferences(prefs)
            refreshAutonomousMealPlan()
        }
    }

    fun addKitchenMemory(insight: String, category: String, actionTaken: String) {
        viewModelScope.launch {
            val memory = KitchenMemoryLog(
                category = category,
                insight = insight,
                actionTaken = actionTaken
            )
            repository.addKitchenMemory(memory)
            refreshAutonomousMealPlan()
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsHelper.shutdown()
        speechHelper.destroy()
        timerJob?.cancel()
    }
}
