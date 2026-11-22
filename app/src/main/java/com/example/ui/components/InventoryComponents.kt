package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.InventoryKpiSummary
import com.example.data.StockStatus
import com.example.ui.BrandFilter
import com.example.ui.theme.AirfitBg
import com.example.ui.theme.AirfitFg
import com.example.ui.theme.AmberAction
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.CrimsonAction
import com.example.ui.theme.CrimsonSurface
import com.example.ui.theme.EmeraldAction
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.ForestPrimary
import com.example.ui.theme.ForestPrimaryDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OnAmberSurface
import com.example.ui.theme.OnCrimsonSurface
import com.example.ui.theme.OnEmeraldSurface
import com.example.ui.theme.PortgearBg
import com.example.ui.theme.PortgearFg
import com.example.ui.theme.SofisticaBg
import com.example.ui.theme.SofisticaFg

@Composable
fun ItemCodeBadge(
    itemCode: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Text(
            text = itemCode,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun BrandBadge(
    brand: String,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (brand.uppercase()) {
        "PORTGEAR" -> PortgearBg to PortgearFg
        "AIRFIT" -> AirfitBg to AirfitFg
        "SOFISTICA" -> SofisticaBg to SofisticaFg
        else -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = bg
    ) {
        Text(
            text = brand.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusBadge(
    status: StockStatus,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status) {
        StockStatus.IN_STOCK -> EmeraldSurface to OnEmeraldSurface
        StockStatus.LOW_REORDER -> AmberSurface to OnAmberSurface
        StockStatus.OUT_OF_STOCK -> CrimsonSurface to OnCrimsonSurface
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = bg
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(fg)
            )
            Text(
                text = status.label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InventoryHeaderBanner(
    skuCount: Int,
    onAddNewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(ForestPrimary, ForestPrimaryDark)
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Inventory2,
                        contentDescription = null,
                        tint = Color(0xFF8CE0B7),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "PORTGEAR INVENTORY",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF9DE6C2),
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bags Stock Control",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
                )
                Text(
                    text = "PORTGEAR · AIRFIT · SOFISTICA · $skuCount SKUs",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                onClick = onAddNewClick,
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("header_add_item_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AddBox,
                        contentDescription = "Add New Item",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "New Item",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun KpiSummaryStrip(
    kpi: InventoryKpiSummary,
    onLowStockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val isWide = maxWidth >= 680.dp
        if (isWide) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiTile(
                    label = "Total Beginning",
                    value = kpi.totalBeginning.toString(),
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    label = "Deliveries (+)",
                    value = "+${kpi.totalDeliveries}",
                    containerColor = EmeraldSurface,
                    contentColor = OnEmeraldSurface,
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    label = "Total Sold (-)",
                    value = "-${kpi.totalSold}",
                    containerColor = CrimsonSurface,
                    contentColor = OnCrimsonSurface,
                    modifier = Modifier.weight(1f)
                )
                KpiTile(
                    label = "Stock on Hand",
                    value = kpi.currentStockOnHand.toString(),
                    containerColor = ForestPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1.1f)
                )
                KpiTile(
                    label = "Low / Out Stock",
                    value = kpi.lowOrOutOfStockCount.toString(),
                    containerColor = if (kpi.lowOrOutOfStockCount > 0) AmberSurface else MaterialTheme.colorScheme.surface,
                    contentColor = if (kpi.lowOrOutOfStockCount > 0) OnAmberSurface else MaterialTheme.colorScheme.onSurface,
                    onClick = onLowStockClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("kpi_low_stock_card")
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiTile(
                        label = "Stock on Hand",
                        value = kpi.currentStockOnHand.toString(),
                        subtitle = "${kpi.totalSkus} bag SKUs",
                        containerColor = ForestPrimary,
                        contentColor = Color.White,
                        modifier = Modifier.weight(1.15f)
                    )
                    KpiTile(
                        label = "Low / Out Stock",
                        value = kpi.lowOrOutOfStockCount.toString(),
                        subtitle = if (kpi.lowOrOutOfStockCount > 0) "Tap to inspect" else "All stocked",
                        containerColor = if (kpi.lowOrOutOfStockCount > 0) AmberSurface else MaterialTheme.colorScheme.surface,
                        contentColor = if (kpi.lowOrOutOfStockCount > 0) OnAmberSurface else MaterialTheme.colorScheme.onSurface,
                        onClick = onLowStockClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("kpi_low_stock_card")
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiTile(
                        label = "Beginning",
                        value = kpi.totalBeginning.toString(),
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )
                    KpiTile(
                        label = "Delivery (+)",
                        value = "+${kpi.totalDeliveries}",
                        containerColor = EmeraldSurface,
                        contentColor = OnEmeraldSurface,
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )
                    KpiTile(
                        label = "Sold (-)",
                        value = "-${kpi.totalSold}",
                        containerColor = CrimsonSurface,
                        contentColor = OnCrimsonSurface,
                        compact = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiTile(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor
    )
    val border = if (containerColor == MaterialTheme.colorScheme.surface) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    } else null

    val content: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (compact) 10.dp else 14.dp, vertical = if (compact) 10.dp else 12.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.82f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                color = contentColor,
                maxLines = 1
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.72f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            colors = cardColors,
            border = border,
            shape = RoundedCornerShape(14.dp),
            modifier = modifier.heightIn(min = 48.dp)
        ) { content() }
    } else {
        Card(
            colors = cardColors,
            border = border,
            shape = RoundedCornerShape(14.dp),
            modifier = modifier
        ) { content() }
    }
}

@Composable
fun QuickActionButtonsGrid(
    lowStockCount: Int,
    onAddDelivery: () -> Unit,
    onRecordSale: () -> Unit,
    onLowStockAlert: () -> Unit,
    onAddNewItem: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddDelivery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldAction,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp)
                    .testTag("add_delivery_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Delivery",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onRecordSale,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonAction,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp)
                    .testTag("record_sale_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Remove,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Record Sale",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onLowStockAlert,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AmberAction,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("low_stock_alert_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.WarningAmber,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (lowStockCount > 0) "Low Stock ($lowStockCount)" else "Low Stock Alert",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onAddNewItem,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.25.dp, MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .testTag("add_new_item_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add New Item",
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SearchAndBrandFilterBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedBrand: BrandFilter,
    onBrandSelect: (BrandFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = {
                Text(
                    text = "Search code, model, color, or size...",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search inventory"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("inventory_search_input")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BrandFilter.entries.forEach { brandFilter ->
                val selected = selectedBrand == brandFilter
                FilterChip(
                    selected = selected,
                    onClick = { onBrandSelect(brandFilter) },
                    label = {
                        Text(
                            text = brandFilter.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("brand_chip_${brandFilter.name.lowercase()}")
                )
            }
        }
    }
}
