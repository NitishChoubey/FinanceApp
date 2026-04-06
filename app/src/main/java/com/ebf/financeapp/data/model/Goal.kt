package com.ebf.financeapp.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals" ,
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"] ,
            childColumns = ["categoryId"] ,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)

data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0 ,
    val categoryId: Int ,
    val budgetLimit: Double ,
    val month: Int ,
    val year: Int ,
    val createdAt: Long = System.currentTimeMillis()
)