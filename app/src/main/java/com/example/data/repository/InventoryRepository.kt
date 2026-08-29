package com.example.data.repository

import com.example.data.local.InventoryDao
import com.example.data.model.FoodState
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface and implementation for managing 'inventory_items' locally.
 */
interface InventoryRepository {
    val activeInventory: Flow<List<InventoryItem>>
    val allInventory: Flow<List<InventoryItem>>

    suspend fun getById(id: Long): InventoryItem?
    suspend fun getActiveInventoryList(): List<InventoryItem>
    suspend fun insertItem(item: InventoryItem): Long
    suspend fun insertAll(items: List<InventoryItem>)
    suspend fun updateItem(item: InventoryItem)
    suspend fun updateState(id: Long, newState: FoodState)
    suspend fun deleteItem(item: InventoryItem)
    suspend fun deleteById(id: Long)
    suspend fun clearAll()
}

class DefaultInventoryRepository(
    private val inventoryDao: InventoryDao
) : InventoryRepository {

    override val activeInventory: Flow<List<InventoryItem>> = inventoryDao.getActiveInventory()

    override val allInventory: Flow<List<InventoryItem>> = inventoryDao.getAllInventory()

    override suspend fun getById(id: Long): InventoryItem? {
        return inventoryDao.getById(id)
    }

    override suspend fun getActiveInventoryList(): List<InventoryItem> {
        return inventoryDao.getActiveInventoryList()
    }

    override suspend fun insertItem(item: InventoryItem): Long {
        return inventoryDao.insertItem(item)
    }

    override suspend fun insertAll(items: List<InventoryItem>) {
        inventoryDao.insertAll(items)
    }

    override suspend fun updateItem(item: InventoryItem) {
        inventoryDao.updateItem(item)
    }

    override suspend fun updateState(id: Long, newState: FoodState) {
        inventoryDao.updateState(id, newState)
    }

    override suspend fun deleteItem(item: InventoryItem) {
        inventoryDao.deleteItem(item)
    }

    override suspend fun deleteById(id: Long) {
        inventoryDao.deleteById(id)
    }

    override suspend fun clearAll() {
        inventoryDao.clearAll()
    }
}
