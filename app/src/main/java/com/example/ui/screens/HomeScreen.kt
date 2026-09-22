package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.AppScreen
import com.example.ui.CivilFundViewModel
import com.example.ui.dialogs.AppDatePickerDialog
import com.example.ui.dialogs.DebtsListDialog
import com.example.ui.dialogs.UpdateOpeningBalanceDialog
import com.example.util.CurrencyUtil
import com.example.util.HijriDateUtil
import com.example.util.PrintAndExportUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: CivilFundViewModel,
    closing: DailyClosingEntity?,
    todayTransactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allTransactions by viewModel.allTransactions.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()
    val allDebts by viewModel.debts.collectAsState()
    val totalDebts by viewModel.totalRemainingDebts.collectAsState()

    val activeDate by viewModel.dashboardDate.collectAsState()
    val dashboardClosing by viewModel.dashboardClosing.collectAsState()
    val dashboardTransactions by viewModel.dashboardTransactions.collectAsState()
    val isToday = activeDate == HijriDateUtil.getTodayGregorianString()

    val effectiveClosing = dashboardClosing ?: closing
    val effectiveTransactions = dashboardTransactions

    var showUpdateOpeningBalanceDialog by remember { mutableStateOf(false) }
    var showDebtsListDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val openingBalance = effectiveClosing?.openingBalance ?: 0.0
    val totalIncome = effectiveClosing?.totalIncome ?: effectiveTransactions.sumOf { it.fundShare }
    val totalExpenses = effectiveClosing?.totalExpenses ?: 0.0
    val netToday = effectiveClosing?.netToday ?: (totalIncome - totalExpenses)
    val expectedBalance = effectiveClosing?.expectedBalance ?: (openingBalance + totalIncome - totalExpenses)
    val actualBalance = effectiveClosing?.actualBalance ?: expectedBalance
    val diff = effectiveClosing?.difference ?: (actualBalance - expectedBalance)

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                viewModel.refreshData()
                delay(350)
                isRefreshing = false
            }
        },
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // Section 0: Day Navigation & Date Selector Bar (تبديل الأيام واختيار التقويم)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_date_switcher"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isToday) Color(0xFF0F2B48) else Color(0xFF1E3A8A)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Previous Day (RTL: Arrow right moves to previous day)
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { viewModel.shiftDashboardDate(-1) }
                                .testTag("btn_prev_day")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "اليوم السابق",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Date Center Capsule (click to open calendar)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { showDatePicker = true }
                                .testTag("btn_open_calendar")
                                .padding(horizontal = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "اختيار من التقويم",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = HijriDateUtil.getFormattedArabicWithDayOfWeek(activeDate),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "الموافق هجرياً: ${HijriDateUtil.getHijriDateFromString(activeDate).formatted}",
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD)
                            )
                        }

                        // Next Day (RTL: Arrow left moves to next day)
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.18f),
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { viewModel.shiftDashboardDate(1) }
                                .testTag("btn_next_day")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "اليوم التالي",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    if (!isToday) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF59E0B)
                            ) {
                                Text(
                                    text = "عرض أرشيف يوم سابق",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .clickable { viewModel.resetDashboardDateToToday() }
                                    .testTag("btn_reset_to_today")
                            ) {
                                Text(
                                    text = "العودة لليوم الحالي ↩",
                                    color = Color(0xFF0F2B48),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Hero Action Card - بيع استمارة
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showSellForm(true) }
                    .testTag("hero_sell_form_button"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F52BA)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = "بيع استمارة",
                                    tint = Color(0xFF0F52BA),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "بيع استمارة",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "تسجيل استمارة جديدة أو تجديد أو بدل فاقد أو تالف",
                                color = Color.White.copy(alpha = 0.88f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "دخول",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Section 2: Financial Metrics Grid with Debt next to Expected Balance
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: رصيد بداية اليوم | إجمالي الخرج | إجمالي الدخل
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardMetricCard(
                        title = "رصيد بداية اليوم 💼",
                        amount = openingBalance,
                        icon = Icons.Default.AccountBalanceWallet,
                        iconTint = Color(0xFF0284C7),
                        iconBg = Color(0xFFE0F2FE),
                        valueColor = Color(0xFF0369A1),
                        modifier = Modifier.weight(1f),
                        onClick = { showUpdateOpeningBalanceDialog = true }
                    )

                    DashboardMetricCard(
                        title = "إجمالي الخرج",
                        amount = totalExpenses,
                        icon = Icons.Default.Payments,
                        iconTint = Color(0xFFDC2626),
                        iconBg = Color(0xFFFEE2E2),
                        valueColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.INCOME_EXPENSE_DEBT) }
                    )

                    DashboardMetricCard(
                        title = "إجمالي الدخل",
                        amount = totalIncome,
                        icon = Icons.Default.MonetizationOn,
                        iconTint = Color(0xFF16A34A),
                        iconBg = Color(0xFFDCFCE7),
                        valueColor = Color(0xFF16A34A),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.INCOME_EXPENSE_DEBT) }
                    )
                }

                // Row 2: الرصيد الفعلي | الرصيد المتوقع | الدين (الآجل)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardMetricCard(
                        title = "الرصيد الفعلي",
                        amount = actualBalance,
                        icon = Icons.Default.ReceiptLong,
                        iconTint = Color(0xFFD97706),
                        iconBg = Color(0xFFFEF3C7),
                        valueColor = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.showActualCashCount(true) }
                    )

                    DashboardMetricCard(
                        title = "الرصيد المتوقع",
                        amount = expectedBalance,
                        icon = Icons.Default.Adjust,
                        iconTint = Color(0xFF7C3AED),
                        iconBg = Color(0xFFF3E8FF),
                        valueColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )

                    DashboardMetricCard(
                        title = "الدين (الآجل) 📋",
                        amount = totalDebts,
                        icon = Icons.Default.Payments,
                        iconTint = Color(0xFFDC2626),
                        iconBg = Color(0xFFFEE2E2),
                        valueColor = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f),
                        onClick = { showDebtsListDialog = true }
                    )
                }

                // Row 3: صافي حركة اليومية
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.navigateTo(AppScreen.OPERATIONS) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                    border = BorderStroke(1.dp, Color(0xFFCCFBF1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "صافي حركة اليومية (الدخل - الخرج):",
                                fontSize = 12.sp,
                                color = Color(0xFF0F766E),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(
                            text = CurrencyUtil.formatRiyal(netToday),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                    }
                }
            }
        }

        // Section 3: Deficit / Surplus Status Banner
        item {
            val isDeficit = diff < 0.0
            val isSurplus = diff > 0.0

            val bannerBg = when {
                isDeficit -> Color(0xFFFFF1F2)
                else -> Color(0xFFF0FDF4)
            }
            val bannerBorder = when {
                isDeficit -> Color(0xFFFECDD3)
                else -> Color(0xFFBBF7D0)
            }
            val bannerIcon = when {
                isDeficit -> Icons.Default.Warning
                else -> Icons.Default.CheckCircle
            }
            val bannerIconTint = when {
                isDeficit -> Color(0xFFE11D48)
                else -> Color(0xFF16A34A)
            }
            val bannerTitle = when {
                isDeficit -> "العجز"
                isSurplus -> "الفائض"
                else -> "مطابق"
            }
            val bannerDesc = when {
                isDeficit -> "يوجد عجز نقدي بين المتوقع والفعلي"
                isSurplus -> "يوجد زيادة في الرصيد الفعلي عن المتوقع"
                else -> "الرصيد الفعلي مطابق تماماً للرصيد المتوقع"
            }
            val bannerValueColor = when {
                isDeficit -> Color(0xFFE11D48)
                else -> Color(0xFF16A34A)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.showActualCashCount(true) }
                    .testTag("balance_status_banner"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = bannerBg),
                border = BorderStroke(1.dp, bannerBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = bannerIcon,
                            contentDescription = null,
                            tint = bannerIconTint,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = bannerTitle,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = bannerValueColor
                            )
                            Text(
                                text = "${CurrencyUtil.formatNumber(abs(diff))} ريال",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = bannerValueColor
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(bannerBorder)
                    )

                    Text(
                        text = bannerDesc,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF475569),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Section 4: معاملات اليوم (Today's Transactions Summary)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                            contentDescription = null,
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isToday) "معاملات اليوم" else "معاملات (${HijriDateUtil.formatArabicDate(activeDate)})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = Color(0xFFF1F5F9)
                    )

                    // 5 Type boxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val newCount = effectiveTransactions.count { it.transactionType == "جديد" }
                        val renewCount = effectiveTransactions.count { it.transactionType == "تجديد" }
                        val lostCount = effectiveTransactions.count { it.transactionType == "بدل فاقد" }
                        val damagedCount = effectiveTransactions.count { it.transactionType == "بدل تالف" }
                        val totalFormsCount = effectiveTransactions.size

                        OperationTypeBox(
                            label = "جديد",
                            count = newCount,
                            icon = Icons.Default.Badge,
                            tint = Color(0xFF2563EB),
                            bg = Color(0xFFEFF6FF),
                            border = Color(0xFFBFDBFE),
                            modifier = Modifier.weight(1f)
                        )
                        OperationTypeBox(
                            label = "تجديد",
                            count = renewCount,
                            icon = Icons.Default.Autorenew,
                            tint = Color(0xFF16A34A),
                            bg = Color(0xFFF0FDF4),
                            border = Color(0xFFBBF7D0),
                            modifier = Modifier.weight(1f)
                        )
                        OperationTypeBox(
                            label = "فاقد",
                            count = lostCount,
                            icon = Icons.Default.Cancel,
                            tint = Color(0xFFDC2626),
                            bg = Color(0xFFFEF2F2),
                            border = Color(0xFFFECDD3),
                            modifier = Modifier.weight(1f)
                        )
                        OperationTypeBox(
                            label = "تالف",
                            count = damagedCount,
                            icon = Icons.Default.Warning,
                            tint = Color(0xFFD97706),
                            bg = Color(0xFFFFFBEB),
                            border = Color(0xFFFDE68A),
                            modifier = Modifier.weight(1f)
                        )
                        OperationTypeBox(
                            label = "الإجمالي",
                            count = totalFormsCount,
                            icon = Icons.Default.Groups,
                            tint = Color(0xFF475569),
                            bg = Color(0xFFF8FAFC),
                            border = Color(0xFFE2E8F0),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gender distribution bar
                    val maleCount = effectiveTransactions.count { it.gender == "ذكر" }
                    val femaleCount = effectiveTransactions.count { it.gender == "أنثى" }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الذكور: $maleCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFFDB2777),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الإناث: $femaleCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF334155)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "الإجمالي: ${effectiveTransactions.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }
                    }
                }
            }
        }

        // Section 5: 8 Quick Action Buttons (2 Rows x 4 Columns)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: الصندوق | اضافة دين | اضافة خرج | اضافة دخل
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardActionButton(
                        title = "الصندوق",
                        icon = Icons.Default.AccountBalanceWallet,
                        backgroundColor = Color(0xFF2563EB),
                        onClick = { viewModel.navigateTo(AppScreen.CASH_FUND) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_cash_fund_home"
                    )
                    DashboardActionButton(
                        title = "اضافة دين",
                        icon = Icons.Default.ReceiptLong,
                        backgroundColor = Color(0xFF7C3AED),
                        onClick = { viewModel.showAddDebt(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_debt_home"
                    )
                    DashboardActionButton(
                        title = "اضافة خرج",
                        icon = Icons.Default.Payments,
                        backgroundColor = Color(0xFFDC2626),
                        onClick = { viewModel.showAddExpense(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_expense_home"
                    )
                    DashboardActionButton(
                        title = "اضافة دخل",
                        icon = Icons.Default.AddCard,
                        backgroundColor = Color(0xFF16A34A),
                        onClick = { viewModel.showAddIncome(true) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_add_income_home"
                    )
                }

                // Row 2: الإعدادات | المواطنون | اليومية | التقارير
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DashboardActionButton(
                        title = "الإعدادات",
                        icon = Icons.Default.Settings,
                        backgroundColor = Color(0xFF475569),
                        onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_settings_home"
                    )
                    DashboardActionButton(
                        title = "المواطنون",
                        icon = Icons.Default.Groups,
                        backgroundColor = Color(0xFF1E40AF),
                        onClick = { viewModel.navigateTo(AppScreen.OPERATIONS) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_citizens_home"
                    )
                    DashboardActionButton(
                        title = "اليومية",
                        icon = Icons.Default.CalendarMonth,
                        backgroundColor = Color(0xFF0891B2),
                        onClick = { viewModel.navigateTo(AppScreen.DAILY_SHEET) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_daily_sheet_home"
                    )
                    DashboardActionButton(
                        title = "التقارير",
                        icon = Icons.Default.BarChart,
                        backgroundColor = Color(0xFF0284C7),
                        onClick = { viewModel.navigateTo(AppScreen.REPORTS) },
                        modifier = Modifier.weight(1f),
                        testTag = "action_reports_home"
                    )
                }
            }
        }

        // Section 6: آخر العمليات (Recent Transactions)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = Color(0xFF0F172A),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isToday) "استمارات اليوم" else "استمارات (${HijriDateUtil.formatArabicDate(activeDate)})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier
                                .clickable { viewModel.navigateTo(AppScreen.OPERATIONS) }
                                .testTag("view_all_transactions_btn")
                        ) {
                            Text(
                                text = "عرض الكل",
                                color = Color(0xFF0284C7),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Table Header
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("الاسم", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), modifier = Modifier.weight(2.4f))
                            Text("النوع", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), modifier = Modifier.weight(1.3f))
                            Text("المبلغ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), modifier = Modifier.weight(1.8f))
                            Text("التاريخ", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), modifier = Modifier.weight(2.2f))
                            Text("الحالة", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF64748B), modifier = Modifier.weight(1.3f))
                        }
                    }

                    val displayList = if (effectiveTransactions.isNotEmpty()) effectiveTransactions.take(10) else allTransactions.take(5)

                    if (displayList.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "📝", fontSize = 28.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "لا توجد عمليات مسجلة لهذا التاريخ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "اضغط على زر (بيع استمارة) بالأعلى لتسجيل استمارة جديدة",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else {
                        displayList.forEachIndexed { index, tx ->
                            if (index > 0) {
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectTxForDetail(tx)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Name with subtle chevron
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(2.4f)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = null,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = tx.citizenName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                // Type Pill
                                val (typeBg, typeColor) = when (tx.transactionType) {
                                    "جديد" -> Color(0xFFEFF6FF) to Color(0xFF1D4ED8)
                                    "تجديد" -> Color(0xFFF0FDF4) to Color(0xFF15803D)
                                    "بدل فاقد" -> Color(0xFFFEF2F2) to Color(0xFFB91C1C)
                                    "بدل تالف" -> Color(0xFFFFFBEB) to Color(0xFFB45309)
                                    else -> Color(0xFFF1F5F9) to Color(0xFF475569)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(typeBg)
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = tx.transactionType,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = typeColor,
                                        maxLines = 1
                                    )
                                }

                                // Amount
                                Text(
                                    text = CurrencyUtil.formatRiyal(tx.salePrice),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.weight(1.8f),
                                    maxLines = 1
                                )

                                // Date
                                Text(
                                    text = tx.gregorianDate,
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.weight(2.2f),
                                    maxLines = 1
                                )

                                // Status
                                val isCancelled = tx.status == "CANCELLED"
                                Box(
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isCancelled) Color(0xFFFEE2E2) else Color(0xFFDCFCE7))
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isCancelled) "ملغية" else "مكتملة",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCancelled) Color(0xFFDC2626) else Color(0xFF15803D),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (showUpdateOpeningBalanceDialog) {
        UpdateOpeningBalanceDialog(
            currentOpeningBalance = openingBalance,
            onDismiss = { showUpdateOpeningBalanceDialog = false },
            onSubmit = { newBalance, reason ->
                showUpdateOpeningBalanceDialog = false
                viewModel.updateOpeningBalance(newBalance, reason)
            }
        )
    }

    if (showDebtsListDialog) {
        DebtsListDialog(
            debts = allDebts,
            totalDebts = totalDebts,
            directorateName = directorateName,
            onDismiss = { showDebtsListDialog = false },
            onPayDebt = { debt ->
                showDebtsListDialog = false
                viewModel.selectDebtForPayment(debt)
            },
            onDeleteDebt = { debtId ->
                viewModel.deleteDebt(debtId)
            },
            onAddNewDebt = {
                showDebtsListDialog = false
                viewModel.showAddDebt(true)
            }
        )
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateString = activeDate,
            onDateSelected = { pickedDate ->
                viewModel.setDashboardDate(pickedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Metric card displaying financial values in the 3x2 grid.
 */
@Composable
fun DashboardMetricCard(
    title: String,
    amount: Double,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
    unit: String = "ريال",
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = CurrencyUtil.formatNumber(amount),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }
    }
}

/**
 * Mini summary box for transactions category breakdown.
 */
@Composable
fun OperationTypeBox(
    label: String,
    count: Int,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    border: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF475569),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count.toString(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }
    }
}

/**
 * Reusable solid action button in the 2x4 grid.
 */
@Composable
fun DashboardActionButton(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .height(68.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}
