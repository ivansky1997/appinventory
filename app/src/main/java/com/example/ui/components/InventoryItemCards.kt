package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.InventorySummaryItem
import com.example.data.StockStatus
import com.example.ui.theme.CrimsonAction
import com.example.ui.theme.EmeraldAction
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OnAmberSurface
import com.example.ui.theme.OnCrimsonSurface
import com.example.ui.theme.OnEmeraldSurface

@Composable
fun AdaptiveInventoryItemsList(
    items: List<InventorySummaryItem>,
    expandedItemCodes: Set<String>,
    onToggleExpand: (String) -> Unit,
    onQuickDelivery: (String) -> Unit,
    onQuickSale: (String) -> Unit,
    onEditItem: (InventorySummaryItem) -> Unit,
    onDeleteItem: (String) -> Unit,
    onAddNewItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingDeleteItem by remember { mutableStateOf<InventorySummaryItem?>(null) }

    if (items.isEmpty()) {
        EmptyInventoryCard(
            onAddNewItem = onAddNewItem,
            modifier = modifier
        )
    } else {
        BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
            val useWideTable = maxWidth >= 700.dp
            if (useWideTable) {
                InventoryWideTable(
                    items = items,
                    onQuickDelivery = onQuickDelivery,
                    onQuickSale = onQuickSale,
                    onEditItem = onEditItem,
                    onRequestDelete = { pendingDeleteItem = it }
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items.forEach { item ->
                        ExpandableInventoryItemCard(
                            item = item,
                            expanded = item.itemCode in expandedItemCodes,
                            onToggleExpand = { onToggleExpand(item.itemCode) },
                            onQuickDelivery = { onQuickDelivery(item.itemCode) },
                            onQuickSale = { onQuickSale(item.itemCode) },
                            onEditItem = { onEditItem(item) },
                            onRequestDelete = { pendingDeleteItem = item }
                        )
                    }
                }
            }
        }
    }

    pendingDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { pendingDeleteItem = null },
            title = { Text("Delete ${item.itemCode}?") },
            text = {
                Text(
                    "This removes ${item.modelName} (${item.brand}) and all of its delivery and sales logs from this device."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteItem(item.itemCode)
                        pendingDeleteItem = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAction)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExpandableInventoryItemCard(
    item: InventorySummaryItem,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onQuickDelivery: () -> Unit,
    onQuickSale: () -> Unit,
    onEditItem: () -> Unit,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stockColor = when (item.status) {
        StockStatus.IN_STOCK -> OnEmeraldSurface
        StockStatus.LOW_REORDER -> OnAmberSurface
        StockStatus.OUT_OF_STOCK -> OnCrimsonSurface
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = if (expanded) 1.5.dp else 1.dp,
            color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("inventory_card_${item.itemCode}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ItemCodeBadge(itemCode = item.itemCode)
                    BrandBadge(brand = item.brand)
                }
                Spacer(modifier = Modifier.width(6.dp))
                StatusBadge(status = item.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.modelName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.colorVariant} · ${item.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = item.currentStock.toString(),
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                            color = stockColor
                        )
                        Text(
                            text = "in stock",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expanded) "Collapse details" else "Expand details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLow,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricColumn(label = "Beg", value = item.beginningStock.toString())
                        MetricColumn(
                            label = "Del (+)",
                            value = "+${item.deliveryTotal}",
                            valueColor = OnEmeraldSurface
                        )
                        MetricColumn(
                            label = "Sales (-)",
                            value = "-${item.salesTotal}",
                            valueColor = OnCrimsonSurface
                        )
                        MetricColumn(
                            label = "Stock",
                            value = item.currentStock.toString(),
                            valueColor = stockColor
                        )
                        MetricColumn(label = "Reorder", value = item.reorderLevel.toString())
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onQuickDelivery,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldAction,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delivery", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = onQuickSale,
                            enabled = item.currentStock > 0,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonAction,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Remove,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sale", style = MaterialTheme.typography.labelMedium)
                        }

                        OutlinedButton(
                            onClick = onEditItem,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit item",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        IconButton(
                            onClick = onRequestDelete,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete item",
                                tint = CrimsonAction
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
            color = valueColor
        )
    }
}

@Composable
private fun InventoryWideTable(
    items: List<InventorySummaryItem>,
    onQuickDelivery: (String) -> Unit,
    onQuickSale: (String) -> Unit,
    onEditItem: (InventorySummaryItem) -> Unit,
    onRequestDelete: (InventorySummaryItem) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableHeaderCell("Code", Modifier.weight(0.95f))
                TableHeaderCell("Brand", Modifier.weight(1.05f))
                TableHeaderCell("Model / Color / Size", Modifier.weight(2.2f))
                TableHeaderCell("Beg", Modifier.weight(0.55f), TextAlign.End)
                TableHeaderCell("+Del", Modifier.weight(0.6f), TextAlign.End)
                TableHeaderCell("-Sale", Modifier.weight(0.6f), TextAlign.End)
                TableHeaderCell("Stock", Modifier.weight(0.65f), TextAlign.End)
                TableHeaderCell("Reord", Modifier.weight(0.6f), TextAlign.End)
                TableHeaderCell("Status", Modifier.weight(1.3f), TextAlign.Center)
                TableHeaderCell("Actions", Modifier.weight(1.3f), TextAlign.End)
            }

            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(0.95f)) {
                        ItemCodeBadge(itemCode = item.itemCode)
                    }
                    Box(modifier = Modifier.weight(1.05f)) {
                        BrandBadge(brand = item.brand)
                    }
                    Column(modifier = Modifier.weight(2.2f).padding(end = 8.dp)) {
                        Text(
                            text = item.modelName,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.colorVariant} · ${item.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    TableMonoCell(item.beginningStock.toString(), Modifier.weight(0.55f))
                    TableMonoCell("+${item.deliveryTotal}", Modifier.weight(0.6f), OnEmeraldSurface)
                    TableMonoCell("-${item.salesTotal}", Modifier.weight(0.6f), OnCrimsonSurface)
                    TableMonoCell(
                        item.currentStock.toString(),
                        Modifier.weight(0.65f),
                        when (item.status) {
                            StockStatus.IN_STOCK -> OnEmeraldSurface
                            StockStatus.LOW_REORDER -> OnAmberSurface
                            StockStatus.OUT_OF_STOCK -> OnCrimsonSurface
                        }
                    )
                    TableMonoCell(item.reorderLevel.toString(), Modifier.weight(0.6f))
                    Box(
                        modifier = Modifier.weight(1.3f),
                        contentAlignment = Alignment.Center
                    ) {
                        StatusBadge(status = item.status)
                    }
                    Row(
                        modifier = Modifier.weight(1.3f),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = { onQuickDelivery(item.itemCode) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Delivery", tint = EmeraldAction)
                        }
                        IconButton(
                            onClick = { onQuickSale(item.itemCode) },
                            enabled = item.currentStock > 0,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Record Sale", tint = CrimsonAction)
                        }
                        IconButton(
                            onClick = { onEditItem(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit Item")
                        }
                        IconButton(
                            onClick = { onRequestDelete(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Item", tint = CrimsonAction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align,
        modifier = modifier
    )
}

@Composable
private fun TableMonoCell(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Text(
        text = text,
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.SemiBold,
        style = MaterialTheme.typography.bodyMedium,
        color = color,
        textAlign = TextAlign.End,
        modifier = modifier
    )
}

@Composable
private fun EmptyInventoryCard(
    onAddNewItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Inventory2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No matching bag items",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Try clearing your search or filter, or add a new SKU to start tracking stock.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddNewItem,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add New Item")
            }
        }
    }
}
