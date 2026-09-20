package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.PrintAndExportUtil

@Composable
fun DailySheetScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedReportDate.collectAsState()
    val allDates by viewModel.allTransactionDates.collectAsState()
    val transactions by viewModel.selectedDateTransactions.collectAsState()
    val closing by viewModel.selectedDateClosing.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }

    val filteredTransactions = remember(transactions, searchQuery, selectedTypeFilter) {
        transactions.filter { tx ->
            val matchesQuery = searchQuery.isBlank() ||
                    tx.citizenName.contains(searchQuery.trim(), ignoreCase = true) ||
                    tx.formNumber.contains(searchQuery.trim(), ignoreCase = true) ||
                    tx.recordNumber.contains(searchQuery.trim(), ignoreCase = true) ||
                    tx.receiptNumber.contains(searchQuery.trim(), ignoreCase = true)

            val matchesType = selectedTypeFilter == "الكل" || tx.transactionType == selectedTypeFilter
            matchesQuery && matchesType
        }
    }

    // Calculations for the selected date
    val totalTransactionsCount = transactions.size
    val totalFundShare = transactions.filter { it.status == "ACTIVE" }.sumOf { it.fundShare }
    val totalDirectorateShare = transactions.filter { it.status == "ACTIVE" }.sumOf { it.directorateShare }
    val totalSalesAmount = transactions.filter { it.status == "ACTIVE" }.sumOf { it.salePrice }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("daily_sheet_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 1: Header and Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "كشف الاستمارات اليومية",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    Text(
                        text = "ورقة الكشف الرسمية اليومية لجميع المواطنين",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Print Button
                    Button(
                        onClick = {
                            val dummyClosing = closing ?: DailyClosingEntity(
                                gregorianDate = selectedDate,
                                hijriDate = transactions.firstOrNull()?.hijriDate ?: "",
                                totalIncome = totalFundShare,
                                totalTransactions = totalTransactionsCount,
                                totalFundShare = totalFundShare,
                                totalDirectorateShare = totalDirectorateShare
                            )
                            PrintAndExportUtil.printDailyReport(
                                context = context,
                                closing = dummyClosing,
                                transactions = transactions,
                                directorateName = directorateName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("print_daily_sheet_button")
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة A4", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Button
                    OutlinedButton(
                        onClick = {
                            val textBuilder = StringBuilder()
                            textBuilder.append("📋 كشف استمارات الأحوال المدنية - $selectedDate\n")
                            textBuilder.append("🏛️ $directorateName\n")
                            textBuilder.append("═══════════════════════\n")
                            textBuilder.append("إجمالي الاستمارات: $totalTransactionsCount\n")
                            textBuilder.append("صافي دخل الصندوق: ${CurrencyUtil.formatRiyal(totalFundShare)}\n")
                            textBuilder.append("حق الإدارة (المدير): ${CurrencyUtil.formatRiyal(totalDirectorateShare)}\n")
                            textBuilder.append("═══════════════════════\n")
                            transactions.forEachIndexed { i, tx ->
                                textBuilder.append("${i + 1}) المواطن: ${tx.citizenName}\n")
                                textBuilder.append("   • استمارة: ${tx.formNumber.ifBlank { "-" }} | قيد: ${tx.recordNumber.ifBlank { "-" }}\n")
                                textBuilder.append("   • النوع: ${tx.transactionType} | صافي الصندوق: ${CurrencyUtil.formatRiyal(tx.fundShare)}\n")
                            }
                            PrintAndExportUtil.shareTextReport(context, "كشف استمارات $selectedDate", textBuilder.toString())
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("share_daily_sheet_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Section 2: Days Selector List (قائمة تعرض جميع الأيام)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = NavyPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "عرض الأيام السابقة (اختر اليوم لعرض كشفه):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }

                    val dateList = if (allDates.contains(viewModel.todayDate)) allDates else listOf(viewModel.todayDate) + allDates
                    val uniqueDates = dateList.distinct().sortedDescending()

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uniqueDates) { dateStr ->
                            val isSelected = dateStr == selectedDate
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NavyPrimary else Color.White,
                                border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .clickable { viewModel.setSelectedReportDate(dateStr) }
                                    .testTag("date_chip_$dateStr")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (dateStr == viewModel.todayDate) "اليوم ($dateStr)" else dateStr,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Summary of the Selected Day
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كشف يوم: $selectedDate",
                            color = Color(0xFFFFD54F),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "$totalTransactionsCount استمارة مسجلة",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("صافي كسب الصندوق (550)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(CurrencyUtil.formatRiyal(totalFundShare), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF86EFAC))
                        }

                        Column {
                            Text("حق الإدارة (المدير 200)", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(CurrencyUtil.formatRiyal(totalDirectorateShare), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                        }

                        Column {
                            Text("إجمالي المبيعات", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                            Text(CurrencyUtil.formatRiyal(totalSalesAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Section 4: Search & Form Type Filter
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث بالاسم، رقم الاستمارة، رقم القيد...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NavyPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("daily_sheet_search_input")
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Filter Chips (جديد، تجديد، فاقد، تالف)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("الكل", "جديد", "تجديد", "بدل فاقد", "بدل تالف").forEach { type ->
                    val isSelected = selectedTypeFilter == type
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) NavyPrimary else Color(0xFFF1F5F9),
                        modifier = Modifier.clickable { selectedTypeFilter = type }
                    ) {
                        Text(
                            text = type,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Section 5: The Official Sheet Cards (List of Citizen Transactions)
        if (filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📑", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (transactions.isEmpty()) "لا توجد استمارات مسجلة في هذا اليوم ($selectedDate)" else "لا توجد نتائج تطابق البحث",
                            fontSize = 14.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            item {
                Text(
                    text = "جدول الاستمارات (${filteredTransactions.size} استمارة):",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
            }

            items(filteredTransactions, key = { it.id }) { tx ->
                DailySheetRecordCard(tx = tx)
            }
        }
    }
}

@Composable
fun DailySheetRecordCard(tx: TransactionEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("sheet_card_${tx.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Form Number, Record Number, Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Form Number Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NavyPrimary.copy(alpha = 0.1f)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(text = "استمارة: ", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(
                                text = tx.formNumber.ifBlank { "-" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }

                    // Record Number Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = InfoBlue.copy(alpha = 0.1f)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text(text = "قيد: ", fontSize = 11.sp, color = Color(0xFF475569))
                            Text(
                                text = tx.recordNumber.ifBlank { "-" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = InfoBlue
                            )
                        }
                    }
                }

                // Time String
                Text(
                    text = tx.timeString,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Citizen Name & Gender
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tx.citizenName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Transaction Type Pill
                val (pillBg, pillFg) = when (tx.transactionType) {
                    "جديد" -> Color(0xFFDCFCE7) to IncomeGreen
                    "تجديد" -> Color(0xFFE0F2FE) to InfoBlue
                    "بدل فاقد" -> Color(0xFFFEF3C7) to Color(0xFFD97706)
                    else -> Color(0xFFF1F5F9) to Color(0xFF475569)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = pillBg
                ) {
                    Text(
                        text = tx.transactionType,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = pillFg,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

            // Shares breakdown row: Fund Share (550), Director Share (200), Total Sale
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("صافي الصندوق", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = CurrencyUtil.formatRiyal(tx.fundShare),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }

                Column {
                    Text("حق الإدارة", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = CurrencyUtil.formatRiyal(tx.directorateShare),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("الإجمالي المسدد", fontSize = 10.sp, color = Color(0xFF64748B))
                    Text(
                        text = CurrencyUtil.formatRiyal(tx.salePrice),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}
