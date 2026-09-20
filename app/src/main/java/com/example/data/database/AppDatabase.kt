package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AppSettingDao
import com.example.data.dao.AuditLogDao
import com.example.data.dao.CashMovementDao
import com.example.data.dao.DailyClosingDao
import com.example.data.dao.DebtDao
import com.example.data.dao.DebtPaymentDao
import com.example.data.dao.DirectorPaymentDao
import com.example.data.dao.ExpenseDao
import com.example.data.dao.FeeSettingDao
import com.example.data.dao.IncomeDao
import com.example.data.dao.TransactionDao
import com.example.data.dao.UserDao
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        FeeSettingEntity::class,
        TransactionEntity::class,
        IncomeEntity::class,
        ExpenseEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        DirectorPaymentEntity::class,
        CashMovementEntity::class,
        DailyClosingEntity::class,
        AuditLogEntity::class,
        AppSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun feeSettingDao(): FeeSettingDao
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun debtDao(): DebtDao
    abstract fun debtPaymentDao(): DebtPaymentDao
    abstract fun directorPaymentDao(): DirectorPaymentDao
    abstract fun cashMovementDao(): CashMovementDao
    abstract fun dailyClosingDao(): DailyClosingDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun userDao(): UserDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "civil_status_fund.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default data on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                val database = getDatabase(context)
                                populateInitialData(database)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            // Default Fees
            database.feeSettingDao().insertAll(
                listOf(
                    FeeSettingEntity(
                        formType = "NEW",
                        typeNameArabic = "جديد",
                        salePrice = 4500.0,
                        stateShare = 3750.0,
                        directorateShare = 200.0,
                        fundShare = 550.0
                    ),
                    FeeSettingEntity(
                        formType = "RENEW",
                        typeNameArabic = "تجديد",
                        salePrice = 4000.0,
                        stateShare = 3750.0,
                        directorateShare = 0.0,
                        fundShare = 250.0
                    ),
                    FeeSettingEntity(
                        formType = "LOST",
                        typeNameArabic = "بدل فاقد",
                        salePrice = 9000.0,
                        stateShare = 8750.0,
                        directorateShare = 0.0,
                        fundShare = 250.0
                    ),
                    FeeSettingEntity(
                        formType = "DAMAGED",
                        typeNameArabic = "بدل تالف",
                        salePrice = 7000.0,
                        stateShare = 6750.0,
                        directorateShare = 0.0,
                        fundShare = 250.0
                    )
                )
            )

            // Default Users
            database.userDao().insertAll(
                listOf(
                    UserEntity(
                        id = 1,
                        username = "admin",
                        fullName = "مدير الإدارة",
                        role = "ADMIN",
                        pin = "1234"
                    ),
                    UserEntity(
                        id = 2,
                        username = "cashier",
                        fullName = "أمين الصندوق",
                        role = "CASHIER",
                        pin = "1234"
                    )
                )
            )

            // Default Settings
            database.appSettingDao().setSetting(
                AppSettingEntity("DIRECTORATE_NAME", "مصلحة الأحوال المدنية والسجل المدني")
            )
            database.appSettingDao().setSetting(
                AppSettingEntity("CURRENT_USER_ID", "2")
            )
        }
    }
}
