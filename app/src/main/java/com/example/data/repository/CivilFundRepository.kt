package com.example.data.repository

import com.example.data.database.AppDatabase
import com.example.data.entity.AppSettingEntity
import com.example.data.entity.AuditLogEntity
import com.example.data.entity.CashMovementEntity
import com.example.data.entity.DailyClosingEntity
import com.example.data.entity.DebtEntity
import com.example.data.entity.DebtPaymentEntity
import com.example.data.entity.DirectorPaymentEntity
import com.example.data.entity.ExpenseEntity
import com.example.data.entity.FeeSettingEntity
import com.example.data.entity.IncomeEntity
import com.example.data.entity.TransactionEntity
import com.example.data.entity.UserEntity
import com.example.util.HijriDateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

class CivilFundRepository(private val db: AppDatabase) {

    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val allFeeSettings: Flow<List<FeeSettingEntity>> = db.feeSettingDao().getAllFeeSettings()
    val allExpenses: Flow<List<ExpenseEntity>> = db.expenseDao().getAllExpenses()
    val allIncomes: Flow<List<IncomeEntity>> = db.incomeDao().getAllIncome()
    val allDebts: Flow<List<DebtEntity>> = db.debtDao().getAllDebts()
    val allCashMovements: Flow<List<CashMovementEntity>> = db.cashMovementDao().getAllMovements()
    val allClosings: Flow<List<DailyClosingEntity>> = db.dailyClosingDao().getAllClosings()
    val allAuditLogs: Flow<List<AuditLogEntity>> = db.auditLogDao().getAllLogs()
    val allUsers: Flow<List<UserEntity>> = db.userDao().getAllUsers()
    val allDirectorPayments: Flow<List<DirectorPaymentEntity>> = db.directorPaymentDao().getAllPayments()
    val allTransactionDates: Flow<List<String>> = db.transactionDao().getAllTransactionDates()
    val totalDirectorEarnedFlow: Flow<Double?> = db.transactionDao().getTotalDirectorShareFlow()
    val totalDirectorPaidFlow: Flow<Double?> = db.directorPaymentDao().getTotalPaidFlow()

