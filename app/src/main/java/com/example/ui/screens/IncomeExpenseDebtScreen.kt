package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DebtEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.IncomeEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtil

@Composable
fun IncomeExpenseDebtScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: الخرج, 1: الدخل, 2: الديون

    val expenses by viewModel.expenses.collectAsState()
    val incomes by viewModel.incomes.collectAsState()
    val debts by viewModel.debts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("income_expense_debt_screen")
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = NavyPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("الخرج (منصرفات)", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_expenses")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("الدخل الآخر", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_incomes")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("الديون", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_debts")
            )
        }

        when (selectedTab) {
            0 -> ExpensesTab(
                expenses = expenses,
                onAddClick = { viewModel.showAddExpense(true) }
            )
            1 -> IncomesTab(
                incomes = incomes,
                onAddClick = { viewModel.showAddIncome(true) }
            )
            2 -> DebtsTab(
                debts = debts,
                onAddClick = { viewModel.showAddDebt(true) },
                onPayClick = { debt -> viewModel.selectDebtForPayment(debt) }
            )
        }
    }
}

@Composable
private fun ExpensesTab(
    expenses: List<ExpenseEntity>,
    onAddClick: () -> Unit
) {
    val totalExpense = expenses.filter { it.status == "ACTIVE" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي المنصرفات المسجلة:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(CurrencyUtil.formatRiyal(totalExpense), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ExpenseRed)
                }

                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    modifier = Modifier.testTag("btn_add_expense_tab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة خرج", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                EmptyStateCard("لا توجد منصرفات مسجلة.")
            }
        } else {
            items(expenses, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.statement, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "مع: ${item.withWhom}", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(text = "${item.gregorianDate} • ${item.timeString}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Text(
                            text = CurrencyUtil.formatRiyal(item.amount),
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomesTab(
    incomes: List<IncomeEntity>,
    onAddClick: () -> Unit
) {
    val totalIncome = incomes.filter { it.status == "ACTIVE" }.sumOf { it.amount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي الإيرادات المسجلة:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(CurrencyUtil.formatRiyal(totalIncome), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                }

                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    modifier = Modifier.testTag("btn_add_income_tab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة دخل", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (incomes.isEmpty()) {
            item {
                EmptyStateCard("لا توجد إيرادات أخرى مسجلة.")
            }
        } else {
            items(incomes, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.statement, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "الفئة: ${item.category} • من: ${item.withWhom}", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(text = "${item.gregorianDate} • ${item.timeString}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                        Text(
                            text = CurrencyUtil.formatRiyal(item.amount),
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtsTab(
    debts: List<DebtEntity>,
    onAddClick: () -> Unit,
    onPayClick: (DebtEntity) -> Unit
) {
    val totalRemaining = debts.sumOf { it.remainingAmount }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("إجمالي الديون المتبقية:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(CurrencyUtil.formatRiyal(totalRemaining), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WarningAmber)
                }

                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber),
                    modifier = Modifier.testTag("btn_add_debt_tab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تسجيل دين", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        if (debts.isEmpty()) {
            item {
                EmptyStateCard("لا توجد ديون مسجلة.")
            }
        } else {
            items(debts, key = { it.id }) { debt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = debt.personName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when (debt.status) {
                                    "FULLY_PAID" -> Color(0xFFE8F5E9)
                                    "PARTIALLY_PAID" -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFFFEBEE)
                                }
                            ) {
                                Text(
                                    text = when (debt.status) {
                                        "FULLY_PAID" -> "مسدد بالكامل ✓"
                                        "PARTIALLY_PAID" -> "مسدد جزئياً"
                                        else -> "غير مسدد"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (debt.status) {
                                        "FULLY_PAID" -> IncomeGreen
                                        "PARTIALLY_PAID" -> WarningAmber
                                        else -> ExpenseRed
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "السبب: ${debt.reason}", fontSize = 12.sp, color = Color(0xFF64748B))

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "المبلغ الأصلي: ${CurrencyUtil.formatRiyal(debt.originalAmount)}", fontSize = 12.sp)
                            Text(text = "المسدد: ${CurrencyUtil.formatRiyal(debt.paidAmount)}", fontSize = 12.sp, color = IncomeGreen)
                            Text(
                                text = "المتبقي: ${CurrencyUtil.formatRiyal(debt.remainingAmount)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (debt.remainingAmount > 0) ExpenseRed else IncomeGreen
                            )
                        }

                        if (debt.remainingAmount > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onPayClick(debt) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                            ) {
                                Text("تسديد مبلغ من الدين", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(message, color = Color(0xFF94A3B8))
        }
    }
}
