package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE gregorianDate = :date ORDER BY id DESC")
    fun getTransactionsByDate(date: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE gregorianDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getTransactionsBetween(startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE citizenName LIKE '%' || :query || '%' 
           OR receiptNumber LIKE '%' || :query || '%' 
           OR transactionType LIKE '%' || :query || '%'
           OR gregorianDate LIKE '%' || :query || '%'
        ORDER BY id DESC
    """)
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE gregorianDate = :date ORDER BY id DESC LIMIT 1")
    suspend fun getLastReceiptForDate(date: String): TransactionEntity?

    @Query("SELECT DISTINCT citizenName FROM transactions ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentCitizenNames(limit: Int = 30): List<String>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query("SELECT DISTINCT gregorianDate FROM transactions ORDER BY gregorianDate DESC")
    fun getAllTransactionDates(): Flow<List<String>>

    @Query("SELECT SUM(directorateShare) FROM transactions WHERE status = 'ACTIVE'")
    fun getTotalDirectorShareFlow(): Flow<Double?>

    @Query("SELECT SUM(directorateShare) FROM transactions WHERE status = 'ACTIVE'")
    suspend fun getTotalDirectorShareSync(): Double?

    @Query("SELECT COUNT(*) FROM transactions WHERE transactionType = 'جديد' AND status = 'ACTIVE'")
    fun getActiveNewCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM transactions WHERE transactionType = 'جديد' AND status = 'ACTIVE'")
    suspend fun getActiveNewCountSync(): Int

    @Query("SELECT * FROM transactions WHERE transactionType = 'جديد' AND status = 'ACTIVE' ORDER BY gregorianDate DESC, id DESC")
    fun getAllActiveNewTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT DISTINCT gregorianDate FROM transactions WHERE transactionType = 'جديد' AND status = 'ACTIVE' ORDER BY gregorianDate DESC")
    fun getDatesWithNewTransactions(): Flow<List<String>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)
}

@Dao
interface FeeSettingDao {
    @Query("SELECT * FROM fee_settings")
    fun getAllFeeSettings(): Flow<List<FeeSettingEntity>>

    @Query("SELECT * FROM fee_settings WHERE formType = :formType")
    suspend fun getFeeSettingSync(formType: String): FeeSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fees: List<FeeSettingEntity>)

    @Update
    suspend fun update(fee: FeeSettingEntity)
}

@Dao
interface IncomeDao {
    @Query("SELECT * FROM income ORDER BY id DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE gregorianDate = :date ORDER BY id DESC")
    fun getIncomeByDate(date: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE gregorianDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getIncomeBetween(startDate: String, endDate: String): Flow<List<IncomeEntity>>

    @Query("SELECT * FROM income WHERE id = :id")
    suspend fun getIncomeById(id: Long): IncomeEntity?

    @Query("DELETE FROM income WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: IncomeEntity): Long

    @Update
    suspend fun update(income: IncomeEntity)
}

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE gregorianDate = :date ORDER BY id DESC")
    fun getExpensesByDate(date: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE gregorianDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getExpensesBetween(startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity): Long

    @Update
    suspend fun update(expense: ExpenseEntity)
}

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY id DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE gregorianDate = :date ORDER BY id DESC")
    fun getDebtsByDate(date: String): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE gregorianDate = :date")
    suspend fun getDebtsByDateSync(date: String): List<DebtEntity>

    @Query("SELECT * FROM debts WHERE status != 'FULLY_PAID' ORDER BY id DESC")
    fun getActiveDebts(): Flow<List<DebtEntity>>

    @Query("SELECT SUM(remainingAmount) FROM debts WHERE status != 'FULLY_PAID'")
    fun getTotalRemainingDebtsFlow(): Flow<Double?>

    @Query("SELECT SUM(remainingAmount) FROM debts WHERE status != 'FULLY_PAID'")
    suspend fun getTotalRemainingDebtsSync(): Double?

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity): Long

    @Update
    suspend fun update(debt: DebtEntity)
}

@Dao
interface DebtPaymentDao {
    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY id DESC")
    fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments WHERE gregorianDate = :date ORDER BY id DESC")
    fun getPaymentsByDate(date: String): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments WHERE gregorianDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getPaymentsBetween(startDate: String, endDate: String): Flow<List<DebtPaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: DebtPaymentEntity): Long
}

@Dao
interface CashMovementDao {
    @Query("SELECT * FROM cash_movements ORDER BY id DESC")
    fun getAllMovements(): Flow<List<CashMovementEntity>>

    @Query("SELECT * FROM cash_movements WHERE gregorianDate = :date ORDER BY id DESC")
    fun getMovementsByDate(date: String): Flow<List<CashMovementEntity>>

    @Query("SELECT * FROM cash_movements WHERE gregorianDate BETWEEN :startDate AND :endDate ORDER BY id DESC")
    fun getMovementsBetween(startDate: String, endDate: String): Flow<List<CashMovementEntity>>

    @Query("SELECT * FROM cash_movements ORDER BY id DESC LIMIT 1")
    suspend fun getLatestMovement(): CashMovementEntity?

    @Query("DELETE FROM cash_movements WHERE referenceId = :referenceId AND movementType = :movementType")
    suspend fun deleteByReferenceAndType(referenceId: Long, movementType: String)

    @Query("DELETE FROM cash_movements WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(movement: CashMovementEntity): Long
}

@Dao
interface DailyClosingDao {
    @Query("SELECT * FROM daily_closings WHERE gregorianDate = :date")
    fun getDailyClosing(date: String): Flow<DailyClosingEntity?>

    @Query("SELECT * FROM daily_closings WHERE gregorianDate = :date")
    suspend fun getDailyClosingSync(date: String): DailyClosingEntity?

    @Query("SELECT * FROM daily_closings WHERE gregorianDate < :date ORDER BY gregorianDate DESC LIMIT 1")
    suspend fun getPreviousDailyClosing(date: String): DailyClosingEntity?

    @Query("SELECT * FROM daily_closings ORDER BY gregorianDate DESC")
    fun getAllClosings(): Flow<List<DailyClosingEntity>>

    @Query("SELECT * FROM daily_closings WHERE gregorianDate BETWEEN :startDate AND :endDate ORDER BY gregorianDate ASC")
    fun getClosingsBetween(startDate: String, endDate: String): Flow<List<DailyClosingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(closing: DailyClosingEntity): Long

    @Update
    suspend fun update(closing: DailyClosingEntity)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY id DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity): Long
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<UserEntity>)

    @Update
    suspend fun update(user: UserEntity)
}

@Dao
interface AppSettingDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<String?>

    @Query("SELECT value FROM app_settings WHERE `key` = :key")
    suspend fun getSettingSync(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}

@Dao
interface DirectorPaymentDao {
    @Query("SELECT * FROM director_payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<DirectorPaymentEntity>>

    @Query("SELECT * FROM director_payments WHERE gregorianDate = :date ORDER BY id DESC")
    fun getPaymentsByDate(date: String): Flow<List<DirectorPaymentEntity>>

    @Query("SELECT SUM(amount) FROM director_payments")
    fun getTotalPaidFlow(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM director_payments")
    suspend fun getTotalPaidSync(): Double?

    @Query("SELECT * FROM director_payments WHERE id = :id")
    suspend fun getPaymentById(id: Long): DirectorPaymentEntity?

    @Query("DELETE FROM director_payments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: DirectorPaymentEntity): Long
}

