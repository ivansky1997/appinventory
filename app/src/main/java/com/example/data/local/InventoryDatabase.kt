package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        InventoryItemEntity::class,
        StockTransactionEntity::class,
        AppMetadataEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class InventoryDatabase : RoomDatabase() {
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: InventoryDatabase? = null

        fun getInstance(context: Context): InventoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    InventoryDatabase::class.java,
                    "portgear_inventory.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
