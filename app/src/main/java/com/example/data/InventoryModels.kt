package com.example.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class BagBrand(val displayName: String, val codePrefix: String) {
    PORTGEAR("PORTGEAR", "PG"),
    AIRFIT("AIRFIT", "AF"),
    SOFISTICA("SOFISTICA", "SF");

    companion object {
        fun fromString(value: String): BagBrand? {
            val normalized = value.trim().uppercase(Locale.US)
            return entries.firstOrNull { it.name == normalized || it.displayName == normalized }
        }
    }
}

enum class StockStatus(val label: String) {
    OUT_OF_STOCK("OUT OF STOCK"),
    LOW_REORDER("LOW - REORDER"),
    IN_STOCK("IN STOCK")
}

enum class TransactionType(val label: String) {
    DELIVERY("DELIVERY"),
    SALE("SALE");

    companion object {
        fun fromString(value: String): TransactionType {
            return entries.firstOrNull { it.name.equals(value.trim(), ignoreCase = true) }
                ?: DELIVERY
        }
    }
}

data class InventorySummaryItem(
    val itemCode: String,
    val brand: String,
    val modelName: String,
    val colorVariant: String,
    val size: String,
    val beginningStock: Int,
    val deliveryTotal: Int,
    val salesTotal: Int,
    val currentStock: Int,
    val reorderLevel: Int,
    val status: StockStatus,
    val createdAt: Long
)

data class InventoryKpiSummary(
    val totalBeginning: Int = 0,
    val totalDeliveries: Int = 0,
    val totalSold: Int = 0,
    val currentStockOnHand: Int = 0,
    val lowOrOutOfStockCount: Int = 0,
    val totalSkus: Int = 0
)

data class StockLogEntry(
    val id: Long,
    val type: TransactionType,
    val itemCode: String,
    val brand: String,
    val modelName: String,
    val colorVariant: String,
    val size: String,
    val quantity: Int,
    val notes: String,
    val dateIso: String,
    val createdAt: Long
)

class InsufficientStockException(
    val itemCode: String,
    val availableStock: Int,
    val requestedQuantity: Int
) : IllegalStateException(
    "Cannot sell $requestedQuantity units of $itemCode. Only $availableStock in stock."
)

class DuplicateItemCodeException(val itemCode: String) : IllegalStateException(
    "Item Code $itemCode already exists."
)

object InventoryDateUtils {
    private val isoFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val timestampFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    private val fileDateFormat: SimpleDateFormat
        get() = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)

    fun todayIso(): String = isoFormat.format(Date())

    fun nowIsoTimestamp(): String = timestampFormat.format(Date())

    fun fileTimestamp(): String = fileDateFormat.format(Date())

    fun isValidIsoDate(value: String): Boolean {
        val trimmed = value.trim()
        if (!Regex("""^\d{4}-\d{2}-\d{2}$""").matches(trimmed)) return false
        return try {
            val fmt = isoFormat.apply { isLenient = false }
            fmt.parse(trimmed) != null
        } catch (_: Exception) {
            false
        }
    }
}

object NaturalSort {
    private val chunkRegex = Regex("""(\d+|\D+)""")

    val itemCodeComparator: Comparator<String> = Comparator { a, b ->
        val normA = a.trim().uppercase(Locale.US)
        val normB = b.trim().uppercase(Locale.US)
        val chunksA = chunkRegex.findAll(normA).map { it.value }.toList()
        val chunksB = chunkRegex.findAll(normB).map { it.value }.toList()
        val count = minOf(chunksA.size, chunksB.size)

        for (i in 0 until count) {
            val ca = chunksA[i]
            val cb = chunksB[i]
            val numA = ca.toLongOrNull()
            val numB = cb.toLongOrNull()
            val cmp = if (numA != null && numB != null) {
                val numericCmp = numA.compareTo(numB)
                if (numericCmp != 0) numericCmp else ca.length.compareTo(cb.length)
            } else {
                ca.compareTo(cb)
            }
            if (cmp != 0) return@Comparator cmp
        }
        chunksA.size.compareTo(chunksB.size)
    }
}
