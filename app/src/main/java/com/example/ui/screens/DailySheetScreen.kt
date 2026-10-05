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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.dialogs.AppDatePickerDialog
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.util.CurrencyUtil
import com.example.util.HijriDateUtil
import com.example.util.PrintAndExportUtil
import kotlin.math.ceil
import kotlin.math.max

private const val ROWS_PER_PAGE = 30 // 15 on right column side, 15 on left column side

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySheetScreen(
    viewModel: CivilFundViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedDate by viewModel.selectedReportDate.collectAsState()
    val allDates by viewModel.allTransactionDates.collectAsState()
    val allDayTransactions by viewModel.selectedDateTransactions.collectAsState()
    val closing by viewModel.selectedDateClosing.collectAsState()
    val directorateName by viewModel.directorateName.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // 1. Category Paper Selection: "جديد", "تجديد", "بدل فاقد", "بدل تالف"
    var selectedFormType by remember { mutableStateOf("جديد") }

    // Filter transactions by the selected category/type
    val categoryTransactions = remember(allDayTransactions, selectedFormType) {
        allDayTransactions
            .filter { it.transactionType == selectedFormType && it.status == "ACTIVE" }
            .sortedBy { it.id }
    }

    // 2. Pagination for the selected category paper sheet
    // Calculate total pages for this category (at least 1 page)
    val calculatedTotalPages = max(1, ceil(categoryTransactions.size.toDouble() / ROWS_PER_PAGE).toInt())
    var currentPageIndex by remember(selectedFormType, selectedDate) { mutableIntStateOf(1) }

    // Ensure currentPageIndex stays within bounds
    LaunchedEffect(calculatedTotalPages) {
        if (currentPageIndex > calculatedTotalPages) {
            currentPageIndex = calculatedTotalPages
        }
    }

    // Slice transactions for the current page (up to 30 items)
    val pageStartIndex = (currentPageIndex - 1) * ROWS_PER_PAGE
    val currentSheetTransactions = remember(categoryTransactions, currentPageIndex) {
        categoryTransactions.drop(pageStartIndex).take(ROWS_PER_PAGE)
    }

    // Date info for display
    val hijriDate = remember(selectedDate) {
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
    val citizenNameFocusRequester = remember { FocusRequester() }

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

    // Dialog state for slot click or date pick
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var txToEditState by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDeleteState by remember { mutableStateOf<TransactionEntity?>(null) }
    var targetSlotIndexForDirectEntry by remember { mutableStateOf<Int?>(null) }

    // Summary calculations
    val totalCategoryFormsCount = categoryTransactions.size
    val currentSheetCount = currentSheetTransactions.size
    val totalCategoryFundShare = categoryTransactions.sumOf { it.fundShare }
    val totalCategoryDirectorShare = categoryTransactions.sumOf { it.directorateShare }
    val totalCategoryRevenue = categoryTransactions.sumOf { it.salePrice }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("official_daily_sheet_screen"),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ==========================================
        // SECTION 1: TOP HEADER & ACTIONS
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "كشف الحركة اليومي للاستمارات الشخصية",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }
                            Text(
                                text = "النموذج الرسمي المعتمد لمصلحة الأحوال المدنية والسجل المدني",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
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
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("print_official_sheet_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة A4 الرسمية", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Date & Directorate Info with Calendar switcher
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$formattedGregorianDate (الموافق: $hijriDate)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = "فرع مديرية: $directorateName",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        OutlinedButton(
                            onClick = { showDatePickerDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تغيير اليوم", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 2: CATEGORY SELECTOR (ورقة مستقلة لكل نوع)
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "اختر نوع الاستمارة لعرض وتسجيل ورقة كشفها المستقلة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf(
                        Triple("جديد", "ورقة استمارات جديد", Color(0xFF059669)),
                        Triple("تجديد", "ورقة استمارات تجديد", Color(0xFF0284C7)),
                        Triple("بدل فاقد", "ورقة بدل فاقد", Color(0xFFD97706)),
                        Triple("بدل تالف", "ورقة بدل تالف", Color(0xFF7C3AED))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { (typeKey, title, typeColor) ->
                            val isSelected = selectedFormType == typeKey
                            val countForThisType = allDayTransactions.count { it.transactionType == typeKey && it.status == "ACTIVE" }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
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
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = typeKey,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) Color.White.copy(alpha = 0.25f) else Color(0xFFE2E8F0)
                                    ) {
                                        Text(
                                            text = "$countForThisType استمارة",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0xFF475569),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 3: IN-SHEET FAST REGISTRATION (التسجيل المباشر داخل الورقة)
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("in_sheet_registration_box"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = IncomeGreen,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FlashOn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تسجيل فوري في ورقة ( $selectedFormType )",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }

                        // Current Sheet Slot Tracker
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "السطر التالي: #${totalCategoryFormsCount + 1}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

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
                            .focusRequester(citizenNameFocusRequester)
                            .testTag("in_sheet_citizen_input"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = IncomeGreen,
                            unfocusedBorderColor = Color(0xFF86EFAC),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Form Number & Record Number & Gender in one compact row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = formNumberInput,
                            onValueChange = { formNumberInput = it },
                            label = { Text("رقم الاستمارة") },
                            placeholder = { Text("مثال: 1001") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("in_sheet_form_num"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = IncomeGreen,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        OutlinedTextField(
                            value = recordNumberInput,
                            onValueChange = { recordNumberInput = it },
                            label = { Text("رقم القيد") },
                            placeholder = { Text("مثال: 501") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("in_sheet_record_num"),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = IncomeGreen,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        // Gender Toggle Pill
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (genderInput == "ذكر") Color(0xFFEFF6FF) else Color(0xFFFDF2F8),
                            border = BorderStroke(1.dp, if (genderInput == "ذكر") Color(0xFF93C5FD) else Color(0xFFF472B6)),
                            modifier = Modifier
                                .height(54.dp)
                                .clickable { genderInput = if (genderInput == "ذكر") "أنثى" else "ذكر" }
                                .padding(horizontal = 4.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp)) {
                                Text(
                                    text = if (genderInput == "ذكر") "👨 ذكر" else "👩 أنثى",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (genderInput == "ذكر") Color(0xFF1D4ED8) else Color(0xFFBE185D)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Submit directly into sheet button
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

                            // Clear name, auto-increment numbers for next citizen
                            citizenNameInput = ""
                            val currentFormLong = formNumberInput.trim().toLongOrNull()
                            if (currentFormLong != null) {
                                formNumberInput = (currentFormLong + 1).toString()
                            }
                            val currentRecLong = recordNumberInput.trim().toLongOrNull()
                            if (currentRecLong != null) {
                                recordNumberInput = (currentRecLong + 1).toString()
                            }

                            // If current sheet just reached capacity, jump to next page
                            if ((totalCategoryFormsCount + 1) > currentPageIndex * ROWS_PER_PAGE) {
                                currentPageIndex++
                            }

                            isSubmitting = false
                        },
                        enabled = canSave,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("in_sheet_submit_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تسجيل وحفظ في الورقة فوراً ⚡ (السطر #${totalCategoryFormsCount + 1})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 4: PAPER SHEET PAGER & CONTROLS (مراجعة الأوراق السابقة والجديدة)
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
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
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أوراق كشف ($selectedFormType) - مراجعة وتنقل:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }

                        // Open New Sheet manually button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                            modifier = Modifier.clickable {
                                currentPageIndex = calculatedTotalPages + 1
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.NoteAdd, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فتح ورقة جديدة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Pager Chips: [ ورقة 1 ] [ ورقة 2 ] [ ورقة 3 ] ...
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val maxPagesToShow = max(calculatedTotalPages, currentPageIndex)
                        items(maxPagesToShow) { pageIdx ->
                            val pageNum = pageIdx + 1
                            val isSelected = pageNum == currentPageIndex
                            val startItem = (pageNum - 1) * ROWS_PER_PAGE + 1
                            val endItem = pageNum * ROWS_PER_PAGE

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NavyPrimary else Color(0xFFF8FAFC),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .clickable { currentPageIndex = pageNum }
                                    .testTag("sheet_page_chip_$pageNum")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📄 ورقة رقم $pageNum ($startItem-$endItem)",
                                        fontSize = 11.sp,
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
        // SECTION 5: THE OFFICIAL PAPER SHEET RENDERING (ورقة الكشف الرسمية المطابقة)
        // ==========================================
        item {
            OfficialPaperSheetView(
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
                    targetSlotIndexForDirectEntry = slotNumber
                    citizenNameFocusRequester.requestFocus()
                },
                onDeleteClick = { tx ->
                    txToDeleteState = tx
                }
            )
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
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تعديل بيانات الاستمارة #${tx.formNumber}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NavyPrimary
                        )
                        IconButton(onClick = { txToEditState = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("اسم المواطن الرباعي") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editFormNum,
                            onValueChange = { editFormNum = it },
                            label = { Text("رقم الاستمارة") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = editRecordNum,
                            onValueChange = { editRecordNum = it },
                            label = { Text("رقم القيد") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

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
                        shape = RoundedCornerShape(10.dp),
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
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("حذف استمارة المواطن: ${tx.citizenName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("سيتم حذف هذه الاستمارة من كشف الورقة وتعديل الصندوق والجرد فوراً.", fontSize = 12.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))
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
 * Renders the Official Government Paper Sheet (كشف الحركة اليومي للاستمارات الشخصية)
 * Symmetrically structured in dual columns with 15 rows on each side (Total 30 slots per sheet).
 */
@Composable
fun OfficialPaperSheetView(
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
    val halfSize = 15 // 15 rows per column side = 30 rows per sheet
    val baseIndex = (pageNumber - 1) * ROWS_PER_PAGE

    val rightTransactions = transactions.take(halfSize)
    val leftTransactions = transactions.drop(halfSize).take(halfSize)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .testTag("official_paper_canvas"),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCFD)),
        border = BorderStroke(2.dp, Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // ==========================================
            // PAPER HEADER: Coat of arms, Republic, Ministry, Date
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Right: Ministry & Authority
                Column(modifier = Modifier.weight(1.1f)) {
                    Text("الجمهورية اليمنية", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("وزارة الداخلية", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text("مصلحة الأحوال المدنية والسجل المدني", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text("فرع مديرية: $directorateName", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }

                // Center: Emblem & Basmala
                Column(
                    modifier = Modifier.weight(1.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("بسم الله الرحمن الرحيم", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                    Text("🦅", fontSize = 20.sp)
                    Text("شعار الجمهورية اليمنية", fontSize = 8.sp, color = Color(0xFF64748B))
                }

                // Left: Metadata (Date, Number, Sheet No)
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("الرقم: .............", fontSize = 9.5.sp, color = Color(0xFF334155))
                    Text("التاريخ: $gregorianDate م", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("الموافق: $hijriDate", fontSize = 9.sp, color = Color(0xFF0369A1))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NavyPrimary.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, NavyPrimary.copy(alpha = 0.3f)),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "ورقة رقم: $pageNumber من $totalPages",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Paper Title Banner
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "كشف الحركة اليومي للاستمارات الشخصية ( $formType ) بمديـرية $directorateName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // OFFICIAL DUAL-SIDED TABLE
            // In Mobile view, we present the rows clearly formatted as dual lists or sequential dual half-tables
            // ==========================================
            Text(
                text = "📋 جدول خانات الورقة (30 خانة رسمية):",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Table Header Bar
            Surface(
                color = Color(0xFFE2E8F0),
                border = BorderStroke(1.dp, Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "م", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(26.dp), textAlign = TextAlign.Center)
                    Text(text = "رقم الاستمارة", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(68.dp), textAlign = TextAlign.Center)
                    Text(text = "رقم القيد", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                    Text(text = "الإســــــــــــــــــــم", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Right)
                    Text(text = "التوقيع/الإجراء", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(75.dp), textAlign = TextAlign.Center)
                }
            }

            // Render all 30 slots (Filled or Dotted Empty)
            for (i in 0 until ROWS_PER_PAGE) {
                val slotNumber = baseIndex + i + 1
                val tx = transactions.getOrNull(i)

                PaperSlotRow(
                    slotNumber = slotNumber,
                    transaction = tx,
                    onRowClick = { if (tx != null) onRowClick(tx) else onEmptySlotClick(slotNumber) },
                    onDeleteClick = { if (tx != null) onDeleteClick(tx) }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFF0F172A), thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // OFFICIAL FOOTER SIGNATURES
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Cashier
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("أمين الصندوق", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("الاسم: $cashierName", fontSize = 9.sp, color = Color(0xFF334155))
                    Text("التوقيع: .....................", fontSize = 9.sp, color = Color(0xFF64748B))
                }

                // Tech Specialist
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("الفني المختص", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("الاسم: .....................", fontSize = 9.sp, color = Color(0xFF334155))
                    Text("التوقيع: .....................", fontSize = 9.sp, color = Color(0xFF64748B))
                }

                // Director
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("مدير فرع الأحوال", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text("الاسم: .....................", fontSize = 9.sp, color = Color(0xFF334155))
                    Text("التوقيع: .....................", fontSize = 9.sp, color = Color(0xFF64748B))
                }
            }
        }
    }
}

/**
 * Individual Row inside the Official Paper Sheet
 */
@Composable
fun PaperSlotRow(
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
                .padding(vertical = 5.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Slot Number (م)
            Text(
                text = "$slotNumber",
                fontSize = 10.sp,
                fontWeight = if (isFilled) FontWeight.Bold else FontWeight.Normal,
                color = if (isFilled) NavyPrimary else Color(0xFF94A3B8),
                modifier = Modifier.width(26.dp),
                textAlign = TextAlign.Center
            )

            if (isFilled) {
                // Form Number
                Text(
                    text = transaction!!.formNumber.ifBlank { "-" },
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.width(68.dp),
                    textAlign = TextAlign.Center
                )

                // Record Number
                Text(
                    text = transaction.recordNumber.ifBlank { "-" },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = InfoBlue,
                    modifier = Modifier.width(55.dp),
                    textAlign = TextAlign.Center
                )

                // Citizen Name
                Text(
                    text = transaction.citizenName,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Right
                )

                // Actions: Edit / Delete & Time
                Row(
                    modifier = Modifier.width(75.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.timeString,
                        fontSize = 8.5.sp,
                        color = Color(0xFF64748B)
                    )
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف",
                            tint = ExpenseRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            } else {
                // Empty Dotted Line Slot
                Text(
                    text = "----------------",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.width(68.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "-------",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.width(55.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "خانة فارغة (اضغط للكتابة هنا)",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Right
                )

                Text(
                    text = "✍️",
                    fontSize = 10.sp,
                    modifier = Modifier.width(75.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
