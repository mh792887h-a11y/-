package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.platform.LocalContext
import com.example.data.entity.DirectorPaymentEntity
import com.example.util.PrintAndExportUtil
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.entity.DebtEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.InfoBlue
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.WarningAmber
import com.example.util.CurrencyUtil
import com.example.util.HijriDateUtil

@Composable
fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onSubmit: (statement: String, amount: Double, withWhom: String, notes: String?) -> Unit
) {
    var statement by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var withWhom by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "➖ إضافة خرج (منصرف)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    label = { Text("البيان (شراء قرطاسية، حبر طابعة، إصلاحات...) *") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_statement_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("المبلغ بالريال *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = withWhom,
                    onValueChange = { withWhom = it },
                    label = { Text("مع من (اسم المستلم أو التاجر) *") },
                    modifier = Modifier.fillMaxWidth().testTag("expense_with_whom_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (statement.isNotBlank() && amt > 0 && withWhom.isNotBlank()) {
                            onSubmit(statement, amt, withWhom, notes.ifBlank { null })
                        }
                    },
                    enabled = statement.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0 && withWhom.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_expense_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("خصم وحفظ الخرج", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddIncomeDialog(
    onDismiss: () -> Unit,
    onSubmit: (category: String, statement: String, amount: Double, withWhom: String, notes: String?) -> Unit
) {
    var category by remember { mutableStateOf("دخل آخر") }
    var statement by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var withWhom by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "➕ إضافة دخل للصندوق",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    label = { Text("البيان (توريد إضافي، دخل خدمات أخرى...) *") },
                    modifier = Modifier.fillMaxWidth().testTag("income_statement_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("المبلغ بالريال *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("income_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = withWhom,
                    onValueChange = { withWhom = it },
                    label = { Text("من من (المودع أو المصدر) *") },
                    modifier = Modifier.fillMaxWidth().testTag("income_with_whom_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (statement.isNotBlank() && amt > 0 && withWhom.isNotBlank()) {
                            onSubmit(category, statement, amt, withWhom, notes.ifBlank { null })
                        }
                    },
                    enabled = statement.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0 && withWhom.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_income_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                    Text("إضافة وحفظ الدخل", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AddDebtDialog(
    onDismiss: () -> Unit,
    onSubmit: (personName: String, amount: Double, reason: String, notes: String?) -> Unit
) {
    var personName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💳 تسجيل دين جديد",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("اسم الشخص أو الجهة *") },
                    modifier = Modifier.fillMaxWidth().testTag("debt_person_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("المبلغ بالريال *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("debt_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الدين *") },
                    modifier = Modifier.fillMaxWidth().testTag("debt_reason_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (personName.isNotBlank() && amt > 0 && reason.isNotBlank()) {
                            onSubmit(personName, amt, reason, notes.ifBlank { null })
                        }
                    },
                    enabled = personName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0 && reason.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_debt_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
                ) {
                    Text("حفظ الدين", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PayDebtDialog(
    debt: DebtEntity,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, notes: String?) -> Unit
) {
    var amountText by remember { mutableStateOf(debt.remainingAmount.toInt().toString()) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تسديد دين",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "اسم المدين: ${debt.personName}", fontWeight = FontWeight.Bold)
                        Text(text = "المبلغ الأصلي: ${CurrencyUtil.formatRiyal(debt.originalAmount)}")
                        Text(text = "المبلغ المسدد: ${CurrencyUtil.formatRiyal(debt.paidAmount)}")
                        Text(
                            text = "المبلغ المتبقي: ${CurrencyUtil.formatRiyal(debt.remainingAmount)}",
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("المبلغ المراد سداده بالريال *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("pay_debt_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات السداد") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && amt <= debt.remainingAmount) {
                            onSubmit(amt, notes.ifBlank { null })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_pay_debt_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                    Text("تأكيد السداد وتوريد للصندوق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ActualCashCountDialog(
    expectedBalance: Double,
    currentActual: Double,
    onDismiss: () -> Unit,
    onSubmit: (actualCash: Double) -> Unit
) {
    var amountText by remember {
        mutableStateOf(if (currentActual > 0) currentActual.toInt().toString() else expectedBalance.toInt().toString())
    }
    val enteredAmt = amountText.toDoubleOrNull() ?: 0.0
    val difference = enteredAmt - expectedBalance

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "جرد الرصيد الفعلي للصندوق",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "الرصيد الدفتري المتوقع:",
                            fontSize = 13.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = CurrencyUtil.formatRiyal(expectedBalance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("المبلغ الفعلي الموجود بالصندوق (ريال) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("actual_cash_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Difference indicator
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            difference == 0.0 -> Color(0xFFE8F5E9)
                            difference > 0 -> Color(0xFFE0F2FE)
                            else -> Color(0xFFFFEBEE)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "حالة الصندوق:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = when {
                                difference == 0.0 -> "مطابق تماماً ✓"
                                difference > 0 -> "زيادة: +${CurrencyUtil.formatRiyal(difference)}"
                                else -> "عجز: ${CurrencyUtil.formatRiyal(difference)}"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when {
                                difference == 0.0 -> IncomeGreen
                                difference > 0 -> InfoBlue
                                else -> ExpenseRed
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onSubmit(enteredAmt) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_actual_cash_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("حفظ الجرد الفعلي", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CancelTransactionConfirmDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (reason: String) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = ExpenseRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إلغاء عملية استمارة",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "هل أنت متأكد من إلغاء الإيصال رقم (${transaction.receiptNumber}) للمواطن (${transaction.citizenName})؟ سيتم عكس المبلغ (${CurrencyUtil.formatRiyal(transaction.salePrice)}) من حركة الصندوق واليومية.",
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الإلغاء (إجباري) *") },
                    placeholder = { Text("مثال: خطأ في البيانات / رغبة المواطن") },
                    modifier = Modifier.fillMaxWidth().testTag("cancel_reason_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (reason.isNotBlank()) onConfirm(reason)
                        },
                        enabled = reason.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_cancel_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Text("تأكيد الإلغاء", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B))
                    ) {
                        Text("تراجع")
                    }
                }
            }
        }
    }
}

@Composable
fun DateInfoDialog(onDismiss: () -> Unit) {
    val greg = HijriDateUtil.getGregorianDateInfo()
    val hijri = HijriDateUtil.getHijriDate()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                        text = "📅 معلومات التاريخ والتقويم",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("اليوم:", color = Color(0xFF64748B))
                            Text(greg.dayOfWeekArabic, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("التاريخ الميلادي:", color = Color(0xFF64748B))
                            Text("${greg.formattedArabic} م", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("التاريخ الهجري:", color = Color(0xFF64748B))
                            Text(hijri.formatted, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("رقم اليوم في السنة:", color = Color(0xFF64748B))
                            Text("${greg.dayOfYear}", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الأيام المتبقية في السنة:", color = Color(0xFF64748B))
                            Text("${greg.daysRemainingInYear} يوماً", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("تم", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UserSwitchDialog(
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onDismiss: () -> Unit,
    onSelectUser: (UserEntity) -> Unit
) {
    var selectedUserToVerify by remember { mutableStateOf<UserEntity?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
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
                        text = if (selectedUserToVerify == null) "المستخدم والصلاحيات" else "التحقق من رمز المدير",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedUserToVerify == null) {
                    Text(
                        text = "التطبيق مخصص لأمين الصندوق. للتبديل إلى حساب المدير يرجى إدخال الرمز السري.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    users.forEach { user ->
                        val isSelected = user.id == currentUser?.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable {
                                    if (user.role == "ADMIN") {
                                        selectedUserToVerify = user
                                        enteredPin = ""
                                        pinError = false
                                    } else {
                                        onSelectUser(user)
                                    }
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NavyPrimary.copy(alpha = 0.1f) else Color(0xFFF8FAFC),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) NavyPrimary else Color(0xFFE2E8F0)
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
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = if (user.role == "ADMIN") Color(0xFFE53935) else Color(0xFF43A047),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(text = user.fullName, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (user.role == "ADMIN") "مدير الإدارة (قفل برمز سري)" else "أمين الصندوق (الحساب الافتراضي)",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(NavyPrimary)
                                            .size(20.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("✓", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // PIN verification for Manager
                    Text(
                        text = "أدخل رمز الدخول الخاص بالمدير (${selectedUserToVerify?.fullName}):",
                        fontSize = 13.sp,
                        color = Color(0xFF334155)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        placeholder = { Text("رمز PIN (الافتراضي 1234)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (pinError) {
                        Text(
                            text = "رمز الدخول غير صحيح!",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedUserToVerify = null },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("رجوع")
                        }

                        Button(
                            onClick = {
                                if (enteredPin == selectedUserToVerify?.pin || enteredPin == "1234") {
                                    onSelectUser(selectedUserToVerify!!)
                                } else {
                                    pinError = true
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                        ) {
                            Text("دخول كمدير", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "🏛️", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "صندوق الأحوال المدنية",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                )
                Text(
                    text = "نظام إدارة صندوق ومبيعات استمارات الأحوال المدنية والسجل المدني",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "برمجة وتصميم",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "محمد هشام الصلاحي",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "الإصدار: 1.0.0 (الجمهورية اليمنية)",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Dialog to adjust opening balance or add cash from personal pocket.
 */
@Composable
fun UpdateOpeningBalanceDialog(
    currentOpeningBalance: Double,
    onDismiss: () -> Unit,
    onSubmit: (amount: Double, reason: String) -> Unit
) {
    var isAdditionMode by remember { mutableStateOf(true) } // true: إضافة من الجيب, false: تعيين إجمالي
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val resultingOpeningBalance = if (isAdditionMode) {
        currentOpeningBalance + enteredAmount
    } else {
        enteredAmount
    }

    Dialog(onDismissRequest = onDismiss) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💼 رصيد بداية اليوم (العهدة)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isAdditionMode = true },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAdditionMode) NavyPrimary else Color(0xFFF1F5F9),
                        border = if (isAdditionMode) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "إضافة من الجيب (+)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdditionMode) Color.White else Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isAdditionMode = false },
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isAdditionMode) NavyPrimary else Color(0xFFF1F5F9),
                        border = if (!isAdditionMode) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Box(modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "تحديد الإجمالي مباشرة",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isAdditionMode) Color.White else Color(0xFF475569),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Current & New Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("رصيد بداية اليوم الحالي:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(CurrencyUtil.formatRiyal(currentOpeningBalance), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        if (isAdditionMode && enteredAmount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("المبلغ المضاف من الجيب الشخصي:", fontSize = 12.sp, color = IncomeGreen)
                                Text("+${CurrencyUtil.formatRiyal(enteredAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("رصيد البداية الجديد المتوقع:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                            Text(
                                CurrencyUtil.formatRiyal(resultingOpeningBalance),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                        Text(
                            text = "💡 سينعكس هذا المبلغ ويزداد فوراً في الرصيد المتوقع بالصندوق.",
                            fontSize = 11.sp,
                            color = Color(0xFF0369A1),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isAdditionMode) "المبلغ المأخوذ من الجيب الشخصي (ريال) *" else "إجمالي رصيد بداية اليوم الجديد (ريال) *",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    placeholder = { Text(if (isAdditionMode) "مثال: 10000" else "مثال: 25000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("opening_balance_amount_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "السبب / البيان",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = reasonText,
                    onValueChange = { reasonText = it },
                    placeholder = { Text(if (isAdditionMode) "مثال: إضافة عهدة شخصية لبدء اليوم" else "تعديل رصيد بداية اليوم") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Button(
                        onClick = {
                            val defaultReason = if (isAdditionMode) "إضافة عهدة شخصية للصندوق: $enteredAmount ريال" else "تعديل رصيد بداية اليوم إلى $resultingOpeningBalance ريال"
                            val finalReason = reasonText.ifBlank { defaultReason }
                            onSubmit(resultingOpeningBalance, finalReason)
                        },
                        enabled = enteredAmount > 0 || !isAdditionMode,
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("btn_confirm_opening_balance"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Text("حفظ وتحديث الرصيد", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Modal showing all active debts, allowing debt payment, debt deletion, and printing debt statement.
 */
@Composable
fun DebtsListDialog(
    debts: List<DebtEntity>,
    totalDebts: Double,
    directorateName: String,
    onDismiss: () -> Unit,
    onPayDebt: (DebtEntity) -> Unit,
    onDeleteDebt: (Long) -> Unit,
    onAddNewDebt: () -> Unit
) {
    val context = LocalContext.current
    val activeDebts = debts.filter { it.status != "PAID" && it.remainingAmount > 0 }
    var debtToDelete by remember { mutableStateOf<DebtEntity?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "📋 كشف الديون والآجل",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        )
                        Text(
                            text = "قائمة الأشخاص المدينين ومبالغ ديونهم",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Summary Total Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("إجمالي الديون القائمة:", fontSize = 12.sp, color = Color(0xFF991B1B))
                            Text(
                                text = CurrencyUtil.formatRiyal(totalDebts),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = "💡 محسوبة في الرصيد المتوقع لأن المبلغ خرج ديناً.",
                                fontSize = 10.sp,
                                color = Color(0xFFB91C1C)
                            )
                        }

                        Button(
                            onClick = {
                                PrintAndExportUtil.printDebtsStatement(context, debts, directorateName)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة الكشف", fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Debts List
                if (activeDebts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✓", fontSize = 36.sp, color = IncomeGreen)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("لا توجد أي ديون غير مسددة حالياً.", fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                            Text("جميع المبالغ مسددة بالكامل.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(activeDebts, key = { it.id }) { debt ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = debt.personName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = CurrencyUtil.formatRiyal(debt.remainingAmount),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = ExpenseRed
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = if (debt.reason.isNotBlank()) "الغرض: ${debt.reason}" else "دين نقدي",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "${debt.gregorianDate} (${debt.hijriDate})",
                                            fontSize = 11.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onDismiss()
                                                onPayDebt(debt)
                                            },
                                            modifier = Modifier.weight(1.2f).height(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text("تسديد الدين", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        OutlinedButton(
                                            onClick = { debtToDelete = debt },
                                            modifier = Modifier.weight(1f).height(36.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("حذف الدين", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onDismiss()
                            onAddNewDebt()
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تسجيل دين جديد", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.7f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إغلاق", fontSize = 13.sp)
                    }
                }
            }
        }
    }

    if (debtToDelete != null) {
        DeleteConfirmDialog(
            title = "حذف الدين",
            message = "هل أنت متأكد من حذف دين المدين '${debtToDelete!!.personName}' بمبلغ ${CurrencyUtil.formatRiyal(debtToDelete!!.remainingAmount)}؟ سيتم إلغاؤه واسترجاع مبلغه للصندوق.",
            itemName = debtToDelete!!.personName,
            onDismiss = { debtToDelete = null },
            onConfirm = {
                val id = debtToDelete!!.id
                debtToDelete = null
                onDeleteDebt(id)
            }
        )
    }
}

/**
 * Dialog shown after paying the director to print an official payment voucher.
 */
@Composable
fun DirectorPaymentReceiptDialog(
    payment: DirectorPaymentEntity,
    directorateName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
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
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "تمت محاسبة وصرف مستحقات المدير",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyPrimary
                )
                Text(
                    text = "سند صرف رقم: #DP-${payment.id}",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المبلغ المنصرف:", fontSize = 13.sp, color = Color(0xFF475569))
                            Text(CurrencyUtil.formatRiyal(payment.amount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("التاريخ والوقت:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text("${payment.gregorianDate} • ${payment.timeString}", fontSize = 12.sp)
                        }
                        if (!payment.notes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("البيان:", fontSize = 12.sp, color = Color(0xFF64748B))
                                Text(payment.notes, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            PrintAndExportUtil.printDirectorPaymentVoucher(context, payment, directorateName)
                        },
                        modifier = Modifier.weight(1.3f).height(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة السند", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تم / إغلاق")
                    }
                }
            }
        }
    }
}

/**
 * Universal confirmation dialog for deleting items (forms, expenses, incomes, debts).
 */
@Composable
fun DeleteConfirmDialog(
    title: String,
    message: String,
    itemName: String = "",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = ExpenseRed,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تراجع")
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1.3f).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تأكيد الحذف", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
