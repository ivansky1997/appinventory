package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.BackupSerializer
import com.example.data.BagBrand
import com.example.data.InventoryKpiSummary
import com.example.data.InventoryRepository
import com.example.data.InventorySummaryItem
import com.example.data.StockLogEntry
import com.example.data.StockStatus
import com.example.data.TransactionType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class MainTab(val title: String) {
    INVENTORY("Inventory"),
    DELIVERY_LOG("Deliveries"),
    SALES_LOG("Sales"),
    BACKUP("Backup")
}

enum class BrandFilter(val label: String) {
    ALL("All"),
    PORTGEAR("PORTGEAR"),
    AIRFIT("AIRFIT"),
    SOFISTICA("SOFISTICA")
}

sealed interface ActiveModal {
    data object None : ActiveModal
    data class AddDelivery(val preselectedItemCode: String? = null) : ActiveModal
    data class RecordSale(val preselectedItemCode: String? = null) : ActiveModal
    data object LowStockAlert : ActiveModal
    data object AddItem : ActiveModal
    data class EditItem(val item: InventorySummaryItem) : ActiveModal
}

data class InventoryUiState(
    val isLoading: Boolean = true,
    val allItems: List<InventorySummaryItem> = emptyList(),
    val filteredItems: List<InventorySummaryItem> = emptyList(),
    val kpi: InventoryKpiSummary = InventoryKpiSummary(),
    val lowAndOutItems: List<InventorySummaryItem> = emptyList(),
    val deliveryLogs: List<StockLogEntry> = emptyList(),
    val salesLogs: List<StockLogEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedBrandFilter: BrandFilter = BrandFilter.ALL,
    val logSearchQuery: String = "",
    val expandedItemCodes: Set<String> = emptySet(),
    val activeTab: MainTab = MainTab.INVENTORY,
    val activeModal: ActiveModal = ActiveModal.None,
    val modalErrorMessage: String? = null
)

