package com.example.accountbook.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.accountbook.local.ExpenseDatabase
import com.example.accountbook.model.Expense
import com.example.accountbook.model.Income
import com.example.accountbook.dto.ExpenseWithCategory
import com.example.accountbook.dto.IncomeWithCategory
import com.example.accountbook.model.ExpenseCategory
import com.example.accountbook.model.IncomeCategory
import com.example.accountbook.repository.ExpenseRepository
import com.example.accountbook.repository.ExpenseCategoryRepository
import com.example.accountbook.repository.IncomeCategoryRepository
import com.example.accountbook.repository.IncomeRepository
import kotlinx.coroutines.launch

/**
 * 가계부 앱의 메인 ViewModel
 * 지출, 소득, 카테고리 관리를 모두 담당하는 통합 ViewModel입니다.
 *
 * 이 ViewModel은 앱의 모든 핵심 기능을 하나의 클래스에서 관리하여
 * 개발 초기 단계의 복잡성을 줄이고 빠른 프로토타이핑을 가능하게 합니다.
 */
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    // Repository 인스턴스들 - 각각의 데이터 영역을 담당합니다
    private val expenseRepository: ExpenseRepository
    private val expenseCategoryRepository: ExpenseCategoryRepository
    private val incomeRepository: IncomeRepository
    private val incomeCategoryRepository: IncomeCategoryRepository

    // === 지출 관련 LiveData ===
    // 이 LiveData들은 UI에서 실시간으로 관찰하여 자동 업데이트가 가능합니다
    val allExpenses: LiveData<List<Expense>>
    val allExpensesWithCategory: LiveData<List<ExpenseWithCategory>>
    val expensesWithPhotos: LiveData<List<Expense>>
    val expensesWithPhotosAndCategory: LiveData<List<ExpenseWithCategory>>

    // === 소득 관련 LiveData ===
    val allIncomes: LiveData<List<Income>>
    val allIncomesWithCategory: LiveData<List<IncomeWithCategory>>

    // === 카테고리 관련 LiveData ===
    val allExpenseCategories: LiveData<List<ExpenseCategory>>
    val allIncomeCategories: LiveData<List<IncomeCategory>>


    init {
        // 데이터베이스 인스턴스와 DAO들을 가져옵니다
        val database = ExpenseDatabase.getDatabase(application)
        val expenseDao = database.expenseDao()
        val incomeDao = database.incomeDao()
        val expenseCategoryDao = database.expenseCategoryDao()
        val incomeCategoryDao = database.incomeCategoryDao()

        // Repository 인스턴스들을 초기화합니다
        // 각 Repository는 해당 도메인의 비즈니스 로직을 캡슐화합니다
        expenseRepository = ExpenseRepository(expenseDao)
        expenseCategoryRepository = ExpenseCategoryRepository(expenseCategoryDao)
        incomeRepository = IncomeRepository(incomeDao)
        incomeCategoryRepository = IncomeCategoryRepository(incomeCategoryDao)

        // LiveData들을 Repository에서 가져와서 초기화합니다
        // 이렇게 하면 데이터베이스의 변경사항이 UI에 자동으로 반영됩니다
        allExpenses = expenseRepository.allExpenses
        allExpensesWithCategory = expenseRepository.getAllExpensesWithCategory()
        expensesWithPhotos = expenseRepository.getExpensesWithValidPhotos()
        expensesWithPhotosAndCategory = expenseRepository.getExpensesWithPhotosAndCategory()

        allIncomes = incomeRepository.allIncomes
        allIncomesWithCategory = incomeRepository.getAllIncomesWithCategory()

        allExpenseCategories = expenseCategoryRepository.getAllCategories()
        allIncomeCategories = incomeCategoryRepository.getAllCategories()

        // 앱 첫 실행시 기본 카테고리들을 자동으로 생성합니다
        // 이렇게 하면 사용자가 바로 앱을 사용할 수 있습니다
        viewModelScope.launch {
            expenseCategoryRepository.insertDefaultExpenseCategories()
            incomeCategoryRepository.insertDefaultIncomeCategories()
        }
    }

    // ========================================
    // 지출 관련 메서드들
    // ========================================

    /**
     * 새로운 지출을 추가합니다
     * @param expense 추가할 지출 객체
     */
    fun insertExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.insertExpense(expense)
    }

    /**
     * 기존 지출 정보를 수정합니다
     * @param expense 수정된 지출 객체
     */
    fun updateExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.updateExpense(expense)
    }

    /**
     * 지출을 삭제합니다
     * @param expense 삭제할 지출 객체
     */
    fun deleteExpense(expense: Expense) = viewModelScope.launch {
        expenseRepository.deleteExpense(expense)
    }

    /**
     * ID로 지출을 삭제합니다 (더 직접적인 방법)
     * @param id 삭제할 지출의 ID
     */
    fun deleteExpenseById(id: Long) = viewModelScope.launch {
        expenseRepository.deleteExpenseById(id)
    }

    /**
     * 특정 ID의 지출을 조회합니다
     * @param id 조회할 지출의 ID
     * @return 지출 객체를 담은 LiveData (없으면 null)
     */
    fun getExpenseById(id: Long): LiveData<Expense?> = liveData {
        emit(expenseRepository.getExpenseById(id))
    }

    /**
     * 지출의 카테고리를 변경합니다
     * @param expenseId 지출 ID
     * @param categoryId 새로운 카테고리 ID (null이면 카테고리 없음)
     */
    fun updateExpenseCategory(expenseId: Long, categoryId: Long?) = viewModelScope.launch {
        expenseRepository.updateExpenseCategory(expenseId, categoryId)
    }

    /**
     * 지출의 사진을 추가하거나 변경합니다
     * @param expenseId 지출 ID
     * @param photoUri 사진 URI (null이면 사진 제거)
     */
    fun updateExpensePhoto(expenseId: Long, photoUri: String?) = viewModelScope.launch {
        expenseRepository.updateExpensePhoto(expenseId, photoUri)
    }

    // ========================================
    // 소득 관련 메서드들
    // ========================================

    /**
     * 새로운 소득을 추가합니다
     * @param income 추가할 소득 객체
     */
    fun insertIncome(income: Income) = viewModelScope.launch {
        incomeRepository.insertIncome(income)
    }

    /**
     * 기존 소득 정보를 수정합니다
     * @param income 수정된 소득 객체
     */
    fun updateIncome(income: Income) = viewModelScope.launch {
        incomeRepository.updateIncome(income)
    }

    /**
     * 소득을 삭제합니다
     * @param income 삭제할 소득 객체
     */
    fun deleteIncome(income: Income) = viewModelScope.launch {
        incomeRepository.deleteIncome(income)
    }

    // ========================================
    // 지출 카테고리 관리 메서드들
    // ========================================

    /**
     * 새로운 지출 카테고리를 추가합니다
     * @param category 추가할 카테고리 객체
     */
    fun insertExpenseCategory(category: ExpenseCategory) = viewModelScope.launch {
        expenseCategoryRepository.insertCategory(category)
    }

    /**
     * 새로운 지출 카테고리를 생성합니다 (유효성 검사 포함)
     * @param name 카테고리 이름
     * @param iconName 아이콘 이름
     */
    fun createExpenseCategory(name: String, iconName: String) = viewModelScope.launch {
        expenseCategoryRepository.createCategory(name, iconName)
    }

    /**
     * 지출 카테고리 정보를 수정합니다
     * @param category 수정된 카테고리 객체
     */
    fun updateExpenseCategory(category: ExpenseCategory) = viewModelScope.launch {
        expenseCategoryRepository.updateCategory(category)
    }

    /**
     * 지출 카테고리 이름을 수정합니다 (중복 체크 포함)
     * @param categoryId 카테고리 ID
     * @param newName 새로운 이름
     */
    fun updateExpenseCategoryName(categoryId: Long, newName: String) = viewModelScope.launch {
        expenseCategoryRepository.updateCategoryName(categoryId, newName)
    }

    /**
     * 지출 카테고리를 삭제합니다
     * @param category 삭제할 카테고리 객체
     */
    fun deleteExpenseCategory(category: ExpenseCategory) = viewModelScope.launch {
        expenseCategoryRepository.deleteCategory(category)
    }

    /**
     * 특정 ID의 지출 카테고리를 조회합니다
     * @param id 조회할 카테고리의 ID
     * @return 카테고리 객체를 담은 LiveData (없으면 null)
     */
    fun getExpenseCategoryById(id: Long): LiveData<ExpenseCategory?> = liveData {
        emit(expenseCategoryRepository.getCategoryById(id))
    }

    /**
     * 지출 카테고리 이름 중복을 체크합니다
     * @param name 체크할 이름
     * @return 중복 여부를 담은 LiveData
     */
    fun isExpenseCategoryNameExists(name: String): LiveData<Boolean> = liveData {
        emit(expenseCategoryRepository.isCategoryNameExists(name))
    }

    // ========================================
    // 소득 카테고리 관리 메서드들
    // ========================================

