package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.InventoryDateUtils
import com.example.data.TransactionType
import com.example.ui.components.AdaptiveInventoryItemsList
import com.example.ui.components.AddEditItemModalSheet
import com.example.ui.components.BackupSettingsScreen
import com.example.ui.components.InventoryHeaderBanner
import com.example.ui.components.KpiSummaryStrip
import com.example.ui.components.LowStockAlertModal
import com.example.ui.components.QuickActionButtonsGrid
import com.example.ui.components.SearchAndBrandFilterBar
import com.example.ui.components.StockTransactionModalSheet
import com.example.ui.components.TransactionLogScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class PendingExportType {
    JSON_BACKUP,
    INVENTORY_CSV,
    DELIVERY_CSV,
    SALES_CSV
}

@Composable
fun PortgearInventoryApp(
    viewModel: InventoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pendingExportType by remember { mutableStateOf<PendingExportType?>(null) }
    var pendingImportedJson by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val createJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val exportType = pendingExportType
        pendingExportType = null
        if (uri != null && exportType == PendingExportType.JSON_BACKUP) {
            scope.launch {
                runCatching {
                    val json = viewModel.buildBackupJson()
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(json.toByteArray(Charsets.UTF_8))
                        } ?: error("Cannot open destination file.")
                    }
                }.onSuccess {
                    viewModel.notifyMessage("JSON backup saved to device")
                }.onFailure { err ->
                    viewModel.notifyMessage("Save failed: ${err.message ?: "Unknown error"}")
                }
            }
        }
    }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val exportType = pendingExportType
        pendingExportType = null
        if (uri != null && exportType != null) {
            scope.launch {
                runCatching {
                    val csv = when (exportType) {
                        PendingExportType.INVENTORY_CSV -> viewModel.buildInventoryCsv()
                        PendingExportType.DELIVERY_CSV -> viewModel.buildDeliveryCsv()
                        PendingExportType.SALES_CSV -> viewModel.buildSalesCsv()
                        PendingExportType.JSON_BACKUP -> viewModel.buildBackupJson()
                    }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            out.write(csv.toByteArray(Charsets.UTF_8))
                        } ?: error("Cannot open destination file.")
                    }
                }.onSuccess {
                    viewModel.notifyMessage("CSV report exported")
                }.onFailure { err ->
                    viewModel.notifyMessage("Export failed: ${err.message ?: "Unknown error"}")
                }
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                            it.readText()
                        } ?: error("Unable to read selected file.")
                    }
                }.onSuccess { raw ->
                    pendingImportedJson = raw
                }.onFailure { err ->
                    viewModel.notifyMessage("Read failed: ${err.message ?: "Cannot open file"}")
                }
            }
        }
    }

    // Back handling for modals and non-home tabs
    BackHandler(enabled = uiState.activeModal != ActiveModal.None || uiState.activeTab != MainTab.INVENTORY) {
        if (uiState.activeModal != ActiveModal.None) {
            viewModel.dismissModal()
        } else if (uiState.activeTab != MainTab.INVENTORY) {
            viewModel.selectTab(MainTab.INVENTORY)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        MainTab.entries.forEach { tab ->
                            val selected = uiState.activeTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        MainTab.entries.forEach { tab ->
                            val selected = uiState.activeTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tabIcon(tab, selected),
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        when (uiState.activeTab) {
                            MainTab.INVENTORY -> {
                                InventoryHomeContent(
                                    uiState = uiState,
                                    onSearchChange = viewModel::updateSearchQuery,
                                    onBrandSelect = viewModel::selectBrandFilter,
                                    onToggleExpand = viewModel::toggleItemExpanded,
                                    onOpenModal = viewModel::openModal,
                                    onDeleteItem = viewModel::deleteItem
                                )
                            }
                            MainTab.DELIVERY_LOG -> {
                                TransactionLogScreen(
                                    type = TransactionType.DELIVERY,
                                    logs = uiState.deliveryLogs,
                                    searchQuery = uiState.logSearchQuery,
                                    onSearchChange = viewModel::updateLogSearchQuery,
                                    onQuickAddClick = { viewModel.openModal(ActiveModal.AddDelivery()) },
                                    onDeleteLog = viewModel::deleteTransaction
                                )
                            }
                            MainTab.SALES_LOG -> {
                                TransactionLogScreen(
                                    type = TransactionType.SALE,
                                    logs = uiState.salesLogs,
                                    searchQuery = uiState.logSearchQuery,
                                    onSearchChange = viewModel::updateLogSearchQuery,
                                    onQuickAddClick = { viewModel.openModal(ActiveModal.RecordSale()) },
                                    onDeleteLog = viewModel::deleteTransaction
                                )
                            }
                            MainTab.BACKUP -> {
                                BackupSettingsScreen(
                                    skuCount = uiState.allItems.size,
                                    deliveryCount = uiState.deliveryLogs.size,
                                    salesCount = uiState.salesLogs.size,
                                    onExportJsonBackup = {
                                        pendingExportType = PendingExportType.JSON_BACKUP
                                        createJsonLauncher.launch(
                                            "portgear-backup-${InventoryDateUtils.fileTimestamp()}.json"
                                        )
                                    },
                                    onShareJsonBackup = {
                                        scope.launch {
                                            shareBackupJsonFile(
                                                context = context,
                                                jsonContent = viewModel.buildBackupJson(),
                                                onError = { msg -> viewModel.notifyMessage(msg) }
                                            )
                                        }
                                    },
                                    onImportJsonBackup = {
                                        importJsonLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                                    },
                                    onExportInventoryCsv = {
                                        pendingExportType = PendingExportType.INVENTORY_CSV
                                        createCsvLauncher.launch(
                                            "portgear-inventory-${InventoryDateUtils.fileTimestamp()}.csv"
                                        )
                                    },
                                    onExportDeliveryCsv = {
                                        pendingExportType = PendingExportType.DELIVERY_CSV
                                        createCsvLauncher.launch(
                                            "portgear-deliveries-${InventoryDateUtils.fileTimestamp()}.csv"
                                        )
                                    },
                                    onExportSalesCsv = {
                                        pendingExportType = PendingExportType.SALES_CSV
                                        createCsvLauncher.launch(
                                            "portgear-sales-${InventoryDateUtils.fileTimestamp()}.csv"
                                        )
                                    },
                                    onRestoreSampleData = viewModel::restoreSampleData,
                                    onClearAllData = viewModel::clearAllData
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Active Modals
    when (val modal = uiState.activeModal) {
        ActiveModal.None -> Unit
        is ActiveModal.AddDelivery -> {
            StockTransactionModalSheet(
                isDelivery = true,
                items = uiState.allItems,
                preselectedItemCode = modal.preselectedItemCode,
                externalError = uiState.modalErrorMessage,
                onClearError = viewModel::clearModalError,
                onDismiss = viewModel::dismissModal,
                onSubmit = viewModel::submitDelivery
            )
        }
        is ActiveModal.RecordSale -> {
            StockTransactionModalSheet(
                isDelivery = false,
                items = uiState.allItems,
                preselectedItemCode = modal.preselectedItemCode,
                externalError = uiState.modalErrorMessage,
                onClearError = viewModel::clearModalError,
                onDismiss = viewModel::dismissModal,
                onSubmit = viewModel::submitSale
            )
        }
        ActiveModal.LowStockAlert -> {
            LowStockAlertModal(
                lowAndOutItems = uiState.lowAndOutItems,
                onRestockClick = { code -> viewModel.openModal(ActiveModal.AddDelivery(code)) },
                onDismiss = viewModel::dismissModal
            )
        }
        ActiveModal.AddItem -> {
            AddEditItemModalSheet(
                editingItem = null,
                externalError = uiState.modalErrorMessage,
                onClearError = viewModel::clearModalError,
                onDismiss = viewModel::dismissModal,
                onSubmit = { code, brand, modelName, color, size, beg, reorder ->
                    viewModel.submitNewItem(code, brand, modelName, color, size, beg, reorder)
                }
            )
        }
        is ActiveModal.EditItem -> {
            AddEditItemModalSheet(
                editingItem = modal.item,
                externalError = uiState.modalErrorMessage,
                onClearError = viewModel::clearModalError,
                onDismiss = viewModel::dismissModal,
                onSubmit = { code, brand, modelName, color, size, beg, reorder ->
                    viewModel.submitEditItem(code, brand, modelName, color, size, beg, reorder)
                }
            )
        }
    }

    // Import Backup confirmation dialog (Replace vs Merge)
    pendingImportedJson?.let { rawJson ->
        AlertDialog(
            onDismissRequest = { pendingImportedJson = null },
            title = { Text("Import Backup File") },
            text = {
                Text(
                    "Choose how to apply this backup to your phone: Replace overwrites current records; Merge updates matching SKUs and adds new logs."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.importBackupJson(rawJson, replaceExisting = true)
                        pendingImportedJson = null
                    }
                ) {
                    Text("Replace All")
                }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { pendingImportedJson = null }) {
                        Text("Cancel")
                    }
                    OutlinedButton(
                        onClick = {
                            viewModel.importBackupJson(rawJson, replaceExisting = false)
                            pendingImportedJson = null
                        }
                    ) {
                        Text("Merge")
                    }
                }
            }
        )
    }
}

@Composable
private fun InventoryHomeContent(
    uiState: InventoryUiState,
    onSearchChange: (String) -> Unit,
    onBrandSelect: (BrandFilter) -> Unit,
    onToggleExpand: (String) -> Unit,
    onOpenModal: (ActiveModal) -> Unit,
    onDeleteItem: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            InventoryHeaderBanner(
                skuCount = uiState.allItems.size,
                onAddNewClick = { onOpenModal(ActiveModal.AddItem) }
            )
        }

        item {
            KpiSummaryStrip(
                kpi = uiState.kpi,
                onLowStockClick = { onOpenModal(ActiveModal.LowStockAlert) }
            )
        }

        item {
            QuickActionButtonsGrid(
                lowStockCount = uiState.lowAndOutItems.size,
                onAddDelivery = { onOpenModal(ActiveModal.AddDelivery()) },
                onRecordSale = { onOpenModal(ActiveModal.RecordSale()) },
                onLowStockAlert = { onOpenModal(ActiveModal.LowStockAlert) },
                onAddNewItem = { onOpenModal(ActiveModal.AddItem) }
            )
        }

        item {
            SearchAndBrandFilterBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = onSearchChange,
                selectedBrand = uiState.selectedBrandFilter,
                onBrandSelect = onBrandSelect
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Inventory Items (${uiState.filteredItems.size})",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Sorted by Item Code",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            AdaptiveInventoryItemsList(
                items = uiState.filteredItems,
                expandedItemCodes = uiState.expandedItemCodes,
                onToggleExpand = onToggleExpand,
                onQuickDelivery = { code -> onOpenModal(ActiveModal.AddDelivery(code)) },
                onQuickSale = { code -> onOpenModal(ActiveModal.RecordSale(code)) },
                onEditItem = { item -> onOpenModal(ActiveModal.EditItem(item)) },
                onDeleteItem = onDeleteItem,
                onAddNewItem = { onOpenModal(ActiveModal.AddItem) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun tabIcon(tab: MainTab, selected: Boolean): ImageVector = when (tab) {
    MainTab.INVENTORY -> if (selected) Icons.Filled.Inventory2 else Icons.Outlined.Inventory2
    MainTab.DELIVERY_LOG -> if (selected) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping
    MainTab.SALES_LOG -> if (selected) Icons.Filled.PointOfSale else Icons.Outlined.PointOfSale
    MainTab.BACKUP -> if (selected) Icons.Filled.CloudDownload else Icons.Outlined.CloudDownload
}

private suspend fun shareBackupJsonFile(
    context: Context,
    jsonContent: String,
    onError: (String) -> Unit
) {
    runCatching {
        val file = withContext(Dispatchers.IO) {
            val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val outFile = File(backupDir, "portgear-backup-${InventoryDateUtils.fileTimestamp()}.json")
            outFile.writeText(jsonContent, Charsets.UTF_8)
            outFile
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Portgear Inventory Backup")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Portgear Backup"))
    }.onFailure { err ->
        onError("Could not share backup: ${err.message ?: "Unknown error"}")
    }
}
