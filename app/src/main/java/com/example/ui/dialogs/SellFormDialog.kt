package com.example.ui.dialogs

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.FeeSettingEntity
import com.example.data.entity.TransactionEntity
import com.example.data.repository.CivilFundRepository
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.HijriDateUtil
import com.example.util.PrintAndExportUtil

data class BatchRowState(
    var citizenName: String = "",
    var formNumber: String = "",
    var recordNumber: String = "",
    var gender: String = "ذكر"
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SellFormDialog(
    feeSettings: List<FeeSettingEntity>,
    recentCitizenNames: List<String>,
    initialDate: String = HijriDateUtil.getTodayGregorianString(),
    onDismiss: () -> Unit,
    onSubmit: (
        citizenName: String,
        formNumber: String,
        recordNumber: String,
        formType: String,
        gender: String,
        notes: String?,
        customGregorianDate: String?,
        customHijriDate: String?
    ) -> Unit,
    onBatchSubmit: (
        items: List<CivilFundRepository.BatchFormItem>,
        formType: String,
        notes: String?,
        customGregorianDate: String?,
        customHijriDate: String?
    ) -> Unit
) {
    // Mode: Single (استمارة مفردة) or Batch (10 خانات)
    var isBatchMode by remember { mutableStateOf(false) }

    // Date management via Calendar Picker
    var selectedDate by remember { mutableStateOf(initialDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    val isToday = selectedDate == HijriDateUtil.getTodayGregorianString()
    val hijriDate = remember(selectedDate) {
        HijriDateUtil.getHijriDateFromString(selectedDate).formatted
    }
    val formattedGregorian = remember(selectedDate) {
        HijriDateUtil.getFormattedArabicWithDayOfWeek(selectedDate)
    }

    // Shared: Transaction Type
    var selectedType by remember { mutableStateOf("جديد") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Single Form state
    var singleCitizenName by remember { mutableStateOf("") }
    var singleFormNumber by remember { mutableStateOf("") }
    var singleRecordNumber by remember { mutableStateOf("") }
    var singleGender by remember { mutableStateOf("ذكر") }

    // Batch 10-slots state
    var baseFormNumber by remember { mutableStateOf("") }
    var baseRecordNumber by remember { mutableStateOf("") }
    val batchRows = remember {
        mutableStateListOf(
            BatchRowState(),
            BatchRowState(),
            BatchRowState(),
            BatchRowState(),
            BatchRowState(),
            BatchRowState(),
            BatchRowState(),
            BatchRowState(),
            BatchRowState(),
            BatchRowState()
        )
    }

    // Current fee calculation
    val currentFee = feeSettings.firstOrNull { it.typeNameArabic == selectedType }
        ?: FeeSettingEntity(
            formType = "NEW",
            typeNameArabic = "جديد",
            salePrice = 4500.0,
            stateShare = 3750.0,
            directorateShare = 200.0,
            fundShare = 550.0
        )

    val matchingCitizens = remember(singleCitizenName) {
        if (singleCitizenName.length >= 2) {
            recentCitizenNames.filter { it.contains(singleCitizenName.trim(), ignoreCase = true) }.take(4)
        } else {
            emptyList()
        }
    }

    val filledBatchCount = batchRows.count { it.citizenName.isNotBlank() }

    // Function to apply sequential numbering to batch rows
    fun applySequentialNumbering() {
        val startForm = baseFormNumber.trim().toLongOrNull()
        val startRecord = baseRecordNumber.trim().toLongOrNull()

        batchRows.forEachIndexed { index, row ->
            if (startForm != null) {
                row.formNumber = (startForm + index).toString()
            }
            if (startRecord != null) {
                row.recordNumber = (startRecord + index).toString()
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 760.dp)
                .padding(6.dp)
                .imePadding()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isBatchMode) Icons.Default.GroupAdd else Icons.Default.ConfirmationNumber,
                                    contentDescription = null,
                                    tint = NavyPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBatchMode) "بيع استمارات متعددة (10 خانات)" else "بيع استمارة أحوال مدنية",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            )
                            Text(
                                text = "تسجيل فوري وإصدار إيصال وتحديث الصندوق",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selector Toggle: [ استمارة مفردة ] | [ ⚡ إدخال متعدد (10 خانات) ]
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(4.dp)) {
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (!isBatchMode) NavyPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable { isBatchMode = false }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "📝 استمارة مفردة",
                                    fontSize = 12.sp,
                                    fontWeight = if (!isBatchMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isBatchMode) Color.White else Color(0xFF475569)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isBatchMode) NavyPrimary else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clickable { isBatchMode = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "⚡ إدخال متعدد (10 خانات)",
                                    fontSize = 12.sp,
                                    fontWeight = if (isBatchMode) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isBatchMode) Color.White else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calendar Date Picker Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isToday) Color(0xFFF8FAFC) else Color(0xFFFFFBEB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isToday) Color(0xFFE2E8F0) else Color(0xFFFDE68A)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formattedGregorian,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NavyPrimary
                                    )
                                    if (!isToday) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFF59E0B)
                                        ) {
                                            Text(
                                                text = "يومية سابقة",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "الموافق هجرياً: $hijriDate",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0369A1)
                                )
                            }

                            Button(
                                onClick = { showDatePicker = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اختر من التقويم", fontSize = 11.sp)
                            }
                        }

                        if (!isToday) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📌 سيتم قيد هذه المعاملة تلقائياً في يومية تاريخ: $selectedDate م",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Transaction Type Selector (Shared)
                Text(
                    text = "نوع المعاملة:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("جديد", "تجديد", "بدل فاقد", "بدل تالف").forEach { type ->
                        val isSelected = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable { selectedType = type },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) NavyPrimary else Color(0xFFF1F5F9)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = type,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Content depending on Mode
                if (!isBatchMode) {
                    // ================= SINGLE FORM MODE =================
                    OutlinedTextField(
                        value = singleCitizenName,
                        onValueChange = { singleCitizenName = it },
                        label = { Text("اسم المواطن الرباعي *") },
                        placeholder = { Text("مثال: علي محمد ناصر الحميري") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = NavyPrimary)
                        },
                        trailingIcon = {
                            if (singleCitizenName.isNotEmpty()) {
                                IconButton(onClick = { singleCitizenName = "" }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("citizen_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Autocomplete chips
                    AnimatedVisibility(visible = matchingCitizens.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 4.dp)) {
                            Text(text = "اقتراحات مسجلة سابقاً:", fontSize = 11.sp, color = Color(0xFF64748B))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matchingCitizens.forEach { name ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFEFF6FF),
                                        modifier = Modifier.clickable { singleCitizenName = name }
                                    ) {
                                        Text(
                                            text = name,
                                            fontSize = 11.sp,
                                            color = NavyPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = singleFormNumber,
                            onValueChange = { singleFormNumber = it },
                            label = { Text("رقم الاستمارة *") },
                            placeholder = { Text("مثال: 1205") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.ConfirmationNumber, contentDescription = null, tint = NavyPrimary)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("form_number_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = singleRecordNumber,
                            onValueChange = { singleRecordNumber = it },
                            label = { Text("رقم القيد *") },
                            placeholder = { Text("مثال: 840") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = NavyPrimary)
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("record_number_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Gender
                    Text(text = "الجنس:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ذكر", "أنثى").forEach { gender ->
                            val isSelected = singleGender == gender
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clickable { singleGender = gender },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) (if (gender == "ذكر") Color(0xFF1E3A8A) else Color(0xFF9D174D)) else Color(0xFFF1F5F9)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (gender == "ذكر") "👨 ذكر" else "👩 أنثى",
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // ================= BATCH (10 SLOTS) MODE =================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "الترقيم التسلسلي التلقائي (اختياري)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                }

                                Button(
                                    onClick = { applySequentialNumbering() },
                                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    enabled = baseFormNumber.isNotBlank() || baseRecordNumber.isNotBlank()
                                ) {
                                    Text("تطبيق التسلسل", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = baseFormNumber,
                                    onValueChange = { baseFormNumber = it },
                                    label = { Text("رقم أول استمارة") },
                                    placeholder = { Text("مثال: 1001") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = baseRecordNumber,
                                    onValueChange = { baseRecordNumber = it },
                                    label = { Text("رقم أول قيد") },
                                    placeholder = { Text("مثال: 501") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "قائمة أسماء المواطنين (10 خانات):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "اكتب أسماء المواطنين في الخانات أدناه، وسيتم حفظ كل اسم كاستمارة مستقلة بإيصال خاص به.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 10 input rows
                    batchRows.forEachIndexed { index, row ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (row.citizenName.isNotBlank()) Color(0xFFF8FAFC) else Color(0xFFFAFAFA)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (row.citizenName.isNotBlank()) Color(0xFFCBD5E1) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (row.citizenName.isNotBlank()) NavyPrimary else Color(0xFF94A3B8),
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${index + 1}",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    OutlinedTextField(
                                        value = row.citizenName,
                                        onValueChange = { row.citizenName = it },
                                        placeholder = { Text("اسم المواطن #${index + 1}") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Mini gender toggle
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (row.gender == "ذكر") Color(0xFFEFF6FF) else Color(0xFFFDF2F8),
                                        modifier = Modifier
                                            .clickable {
                                                row.gender = if (row.gender == "ذكر") "أنثى" else "ذكر"
                                            }
                                            .border(
                                                1.dp,
                                                if (row.gender == "ذكر") Color(0xFF93C5FD) else Color(0xFFF472B6),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = if (row.gender == "ذكر") "ذكر" else "أنثى",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (row.gender == "ذكر") Color(0xFF1D4ED8) else Color(0xFFBE185D)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = row.formNumber,
                                        onValueChange = { row.formNumber = it },
                                        placeholder = { Text("رقم الاستمارة") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )

                                    OutlinedTextField(
                                        value = row.recordNumber,
                                        onValueChange = { row.recordNumber = it },
                                        placeholder = { Text("رقم القيد") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات عامة (اختياري)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Financial Summary Card
                val totalAmount = if (!isBatchMode) currentFee.salePrice else (filledBatchCount * currentFee.salePrice)
                val totalFundShare = if (!isBatchMode) currentFee.fundShare else (filledBatchCount * currentFee.fundShare)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (!isBatchMode) "المبلغ المطلوب:" else "إجمالي المبلغ ($filledBatchCount استمارة):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = NavyPrimary
                            )
                            Text(
                                text = CurrencyUtil.formatRiyal(totalAmount),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = IncomeGreen
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFCBD5E1))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "صافي نصيب الصندوق:", fontSize = 12.sp, color = Color(0xFF475569))
                            Text(
                                text = CurrencyUtil.formatRiyal(totalFundShare),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = InfoBlue
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Submit Action Button
                val canSubmit = if (!isBatchMode) {
                    !isSubmitting && singleCitizenName.isNotBlank() && singleFormNumber.isNotBlank() && singleRecordNumber.isNotBlank()
                } else {
                    !isSubmitting && filledBatchCount > 0
                }

                Button(
                    onClick = {
                        if (!canSubmit) return@Button
                        isSubmitting = true
                        val finalCustomDate = if (!isToday) selectedDate else null

                        if (!isBatchMode) {
                            onSubmit(
                                singleCitizenName.trim(),
                                singleFormNumber.trim(),
                                singleRecordNumber.trim(),
                                selectedType,
                                singleGender,
                                notes.ifBlank { null },
                                finalCustomDate,
                                null
                            )
                        } else {
                            val itemsToSave = batchRows
                                .filter { it.citizenName.isNotBlank() }
                                .map { row ->
                                    CivilFundRepository.BatchFormItem(
                                        citizenName = row.citizenName.trim(),
                                        formNumber = row.formNumber.trim().ifBlank { "-" },
                                        recordNumber = row.recordNumber.trim().ifBlank { "-" },
                                        gender = row.gender
                                    )
                                }
                            onBatchSubmit(
                                itemsToSave,
                                selectedType,
                                notes.ifBlank { null },
                                finalCustomDate,
                                null
                            )
                        }
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_transaction_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = if (!isBatchMode) "حفظ وإصدار الإيصال" else "حفظ كافة الاستمارات ($filledBatchCount استمارة)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateString = selectedDate,
            onDateSelected = { pickedDate ->
                selectedDate = pickedDate
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

/**
 * Receipt Confirmation Modal shown after successful sale.
 * Features:
 * 1) Official Print / Save as PDF (Does NOT save photos to gallery)
 * 2) Share text via WhatsApp or messaging
 */
@Composable
fun ReceiptConfirmationDialog(
    receipt: TransactionEntity,
    directorateName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(10.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "تم تسجيل العملية بنجاح",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                )

                Text(
                    text = "إيصال رقم: #${receipt.receiptNumber}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        ReceiptRow(label = "اسم المواطن:", value = receipt.citizenName, isBold = true)
                        ReceiptRow(label = "رقم الاستمارة:", value = receipt.formNumber.ifBlank { "-" }, isBold = true)
                        ReceiptRow(label = "رقم القيد:", value = receipt.recordNumber.ifBlank { "-" }, isBold = true)
                        ReceiptRow(label = "نوع المعاملة:", value = "استمارة ${receipt.transactionType}")
                        ReceiptRow(label = "الجنس:", value = receipt.gender)
                        ReceiptRow(label = "المبلغ الإجمالي:", value = CurrencyUtil.formatRiyal(receipt.salePrice), isBold = true)
                        ReceiptRow(label = "دخل الصندوق الصافي:", value = CurrencyUtil.formatRiyal(receipt.fundShare), valueColor = IncomeGreen, isBold = true)
                        ReceiptRow(label = "حق الإدارة (المدير):", value = CurrencyUtil.formatRiyal(receipt.directorateShare), valueColor = NavyPrimary, isBold = true)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE2E8F0))
                        ReceiptRow(label = "التاريخ الميلادي:", value = receipt.gregorianDate)
                        ReceiptRow(label = "التاريخ الهجري:", value = receipt.hijriDate)
                        ReceiptRow(label = "الوقت:", value = receipt.timeString)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Information banner about PDF export & no gallery clutter
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0FDF4),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📄 التصدير يتم بتنسيق PDF رسمي عبر نظام الطباعة مباشرة ولا يتم حفظ أي صور في ألبوم المعرض.",
                        fontSize = 11.sp,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: PDF Print/Export | WhatsApp Share | Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Option 1: PDF Export & Print
                    Button(
                        onClick = {
                            PrintAndExportUtil.printTransactionReceipt(context, receipt, directorateName)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .height(48.dp)
                            .testTag("print_receipt_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = InfoBlue)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "تصدير / طباعة PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Option 2: Share text
                    Button(
                        onClick = {
                            val shareText = """
                                📄 الجمهورية اليمنية - مصلحة الأحوال المدنية
                                إيصال استلام رسمي رقم: #${receipt.receiptNumber}
                                المواطن: ${receipt.citizenName}
                                الاستمارة: ${receipt.formNumber} | القيد: ${receipt.recordNumber}
                                النوع: استمارة ${receipt.transactionType}
                                المبلغ: ${CurrencyUtil.formatRiyal(receipt.salePrice)}
                                التاريخ: ${receipt.gregorianDate} م (${receipt.hijriDate})
                            """.trimIndent()
                            PrintAndExportUtil.shareTextReport(context, "إيصال #${receipt.receiptNumber}", shareText)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "مشاركة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("close_receipt_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "إغلاق النافذة", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = Color.Unspecified
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
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor
        )
    }
}
