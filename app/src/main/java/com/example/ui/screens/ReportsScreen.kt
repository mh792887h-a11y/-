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
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.data.entity.DailyClosingEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.components.TransactionCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.PrintAndExportUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    var selectedPreset by remember { mutableStateOf("اليوم") } // "اليوم", "الشهر", "الكل"

    val todayDate = viewModel.todayDate
    val allTxs by viewModel.allTransactions.collectAsState()
    val allExpenses by viewModel.expenses.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()
    val context = LocalContext.current

    // Filter transactions based on selectedPreset
    val filteredTxs = remember(allTxs, selectedPreset) {
        when (selectedPreset) {
            "اليوم" -> allTxs.filter { it.gregorianDate == todayDate }
            "الشهر" -> {
                val currentMonthPrefix = todayDate.substringBeforeLast("-") // YYYY-MM
                allTxs.filter { it.gregorianDate.startsWith(currentMonthPrefix) }
            }
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
    val filteredExpenses = remember(allExpenses, selectedPreset) {
        when (selectedPreset) {
            "اليوم" -> allExpenses.filter { it.gregorianDate == todayDate && it.status == "ACTIVE" }
            "الشهر" -> {
                val currentMonthPrefix = todayDate.substringBeforeLast("-")
                allExpenses.filter { it.gregorianDate.startsWith(currentMonthPrefix) && it.status == "ACTIVE" }
            }
            else -> allExpenses.filter { it.status == "ACTIVE" }
        }
    }
    val totalExpensesAmt = filteredExpenses.sumOf { it.amount }
    val netAmount = totalSales - totalExpensesAmt

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Preset selector
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "فترة التقرير:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("اليوم", "الشهر", "الكل").forEach { preset ->
                        val isSelected = selectedPreset == preset
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) NavyPrimary else Color(0xFFE2E8F0),
                            modifier = Modifier.clickable { selectedPreset = preset }
                        ) {
                            Text(
                                text = preset,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF1E293B)
                            )
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
                        val mockClosing = DailyClosingEntity(
                            gregorianDate = todayDate,
                            hijriDate = "تقرير $selectedPreset",
                            openingBalance = 0.0,
                            totalIncome = totalSales,
                            totalExpenses = totalExpensesAmt,
                            netToday = netAmount,
                            expectedBalance = netAmount,
                            actualBalance = netAmount,
                            totalTransactions = activeTxs.size,
                            malesCount = malesCount,
                            femalesCount = femalesCount,
                            totalStateShare = totalState,
                            totalDirectorateShare = totalDirectorate,
                            totalFundShare = totalFund
                        )
                        PrintAndExportUtil.printDailyReport(context, mockClosing, activeTxs, directorateName)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                ) {
                    Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("طباعة تقرير", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val summaryText = """
                            الجمهورية اليمنية - $directorateName
                            تقرير مبيعات استمارات الأحوال المدنية ($selectedPreset)
                            ------------------------------------
                            إجمالي المبيعات: ${CurrencyUtil.formatRiyal(totalSales)}
                            عدد المعاملات: ${activeTxs.size} (ذكور: $malesCount، إناث: $femalesCount)
                            حق الدولة: ${CurrencyUtil.formatRiyal(totalState)}
                            حصة الإدارة: ${CurrencyUtil.formatRiyal(totalDirectorate)}
                            نصيب الصندوق: ${CurrencyUtil.formatRiyal(totalFund)}
                            إجمالي المنصرفات: ${CurrencyUtil.formatRiyal(totalExpensesAmt)}
                            صافي الصندوق: ${CurrencyUtil.formatRiyal(netAmount)}
                            ------------------------------------
                            برمجة وتصميم محمد هشام الصلاحي
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
            items(filteredTxs.take(30), key = { it.id }) { tx ->
                TransactionCard(tx = tx, onClick = { viewModel.selectTxForDetail(tx) })
            }
        }
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
