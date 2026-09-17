package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BudgetEntity
import com.example.data.model.RiskSeverity
import com.example.data.model.TransactionEntity
import com.example.domain.analysis.CategorizationEngine
import com.example.domain.analysis.RiskDetectionEngine
import com.example.domain.analysis.SpendingAnalyticsEngine
import com.example.domain.data.CsvParser
import com.example.domain.data.DemoData
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SpendWise", appName)
  }

  @Test
  fun `test categorization engine accuracy`() {
    val starbucks = CategorizationEngine.categorize("Starbucks Coffee")
    assertEquals("Food", starbucks.category)
    assertTrue(starbucks.confidence > 0.8f)

    val traderJoes = CategorizationEngine.categorize("Trader Joe's Supermarket")
    assertEquals("Groceries", traderJoes.category)

    val uber = CategorizationEngine.categorize("Uber Trip Ride")
    assertEquals("Transportation", uber.category)

    val netflix = CategorizationEngine.categorize("Netflix Subscription")
    assertEquals("Subscriptions", netflix.category)
  }

  @Test
  fun `test risk detection engine flags large spike`() {
    val normalTx = listOf(
      TransactionEntity(merchant = "Chipotle", amount = 15.0, type = "EXPENSE", category = "Food"),
      TransactionEntity(merchant = "Subway", amount = 12.0, type = "EXPENSE", category = "Food"),
      TransactionEntity(merchant = "Diner", amount = 18.0, type = "EXPENSE", category = "Food"),
      // Outlier spike
      TransactionEntity(merchant = "Luxury Steakhouse Outing", amount = 240.0, type = "EXPENSE", category = "Food", isFlagged = true)
    )

    val alerts = RiskDetectionEngine.analyze(
      transactions = normalTx,
      budgets = listOf(BudgetEntity(category = "Food", monthlyLimit = 150.0)),
      monthlyIncome = 2500.0,
      targetBudget = 1800.0
    )

    assertTrue("Should detect at least one risk alert", alerts.isNotEmpty())
    val topAlert = alerts.first()
    assertTrue(topAlert.severity == RiskSeverity.HIGH || topAlert.severity == RiskSeverity.MEDIUM)
    assertTrue(alerts.any { it.reason.contains("Unusual spending pattern detected") })
  }

  @Test
  fun `test csv parser parses sample rows correctly`() {
    val csv = DemoData.getSampleCsvString()
    val result = CsvParser.parse(csv, emptyList())

    assertTrue(result.totalRows > 5)
    assertTrue(result.validRows > 5)
    assertEquals(0, result.invalidRows)
  }
}
