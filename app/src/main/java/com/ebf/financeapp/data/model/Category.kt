package com.ebf.financeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0 ,
    val name:  String,
    val icon:  String ,
    val colorHex:  String ,
    val isDefault:  Boolean = true

)

// Pre-seeded category list — injected on first DB creation
val DEFAULT_CATEGORIES = listOf(
    Category(name = "Food & Dining",    icon = "restaurant",      colorHex = "#378ADD"),
    Category(name = "Transport",        icon = "directions_car",  colorHex = "#EF9F27"),
    Category(name = "Shopping",         icon = "shopping_bag",    colorHex = "#D4537E"),
    Category(name = "Health",           icon = "favorite",        colorHex = "#1D9E75"),
    Category(name = "Entertainment",    icon = "movie",           colorHex = "#8B5CF6"),
    Category(name = "Bills & Utilities",icon = "receipt_long",    colorHex = "#E24B4A"),
    Category(name = "Salary",           icon = "payments",        colorHex = "#10B981"),
    Category(name = "Education",        icon = "school",          colorHex = "#6366F1"),
    Category(name = "Travel",           icon = "flight",          colorHex = "#F59E0B"),
    Category(name = "Investments",      icon = "trending_up",     colorHex = "#0EA5E9"),
    Category(name = "Gifts",            icon = "card_giftcard",   colorHex = "#EC4899"),
    Category(name = "Other",            icon = "category",        colorHex = "#64748B"),
)