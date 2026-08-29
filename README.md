# 🍳 Mealio AI — Autonomous Smart Kitchen Copilot & Inventory Intelligence

> **Zero food waste, hands-free voice cooking, and continuous culinary personalization powered by on-device intelligence and Gemini AI.**

---

## 🌟 Table of Contents
1. [Overview & Vision](#-overview--vision)
2. [Why Choose Mealio AI Over Other Cooking Apps?](#-why-choose-mealio-ai-over-other-cooking-apps)
3. [Key Features & Capabilities](#-key-features--capabilities)
4. [System Architecture & Tech Stack](#-system-architecture--tech-stack)
5. [End-to-End User & Data Workflows](#-end-to-end-user--data-workflows)
6. [Why Mealio AI is Maximum Efficiency](#-why-mealio-ai-is-maximum-efficiency)
7. [Getting Started & Configuration](#-getting-started--configuration)

---

## 📖 Overview & Vision

Traditional recipe apps are **static digital cookbooks**: they display thousands of recipes requiring long shopping lists, ignore the ingredients you already have rotting in your fridge, force you to touch sticky screens while cooking, and never remember your personal tastes.

**Mealio AI** flips the paradigm into an **Autonomous Kitchen Copilot**:
- It tracks your fridge and pantry items in real-time, highlighting expiration timelines.
- It dynamically synthesizes daily meal plans prioritizing expiring items to **eliminate food waste**.
- It guides you step-by-step with **hands-free bidirectional voice interaction** (listening and speaking so you never have to touch your device with messy hands).
- It continuously learns from your kitchen memories, dietary goals, and taste feedback to refine every future recommendation.

---

## 🏆 Why Choose Mealio AI Over Other Cooking Apps?

| Feature / Dimension | Standard Recipe Apps (Tasty, Allrecipes, Paprika) | Mealio AI |
| :--- | :--- | :--- |
| **Inventory Awareness** | ❌ None. Tells you what to buy, not what to use. | ✅ **Real-Time Inventory Engine**: Calculates exact match percentages and missing ingredients against your current stock. |
| **Food Waste Elimination** | ❌ Ignored. Food spoils while you look for recipes. | ✅ **Smart Expiry Ranking**: Automatically prioritizes items nearing expiration date in recipe ranking and meal planning. |
| **Cooking Experience** | ❌ Manual swiping on screen with dirty/wet hands. | ✅ **Full Hands-Free Voice Control**: Speak *"Next step"*, *"Read ingredients"*, *"Start timer"*, or ask *"What can I substitute for garlic?"*. |
| **Culinary Science** | ❌ Plain text instructions without context. | ✅ **Food Chemistry Tips**: Explains the *why* (Maillard reaction, emulsion stability, resting meat) behind critical steps. |
| **Taste Memory** | ❌ Generic static ratings that don't alter recommendations. | ✅ **Adaptive Taste Memory**: Automatically adapts recipes when you adjust spice tolerance, dietary regimes, or disliked ingredients. |
| **Offline Reliability** | ❌ Breaks or requires constant network for recipes. | ✅ **Hybrid Architecture**: Full local rule-based AI engine and Room SQLite persistence with seamless Gemini cloud escalation. |

---

## 🚀 Key Features & Capabilities

### 1. Autonomous Food Waste Prevention & Inventory Management
- **Expiry Tracking**: Color-coded urgency indicators (🟢 Safe, 🟡 Expiring Soon, 🔴 Critical Expiry) with days remaining.
- **Smart Category Filtering**: Produce, Dairy & Eggs, Proteins, Pantry, Bakery, and Spices.
- **Dynamic Match Percentage**: Every recipe dynamically computes its ingredient availability percentage based on your live inventory.

### 2. Autonomous Daily Meal Planner
- Generates **Breakfast, Lunch, and Dinner plans** tailored to your current inventory with 1 tap.
- Automatically calculates:
  - Total daily calories, protein, carbs, and fats.
  - Active prep and cook durations.
  - Expiring ingredients saved by following the plan.
- Direct 1-tap transition from meal plan into active voice-guided cooking sessions.

### 3. Hands-Free Interactive Voice Cooking
- **Bidirectional Voice**: Integrated Android `SpeechRecognizer` and `TextToSpeech` engine.
- **Voice Command Triggers**:
  - *"Next step"* / *"Previous step"* / *"Done with step"*
  - *"Repeat step"* / *"Read step aloud"*
  - *"What ingredients do I need?"*
  - *"Start timer"* / *"Pause timer"* / *"How much time is left?"*
  - *"Chef tip"* / *"Why do I do this?"*
  - *"Finish cooking"*
- **Live In-Cooking Q&A**: Ask any culinary substitution or temperature question out loud during active cooking and receive voice answers from Chef AI.

### 4. Continuous Taste Learning & Kitchen Memory
- **Preference Profiles**: Configure dietary focus (e.g., High Protein, Mediterranean, Keto, Plant-Forward, Low Carb), max cooking times, spice tolerance, and disliked ingredients.
- **Active Memory Log**: Records user feedback, culinary observations, and session ratings to dynamically tune future recipe steps and ingredient substitutions.

### 5. Smart Scanner System
- Camera and vision simulation interface allowing quick ingredient recognition (Produce, Meat, Dairy, Pantry) to populate inventory in seconds.

### 6. AI Chef Chat (`Ask Mealio`)
- Context-aware culinary chatbot with quick action pills, voice-to-text input, TTS audio playback, and suggested follow-up prompts.

---

## 🏗️ System Architecture & Tech Stack

Mealio AI is built with modern, idiomatic Android practices following clean **MVVM (Model-View-ViewModel)** and **Unidirectional Data Flow (UDF)**.

```
┌─────────────────────────────────────────────────────────────┐
│                    Jetpack Compose UI                       │
│  (HomeScreen, InventoryScreen, RecipesScreen, CookingMode,  │
│   AskMealyScreen, ProfileAndMemoryScreen, ScanScreen)       │
└──────────────────────────────┬──────────────────────────────┘
                               │ Observes StateFlow / Dispatches Intents
┌──────────────────────────────▼──────────────────────────────┐
│                    KitchenViewModel                         │
│  - StateFlow UI State Management                            │
│  - Cooking Session Engine & Timer State Machine              │
│  - Speech Recognition & Voice Command Dispatcher             │
│  - Text-To-Speech Controller                                │
└──────────────────────────────┬──────────────────────────────┘
                               │
┌──────────────────────────────▼──────────────────────────────┐
│                    KitchenRepository                        │
│  - Assembles unified KitchenContext (Items, Prefs, Memory)  │
│  - AI Provider Routing (Gemini AI <-> Local Rule-Based AI)  │
└───────────────┬─────────────────────────────┬───────────────┘
                │                             │
┌───────────────▼─────────────┐ ┌─────────────▼───────────────┐
│     Room Local Database     │ │         AI Engines          │
│ - InventoryItemDao          │ │ - GeminiAiProvider (Cloud)  │
│ - RecipeDao                 │ │ - LocalRuleBasedAIProvider  │
│ - UserPreferencesDao        │ │   (Offline Fallback Engine) │
│ - KitchenMemoryDao          │ └─────────────────────────────┘
│ - CookingSessionDao         │
└─────────────────────────────┘
```

### Component Details
- **UI Toolkit**: 100% Jetpack Compose with Material Design 3 (M3) dynamic theming, custom high-contrast color palettes (`SageGreen`, `ToastedAmber`, `ExpiringRed`), and accessibility touch targets (≥48dp).
- **Local Persistence**: **Android Room 2.7+** with SQLite database (`mealy_ai_database`, v2) for zero-latency offline persistence.
- **Audio & Voice Processing**:
  - `SpeechRecognitionHelper`: Android `SpeechRecognizer` with RMS audio level analysis for responsive mic pulsations.
  - `AudioPermissionHandler`: Clean Compose runtime permission requesting with graceful fallback and settings navigation.
  - `TextToSpeechHelper`: Android `TextToSpeech` with auto-narration options.
- **AI Engine**:
  - **Gemini API (`gemini-2.5-flash`)**: High-speed generative recipe synthesis, culinary reasoning, and personalized chat.
  - **LocalRuleBasedAIProvider**: Resilient on-device heuristic engine ranking recipes by expiry urgency, pantry coverage, and macro goals even when offline.

---

## 🔄 End-to-End User & Data Workflows

### Workflow 1: Autonomous Waste-Eliminating Recipe Recommendation
1. User adds or scans groceries into inventory.
2. Expiration timestamps are evaluated. Items expiring within 3 days are weighted with high priority scores.
3. `KitchenRepository` compiles `KitchenContext` (Inventory + Expiry Urgency + User Preferences + Disliked Ingredients + Past Memory Logs).
4. AI calculates recipe rankings:
   $$\text{Rank Score} = (\text{Match \%} \times 0.4) + (\text{Expiry Priority Bonus} \times 0.4) + (\text{Preference Match} \times 0.2)$$
5. The UI surfaces top ranked recipes with badges highlighting expiring ingredients saved.

### Workflow 2: Hands-Free Voice Guided Cooking
1. User taps **"Start Interactive Voice Cooking"** on any recipe.
2. The `CookingModeScreen` launches in high-contrast view, automatically pre-loading timers and step 1 ingredients.
3. If auto-voice is enabled, the app vocalizes the first step immediately.
4. The cook speaks commands (*"Next step"*, *"Start 5 minute timer"*, *"What ingredients do I need?"*).
5. Speech is processed through `processVoiceCommand`:
   - Navigation commands advance the state machine without screen touch.
   - Timer commands start/pause background countdown jobs.
   - General queries query Chef AI for immediate voice answers.
6. Upon completing the final step, the app prompts for a rating and tasting note, which is logged to `KitchenMemoryLog` to tune future meals.

---

## ⚡ Why Mealio AI is Maximum Efficiency

1. **Financial & Environmental Efficiency**: An average household wastes over 30% of perishable groceries. Mealio AI actively prioritizes ingredients before they spoil, saving money on grocery bills.
2. **Time Efficiency**: Eliminates "decision fatigue" with 1-tap automated daily meal plans that combine pantry items intelligently.
3. **Kitchen Workflow Efficiency**: Hands-free voice navigation prevents constantly washing and drying hands to touch your smartphone screen while sautéing, kneading, or handling raw ingredients.
4. **On-Device Battery & Network Efficiency**: Utilizes lightweight Room Flow queries and local AI heuristics, ensuring instant responsiveness with minimal network overhead.

---

## 🛠️ Getting Started & Configuration

### Build & Run
- Built using Android Gradle Plugin with Kotlin DSL.
- Run tests:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- Build APK:
  ```bash
  gradle :app:assembleDebug
  ```

### API Keys (Optional Cloud Mode)
- Mealio AI operates out-of-the-box using its built-in rule-based intelligence engine.
- To enable Google Gemini AI cloud intelligence, set `GEMINI_API_KEY` in the AI Studio Secrets panel or your build configuration.
