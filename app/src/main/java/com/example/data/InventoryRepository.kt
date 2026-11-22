package com.example.data

import com.example.data.local.AppMetadataEntity
import com.example.data.local.InventoryDao
import com.example.data.local.InventoryItemEntity
import com.example.data.local.StockTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Locale

class InventoryRepository(private val dao: InventoryDao) {

    companion object {
        private const val META_SEEDED_V1 = "seeded_v1"
    }

    val inventorySummaries: Flow<List<InventorySummaryItem>> = combine(
        dao.observeAllItems(),
        dao.observeAllTransactions()
    ) { items, transactions ->
        computeSummaries(items, transactions)
    }

    val stockLogs: Flow<List<StockLogEntry>> = combine(
        dao.observeAllItems(),
        dao.observeAllTransactions()
    ) { items, transactions ->
        val itemMap = items.associateBy { it.itemCode }
        transactions.map { tx ->
            val matchedItem = itemMap[tx.itemCode]
            StockLogEntry(
                id = tx.id,
                type = TransactionType.fromString(tx.type),
                itemCode = tx.itemCode,
                brand = matchedItem?.brand ?: "UNKNOWN",
                modelName = matchedItem?.modelName ?: "Deleted Item",
                colorVariant = matchedItem?.colorVariant ?: "-",
                size = matchedItem?.size ?: "-",
                quantity = tx.quantity,
                notes = tx.notes,
                dateIso = tx.dateIso,
                createdAt = tx.createdAt
            )
        }
    }

    suspend fun ensureSampleDataSeeded() {
        val alreadySeeded = dao.getMetadata(META_SEEDED_V1) == "true"
        if (!alreadySeeded) {
            val existingItems = dao.getAllItemsOnce()
            if (existingItems.isEmpty()) {
                dao.replaceAllData(
                    items = SampleData.defaultItems(),
                    transactions = SampleData.defaultTransactions()
                )
            }
            dao.setMetadata(AppMetadataEntity(META_SEEDED_V1, "true"))
        }
    }

    suspend fun addItem(
        rawItemCode: String,
        brand: String,
        modelName: String,
        colorVariant: String,
        size: String,
        beginningStock: Int,
        reorderLevel: Int
    ): Result<Unit> = runCatching {
        val normalizedCode = rawItemCode.trim().uppercase(Locale.US)
        require(normalizedCode.isNotEmpty()) { "Item Code is required." }
        require(modelName.trim().isNotEmpty()) { "Model / Item Name is required." }
        require(beginningStock >= 0) { "Beginning stock cannot be negative." }
        require(reorderLevel >= 0) { "Reorder level cannot be negative." }

        if (dao.getItemByCode(normalizedCode) != null) {
            throw DuplicateItemCodeException(normalizedCode)
        }

        dao.insertItem(
            InventoryItemEntity(
                itemCode = normalizedCode,
                brand = brand.trim().uppercase(Locale.US),
                modelName = modelName.trim(),
                colorVariant = colorVariant.trim().ifEmpty { "Standard" },
                size = size.trim().ifEmpty { "One Size" },
                beginningStock = beginningStock,
                reorderLevel = reorderLevel
            )
        )
    }

    suspend fun updateItem(
        itemCode: String,
        brand: String,
        modelName: String,
        colorVariant: String,
        size: String,
        beginningStock: Int,
        reorderLevel: Int
    ): Result<Unit> = runCatching {
        val existing = dao.getItemByCode(itemCode)
            ?: throw IllegalArgumentException("Item $itemCode not found.")
        require(modelName.trim().isNotEmpty()) { "Model / Item Name is required." }
        require(beginningStock >= 0) { "Beginning stock cannot be negative." }
        require(reorderLevel >= 0) { "Reorder level cannot be negative." }

        dao.updateItem(
            existing.copy(
                brand = brand.trim().uppercase(Locale.US),
                modelName = modelName.trim(),
                colorVariant = colorVariant.trim().ifEmpty { "Standard" },
                size = size.trim().ifEmpty { "One Size" },
                beginningStock = beginningStock,
                reorderLevel = reorderLevel
            )
        )
    }

    suspend fun deleteItem(itemCode: String) {
        dao.deleteItemAndTransactions(itemCode)
    }

    suspend fun recordDelivery(
        itemCode: String,
        quantity: Int,
        notes: String,
        dateIso: String
    ): Result<Unit> = runCatching {
        val normalizedCode = itemCode.trim().uppercase(Locale.US)
        val item = dao.getItemByCode(normalizedCode)
            ?: throw IllegalArgumentException("Please select a valid Item Code.")
        require(quantity > 0) { "Delivery quantity must be greater than 0." }
        val validDate = dateIso.trim().let {
            if (InventoryDateUtils.isValidIsoDate(it)) it else throw IllegalArgumentException("Use YYYY-MM-DD date format.")
        }

        dao.insertTransaction(
            StockTransactionEntity(
                type = TransactionType.DELIVERY.name,
                itemCode = item.itemCode,
                quantity = quantity,
                notes = notes.trim(),
                dateIso = validDate
            )
        )
    }