    fun getTransactionsByDate(date: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getTransactionsByDate(date)

    fun getTransactionsBetween(startDate: String, endDate: String): Flow<List<TransactionEntity>> =
        db.transactionDao().getTransactionsBetween(startDate, endDate)

    fun searchTransactions(query: String): Flow<List<TransactionEntity>> =
        db.transactionDao().searchTransactions(query)

    fun getDailyClosing(date: String): Flow<DailyClosingEntity?> =
        db.dailyClosingDao().getDailyClosing(date)

    fun getMovementsByDate(date: String): Flow<List<CashMovementEntity>> =
        db.cashMovementDao().getMovementsByDate(date)

    suspend fun getRecentCitizenNames(): List<String> = withContext(Dispatchers.IO) {
        db.transactionDao().getRecentCitizenNames()
    }

    suspend fun getCurrentUser(): UserEntity = withContext(Dispatchers.IO) {
        val userIdStr = db.appSettingDao().getSettingSync("CURRENT_USER_ID") ?: "2"
        val userId = userIdStr.toIntOrNull() ?: 2
        db.userDao().getUserById(userId) ?: UserEntity(
            id = 2,
            username = "cashier",
            fullName = "أمين الصندوق",
            role = "CASHIER"
        )
    }

    suspend fun setCurrentUser(userId: Int) = withContext(Dispatchers.IO) {
        db.appSettingDao().setSetting(AppSettingEntity("CURRENT_USER_ID", userId.toString()))
    }

    suspend fun getFeeSetting(formType: String): FeeSettingEntity? = withContext(Dispatchers.IO) {
        db.feeSettingDao().getFeeSettingSync(formType)
    }

    suspend fun getDirectorateName(): String = withContext(Dispatchers.IO) {
        db.appSettingDao().getSettingSync("DIRECTORATE_NAME") ?: "مصلحة الأحوال المدنية والسجل المدني"
    }

    suspend fun setDirectorateName(name: String) = withContext(Dispatchers.IO) {
        db.appSettingDao().setSetting(AppSettingEntity("DIRECTORATE_NAME", name))
    }

    /**
     * Sells a civil status application form to a citizen.
     */
    suspend fun sellForm(
        citizenName: String,
        formNumber: String = "",
        recordNumber: String = "",
        formTypeNameArabic: String, // "جديد", "تجديد", "بدل فاقد", "بدل تالف"
        gender: String, // "ذكر", "أنثى"
        notes: String? = null
    ): Result<TransactionEntity> = withContext(Dispatchers.IO) {
        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted
        val time = HijriDateUtil.getNowTimeString()
        val user = getCurrentUser()

        // Check if daily closing is closed
        val closing = db.dailyClosingDao().getDailyClosingSync(today)
        if (closing != null && closing.status == "CLOSED" && user.role != "ADMIN") {
            return@withContext Result.failure(Exception("اليومية مغلقة لهذا اليوم، ولا يمكن إضافة عمليات إلا بموافقة المدير."))
        }

        // Map type name to key
        val typeKey = when (formTypeNameArabic) {
            "جديد" -> "NEW"
            "تجديد" -> "RENEW"
            "بدل فاقد" -> "LOST"
            "بدل تالف" -> "DAMAGED"
            else -> "NEW"
        }

        val feeSetting = db.feeSettingDao().getFeeSettingSync(typeKey)
            ?: return@withContext Result.failure(Exception("لم يتم العثور على إعدادات الرسوم لهذه المعاملة."))

        // Generate next receipt number: YYYYMMDD-0001
        val dateDigits = today.replace("-", "")
        val lastTx = db.transactionDao().getLastReceiptForDate(today)
        val nextSeq = if (lastTx != null && lastTx.receiptNumber.startsWith(dateDigits)) {
            val seqStr = lastTx.receiptNumber.substringAfter("-")
            (seqStr.toIntOrNull() ?: 0) + 1
        } else {
            1
        }
        val receiptNumber = String.format(Locale.US, "%s-%04d", dateDigits, nextSeq)

        val tx = TransactionEntity(
            receiptNumber = receiptNumber,
            citizenName = citizenName.trim(),
            formNumber = formNumber.trim(),
            recordNumber = recordNumber.trim(),
            transactionType = formTypeNameArabic,
            gender = gender,
            salePrice = feeSetting.salePrice,
            stateShare = feeSetting.stateShare,
            directorateShare = feeSetting.directorateShare,
            fundShare = feeSetting.fundShare,
            gregorianDate = today,
            hijriDate = hijri,
            timeString = time,
            notes = notes?.trim(),
            createdByUserId = user.id,
            createdByName = user.fullName,
            status = "ACTIVE"
        )

        val txId = db.transactionDao().insert(tx)
        val savedTx = tx.copy(id = txId)

        // Add cash movement: net gain for the fund (الكسب الصافي للصندوق مثلاً 550 للاستمارة الجديد)
        val latestMovement = db.cashMovementDao().getLatestMovement()
        val currentBalance = latestMovement?.balanceAfter ?: 0.0
        val newBalance = currentBalance + feeSetting.fundShare

        val detailsText = buildString {
            append("دخل صافي للصندوق (استمارة $formTypeNameArabic) - $citizenName")
            if (formNumber.isNotBlank()) append(" | استمارة: $formNumber")
            if (recordNumber.isNotBlank()) append(" | قيد: $recordNumber")
        }

        db.cashMovementDao().insert(
            CashMovementEntity(
                movementType = "دخل استمارة",
                statement = detailsText,
                amountIn = feeSetting.fundShare,
                amountOut = 0.0,
                balanceAfter = newBalance,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                referenceId = txId
            )
        )

        // Record Audit Log
        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "بيع استمارة",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                newValue = "${feeSetting.fundShare} ريال (صافي الصندوق)",
                details = "تسجيل استمارة $formTypeNameArabic للمواطن $citizenName - استمارة: $formNumber - قيد: $recordNumber"
            )
        )

        // Update daily closing stats
        recalculateDailyClosing(today)

