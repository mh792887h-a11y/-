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
import androidx.compose.material.icons.filled.Settings
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
    val showUserSwitch by viewModel.showUserSwitchModal.collectAsState()
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
                    onUserClick = { viewModel.showUserSwitch(true) },
                    onDateClick = { viewModel.showDateInfo(true) }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier.padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
            ) {
                NavigationBarItem(
                    selected = currentScreen == AppScreen.DASHBOARD,
                    onClick = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "الرئيسية") },
                    label = { Text("الرئيسية", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.DAILY_SHEET,
                    onClick = { viewModel.navigateTo(AppScreen.DAILY_SHEET) },
                    icon = { Icon(imageVector = Icons.Default.ListAlt, contentDescription = "كشف الاستمارات") },
                    label = { Text("كشف الاستمارات", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_daily_sheet")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.DIRECTOR_SHARE,
                    onClick = { viewModel.navigateTo(AppScreen.DIRECTOR_SHARE) },
                    icon = { Icon(imageVector = Icons.Default.AttachMoney, contentDescription = "حق الإدارة") },
                    label = { Text("حق الإدارة", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_director_share")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.CASH_FUND,
                    onClick = { viewModel.navigateTo(AppScreen.CASH_FUND) },
                    icon = { Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = "الصندوق") },
                    label = { Text("الصندوق واليومية", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_cash_fund")
                )

                NavigationBarItem(
                    selected = currentScreen == AppScreen.SETTINGS,
                    onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = "الإعدادات") },
                    label = { Text("الإعدادات", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NavyPrimary,
                        selectedTextColor = NavyPrimary,
                        indicatorColor = NavyPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_settings")
                )
            }
        },
        floatingActionButton = {
            if (currentScreen == AppScreen.DASHBOARD || currentScreen == AppScreen.DAILY_SHEET) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showSellForm(true) },
                    containerColor = NavyPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("quick_sell_fab")
                ) {
                    Text(text = "🪪", fontSize = 20.sp)
                    Text(
                        text = " بيع استمارة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
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

    // User Switch Dialog
    if (showUserSwitch) {
        UserSwitchDialog(
            users = users,
            currentUser = currentUser,
            onDismiss = { viewModel.showUserSwitch(false) },
            onSelectUser = { user ->
                viewModel.switchUser(user)
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
