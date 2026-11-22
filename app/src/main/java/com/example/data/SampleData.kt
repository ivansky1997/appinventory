package com.example.data

import com.example.data.local.InventoryItemEntity
import com.example.data.local.StockTransactionEntity

object SampleData {
    fun defaultItems(): List<InventoryItemEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // PORTGEAR
            InventoryItemEntity(
                itemCode = "PG-001",
                brand = BagBrand.PORTGEAR.displayName,
                modelName = "Hardcase Luggage 8-Wheel",
                colorVariant = "Matte Charcoal",
                size = "24\" Medium",
                beginningStock = 12,
                reorderLevel = 5,
                createdAt = now - 80_000
            ),
            InventoryItemEntity(
                itemCode = "PG-002",
                brand = BagBrand.PORTGEAR.displayName,
                modelName = "Expandable Travel Duffel",
                colorVariant = "Olive Tactical",
                size = "45L",
                beginningStock = 10,
                reorderLevel = 4,
                createdAt = now - 70_000
            ),
            InventoryItemEntity(
                itemCode = "PG-003",
                brand = BagBrand.PORTGEAR.displayName,
                modelName = "Anti-Theft Laptop Backpack",
                colorVariant = "Jet Black",
                size = "15.6\"",
                beginningStock = 14,
                reorderLevel = 6,
                createdAt = now - 60_000
            ),
            // AIRFIT
            InventoryItemEntity(
                itemCode = "AF-001",
                brand = BagBrand.AIRFIT.displayName,
                modelName = "Lightweight Cabin Trolley",
                colorVariant = "Cobalt Navy",
                size = "20\" Cabin",
                beginningStock = 8,
                reorderLevel = 4,
                createdAt = now - 50_000
            ),
            InventoryItemEntity(
                itemCode = "AF-002",
                brand = BagBrand.AIRFIT.displayName,
                modelName = "Gym & Sports Sling Bag",
                colorVariant = "Graphite Gray",
                size = "Compact",
                beginningStock = 15,
                reorderLevel = 5,
                createdAt = now - 40_000
            ),
            // SOFISTICA
            InventoryItemEntity(
                itemCode = "SF-001",
                brand = BagBrand.SOFISTICA.displayName,
                modelName = "Classic Quilted Tote Bag",
                colorVariant = "Bordeaux Wine",
                size = "Large",
                beginningStock = 9,
                reorderLevel = 4,
                createdAt = now - 30_000
            ),
            InventoryItemEntity(
                itemCode = "SF-002",
                brand = BagBrand.SOFISTICA.displayName,
                modelName = "Executive Crossbody Bag",
                colorVariant = "Cognac Tan",
                size = "Medium",
                beginningStock = 6,
                reorderLevel = 4,
                createdAt = now - 20_000
            ),
            InventoryItemEntity(
                itemCode = "SF-003",
                brand = BagBrand.SOFISTICA.displayName,
                modelName = "Mini Sling Fashion Bag",
                colorVariant = "Blush Pearl",
                size = "Mini",
                beginningStock = 4,
                reorderLevel = 3,
                createdAt = now - 10_000
            )
        )
    }

    fun defaultTransactions(): List<StockTransactionEntity> {
        val today = InventoryDateUtils.todayIso()
        val now = System.currentTimeMillis()
        return listOf(
            // Deliveries
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "PG-001",
                quantity = 8,
                notes = "DR-4021 Main Warehouse Restock",
                dateIso = today,
                createdAt = now - 90_000
            ),
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "PG-002",
                quantity = 4,
                notes = "DR-4022 Portgear Dispatch",
                dateIso = today,
                createdAt = now - 85_000
            ),
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "PG-003",
                quantity = 6,
                notes = "DR-4023 Tech Pack Batch",
                dateIso = today,
                createdAt = now - 80_000
            ),
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "AF-001",
                quantity = 5,
                notes = "DR-4025 Airfit Flight Series",
                dateIso = today,
                createdAt = now - 75_000
            ),
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "AF-002",
                quantity = 10,
                notes = "DR-4026 Fitness Promo Stock",
                dateIso = today,
                createdAt = now - 70_000
            ),
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "SF-001",
                quantity = 3,
                notes = "DR-4028 Sofistica Boutique",
                dateIso = today,
                createdAt = now - 65_000
            ),
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "SF-002",
                quantity = 4,
                notes = "DR-4029 Sofistica Boutique",
                dateIso = today,
                createdAt = now - 60_000
            ),
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = "SF-003",
                quantity = 2,
                notes = "DR-4030 Mini Collection",
                dateIso = today,
                createdAt = now - 55_000
            ),

            // Sales
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "PG-001",
                quantity = 6,
                notes = "INV-1081 Corporate Travel Order",
                dateIso = today,
                createdAt = now - 50_000
            ),
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "PG-002",
                quantity = 11,
                notes = "INV-1082 Weekend Expo Sales",
                dateIso = today,
                createdAt = now - 45_000
            ),
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "PG-003",
                quantity = 5,
                notes = "INV-1084 Walk-in Retail",
                dateIso = today,
                createdAt = now - 40_000
            ),
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "AF-001",
                quantity = 13,
                notes = "INV-1086 Airline Crew Bulk Sale",
                dateIso = today,
                createdAt = now - 35_000
            ),
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "AF-002",
                quantity = 9,
                notes = "INV-1088 Sports Club Order",
                dateIso = today,
                createdAt = now - 30_000
            ),
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "SF-001",
                quantity = 9,
                notes = "INV-1090 Boutique Counter",
                dateIso = today,
                createdAt = now - 25_000
            ),
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "SF-002",
                quantity = 2,
                notes = "INV-1091 Gift Box Order",
                dateIso = today,
                createdAt = now - 20_000
            ),
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = "SF-003",
                quantity = 6,
                notes = "INV-1093 Flash Sale Sold Out",
                dateIso = today,
                createdAt = now - 15_000
            )
        )
    }
}
