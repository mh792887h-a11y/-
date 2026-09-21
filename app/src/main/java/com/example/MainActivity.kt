package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.CivilFundViewModel
import com.example.ui.components.AppHeader
import com.example.ui.dialogs.AboutDialog
import com.example.ui.dialogs.ActualCashCountDialog
import com.example.ui.dialogs.AddDebtDialog
import com.example.ui.dialogs.AddExpenseDialog
import com.example.ui.dialogs.AddIncomeDialog
import com.example.ui.dialogs.CancelTransactionConfirmDialog
import com.example.ui.dialogs.DateInfoDialog
import com.example.ui.dialogs.PayDebtDialog
import com.example.ui.dialogs.PayDirectorDialog
import com.example.ui.dialogs.ReceiptConfirmationDialog
import com.example.ui.dialogs.SellFormDialog
import com.example.ui.dialogs.UserSwitchDialog
import com.example.ui.screens.CashFundScreen
import com.example.ui.screens.DailyClosingScreen
import com.example.ui.screens.DailySheetScreen
import com.example.ui.screens.DirectorShareScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.IncomeExpenseDebtScreen
import com.example.ui.screens.OperationsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyPrimary
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: CivilFundViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Strictly enforce Right-to-Left (RTL) Arabic layout
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    CivilFundApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun CivilFundApp(viewModel: CivilFundViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val todayClosing by viewModel.todayClosing.collectAsState()
    val todayTransactions by viewModel.todayTransactions.collectAsState()
    val feeSettings by viewModel.feeSettings.collectAsState()
    val users by viewModel.users.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()

    // Dialog states
    val showSellForm by viewModel.showSellFormModal.collectAsState()
    val showPayDirector by viewModel.showPayDirectorModal.collectAsState()
    val directorRemaining by viewModel.directorRemaining.collectAsState()
    val lastReceipt by viewModel.lastSavedReceipt.collectAsState()
    val showAddExpense by viewModel.showAddExpenseModal.collectAsState()
    val showAddIncome by viewModel.showAddIncomeModal.collectAsState()
    val showAddDebt by viewModel.showAddDebtModal.collectAsState()
    val selectedDebtForPayment by viewModel.selectedDebtForPayment.collectAsState()
    val txToCancel by viewModel.txToCancel.collectAsState()
    val showActualCash by viewModel.showActualCashModal.collectAsState()
    val showCloseDailyConfirm by viewModel.showCloseDailyConfirmModal.collectAsState()
    val showDateInfo by viewModel.showDateInfoModal.collectAsState()
    val showAbout by viewModel.showAboutModal.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Handle snackbars
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column(modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())) {
                AppHeader(
                    currentUser = currentUser,
                    onSettingsClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onUserClick = { },
                    onDateClick = { viewModel.showDateInfo(true) }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xFF0A192F),
                tonalElevation = 8.dp,
                modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            ) {
                val navItems = listOf(
                    Triple(AppScreen.DASHBOARD, Icons.Default.Home, "الرئيسية"),
                    Triple(AppScreen.OPERATIONS, Icons.Default.ReceiptLong, "العمليات"),
                    Triple(AppScreen.DAILY_SHEET, Icons.Default.CalendarMonth, "اليومية"),
                    Triple(AppScreen.CASH_FUND, Icons.Default.AccountBalanceWallet, "الصندوق"),
                    Triple(AppScreen.REPORTS, Icons.Default.BarChart, "التقارير"),
                    Triple(AppScreen.SETTINGS, Icons.Default.Settings, "الإعدادات")
                )
                navItems.forEach { (screen, icon, label) ->
                    val selected = currentScreen == screen
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.navigateTo(screen) },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                                if (selected) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(18.dp)
                                            .height(2.dp)
                                            .clip(RoundedCornerShape(1.dp))
                                            .background(Color(0xFF38BDF8))
                                    )
                                }
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF38BDF8),
                            selectedTextColor = Color(0xFF38BDF8),
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8),
                            indicatorColor = Color.Transparent
                        ),
                        modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> HomeScreen(
                        viewModel = viewModel,
                        closing = todayClosing,
                        todayTransactions = todayTransactions
                    )
                    AppScreen.DAILY_SHEET -> DailySheetScreen(viewModel = viewModel)
                    AppScreen.DIRECTOR_SHARE -> DirectorShareScreen(viewModel = viewModel)
                    AppScreen.CASH_FUND -> CashFundScreen(viewModel = viewModel)
                    AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                    AppScreen.OPERATIONS -> OperationsScreen(viewModel = viewModel)
                    AppScreen.DAILY -> DailyClosingScreen(
                        viewModel = viewModel,
                        closing = todayClosing,
                        transactions = todayTransactions
                    )
                    AppScreen.INCOME_EXPENSE_DEBT -> IncomeExpenseDebtScreen(viewModel = viewModel)
                    AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Sell Form Dialog
    if (showSellForm) {
        var recentNames by remember { mutableStateOf(emptyList<String>()) }
        LaunchedEffect(Unit) {
            recentNames = viewModel.getRecentCitizenNames()
        }

        SellFormDialog(
            feeSettings = feeSettings,
            recentCitizenNames = recentNames,
            onDismiss = { viewModel.showSellForm(false) },
            onSubmit = { citizen, formNum, recordNum, type, gender, notes ->
                viewModel.sellForm(citizen, formNum, recordNum, type, gender, notes)
            }
        )
    }

    // Pay Director Dialog
    if (showPayDirector) {
        PayDirectorDialog(
            currentRemaining = directorRemaining,
            onDismiss = { viewModel.showPayDirector(false) },
            onSubmit = { amt, notes ->
                viewModel.payDirector(amt, notes)
            }
        )
    }

    // Receipt Confirmation Dialog
    if (lastReceipt != null) {
        ReceiptConfirmationDialog(
            receipt = lastReceipt!!,
            directorateName = directorateName,
            onDismiss = { viewModel.dismissReceipt() }
        )
    }

    // Add Expense Dialog
    if (showAddExpense) {
        AddExpenseDialog(
            onDismiss = { viewModel.showAddExpense(false) },
            onSubmit = { stmt, amt, whom, notes ->
                viewModel.addExpense(stmt, amt, whom, notes)
            }
        )
    }

    // Add Income Dialog
    if (showAddIncome) {
        AddIncomeDialog(
            onDismiss = { viewModel.showAddIncome(false) },
            onSubmit = { cat, stmt, amt, whom, notes ->
                viewModel.addOtherIncome(cat, stmt, amt, whom, notes)
            }
        )
    }

    // Add Debt Dialog
    if (showAddDebt) {
        AddDebtDialog(
            onDismiss = { viewModel.showAddDebt(false) },
            onSubmit = { person, amt, reason, notes ->
                viewModel.addDebt(person, amt, reason, notes)
            }
        )
    }

    // Pay Debt Dialog
    if (selectedDebtForPayment != null) {
        PayDebtDialog(
            debt = selectedDebtForPayment!!,
            onDismiss = { viewModel.selectDebtForPayment(null) },
            onSubmit = { amt, notes ->
                viewModel.payDebt(selectedDebtForPayment!!.id, amt, notes)
            }
        )
    }

    // Actual Cash Count Dialog
    if (showActualCash) {
        val expBal = todayClosing?.expectedBalance ?: 0.0
        val actBal = todayClosing?.actualBalance ?: expBal
        ActualCashCountDialog(
            expectedBalance = expBal,
            currentActual = actBal,
            onDismiss = { viewModel.showActualCashCount(false) },
            onSubmit = { actualCash ->
                viewModel.updateActualCashBalance(actualCash)
            }
        )
    }

    // Close Daily Confirm Dialog
    if (showCloseDailyConfirm) {
        val expBal = todayClosing?.expectedBalance ?: 0.0
        val actBal = todayClosing?.actualBalance ?: expBal
        ActualCashCountDialog(
            expectedBalance = expBal,
            currentActual = actBal,
            onDismiss = { viewModel.showCloseDailyConfirm(false) },
            onSubmit = { actualCash ->
                viewModel.closeDaily(actualCash)
            }
        )
    }

    // Cancel Transaction Dialog
    if (txToCancel != null) {
        CancelTransactionConfirmDialog(
            transaction = txToCancel!!,
            onDismiss = { viewModel.promptCancelTx(null) },
            onConfirm = { reason ->
                viewModel.cancelTransaction(txToCancel!!.id, reason)
            }
        )
    }

    // Date Info Dialog
    if (showDateInfo) {
        DateInfoDialog(onDismiss = { viewModel.showDateInfo(false) })
    }

    // About Dialog
    if (showAbout) {
        AboutDialog(onDismiss = { viewModel.showAbout(false) })
    }
}
