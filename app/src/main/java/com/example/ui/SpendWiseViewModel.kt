package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AiInsight
import com.example.data.model.BudgetEntity
import com.example.data.model.CopilotMessage
import com.example.data.model.GoalEntity
import com.example.data.model.RecurringEntity
import com.example.data.model.RiskAlert
import com.example.data.model.SavingOpportunity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserProfile
import com.example.data.repository.SpendWiseRepository
import com.example.domain.analysis.CategorizationEngine
import com.example.domain.analysis.DetailedHealthOverview
import com.example.domain.analysis.FinancialHealthDiagnostics
import com.example.domain.analysis.RiskDetectionEngine
import com.example.domain.analysis.SavingOpportunitiesEngine
import com.example.domain.analysis.SpendingAnalytics
import com.example.domain.analysis.SpendingAnalyticsEngine
import com.example.domain.analysis.SpendWiseCopilotEngine
import com.example.domain.data.CsvImportResult
import com.example.domain.data.CsvParser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SpendWiseScreen(val title: String) {
    LANDING("Welcome"),
    AUTH("Sign In / Register"),
    ONBOARDING("Get Started"),
    DASHBOARD("Dashboard"),
    TRANSACTIONS("Ledger & Activity"),
    BUDGETS("Budgets"),
    ANALYSIS("Spending Analysis"),
    RISK("Risk Center"),
    RECURRING("Recurring Payments"),
    GOALS("Financial Goals"),
    COPILOT("SpendWise Copilot"),
    INSIGHTS("AI Insights"),
    REPORTS("Monthly Reports"),
    SETTINGS("Settings")
}

class SpendWiseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SpendWiseRepository(application.applicationContext)

    private val _currentScreen = MutableStateFlow(SpendWiseScreen.DASHBOARD)
    val currentScreen: StateFlow<SpendWiseScreen> = _currentScreen.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.userProfile
    val transactions: StateFlow<List<TransactionEntity>> = repository.transactions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val budgets: StateFlow<List<BudgetEntity>> = repository.budgets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val goals: StateFlow<List<GoalEntity>> = repository.goals.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val recurring: StateFlow<List<RecurringEntity>> = repository.recurring.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    val aiInsights: StateFlow<List<AiInsight>> = repository.aiInsights
    val isGeneratingAi: StateFlow<Boolean> = repository.isGeneratingAi

    // Search and Filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow<String?>(null) // "ALL", "EXPENSE", "INCOME"
    val typeFilter: StateFlow<String?> = _typeFilter.asStateFlow()

    private val _sortAscending = MutableStateFlow(false)
    val sortAscending: StateFlow<Boolean> = _sortAscending.asStateFlow()

    // Analytics and Risk Alerts
    val analytics: StateFlow<SpendingAnalytics> = combine(
        repository.transactions,
        repository.budgets,
        repository.userProfile
    ) { txList, bgList, profile ->
        SpendingAnalyticsEngine.calculate(
            transactions = txList,
            budgets = bgList,
            monthlyIncome = profile.monthlyIncome,
            targetBudget = profile.targetBudget
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        SpendingAnalyticsEngine.calculate(emptyList(), emptyList(), 4200.0, 2740.0)
    )

    val riskAlerts: StateFlow<List<RiskAlert>> = combine(
        repository.transactions,
        repository.budgets,
        repository.userProfile
    ) { txList, bgList, profile ->
        RiskDetectionEngine.analyze(
            transactions = txList,
            budgets = bgList,
            monthlyIncome = profile.monthlyIncome,
            targetBudget = profile.targetBudget
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val savingOpportunities: StateFlow<List<SavingOpportunity>> = combine(
        analytics,
        repository.transactions,
        repository.budgets,
        repository.userProfile
    ) { an, txList, bgList, profile ->
        SavingOpportunitiesEngine.evaluate(
            analytics = an,
            transactions = txList,
            budgets = bgList,
            currencySymbol = profile.currencySymbol
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val detailedHealthOverview: StateFlow<DetailedHealthOverview> = combine(
        analytics,
        repository.transactions,
        repository.budgets
    ) { an, txList, bgList ->
        FinancialHealthDiagnostics.computeDetailedOverview(an, txList, bgList)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FinancialHealthDiagnostics.computeDetailedOverview(
            SpendingAnalyticsEngine.calculate(emptyList(), emptyList(), 4200.0, 2740.0),
            emptyList(),
            emptyList()
        )
    )

    // Copilot State
    private val _copilotMessages = MutableStateFlow<List<CopilotMessage>>(
        listOf(
            CopilotMessage(
                text = "Hello! I'm your SpendWise Copilot. I analyze your real spending patterns, flag budget risks, and detect saving opportunities. What would you like to explore today?",
                isUser = false,
                suggestedPrompts = listOf(
                    "Where did I spend the most this month?",
                    "What subscriptions do I have?",
                    "Why did my expenses increase?",
                    "How much did I spend on food?",
                    "Show me unusual transactions.",
                    "Help me understand my budget."
                )
            )
        )
    )
    val copilotMessages: StateFlow<List<CopilotMessage>> = _copilotMessages.asStateFlow()

    private val _isCopilotThinking = MutableStateFlow(false)
    val isCopilotThinking: StateFlow<Boolean> = _isCopilotThinking.asStateFlow()

    fun navigateTo(screen: SpendWiseScreen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun setTypeFilter(type: String?) {
        _typeFilter.value = type
    }

    fun toggleSort() {
        _sortAscending.value = !_sortAscending.value
    }

    fun addTransaction(
        merchant: String,
        amount: Double,
        type: String,
        category: String,
        notes: String,
        isFlagged: Boolean = false,
        riskReason: String? = null
    ) {
        viewModelScope.launch {
            val assignedCat = if (category.isBlank()) CategorizationEngine.categorize(merchant).category else category
            val tx = TransactionEntity(
                merchant = merchant.trim(),
                amount = amount,
                type = type,
                category = assignedCat,
                notes = notes.trim(),
                isFlagged = isFlagged,
                riskReason = riskReason,
                date = System.currentTimeMillis()
            )
            repository.addTransaction(tx)
        }
    }

    fun updateTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(tx)
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    fun importCsv(csvContent: String): CsvImportResult {
        val existing = transactions.value
        val result = CsvParser.parse(csvContent, existing)
        val validEntities = result.rows.filter { it.isValid }.map {
            TransactionEntity(
                merchant = it.merchant,
                amount = it.amount,
                type = it.type,
                category = it.category,
                date = it.date,
                notes = "Imported from CSV file"
            )
        }
        if (validEntities.isNotEmpty()) {
            viewModelScope.launch {
                repository.addTransactions(validEntities)
            }
        }
        return result
    }

    fun saveBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val existing = budgets.value.find { it.category.equals(category, true) }
            val entity = existing?.copy(monthlyLimit = limit) ?: BudgetEntity(category = category, monthlyLimit = limit)
            repository.saveBudget(entity)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Goals Management
    fun saveGoal(
        title: String,
        targetAmount: Double,
        currentAmount: Double,
        deadline: String,
        monthlyContribution: Double,
        category: String,
        icon: String
    ) {
        viewModelScope.launch {
            val goal = GoalEntity(
                title = title.trim(),
                targetAmount = targetAmount,
                currentAmount = currentAmount,
                deadline = deadline.trim(),
                monthlyContribution = monthlyContribution,
                category = category,
                icon = icon
            )
            repository.saveGoal(goal)
        }
    }

    fun contributeToGoal(goalId: String, amount: Double) {
        viewModelScope.launch {
            repository.contributeToGoal(goalId, amount)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Recurring Subscriptions Management
    fun saveRecurring(
        merchant: String,
        amount: Double,
        frequency: String,
        category: String,
        nextPaymentDate: String,
        status: String
    ) {
        viewModelScope.launch {
            val item = RecurringEntity(
                merchant = merchant.trim(),
                amount = amount,
                frequency = frequency,
                category = category,
                nextPaymentDate = nextPaymentDate,
                status = status
            )
            repository.saveRecurring(item)
        }
    }

    fun updateRecurringStatus(id: String, status: String) {
        viewModelScope.launch {
            repository.updateRecurringStatus(id, status)
        }
    }

    fun deleteRecurring(item: RecurringEntity) {
        viewModelScope.launch {
            repository.deleteRecurring(item)
        }
    }

    // SpendWise Copilot
    fun askCopilot(question: String) {
        if (question.isBlank()) return

        val userMsg = CopilotMessage(
            text = question.trim(),
            isUser = true
        )
        _copilotMessages.value = _copilotMessages.value + userMsg

        viewModelScope.launch {
            _isCopilotThinking.value = true
            delay(400) // smooth interaction pacing

            val answer = SpendWiseCopilotEngine.generateAnswer(
                question = question,
                analytics = analytics.value,
                transactions = transactions.value,
                budgets = budgets.value,
                currencySymbol = userProfile.value.currencySymbol
            )

            val nextPrompts = when {
                question.contains("food", true) -> listOf("Where did I spend the most this month?", "Show me unusual transactions.")
                question.contains("subscription", true) -> listOf("Why did my expenses increase?", "Help me understand my budget.")
                question.contains("budget", true) -> listOf("How can I reach my savings goal faster?", "What subscriptions do I have?")
                else -> listOf("Where did I spend the most this month?", "What subscriptions do I have?", "How can I reach my savings goal faster?")
            }

            val botMsg = CopilotMessage(
                text = answer,
                isUser = false,
                suggestedPrompts = nextPrompts
            )
            _copilotMessages.value = _copilotMessages.value + botMsg
            _isCopilotThinking.value = false
        }
    }

    fun refreshAi() {
        viewModelScope.launch {
            repository.refreshAiInsights()
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            repository.seedDemoData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun updateProfile(profile: UserProfile) {
        repository.updateUserProfile(profile)
    }

    fun completeOnboarding(
        income: Double,
        budget: Double,
        currency: String,
        categories: List<String>,
        goal: String
    ) {
        val updated = userProfile.value.copy(
            monthlyIncome = income,
            targetBudget = budget,
            currencySymbol = currency,
            mainCategories = categories,
            financialGoal = goal,
            isOnboarded = true
        )
        repository.updateUserProfile(updated)
        navigateTo(SpendWiseScreen.DASHBOARD)
    }
}
