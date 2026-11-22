package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.BagBrand
import com.example.data.InventoryDateUtils
import com.example.data.InventorySummaryItem
import com.example.data.StockStatus
import com.example.ui.theme.AmberAction
import com.example.ui.theme.AmberSurface
import com.example.ui.theme.CrimsonAction
import com.example.ui.theme.CrimsonSurface
import com.example.ui.theme.EmeraldAction
import com.example.ui.theme.EmeraldSurface
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OnAmberSurface
import com.example.ui.theme.OnCrimsonSurface
import com.example.ui.theme.OnEmeraldSurface
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockTransactionModalSheet(
    isDelivery: Boolean,
    items: List<InventorySummaryItem>,
    preselectedItemCode: String?,
    externalError: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (itemCode: String, quantity: Int, notes: String, dateIso: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCode by remember(preselectedItemCode, items) {
        mutableStateOf(
            preselectedItemCode
                ?: items.firstOrNull { if (isDelivery) true else it.currentStock > 0 }?.itemCode
                ?: items.firstOrNull()?.itemCode.orEmpty()
        )
    }
    var qtyText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var dateIso by remember { mutableStateOf(InventoryDateUtils.todayIso()) }
    var localError by remember { mutableStateOf<String?>(null) }
    var showItemPicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedItem = items.firstOrNull { it.itemCode == selectedCode }
    val parsedQty = qtyText.trim().toIntOrNull()
    val exceedsStock = !isDelivery && selectedItem != null && parsedQty != null && parsedQty > selectedItem.currentStock
    val displayError = externalError ?: localError

    val accentColor = if (isDelivery) EmeraldAction else CrimsonAction
    val accentSurface = if (isDelivery) EmeraldSurface else CrimsonSurface
    val accentOnSurface = if (isDelivery) OnEmeraldSurface else OnCrimsonSurface

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentSurface
                    ) {
                        Icon(
                            imageVector = if (isDelivery) Icons.Filled.Add else Icons.Filled.Remove,
                            contentDescription = null,
                            tint = accentOnSurface,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isDelivery) "Add Delivery (+ Stock)" else "Record Sale (- Stock)",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (isDelivery) "Stock-In updates Current Stock immediately"
                            else "Stock-Out blocks sales exceeding stock on hand",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            if (items.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AmberSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No bag items in inventory yet. Add an item first before logging stock.",
                        color = OnAmberSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                // Item Code Picker Field
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Bag Item",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Surface(
                        onClick = {
                            onClearError()
                            localError = null
                            showItemPicker = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("modal_item_picker_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (selectedItem != null) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        ItemCodeBadge(itemCode = selectedItem.itemCode)
                                        BrandBadge(brand = selectedItem.brand)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${selectedItem.modelName} (${selectedItem.colorVariant} · ${selectedItem.size})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Text("Tap to choose an Item Code")
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = "Choose item"
                            )
                        }
                    }
                }

                if (selectedItem != null) {
                    val projected = if (parsedQty != null && parsedQty > 0) {
                        if (isDelivery) selectedItem.currentStock + parsedQty else selectedItem.currentStock - parsedQty
                    } else null

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (exceedsStock) CrimsonSurface else MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(
                            1.dp,
                            if (exceedsStock) CrimsonAction else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Current Stock: ${selectedItem.currentStock}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (exceedsStock) OnCrimsonSurface else MaterialTheme.colorScheme.onSurface
                            )
                            if (projected != null) {
                                Text(
                                    text = if (exceedsStock) "Exceeds stock!" else "New Stock: $projected",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (exceedsStock) OnCrimsonSurface else accentOnSurface
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = qtyText,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() }) {
                            qtyText = value
                            localError = null
                            onClearError()
                        }
                    },
                    label = { Text(if (isDelivery) "Quantity Delivered (> 0)" else "Quantity Sold (> 0)") },
                    placeholder = { Text("e.g. 5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = exceedsStock || displayError != null,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_quantity_input")
                )

                OutlinedTextField(
                    value = dateIso,
                    onValueChange = { value ->
                        dateIso = value
                        localError = null
                        onClearError()
                    },
                    label = { Text("Date (YYYY-MM-DD)") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Pick date"
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_date_input")
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = {
                        Text(
                            if (isDelivery) "DR / Receipt # or Notes (optional)"
                            else "Receipt # / Customer Notes (optional)"
                        )
                    },
                    placeholder = {
                        Text(if (isDelivery) "e.g. DR-4031 Warehouse Batch" else "e.g. INV-1095 Retail Counter")
                    },
                    maxLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("modal_notes_input")
                )

                if (exceedsStock || displayError != null) {
                    val errorMsg = if (exceedsStock && selectedItem != null) {
                        "Blocked: You cannot sell $parsedQty units of ${selectedItem.itemCode}. Only ${selectedItem.currentStock} in stock."
                    } else {
                        displayError.orEmpty()
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CrimsonSurface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ErrorOutline,
                                contentDescription = null,
                                tint = CrimsonAction
                            )
                            Text(
                                text = errorMsg,
                                style = MaterialTheme.typography.bodySmall,
                                color = OnCrimsonSurface,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        val qty = qtyText.trim().toIntOrNull()
                        when {
                            selectedItem == null -> localError = "Please select an item."
                            qty == null || qty <= 0 -> localError = "Enter a quantity greater than 0."
                            !InventoryDateUtils.isValidIsoDate(dateIso) -> localError = "Enter a valid date in YYYY-MM-DD format."
                            exceedsStock -> localError = "Cannot sell more than ${selectedItem.currentStock} available units."
                            else -> onSubmit(selectedItem.itemCode, qty, notesText, dateIso.trim())
                        }
                    },
                    enabled = !exceedsStock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .testTag("modal_submit_transaction_button")
                ) {
                    Text(
                        text = if (isDelivery) "Save Delivery (+ Stock)" else "Confirm Sale (- Stock)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }

    if (showItemPicker) {
        ItemPickerDialog(
            items = items,
            selectedCode = selectedCode,
            onSelect = { code ->
                selectedCode = code
                showItemPicker = false
            },
            onDismiss = { showItemPicker = false }
        )
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                                timeZone = TimeZone.getTimeZone("UTC")
                            }
                            dateIso = fmt.format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ItemPickerDialog(
    items: List<InventorySummaryItem>,
    selectedCode: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pickerQuery by remember { mutableStateOf("") }
    val filtered = remember(items, pickerQuery) {
        val q = pickerQuery.trim().lowercase(Locale.US)
        if (q.isEmpty()) items
        else items.filter {
            it.itemCode.lowercase(Locale.US).contains(q) ||
                it.brand.lowercase(Locale.US).contains(q) ||
                it.modelName.lowercase(Locale.US).contains(q) ||
                it.colorVariant.lowercase(Locale.US).contains(q)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Item Code") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = pickerQuery,
                    onValueChange = { pickerQuery = it },
                    placeholder = { Text("Search SKU or model...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.itemCode }) { item ->
                        val isSelected = item.itemCode == selectedCode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item.itemCode) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ItemCodeBadge(itemCode = item.itemCode)
                                        BrandBadge(brand = item.brand)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.modelName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${item.colorVariant} · ${item.size}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${item.currentStock}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (item.currentStock <= 0) CrimsonAction else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun LowStockAlertModal(
    lowAndOutItems: List<InventorySummaryItem>,
    onRestockClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sortedAlerts = remember(lowAndOutItems) {
        lowAndOutItems.sortedWith(
            compareBy<InventorySummaryItem> {
                if (it.status == StockStatus.OUT_OF_STOCK) 0 else 1
            }.thenBy { it.currentStock }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (sortedAlerts.isEmpty()) Icons.Filled.CheckCircle else Icons.Filled.WarningAmber,
                contentDescription = null,
                tint = if (sortedAlerts.isEmpty()) EmeraldAction else AmberAction,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = if (sortedAlerts.isEmpty()) "All Stock Healthy"
                else "Low & Out of Stock (${sortedAlerts.size})"
            )
        },
        text = {
            if (sortedAlerts.isEmpty()) {
                Text(
                    text = "Every bag SKU across PORTGEAR, AIRFIT, and SOFISTICA is currently above its reorder level.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedAlerts, key = { it.itemCode }) { item ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (item.status == StockStatus.OUT_OF_STOCK) CrimsonSurface else AmberSurface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        ItemCodeBadge(itemCode = item.itemCode)
                                        BrandBadge(brand = item.brand)
                                    }
                                    StatusBadge(status = item.status)
                                }
                                Text(
                                    text = "${item.modelName} (${item.colorVariant})",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Stock: ${item.currentStock} / Reorder <= ${item.reorderLevel}",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (item.status == StockStatus.OUT_OF_STOCK) OnCrimsonSurface else OnAmberSurface
                                    )
                                    Button(
                                        onClick = { onRestockClick(item.itemCode) },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAction),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        modifier = Modifier.heightIn(min = 38.dp)
                                    ) {
                                        Text("+ Restock", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditItemModalSheet(
    editingItem: InventorySummaryItem?,
    externalError: String?,
    onClearError: () -> Unit,
    onDismiss: () -> Unit,
    onSubmit: (
        itemCode: String,
        brand: String,
        modelName: String,
        colorVariant: String,
        size: String,
        beginningStock: Int,
        reorderLevel: Int
    ) -> Unit
) {
    val isEditing = editingItem != null
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var itemCode by remember(editingItem) { mutableStateOf(editingItem?.itemCode ?: "") }
    var selectedBrand by remember(editingItem) { mutableStateOf(editingItem?.brand ?: BagBrand.PORTGEAR.displayName) }
    var modelName by remember(editingItem) { mutableStateOf(editingItem?.modelName ?: "") }
    var colorVariant by remember(editingItem) { mutableStateOf(editingItem?.colorVariant ?: "") }
    var size by remember(editingItem) { mutableStateOf(editingItem?.size ?: "") }
    var beginningText by remember(editingItem) { mutableStateOf(editingItem?.beginningStock?.toString() ?: "0") }
    var reorderText by remember(editingItem) { mutableStateOf(editingItem?.reorderLevel?.toString() ?: "4") }
    var localError by remember { mutableStateOf<String?>(null) }

    val displayError = externalError ?: localError

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (isEditing) "Edit Item ${editingItem?.itemCode}" else "Add New Bag Item",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Items automatically sort by Item Code",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            // Brand selection chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Brand", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BagBrand.entries.forEach { brand ->
                        val isSelected = selectedBrand.equals(brand.displayName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedBrand = brand.displayName
                                if (!isEditing && itemCode.isBlank()) {
                                    itemCode = "${brand.codePrefix}-"
                                }
                            },
                            label = { Text(brand.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = itemCode,
                onValueChange = {
                    if (!isEditing) {
                        itemCode = it.uppercase(Locale.US)
                        localError = null
                        onClearError()
                    }
                },
                enabled = !isEditing,
                label = { Text("Item Code (e.g., PG-004, AF-003, SF-004)") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_modal_code_input")
            )

            OutlinedTextField(
                value = modelName,
                onValueChange = {
                    modelName = it
                    localError = null
                    onClearError()
                },
                label = { Text("Model / Item Name") },
                placeholder = { Text("e.g., Cabin Spinner Luggage 20\"") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("item_modal_model_input")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = colorVariant,
                    onValueChange = { colorVariant = it },
                    label = { Text("Color / Variant") },
                    placeholder = { Text("e.g., Matte Black") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = size,
                    onValueChange = { size = it },
                    label = { Text("Size") },
                    placeholder = { Text("e.g., 24\" / Large") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = beginningText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) beginningText = it },
                    label = { Text("Beginning Stock") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_modal_beginning_input")
                )
                OutlinedTextField(
                    value = reorderText,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) reorderText = it },
                    label = { Text("Reorder Level") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("item_modal_reorder_input")
                )
            }

            if (displayError != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CrimsonSurface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = displayError,
                        color = OnCrimsonSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Button(
                onClick = {
                    val beg = beginningText.trim().toIntOrNull()
                    val reord = reorderText.trim().toIntOrNull()
                    when {
                        itemCode.trim().isEmpty() -> localError = "Item Code is required."
                        modelName.trim().isEmpty() -> localError = "Model / Item Name is required."
                        beg == null || beg < 0 -> localError = "Beginning Stock must be 0 or higher."
                        reord == null || reord < 0 -> localError = "Reorder Level must be 0 or higher."
                        else -> onSubmit(itemCode, selectedBrand, modelName, colorVariant, size, beg, reord)
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("item_modal_save_button")
            ) {
                Text(
                    text = if (isEditing) "Save Item Changes" else "Add Item to Inventory",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
