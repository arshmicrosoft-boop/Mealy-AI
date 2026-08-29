package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val ingredientListType = Types.newParameterizedType(List::class.java, RecipeIngredient::class.java)
    private val ingredientListAdapter = moshi.adapter<List<RecipeIngredient>>(ingredientListType)

    private val stepListType = Types.newParameterizedType(List::class.java, RecipeStep::class.java)
    private val stepListAdapter = moshi.adapter<List<RecipeStep>>(stepListType)

    private val subListType = Types.newParameterizedType(List::class.java, SubstitutionAdvice::class.java)
    private val subListAdapter = moshi.adapter<List<SubstitutionAdvice>>(subListType)

    @TypeConverter
    fun fromFoodState(state: FoodState): String = state.name

    @TypeConverter
    fun toFoodState(value: String): FoodState = try {
        FoodState.valueOf(value)
    } catch (e: Exception) {
        FoodState.STORED
    }

    @TypeConverter
    fun fromIngredientCategory(category: IngredientCategory): String = category.name

    @TypeConverter
    fun toIngredientCategory(value: String): IngredientCategory = try {
        IngredientCategory.valueOf(value)
    } catch (e: Exception) {
        IngredientCategory.OTHER
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String = stringListAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun toStringList(json: String?): List<String> = if (json.isNullOrEmpty()) emptyList() else try {
        stringListAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    @TypeConverter
    fun fromIngredientList(list: List<RecipeIngredient>?): String = ingredientListAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun toIngredientList(json: String?): List<RecipeIngredient> = if (json.isNullOrEmpty()) emptyList() else try {
        ingredientListAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    @TypeConverter
    fun fromStepList(list: List<RecipeStep>?): String = stepListAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun toStepList(json: String?): List<RecipeStep> = if (json.isNullOrEmpty()) emptyList() else try {
        stepListAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    @TypeConverter
    fun fromSubList(list: List<SubstitutionAdvice>?): String = subListAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun toSubList(json: String?): List<SubstitutionAdvice> = if (json.isNullOrEmpty()) emptyList() else try {
        subListAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