    suspend fun recordSale(
        itemCode: String,
        quantity: Int,
        notes: String,
        dateIso: String
    ): Result<Unit> = runCatching {
        val normalizedCode = itemCode.trim().uppercase(Locale.US)
        val item = dao.getItemByCode(normalizedCode)
            ?: throw IllegalArgumentException("Please select a valid Item Code.")
        require(quantity > 0) { "Sale quantity must be greater than 0." }
        val validDate = dateIso.trim().let {
            if (InventoryDateUtils.isValidIsoDate(it)) it else throw IllegalArgumentException("Use YYYY-MM-DD date format.")
        }

        val deliveries = dao.getTransactionSumForItem(item.itemCode, TransactionType.DELIVERY.name)
        val sales = dao.getTransactionSumForItem(item.itemCode, TransactionType.SALE.name)
        val currentStock = item.beginningStock + deliveries - sales

        if (quantity > currentStock) {
            throw InsufficientStockException(
                itemCode = item.itemCode,
                availableStock = currentStock,
                requestedQuantity = quantity
            )
        }

        dao.insertTransaction(
            StockTransactionEntity(
                type = TransactionType.SALE.name,
                itemCode = item.itemCode,
                quantity = quantity,
                notes = notes.trim(),
                dateIso = validDate
            )
        )
    }

    suspend fun deleteTransaction(id: Long) {
        dao.deleteTransactionById(id)
    }

    suspend fun exportBackupJson(): String {
        val items = dao.getAllItemsOnce()
        val transactions = dao.getAllTransactionsOnce()
        return BackupSerializer.exportToJson(items, transactions)
    }

    suspend fun importBackupJson(rawJson: String, replaceExisting: Boolean): Result<Pair<Int, Int>> = runCatching {
        val payload = BackupSerializer.parseBackupJson(rawJson).getOrThrow()
        if (replaceExisting) {
            dao.replaceAllData(payload.items, payload.transactions)
        } else {
            dao.upsertItems(payload.items)
            val existingTx = dao.getAllTransactionsOnce()
            val existingKeys = existingTx.map {
                "${it.type}|${it.itemCode}|${it.quantity}|${it.dateIso}|${it.notes}|${it.createdAt}"
            }.toSet()
            val newTx = payload.transactions.filter {
                "${it.type}|${it.itemCode}|${it.quantity}|${it.dateIso}|${it.notes}|${it.createdAt}" !in existingKeys
            }
            if (newTx.isNotEmpty()) {
                dao.insertTransactions(newTx)
            }
        }
        dao.setMetadata(AppMetadataEntity(META_SEEDED_V1, "true"))
        payload.items.size to payload.transactions.size
    }

    suspend fun restoreSampleData() {
        dao.replaceAllData(
            items = SampleData.defaultItems(),
            transactions = SampleData.defaultTransactions()
        )
        dao.setMetadata(AppMetadataEntity(META_SEEDED_V1, "true"))
    }

    suspend fun clearAllData() {
        dao.replaceAllData(emptyList(), emptyList())
        dao.setMetadata(AppMetadataEntity(META_SEEDED_V1, "true"))
    }

    private fun computeSummaries(
        items: List<InventoryItemEntity>,
        transactions: List<StockTransactionEntity>
    ): List<InventorySummaryItem> {
        val deliveriesByCode = mutableMapOf<String, Int>()
        val salesByCode = mutableMapOf<String, Int>()

        for (tx in transactions) {
            when (TransactionType.fromString(tx.type)) {
                TransactionType.DELIVERY -> {
                    deliveriesByCode[tx.itemCode] = (deliveriesByCode[tx.itemCode] ?: 0) + tx.quantity
                }
                TransactionType.SALE -> {
                    salesByCode[tx.itemCode] = (salesByCode[tx.itemCode] ?: 0) + tx.quantity
                }
            }
        }

        return items
            .sortedWith { a, b -> NaturalSort.itemCodeComparator.compare(a.itemCode, b.itemCode) }
            .map { item ->
                val delTotal = deliveriesByCode[item.itemCode] ?: 0
                val saleTotal = salesByCode[item.itemCode] ?: 0
                val current = item.beginningStock + delTotal - saleTotal
                val status = when {
                    current <= 0 -> StockStatus.OUT_OF_STOCK
                    current <= item.reorderLevel -> StockStatus.LOW_REORDER
                    else -> StockStatus.IN_STOCK
                }
                InventorySummaryItem(
                    itemCode = item.itemCode,
                    brand = item.brand,
                    modelName = item.modelName,
                    colorVariant = item.colorVariant,
                    size = item.size,
                    beginningStock = item.beginningStock,
                    deliveryTotal = delTotal,
                    salesTotal = saleTotal,
                    currentStock = current,
                    reorderLevel = item.reorderLevel,
                    status = status,
                    createdAt = item.createdAt
                )
            }
    }
}
