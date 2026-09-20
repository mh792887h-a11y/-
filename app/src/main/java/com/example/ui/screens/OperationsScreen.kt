package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.TransactionEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.PrintAndExportUtil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OperationsScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val filterGender by viewModel.filterGender.collectAsState()
    val selectedTx by viewModel.selectedTxForDetail.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()

    val totalActiveAmount = transactions.filter { it.status == "ACTIVE" }.sumOf { it.salePrice }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("operations_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("بحث باسم المواطن، رقم الإيصال، التاريخ...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = NavyPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("operations_search_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Filter Chips (Type & Gender)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Type chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("الكل", "جديد", "تجديد", "بدل فاقد", "بدل تالف").forEach { type ->
                        val isSelected = filterType == type
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) NavyPrimary else Color(0xFFE2E8F0),
                            modifier = Modifier.clickable { viewModel.filterType.value = type }
                        ) {
                            Text(
                                text = type,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }

                // Gender chips
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("الكل", "ذكر", "أنثى").forEach { gender ->
                        val isSelected = filterGender == gender
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) (if (gender == "أنثى") Color(0xFF9D174D) else Color(0xFF1E3A8A)) else Color(0xFFE2E8F0),
                            modifier = Modifier.clickable { viewModel.filterGender.value = gender }
                        ) {
                            Text(
                                text = gender,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF1E293B)
                            )
                        }
                    }
                }
            }
        }

        // Count & Total amount bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "عدد النتائج: ${transactions.size}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "الإجمالي: ${CurrencyUtil.formatRiyal(totalActiveAmount)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
            }
        }

        if (transactions.isEmpty()) {
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
                        Text(
                            text = "لا توجد عمليات تطابق البحث أو الفلتر المحدد.",
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { tx ->
                TransactionCard(
                    tx = tx,
                    onClick = { viewModel.selectTxForDetail(tx) }
                )
            }
        }
    }

    // Detail Dialog
    if (selectedTx != null) {
        val tx = selectedTx!!
        val context = LocalContext.current
        Dialog(onDismissRequest = { viewModel.selectTxForDetail(null) }) {
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
                        IconButton(onClick = { viewModel.selectTxForDetail(null) }) {
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
                            DetailItem(label = "رقم الإيصال:", value = tx.receiptNumber, isBold = true)
                            DetailItem(label = "اسم المواطن:", value = tx.citizenName, isBold = true)
                            DetailItem(label = "نوع المعاملة:", value = "استمارة ${tx.transactionType}")
                            DetailItem(label = "الجنس:", value = tx.gender)
                            DetailItem(label = "المبلغ الإجمالي:", value = CurrencyUtil.formatRiyal(tx.salePrice), color = IncomeGreen, isBold = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            DetailItem(label = "حق الدولة:", value = CurrencyUtil.formatRiyal(tx.stateShare))
                            DetailItem(label = "حصة الإدارة:", value = CurrencyUtil.formatRiyal(tx.directorateShare))
                            DetailItem(label = "نصيب الصندوق:", value = CurrencyUtil.formatRiyal(tx.fundShare), color = InfoBlue, isBold = true)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            DetailItem(label = "التاريخ الميلادي:", value = tx.gregorianDate)
                            DetailItem(label = "التاريخ الهجري:", value = tx.hijriDate)
                            DetailItem(label = "الوقت:", value = tx.timeString)
                            DetailItem(label = "المسجل:", value = tx.createdByName)
                            if (!tx.notes.isNullOrBlank()) {
                                DetailItem(label = "ملاحظات:", value = tx.notes)
                            }
                            if (tx.status == "CANCELLED") {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                                DetailItem(label = "الحالة:", value = "ملغاة", color = ExpenseRed, isBold = true)
                                DetailItem(label = "أُلغيت بواسطة:", value = tx.cancelledBy ?: "-")
                                DetailItem(label = "سبب الإلغاء:", value = tx.cancellationReason ?: "-")
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
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة", fontSize = 13.sp)
                        }

                        if (tx.status == "ACTIVE") {
                            Button(
                                onClick = {
                                    viewModel.promptCancelTx(tx)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إلغاء العملية", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailItem(
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
