package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory_items")
    fun observeAllItems(): Flow<List<InventoryItemEntity>>

    @Query("SELECT * FROM inventory_items")
    suspend fun getAllItemsOnce(): List<InventoryItemEntity>

    @Query("SELECT * FROM inventory_items WHERE itemCode = :itemCode LIMIT 1")
    suspend fun getItemByCode(itemCode: String): InventoryItemEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertItem(item: InventoryItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertItems(items: List<InventoryItemEntity>)

    @Update
    suspend fun updateItem(item: InventoryItemEntity)

    @Query("DELETE FROM inventory_items WHERE itemCode = :itemCode")
    suspend fun deleteItemByCode(itemCode: String)

    @Query("SELECT * FROM stock_transactions ORDER BY dateIso DESC, createdAt DESC, id DESC")
    fun observeAllTransactions(): Flow<List<StockTransactionEntity>>

    @Query("SELECT * FROM stock_transactions ORDER BY dateIso DESC, createdAt DESC, id DESC")
    suspend fun getAllTransactionsOnce(): List<StockTransactionEntity>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM stock_transactions WHERE itemCode = :itemCode AND type = :type")
    suspend fun getTransactionSumForItem(itemCode: String, type: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<StockTransactionEntity>)

    @Query("DELETE FROM stock_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM stock_transactions WHERE itemCode = :itemCode")
    suspend fun deleteTransactionsForItem(itemCode: String)

    @Query("DELETE FROM stock_transactions")
    suspend fun deleteAllTransactions()

    @Query("DELETE FROM inventory_items")
    suspend fun deleteAllItems()

    @Query("SELECT value FROM app_metadata WHERE key = :key LIMIT 1")
    suspend fun getMetadata(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMetadata(entry: AppMetadataEntity)

    @Transaction
    suspend fun replaceAllData(
        items: List<InventoryItemEntity>,
        transactions: List<StockTransactionEntity>
    ) {
        deleteAllTransactions()
        deleteAllItems()
        upsertItems(items)
        insertTransactions(transactions)
    }

    @Transaction
    suspend fun deleteItemAndTransactions(itemCode: String) {
        deleteTransactionsForItem(itemCode)
        deleteItemByCode(itemCode)
    }
}
