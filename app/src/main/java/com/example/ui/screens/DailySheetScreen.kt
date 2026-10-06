package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.ViewColumn
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.TransactionEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.dialogs.AppDatePickerDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.HijriDateUtil
import com.example.util.PrintAndExportUtil
import kotlin.math.ceil
import kotlin.math.max

// Official sheet has 50 entries per page (25 rows on Right half, 25 rows on Left half)
private const val ROWS_PER_PAGE = 50
private const val HALF_ROWS = 25

@Composable
fun DailySheetScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedReportDate.collectAsState()
    val allDayTransactions by viewModel.selectedDateTransactions.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // 1. Category Paper Selection: "جديد", "تجديد", "بدل فاقد", "بدل تالف"
    var selectedFormType by remember { mutableStateOf("جديد") }

    // Filter transactions by the selected category
    val categoryTransactions = remember(allDayTransactions, selectedFormType) {
        allDayTransactions
            .filter { it.transactionType == selectedFormType && it.status == "ACTIVE" }
            .sortedBy { it.id }
    }

    // 2. Pagination for the selected category paper sheet (50 entries per page)
    val calculatedTotalPages = remember(categoryTransactions.size) {
        max(1, ceil(categoryTransactions.size.toDouble() / ROWS_PER_PAGE).toInt())
    }
    var currentPageIndex by remember(selectedFormType, selectedDate) { mutableIntStateOf(1) }

    LaunchedEffect(calculatedTotalPages) {
        if (currentPageIndex > calculatedTotalPages) {
            currentPageIndex = calculatedTotalPages
        }
    }

    // Slice transactions for current 50-slot page
    val pageStartIndex = (currentPageIndex - 1) * ROWS_PER_PAGE
    val currentSheetTransactions = remember(categoryTransactions, currentPageIndex) {
        categoryTransactions.drop(pageStartIndex).take(ROWS_PER_PAGE)
    }

    // Date info for display
    val hijriDate = remember(selectedDate, allDayTransactions) {
        allDayTransactions.firstOrNull()?.hijriDate
            ?: HijriDateUtil.getHijriDateFromString(selectedDate).formatted
    }
    val formattedGregorianDate = remember(selectedDate) {
        HijriDateUtil.getFormattedArabicWithDayOfWeek(selectedDate)
    }
    val isToday = selectedDate == viewModel.todayDate

    // In-Sheet Fast Input States
    var citizenNameInput by remember { mutableStateOf("") }
    var formNumberInput by remember { mutableStateOf("") }
    var recordNumberInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("ذكر") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Auto-suggest next sequential numbers based on the last registered transaction
    LaunchedEffect(categoryTransactions, selectedFormType) {
        val lastTx = categoryTransactions.lastOrNull()
        if (lastTx != null) {
            val lastFormNum = lastTx.formNumber.trim().toLongOrNull()
            if (lastFormNum != null && formNumberInput.isBlank()) {
                formNumberInput = (lastFormNum + 1).toString()
            }
            val lastRecordNum = lastTx.recordNumber.trim().toLongOrNull()
            if (lastRecordNum != null && recordNumberInput.isBlank()) {
                recordNumberInput = (lastRecordNum + 1).toString()
            }
        }
    }

    // Dialog state for slot click, date pick, edit, delete
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var txToEditState by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDeleteState by remember { mutableStateOf<TransactionEntity?>(null) }
    var slotToRegisterState by remember { mutableStateOf<Int?>(null) }

    val totalCategoryFormsCount = categoryTransactions.size

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("official_daily_sheet_screen"),
        contentPadding = PaddingValues(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ==========================================
        // SECTION 1: TOP CONTROL & PRINT BAR
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "كشف الحركة اليومي للاستمارات الشخصية",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "الورقة الرسمية المعتمدة بـ 50 خانة (25 يمين + 25 يسار)",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }

                        // Print Official A4 Button
                        Button(
                            onClick = {
                                PrintAndExportUtil.printOfficialMovementSheet(
                                    context = context,
                                    formType = selectedFormType,
                                    gregorianDate = selectedDate,
                                    hijriDate = hijriDate,
                                    pageNumber = currentPageIndex,
                                    totalPages = calculatedTotalPages,
                                    pageTransactions = currentSheetTransactions,
                                    directorateName = directorateName,
                                    cashierName = currentUser?.fullName ?: "أمين الصندوق"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("print_official_sheet_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة A4 الورقة الرسمية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$formattedGregorianDate (الموافق: $hijriDate)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = "مديرية: $directorateName | أمين الصندوق: ${currentUser?.fullName ?: "أمين الصندوق"}",
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        OutlinedButton(
                            onClick = { showDatePickerDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تغيير اليوم", fontSize = 10.5.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 2: CATEGORY TABS (أوراق مستقلة لكل نوع)
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "اختر ورقة المعاملة (لكل نوع ورقة كشف مستقلة):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val categories = listOf(
                        Triple("جديد", "استمارات جديد", Color(0xFF059669)),
                        Triple("تجديد", "استمارات تجديد", Color(0xFF0284C7)),
                        Triple("بدل فاقد", "استمارات فاقد", Color(0xFFD97706)),
                        Triple("بدل تالف", "استمارات تالف", Color(0xFF7C3AED))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { (typeKey, title, typeColor) ->
                            val isSelected = selectedFormType == typeKey
                            val countForThisType = allDayTransactions.count { it.transactionType == typeKey && it.status == "ACTIVE" }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) typeColor else Color(0xFFF1F5F9),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedFormType = typeKey
                                        currentPageIndex = 1
                                    }
                                    .testTag("tab_sheet_$typeKey")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = typeKey,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "$countForThisType استمارة",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 3: IN-SHEET FAST REGISTRATION BAR
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("in_sheet_registration_box"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = IncomeGreen,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "تسجيل فوري في ورقة ( $selectedFormType )",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "السطر التالي: #${totalCategoryFormsCount + 1}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Citizen Name Input
                    OutlinedTextField(
                        value = citizenNameInput,
                        onValueChange = { citizenNameInput = it },
                        label = { Text("اسم المواطن الرباعي *") },
                        placeholder = { Text("اكتب اسم المواطن هنا...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = IncomeGreen)
                        },
                        trailingIcon = {
                            if (citizenNameInput.isNotEmpty()) {
                                IconButton(onClick = { citizenNameInput = "" }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("in_sheet_citizen_input"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IncomeGreen,
                            unfocusedBorderColor = Color(0xFF86EFAC),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = recordNumberInput,
                            onValueChange = { recordNumberInput = it },
                            label = { Text("رقم القيد") },
                            placeholder = { Text("501") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("in_sheet_record_num"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = IncomeGreen,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        OutlinedTextField(
                            value = formNumberInput,
                            onValueChange = { formNumberInput = it },
                            label = { Text("رقم الاستمارة") },
                            placeholder = { Text("1001") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("in_sheet_form_num"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = IncomeGreen,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (genderInput == "ذكر") Color(0xFFEFF6FF) else Color(0xFFFDF2F8),
                            border = BorderStroke(1.dp, if (genderInput == "ذكر") Color(0xFF93C5FD) else Color(0xFFF472B6)),
                            modifier = Modifier
                                .height(50.dp)
                                .clickable { genderInput = if (genderInput == "ذكر") "أنثى" else "ذكر" }
                                .padding(horizontal = 2.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = if (genderInput == "ذكر") "👨 ذكر" else "👩 أنثى",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (genderInput == "ذكر") Color(0xFF1D4ED8) else Color(0xFFBE185D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    val canSave = citizenNameInput.isNotBlank() && !isSubmitting
                    Button(
                        onClick = {
                            if (!canSave) return@Button
                            isSubmitting = true
                            viewModel.sellForm(
                                citizenName = citizenNameInput.trim(),
                                formNumber = formNumberInput.trim().ifBlank { "-" },
                                recordNumber = recordNumberInput.trim().ifBlank { "-" },
                                formTypeNameArabic = selectedFormType,
                                gender = genderInput,
                                notes = null,
                                customGregorianDate = if (!isToday) selectedDate else null,
                                customHijriDate = null
                            )

                            citizenNameInput = ""
                            val currentFormLong = formNumberInput.trim().toLongOrNull()
                            if (currentFormLong != null) {
                                formNumberInput = (currentFormLong + 1).toString()
                            }
                            val currentRecLong = recordNumberInput.trim().toLongOrNull()
                            if (currentRecLong != null) {
                                recordNumberInput = (currentRecLong + 1).toString()
                            }

                            if ((totalCategoryFormsCount + 1) > currentPageIndex * ROWS_PER_PAGE) {
                                currentPageIndex++
                            }

                            isSubmitting = false
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("in_sheet_submit_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تسجيل وحفظ في الورقة ⚡ (السطر #${totalCategoryFormsCount + 1})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 4: PAPER PAGER (مراجعة الأوراق السابقة والجديدة)
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = NavyPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "أوراق كشف ($selectedFormType):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.clickable {
                                currentPageIndex = calculatedTotalPages + 1
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("+ فتح ورقة جديدة (50 خانة)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val maxPagesToShow = max(calculatedTotalPages, currentPageIndex)
                        items(maxPagesToShow) { pageIdx ->
                            val pageNum = pageIdx + 1
                            val isSelected = pageNum == currentPageIndex
                            val startItem = (pageNum - 1) * ROWS_PER_PAGE + 1
                            val endItem = pageNum * ROWS_PER_PAGE

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NavyPrimary else Color(0xFFF8FAFC),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .clickable { currentPageIndex = pageNum }
                                    .testTag("sheet_page_chip_$pageNum")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📄 ورقة $pageNum ($startItem-$endItem)",
                                        fontSize = 10.5.sp,
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

        // ==========================================
        // SECTION 5: EXACT SCANNED PAPER SHEET VIEW (25 rows on right + 25 rows on left)
        // ==========================================
        item {
            ExactOfficial50RowPaperView(
                formType = selectedFormType,
                directorateName = directorateName,
                gregorianDate = selectedDate,
                hijriDate = hijriDate,
                pageNumber = currentPageIndex,
                totalPages = max(calculatedTotalPages, currentPageIndex),
                transactions = currentSheetTransactions,
                cashierName = currentUser?.fullName ?: "أمين الصندوق",
                onRowClick = { tx ->
                    txToEditState = tx
                },
                onEmptySlotClick = { slotNumber ->
                    slotToRegisterState = slotNumber
                },
                onDeleteClick = { tx ->
                    txToDeleteState = tx
                }
            )
        }
    }

    // Safe Dialog for direct slot click
    if (slotToRegisterState != null) {
        val slotNum = slotToRegisterState!!
        var directName by remember { mutableStateOf("") }
        var directFormNum by remember { mutableStateOf(formNumberInput) }
        var directRecordNum by remember { mutableStateOf(recordNumberInput) }
        var directGender by remember { mutableStateOf("ذكر") }

        Dialog(
            onDismissRequest = { slotToRegisterState = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تسجيل استمارة في السطر #$slotNum ($selectedFormType)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = NavyPrimary
                        )
                        IconButton(onClick = { slotToRegisterState = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = directName,
                        onValueChange = { directName = it },
                        label = { Text("اسم المواطن الرباعي *") },
                        placeholder = { Text("اكتب الاسم الرباعي...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = directRecordNum,
                            onValueChange = { directRecordNum = it },
                            label = { Text("رقم القيد") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = directFormNum,
                            onValueChange = { directFormNum = it },
                            label = { Text("رقم الاستمارة") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("ذكر", "أنثى").forEach { g ->
                            val isSelected = directGender == g
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) (if (g == "ذكر") Color(0xFF1E3A8A) else Color(0xFF9D174D)) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { directGender = g }
                            ) {
                                Text(
                                    text = if (g == "ذكر") "👨 ذكر" else "👩 أنثى",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF334155),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (directName.isBlank()) return@Button
                            viewModel.sellForm(
                                citizenName = directName.trim(),
                                formNumber = directFormNum.trim().ifBlank { "-" },
                                recordNumber = directRecordNum.trim().ifBlank { "-" },
                                formTypeNameArabic = selectedFormType,
                                gender = directGender,
                                notes = null,
                                customGregorianDate = if (!isToday) selectedDate else null,
                                customHijriDate = null
                            )
                            slotToRegisterState = null
                        },
                        enabled = directName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ الاستمارة في السطر #$slotNum", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePickerDialog) {
        AppDatePickerDialog(
            initialDateString = selectedDate,
            onDateSelected = { pickedDate ->
                viewModel.setSelectedReportDate(pickedDate)
                showDatePickerDialog = false
            },
            onDismiss = { showDatePickerDialog = false }
        )
    }

    // Edit Transaction Dialog
    if (txToEditState != null) {
        val tx = txToEditState!!
        var editName by remember { mutableStateOf(tx.citizenName) }
        var editFormNum by remember { mutableStateOf(tx.formNumber) }
        var editRecordNum by remember { mutableStateOf(tx.recordNumber) }
        var editGender by remember { mutableStateOf(tx.gender) }

        Dialog(
            onDismissRequest = { txToEditState = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تعديل بيانات الاستمارة #${tx.formNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = NavyPrimary
                        )
                        IconButton(onClick = { txToEditState = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("اسم المواطن الرباعي") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editRecordNum,
                            onValueChange = { editRecordNum = it },
                            label = { Text("رقم القيد") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = editFormNum,
                            onValueChange = { editFormNum = it },
                            label = { Text("رقم الاستمارة") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateTransaction(
                                transactionId = tx.id,
                                citizenName = editName.trim(),
                                formNumber = editFormNum.trim(),
                                recordNumber = editRecordNum.trim(),
                                transactionType = tx.transactionType,
                                gender = editGender,
                                notes = tx.notes,
                                gregorianDate = tx.gregorianDate,
                                hijriDate = tx.hijriDate
                            )
                            txToEditState = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("حفظ التعديلات في الكشف", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Delete Confirm Dialog
    if (txToDeleteState != null) {
        val tx = txToDeleteState!!
        Dialog(onDismissRequest = { txToDeleteState = null }) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("حذف استمارة: ${tx.citizenName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("سيتم حذف هذه الاستمارة من الكشف وتحديث الصندوق فوراً.", fontSize = 11.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { txToDeleteState = null }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                viewModel.deleteTransaction(tx.id)
                                txToDeleteState = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأكيد الحذف", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Exact replica of the official scanned paper document:
 * Header + 25 rows on right half + 25 rows on left half (50 slots total per sheet) + Signatures footer.
 */
@Composable
fun ExactOfficial50RowPaperView(
    formType: String,
    directorateName: String,
    gregorianDate: String,
    hijriDate: String,
    pageNumber: Int,
    totalPages: Int,
    transactions: List<TransactionEntity>,
    cashierName: String,
    onRowClick: (TransactionEntity) -> Unit,
    onEmptySlotClick: (Int) -> Unit,
    onDeleteClick: (TransactionEntity) -> Unit
) {
    val baseIndex = (pageNumber - 1) * ROWS_PER_PAGE

    // View toggle: Side 1 (Right Half 1..25), Side 2 (Left Half 26..50), or Combined Side-by-Side
    var viewMode by remember { mutableStateOf("FULL") } // "FULL", "SIDE_A", "SIDE_B"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(4.dp))
            .testTag("official_paper_canvas"),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFD)),
        border = BorderStroke(2.dp, Color(0xFF0F172A))
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            // ==========================================
            // OFFICIAL HEADER (الترويسة الرسمية المعتمدة)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Right: Republic & Ministry
                Column(modifier = Modifier.weight(1.1f)) {
                    Text("الجمهورية اليمنية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("وزارة الداخلية", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text("مصلحة الأحوال المدنية والسجل المدني", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                }

                // Center: Emblem & Basmala
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("بسم الله الرحمن الرحيم", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text("🦅", fontSize = 18.sp)
                }

                // Left: Metadata
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("الرقم : ....................", fontSize = 8.5.sp, color = Color(0xFF334155))
                    Text("التاريخ : $gregorianDate م", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("الموافق : $hijriDate", fontSize = 8.5.sp, color = Color(0xFF0369A1))
                    Text("المرفقات : ....................", fontSize = 8.5.sp, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Main Paper Title Banner (Exact wording from document)
            Surface(
                shape = RoundedCornerShape(2.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "كشف الحركة اليومي للاستمارات الشخصية ( $formType ) بمديـرية $directorateName - ورقة ($pageNumber من $totalPages)",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // View toggle: [ النصف الأيمن 1-25 ] | [ النصف الأيسر 26-50 ] | [ عرض الورقة كاملة ↔ ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (viewMode == "SIDE_A") NavyPrimary else Color(0xFFE2E8F0),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewMode = "SIDE_A" }
                ) {
                    Text(
                        text = "الجانب الأيمن (1-25)",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewMode == "SIDE_A") Color.White else Color(0xFF334155),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (viewMode == "SIDE_B") NavyPrimary else Color(0xFFE2E8F0),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewMode = "SIDE_B" }
                ) {
                    Text(
                        text = "الجانب الأيسر (26-50)",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewMode == "SIDE_B") Color.White else Color(0xFF334155),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (viewMode == "FULL") NavyPrimary else Color(0xFFE2E8F0),
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { viewMode = "FULL" }
                ) {
                    Text(
                        text = "عرض الورقة كاملة ↔",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewMode == "FULL") Color.White else Color(0xFF334155),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (viewMode == "FULL") {
                // Full side-by-side table matching the document exactly with horizontal scroll
                Box(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    Column(modifier = Modifier.width(620.dp)) {
                        // Table Header Bar (Both Halves)
                        Surface(
                            color = Color(0xFFE2E8F0),
                            border = BorderStroke(1.dp, Color(0xFF0F172A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp, horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Right Half Headers
                                Text(text = "رقم القيد", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                                Text(text = "رقم الاستمارة", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(58.dp), textAlign = TextAlign.Center)
                                Text(text = "الإســـــــــــ ـــــــــــم", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(135.dp), textAlign = TextAlign.Right)
                                Text(text = "تاريخ القيد/التوقيع", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                                Text(text = "م", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)

                                Box(modifier = Modifier.width(2.dp).height(20.dp).background(Color(0xFF0F172A)))

                                // Left Half Headers
                                Text(text = "رقم القيد", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
                                Text(text = "رقم الاستمارة", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(58.dp), textAlign = TextAlign.Center)
                                Text(text = "الإســـــــــــ ـــــــــــم", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(135.dp), textAlign = TextAlign.Right)
                                Text(text = "تاريخ القيد/التوقيع", fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                                Text(text = "م", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(18.dp), textAlign = TextAlign.Center)
                            }
                        }

                        // Render 25 Rows (Right half: 1..25, Left half: 26..50)
                        for (r in 0 until HALF_ROWS) {
                            val rightSlot = baseIndex + r + 1
                            val rightTx = transactions.getOrNull(r)

                            val leftSlot = baseIndex + HALF_ROWS + r + 1
                            val leftTx = transactions.getOrNull(HALF_ROWS + r)

                            Surface(
                                color = if (r % 2 == 0) Color.White else Color(0xFFF8FAFC),
                                border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp, horizontal = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Right Half Slot
                                    DualHalfCell(
                                        slotNumber = rightSlot,
                                        transaction = rightTx,
                                        onClick = { if (rightTx != null) onRowClick(rightTx) else onEmptySlotClick(rightSlot) }
                                    )

                                    Box(modifier = Modifier.width(2.dp).height(22.dp).background(Color(0xFF0F172A)))

                                    // Left Half Slot
                                    DualHalfCell(
                                        slotNumber = leftSlot,
                                        transaction = leftTx,
                                        onClick = { if (leftTx != null) onRowClick(leftTx) else onEmptySlotClick(leftSlot) }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Column Focused View (Side A: 1-25 or Side B: 26-50)
                val startRow = if (viewMode == "SIDE_A") 0 else HALF_ROWS
                val endRow = if (viewMode == "SIDE_A") HALF_ROWS else ROWS_PER_PAGE

                Surface(
                    color = Color(0xFFE2E8F0),
                    border = BorderStroke(1.dp, Color(0xFF0F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "م", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp), textAlign = TextAlign.Center)
                        Text(text = "رقم القيد", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.Center)
                        Text(text = "رقم الاستمارة", fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                        Text(text = "الإســـــــــــ ـــــــــــم", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
                        Text(text = "التوقيع/الإجراء", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(65.dp), textAlign = TextAlign.Center)
                    }
                }

                for (i in startRow until endRow) {
                    val slotNumber = baseIndex + i + 1
                    val tx = transactions.getOrNull(i)

                    SingleFocusedSlotRow(
                        slotNumber = slotNumber,
                        transaction = tx,
                        onRowClick = { if (tx != null) onRowClick(tx) else onEmptySlotClick(slotNumber) },
                        onDeleteClick = { if (tx != null) onDeleteClick(tx) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFF0F172A), thickness = 1.dp)
            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // OFFICIAL FOOTER SIGNATURES (التوقيعات الرسمية)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Right: Cashier
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("أمين الصندوق", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("الاسم / $cashierName", fontSize = 8.5.sp, color = Color(0xFF334155))
                    Text("التوقيع / ....................", fontSize = 8.sp, color = Color(0xFF64748B))
                }

                // Center: Technical Officer
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("الفني المختص باستلام البطائق وطباعتها", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), textAlign = TextAlign.Center)
                    Text("الاسم / ....................", fontSize = 8.5.sp, color = Color(0xFF334155))
                    Text("التوقيع / ....................", fontSize = 8.sp, color = Color(0xFF64748B))
                }

                // Left: Directorate Manager
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("مدير فرع الأحوال بالمديرية", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("الاسم / ....................", fontSize = 8.5.sp, color = Color(0xFF334155))
                    Text("التوقيع / ....................", fontSize = 8.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}

@Composable
fun DualHalfCell(
    slotNumber: Int,
    transaction: TransactionEntity?,
    onClick: () -> Unit
) {
    val isFilled = transaction != null

    Row(
        modifier = Modifier
            .width(306.dp)
            .clickable { onClick() }
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = transaction?.recordNumber?.ifBlank { "" } ?: "------",
            fontSize = 8.5.sp,
            fontWeight = if (isFilled) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isFilled) InfoBlue else Color(0xFFCBD5E1),
            modifier = Modifier.width(48.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = transaction?.formNumber?.ifBlank { "" } ?: "------",
            fontSize = 9.sp,
            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
            color = if (isFilled) Color(0xFF0F172A) else Color(0xFFCBD5E1),
            modifier = Modifier.width(58.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = transaction?.citizenName ?: "خانة فارغة (اضغط للتسجيل)",
            fontSize = 9.5.sp,
            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
            color = if (isFilled) Color(0xFF0F172A) else Color(0xFF94A3B8),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(135.dp),
            textAlign = TextAlign.Right
        )

        Text(
            text = transaction?.timeString ?: "✍️",
            fontSize = 7.5.sp,
            color = if (isFilled) Color(0xFF64748B) else Color(0xFF94A3B8),
            modifier = Modifier.width(55.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "$slotNumber",
            fontSize = 8.5.sp,
            fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
            color = if (isFilled) NavyPrimary else Color(0xFF94A3B8),
            modifier = Modifier.width(18.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SingleFocusedSlotRow(
    slotNumber: Int,
    transaction: TransactionEntity?,
    onRowClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isFilled = transaction != null
    val isEven = slotNumber % 2 == 0

    Surface(
        color = if (isFilled) (if (isEven) Color(0xFFF8FAFC) else Color.White) else Color(0xFFFAFAFA),
        border = BorderStroke(0.5.dp, Color(0xFFCBD5E1)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .testTag("paper_slot_$slotNumber")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$slotNumber",
                fontSize = 9.sp,
                fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
                color = if (isFilled) NavyPrimary else Color(0xFF94A3B8),
                modifier = Modifier.width(22.dp),
                textAlign = TextAlign.Center
            )

            if (isFilled) {
                Text(
                    text = transaction!!.recordNumber.ifBlank { "-" },
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InfoBlue,
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = transaction.formNumber.ifBlank { "-" },
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = transaction.citizenName,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Right
                )

                Row(
                    modifier = Modifier.width(65.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.timeString,
                        fontSize = 8.sp,
                        color = Color(0xFF64748B)
                    )
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = ExpenseRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "-------",
                    fontSize = 8.5.sp,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.width(50.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "-------------",
                    fontSize = 8.5.sp,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.width(60.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "خانة فارغة (اضغط للتسجيل)",
                    fontSize = 9.5.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Right
                )

                Text(
                    text = "✍️",
                    fontSize = 9.sp,
                    modifier = Modifier.width(65.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