private data class FilterAndUiControls(
    val searchQuery: String = "",
    val selectedBrandFilter: BrandFilter = BrandFilter.ALL,
    val logSearchQuery: String = "",
    val expandedItemCodes: Set<String> = emptySet(),
    val activeTab: MainTab = MainTab.INVENTORY,
    val activeModal: ActiveModal = ActiveModal.None,
    val modalErrorMessage: String? = null,
    val isSeeded: Boolean = false
)

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    private val controls = MutableStateFlow(FilterAndUiControls())

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    val uiState: StateFlow<InventoryUiState> = combine(
        repository.inventorySummaries,
        repository.stockLogs,
        controls
    ) { items, logs, ctrl ->
        val query = ctrl.searchQuery.trim().lowercase(Locale.US)
        val filteredItems = items.filter { item ->
            val brandMatches = when (ctrl.selectedBrandFilter) {
                BrandFilter.ALL -> true
                BrandFilter.PORTGEAR -> item.brand.equals(BagBrand.PORTGEAR.displayName, ignoreCase = true)
                BrandFilter.AIRFIT -> item.brand.equals(BagBrand.AIRFIT.displayName, ignoreCase = true)
                BrandFilter.SOFISTICA -> item.brand.equals(BagBrand.SOFISTICA.displayName, ignoreCase = true)
            }
            val queryMatches = if (query.isEmpty()) {
                true
            } else {
                item.itemCode.lowercase(Locale.US).contains(query) ||
                    item.brand.lowercase(Locale.US).contains(query) ||
                    item.modelName.lowercase(Locale.US).contains(query) ||
                    item.colorVariant.lowercase(Locale.US).contains(query) ||
                    item.size.lowercase(Locale.US).contains(query)
            }
            brandMatches && queryMatches
        }

        val lowAndOut = items.filter {
            it.status == StockStatus.OUT_OF_STOCK || it.status == StockStatus.LOW_REORDER
        }

        val kpi = InventoryKpiSummary(
            totalBeginning = items.sumOf { it.beginningStock },
            totalDeliveries = items.sumOf { it.deliveryTotal },
            totalSold = items.sumOf { it.salesTotal },
            currentStockOnHand = items.sumOf { it.currentStock },
            lowOrOutOfStockCount = lowAndOut.size,
            totalSkus = items.size
        )

        val logQuery = ctrl.logSearchQuery.trim().lowercase(Locale.US)
        val filteredLogs = if (logQuery.isEmpty()) {
            logs
        } else {
            logs.filter { log ->
                log.itemCode.lowercase(Locale.US).contains(logQuery) ||
                    log.brand.lowercase(Locale.US).contains(logQuery) ||
                    log.modelName.lowercase(Locale.US).contains(logQuery) ||
                    log.colorVariant.lowercase(Locale.US).contains(logQuery) ||
                    log.notes.lowercase(Locale.US).contains(logQuery) ||
                    log.dateIso.lowercase(Locale.US).contains(logQuery)
            }
        }

        InventoryUiState(
            isLoading = !ctrl.isSeeded,
            allItems = items,
            filteredItems = filteredItems,
            kpi = kpi,
            lowAndOutItems = lowAndOut,
            deliveryLogs = filteredLogs.filter { it.type == TransactionType.DELIVERY },
            salesLogs = filteredLogs.filter { it.type == TransactionType.SALE },
            searchQuery = ctrl.searchQuery,
            selectedBrandFilter = ctrl.selectedBrandFilter,
            logSearchQuery = ctrl.logSearchQuery,
            expandedItemCodes = ctrl.expandedItemCodes,
            activeTab = ctrl.activeTab,
            activeModal = ctrl.activeModal,
            modalErrorMessage = ctrl.modalErrorMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = InventoryUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureSampleDataSeeded()
            controls.value = controls.value.copy(isSeeded = true)
        }
    }

    fun selectTab(tab: MainTab) {
        controls.value = controls.value.copy(activeTab = tab)
    }

    fun updateSearchQuery(query: String) {
        controls.value = controls.value.copy(searchQuery = query)
    }

    fun selectBrandFilter(filter: BrandFilter) {
        controls.value = controls.value.copy(selectedBrandFilter = filter)
    }

    fun updateLogSearchQuery(query: String) {
        controls.value = controls.value.copy(logSearchQuery = query)
    }

    fun toggleItemExpanded(itemCode: String) {
        val current = controls.value.expandedItemCodes
        val updated = if (itemCode in current) current - itemCode else current + itemCode
        controls.value = controls.value.copy(expandedItemCodes = updated)
    }

    fun openModal(modal: ActiveModal) {
        controls.value = controls.value.copy(activeModal = modal, modalErrorMessage = null)
    }

    fun dismissModal() {
        controls.value = controls.value.copy(activeModal = ActiveModal.None, modalErrorMessage = null)
    }

    fun clearModalError() {
        controls.value = controls.value.copy(modalErrorMessage = null)
    }

    fun submitDelivery(itemCode: String, quantity: Int, notes: String, dateIso: String) {
        viewModelScope.launch {
            repository.recordDelivery(itemCode, quantity, notes, dateIso)
                .onSuccess {
                    dismissModal()
                    _snackbarEvents.emit("Added +$quantity delivery for ${itemCode.trim().uppercase(Locale.US)}")
                }
                .onFailure { err ->
                    controls.value = controls.value.copy(
                        modalErrorMessage = err.message ?: "Could not record delivery."
                    )
                }
        }
    }

    fun submitSale(itemCode: String, quantity: Int, notes: String, dateIso: String) {
        viewModelScope.launch {
            repository.recordSale(itemCode, quantity, notes, dateIso)
                .onSuccess {
                    dismissModal()
                    _snackbarEvents.emit("Recorded -$quantity sale for ${itemCode.trim().uppercase(Locale.US)}")
                }
                .onFailure { err ->
                    controls.value = controls.value.copy(
                        modalErrorMessage = err.message ?: "Could not record sale."
                    )
                }
        }
    }

    fun submitNewItem(
        itemCode: String,
        brand: String,
        modelName: String,
        colorVariant: String,
        size: String,
        beginningStock: Int,
        reorderLevel: Int
    ) {
        viewModelScope.launch {
            repository.addItem(
                rawItemCode = itemCode,
                brand = brand,
                modelName = modelName,
                colorVariant = colorVariant,
                size = size,
                beginningStock = beginningStock,
                reorderLevel = reorderLevel
            ).onSuccess {
                dismissModal()
                _snackbarEvents.emit("Added ${itemCode.trim().uppercase(Locale.US)} to inventory")
            }.onFailure { err ->
                controls.value = controls.value.copy(
                    modalErrorMessage = err.message ?: "Could not add item."
                )
            }
        }
    }

    fun submitEditItem(
        itemCode: String,
        brand: String,
        modelName: String,
        colorVariant: String,
        size: String,
        beginningStock: Int,
        reorderLevel: Int
    ) {
        viewModelScope.launch {
            repository.updateItem(
                itemCode = itemCode,
                brand = brand,
                modelName = modelName,
                colorVariant = colorVariant,
                size = size,
                beginningStock = beginningStock,
                reorderLevel = reorderLevel
            ).onSuccess {
                dismissModal()
                _snackbarEvents.emit("Updated $itemCode")
            }.onFailure { err ->
                controls.value = controls.value.copy(
                    modalErrorMessage = err.message ?: "Could not update item."
                )
            }
        }
    }

    fun deleteItem(itemCode: String) {
        viewModelScope.launch {
            repository.deleteItem(itemCode)
            _snackbarEvents.emit("Deleted $itemCode and its transaction logs")
        }
    }

    fun deleteTransaction(log: StockLogEntry) {
        viewModelScope.launch {
            repository.deleteTransaction(log.id)
            _snackbarEvents.emit("Removed ${log.type.label.lowercase(Locale.US)} entry for ${log.itemCode}")
        }
    }

    suspend fun buildBackupJson(): String = repository.exportBackupJson()

    fun buildInventoryCsv(): String = BackupSerializer.exportInventoryCsv(uiState.value.allItems)

    fun buildDeliveryCsv(): String = BackupSerializer.exportTransactionsCsv(uiState.value.deliveryLogs)

    fun buildSalesCsv(): String = BackupSerializer.exportTransactionsCsv(uiState.value.salesLogs)

    fun importBackupJson(rawJson: String, replaceExisting: Boolean) {
        viewModelScope.launch {
            repository.importBackupJson(rawJson, replaceExisting)
                .onSuccess { (itemsCount, txCount) ->
                    val modeText = if (replaceExisting) "Restored" else "Merged"
                    _snackbarEvents.emit("$modeText $itemsCount items and $txCount log entries")
                }
                .onFailure { err ->
                    _snackbarEvents.emit("Import failed: ${err.message ?: "Invalid backup JSON"}")
                }
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            repository.restoreSampleData()
            _snackbarEvents.emit("Sample inventory restored for PORTGEAR, AIRFIT & SOFISTICA")
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _snackbarEvents.emit("All inventory items and logs cleared")
        }
    }

    fun notifyMessage(message: String) {
        viewModelScope.launch {
            _snackbarEvents.emit(message)
        }
    }

    class Factory(private val repository: InventoryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(repository) as T
        }
    }
}