//    /**
//     * 새로운 소득 카테고리를 추가합니다
//     * @param category 추가할 카테고리 객체
//     */
//    fun insertIncomeCategory(category: IncomeCategory) = viewModelScope.launch {
//        incomeCategoryRepository.insertCategory(category)
//    }

    /**
     * 소득 카테고리 정보를 수정합니다
     * @param category 수정된 카테고리 객체
     */
    fun updateIncomeCategory(category: IncomeCategory) = viewModelScope.launch {
        incomeCategoryRepository.updateCategory(category)
    }

    /**
     * 소득 카테고리를 삭제합니다
     * @param category 삭제할 카테고리 객체
     */
    fun deleteIncomeCategory(category: IncomeCategory) = viewModelScope.launch {
        incomeCategoryRepository.deleteCategory(category)
    }

    /**
     * 소득 카테고리 이름 중복을 체크합니다
     * @param name 체크할 이름
     * @return 중복 여부를 담은 LiveData
     */
    fun isIncomeCategoryNameExists(name: String): LiveData<Boolean> = liveData {
        emit(incomeCategoryRepository.isCategoryNameExists(name))
    }

    // ========================================
    // 조회 및 분석 메서드들
    // ========================================

    /**
     * 특정 카테고리의 지출들을 조회합니다
     * @param categoryId 카테고리 ID
     * @return 해당 카테고리의 지출 목록을 담은 LiveData
     */
    fun getExpensesByCategory(categoryId: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseRepository.getExpensesByCategory(categoryId)
    }

    /**
     * 특정 날짜 범위의 지출들을 조회합니다
     * @param startDate 시작 날짜 (timestamp)
     * @param endDate 종료 날짜 (timestamp)
     * @return 해당 기간의 지출 목록을 담은 LiveData
     */
    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>> {
        return expenseRepository.getExpensesByDateRange(startDate, endDate)
    }

    // TODO: 향후 필요할 때 소득 관련 조회 메서드들도 추가할 수 있습니다
    // fun getIncomesByCategory(categoryId: Long): LiveData<List<IncomeWithCategory>>
    // fun getIncomesByDateRange(startDate: Long, endDate: Long): LiveData<List<IncomeWithCategory>>

    // TODO: 달력 화면을 위한 통합 분석 메서드들도 추가 가능합니다
    // fun getDailyFinancialSummary(year: Int, month: Int): LiveData<Map<String, DailyTotal>>
    // fun getMonthlyTotals(startDate: Long, endDate: Long): LiveData<MonthlyFinancialSummary>
}