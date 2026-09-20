package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility for formatting currency and numbers in Arabic.
 */
object CurrencyUtil {
    private val arabicSymbols = DecimalFormatSymbols(Locale("ar")).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }
    
    // Standard english digits formatter with comma grouping for clear financial display
    private val standardSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val numberFormatter = DecimalFormat("#,##0", standardSymbols)
    private val decimalFormatter = DecimalFormat("#,##0.##", standardSymbols)

    fun formatRiyal(amount: Double): String {
        return "${numberFormatter.format(amount)} ريال"
    }

    fun formatNumber(amount: Double): String {
        return numberFormatter.format(amount)
    }

    fun formatNumber(amount: Long): String {
        return numberFormatter.format(amount)
    }

    fun formatNumber(amount: Int): String {
        return numberFormatter.format(amount.toLong())
    }
}

/**
 * High precision Hijri Date Converter and Calendar Utilities.
 * Implements tabular Islamic calendar / Umm al-Qura compatible conversion with Arabic naming.
 */
object HijriDateUtil {
    private val HIJRI_MONTHS = arrayOf(
        "محرم", "صفر", "ربيع الأول", "ربيع الثاني",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوال", "ذو القعدة", "ذو الحجة"
    )

    private val GREGORIAN_MONTHS_ARABIC = arrayOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    private val WEEK_DAYS_ARABIC = arrayOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    data class HijriDate(
        val year: Int,
        val month: Int, // 1-indexed (1..12)
        val day: Int,
        val monthName: String,
        val formatted: String,
        val yearString: String = "$year هـ"
    )

    data class GregorianDateInfo(
        val dateString: String, // YYYY-MM-DD
        val formattedArabic: String, // e.g. "20 سبتمبر 2026"
        val dayOfWeekArabic: String, // e.g. "الأحد"
        val dayOfYear: Int,
        val daysRemainingInYear: Int,
        val year: Int,
        val month: Int,
        val day: Int
    )

    /**
     * Converts a Gregorian Date to Hijri date using astronomical tabular algorithm.
     */
    fun getHijriDate(calendar: Calendar = Calendar.getInstance()): HijriDate {
        val gYear = calendar.get(Calendar.YEAR)
        val gMonth = calendar.get(Calendar.MONTH) + 1 // 1-indexed
        val gDay = calendar.get(Calendar.DAY_OF_MONTH)

        // Julian Day Number calculation
        var y = gYear
        var m = gMonth
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + (a / 4)
        val jd = ((365.25 * (y + 4716)).toLong() + (30.6001 * (m + 1)).toLong() + gDay + b - 1524.5).toLong()

        // Epoch of Hijri calendar: JD 1948439.5 = 16 July 622 CE
        val l = (jd - 1948440 + 10632).toInt()
        val n = (l - 1) / 10631
        val l2 = l - 10631 * n + 354
        val j = (((10985 - l2) / 5316) * (50 * l2) / 17719) +
                (((l2 / 5670) * (43 * l2) / 15238))
        val l3 = l2 - (((30 - j) / 15) * (17719 * j) / 50) -
                (((j / 16) * (15238 * j) / 43)) + 29
        val hMonth = (24 * l3) / 709
        val hDay = (l3 - ((709 * hMonth) / 24))
        val hYear = 30 * n + j - 30

        val safeMonth = hMonth.coerceIn(1, 12)
        val safeDay = hDay.coerceIn(1, 30)
        val monthName = HIJRI_MONTHS[safeMonth - 1]
        val formatted = "$safeDay $monthName $hYear هـ"

        return HijriDate(
            year = hYear,
            month = safeMonth,
            day = safeDay,
            monthName = monthName,
            formatted = formatted
        )
    }

    fun getHijriDateFromString(gregorianDateStr: String): HijriDate {
        return try {
            val parts = gregorianDateStr.split("-")
            val cal = Calendar.getInstance()
            cal.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
            getHijriDate(cal)
        } catch (e: Exception) {
            getHijriDate()
        }
    }

    fun getTodayGregorianString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getNowTimeString(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale("ar"))
        return sdf.format(Date())
    }

    fun getGregorianDateInfo(calendar: Calendar = Calendar.getInstance()): GregorianDateInfo {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) // 1 = Sunday
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        val totalDays = if (isLeap) 366 else 365
        val daysRemaining = totalDays - dayOfYear

        val dateString = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
        val formattedArabic = "$day ${GREGORIAN_MONTHS_ARABIC[month - 1]} $year"
        val dayOfWeekArabic = WEEK_DAYS_ARABIC[dayOfWeek - 1]

        return GregorianDateInfo(
            dateString = dateString,
            formattedArabic = formattedArabic,
            dayOfWeekArabic = dayOfWeekArabic,
            dayOfYear = dayOfYear,
            daysRemainingInYear = daysRemaining,
            year = year,
            month = month,
            day = day
        )
    }

    fun formatArabicDate(gregorianDateStr: String): String {
        return try {
            val parts = gregorianDateStr.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()
            "$day ${GREGORIAN_MONTHS_ARABIC.getOrElse(month - 1) { "" }} $year"
        } catch (e: Exception) {
            gregorianDateStr
        }
    }
}
