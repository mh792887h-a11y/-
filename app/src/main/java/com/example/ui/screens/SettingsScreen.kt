package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.FeeSettingEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtil
import com.example.util.PrintAndExportUtil
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val feeSettings by viewModel.feeSettings.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showFeeEditorDialog by remember { mutableStateOf<FeeSettingEntity?>(null) }
    var showDirectorateDialog by remember { mutableStateOf(false) }
    var showAuditLogsDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section: Directorate Name
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "اسم الإدارة / المصلحة", fontSize = 12.sp, color = Color(0xFF64748B))
                        Text(text = directorateName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = NavyPrimary)
                    }

                    if (currentUser?.role == "ADMIN") {
                        IconButton(onClick = { showDirectorateDialog = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = NavyPrimary)
                        }
                    }
                }
            }
        }

        // Section: Fee Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                                text = "إعدادات رسوم الاستمارات",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                            Text(
                                text = "التعديل يطبّق على المعاملات الجديدة فقط (صلاحية المدير)",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        if (currentUser?.role != "ADMIN") {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    feeSettings.forEach { fee ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable(enabled = currentUser?.role == "ADMIN") {
                                    showFeeEditorDialog = fee
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "استمارة ${fee.typeNameArabic}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = "دولة: ${CurrencyUtil.formatNumber(fee.stateShare)} | إدارة: ${CurrencyUtil.formatNumber(fee.directorateShare)} | صندوق: ${CurrencyUtil.formatNumber(fee.fundShare)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = CurrencyUtil.formatRiyal(fee.salePrice),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = IncomeGreen
                                )
                                if (currentUser?.role == "ADMIN") {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = NavyPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        // Section: Fast Access Tools
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    SettingsActionRow(
                        title = "تبديل المستخدم الحالي",
                        subtitle = "المستخدم الحالي: ${currentUser?.fullName} (${currentUser?.role})",
                        icon = Icons.Default.AccountCircle,
                        onClick = { viewModel.showUserSwitch(true) }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsActionRow(
                        title = "سجل المراجعة والرقابة (Audit Log)",
                        subtitle = "سجل تفصيلي بكافة العمليات والتعديلات والإلغاءات",
                        icon = Icons.Default.History,
                        onClick = { showAuditLogsDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsActionRow(
                        title = "تصدير نسخة احتياطية من البيانات",
                        subtitle = "حفظ ومشاركة نسخة كاملة من قاعدة البيانات بتنسيق JSON",
                        icon = Icons.Default.Share,
                        onClick = {
                            coroutineScope.launch {
                                val json = viewModel.exportBackupJson()
                                PrintAndExportUtil.shareTextReport(context, "نسخة احتياطية صندوق الأحوال المدنية", json)
                            }
                        }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsActionRow(
                        title = "استعادة نسخة احتياطية",
                        subtitle = "استيراد قاعدة البيانات من نص JSON سابق",
                        icon = Icons.Default.Upload,
                        onClick = { showRestoreDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsActionRow(
                        title = "معلومات التقويم والتاريخ",
                        subtitle = "عرض تفاصيل التاريخ الميلادي والهجري وأيام السنة",
                        icon = Icons.Default.CalendarToday,
                        onClick = { viewModel.showDateInfo(true) }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SettingsActionRow(
                        title = "حول التطبيق والمطور",
                        subtitle = "برمجة وتصميم محمد هشام الصلاحي",
                        icon = Icons.Default.Info,
                        onClick = { viewModel.showAbout(true) }
                    )
                }
            }
        }
    }

    // Fee Editor Dialog
    if (showFeeEditorDialog != null) {
        val fee = showFeeEditorDialog!!
        var salePriceText by remember { mutableStateOf(fee.salePrice.toInt().toString()) }
        var stateShareText by remember { mutableStateOf(fee.stateShare.toInt().toString()) }
        var dirShareText by remember { mutableStateOf(fee.directorateShare.toInt().toString()) }

        val sale = salePriceText.toDoubleOrNull() ?: 0.0
        val state = stateShareText.toDoubleOrNull() ?: 0.0
        val dir = dirShareText.toDoubleOrNull() ?: 0.0
        val fund = sale - state - dir

        Dialog(onDismissRequest = { showFeeEditorDialog = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(12.dp)
                    .imePadding(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Text(
                        text = "تعديل رسوم: استمارة ${fee.typeNameArabic}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = salePriceText,
                        onValueChange = { salePriceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("سعر البيع للمواطن بالريال *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = stateShareText,
                        onValueChange = { stateShareText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("حق الدولة بالريال *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = dirShareText,
                        onValueChange = { dirShareText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("حصة الإدارة بالريال *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (fund >= 0) Color(0xFFF1F5F9) else Color(0xFFFFEBEE)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "نصيب الصندوق المحتسب:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                text = CurrencyUtil.formatRiyal(fund),
                                fontWeight = FontWeight.Bold,
                                color = if (fund >= 0) InfoBlue else ExpenseRed,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (sale > 0 && fund >= 0) {
                                viewModel.updateFeeSetting(fee.formType, sale, state, dir)
                                showFeeEditorDialog = null
                            }
                        },
                        enabled = sale > 0 && fund >= 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("حفظ الأسعار الجديدة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Directorate Name Dialog
    if (showDirectorateDialog) {
        var tempName by remember { mutableStateOf(directorateName) }
        Dialog(onDismissRequest = { showDirectorateDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(12.dp)
                    .imePadding(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("تعديل اسم الإدارة أو الفرع", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (tempName.isNotBlank()) {
                                viewModel.updateDirectorateName(tempName.trim())
                                showDirectorateDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("حفظ", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Restore Backup Dialog
    if (showRestoreDialog) {
        var jsonInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showRestoreDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(12.dp)
                    .imePadding(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("استعادة نسخة احتياطية", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                    Text("الصق نص JSON الخاص بالنسخة الاحتياطية هنا:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            if (jsonInput.isNotBlank()) {
                                viewModel.restoreBackupJson(jsonInput.trim())
                                showRestoreDialog = false
                            }
                        },
                        enabled = jsonInput.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("استعادة البيانات", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Audit Logs Dialog
    if (showAuditLogsDialog) {
        Dialog(onDismissRequest = { showAuditLogsDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .padding(10.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "سجل المراجعة والرقابة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NavyPrimary
                        )
                        IconButton(onClick = { showAuditLogsDialog = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (auditLogs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد سجلات بعد.", color = Color(0xFF94A3B8))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(auditLogs, key = { it.id }) { log ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = log.actionType, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                            Text(text = log.performedBy, fontSize = 11.sp, color = Color(0xFF64748B))
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = log.details, fontSize = 12.sp, color = Color(0xFF334155))
                                        Text(text = "${log.gregorianDate} • ${log.hijriDate}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
        }
    }
}
