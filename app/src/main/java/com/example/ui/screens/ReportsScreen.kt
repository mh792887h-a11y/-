package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.TransactionEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.components.TransactionCard
import com.example.ui.dialogs.DeleteConfirmDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.HijriDateUtil
import com.example.util.PrintAndExportUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf("اليوم") } // "اليوم", "الأسبوع", "الشهر", "السنة", "الكل", "فترة مخصصة"
    val todayDate = viewModel.todayDate

    var customStartDate by remember { mutableStateOf(todayDate) }
    var customEndDate by remember { mutableStateOf(todayDate) }

    var selectedTxForView by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val allTxs by viewModel.allTransactions.collectAsState()
    val allExpenses by viewModel.expenses.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()
    val context = LocalContext.current

    // Calculate dates for presets
    val sevenDaysAgo = remember(todayDate) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -6)
            sdf.format(cal.time)
        } catch (e: Exception) {
            todayDate
        }
    }
    val currentMonthPrefix = remember(todayDate) { todayDate.substringBeforeLast("-") }
    val currentYearPrefix = remember(todayDate) { todayDate.substringBefore("-") }

    // Filter transactions based on preset
    val filteredTxs = remember(allTxs, selectedPreset, customStartDate, customEndDate) {
        when (selectedPreset) {
            "اليوم" -> allTxs.filter { it.gregorianDate == todayDate }
            "الأسبوع" -> allTxs.filter { it.gregorianDate >= sevenDaysAgo && it.gregorianDate <= todayDate }
            "الشهر" -> allTxs.filter { it.gregorianDate.startsWith(currentMonthPrefix) }
            "السنة" -> allTxs.filter { it.gregorianDate.startsWith(currentYearPrefix) }
            "فترة مخصصة" -> allTxs.filter { it.gregorianDate in customStartDate..customEndDate }
            else -> allTxs
        }
    }

    val activeTxs = filteredTxs.filter { it.status == "ACTIVE" }
    val totalSales = activeTxs.sumOf { it.salePrice }
    val totalState = activeTxs.sumOf { it.stateShare }
    val totalDirectorate = activeTxs.sumOf { it.directorateShare }
    val totalFund = activeTxs.sumOf { it.fundShare }
    val malesCount = activeTxs.count { it.gender == "ذكر" }
    val femalesCount = activeTxs.count { it.gender == "أنثى" }

    // Filter expenses
    val filteredExpenses = remember(allExpenses, selectedPreset, customStartDate, customEndDate) {
        when (selectedPreset) {
            "اليوم" -> allExpenses.filter { it.gregorianDate == todayDate && it.status == "ACTIVE" }
            "الأسبوع" -> allExpenses.filter { it.gregorianDate >= sevenDaysAgo && it.gregorianDate <= todayDate && it.status == "ACTIVE" }
            "الشهر" -> allExpenses.filter { it.gregorianDate.startsWith(currentMonthPrefix) && it.status == "ACTIVE" }
            "السنة" -> allExpenses.filter { it.gregorianDate.startsWith(currentYearPrefix) && it.status == "ACTIVE" }
            "فترة مخصصة" -> allExpenses.filter { it.gregorianDate in customStartDate..customEndDate && it.status == "ACTIVE" }
            else -> allExpenses.filter { it.status == "ACTIVE" }
        }
    }
    val totalExpensesAmt = filteredExpenses.sumOf { it.amount }
    val netAmount = totalSales - totalExpensesAmt

    val subtitleText = when (selectedPreset) {
        "اليوم" -> "بتاريخ $todayDate م (${HijriDateUtil.getHijriDate()})"
        "الأسبوع" -> "من $sevenDaysAgo إلى $todayDate م"
        "الشهر" -> "لشهر $currentMonthPrefix م"
        "السنة" -> "لعام $currentYearPrefix م"
        "فترة مخصصة" -> "من $customStartDate إلى $customEndDate م"
        else -> "كافة العمليات المسجلة حتى $todayDate م"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Preset selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "فترة التقرير:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("اليوم", "الأسبوع", "الشهر", "السنة", "الكل", "فترة مخصصة").forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) NavyPrimary else Color(0xFFE2E8F0),
                            modifier = Modifier.clickable { selectedPreset = preset }
                        ) {
                            Text(
                                text = preset,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }

                // Custom Date Range Inputs
                if (selectedPreset == "فترة مخصصة") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "تحديد الفترة المخصصة (YYYY-MM-DD):",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customStartDate,
                                    onValueChange = { customStartDate = it },
                                    label = { Text("من تاريخ") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = customEndDate,
                                    onValueChange = { customEndDate = it },
                                    label = { Text("إلى تاريخ") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ملخص تقرير فترة: $selectedPreset",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Text(
                        text = subtitleText,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ReportMetricRow("إجمالي مبيعات الاستمارات:", CurrencyUtil.formatRiyal(totalSales), valueColor = IncomeGreen, isBold = true)
                    ReportMetricRow("عدد المعاملات المنفذة:", "${activeTxs.size} (ذكور: $malesCount، إناث: $femalesCount)")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    ReportMetricRow("حق الدولة:", CurrencyUtil.formatRiyal(totalState))
                    ReportMetricRow("حصة الإدارة:", CurrencyUtil.formatRiyal(totalDirectorate))
                    ReportMetricRow("نصيب الصندوق:", CurrencyUtil.formatRiyal(totalFund), valueColor = InfoBlue, isBold = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                    ReportMetricRow("إجمالي المنصرفات (الخرج):", CurrencyUtil.formatRiyal(totalExpensesAmt), valueColor = ExpenseRed, isBold = true)
                    ReportMetricRow("الصافي للفترة:", CurrencyUtil.formatRiyal(netAmount), valueColor = if (netAmount >= 0) IncomeGreen else ExpenseRed, isBold = true)
                }
            }
        }

        // Action Buttons: Print & Share
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        PrintAndExportUtil.printCustomPeriodReport(
                            context = context,
                            periodTitle = "كشف حساب وتقارير فترة: $selectedPreset",
                            periodSubtitle = subtitleText,
                            transactions = activeTxs,
                            expenses = filteredExpenses,
                            directorateName = directorateName
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طباعة التقرير", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val summaryText = """
                            الجمهورية اليمنية - $directorateName
                            كشف حساب ومبيعات الأحوال المدنية ($selectedPreset)
                            $subtitleText
                            ------------------------------------
                            إجمالي المبيعات: ${CurrencyUtil.formatRiyal(totalSales)}
                            عدد المعاملات: ${activeTxs.size} (ذكور: $malesCount، إناث: $femalesCount)
                            حق الدولة: ${CurrencyUtil.formatRiyal(totalState)}
                            حصة الإدارة: ${CurrencyUtil.formatRiyal(totalDirectorate)}
                            نصيب الصندوق: ${CurrencyUtil.formatRiyal(totalFund)}
                            إجمالي المنصرفات: ${CurrencyUtil.formatRiyal(totalExpensesAmt)}
                            صافي الصندوق للفترة: ${CurrencyUtil.formatRiyal(netAmount)}
                        """.trimIndent()
                        PrintAndExportUtil.shareTextReport(context, "تقرير أحوال مدنية", summaryText)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("مشاركة", fontWeight = FontWeight.Bold)
                }
            }
        }

        // List of transactions included
        item {
            Text(
                text = "قائمة العمليات المشمولة (${filteredTxs.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )
            )
        }

        if (filteredTxs.isEmpty()) {
            item {
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
                        Text("لا توجد عمليات لهذه الفترة.", color = Color(0xFF94A3B8))
                    }
                }
            }
        } else {
            items(filteredTxs.take(100), key = { it.id }) { tx ->
                TransactionCard(tx = tx, onClick = { selectedTxForView = tx })
            }
        }
    }

    // Detail Dialog for transaction clicked from reports
    if (selectedTxForView != null) {
        val tx = selectedTxForView!!
        Dialog(onDismissRequest = { selectedTxForView = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(12.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تفاصيل العملية",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        IconButton(onClick = { selectedTxForView = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            ReportDetailItem(label = "رقم الإيصال:", value = tx.receiptNumber, isBold = true)
                            ReportDetailItem(label = "اسم المواطن:", value = tx.citizenName, isBold = true)
                            ReportDetailItem(label = "نوع المعاملة:", value = "استمارة ${tx.transactionType}")
                            ReportDetailItem(label = "الجنس:", value = tx.gender)
                            ReportDetailItem(label = "المبلغ الإجمالي:", value = CurrencyUtil.formatRiyal(tx.salePrice), color = IncomeGreen, isBold = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            ReportDetailItem(label = "حق الدولة:", value = CurrencyUtil.formatRiyal(tx.stateShare))
                            ReportDetailItem(label = "حصة الإدارة:", value = CurrencyUtil.formatRiyal(tx.directorateShare))
                            ReportDetailItem(label = "نصيب الصندوق:", value = CurrencyUtil.formatRiyal(tx.fundShare), color = InfoBlue, isBold = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            ReportDetailItem(label = "التاريخ الميلادي:", value = tx.gregorianDate)
                            ReportDetailItem(label = "التاريخ الهجري:", value = tx.hijriDate)
                            ReportDetailItem(label = "الوقت:", value = tx.timeString)
                            ReportDetailItem(label = "المسجل:", value = tx.createdByName)
                            if (tx.status == "CANCELLED") {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                ReportDetailItem(label = "الحالة:", value = "ملغاة", color = ExpenseRed, isBold = true)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                PrintAndExportUtil.printTransactionReceipt(context, tx, directorateName)
                            },
                            modifier = Modifier
                                .weight(1.1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة", fontSize = 12.sp)
                        }

                        if (tx.status == "ACTIVE") {
                            Button(
                                onClick = {
                                    viewModel.promptCancelTx(tx)
                                    selectedTxForView = null
                                },
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                            ) {
                                Text("إلغاء", fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                txToDelete = tx
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حذف", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (txToDelete != null) {
        val target = txToDelete!!
        DeleteConfirmDialog(
            title = "حذف العملية نهائياً",
            message = "هل أنت متأكد من حذف عملية المواطن (${target.citizenName}) رقم الإيصال #${target.receiptNumber}؟ سيتم حذفها نهائياً وإعادة حساب كافة الإيرادات والصندوق تلقائياً.",
            onDismiss = { txToDelete = null },
            onConfirm = {
                viewModel.deleteTransaction(target.id)
                selectedTxForView = null
                txToDelete = null
            }
        )
    }
}

@Composable
private fun ReportMetricRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF475569))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

@Composable
private fun ReportDetailItem(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
    }
}
