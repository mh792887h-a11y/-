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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.entity.CashMovementEntity
import com.example.data.entity.DirectorPaymentEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.dialogs.DirectorPaymentReceiptDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.PrintAndExportUtil

data class DailyDirectorShare(
    val gregorianDate: String,
    val newCount: Int,
    val directorShare: Double
)

@Composable
fun CashFundScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: حركة الصندوق, 1: سجل حساب المدير

    val movements by viewModel.cashMovements.collectAsState()
    val latestBalance = movements.firstOrNull()?.balanceAfter ?: 0.0

    // Director Ledger states
    val directorTotalNewForms by viewModel.totalNewFormsCount.collectAsState()
    val directorTotalEarned by viewModel.totalDirectorEarned.collectAsState()
    val directorTotalPaid by viewModel.totalDirectorPaid.collectAsState()
    val directorRemainingBalance by viewModel.directorRemaining.collectAsState()
    val activeNewTransactions by viewModel.activeNewTransactions.collectAsState()
    val directorPayments by viewModel.allDirectorPayments.collectAsState()
    val directorPaymentToPrint by viewModel.directorPaymentToPrint.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()

    val directorDailyBreakdown = remember(activeNewTransactions) {
        activeNewTransactions
            .groupBy { it.gregorianDate }
            .map { (date, list) ->
                DailyDirectorShare(
                    gregorianDate = date,
                    newCount = list.size,
                    directorShare = list.size * 200.0
                )
            }
            .sortedByDescending { it.gregorianDate }
    }

    var showPartialPayDialog by remember { mutableStateOf(false) }
    var showPayAllConfirmDialog by remember { mutableStateOf(false) }
    var voucherToPrint by remember { mutableStateOf<DirectorPaymentEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
    ) {
        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = NavyPrimary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "حركة الصندوق العامة",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "سجل حساب المدير",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                        if (directorRemainingBalance > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = ExpenseRed
                            ) {
                                Text(
                                    text = CurrencyUtil.formatNumber(directorRemainingBalance),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Tab 0: General Cash Movements
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("cash_fund_screen_list"),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Balance Header Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = NavyPrimary)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "الرصيد الدفتري الحالي للصندوق",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = CurrencyUtil.formatRiyal(latestBalance),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل حركة الصندوق (الوارد والمنصرف)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "عدد الحركات: ${movements.size}",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                if (movements.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("لا توجد حركات مسجلة بالصندوق حتى الآن.", color = Color(0xFF94A3B8))
                            }
                        }
                    }
                } else {
                    items(movements, key = { it.id }) { item ->
                        CashMovementCard(item)
                    }
                }
            }
        } else {
            // Tab 1: Director Ledger Screen (سجل حساب المدير)
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("director_ledger_list"),
                contentPadding = PaddingValues(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section: Manager Summary Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "حساب مستحقات المدير (حق الإدارة)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = NavyPrimary
                                    )
                                    Text(
                                        text = "المستحق: 200 ريال عن كل استمارة نوع (جديد)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Button(
                                    onClick = {
                                        PrintAndExportUtil.printDirectorStatement(
                                            context = context,
                                            directorateName = directorateName,
                                            payments = directorPayments,
                                            totalEarned = directorTotalEarned,
                                            totalPaid = directorTotalPaid,
                                            remaining = directorRemainingBalance
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("طباعة الكشف", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Grid of Director Financials
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("عدد استمارات جديد", fontSize = 10.sp, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("$directorTotalNewForms", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("إجمالي المستحق", fontSize = 10.sp, color = Color(0xFF64748B))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(CurrencyUtil.formatNumber(directorTotalEarned), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("إجمالي المسلّم للمدير", fontSize = 10.sp, color = Color(0xFF166534))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(CurrencyUtil.formatRiyal(directorTotalPaid), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (directorRemainingBalance > 0) Color(0xFFFFF1F2) else Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, if (directorRemainingBalance > 0) Color(0xFFFECDD3) else Color(0xFFBBF7D0))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("المتبقي طرف الصندوق", fontSize = 10.sp, color = if (directorRemainingBalance > 0) Color(0xFF991B1B) else Color(0xFF166534))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(CurrencyUtil.formatRiyal(directorRemainingBalance), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (directorRemainingBalance > 0) ExpenseRed else IncomeGreen)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Action buttons: Pay All or Pay Partial
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showPayAllConfirmDialog = true },
                                    enabled = directorRemainingBalance > 0,
                                    modifier = Modifier.weight(1.3f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("سداد المبلغ كاملاً", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showPartialPayDialog = true },
                                    enabled = directorRemainingBalance > 0,
                                    modifier = Modifier.weight(1f).height(44.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary)
                                ) {
                                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("سداد دفعة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Section: Day-by-Day Breakdown
                item {
                    Text(
                        text = "📅 كشف حساب المدير بالأيام (عدد الجديد ومستحق كل يوم)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                }

                if (directorDailyBreakdown.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("لا توجد مبيعات استمارات (جديد) مسجلة حتى الآن.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(directorDailyBreakdown) { day ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE0F2FE)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(20.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = day.gregorianDate,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "${day.newCount} استمارة جديد × 200 ريال",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Text(
                                    text = CurrencyUtil.formatRiyal(day.directorShare),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                            }
                        }
                    }
                }

                // Section: Disbursed Payments Log
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🧾 سجل الدفعات والمبالغ المسلمة للمدير",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                }

                if (directorPayments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                                Text("لم يتم صرف أي مبالغ أو دفعات للمدير بعد.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                            }
                        }
                    }
                } else {
                    items(directorPayments, key = { it.id }) { payment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = CurrencyUtil.formatRiyal(payment.amount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = IncomeGreen
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF1F5F9)
                                        ) {
                                            Text(
                                                text = "#DP-${payment.id}",
                                                fontSize = 10.sp,
                                                color = Color(0xFF475569),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${payment.gregorianDate} (${payment.timeString})",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                    if (!payment.notes.isNullOrBlank()) {
                                        Text(
                                            text = payment.notes,
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { voucherToPrint = payment }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = "طباعة سند الصرف",
                                        tint = NavyPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Confirm Pay All
    if (showPayAllConfirmDialog) {
        Dialog(onDismissRequest = { showPayAllConfirmDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.92f).padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                        text = "تأكيد سداد مستحقات المدير بالكامل",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = NavyPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "هل تريد تسليم كامل المبلغ المتبقي للمدير وقدره ${CurrencyUtil.formatRiyal(directorRemainingBalance)} نقداً؟ سيتم تصفير رصيده وإصدار سند صرف.",
                        fontSize = 13.sp,
                        color = Color(0xFF475569)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPayAllConfirmDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                showPayAllConfirmDialog = false
                                viewModel.payDirector(directorRemainingBalance, "سداد كامل المستحقات المتبقية حتى تاريخه")
                            },
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                        ) {
                            Text("تأكيد الصرف", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Dialog: Pay Partial
    if (showPartialPayDialog) {
        var partialAmountText by remember { mutableStateOf("") }
        var partialNotesText by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showPartialPayDialog = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.92f).padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تسليم دفعة من مستحقات المدير",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavyPrimary
                        )
                        IconButton(onClick = { showPartialPayDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "المبلغ المتبقي له طرف الصندوق: ${CurrencyUtil.formatRiyal(directorRemainingBalance)}",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = partialAmountText,
                        onValueChange = { partialAmountText = it },
                        label = { Text("المبلغ المسلم (ريال) *") },
                        placeholder = { Text("مثال: 5000") },
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = partialNotesText,
                        onValueChange = { partialNotesText = it },
                        label = { Text("ملاحظات / البيان") },
                        placeholder = { Text("مثال: دفعة تحت الحساب") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    val entered = partialAmountText.toDoubleOrNull() ?: 0.0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPartialPayDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }

                        Button(
                            onClick = {
                                if (entered > 0) {
                                    showPartialPayDialog = false
                                    viewModel.payDirector(entered, partialNotesText.ifBlank { null })
                                }
                            },
                            enabled = entered > 0,
                            modifier = Modifier.weight(1.3f),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text("تأكيد الصرف", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Auto-show receipt right after payment
    if (directorPaymentToPrint != null) {
        DirectorPaymentReceiptDialog(
            payment = directorPaymentToPrint!!,
            directorateName = directorateName,
            onDismiss = { viewModel.dismissDirectorPaymentReceipt() }
        )
    }

    // Show voucher from payment history list
    if (voucherToPrint != null) {
        DirectorPaymentReceiptDialog(
            payment = voucherToPrint!!,
            directorateName = directorateName,
            onDismiss = { voucherToPrint = null }
        )
    }
}

@Composable
fun CashMovementCard(item: CashMovementEntity) {
    val isIn = item.amountIn > 0

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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isIn) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (isIn) IncomeGreen else ExpenseRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.statement,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.gregorianDate} • ${item.timeString}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isIn) "+${CurrencyUtil.formatRiyal(item.amountIn)}" else "-${CurrencyUtil.formatRiyal(item.amountOut)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isIn) IncomeGreen else ExpenseRed
                )
                Text(
                    text = "الرصيد: ${CurrencyUtil.formatNumber(item.balanceAfter)}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
