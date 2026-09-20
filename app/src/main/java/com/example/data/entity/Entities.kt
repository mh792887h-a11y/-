package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole(val titleArabic: String) {
    ADMIN("مدير الإدارة"),
    CASHIER("أمين الصندوق")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val fullName: String,
    val role: String, // "ADMIN" or "CASHIER"
    val pin: String = "1234",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fee_settings")
data class FeeSettingEntity(
    @PrimaryKey val formType: String, // "NEW", "RENEW", "LOST", "DAMAGED"
    val typeNameArabic: String,       // "جديد", "تجديد", "بدل فاقد", "بدل تالف"
    val salePrice: Double,            // e.g. 4500, 4000, 9000, 7000
    val stateShare: Double,           // e.g. 3750, 3750, 8750, 6750
    val directorateShare: Double,     // e.g. 200, 0, 0, 0
    val fundShare: Double,            // e.g. 550, 250, 250, 250
    val lastUpdatedAt: Long = System.currentTimeMillis(),
    val lastUpdatedBy: String = "المدير"
)

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["receiptNumber"], unique = true), Index(value = ["gregorianDate"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,        // e.g. "20260920-0001"
    val citizenName: String,          // e.g. "محمد أحمد علي"
    val transactionType: String,      // "جديد", "تجديد", "بدل فاقد", "بدل تالف"
    val gender: String,               // "ذكر", "أنثى"
    val salePrice: Double,            // 4500
    val stateShare: Double,           // 3750
    val directorateShare: Double,     // 200
    val fundShare: Double,            // 550
    val gregorianDate: String,        // YYYY-MM-DD
    val hijriDate: String,            // e.g. "8 ربيع الأول 1448 هـ"
    val timeString: String,           // e.g. "10:30 ص"
    val notes: String? = null,
    val createdByUserId: Int = 1,
    val createdByName: String = "أمين الصندوق",
    val status: String = "ACTIVE",    // "ACTIVE" or "CANCELLED"
    val cancelledAt: Long? = null,
    val cancelledBy: String? = null,
    val cancellationReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "income", indices = [Index(value = ["gregorianDate"])])
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String,             // "دخل الاستمارات", "دخل آخر", "تسديد دين", "إضافة أخرى"
    val statement: String,            // البيان
    val amount: Double,
    val withWhom: String,             // مع من
    val gregorianDate: String,        // YYYY-MM-DD
    val hijriDate: String,
    val timeString: String,
    val notes: String? = null,
    val createdByUserId: Int = 1,
    val createdByName: String = "أمين الصندوق",
    val status: String = "ACTIVE",    // "ACTIVE" or "CANCELLED"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "expenses", indices = [Index(value = ["gregorianDate"])])
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val statement: String,            // البيان مثل: شراء قرطاسية
    val amount: Double,               // المبلغ مثل: 5000
    val withWhom: String,             // مع من مثل: أحمد محمد
    val gregorianDate: String,        // YYYY-MM-DD
    val hijriDate: String,
    val timeString: String,
    val notes: String? = null,
    val createdByUserId: Int = 1,
    val createdByName: String = "أمين الصندوق",
    val status: String = "ACTIVE",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,           // اسم الشخص
    val originalAmount: Double,       // المبلغ الأصلي
    val paidAmount: Double = 0.0,     // المسدد
    val remainingAmount: Double,      // المتبقي
    val reason: String,               // السبب
    val gregorianDate: String,        // YYYY-MM-DD
    val hijriDate: String,
    val notes: String? = null,
    val status: String = "UNPAID",    // "UNPAID", "PARTIALLY_PAID", "FULLY_PAID"
    val createdByName: String = "أمين الصندوق",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val paidAmount: Double,
    val remainingAfterPayment: Double,
    val gregorianDate: String,
    val hijriDate: String,
    val timeString: String,
    val receivedByName: String = "أمين الصندوق",
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_movements", indices = [Index(value = ["gregorianDate"])])
data class CashMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val movementType: String,         // "بيع استمارة", "دخل آخر", "تسديد دين", "إضافة خرج", "إلغاء عملية"
    val statement: String,            // البيان التفصيلي
    val amountIn: Double = 0.0,       // وارد (+)
    val amountOut: Double = 0.0,      // منصرف (-)
    val balanceAfter: Double,         // الرصيد بعد الحركة
    val gregorianDate: String,
    val hijriDate: String,
    val timeString: String,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: Long? = null
)

@Entity(tableName = "daily_closings", indices = [Index(value = ["gregorianDate"], unique = true)])
data class DailyClosingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gregorianDate: String,        // YYYY-MM-DD
    val hijriDate: String,
    val openingBalance: Double = 0.0, // رصيد بداية اليوم
    val totalIncome: Double = 0.0,    // إجمالي الدخل
    val totalExpenses: Double = 0.0,  // إجمالي الخرج
    val netToday: Double = 0.0,       // صافي اليوم
    val expectedBalance: Double = 0.0,// الرصيد المتوقع
    val actualBalance: Double = 0.0,  // الرصيد الفعلي
    val difference: Double = 0.0,     // الفرق (الفعلي - المتوقع)
    val status: String = "OPEN",      // "OPEN", "CLOSED"
    val closedAt: Long? = null,
    val closedByName: String? = null,
    val reopenedAt: Long? = null,
    val reopenedByName: String? = null,
    // Counts
    val totalTransactions: Int = 0,
    val malesCount: Int = 0,
    val femalesCount: Int = 0,
    val newCount: Int = 0,
    val renewCount: Int = 0,
    val lostCount: Int = 0,
    val damagedCount: Int = 0,
    // Shares
    val totalStateShare: Double = 0.0,
    val totalDirectorateShare: Double = 0.0,
    val totalFundShare: Double = 0.0,
    val otherIncome: Double = 0.0,
    val debtPaidAmount: Double = 0.0
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val actionType: String,           // "تسجيل عملية", "إلغاء عملية", "تعديل رسوم", "إغلاق اليومية", "إعادة فتح اليومية", إلخ
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis(),
    val gregorianDate: String,
    val hijriDate: String,
    val oldValue: String? = null,
    val newValue: String? = null,
    val details: String
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
