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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.AppScreen
import com.example.ui.CivilFundViewModel
import com.example.ui.components.OperationActionButton
import com.example.ui.components.StatCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtil

@Composable
fun HomeScreen(
    viewModel: CivilFundViewModel,
    closing: DailyClosingEntity?,
    todayTransactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val openingBalance = closing?.openingBalance ?: 0.0
    val totalIncome = closing?.totalIncome ?: 0.0
    val totalExpenses = closing?.totalExpenses ?: 0.0
    val netToday = closing?.netToday ?: 0.0
    val expectedBalance = closing?.expectedBalance ?: 0.0
    val actualBalance = closing?.actualBalance ?: expectedBalance
    val diff = closing?.difference ?: (actualBalance - expectedBalance)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Primary Button: بيع استمارة
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .clickable { viewModel.showSellForm(true) }
                    .testTag("hero_sell_form_button"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🪪", fontSize = 28.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "بيع استمارة جديدة",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "تسجيل فوري للمواطن واحتساب الحصص تلقائياً",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFD54F))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "تسجيل الآن",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }
            }
        }

        // Section 1: Financial Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "الحالة المالية للصندوق اليوم",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                // Row A: رصيد بداية اليوم & صافي اليوم
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "رصيد بداية اليوم",
                        amount = openingBalance,
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = NavyPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "صافي اليوم",
                        amount = netToday,
                        accentColor = if (netToday >= 0) IncomeGreen else ExpenseRed,
                        containerColor = if (netToday >= 0) IncomeGreenContainer else ExpenseRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row B: إجمالي الدخل & إجمالي الخرج
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "إجمالي الدخل اليوم",
                        amount = totalIncome,
                        icon = Icons.Default.ArrowDownward,
                        accentColor = IncomeGreen,
                        containerColor = IncomeGreenContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "إجمالي الخرج اليوم",
                        amount = totalExpenses,
                        icon = Icons.Default.ArrowUpward,
                        accentColor = ExpenseRed,
                        containerColor = ExpenseRedContainer,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row C: الرصيد المتوقع والفعلي وحالة الصندوق
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "الرصيد الدفتري المتوقع",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B)
                                )
                                Text(
                                    text = CurrencyUtil.formatRiyal(expectedBalance),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "الرصيد الفعلي بالجرد",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "تعديل الجرد",
                                        tint = InfoBlue,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { viewModel.showActualCashCount(true) }
                                    )
                                }
                                Text(
                                    text = CurrencyUtil.formatRiyal(actualBalance),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = InfoBlue
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFE2E8F0))

                        // Fund Status: Match / Deficit / Surplus
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (diff == 0.0) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (diff == 0.0) IncomeGreen else if (diff > 0) InfoBlue else ExpenseRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حالة الصندوق:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = when {
                                    diff == 0.0 -> IncomeGreenContainer
                                    diff > 0 -> Color(0xFFE0F2FE)
                                    else -> ExpenseRedContainer
                                }
                            ) {
                                Text(
                                    text = when {
                                        diff == 0.0 -> "مطابق تماماً ✓"
                                        diff > 0 -> "زيادة: +${CurrencyUtil.formatRiyal(diff)}"
                                        else -> "عجز: ${CurrencyUtil.formatRiyal(diff)}"
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = when {
                                        diff == 0.0 -> IncomeGreen
                                        diff > 0 -> InfoBlue
                                        else -> ExpenseRed
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Main Operations Buttons Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "العمليات والإجراءات السريعة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                // Row 1: إضافة دخل & إضافة خرج
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OperationActionButton(
                        title = "إضافة دخل",
                        icon = "➕",
                        onClick = { viewModel.showAddIncome(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_income"
                    )
                    OperationActionButton(
                        title = "إضافة خرج",
                        icon = "➖",
                        onClick = { viewModel.showAddExpense(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_expense"
                    )
                }

                // Row 2: إضافة دين & حركة الصندوق
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OperationActionButton(
                        title = "إضافة دين",
                        icon = "💳",
                        onClick = { viewModel.showAddDebt(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_debt"
                    )
                    OperationActionButton(
                        title = "حركة الصندوق",
                        icon = "💰",
                        onClick = { viewModel.navigateTo(AppScreen.CASH_FUND) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_cash_fund"
                    )
                }

                // Row 3: اليومية & التقارير
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OperationActionButton(
                        title = "اليومية والإغلاق",
                        icon = "📋",
                        onClick = { viewModel.navigateTo(AppScreen.DAILY) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_daily_closing"
                    )
                    OperationActionButton(
                        title = "التقارير الشاملة",
                        icon = "📊",
                        onClick = { viewModel.navigateTo(AppScreen.REPORTS) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_reports"
                    )
                }
            }
        }

        // Section 3: Today's Summary Overview (Counts & Shares)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "إحصائيات استمارات اليوم",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryPill(label = "الإجمالي", value = "${closing?.totalTransactions ?: 0}")
                        SummaryPill(label = "الذكور", value = "${closing?.malesCount ?: 0}")
                        SummaryPill(label = "الإناث", value = "${closing?.femalesCount ?: 0}")
                        SummaryPill(label = "جديد", value = "${closing?.newCount ?: 0}")
                        SummaryPill(label = "تجديد", value = "${closing?.renewCount ?: 0}")
                        SummaryPill(label = "فاقد", value = "${closing?.lostCount ?: 0}")
                        SummaryPill(label = "تالف", value = "${closing?.damagedCount ?: 0}")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFCBD5E1))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("حق الدولة", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(CurrencyUtil.formatRiyal(closing?.totalStateShare ?: 0.0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("حصة الإدارة", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(CurrencyUtil.formatRiyal(closing?.totalDirectorateShare ?: 0.0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("نصيب الصندوق", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(CurrencyUtil.formatRiyal(closing?.totalFundShare ?: 0.0), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InfoBlue)
                        }
                    }
                }
            }
        }

        // Section 4: Recent Transactions header & link
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "آخر عمليات اليوم (${todayTransactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                Text(
                    text = "عرض الكل",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = InfoBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.OPERATIONS) }
                )
            }
        }

        if (todayTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد عمليات مسجلة لهذا اليوم حتى الآن.\nاضغط «بيع استمارة جديدة» للبدء.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color(0xFF94A3B8),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        )
                    }
                }
            }
        } else {
            items(todayTransactions.take(5)) { tx ->
                TransactionCard(
                    tx = tx,
                    onClick = { viewModel.selectTxForDetail(tx) }
                )
            }
        }
    }
}

@Composable
fun SummaryPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
    }
}

@Composable
fun TransactionCard(
    tx: TransactionEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("tx_card_${tx.receiptNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (tx.status == "CANCELLED") Color(0xFFFFF1F2) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (tx.gender == "ذكر") Color(0xFFDBEAFE) else Color(0xFFFCE7F3)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (tx.gender == "ذكر") "👨" else "👩", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = tx.citizenName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "استمارة ${tx.transactionType}",
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${tx.receiptNumber}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtil.formatRiyal(tx.salePrice),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (tx.status == "CANCELLED") ExpenseRed else IncomeGreen
                )
                Text(
                    text = if (tx.status == "CANCELLED") "ملغاة" else tx.timeString,
                    fontSize = 11.sp,
                    color = if (tx.status == "CANCELLED") ExpenseRed else Color(0xFF64748B)
                )
            }
        }
    }
}
