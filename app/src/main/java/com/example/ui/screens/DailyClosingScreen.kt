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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedContainer
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenContainer
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtil
import com.example.util.HijriDateUtil
import com.example.util.PrintAndExportUtil

@Composable
fun DailyClosingScreen(
    viewModel: CivilFundViewModel,
    closing: DailyClosingEntity?,
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()
    val context = LocalContext.current

    val isClosed = closing?.status == "CLOSED"
    val opening = closing?.openingBalance ?: 0.0
    val totalIncome = closing?.totalIncome ?: 0.0
    val totalExpenses = closing?.totalExpenses ?: 0.0
    val netToday = closing?.netToday ?: 0.0
    val expected = closing?.expectedBalance ?: 0.0
    val actual = closing?.actualBalance ?: expected
    val diff = closing?.difference ?: (actual - expected)

    // Detailed counts
    val activeTxs = transactions.filter { it.status == "ACTIVE" }
    val newMales = activeTxs.count { it.transactionType == "جديد" && it.gender == "ذكر" }
    val newFemales = activeTxs.count { it.transactionType == "جديد" && it.gender == "أنثى" }

    val renewMales = activeTxs.count { it.transactionType == "تجديد" && it.gender == "ذكر" }
    val renewFemales = activeTxs.count { it.transactionType == "تجديد" && it.gender == "أنثى" }

    val lostMales = activeTxs.count { it.transactionType == "بدل فاقد" && it.gender == "ذكر" }
    val lostFemales = activeTxs.count { it.transactionType == "بدل فاقد" && it.gender == "أنثى" }

    val damagedMales = activeTxs.count { it.transactionType == "بدل تالف" && it.gender == "ذكر" }
    val damagedFemales = activeTxs.count { it.transactionType == "بدل تالف" && it.gender == "أنثى" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_closing_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isClosed) Color(0xFFFEF3C7) else Color(0xFFE0F2FE)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isClosed) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isClosed) WarningAmber else InfoBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isClosed) "اليومية مغلقة" else "اليومية مفتوحة وجارية",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isClosed) WarningAmber else InfoBlue
                            )
                            Text(
                                text = if (isClosed) "أُغلقت بواسطة: ${closing?.closedByName ?: "المسؤول"}" else "يمكن إضافة وتعديل العمليات",
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                    }

                    // Print daily report button
                    IconButton(
                        onClick = {
                            if (closing != null) {
                                PrintAndExportUtil.printDailyReport(context, closing, activeTxs, directorateName)
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = "طباعة اليومية", tint = NavyPrimary)
                    }
                }
            }
        }

        // Financial summary table card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "الخلاصة المالية لليومية",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    DailyMetricRow("رصيد بداية اليوم (الافتتاحي):", CurrencyUtil.formatRiyal(opening))
                    DailyMetricRow("إجمالي الدخل اليوم:", CurrencyUtil.formatRiyal(totalIncome), valueColor = IncomeGreen, isBold = true)
                    DailyMetricRow("إجمالي الخرج اليوم:", CurrencyUtil.formatRiyal(totalExpenses), valueColor = ExpenseRed, isBold = true)
                    DailyMetricRow("صافي حركة اليوم:", CurrencyUtil.formatRiyal(netToday), valueColor = if (netToday >= 0) IncomeGreen else ExpenseRed, isBold = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
                    DailyMetricRow("الرصيد الدفتري المتوقع:", CurrencyUtil.formatRiyal(expected), isBold = true)
                    DailyMetricRow("الرصيد الفعلي بالجرد:", CurrencyUtil.formatRiyal(actual), valueColor = InfoBlue, isBold = true)
                    DailyMetricRow(
                        "حالة الصندوق (الفرق):",
                        when {
                            diff == 0.0 -> "مطابق تماماً ✓"
                            diff > 0 -> "زيادة (+${CurrencyUtil.formatRiyal(diff)})"
                            else -> "عجز (${CurrencyUtil.formatRiyal(diff)})"
                        },
                        valueColor = when {
                            diff == 0.0 -> IncomeGreen
                            diff > 0 -> InfoBlue
                            else -> ExpenseRed
                        },
                        isBold = true
                    )
                }
            }
        }

        // Transactions breakdown table by gender & type
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "جدول تفصيل المعاملات حسب النوع والجنس",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("النوع", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("ذكور", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Text("إناث", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                        Text("الإجمالي", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }

                    BreakdownRow("جديد", newMales, newFemales)
                    BreakdownRow("تجديد", renewMales, renewFemales)
                    BreakdownRow("بدل فاقد", lostMales, lostFemales)
                    BreakdownRow("بدل تالف", damagedMales, damagedFemales)

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    // Total Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("إجمالي الاستمارات:", modifier = Modifier.weight(1.5f), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                        Text("${closing?.malesCount ?: 0}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Text("${closing?.femalesCount ?: 0}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                        Text("${closing?.totalTransactions ?: 0}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center, color = IncomeGreen)
                    }
                }
            }
        }

        // Shares summary card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "توزيع الحصص والأنصبة المالية",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    DailyMetricRow("حق الدولة:", CurrencyUtil.formatRiyal(closing?.totalStateShare ?: 0.0), isBold = true)
                    DailyMetricRow("حصة الإدارة:", CurrencyUtil.formatRiyal(closing?.totalDirectorateShare ?: 0.0), isBold = true)
                    DailyMetricRow("نصيب الصندوق:", CurrencyUtil.formatRiyal(closing?.totalFundShare ?: 0.0), valueColor = InfoBlue, isBold = true)
                    DailyMetricRow("دخل آخر غير الاستمارات:", CurrencyUtil.formatRiyal(closing?.otherIncome ?: 0.0))
                    DailyMetricRow("ديون مسددة اليوم:", CurrencyUtil.formatRiyal(closing?.debtPaidAmount ?: 0.0))
                }
            }
        }

        // Actions: Close Daily / Reopen Daily
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isClosed) {
                    Button(
                        onClick = { viewModel.showCloseDailyConfirm(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("close_daily_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إغلاق اليومية وتأمين الحسابات", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Closed state
                    if (currentUser?.role == "ADMIN") {
                        Button(
                            onClick = { viewModel.reopenDaily() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("reopen_daily_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                        ) {
                            Icon(imageVector = Icons.Default.LockOpen, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("إعادة فتح اليومية (صلاحية المدير)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Text(
                            text = "اليومية مغلقة. لإعادة فتحها يرجى مراجعة مدير الإدارة.",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyMetricRow(
    label: String,
    value: String,
    valueColor: Color = Color.Unspecified,
    isBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color(0xFF475569))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun BreakdownRow(type: String, males: Int, females: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(type, modifier = Modifier.weight(1.5f), fontSize = 13.sp)
        Text("$males", modifier = Modifier.weight(1f), fontSize = 13.sp, textAlign = TextAlign.Center)
        Text("$females", modifier = Modifier.weight(1f), fontSize = 13.sp, textAlign = TextAlign.Center)
        Text("${males + females}", modifier = Modifier.weight(1f), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
