package com.example

import com.example.data.DuplicateItemCodeException
import com.example.data.InsufficientStockException
import com.example.data.InventoryRepository
import com.example.data.NaturalSort
import com.example.data.StockStatus
import com.example.data.local.AppMetadataEntity
import com.example.data.local.InventoryDao
import com.example.data.local.InventoryItemEntity
import com.example.data.local.StockTransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeInventoryDao : InventoryDao {
    private val itemsFlow = MutableStateFlow<List<InventoryItemEntity>>(emptyList())
    private val txFlow = MutableStateFlow<List<StockTransactionEntity>>(emptyList())
    private val meta = mutableMapOf<String, String>()
    private var nextTxId = 1L

    override fun observeAllItems(): Flow<List<InventoryItemEntity>> = itemsFlow

    override suspend fun getAllItemsOnce(): List<InventoryItemEntity> = itemsFlow.value

    override suspend fun getItemByCode(itemCode: String): InventoryItemEntity? =
        itemsFlow.value.firstOrNull { it.itemCode == itemCode }

    override suspend fun insertItem(item: InventoryItemEntity) {
        if (itemsFlow.value.any { it.itemCode == item.itemCode }) {
            throw IllegalStateException("Duplicate key ${item.itemCode}")
        }
        itemsFlow.value = itemsFlow.value + item
    }

    override suspend fun upsertItems(items: List<InventoryItemEntity>) {
        val map = itemsFlow.value.associateBy { it.itemCode }.toMutableMap()
        items.forEach { map[it.itemCode] = it }
        itemsFlow.value = map.values.toList()
    }

    override suspend fun updateItem(item: InventoryItemEntity) {
        itemsFlow.value = itemsFlow.value.map { if (it.itemCode == item.itemCode) item else it }
    }

    override suspend fun deleteItemByCode(itemCode: String) {
        itemsFlow.value = itemsFlow.value.filterNot { it.itemCode == itemCode }
    }

    override fun observeAllTransactions(): Flow<List<StockTransactionEntity>> =
        txFlow.map { list -> list.sortedWith(compareByDescending<StockTransactionEntity> { it.dateIso }.thenByDescending { it.createdAt }.thenByDescending { it.id }) }

    override suspend fun getAllTransactionsOnce(): List<StockTransactionEntity> = txFlow.value

    override suspend fun getTransactionSumForItem(itemCode: String, type: String): Int =
        txFlow.value.filter { it.itemCode == itemCode && it.type == type }.sumOf { it.quantity }

    override suspend fun insertTransaction(transaction: StockTransactionEntity): Long {
        val assignedId = if (transaction.id == 0L) nextTxId++ else transaction.id
        txFlow.value = txFlow.value + transaction.copy(id = assignedId)
        return assignedId
    }

    override suspend fun insertTransactions(transactions: List<StockTransactionEntity>) {
        val added = transactions.map { tx ->
            if (tx.id == 0L) tx.copy(id = nextTxId++) else tx
        }
        txFlow.value = txFlow.value + added
    }

    override suspend fun deleteTransactionById(id: Long) {
        txFlow.value = txFlow.value.filterNot { it.id == id }
    }

    override suspend fun deleteTransactionsForItem(itemCode: String) {
        txFlow.value = txFlow.value.filterNot { it.itemCode == itemCode }
    }

    override suspend fun deleteAllTransactions() {
        txFlow.value = emptyList()
    }

    override suspend fun deleteAllItems() {
        itemsFlow.value = emptyList()
    }

    override suspend fun getMetadata(key: String): String? = meta[key]

    override suspend fun setMetadata(entry: AppMetadataEntity) {
        meta[entry.key] = entry.value
    }
}

class InventoryRepositoryTest {

    private lateinit var repository: InventoryRepository

    @Before
    fun setup() {
        repository = InventoryRepository(FakeInventoryDao())
    }

    @Test
    fun `sample data seeds all three brands and computes statuses`() = runTest {
        repository.ensureSampleDataSeeded()
        val items = repository.inventorySummaries.first()

        assertEquals(8, items.size)
        assertEquals(
            listOf("AF-001", "AF-002", "PG-001", "PG-002", "PG-003", "SF-001", "SF-002", "SF-003"),
            items.map { it.itemCode }
        )

        val af001 = items.first { it.itemCode == "AF-001" }
        assertEquals(0, af001.currentStock)
        assertEquals(StockStatus.OUT_OF_STOCK, af001.status)

        val pg002 = items.first { it.itemCode == "PG-002" }
        assertEquals(3, pg002.currentStock)
        assertEquals(StockStatus.LOW_REORDER, pg002.status)

        val pg001 = items.first { it.itemCode == "PG-001" }
        assertEquals(14, pg001.currentStock)
        assertEquals(StockStatus.IN_STOCK, pg001.status)
    }

    @Test
    fun `delivery increases stock and overselling is blocked`() = runTest {
        repository.ensureSampleDataSeeded()

        val blockedSale = repository.recordSale("AF-001", 1, "INV-999", "2026-11-22")
        assertTrue(blockedSale.isFailure)
        assertTrue(blockedSale.exceptionOrNull() is InsufficientStockException)

        val delivery = repository.recordDelivery("AF-001", 5, "DR-999", "2026-11-22")
        assertTrue(delivery.isSuccess)

        val afterDelivery = repository.inventorySummaries.first().first { it.itemCode == "AF-001" }
        assertEquals(5, afterDelivery.currentStock)
        assertEquals(StockStatus.IN_STOCK, afterDelivery.status)

        val validSale = repository.recordSale("AF-001", 3, "INV-1000", "2026-11-22")
        assertTrue(validSale.isSuccess)

        val afterSale = repository.inventorySummaries.first().first { it.itemCode == "AF-001" }
        assertEquals(2, afterSale.currentStock)
        assertEquals(StockStatus.LOW_REORDER, afterSale.status)
    }

    @Test
    fun `adding new items auto-sorts and prevents duplicates`() = runTest {
        repository.ensureSampleDataSeeded()

        val added = repository.addItem(
            rawItemCode = "pg-010",
            brand = "PORTGEAR",
            modelName = "Commuter Briefcase",
            colorVariant = "Slate",
            size = "16\"",
            beginningStock = 7,
            reorderLevel = 3
        )
        assertTrue(added.isSuccess)

        val duplicate = repository.addItem(
            rawItemCode = "PG-010",
            brand = "PORTGEAR",
            modelName = "Dup",
            colorVariant = "Black",
            size = "M",
            beginningStock = 1,
            reorderLevel = 1
        )
        assertTrue(duplicate.isFailure)
        assertTrue(duplicate.exceptionOrNull() is DuplicateItemCodeException)

        val codes = repository.inventorySummaries.first().map { it.itemCode }
        assertEquals(
            listOf("AF-001", "AF-002", "PG-001", "PG-002", "PG-003", "PG-010", "SF-001", "SF-002", "SF-003"),
            codes
        )
    }

    @Test
    fun `natural sort orders item codes alphabetically and numerically`() {
        val sorted = listOf("PG-010", "SF-001", "PG-002", "AF-001", "PG-1")
            .sortedWith(NaturalSort.itemCodeComparator)
        assertEquals(listOf("AF-001", "PG-1", "PG-002", "PG-010", "SF-001"), sorted)
    }
}
