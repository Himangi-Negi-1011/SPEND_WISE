package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BudgetEntity
import com.example.data.model.TransactionEntity
import com.example.ui.SpendWiseScreen
import com.example.ui.SpendWiseViewModel
import com.example.ui.components.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: SpendWiseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendWiseTheme(darkTheme = true) {
                SpendWiseApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpendWiseApp(viewModel: SpendWiseViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val budgets by viewModel.budgets.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val recurring by viewModel.recurring.collectAsStateWithLifecycle()
    val savingOpportunities by viewModel.savingOpportunities.collectAsStateWithLifecycle()
    val detailedHealthOverview by viewModel.detailedHealthOverview.collectAsStateWithLifecycle()
    val copilotMessages by viewModel.copilotMessages.collectAsStateWithLifecycle()
    val isCopilotThinking by viewModel.isCopilotThinking.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val riskAlerts by viewModel.riskAlerts.collectAsStateWithLifecycle()
    val aiInsights by viewModel.aiInsights.collectAsStateWithLifecycle()
    val isGeneratingAi by viewModel.isGeneratingAi.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val timeHorizon by viewModel.timeHorizon.collectAsStateWithLifecycle()
    val selectedDayIndex by viewModel.selectedDayIndex.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Dialog States
    var showAddEditTxDialog by remember { mutableStateOf(false) }
    var editingTx by remember { mutableStateOf<TransactionEntity?>(null) }
    var showCsvImportDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var editingBudget by remember { mutableStateOf<BudgetEntity?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddRecurringDialog by remember { mutableStateOf(false) }
    var showHealthDiagnosticsDialog by remember { mutableStateOf(false) }

    // Standalone Flows: Landing, Auth, Onboarding
    if (currentScreen == SpendWiseScreen.LANDING) {
        LandingScreen(
            onStartTracking = {
                if (userProfile.isOnboarded) {
                    viewModel.navigateTo(SpendWiseScreen.DASHBOARD)
                } else {
                    viewModel.navigateTo(SpendWiseScreen.ONBOARDING)
                }
            },
            onExploreDemo = {
                viewModel.seedDemoData()
                viewModel.navigateTo(SpendWiseScreen.DASHBOARD)
            },
            onOpenAuth = {
                viewModel.navigateTo(SpendWiseScreen.AUTH)
            }
        )
        return
    }

    if (currentScreen == SpendWiseScreen.AUTH) {
        val authLoading by viewModel.authLoading.collectAsState()
        val authError by viewModel.authError.collectAsState()

        AuthScreen(
            isLoading = authLoading,
            errorMessage = authError,
            onSignInEmail = { email, password ->
                viewModel.signInWithClerk(email, password) {
                    if (userProfile.isOnboarded) {
                        viewModel.navigateTo(SpendWiseScreen.DASHBOARD)
                    } else {
                        viewModel.navigateTo(SpendWiseScreen.ONBOARDING)
                    }
                }
            },
            onSignUpEmail = { email, password, first, last ->
                viewModel.signUpWithClerk(email, password, first, last) {
                    if (userProfile.isOnboarded) {
                        viewModel.navigateTo(SpendWiseScreen.DASHBOARD)
                    } else {
                        viewModel.navigateTo(SpendWiseScreen.ONBOARDING)
                    }
                }
            },
            onSignInSocial = { provider ->
                viewModel.signInWithSocial(provider) {
                    if (userProfile.isOnboarded) {
                        viewModel.navigateTo(SpendWiseScreen.DASHBOARD)
                    } else {
                        viewModel.navigateTo(SpendWiseScreen.ONBOARDING)
                    }
                }
            },
            onContinueGuest = {
                viewModel.signInAsVerifiedDemo {
                    viewModel.navigateTo(SpendWiseScreen.DASHBOARD)
                }
            },
            onClearError = { viewModel.clearAuthError() }
        )
        return
    }

    if (currentScreen == SpendWiseScreen.ONBOARDING) {
        OnboardingScreen(
            initialIncome = userProfile.monthlyIncome,
            initialBudget = userProfile.targetBudget,
            initialCurrency = userProfile.currencySymbol,
            onFinishOnboarding = { inc, bud, curr, cats, goal ->
                viewModel.completeOnboarding(inc, bud, curr, cats, goal)
            }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(modifier = Modifier.height(18.dp))
                // Drawer Brand Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(EmeraldContainer, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("SW", fontWeight = FontWeight.ExtraBold, color = EmeraldLight, fontSize = 18.sp)
                    }
                    Column {
                        Text("SpendWise", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                        Text("Financial Intelligence & Risk", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = DarkBorder)

                // Navigation Items
                val navItems = listOf(
                    Triple(SpendWiseScreen.DASHBOARD, Icons.Default.Dashboard, "Dashboard"),
                    Triple(SpendWiseScreen.TRANSACTIONS, Icons.Default.ReceiptLong, "Transactions"),
                    Triple(SpendWiseScreen.BUDGETS, Icons.Default.PieChart, "Budgets"),
                    Triple(SpendWiseScreen.GOALS, Icons.Default.TrackChanges, "Financial Goals (${goals.size})"),
                    Triple(SpendWiseScreen.RECURRING, Icons.Default.Repeat, "Subscriptions (${recurring.size})"),
                    Triple(SpendWiseScreen.COPILOT, Icons.Default.AutoAwesome, "SpendWise Copilot"),
                    Triple(SpendWiseScreen.ANALYSIS, Icons.Default.Analytics, "Spending Analysis"),
                    Triple(SpendWiseScreen.RISK, Icons.Default.WarningAmber, "Risk Signals (${riskAlerts.size})"),
                    Triple(SpendWiseScreen.INSIGHTS, Icons.Default.Psychology, "Gemini AI Insights"),
                    Triple(SpendWiseScreen.REPORTS, Icons.Default.Assessment, "Monthly Reports"),
                    Triple(SpendWiseScreen.SETTINGS, Icons.Default.Settings, "Settings"),
                    Triple(SpendWiseScreen.LANDING, Icons.Default.Home, "Welcome Showcase")
                )

                navItems.forEach { (screen, icon, label) ->
                    val isSelected = currentScreen == screen
                    NavigationDrawerItem(
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (screen == SpendWiseScreen.RISK && riskAlerts.isNotEmpty()) AmberWarning
                                else if (isSelected) EmeraldLight else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) EmeraldLight else TextPrimary
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            viewModel.navigateTo(screen)
                            coroutineScope.launch { drawerState.close() }
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = EmeraldContainer.copy(alpha = 0.4f),
                            unselectedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Demo Action
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = DarkSurfaceElevated
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Need fresh sample data?", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        TextButton(
                            onClick = {
                                viewModel.seedDemoData()
                                coroutineScope.launch { drawerState.close() }
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Reset Demo Ledger", color = TealSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                SpendWiseTopBar(
                    currentScreen = currentScreen,
                    currencySymbol = userProfile.currencySymbol,
                    onOpenNav = { coroutineScope.launch { drawerState.open() } },
                    onNavigate = { viewModel.navigateTo(it) },
                    isDemoMode = userProfile.isDemoMode
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 4.dp
                ) {
                    val bottomItems = listOf(
                        Triple(SpendWiseScreen.DASHBOARD, Icons.Default.Dashboard, "Home"),
                        Triple(SpendWiseScreen.TRANSACTIONS, Icons.Default.ReceiptLong, "Ledger"),
                        Triple(SpendWiseScreen.GOALS, Icons.Default.TrackChanges, "Goals"),
                        Triple(SpendWiseScreen.RECURRING, Icons.Default.Repeat, "Subs"),
                        Triple(SpendWiseScreen.COPILOT, Icons.Default.AutoAwesome, "Copilot")
                    )

                    bottomItems.forEach { (screen, icon, label) ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) EmeraldLight else TextSecondary
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) EmeraldLight else TextSecondary
                                )
                            },
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = EmeraldContainer.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            },
            containerColor = DarkBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    SpendWiseScreen.DASHBOARD -> DashboardScreen(
                        analytics = analytics,
                        riskAlerts = riskAlerts,
                        aiInsights = aiInsights,
                        budgets = budgets,
                        goals = goals,
                        recentTransactions = transactions,
                        savingOpportunities = savingOpportunities,
                        detailedHealthOverview = detailedHealthOverview,
                        userProfile = userProfile,
                        currencySymbol = userProfile.currencySymbol,
                        timeHorizon = timeHorizon,
                        onTimeHorizonChange = { viewModel.setTimeHorizon(it) },
                        selectedDayIndex = selectedDayIndex,
                        onSelectDayIndex = { viewModel.setSelectedDayIndex(it) },
                        onNavigate = { viewModel.navigateTo(it) },
                        onAddTransaction = {
                            editingTx = null
                            showAddEditTxDialog = true
                        },
                        onQuickAddExpense = { merchant, amount, category ->
                            viewModel.quickAddExpense(merchant, amount, category)
                        },
                        onResolveAlert = { alertId ->
                            viewModel.resolveRiskAlert(alertId)
                        },
                        onApplyOpportunity = { opp ->
                            viewModel.applySavingOpportunity(opp)
                        },
                        onContributeGoal = { goalId, amount ->
                            viewModel.quickContributeToGoal(goalId, amount)
                        },
                        onOpenCsvImport = { showCsvImportDialog = true },
                        onOpenDiagnostics = { showHealthDiagnosticsDialog = true }
                    )
                    SpendWiseScreen.TRANSACTIONS -> TransactionsScreen(
                        transactions = transactions,
                        searchQuery = searchQuery,
                        categoryFilter = categoryFilter,
                        typeFilter = typeFilter,
                        currencySymbol = userProfile.currencySymbol,
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                        onTypeFilterChange = { viewModel.setTypeFilter(it) },
                        onAddTransaction = {
                            editingTx = null
                            showAddEditTxDialog = true
                        },
                        onOpenCsvImport = { showCsvImportDialog = true },
                        onEditTransaction = { tx ->
                            editingTx = tx
                            showAddEditTxDialog = true
                        },
                        onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
                    )
                    SpendWiseScreen.BUDGETS -> BudgetsScreen(
                        budgets = budgets,
                        transactions = transactions,
                        targetTotalBudget = userProfile.targetBudget,
                        currencySymbol = userProfile.currencySymbol,
                        onAddEditBudget = { bg ->
                            editingBudget = bg
                            showBudgetDialog = true
                        },
                        onDeleteBudget = { bg -> viewModel.deleteBudget(bg) }
                    )
                    SpendWiseScreen.GOALS -> GoalsScreen(
                        goals = goals,
                        currencySymbol = userProfile.currencySymbol,
                        onAddGoal = { showAddGoalDialog = true },
                        onContribute = { goal, amt -> viewModel.contributeToGoal(goal.id, amt) },
                        onDeleteGoal = { goal -> viewModel.deleteGoal(goal) }
                    )
                    SpendWiseScreen.RECURRING -> RecurringScreen(
                        recurringList = recurring,
                        currencySymbol = userProfile.currencySymbol,
                        onAddRecurring = { showAddRecurringDialog = true },
                        onToggleStatus = { item ->
                            val nextStatus = if (item.status == "ACTIVE") "REVIEW" else "ACTIVE"
                            viewModel.updateRecurringStatus(item.id, nextStatus)
                        },
                        onDeleteRecurring = { item -> viewModel.deleteRecurring(item) }
                    )
                    SpendWiseScreen.COPILOT -> CopilotScreen(
                        messages = copilotMessages,
                        isThinking = isCopilotThinking,
                        onSendMessage = { query -> viewModel.askCopilot(query) }
                    )
                    SpendWiseScreen.ANALYSIS -> SpendingAnalysisScreen(
                        analytics = analytics,
                        currencySymbol = userProfile.currencySymbol
                    )
                    SpendWiseScreen.RISK -> RiskDetectionScreen(
                        riskAlerts = riskAlerts,
                        currencySymbol = userProfile.currencySymbol,
                        onResolveAlert = { alertId -> viewModel.resolveRiskAlert(alertId) }
                    )
                    SpendWiseScreen.INSIGHTS -> AiInsightsScreen(
                        aiInsights = aiInsights,
                        isGenerating = isGeneratingAi,
                        currencySymbol = userProfile.currencySymbol,
                        onRefreshAi = { viewModel.refreshAi() }
                    )
                    SpendWiseScreen.REPORTS -> ReportsScreen(
                        analytics = analytics,
                        transactions = transactions,
                        budgets = budgets,
                        profile = userProfile
                    )
                    SpendWiseScreen.SETTINGS -> SettingsScreen(
                        profile = userProfile,
                        onUpdateProfile = { viewModel.updateProfile(it) },
                        onSeedDemoData = { viewModel.seedDemoData() },
                        onClearAllData = { viewModel.clearAllData() },
                        onSignOut = { viewModel.signOutClerk() }
                    )
                    else -> Unit
                }
            }
        }
    }

    // Dialogs
    if (showAddEditTxDialog) {
        AddEditTransactionDialog(
            initialTx = editingTx,
            currencySymbol = userProfile.currencySymbol,
            onDismiss = {
                showAddEditTxDialog = false
                editingTx = null
            },
            onSave = { merchant, amount, type, category, notes, isFlagged, riskReason ->
                if (editingTx == null) {
                    viewModel.addTransaction(merchant, amount, type, category, notes, isFlagged, riskReason)
                } else {
                    viewModel.updateTransaction(
                        editingTx!!.copy(
                            merchant = merchant,
                            amount = amount,
                            type = type,
                            category = category,
                            notes = notes,
                            isFlagged = isFlagged,
                            riskReason = riskReason
                        )
                    )
                }
                showAddEditTxDialog = false
                editingTx = null
            }
        )
    }

    if (showCsvImportDialog) {
        CsvImportDialog(
            existingTransactions = transactions,
            onDismiss = { showCsvImportDialog = false },
            onConfirmImport = { csvContent ->
                viewModel.importCsv(csvContent)
                showCsvImportDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            initialBudget = editingBudget,
            currencySymbol = userProfile.currencySymbol,
            onDismiss = {
                showBudgetDialog = false
                editingBudget = null
            },
            onSave = { category, limit ->
                viewModel.saveBudget(category, limit)
                showBudgetDialog = false
                editingBudget = null
            },
            onDelete = if (editingBudget != null) {
                {
                    viewModel.deleteBudget(editingBudget!!)
                    showBudgetDialog = false
                    editingBudget = null
                }
            } else null
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            currencySymbol = userProfile.currencySymbol,
            onDismiss = { showAddGoalDialog = false },
            onSave = { title, target, current, deadline, contrib, category, icon ->
                viewModel.saveGoal(title, target, current, deadline, contrib, category, icon)
                showAddGoalDialog = false
            }
        )
    }

    if (showAddRecurringDialog) {
        AddRecurringDialog(
            currencySymbol = userProfile.currencySymbol,
            onDismiss = { showAddRecurringDialog = false },
            onSave = { merchant, amount, frequency, category, nextDate, status ->
                viewModel.saveRecurring(merchant, amount, frequency, category, nextDate, status)
                showAddRecurringDialog = false
            }
        )
    }

    if (showHealthDiagnosticsDialog) {
        HealthDiagnosticsDialog(
            overview = detailedHealthOverview,
            onDismiss = { showHealthDiagnosticsDialog = false }
        )
    }
}
