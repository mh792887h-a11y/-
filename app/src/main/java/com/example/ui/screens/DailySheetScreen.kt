package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.entity.TransactionEntity
import com.example.ui.CivilFundViewModel
import com.example.ui.dialogs.AppDatePickerDialog
import com.example.ui.theme.ExpenseRed
import com.example.util.HijriDateUtil
import com.example.util.PrintAndExportUtil
import kotlin.math.ceil
import kotlin.math.max

// Official sheet has 50 entries per page (25 rows on Right half, 25 rows on Left half)
private const val ROWS_PER_PAGE = 50
private const val HALF_ROWS = 25

// Exact color palette matching the official scanned movement sheet image
private val TableHeaderOlive = Color(0xFFAECF9B)
private val GridBorderBlack = Color(0xFF000000)
private val NavyBrand = Color(0xFF0F385A)

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

    // In-Sheet Fast Direct Input States
    var citizenNameInput by remember { mutableStateOf("") }
    var formNumberInput by remember { mutableStateOf("") }
    var recordNumberInput by remember { mutableStateOf("") }
    var genderInput by remember { mutableStateOf("ذكر") }
    var isSubmitting by remember { mutableStateOf(false) }
    var lastSubmittedMessage by remember { mutableStateOf<String?>(null) }

    // Next slot tracker
    val nextFreeSlotNumber = remember(categoryTransactions.size) {
        categoryTransactions.size + 1
    }
    var targetSlotNumber by remember(nextFreeSlotNumber) {
        mutableIntStateOf(nextFreeSlotNumber)
    }

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
        } else {
            if (formNumberInput.isBlank()) formNumberInput = "1001"
            if (recordNumberInput.isBlank()) recordNumberInput = "501"
        }
        targetSlotNumber = categoryTransactions.size + 1
    }

    // Dialog state for edit, delete, and date picking
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var txToEditState by remember { mutableStateOf<TransactionEntity?>(null) }
    var txToDeleteState by remember { mutableStateOf<TransactionEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0))
            .testTag("official_daily_sheet_screen"),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ==========================================
        // SECTION 1: TOP CONTROL BAR (أوراق المعاملات والطباعة)
        // ==========================================
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = NavyBrand),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Row 1: Title & Print Button
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
                                text = "الورقة الرسمية المعتمدة (25 يمين + 25 يسار)",
                                fontSize = 11.sp,
                                color = Color(0xFFBAE6FD)
                            )
                        }

                        Button(
                            onClick = {
                                PrintAndExportUtil.printOfficialMovementSheet(
                                    context = context,
                                    formType = selectedFormType,
                                    gregorianDate = selectedDate,
                                    hijriDate = hijriDate,
                                    pageNumber = currentPageIndex,
                                    totalPages = max(calculatedTotalPages, currentPageIndex),
                                    pageTransactions = currentSheetTransactions,
                                    directorateName = directorateName,
                                    cashierName = currentUser?.fullName ?: "أمين الصندوق"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TableHeaderOlive),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("print_official_sheet_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة A4 الورقة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 2: Category Tabs [جديد] [تجديد] [بدل فاقد] [بدل تالف]
                    val categories = listOf(
                        Triple("جديد", "جديد", Color(0xFF15803D)),
                        Triple("تجديد", "تجديد", Color(0xFF0284C7)),
                        Triple("بدل فاقد", "فاقد", Color(0xFFD97706)),
                        Triple("بدل تالف", "تالف", Color(0xFF7C3AED))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        categories.forEach { (typeKey, shortTitle, badgeColor) ->
                            val isSelected = selectedFormType == typeKey
                            val count = allDayTransactions.count { it.transactionType == typeKey && it.status == "ACTIVE" }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedFormType = typeKey
                                        currentPageIndex = 1
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = shortTitle,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) NavyBrand else Color.White
                                    )
                                    Text(
                                        text = "($count)",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) badgeColor else Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Row 3: Page Navigation & Date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📄 ورقة $currentPageIndex من $calculatedTotalPages",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (currentPageIndex > 1) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable { currentPageIndex-- }
                                ) {
                                    Text("السابقة", fontSize = 9.5.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            if (currentPageIndex < calculatedTotalPages) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable { currentPageIndex++ }
                                ) {
                                    Text("التالية", fontSize = 9.5.sp, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.clickable {
                                    currentPageIndex = calculatedTotalPages + 1
                                    targetSlotNumber = (currentPageIndex - 1) * ROWS_PER_PAGE + 1
                                }
                            ) {
                                Text("+ فتح ورقة", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }

                        OutlinedButton(
                            onClick = { showDatePickerDialog = true },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "$formattedGregorianDate", fontSize = 9.5.sp)
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 2: FAST DIRECT IN-SHEET ENTRY BAR
        // ==========================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("in_sheet_registration_box"),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8F2)),
                border = BorderStroke(1.5.dp, Color(0xFF4A7C59)),
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
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تدوين في ورقة ( $selectedFormType ) - السطر المحدد: #$targetSlotNumber",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        if (lastSubmittedMessage != null) {
                            Text(
                                text = lastSubmittedMessage ?: "",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = citizenNameInput,
                            onValueChange = { citizenNameInput = it },
                            label = { Text("الاسم الرباعي", fontSize = 10.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(2f)
                                .testTag("in_sheet_citizen_input"),
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFF2E7D32),
                                unfocusedBorderColor = Color(0xFF81C784)
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )

                        OutlinedTextField(
                            value = formNumberInput,
                            onValueChange = { formNumberInput = it },
                            label = { Text("رقم الاستمارة", fontSize = 9.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            modifier = Modifier
                                .weight(1.1f)
                                .testTag("in_sheet_form_num"),
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = recordNumberInput,
                            onValueChange = { recordNumberInput = it },
                            label = { Text("رقم القيد", fontSize = 9.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("in_sheet_record_num"),
                            shape = RoundedCornerShape(6.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )

                        val canSave = citizenNameInput.isNotBlank() && !isSubmitting
                        Button(
                            onClick = {
                                if (!canSave) return@Button
                                isSubmitting = true

                                val nameToSave = citizenNameInput.trim()
                                val fNumToSave = formNumberInput.trim().ifBlank { "-" }
                                val rNumToSave = recordNumberInput.trim().ifBlank { "-" }

                                viewModel.sellForm(
                                    citizenName = nameToSave,
                                    formNumber = fNumToSave,
                                    recordNumber = rNumToSave,
                                    formTypeNameArabic = selectedFormType,
                                    gender = genderInput,
                                    notes = null,
                                    customGregorianDate = if (!isToday) selectedDate else null,
                                    customHijriDate = null,
                                    showReceipt = false
                                )

                                lastSubmittedMessage = "تم تدوين #$targetSlotNumber بنجاح! ✓"
                                citizenNameInput = ""

                                val currentFormLong = fNumToSave.toLongOrNull()
                                if (currentFormLong != null) {
                                    formNumberInput = (currentFormLong + 1).toString()
                                }
                                val currentRecLong = rNumToSave.toLongOrNull()
                                if (currentRecLong != null) {
                                    recordNumberInput = (currentRecLong + 1).toString()
                                }

                                targetSlotNumber++
                                if (targetSlotNumber > currentPageIndex * ROWS_PER_PAGE) {
                                    currentPageIndex = ceil(targetSlotNumber.toDouble() / ROWS_PER_PAGE).toInt()
                                }

                                isSubmitting = false
                            },
                            enabled = canSave,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Text("تدوين ✍️", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 3: THE EXACT OFFICIAL GOVERNMENT PAPER AS SHOWN IN THE IMAGE
        // ==========================================
        item {
            ExactScannedPaperSheetView(
                formType = selectedFormType,
                directorateName = directorateName,
                gregorianDate = selectedDate,
                hijriDate = hijriDate,
                pageNumber = currentPageIndex,
                totalPages = max(calculatedTotalPages, currentPageIndex),
                transactions = currentSheetTransactions,
                selectedSlot = targetSlotNumber,
                cashierName = currentUser?.fullName ?: "أمين الصندوق",
                onSlotSelected = { slotNum ->
                    targetSlotNumber = slotNum
                },
                onRowEdit = { tx ->
                    txToEditState = tx
                },
                onRowDelete = { tx ->
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

    // Safe Edit Transaction Dialog
    if (txToEditState != null) {
        val tx = txToEditState!!
        var editName by remember { mutableStateOf(tx.citizenName) }
        var editFormNum by remember { mutableStateOf(tx.formNumber) }
        var editRecordNum by remember { mutableStateOf(tx.recordNumber) }

        AlertDialog(
            onDismissRequest = { txToEditState = null },
            title = {
                Text(
                    text = "تعديل بيانات الاستمارة #${tx.formNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("اسم المواطن الرباعي") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editRecordNum,
                            onValueChange = { editRecordNum = it },
                            label = { Text("رقم القيد") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = editFormNum,
                            onValueChange = { editFormNum = it },
                            label = { Text("رقم الاستمارة") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTransaction(
                            transactionId = tx.id,
                            citizenName = editName.trim(),
                            formNumber = editFormNum.trim(),
                            recordNumber = editRecordNum.trim(),
                            transactionType = tx.transactionType,
                            gender = tx.gender,
                            notes = tx.notes,
                            gregorianDate = tx.gregorianDate,
                            hijriDate = tx.hijriDate
                        )
                        txToEditState = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NavyBrand)
                ) {
                    Text("حفظ التعديل")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToEditState = null }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Safe Delete Transaction Dialog
    if (txToDeleteState != null) {
        val tx = txToDeleteState!!
        AlertDialog(
            onDismissRequest = { txToDeleteState = null },
            title = {
                Text(
                    text = "حذف الاستمارة من الكشف",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ExpenseRed
                )
            },
            text = {
                Text(
                    text = "هل تريد حذف استمارة (${tx.citizenName}) من الورقة؟ سيتم إلغاؤها من الصندوق فوراً.",
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx.id)
                        txToDeleteState = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("تأكيد الحذف", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDeleteState = null }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * EXACT Visual Component of the Official Movement Sheet Paper as shown in the user's uploaded screenshot:
 * 1) Top Header: Calligraphy "الجمهورية اليمنية" + Basmala + Eagle + Seal & Metadata
 * 2) Title: "كشف الحركة اليومي للاستمارات الشخصية ( ......... ) مديرية ........."
 * 3) Olive Table Header: 10 Columns (5 right + 5 left)
 * 4) 25 Rows (Right: 1-25, Left: 26-50)
 * 5) Official 3-Column Footer: أمين الصندوق | الفني المختص | مدير فرع الأحوال
 */
@Composable
fun ExactScannedPaperSheetView(
    formType: String,
    directorateName: String,
    gregorianDate: String,
    hijriDate: String,
    pageNumber: Int,
    totalPages: Int,
    transactions: List<TransactionEntity>,
    selectedSlot: Int,
    cashierName: String,
    onSlotSelected: (Int) -> Unit,
    onRowEdit: (TransactionEntity) -> Unit,
    onRowDelete: (TransactionEntity) -> Unit
) {
    val baseIndex = (pageNumber - 1) * ROWS_PER_PAGE

    // View toggle: [عرض كامل للورقة ↔] | [النصف الأيمن 1-25] | [النصف الأيسر 26-50]
    var viewMode by remember { mutableStateOf("FULL") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(2.dp))
            .testTag("exact_scanned_paper_container"),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, GridBorderBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            // View Mode Selector for Easy Viewing
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (viewMode == "FULL") Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, Color.Gray),
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { viewMode = "FULL" }
                ) {
                    Text(
                        text = "عرض الورقة كاملة ↔",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewMode == "FULL") Color.White else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (viewMode == "RIGHT") Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, Color.Gray),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewMode = "RIGHT" }
                ) {
                    Text(
                        text = "النصف الأيمن (1-25)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewMode == "RIGHT") Color.White else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (viewMode == "LEFT") Color(0xFF1E293B) else Color(0xFFF1F5F9),
                    border = BorderStroke(0.5.dp, Color.Gray),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewMode = "LEFT" }
                ) {
                    Text(
                        text = "النصف الأيسر (26-50)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewMode == "LEFT") Color.White else Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 3.dp)
                    )
                }
            }

            // ==========================================
            // 1. OFFICIAL PAPER HEADER (الترويسة كما في الصورة المرفقة)
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Right: Calligraphy Republic & Ministry
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "الجمهورية اليمنية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "وزارة الداخلية",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "مصلحة الأحوال المدنية والسجل المدني",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                // Center: Basmala + Eagle Coat of Arms
                Column(
                    modifier = Modifier.weight(1.1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "بسم الله الرحمن الرحيم",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_yemen_eagle),
                        contentDescription = "شعار الجمهورية اليمنية",
                        modifier = Modifier
                            .height(30.dp)
                            .width(55.dp)
                    )
                }

                // Left: Seal + Dotted Metadata
                Row(
                    modifier = Modifier.weight(1.2f),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Top
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_civil_status_seal),
                        contentDescription = "ختم الأحوال المدنية",
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 4.dp)
                    )
                    Column {
                        Text(text = "الرقــــــم : .................", fontSize = 8.sp, color = Color.Black)
                        Text(text = "التاريـــــخ : $gregorianDate م", fontSize = 8.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(text = "الموافــــق : $hijriDate", fontSize = 7.5.sp, color = Color.Black)
                        Text(text = "المرفقـــات : .................", fontSize = 8.sp, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // ==========================================
            // 2. MAIN DOCUMENT TITLE (العنوان كما في الصورة)
            // ==========================================
            Text(
                text = "كشف الحركة اليومي للاستمارات الشخصية ( $formType ) مديرية $directorateName",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            // ==========================================
            // 3. TABLE VIEW (10 أعمدة كما في الصورة تماماً)
            // ==========================================
            if (viewMode == "FULL") {
                // Wide side-by-side view with smooth horizontal scrolling
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    Column(modifier = Modifier.width(620.dp)) {
                        // Table Header Row with Olive-Green Background (#AECF9B)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(TableHeaderOlive)
                                .border(BorderStroke(1.dp, GridBorderBlack)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // RIGHT HALF HEADERS (Columns 1 to 5)
                            TableHeaderCell(text = "رقم القيد\nالتسلسلي", width = 48.dp)
                            TableHeaderCell(text = "رقم\nالاستمارة", width = 52.dp)
                            TableHeaderCell(text = "الاســـــــــــــــــم", width = 135.dp)
                            TableHeaderCell(text = "تاريخ القيد /\nتوقيع الفني المختص", width = 55.dp)
                            TableHeaderCell(text = "م", width = 20.dp)

                            // LEFT HALF HEADERS (Columns 6 to 10)
                            TableHeaderCell(text = "رقم القيد\nالتسلسلي", width = 48.dp)
                            TableHeaderCell(text = "رقم\nالاستمارة", width = 52.dp)
                            TableHeaderCell(text = "الاســـــــــــــــــم", width = 135.dp)
                            TableHeaderCell(text = "تاريخ القيد /\nتوقيع الفني المختص", width = 55.dp)
                            TableHeaderCell(text = "م", width = 20.dp)
                        }

                        // 25 ROWS (Right: 1..25 | Left: 26..50)
                        for (r in 0 until HALF_ROWS) {
                            val rightSlot = baseIndex + r + 1
                            val rightTx = transactions.getOrNull(r)

                            val leftSlot = baseIndex + HALF_ROWS + r + 1
                            val leftTx = transactions.getOrNull(HALF_ROWS + r)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .border(BorderStroke(0.5.dp, GridBorderBlack)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Right Half Slot (1..25)
                                ScannedHalfRowCells(
                                    slotNumber = rightSlot,
                                    transaction = rightTx,
                                    isSelected = selectedSlot == rightSlot,
                                    onClick = {
                                        if (rightTx != null) onRowEdit(rightTx) else onSlotSelected(rightSlot)
                                    }
                                )

                                // Left Half Slot (26..50)
                                ScannedHalfRowCells(
                                    slotNumber = leftSlot,
                                    transaction = leftTx,
                                    isSelected = selectedSlot == leftSlot,
                                    onClick = {
                                        if (leftTx != null) onRowEdit(leftTx) else onSlotSelected(leftSlot)
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Focused Single Column Set View (Right 1-25 or Left 26-50)
                val startRow = if (viewMode == "RIGHT") 0 else HALF_ROWS
                val endRow = if (viewMode == "RIGHT") HALF_ROWS else ROWS_PER_PAGE

                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TableHeaderOlive)
                            .border(BorderStroke(1.dp, GridBorderBlack)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableHeaderCell(text = "رقم القيد\nالتسلسلي", width = 55.dp)
                        TableHeaderCell(text = "رقم\nالاستمارة", width = 58.dp)
                        TableHeaderCell(text = "الاســـــــــــــــــم", width = 0.dp, weight = 1f)
                        TableHeaderCell(text = "تاريخ القيد /\nتوقيع الفني", width = 60.dp)
                        TableHeaderCell(text = "م", width = 24.dp)
                    }

                    for (i in startRow until endRow) {
                        val slotNum = baseIndex + i + 1
                        val tx = transactions.getOrNull(i)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (selectedSlot == slotNum) Color(0xFFDCFCE7) else Color.White)
                                .border(BorderStroke(0.5.dp, GridBorderBlack))
                                .clickable {
                                    if (tx != null) onRowEdit(tx) else onSlotSelected(slotNum)
                                }
                                .padding(vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tx?.recordNumber?.ifBlank { "" } ?: "",
                                fontSize = 9.sp,
                                modifier = Modifier
                                    .width(55.dp)
                                    .border(BorderStroke(0.5.dp, GridBorderBlack))
                                    .padding(vertical = 3.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = tx?.formNumber?.ifBlank { "" } ?: "",
                                fontSize = 9.sp,
                                modifier = Modifier
                                    .width(58.dp)
                                    .border(BorderStroke(0.5.dp, GridBorderBlack))
                                    .padding(vertical = 3.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = tx?.citizenName ?: "",
                                fontSize = 9.5.sp,
                                fontWeight = if (tx != null) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .weight(1f)
                                    .border(BorderStroke(0.5.dp, GridBorderBlack))
                                    .padding(vertical = 3.dp, horizontal = 4.dp),
                                textAlign = TextAlign.Right
                            )
                            Text(
                                text = tx?.timeString ?: "",
                                fontSize = 8.sp,
                                modifier = Modifier
                                    .width(60.dp)
                                    .border(BorderStroke(0.5.dp, GridBorderBlack))
                                    .padding(vertical = 3.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "$slotNum",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .width(24.dp)
                                    .border(BorderStroke(0.5.dp, GridBorderBlack))
                                    .padding(vertical = 3.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ==========================================
            // 4. FOOTER SIGNATURES (التوقيعات الثلاثة كما في الصورة المرفقة تماماً)
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Right: أمين الصندوق
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "أمين الصندوق", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "الاسم / $cashierName", fontSize = 9.sp, color = Color.Black)
                    Text(text = "التوقيع /", fontSize = 9.sp, color = Color.Black)
                }

                // Center: الفني المختص باستلام البطائق وطباعتها
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text(
                        text = "الفني المختص باستلام البطائق وطباعتها",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "الاسم /", fontSize = 9.sp, color = Color.Black)
                    Text(text = "التوقيع /", fontSize = 9.sp, color = Color.Black)
                }

                // Left: مدير فرع الأحوال المدنية بمديرية
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = "مدير فرع الأحوال المدنية بمديرية", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "الاسم /", fontSize = 9.sp, color = Color.Black)
                    Text(text = "التوقيع /", fontSize = 9.sp, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    weight: Float? = null
) {
    val mod = if (weight != null) {
        Modifier
            .border(BorderStroke(0.6.dp, GridBorderBlack))
            .padding(vertical = 4.dp, horizontal = 1.dp)
    } else {
        Modifier
            .width(width)
            .border(BorderStroke(0.6.dp, GridBorderBlack))
            .padding(vertical = 4.dp, horizontal = 1.dp)
    }

    Text(
        text = text,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        textAlign = TextAlign.Center,
        lineHeight = 10.sp,
        modifier = mod
    )
}

@Composable
fun ScannedHalfRowCells(
    slotNumber: Int,
    transaction: TransactionEntity?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFFDCFCE7) else Color.Transparent

    Row(
        modifier = Modifier
            .width(310.dp)
            .background(bg)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. رقم القيد التسلسلي
        Text(
            text = transaction?.recordNumber?.ifBlank { "" } ?: "",
            fontSize = 8.5.sp,
            modifier = Modifier
                .width(48.dp)
                .border(BorderStroke(0.5.dp, GridBorderBlack))
                .padding(vertical = 2.dp),
            textAlign = TextAlign.Center
        )

        // 2. رقم الاستمارة
        Text(
            text = transaction?.formNumber?.ifBlank { "" } ?: "",
            fontSize = 8.5.sp,
            modifier = Modifier
                .width(52.dp)
                .border(BorderStroke(0.5.dp, GridBorderBlack))
                .padding(vertical = 2.dp),
            textAlign = TextAlign.Center
        )

        // 3. الاســـــــــم
        Text(
            text = transaction?.citizenName ?: "",
            fontSize = 9.sp,
            fontWeight = if (transaction != null) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(135.dp)
                .border(BorderStroke(0.5.dp, GridBorderBlack))
                .padding(vertical = 2.dp, horizontal = 3.dp),
            textAlign = TextAlign.Right
        )

        // 4. تاريخ القيد / توقيع الفني المختص
        Text(
            text = transaction?.timeString ?: "",
            fontSize = 7.5.sp,
            modifier = Modifier
                .width(55.dp)
                .border(BorderStroke(0.5.dp, GridBorderBlack))
                .padding(vertical = 2.dp),
            textAlign = TextAlign.Center
        )

        // 5. م (Sequential number)
        Text(
            text = "$slotNumber",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(20.dp)
                .border(BorderStroke(0.5.dp, GridBorderBlack))
                .padding(vertical = 2.dp),
            textAlign = TextAlign.Center
        )
    }
}
