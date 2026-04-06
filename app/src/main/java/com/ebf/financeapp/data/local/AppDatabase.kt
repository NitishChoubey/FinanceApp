package com.ebf.financeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ebf.financeapp.data.model.Category
import com.ebf.financeapp.data.model.DEFAULT_CATEGORIES
import com.ebf.financeapp.data.model.Goal
import com.ebf.financeapp.data.model.Transaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Transaction::class , Category::class , Goal::class],
    version = 1 ,
    exportSchema = false
)

@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase(){

    abstract fun transactionDao() : TransactionDao
    abstract fun categoryDao() : CategoryDao
    abstract fun goalDao() : GoalDao

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_db"
                )
                    .addCallback(object  : Callback(){
                        override fun onCreate(db: SupportSQLiteDatabase){
                            super.onCreate(db)
                            //seed default categories on first install
                            INSTANCE?.let{ database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    database.categoryDao().insertAll(DEFAULT_CATEGORIES)
                                    // Seed demo transactions so app doesn't open empty
                                    seedDemoData(database)
                                }

                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private suspend fun seedDemoData(db : AppDatabase){
            val now = System.currentTimeMillis()
            val day = 86_400_000L // ms in one day

            val demoTransactions = listOf(
                Transaction(amount = 85000.0,  type = com.ebf.financeapp.data.model.TransactionType.INCOME,  categoryId = 7, title = "Monthly Salary",      date = now - day * 2),
                Transaction(amount = 1200.0,   type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 1, title = "Groceries",           date = now - day * 1),
                Transaction(amount = 450.0,    type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 2, title = "Uber ride",           date = now - day * 1),
                Transaction(amount = 2500.0,   type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 5, title = "Netflix + Spotify",   date = now - day * 3),
                Transaction(amount = 800.0,    type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 1, title = "Restaurant dinner",   date = now - day * 4),
                Transaction(amount = 5000.0,   type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 6, title = "Electricity Bill",    date = now - day * 5),
                Transaction(amount = 3200.0,   type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 3, title = "T-shirt & Jeans",     date = now - day * 6),
                Transaction(amount = 10000.0,  type = com.ebf.financeapp.data.model.TransactionType.INCOME,  categoryId = 7, title = "Freelance payment",   date = now - day * 7),
                Transaction(amount = 600.0,    type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 4, title = "Pharmacy",            date = now - day * 8),
                Transaction(amount = 1500.0,   type = com.ebf.financeapp.data.model.TransactionType.EXPENSE, categoryId = 2, title = "Petrol",              date = now - day * 9),
            )
            demoTransactions.forEach { db.transactionDao().insert(it) }
        }
    }
}