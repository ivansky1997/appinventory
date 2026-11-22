package com.example.data

import com.example.data.local.InventoryItemEntity
import com.example.data.local.StockTransactionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class BackupPayload(
    val items: List<InventoryItemEntity>,
    val transactions: List<StockTransactionEntity>,
    val exportedAt: String? = null
)

object BackupSerializer {

    fun exportToJson(
        items: List<InventoryItemEntity>,
        transactions: List<StockTransactionEntity>
    ): String {
        val root = JSONObject().apply {
            put("app", "Portgear Inventory")
            put("version", 1)
            put("exportedAt", InventoryDateUtils.nowIsoTimestamp())

            val itemsArray = JSONArray()
            items.sortedWith { a, b -> NaturalSort.itemCodeComparator.compare(a.itemCode, b.itemCode) }
                .forEach { item ->
                    itemsArray.put(
                        JSONObject().apply {
                            put("itemCode", item.itemCode)
                            put("brand", item.brand)
                            put("modelName", item.modelName)
                            put("colorVariant", item.colorVariant)
                            put("size", item.size)
                            put("beginningStock", item.beginningStock)
                            put("reorderLevel", item.reorderLevel)
                            put("createdAt", item.createdAt)
                        }
                    )
                }
            put("items", itemsArray)

            val txArray = JSONArray()
            transactions.forEach { tx ->
                txArray.put(
                    JSONObject().apply {
                        put("type", tx.type)
                        put("itemCode", tx.itemCode)
                        put("quantity", tx.quantity)
                        put("notes", tx.notes)
                        put("dateIso", tx.dateIso)
                        put("createdAt", tx.createdAt)
                    }
                )
            }
            put("transactions", txArray)
        }
        return root.toString(2)
    }

    fun parseBackupJson(rawJson: String): Result<BackupPayload> {
        return runCatching {
            val root = JSONObject(rawJson.trim())
            val itemsJson = root.optJSONArray("items")
                ?: throw IllegalArgumentException("Invalid backup file: missing 'items' array.")
            val txJson = root.optJSONArray("transactions") ?: JSONArray()

            val items = mutableListOf<InventoryItemEntity>()
            val seenCodes = mutableSetOf<String>()
            for (i in 0 until itemsJson.length()) {
                val obj = itemsJson.getJSONObject(i)
                val code = obj.optString("itemCode").trim().uppercase(Locale.US)
                if (code.isEmpty() || !seenCodes.add(code)) continue

                val brand = obj.optString("brand").trim().uppercase(Locale.US).ifEmpty { "PORTGEAR" }
                val modelName = obj.optString("modelName").trim().ifEmpty { "Unnamed Bag" }
                val colorVariant = obj.optString("colorVariant").trim()
                val size = obj.optString("size").trim()
                val beginning = obj.optInt("beginningStock", 0).coerceAtLeast(0)
                val reorder = obj.optInt("reorderLevel", 0).coerceAtLeast(0)
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                items.add(
                    InventoryItemEntity(
                        itemCode = code,
                        brand = brand,
                        modelName = modelName,
                        colorVariant = colorVariant,
                        size = size,
                        beginningStock = beginning,
                        reorderLevel = reorder,
                        createdAt = createdAt
                    )
                )
            }

            val transactions = mutableListOf<StockTransactionEntity>()
            for (i in 0 until txJson.length()) {
                val obj = txJson.getJSONObject(i)
                val code = obj.optString("itemCode").trim().uppercase(Locale.US)
                val qty = obj.optInt("quantity", 0)
                if (code.isEmpty() || qty <= 0) continue

                val type = TransactionType.fromString(obj.optString("type")).name
                val notes = obj.optString("notes").trim()
                val dateIso = obj.optString("dateIso").trim().let {
                    if (InventoryDateUtils.isValidIsoDate(it)) it else InventoryDateUtils.todayIso()
                }
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                transactions.add(
                    StockTransactionEntity(
                        type = type,
                        itemCode = code,
                        quantity = qty,
                        notes = notes,
                        dateIso = dateIso,
                        createdAt = createdAt
                    )
                )
            }

            BackupPayload(
                items = items,
                transactions = transactions,
                exportedAt = root.optString("exportedAt").takeIf { it.isNotBlank() }
            )
        }
    }

    fun exportInventoryCsv(summaries: List<InventorySummaryItem>): String {
        val sb = StringBuilder()
        sb.appendLine("Item Code,Brand,Model / Item Name,Color / Variant,Size,Beginning Stock,Delivery (+),Sales (-),Current Stock,Reorder Level,Status")
        summaries.forEach { item ->
            sb.appendLine(
                listOf(
                    csvCell(item.itemCode),
                    csvCell(item.brand),
                    csvCell(item.modelName),
                    csvCell(item.colorVariant),
                    csvCell(item.size),
                    item.beginningStock.toString(),
                    item.deliveryTotal.toString(),
                    item.salesTotal.toString(),
                    item.currentStock.toString(),
                    item.reorderLevel.toString(),
                    csvCell(item.status.label)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    fun exportTransactionsCsv(logs: List<StockLogEntry>): String {
        val sb = StringBuilder()
        sb.appendLine("Date,Type,Item Code,Brand,Model,Color,Size,Quantity,Notes / Ref #")
        logs.forEach { log ->
            sb.appendLine(
                listOf(
                    csvCell(log.dateIso),
                    csvCell(log.type.label),
                    csvCell(log.itemCode),
                    csvCell(log.brand),
                    csvCell(log.modelName),
                    csvCell(log.colorVariant),
                    csvCell(log.size),
                    log.quantity.toString(),
                    csvCell(log.notes)
                ).joinToString(",")
            )
        }
        return sb.toString()
    }

    private fun csvCell(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }
}