        Result.success(savedTx)
    }

    /**
     * Cancels a transaction (Soft delete with cash reversal)
     */
    suspend fun cancelTransaction(
        transactionId: Long,
        reason: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        val tx = db.transactionDao().getTransactionById(transactionId)
            ?: return@withContext Result.failure(Exception("العملية غير موجودة."))

        if (tx.status == "CANCELLED") {
            return@withContext Result.failure(Exception("هذه العملية ملغاة مسبقاً."))
        }

        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted
        val time = HijriDateUtil.getNowTimeString()

        val updatedTx = tx.copy(
            status = "CANCELLED",
            cancelledAt = System.currentTimeMillis(),
            cancelledBy = user.fullName,
            cancellationReason = reason.trim()
        )
        db.transactionDao().update(updatedTx)

        // Cash reversal movement for fund share
        val latestMovement = db.cashMovementDao().getLatestMovement()
        val currentBalance = latestMovement?.balanceAfter ?: 0.0
        val newBalance = currentBalance - tx.fundShare

        db.cashMovementDao().insert(
            CashMovementEntity(
                movementType = "إلغاء عملية",
                statement = "إلغاء استمارة ${tx.citizenName} (إيصال ${tx.receiptNumber}) - سبب: ${reason.trim()}",
                amountIn = 0.0,
                amountOut = tx.fundShare,
                balanceAfter = newBalance,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                referenceId = tx.id
            )
        )

        // Audit Log
        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "إلغاء عملية",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                oldValue = "نشطة (${tx.salePrice} ريال)",
                newValue = "ملغاة",
                details = "إلغاء عملية ${tx.receiptNumber} للمواطن ${tx.citizenName}. السبب: $reason"
            )
        )

        recalculateDailyClosing(tx.gregorianDate)
        Result.success(Unit)
    }

    /**
     * Adds an expense (خرج)
     */
    suspend fun addExpense(
        statement: String,
        amount: Double,
        withWhom: String,
        notes: String? = null
    ): Result<ExpenseEntity> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(Exception("المبلغ يجب أن يكون أكبر من الصفر."))
        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted
        val time = HijriDateUtil.getNowTimeString()
        val user = getCurrentUser()

        val closing = db.dailyClosingDao().getDailyClosingSync(today)
        if (closing != null && closing.status == "CLOSED" && user.role != "ADMIN") {
            return@withContext Result.failure(Exception("اليومية مغلقة لهذا اليوم."))
        }

        val exp = ExpenseEntity(
            statement = statement.trim(),
            amount = amount,
            withWhom = withWhom.trim(),
            gregorianDate = today,
            hijriDate = hijri,
            timeString = time,
            notes = notes?.trim(),
            createdByUserId = user.id,
            createdByName = user.fullName
        )
        val id = db.expenseDao().insert(exp)
        val savedExp = exp.copy(id = id)

        // Deduct from cash balance
        val latestMovement = db.cashMovementDao().getLatestMovement()
        val currentBalance = latestMovement?.balanceAfter ?: 0.0
        val newBalance = currentBalance - amount

        db.cashMovementDao().insert(
            CashMovementEntity(
                movementType = "إضافة خرج",
                statement = "منصرف: ${statement.trim()} (مع: ${withWhom.trim()})",
                amountIn = 0.0,
                amountOut = amount,
                balanceAfter = newBalance,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                referenceId = id
            )
        )

        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "إضافة خرج",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                newValue = "$amount ريال",
                details = "صرف مبلغ $amount ريال لـ $statement مع $withWhom"
            )
        )

        recalculateDailyClosing(today)
        Result.success(savedExp)
    }

    /**
     * Adds other income (دخل آخر)
     */
    suspend fun addOtherIncome(
        category: String,
        statement: String,
        amount: Double,
        withWhom: String,
        notes: String? = null
    ): Result<IncomeEntity> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(Exception("المبلغ يجب أن يكون أكبر من الصفر."))
        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted
        val time = HijriDateUtil.getNowTimeString()
        val user = getCurrentUser()

        val inc = IncomeEntity(
            category = category,
            statement = statement.trim(),
            amount = amount,
            withWhom = withWhom.trim(),
            gregorianDate = today,
            hijriDate = hijri,
            timeString = time,
            notes = notes?.trim(),
            createdByUserId = user.id,
            createdByName = user.fullName
        )
        val id = db.incomeDao().insert(inc)
        val savedInc = inc.copy(id = id)

        val latestMovement = db.cashMovementDao().getLatestMovement()
        val currentBalance = latestMovement?.balanceAfter ?: 0.0
        val newBalance = currentBalance + amount

        db.cashMovementDao().insert(
            CashMovementEntity(
                movementType = "دخل آخر",
                statement = "$category: ${statement.trim()} (من: ${withWhom.trim()})",
                amountIn = amount,
                amountOut = 0.0,
                balanceAfter = newBalance,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                referenceId = id
            )
        )

        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "إضافة دخل",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                newValue = "$amount ريال",
                details = "إضافة دخل $category بمبلغ $amount ريال بيان: $statement"
            )
        )

        recalculateDailyClosing(today)
        Result.success(savedInc)
    }

    /**
     * Adds a debt record (إضافة دين)
     */
    suspend fun addDebt(
        personName: String,
        amount: Double,
        reason: String,
        notes: String? = null
    ): Result<DebtEntity> = withContext(Dispatchers.IO) {
        if (amount <= 0) return@withContext Result.failure(Exception("المبلغ يجب أن يكون أكبر من الصفر."))
        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted
        val user = getCurrentUser()

        val debt = DebtEntity(
            personName = personName.trim(),
            originalAmount = amount,
            paidAmount = 0.0,
            remainingAmount = amount,
            reason = reason.trim(),
            gregorianDate = today,
            hijriDate = hijri,
            notes = notes?.trim(),
            status = "UNPAID",
            createdByName = user.fullName
        )
        val id = db.debtDao().insert(debt)
        val savedDebt = debt.copy(id = id)

        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "تسجيل دين",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                newValue = "$amount ريال",
                details = "تسجيل دين على ${personName.trim()} بمبلغ $amount ريال بسبب: $reason"
            )
        )

        Result.success(savedDebt)
    }

    /**
     * Pays all or part of a debt (تسديد دين)
     */
    suspend fun payDebt(
        debtId: Long,
        paymentAmount: Double,
        notes: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val debt = db.debtDao().getDebtById(debtId)
            ?: return@withContext Result.failure(Exception("لم يتم العثور على سجل الدين."))

        if (paymentAmount <= 0) {
            return@withContext Result.failure(Exception("مبلغ السداد يجب أن يكون أكبر من الصفر."))
        }
        if (paymentAmount > debt.remainingAmount) {
            return@withContext Result.failure(Exception("مبلغ السداد أكبر من المبلغ المتبقي (${debt.remainingAmount} ريال)."))
        }

        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted
        val time = HijriDateUtil.getNowTimeString()
        val user = getCurrentUser()

        val newPaid = debt.paidAmount + paymentAmount
        val newRemaining = debt.remainingAmount - paymentAmount
        val newStatus = if (newRemaining <= 0.0) "FULLY_PAID" else "PARTIALLY_PAID"

        val updatedDebt = debt.copy(
            paidAmount = newPaid,
            remainingAmount = newRemaining,
            status = newStatus
        )
        db.debtDao().update(updatedDebt)

        db.debtPaymentDao().insert(
            DebtPaymentEntity(
                debtId = debtId,
                paidAmount = paymentAmount,
                remainingAfterPayment = newRemaining,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                receivedByName = user.fullName,
                notes = notes?.trim()
            )
        )

        // Add to cash balance & movements
        val latestMovement = db.cashMovementDao().getLatestMovement()
        val currentBalance = latestMovement?.balanceAfter ?: 0.0
        val newBalance = currentBalance + paymentAmount

        db.cashMovementDao().insert(
            CashMovementEntity(
                movementType = "تسديد دين",
                statement = "سداد دين من: ${debt.personName} (المتبقي: $newRemaining ريال)",
                amountIn = paymentAmount,
                amountOut = 0.0,
                balanceAfter = newBalance,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                referenceId = debtId
            )
        )

        // Also record as Income record of type DEBT_REPAYMENT
        db.incomeDao().insert(
            IncomeEntity(
                category = "تسديد دين",
                statement = "سداد دين ${debt.personName}",
                amount = paymentAmount,
                withWhom = debt.personName,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                notes = notes?.trim(),
                createdByUserId = user.id,
                createdByName = user.fullName
            )
        )

        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "سداد دين",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                newValue = "$paymentAmount ريال",
                details = "سداد مبلغ $paymentAmount ريال من دين ${debt.personName}، المتبقي: $newRemaining ريال"
            )
        )

        recalculateDailyClosing(today)
        Result.success(Unit)
    }

    /**
     * Updates the actual physical cash counted in fund (الرصيد الفعلي)
     */
    suspend fun updateActualBalance(
        date: String,
        actualCash: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val closing = getOrCreateDailyClosing(date)
        val diff = actualCash - closing.expectedBalance
        val updated = closing.copy(
            actualBalance = actualCash,
            difference = diff
        )
        db.dailyClosingDao().update(updated)

        val user = getCurrentUser()
        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "جرد الصندوق الفعلي",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = date,
                hijriDate = closing.hijriDate,
                newValue = "$actualCash ريال",
                details = "تحديث الرصيد الفعلي للصندوق: $actualCash ريال، الفرق: $diff ريال"
            )
        )
        Result.success(Unit)
    }

    /**
     * Closes the daily closing (إغلاق اليومية)
     */
    suspend fun closeDailyClosing(
        date: String,
        actualCash: Double
    ): Result<DailyClosingEntity> = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        recalculateDailyClosing(date)
        val closing = getOrCreateDailyClosing(date)
        val diff = actualCash - closing.expectedBalance

        val updated = closing.copy(
            actualBalance = actualCash,
            difference = diff,
            status = "CLOSED",
            closedAt = System.currentTimeMillis(),
            closedByName = user.fullName
        )
        db.dailyClosingDao().update(updated)

        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "إغلاق اليومية",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = date,
                hijriDate = closing.hijriDate,
                newValue = "مغلقة",
                details = "إغلاق يومية $date بواسطة ${user.fullName}، متوقع: ${closing.expectedBalance} ريال، فعلي: $actualCash ريال، فرق: $diff ريال"
            )
        )

        Result.success(updated)
    }

    /**
     * Reopens a closed daily closing (Only Director / ADMIN)
     */
    suspend fun reopenDailyClosing(date: String): Result<Unit> = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        if (user.role != "ADMIN") {
            return@withContext Result.failure(Exception("فقط مدير الإدارة مخول بإعادة فتح اليومية المغلقة."))
        }

        val closing = db.dailyClosingDao().getDailyClosingSync(date)
            ?: return@withContext Result.failure(Exception("لم يتم العثور على سجل اليومية."))

        val updated = closing.copy(
            status = "OPEN",
            reopenedAt = System.currentTimeMillis(),
            reopenedByName = user.fullName
        )
        db.dailyClosingDao().update(updated)

        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "إعادة فتح اليومية",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = date,
                hijriDate = closing.hijriDate,
                newValue = "مفتوحة",
                details = "إعادة فتح يومية $date بواسطة مدير الإدارة ${user.fullName}"
            )
        )

        Result.success(Unit)
    }

    /**
     * Updates fee settings (Only Director / ADMIN)
     */
    suspend fun updateFeeSetting(
        formType: String,
        salePrice: Double,
        stateShare: Double,
        directorateShare: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = getCurrentUser()
        if (user.role != "ADMIN") {
            return@withContext Result.failure(Exception("تعديل الرسوم مقتصر على مدير الإدارة فقط."))
        }

        val fundShare = salePrice - stateShare - directorateShare
        if (fundShare < 0) {
            return@withContext Result.failure(Exception("مجموع حق الدولة وحصة الإدارة أكبر من سعر البيع!"))
        }

        val oldSetting = db.feeSettingDao().getFeeSettingSync(formType)
            ?: return@withContext Result.failure(Exception("نوع المعاملة غير صالح."))

        val newSetting = oldSetting.copy(
            salePrice = salePrice,
            stateShare = stateShare,
            directorateShare = directorateShare,
            fundShare = fundShare,
            lastUpdatedAt = System.currentTimeMillis(),
            lastUpdatedBy = user.fullName
        )
        db.feeSettingDao().update(newSetting)

        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted

        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "تعديل الرسوم",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                oldValue = "سعر: ${oldSetting.salePrice} (دولة: ${oldSetting.stateShare}, إدارة: ${oldSetting.directorateShare}, صندوق: ${oldSetting.fundShare})",
                newValue = "سعر: $salePrice (دولة: $stateShare, إدارة: $directorateShare, صندوق: $fundShare)",
                details = "تعديل رسوم استمارة ${oldSetting.typeNameArabic} بواسطة ${user.fullName}"
            )
        )

        Result.success(Unit)
    }

    /**
     * Recalculates all metrics for a given date's closing
     */
    suspend fun recalculateDailyClosing(date: String): DailyClosingEntity = withContext(Dispatchers.IO) {
        val closing = getOrCreateDailyClosing(date)

        // Collect all active transactions for this date
        val transactions = db.transactionDao().getTransactionsByDate(date).firstOrNull().orEmpty()
            .filter { it.status == "ACTIVE" }

        val newCount = transactions.count { it.transactionType == "جديد" }
        val renewCount = transactions.count { it.transactionType == "تجديد" }
        val lostCount = transactions.count { it.transactionType == "بدل فاقد" }
        val damagedCount = transactions.count { it.transactionType == "بدل تالف" }

        val malesCount = transactions.count { it.gender == "ذكر" }
        val femalesCount = transactions.count { it.gender == "أنثى" }
        val totalTransactions = transactions.size

        val formSalesIncome = transactions.sumOf { it.salePrice }
        val totalStateShare = transactions.sumOf { it.stateShare }
        val totalDirectorateShare = transactions.sumOf { it.directorateShare }
        val totalFundShare = transactions.sumOf { it.fundShare }

        // Expenses for this date
        val expenses = db.expenseDao().getExpensesByDate(date).firstOrNull().orEmpty()
            .filter { it.status == "ACTIVE" }
        val totalExpenses = expenses.sumOf { it.amount }

        // Other income for this date (excluding form sales)
        val otherIncomes = db.incomeDao().getIncomeByDate(date).firstOrNull().orEmpty()
            .filter { it.status == "ACTIVE" && it.category != "دخل الاستمارات" }
        val otherIncomeTotal = otherIncomes.filter { it.category != "تسديد دين" }.sumOf { it.amount }
        val debtPaidTotal = otherIncomes.filter { it.category == "تسديد دين" }.sumOf { it.amount }

        // Total fund income is the net fund share + other income + debt repayment
        val totalIncome = totalFundShare + otherIncomeTotal + debtPaidTotal
        val netToday = totalIncome - totalExpenses
        val expectedBalance = closing.openingBalance + netToday
        val diff = closing.actualBalance - expectedBalance

        val updated = closing.copy(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netToday = netToday,
            expectedBalance = expectedBalance,
            difference = diff,
            totalTransactions = totalTransactions,
            malesCount = malesCount,
            femalesCount = femalesCount,
            newCount = newCount,
            renewCount = renewCount,
            lostCount = lostCount,
            damagedCount = damagedCount,
            totalStateShare = totalStateShare,
            totalDirectorateShare = totalDirectorateShare,
            totalFundShare = totalFundShare,
            otherIncome = otherIncomeTotal,
            debtPaidAmount = debtPaidTotal
        )

        db.dailyClosingDao().update(updated)
        updated
    }

    private suspend fun getOrCreateDailyClosing(date: String): DailyClosingEntity {
        val existing = db.dailyClosingDao().getDailyClosingSync(date)
        if (existing != null) return existing

        // Get previous day's closing to inherit opening balance
        val prevClosing = db.dailyClosingDao().getPreviousDailyClosing(date)
        val openingBalance = prevClosing?.expectedBalance ?: 0.0
        val hijri = HijriDateUtil.getHijriDateFromString(date).formatted

        val newClosing = DailyClosingEntity(
            gregorianDate = date,
            hijriDate = hijri,
            openingBalance = openingBalance,
            expectedBalance = openingBalance,
            actualBalance = openingBalance,
            difference = 0.0,
            status = "OPEN"
        )
        val id = db.dailyClosingDao().insert(newClosing)
        return newClosing.copy(id = id)
    }

    /**
     * Exports full database as JSON
     */
    suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("exportTime", System.currentTimeMillis())
        root.put("appName", "صندوق الأحوال المدنية")

        // Transactions
        val txs = db.transactionDao().getAllTransactions().firstOrNull().orEmpty()
        val txArr = JSONArray()
        txs.forEach {
            txArr.put(
                JSONObject().apply {
                    put("receiptNumber", it.receiptNumber)
                    put("citizenName", it.citizenName)
                    put("transactionType", it.transactionType)
                    put("gender", it.gender)
                    put("salePrice", it.salePrice)
                    put("stateShare", it.stateShare)
                    put("directorateShare", it.directorateShare)
                    put("fundShare", it.fundShare)
                    put("gregorianDate", it.gregorianDate)
                    put("hijriDate", it.hijriDate)
                    put("timeString", it.timeString)
                    put("status", it.status)
                }
            )
        }
        root.put("transactions", txArr)

        // Expenses
        val expenses = db.expenseDao().getAllExpenses().firstOrNull().orEmpty()
        val expArr = JSONArray()
        expenses.forEach {
            expArr.put(
                JSONObject().apply {
                    put("statement", it.statement)
                    put("amount", it.amount)
                    put("withWhom", it.withWhom)
                    put("gregorianDate", it.gregorianDate)
                    put("hijriDate", it.hijriDate)
                }
            )
        }
        root.put("expenses", expArr)

        // Fees
        val fees = db.feeSettingDao().getAllFeeSettings().firstOrNull().orEmpty()
        val feeArr = JSONArray()
        fees.forEach {
            feeArr.put(
                JSONObject().apply {
                    put("formType", it.formType)
                    put("typeNameArabic", it.typeNameArabic)
                    put("salePrice", it.salePrice)
                    put("stateShare", it.stateShare)
                    put("directorateShare", it.directorateShare)
                    put("fundShare", it.fundShare)
                }
            )
        }
        root.put("fees", feeArr)

        root.toString(2)
    }

    /**
     * Restores database from JSON string
     */
    suspend fun restoreDatabaseFromJson(jsonStr: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonStr)
            if (root.has("fees")) {
                val feeArr = root.getJSONArray("fees")
                for (i in 0 until feeArr.length()) {
                    val obj = feeArr.getJSONObject(i)
                    val fee = FeeSettingEntity(
                        formType = obj.getString("formType"),
                        typeNameArabic = obj.getString("typeNameArabic"),
                        salePrice = obj.getDouble("salePrice"),
                        stateShare = obj.getDouble("stateShare"),
                        directorateShare = obj.getDouble("directorateShare"),
                        fundShare = obj.getDouble("fundShare")
                    )
                    db.feeSettingDao().update(fee)
                }
            }
            val user = getCurrentUser()
            val today = HijriDateUtil.getTodayGregorianString()
            val hijri = HijriDateUtil.getHijriDate().formatted

            db.auditLogDao().insert(
                AuditLogEntity(
                    actionType = "استعادة نسخة احتياطية",
                    performedBy = "${user.fullName} (${user.role})",
                    gregorianDate = today,
                    hijriDate = hijri,
                    details = "تمت استعادة البيانات بنجاح من ملف النسخة الاحتياطية."
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("ملف النسخة الاحتياطية غير صالح: ${e.localizedMessage}"))
        }
    }

    /**
     * Records a payment to the Director from their accumulated share (حق الإدارة)
     */
    suspend fun payDirector(
        amount: Double,
        notes: String? = null
    ): Result<DirectorPaymentEntity> = withContext(Dispatchers.IO) {
        if (amount <= 0) {
            return@withContext Result.failure(Exception("يرجى إدخال مبلغ صحيح."))
        }

        val today = HijriDateUtil.getTodayGregorianString()
        val hijri = HijriDateUtil.getHijriDate().formatted
        val time = HijriDateUtil.getNowTimeString()
        val user = getCurrentUser()

        val payment = DirectorPaymentEntity(
            amount = amount,
            gregorianDate = today,
            hijriDate = hijri,
            timeString = time,
            notes = notes?.trim(),
            paidByName = user.fullName
        )
        val id = db.directorPaymentDao().insert(payment)
        val saved = payment.copy(id = id)

        // Record cash movement (خرج من الصندوق للمدير)
        val latestMovement = db.cashMovementDao().getLatestMovement()
        val currentBalance = latestMovement?.balanceAfter ?: 0.0
        val newBalance = currentBalance - amount

        val noteStr = if (!notes.isNullOrBlank()) " ($notes)" else ""
        db.cashMovementDao().insert(
            CashMovementEntity(
                movementType = "محاسبة المدير",
                statement = "صرف دفعة للمدير من حق الإدارة - $amount ريال$noteStr",
                amountIn = 0.0,
                amountOut = amount,
                balanceAfter = newBalance,
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                referenceId = id
            )
        )

        // Record as an expense so daily closing accounts for it
        db.expenseDao().insert(
            ExpenseEntity(
                statement = "صرف دفعة للمدير من مستحقات حق الإدارة$noteStr",
                amount = amount,
                withWhom = "مدير الإدارة",
                gregorianDate = today,
                hijriDate = hijri,
                timeString = time,
                notes = noteStr.ifBlank { null },
                createdByUserId = user.id,
                createdByName = user.fullName,
                status = "ACTIVE"
            )
        )

        // Audit Log
        db.auditLogDao().insert(
            AuditLogEntity(
                actionType = "محاسبة المدير",
                performedBy = "${user.fullName} (${user.role})",
                gregorianDate = today,
                hijriDate = hijri,
                newValue = "$amount ريال",
                details = "صرف دفعة لمدير الإدارة بمبلغ $amount ريال$noteStr"
            )
        )

        recalculateDailyClosing(today)
        Result.success(saved)
    }
}
