package com.example.data.ai

/**
 * GeminiProvider implementation in Kotlin that uses GEMINI_API_KEY to fetch recipe
 * recommendations based on a provided list of ingredients and kitchen context.
 */
class GeminiProvider(
    geminiService: GeminiService = GeminiService(),
    fallbackProvider: AIProvider = LocalRuleBasedAIProvider()
) : GeminiAIProvider(geminiService, fallbackProvider)

