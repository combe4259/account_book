package com.example.accountbook.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.accountbook.dto.ExpenseWithCategory // 추가!
import com.example.accountbook.view.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyExpenseScreen(
    selectedDate: Long,
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToAdd: (Long) -> Unit
) {
    val allExpensesWithCategory by viewModel.allExpensesWithCategory.observeAsState(emptyList())

    //팝업창 관리
    var selectedExpenseForDetail by remember { mutableStateOf<ExpenseWithCategory?>(null) } // 타입 변경!

    // 선택된 날짜의 지출만 필터링
    val dailyExpenses = remember(allExpensesWithCategory, selectedDate) {
        filterExpensesByDate(allExpensesWithCategory, selectedDate)
    }

    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
    val totalDailyExpense = dailyExpenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(dateFormat.format(Date(selectedDate)))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAdd(selectedDate) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "지출 추가")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // 일일 지출 요약 카드
            DailySummaryCard(
                totalExpense = totalDailyExpense,
                expenseCount = dailyExpenses.size
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 지출 목록
            if (dailyExpenses.isEmpty()) {
                EmptyDayState(
                    onAddExpense = { onNavigateToAdd(selectedDate) }
                )
            } else {
                DailyExpenseList(
                    expenses = dailyExpenses,
                    onDeleteExpense = { expense ->
                        // ExpenseWithCategory를 Expense로 변환해서 전달
                        viewModel.deleteExpense(expense.toExpense())
                    },
                    onExpenseClick = { expense ->
                        selectedExpenseForDetail = expense
                    }
                )
            }
        }

        selectedExpenseForDetail?.let { expense ->
            ExpenseDetailDialog(
                expense = expense,
                onDismiss = { selectedExpenseForDetail = null },
                onDelete = {
                    viewModel.deleteExpense(expense.toExpense())
                    selectedExpenseForDetail = null
                }
            )
        }
    }
}

@Composable
fun DailySummaryCard(
    totalExpense: Double,
    expenseCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "오늘의 지출",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${expenseCount}건",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Text(
                text = NumberFormat.getNumberInstance(Locale.KOREA).format(totalExpense) + "원",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun EmptyDayState(
    onAddExpense: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "이 날짜에는 지출 내역이 없습니다",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onAddExpense,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("지출 추가하기")
            }
        }
    }
}

@Composable
fun DailyExpenseList(
    expenses: List<ExpenseWithCategory>, // 타입 변경!
    onDeleteExpense: (ExpenseWithCategory) -> Unit, // 타입 변경!
    onExpenseClick: (ExpenseWithCategory) -> Unit // 타입 변경!
) {
    val sortedExpenses = remember(expenses) {
        expenses.sortedByDescending { it.date }
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(sortedExpenses) { expense ->
            DailyExpenseItem(
                expense = expense,
                onDelete = { onDeleteExpense(expense) },
                onClick = { onExpenseClick(expense) }
            )
        }
    }
}

@Composable
fun DailyExpenseItem(
    expense: ExpenseWithCategory, // 타입 변경!
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.KOREA)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 카테고리 아이콘 추가!
                expense.iconName?.let { iconName ->
                    Text(
                        text = getIconEmoji(iconName),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Column {
                    Text(
                        text = expense.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // 카테고리 이름 추가!
                    Text(
                        text = expense.categoryName ?: "카테고리 없음",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = timeFormat.format(Date(expense.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = NumberFormat.getNumberInstance(Locale.KOREA)
                        .format(expense.amount) + "원",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseDetailDialog(
    expense: ExpenseWithCategory, // 타입 변경!
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm", Locale.KOREA)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "지출 상세 정보",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 상품명
                DetailRow(
                    label = "상품명",
                    value = expense.productName
                )

                // 카테고리 (실제 이름 표시!)
                DetailRow(
                    label = "카테고리",
                    value = expense.categoryName ?: "카테고리 없음"
                )

                // 금액
                DetailRow(
                    label = "금액",
                    value = NumberFormat.getNumberInstance(Locale.KOREA)
                        .format(expense.amount) + "원"
                )

                // 날짜
                DetailRow(
                    label = "날짜",
                    value = dateFormat.format(Date(expense.date))
                )

                // 이미지 (있는 경우만)
                expense.photoUri?.let { imagePath ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "첨부 이미지",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 이미지 표시
                    AsyncImage(
                        model = imagePath,
                        contentDescription = "지출 이미지",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = android.R.drawable.ic_menu_report_image),
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("삭제")
            }
        }
    )
}

@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

// 유틸리티 함수들
fun filterExpensesByDate(allExpensesWithCategory: List<ExpenseWithCategory>, targetDate: Long): List<ExpenseWithCategory> { // 타입 변경!
    val targetCalendar = Calendar.getInstance().apply { timeInMillis = targetDate }
    val targetYear = targetCalendar.get(Calendar.YEAR)
    val targetMonth = targetCalendar.get(Calendar.MONTH)
    val targetDay = targetCalendar.get(Calendar.DAY_OF_MONTH)

    return allExpensesWithCategory.filter { expense ->
        val expenseCalendar = Calendar.getInstance().apply { timeInMillis = expense.date }
        expenseCalendar.get(Calendar.YEAR) == targetYear &&
                expenseCalendar.get(Calendar.MONTH) == targetMonth &&
                expenseCalendar.get(Calendar.DAY_OF_MONTH) == targetDay
    }.sortedByDescending { it.date }
}

// 아이콘 이모지 함수 추가!
private fun getIconEmoji(iconName: String): String {
    return when (iconName) {
        "restaurant" -> "🍽️"
        "directions_car" -> "🚗"
        "shopping_cart" -> "🛒"
        "local_hospital" -> "🏥"
        "movie" -> "🎬"
        "more_horiz" -> "📦"
        "coffee" -> "☕"
        "home" -> "🏠"
        "work" -> "💼"
        "school" -> "🏫"
        "sports" -> "⚽"
        "beauty" -> "💄"
        "gas_station" -> "⛽"
        "phone" -> "📱"
        "book" -> "📚"
        else -> "📦"
    }
}

// ExpenseWithCategory를 Expense로 변환하는 확장 함수
fun ExpenseWithCategory.toExpense() = com.example.accountbook.model.Expense(
    id = this.id,
    productName = this.productName,
    amount = this.amount,
    categoryId = this.categoryId,
    date = this.date,
    photoUri = this.photoUri
)