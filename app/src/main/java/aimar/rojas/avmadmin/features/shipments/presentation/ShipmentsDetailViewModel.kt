package aimar.rojas.avmadmin.features.shipments.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import aimar.rojas.avmadmin.domain.model.Party
import aimar.rojas.avmadmin.domain.model.Trade
import aimar.rojas.avmadmin.core.sync.ManualSyncManager
import aimar.rojas.avmadmin.features.parties.domain.PartiesRepository
import aimar.rojas.avmadmin.features.shipments.domain.ShipmentExpensesRepository
import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpense
import aimar.rojas.avmadmin.features.shipments.domain.model.ShipmentExpenseCategory
import aimar.rojas.avmadmin.features.trades.domain.TradesRepository
import aimar.rojas.avmadmin.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ShipmentsDetailViewModel @Inject constructor(
    private val tradesRepository: TradesRepository,
    private val shipmentExpensesRepository: ShipmentExpensesRepository,
    private val partiesRepository: PartiesRepository,
    private val selectionsRepository: aimar.rojas.avmadmin.features.selections.domain.SelectionsRepository,
    private val manualSyncManager: ManualSyncManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shipmentId: Int = checkNotNull(savedStateHandle["shipmentId"])

    private val _uiState = MutableStateFlow(ShipmentsDetailUiState())
    val uiState: StateFlow<ShipmentsDetailUiState> = _uiState.asStateFlow()

    init {
        loadTrades()
        loadParties()
        observeExpenses()
        observePendingSyncTrades()
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            shipmentExpensesRepository.observeExpenses(shipmentId).collect { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
            }
        }
    }

    private fun observePendingSyncTrades() {
        viewModelScope.launch {
            selectionsRepository.getPendingSyncTradeIds().collect { pendingIds ->
                _uiState.update { it.copy(pendingSyncTradeIds = pendingIds.toSet()) }
            }
        }
    }

    private fun loadParties() {
        viewModelScope.launch {
            partiesRepository.getParties()
                .onSuccess { result ->
                    val parties = result.parties
                    _uiState.value = _uiState.value.copy(
                        suppliers = parties.filter { it.partyRole.uppercase() in listOf("PRODUCER", "SUPPLIER") },
                        clients = parties.filter { it.partyRole.uppercase() in listOf("BUYER", "CLIENT", "CUSTOMER") }
                    )
                }
                .onFailure {
                    // Fail silently or handle
                }
        }
    }

    fun loadTrades() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            tradesRepository.getTrades(shipmentId = shipmentId)
                .onSuccess { result ->
                    val purchases = result.trades.filter { it.tradeType == "PURCHASE" }
                    val sales = result.trades.filter { it.tradeType == "SALE" }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        purchases = purchases,
                        sales = sales
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar transacciones"
                    )
                }
        }
    }

    fun showCreateDialog(tradeType: String) {
        _uiState.value = _uiState.value.copy(
            showCreateDialog = true,
            createTradeType = tradeType,
            createPartyId = null,
            createStartDatetime = "",
            createEndDatetime = "",
            createDiscountWeightPerTray = "",
            createVarietyAvocado = "",
            createStatus = "OPEN",
            showStartDateTimePicker = false,
            showEndDateTimePicker = false
        )
    }

    fun hideCreateDialog() {
        _uiState.value = _uiState.value.copy(showCreateDialog = false)
    }

    fun showCreateExpenseSheet() {
        _uiState.value = _uiState.value.copy(
            showCreateExpenseSheet = true,
            expenseCategory = ShipmentExpenseCategory.LABOR,
            expenseSubcategory = null,
            expenseAmount = "",
            expenseQuantity = "",
            expenseUnitPrice = "",
            expenseDescription = "",
            expenseError = null
        )
    }

    fun hideCreateExpenseSheet() {
        _uiState.value = _uiState.value.copy(showCreateExpenseSheet = false)
    }

    fun onExpenseCategoryChange(category: String) {
        _uiState.value = _uiState.value.copy(
            expenseCategory = category,
            expenseSubcategory = null
        )
    }

    fun onExpenseSubcategoryChange(subcategory: String?) {
        _uiState.value = _uiState.value.copy(expenseSubcategory = subcategory)
    }

    fun onExpenseAmountChange(value: String) {
        _uiState.value = _uiState.value.copy(expenseAmount = value)
    }

    fun onExpenseQuantityChange(value: String) {
        _uiState.value = _uiState.value.copy(expenseQuantity = value)
    }

    fun onExpenseUnitPriceChange(value: String) {
        _uiState.value = _uiState.value.copy(expenseUnitPrice = value)
    }

    fun onExpenseDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(expenseDescription = value)
    }

    fun createExpense() {
        val currentState = _uiState.value
        val amount = currentState.expenseAmount.toDoubleOrNull()

        if (amount == null || amount <= 0.0) {
            _uiState.value = currentState.copy(expenseError = "Ingresa un monto válido")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSavingExpense = true, expenseError = null)

            shipmentExpensesRepository.createExpense(
                shipmentId = shipmentId,
                category = currentState.expenseCategory,
                subcategory = currentState.expenseSubcategory,
                amount = amount,
                quantity = currentState.expenseQuantity.toDoubleOrNull(),
                unitPrice = currentState.expenseUnitPrice.toDoubleOrNull(),
                description = currentState.expenseDescription.takeIf { it.isNotBlank() },
                expenseDate = DateUtils.currentUtcSyncTimestamp()
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isSavingExpense = false,
                        showCreateExpenseSheet = false,
                        expenseAmount = "",
                        expenseQuantity = "",
                        expenseUnitPrice = "",
                        expenseDescription = "",
                        expenseError = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isSavingExpense = false,
                        expenseError = exception.message ?: "No se pudo guardar el costo"
                    )
                }
        }
    }

    fun onPartySelected(partyId: Int) {
        _uiState.value = _uiState.value.copy(createPartyId = partyId)
    }

    fun onStartDatetimeChange(datetime: String) {
        _uiState.value = _uiState.value.copy(createStartDatetime = datetime)
    }

    fun onEndDatetimeChange(datetime: String) {
        _uiState.value = _uiState.value.copy(createEndDatetime = datetime)
    }

    fun onDiscountWeightChange(weight: String) {
        _uiState.value = _uiState.value.copy(createDiscountWeightPerTray = weight)
    }

    fun onVarietyAvocadoChange(variety: String) {
        _uiState.value = _uiState.value.copy(createVarietyAvocado = variety)
    }

    fun onCreateStatusChange(status: String) {
        _uiState.value = _uiState.value.copy(
            createStatus = status,
            createEndDatetime = if (status == "OPEN") "" else _uiState.value.createEndDatetime
        )
    }

    fun onStartDatetimeSelected(date: Date) {
        _uiState.value = _uiState.value.copy(
            createStartDatetime = DateUtils.formatToApiDate(date) + "T00:00:00Z", // Simplified for now
            showStartDateTimePicker = false
        )
    }

    fun onEndDatetimeSelected(date: Date) {
        _uiState.value = _uiState.value.copy(
            createEndDatetime = DateUtils.formatToApiDate(date) + "T00:00:00Z",
            showEndDateTimePicker = false
        )
    }

    fun showStartDateTimePicker() {
        _uiState.value = _uiState.value.copy(showStartDateTimePicker = true)
    }

    fun hideStartDateTimePicker() {
        _uiState.value = _uiState.value.copy(showStartDateTimePicker = false)
    }

    fun showEndDateTimePicker() {
        _uiState.value = _uiState.value.copy(showEndDateTimePicker = true)
    }

    fun hideEndDateTimePicker() {
        _uiState.value = _uiState.value.copy(showEndDateTimePicker = false)
    }

    fun createTrade() {
        val currentState = _uiState.value
        
        if (currentState.createPartyId == null) {
            _uiState.value = currentState.copy(error = "Debe seleccionar un contacto")
            return
        }
        
        if (currentState.createStartDatetime.isBlank()) {
            _uiState.value = currentState.copy(error = "La fecha de inicio es requerida")
            return
        }
        
        if (currentState.createVarietyAvocado.isBlank()) {
            _uiState.value = currentState.copy(error = "La variedad es requerida")
            return
        }

        val discountWeight = currentState.createDiscountWeightPerTray.toDoubleOrNull()
        if (discountWeight == null) {
            _uiState.value = currentState.copy(error = "El descuento de peso debe ser un número válido")
            return
        }

        if (currentState.createStatus == "CLOSED" && currentState.createEndDatetime.isBlank()) {
            _uiState.value = currentState.copy(error = "La fecha de fin es requerida para transacciones pasadas")
            return
        }

        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            val endDatetime = if (currentState.createStatus == "CLOSED") currentState.createEndDatetime else null

            tradesRepository.createTrade(
                partyId = currentState.createPartyId,
                shipmentId = shipmentId,
                tradeType = currentState.createTradeType,
                startDatetime = currentState.createStartDatetime,
                endDatetime = endDatetime,
                discountWeightPerTray = discountWeight,
                varietyAvocado = currentState.createVarietyAvocado
            )
            .onSuccess {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    showCreateDialog = false
                )
                loadTrades() // Reload after success
            }
            .onFailure { exception ->
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = exception.message ?: "Error al crear transacción"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun syncTrade(tradeId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val status = manualSyncManager.syncNow()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = if (status.state == "error") status.message else null
                )
            }
        }
    }
}

data class ShipmentsDetailUiState(
    val purchases: List<Trade> = emptyList(),
    val sales: List<Trade> = emptyList(),
    val expenses: List<ShipmentExpense> = emptyList(),
    val suppliers: List<Party> = emptyList(),
    val clients: List<Party> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingSyncTradeIds: Set<Int> = emptySet(),
    
    // Create Trade State
    val showCreateDialog: Boolean = false,
    val createTradeType: String = "PURCHASE",
    val createPartyId: Int? = null,
    val createStartDatetime: String = "",
    val createEndDatetime: String = "",
    val createDiscountWeightPerTray: String = "",
    val createVarietyAvocado: String = "",
    val createStatus: String = "OPEN",
    val showStartDateTimePicker: Boolean = false,
    val showEndDateTimePicker: Boolean = false,

    // Create Expense State
    val showCreateExpenseSheet: Boolean = false,
    val isSavingExpense: Boolean = false,
    val expenseCategory: String = ShipmentExpenseCategory.LABOR,
    val expenseSubcategory: String? = null,
    val expenseAmount: String = "",
    val expenseQuantity: String = "",
    val expenseUnitPrice: String = "",
    val expenseDescription: String = "",
    val expenseError: String? = null
)
