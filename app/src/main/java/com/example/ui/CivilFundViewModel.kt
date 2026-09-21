package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.CashMovementEntity
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.DebtEntity
import com.example.data.entity.DirectorPaymentEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FeeSettingEntity
import com.example.data.entity.IncomeEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.CivilFundRepository
import com.example.util.HijriDateUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val titleArabic: String) {
    DASHBOARD("الرئيسية"),
    DAILY_SHEET("كشف الاستمارات"),
    DIRECTOR_SHARE("حق الإدارة"),
    CASH_FUND("الصندوق واليومية"),
    SETTINGS("الإعدادات"),
    // Preserved for backwards compatibility
    OPERATIONS("العمليات"),
    DAILY("اليومية"),
    INCOME_EXPENSE_DEBT("المالية"),
    REPORTS("التقارير")
}

@OptIn(ExperimentalCoroutinesApi::class)
class CivilFundViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = CivilFundRepository(db)

    val todayDate: String = HijriDateUtil.getTodayGregorianString()

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _directorateName = MutableStateFlow("مصلحة الأحوال المدنية والسجل المدني")
    val directorateName: StateFlow<String> = _directorateName.asStateFlow()

    // Dialog & UI states
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _lastSavedReceipt = MutableStateFlow<TransactionEntity?>(null)
    val lastSavedReceipt: StateFlow<TransactionEntity?> = _lastSavedReceipt.asStateFlow()

    private val _showSellFormModal = MutableStateFlow(false)
    val showSellFormModal: StateFlow<Boolean> = _showSellFormModal.asStateFlow()

    private val _showPayDirectorModal = MutableStateFlow(false)
    val showPayDirectorModal: StateFlow<Boolean> = _showPayDirectorModal.asStateFlow()

    private val _showAddExpenseModal = MutableStateFlow(false)
    val showAddExpenseModal: StateFlow<Boolean> = _showAddExpenseModal.asStateFlow()

    private val _showAddIncomeModal = MutableStateFlow(false)
    val showAddIncomeModal: StateFlow<Boolean> = _showAddIncomeModal.asStateFlow()

    private val _showAddDebtModal = MutableStateFlow(false)
    val showAddDebtModal: StateFlow<Boolean> = _showAddDebtModal.asStateFlow()

    private val _selectedDebtForPayment = MutableStateFlow<DebtEntity?>(null)
    val selectedDebtForPayment: StateFlow<DebtEntity?> = _selectedDebtForPayment.asStateFlow()

    private val _selectedTxForDetail = MutableStateFlow<TransactionEntity?>(null)
    val selectedTxForDetail: StateFlow<TransactionEntity?> = _selectedTxForDetail.asStateFlow()

    private val _txToCancel = MutableStateFlow<TransactionEntity?>(null)
    val txToCancel: StateFlow<TransactionEntity?> = _txToCancel.asStateFlow()

    private val _showActualCashModal = MutableStateFlow(false)
    val showActualCashModal: StateFlow<Boolean> = _showActualCashModal.asStateFlow()

    private val _showCloseDailyConfirmModal = MutableStateFlow(false)
    val showCloseDailyConfirmModal: StateFlow<Boolean> = _showCloseDailyConfirmModal.asStateFlow()

    private val _showUserSwitchModal = MutableStateFlow(false)
    val showUserSwitchModal: StateFlow<Boolean> = _showUserSwitchModal.asStateFlow()

    private val _showDateInfoModal = MutableStateFlow(false)
    val showDateInfoModal: StateFlow<Boolean> = _showDateInfoModal.asStateFlow()

    private val _showAboutModal = MutableStateFlow(false)
    val showAboutModal: StateFlow<Boolean> = _showAboutModal.asStateFlow()

    private val _showDebtsListModal = MutableStateFlow(false)
    val showDebtsListModal: StateFlow<Boolean> = _showDebtsListModal.asStateFlow()

    private val _showUpdateOpeningBalanceModal = MutableStateFlow(false)
    val showUpdateOpeningBalanceModal: StateFlow<Boolean> = _showUpdateOpeningBalanceModal.asStateFlow()

    private val _directorPaymentToPrint = MutableStateFlow<DirectorPaymentEntity?>(null)
    val directorPaymentToPrint: StateFlow<DirectorPaymentEntity?> = _directorPaymentToPrint.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Date selection for Daily Sheet (عرض جميع الأيام)
    private val _selectedReportDate = MutableStateFlow(todayDate)
    val selectedReportDate: StateFlow<String> = _selectedReportDate.asStateFlow()

    val allTransactionDates: StateFlow<List<String>> = repository.allTransactionDates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf(todayDate))

    val selectedDateTransactions: StateFlow<List<TransactionEntity>> = _selectedReportDate
        .flatMapLatest { date -> repository.getTransactionsByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateClosing: StateFlow<DailyClosingEntity?> = _selectedReportDate
        .flatMapLatest { date -> repository.getDailyClosing(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Director Share (حق الإدارة - 200 ريال عن كل استمارة جديد عبر جميع الأيام)
    val allDirectorPayments: StateFlow<List<DirectorPaymentEntity>> = repository.allDirectorPayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalNewFormsCount: StateFlow<Int> = repository.totalNewFormsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val activeNewTransactions: StateFlow<List<TransactionEntity>> = repository.activeNewTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalDirectorEarned: StateFlow<Double> = repository.totalDirectorEarned
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalDirectorPaid: StateFlow<Double> = repository.totalDirectorPaid
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val directorRemaining: StateFlow<Double> = repository.directorRemaining
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Active Debts
    val activeDebts: StateFlow<List<DebtEntity>> = repository.activeDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRemainingDebts: StateFlow<Double> = repository.totalRemainingDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Search & Filter state in Operations
    val searchQuery = MutableStateFlow("")
    val filterType = MutableStateFlow("الكل") // "الكل", "جديد", "تجديد", "بدل فاقد", "بدل تالف"
    val filterGender = MutableStateFlow("الكل") // "الكل", "ذكر", "أنثى"
    val filterStatus = MutableStateFlow("الكل") // "الكل", "ACTIVE", "CANCELLED"

    // Data streams from Room
    val todayClosing: StateFlow<DailyClosingEntity?> = repository.getDailyClosing(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayTransactions: StateFlow<List<TransactionEntity>> = repository.getTransactionsByDate(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        searchQuery,
        filterType,
        filterGender,
        filterStatus
    ) { txs, query, type, gender, status ->
        txs.filter { tx ->
            val matchesQuery = query.isBlank() ||
                    tx.citizenName.contains(query, ignoreCase = true) ||
                    tx.receiptNumber.contains(query, ignoreCase = true) ||
                    tx.gregorianDate.contains(query, ignoreCase = true) ||
                    tx.salePrice.toString().contains(query)

            val matchesType = type == "الكل" || tx.transactionType == type
            val matchesGender = gender == "الكل" || tx.gender == gender
            val matchesStatus = status == "الكل" || tx.status == status

            matchesQuery && matchesType && matchesGender && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val feeSettings: StateFlow<List<FeeSettingEntity>> = repository.allFeeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashMovements: StateFlow<List<CashMovementEntity>> = repository.allCashMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomes: StateFlow<List<IncomeEntity>> = repository.allIncomes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInitialData()
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun showSellForm(show: Boolean) {
        _showSellFormModal.value = show
    }

    fun showAddExpense(show: Boolean) {
        _showAddExpenseModal.value = show
    }

    fun showAddIncome(show: Boolean) {
        _showAddIncomeModal.value = show
    }

    fun showAddDebt(show: Boolean) {
        _showAddDebtModal.value = show
    }

    fun selectDebtForPayment(debt: DebtEntity?) {
        _selectedDebtForPayment.value = debt
    }

    fun selectTxForDetail(tx: TransactionEntity?) {
        _selectedTxForDetail.value = tx
    }

    fun promptCancelTx(tx: TransactionEntity?) {
        _txToCancel.value = tx
    }

    fun showActualCashCount(show: Boolean) {
        _showActualCashModal.value = show
    }

    fun showCloseDailyConfirm(show: Boolean) {
        _showCloseDailyConfirmModal.value = show
    }

    fun showUserSwitch(show: Boolean) {
        _showUserSwitchModal.value = show
    }

    fun showDateInfo(show: Boolean) {
        _showDateInfoModal.value = show
    }

    fun showAbout(show: Boolean) {
        _showAboutModal.value = show
    }

    fun dismissReceipt() {
        _lastSavedReceipt.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _currentUser.value = repository.getCurrentUser()
            _directorateName.value = repository.getDirectorateName()
            repository.recalculateDailyClosing(todayDate)
        }
    }

    fun switchUser(user: UserEntity) {
        viewModelScope.launch {
            repository.setCurrentUser(user.id)
            _currentUser.value = user
            _showUserSwitchModal.value = false
            _snackbarMessage.value = "تم التبديل إلى: ${user.fullName} (${user.role})"
        }
    }

    fun setSelectedReportDate(date: String) {
        _selectedReportDate.value = date
    }

    fun showPayDirector(show: Boolean) {
        _showPayDirectorModal.value = show
    }

    fun dismissDirectorPaymentReceipt() {
        _directorPaymentToPrint.value = null
    }

    fun showDebtsList(show: Boolean) {
        _showDebtsListModal.value = show
    }

    fun showUpdateOpeningBalance(show: Boolean) {
        _showUpdateOpeningBalanceModal.value = show
    }

    fun updateOpeningBalance(amount: Double, reason: String) {
        viewModelScope.launch {
            val result = repository.updateOpeningBalance(todayDate, amount, reason)
            result.onSuccess {
                _showUpdateOpeningBalanceModal.value = false
                _snackbarMessage.value = "تم تحديث رصيد بداية اليوم بنجاح إلى $amount ريال."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر تعديل رصيد بداية اليوم."
            }
        }
    }

    fun deleteTransaction(txId: Long) {
        viewModelScope.launch {
            val result = repository.deleteTransaction(txId)
            result.onSuccess {
                _selectedTxForDetail.value = null
                _snackbarMessage.value = "تم حذف الاستمارة وإلغاء أثرها من الصندوق بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر حذف الاستمارة."
            }
        }
    }

    fun deleteExpense(expenseId: Long) {
        viewModelScope.launch {
            val result = repository.deleteExpense(expenseId)
            result.onSuccess {
                _snackbarMessage.value = "تم حذف الخرج واسترجاع مبلغه للصندوق بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر حذف الخرج."
            }
        }
    }

    fun deleteIncome(incomeId: Long) {
        viewModelScope.launch {
            val result = repository.deleteIncome(incomeId)
            result.onSuccess {
                _snackbarMessage.value = "تم حذف الدخل بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر حذف الدخل."
            }
        }
    }

    fun deleteDebt(debtId: Long) {
        viewModelScope.launch {
            val result = repository.deleteDebt(debtId)
            result.onSuccess {
                _selectedDebtForPayment.value = null
                _snackbarMessage.value = "تم حذف الدين واسترجاع مبلغه للصندوق بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر حذف الدين."
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.recalculateDailyClosing(todayDate)
            kotlinx.coroutines.delay(400)
            _isRefreshing.value = false
        }
    }

    fun payDirector(amount: Double, notes: String?, isFullPayment: Boolean = false) {
        if (amount <= 0) {
            _snackbarMessage.value = "يرجى إدخال مبلغ صحيح للصرف."
            return
        }
        viewModelScope.launch {
            val result = repository.payDirector(amount, notes)
            result.onSuccess { payment ->
                _showPayDirectorModal.value = false
                _directorPaymentToPrint.value = payment
                _snackbarMessage.value = "تمت محاسبة المدير وصرف مبلغ $amount ريال بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر صرف المبلغ للمدير."
            }
        }
    }

    fun sellForm(
        citizenName: String,
        formNumber: String,
        recordNumber: String,
        formTypeNameArabic: String,
        gender: String,
        notes: String?,
        customGregorianDate: String? = null,
        customHijriDate: String? = null
    ) {
        if (citizenName.isBlank()) {
            _snackbarMessage.value = "يرجى كتابة اسم المواطن رباعي."
            return
        }
        if (formNumber.isBlank() || recordNumber.isBlank()) {
            _snackbarMessage.value = "يرجى تسجيل رقم الاستمارة ورقم القيد بخاناتهما."
            return
        }
        viewModelScope.launch {
            val result = repository.sellForm(
                citizenName, formNumber, recordNumber, formTypeNameArabic, gender, notes,
                customGregorianDate, customHijriDate
            )
            result.onSuccess { tx ->
                _showSellFormModal.value = false
                _lastSavedReceipt.value = tx
                _snackbarMessage.value = "تم تسجيل العملية بنجاح برقم: ${tx.receiptNumber}"
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر حفظ العملية، حاول مرة أخرى."
            }
        }
    }

    fun cancelTransaction(txId: Long, reason: String) {
        if (reason.isBlank()) {
            _snackbarMessage.value = "يرجى كتابة سبب الإلغاء."
            return
        }
        viewModelScope.launch {
            val result = repository.cancelTransaction(txId, reason)
            result.onSuccess {
                _txToCancel.value = null
                _selectedTxForDetail.value = null
                _snackbarMessage.value = "تم إلغاء العملية وعكس أثرها في الصندوق بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر إلغاء العملية."
            }
        }
    }

    fun addExpense(statement: String, amount: Double, withWhom: String, notes: String?) {
        if (statement.isBlank() || amount <= 0 || withWhom.isBlank()) {
            _snackbarMessage.value = "يرجى ملء جميع الحقول بمبالغ صحيحة."
            return
        }
        viewModelScope.launch {
            val result = repository.addExpense(statement, amount, withWhom, notes)
            result.onSuccess {
                _showAddExpenseModal.value = false
                _snackbarMessage.value = "تم تسجيل الخرج وخصمه من الصندوق بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر تسجيل الخرج."
            }
        }
    }

    fun addOtherIncome(category: String, statement: String, amount: Double, withWhom: String, notes: String?) {
        if (statement.isBlank() || amount <= 0) {
            _snackbarMessage.value = "يرجى إدخال بيان ومبلغ صحيح."
            return
        }
        viewModelScope.launch {
            val result = repository.addOtherIncome(category, statement, amount, withWhom, notes)
            result.onSuccess {
                _showAddIncomeModal.value = false
                _snackbarMessage.value = "تمت إضافة الدخل إلى حركة الصندوق بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر إضافة الدخل."
            }
        }
    }

    fun addDebt(personName: String, amount: Double, reason: String, notes: String?) {
        if (personName.isBlank() || amount <= 0) {
            _snackbarMessage.value = "يرجى إدخال اسم الشخص ومبلغ الدين."
            return
        }
        viewModelScope.launch {
            val result = repository.addDebt(personName, amount, reason, notes)
            result.onSuccess {
                _showAddDebtModal.value = false
                _snackbarMessage.value = "تم تسجيل الدين بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر تسجيل الدين."
            }
        }
    }

    fun payDebt(debtId: Long, amount: Double, notes: String?) {
        if (amount <= 0) {
            _snackbarMessage.value = "مبلغ السداد يجب أن يكون أكبر من الصفر."
            return
        }
        viewModelScope.launch {
            val result = repository.payDebt(debtId, amount, notes)
            result.onSuccess {
                _selectedDebtForPayment.value = null
                _snackbarMessage.value = "تم تسجيل السداد وإضافته للصندوق بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر تسجيل السداد."
            }
        }
    }

    fun updateActualCashBalance(amount: Double) {
        viewModelScope.launch {
            val result = repository.updateActualBalance(todayDate, amount)
            result.onSuccess {
                _showActualCashModal.value = false
                _snackbarMessage.value = "تم تحديث الرصيد الفعلي للصندوق ومقارنته بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر تحديث الرصيد."
            }
        }
    }

    fun closeDaily(actualCash: Double) {
        viewModelScope.launch {
            val result = repository.closeDailyClosing(todayDate, actualCash)
            result.onSuccess {
                _showCloseDailyConfirmModal.value = false
                _snackbarMessage.value = "تم إغلاق اليومية بنجاح وتأمين الحسابات."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر إغلاق اليومية."
            }
        }
    }

    fun reopenDaily() {
        viewModelScope.launch {
            val result = repository.reopenDailyClosing(todayDate)
            result.onSuccess {
                _snackbarMessage.value = "تمت إعادة فتح اليومية بنجاح بواسطة المدير."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "فقط المدير يستطيع إعادة فتح اليومية."
            }
        }
    }

    fun updateFeeSetting(formType: String, salePrice: Double, stateShare: Double, directorateShare: Double) {
        viewModelScope.launch {
            val result = repository.updateFeeSetting(formType, salePrice, stateShare, directorateShare)
            result.onSuccess {
                _snackbarMessage.value = "تم تعديل الرسوم بنجاح. العمليات السابقة تحتفظ بأسعارها."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر تعديل الرسوم."
            }
        }
    }

    fun updateDirectorateName(newName: String) {
        viewModelScope.launch {
            repository.setDirectorateName(newName)
            _directorateName.value = newName
            _snackbarMessage.value = "تم حفظ اسم الإدارة بنجاح."
        }
    }

    suspend fun getRecentCitizenNames(): List<String> {
        return repository.getRecentCitizenNames()
    }

    suspend fun exportBackupJson(): String {
        return repository.exportDatabaseToJson()
    }

    fun restoreBackupJson(jsonStr: String) {
        viewModelScope.launch {
            val result = repository.restoreDatabaseFromJson(jsonStr)
            result.onSuccess {
                _snackbarMessage.value = "تمت استعادة البيانات بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "فشل استعادة البيانات."
            }
        }
    }

    fun payDirector(amount: Double, notes: String? = null) {
        if (amount <= 0) {
            _snackbarMessage.value = "يرجى إدخال مبلغ صحيح."
            return
        }
        viewModelScope.launch {
            val result = repository.payDirector(amount, notes)
            result.onSuccess { payment ->
                _directorPaymentToPrint.value = payment
                _snackbarMessage.value = "تم تسجيل صرف الدفعة للمدير بنجاح."
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "تعذر صرف المبلغ."
            }
        }
    }
}
