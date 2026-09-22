package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NavyPrimary
import com.example.util.HijriDateUtil
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDatePickerDialog(
    initialDateString: String? = null,
    onDateSelected: (selectedDateString: String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialMillis = remember(initialDateString) {
        if (!initialDateString.isNullOrBlank()) {
            try {
                val parts = initialDateString.split("-")
                val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                cal.clear()
                cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                cal.timeInMillis
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val selectedMillis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    cal.timeInMillis = selectedMillis
                    val y = cal.get(Calendar.YEAR)
                    val m = cal.get(Calendar.MONTH) + 1
                    val d = cal.get(Calendar.DAY_OF_MONTH)
                    val dateStr = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
                    onDateSelected(dateStr)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("تأكيد التاريخ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = {
                        val todayStr = HijriDateUtil.getTodayGregorianString()
                        onDateSelected(todayStr)
                    }
                ) {
                    Text("اليوم الحالي", color = Color(0xFF0284C7), fontWeight = FontWeight.SemiBold)
                }

                TextButton(onClick = onDismiss) {
                    Text("إلغاء", color = Color(0xFF64748B))
                }
            }
        },
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Live selected date in Gregorian & Hijri preview
            val currentMillis = datePickerState.selectedDateMillis ?: initialMillis
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = currentMillis }
            val y = cal.get(Calendar.YEAR)
            val m = cal.get(Calendar.MONTH) + 1
            val d = cal.get(Calendar.DAY_OF_MONTH)
            val previewDateStr = String.format(Locale.US, "%04d-%02d-%02d", y, m, d)
            val previewHijri = HijriDateUtil.getHijriDateFromString(previewDateStr)
            val previewFormattedGreg = HijriDateUtil.getFormattedArabicWithDayOfWeek(previewDateStr)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text(
                    text = previewFormattedGreg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = NavyPrimary
                )
                Text(
                    text = "الموافق هجرياً: ${previewHijri.formatted}",
                    fontSize = 12.sp,
                    color = Color(0xFF0369A1)
                )
            }

            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}
