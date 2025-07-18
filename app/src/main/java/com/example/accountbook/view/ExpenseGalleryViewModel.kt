package com.example.accountbook.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.accountbook.dto.ExpenseWithCategory
import kotlinx.coroutines.launch
import com.example.accountbook.repository.ExpenseRepository

// 갤러리 화면의 상태를 관리
data class GalleryUiState(
    val expensesWithImages: List<ExpenseWithCategory> = emptyList(),  // ExpenseWithCategory로 변경!
    val isLoading: Boolean = true,
    val selectedExpense: ExpenseWithCategory? = null,
    val showDetailDialog: Boolean = false
)

class ExpenseGalleryViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(GalleryUiState())
    val uiState: LiveData<GalleryUiState> = _uiState

    init {
        loadExpensesWithImages()
    }

    private fun loadExpensesWithImages() {
        _uiState.value = _uiState.value?.copy(isLoading = true)

        // ExpenseWithCategory 데이터 사용 - 타입 일치!
        repository.getExpensesWithPhotosAndCategory().observeForever { expenses ->
            _uiState.value = _uiState.value?.copy(
                isLoading = false,
                expensesWithImages = expenses ?: emptyList()  // 이제 타입이 맞음!
            )
        }
    }

    fun showImageDetail(expense: ExpenseWithCategory) {
        _uiState.value = _uiState.value?.copy(
            selectedExpense = expense,
            showDetailDialog = true
        )
    }

    fun hideImageDetail() {
        _uiState.value = _uiState.value?.copy(
            selectedExpense = null,
            showDetailDialog = false
        )
    }

    fun refreshGallery() {
        loadExpensesWithImages()
    }
}