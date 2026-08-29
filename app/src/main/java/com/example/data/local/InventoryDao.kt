package com.example.data.local

import androidx.room.*
import com.example.data.model.FoodState
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY addedTimestamp DESC")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE state != 'DISCARDED' AND state != 'USED' ORDER BY expiryTimestamp ASC")
    fun getActiveInventory(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items ORDER BY addedTimestamp DESC")
    fun getAllInventory(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getById(id: Long): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE state != 'DISCARDED' AND state != 'USED' ORDER BY expiryTimestamp ASC")
    suspend fun getActiveInventoryList(): List<InventoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>)

    @Update
    suspend fun update(item: InventoryItem)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Delete
    suspend fun deleteItem(item: InventoryItem)

    @Query("UPDATE inventory_items SET state = :newState WHERE id = :id")
    suspend fun updateState(id: Long, newState: FoodState)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM inventory_items")
    suspend fun clearAll()
}
