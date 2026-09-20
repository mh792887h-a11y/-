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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.RemoveCircleOutline
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.AppScreen
import com.example.ui.CivilFundViewModel
import com.example.ui.components.StatCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.PrintAndExportUtil

@Composable
fun HomeScreen(
    viewModel: CivilFundViewModel,
    closing: DailyClosingEntity?,
    todayTransactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val directorRemaining by viewModel.directorRemaining.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

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
        // Section 1: Hero Action Button (بيع استمارة جديدة - الزر الرئيسي الفخم والمباشر)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .clickable { viewModel.showSellForm(true) }
                    .testTag("hero_sell_form_button"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
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
                                text = "تسجيل الاسم، رقم الاستمارة، ورقم القيد",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFD54F))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "+ بيع الآن",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }
            }
        }

        // Section 2: Financial Snapshot Bar (رصيد الصندوق، صافي كسب اليوم، وحق المدير)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "رصيد أمين الصندوق الفعلي بالجرد:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = CurrencyUtil.formatRiyal(actualBalance),
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = InfoBlue
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
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

                    // 3 Financial indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // الصندوق صافي اليوم
                        Column {
                            Text("دخل الصندوق اليوم", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = CurrencyUtil.formatRiyal(totalIncome),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }

                        // خرج ومصروفات اليوم
                        Column {
                            Text("مصروفات اليوم", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = CurrencyUtil.formatRiyal(totalExpenses),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }

                        // مستحق حق المدير المتبقي
                        Column(horizontalAlignment = Alignment.End) {
                            Text("مستحق المدير المتبقي", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = CurrencyUtil.formatRiyal(directorRemaining),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (directorRemaining > 0) Color(0xFFD97706) else Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Clean & Ordered Action Buttons Grid (أزرار مرتبة ومنظمة وأنيقة - ليست فوضى)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "القوائم والخدمات السريعة",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                // Row 1: كشف الاستمارات اليومية & حساب حق المدير
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeMenuTile(
                        title = "كشف الاستمارات",
                        subtitle = "عرض جميع الأيام والأسماء",
                        iconEmoji = "📋",
                        badgeText = "${todayTransactions.size} اليوم",
                        onClick = { viewModel.navigateTo(AppScreen.DAILY_SHEET) },
                        modifier = Modifier.weight(1f),
                        testTag = "nav_daily_sheet_button"
                    )

                    HomeMenuTile(
                        title = "حق الإدارة (المدير)",
                        subtitle = "محاسبة وصرف الدفعات",
                        iconEmoji = "🤝",
                        badgeText = CurrencyUtil.formatRiyal(directorRemaining),
                        onClick = { viewModel.navigateTo(AppScreen.DIRECTOR_SHARE) },
                        modifier = Modifier.weight(1f),
                        testTag = "nav_director_share_button"
                    )
                }

                // Row 2: تسجيل خرج ومصروف & حركة الصندوق واليومية
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HomeMenuTile(
                        title = "تسجيل خرج ومصروف",
                        subtitle = "خصم نفقات من الصندوق",
                        iconEmoji = "💸",
                        badgeText = null,
                        onClick = { viewModel.showAddExpense(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_expense_home"
                    )

                    HomeMenuTile(
                        title = "حركة الصندوق واليومية",
                        subtitle = "الجرد وإغلاق اليومية",
                        iconEmoji = "🏦",
                        badgeText = when {
                            diff == 0.0 -> "مطابق ✓"
                            diff > 0 -> "+${CurrencyUtil.formatRiyal(diff)}"
                            else -> "عجز"
                        },
                        onClick = { viewModel.navigateTo(AppScreen.CASH_FUND) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_cash_fund_home"
                    )
                }
            }
        }

        // Section 4: Daily Counts Breakdown (إحصائية الاستمارات)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "إحصائيات استمارات اليوم",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyPrimary
                        )

                        Text(
                            text = "الإجمالي: ${todayTransactions.size} استمارة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF475569)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryMiniPill(label = "جديد", count = todayTransactions.count { it.transactionType == "جديد" })
                        SummaryMiniPill(label = "تجديد", count = todayTransactions.count { it.transactionType == "تجديد" })
                        SummaryMiniPill(label = "بدل فاقد", count = todayTransactions.count { it.transactionType == "بدل فاقد" })
                        SummaryMiniPill(label = "بدل تالف", count = todayTransactions.count { it.transactionType == "بدل تالف" })
                    }
                }
            }
        }

        // Section 5: Recent Transactions Today
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر الاستمارات المسجلة اليوم:",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                Text(
                    text = "عرض الكل",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = InfoBlue,
                    modifier = Modifier
                        .clickable { viewModel.navigateTo(AppScreen.DAILY_SHEET) }
                        .padding(4.dp)
                )
            }
        }

        if (todayTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📝", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "لم يتم بيع استمارات اليوم بعد.",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = "اضغط على زر (بيع استمارة جديدة) بالأعلى للبدء.",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(todayTransactions.take(5), key = { it.id }) { tx ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            PrintAndExportUtil.printTransactionReceipt(
                                context = context,
                                tx = tx,
                                directorateName = "مصلحة الأحوال المدنية والسجل المدني"
                            )
                        }
                        .testTag("home_tx_item_${tx.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NavyPrimary.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tx.gender == "ذكر") "👨" else "👩",
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = tx.citizenName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "استمارة: ${tx.formNumber.ifBlank { "-" }} | قيد: ${tx.recordNumber.ifBlank { "-" }} • ${tx.transactionType}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${CurrencyUtil.formatRiyal(tx.fundShare)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            Text(
                                text = "صافي الصندوق",
                                fontSize = 9.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeMenuTile(
    title: String,
    subtitle: String,
    iconEmoji: String,
    badgeText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Card(
        modifier = modifier
            .height(116.dp)
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconEmoji, fontSize = 20.sp)
                }

                if (badgeText != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyPrimary.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SummaryMiniPill(label: String, count: Int) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$label: ", fontSize = 11.sp, color = Color(0xFF64748B))
            Text(
                text = "$count",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        }
    }
}
