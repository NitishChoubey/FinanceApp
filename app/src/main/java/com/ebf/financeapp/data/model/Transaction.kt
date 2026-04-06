package com.ebf.financeapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class  TransactionType {INCOME ,  EXPENSE}

@Entity(
    tableName = "transactions" ,
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"] ,
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_DEFAULT
        )
    ] ,
    indices = [Index("categoryId")]
)

data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0 ,
    val amount:  Double ,
    val type: TransactionType ,
    val categoryId: Int ,
    val title: String ,
    val note: String = "" ,
    val date: Long = System.currentTimeMillis() ,
    val createdAt: Long = System.currentTimeMillis()
)
