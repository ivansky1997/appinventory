package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class InventoryItemEntity(
    @PrimaryKey val itemCode: String,
    val brand: String,
    val modelName: String,
    val colorVariant: String,
    val size: String,
    val beginningStock: Int,
    val reorderLevel: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "stock_transactions",
    indices = [Index(value = ["itemCode"]), Index(value = ["type"]), Index(value = ["dateIso"])]
)
data class StockTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "DELIVERY" or "SALE"
    val itemCode: String,
    val quantity: Int,
    val notes: String,
    val dateIso: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_metadata")
data class AppMetadataEntity(
    @PrimaryKey val key: String,
    val value: String
)
