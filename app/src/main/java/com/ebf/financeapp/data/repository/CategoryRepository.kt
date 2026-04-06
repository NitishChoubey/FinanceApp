package com.ebf.financeapp.data.repository


import com.ebf.financeapp.data.local.CategoryDao
import com.ebf.financeapp.data.model.Category
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val dao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> = dao.getAllCategories()

    suspend fun getCategoryById(id: Int): Category? = dao.getCategoryById(id)

    suspend fun addCategory(category: Category) = dao.insert(category)
}