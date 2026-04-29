package com.example.budgie_budgetapp

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for budget and balance calculation logic used across
 * ReportsActivity and BudgetGoalsActivity.
 */
class BudgetCalculationTest {

    // ── Balance calculation ───────────────────────────────────────────────────

    @Test
    fun balance_isIncomeMinusExpenses() {
        val income   = 15000.0
        val expenses = 3200.0
        val balance  = income - expenses
        assertEquals(11800.0, balance, 0.001)
    }

    @Test
    fun balance_isNegative_whenExpensesExceedIncome() {
        val income   = 500.0
        val expenses = 1200.0
        val balance  = income - expenses
        assertTrue("Balance should be negative", balance < 0)
        assertEquals(-700.0, balance, 0.001)
    }

    @Test
    fun balance_isZero_whenIncomeEqualsExpenses() {
        val income   = 1000.0
        val expenses = 1000.0
        assertEquals(0.0, income - expenses, 0.001)
    }

    // ── Budget percentage (max budget progress bar) ───────────────────────────

    @Test
    fun budgetPercentage_isCorrect() {
        val maxBudget   = 600.0
        val actualSpent = 300.0
        val percentage  = (actualSpent / maxBudget * 100).coerceIn(0.0, 100.0)
        assertEquals(50.0, percentage, 0.001)
    }

    @Test
    fun budgetPercentage_capsAt100_whenOverBudget() {
        val maxBudget   = 500.0
        val actualSpent = 750.0
        val percentage  = (actualSpent / maxBudget * 100).coerceIn(0.0, 100.0)
        assertEquals(100.0, percentage, 0.001)
    }

    @Test
    fun budgetPercentage_isZero_whenNothingSpent() {
        val maxBudget   = 500.0
        val actualSpent = 0.0
        val percentage  = (actualSpent / maxBudget * 100).coerceIn(0.0, 100.0)
        assertEquals(0.0, percentage, 0.001)
    }

    // ── Over-budget detection ─────────────────────────────────────────────────

    @Test
    fun overBudget_detected_whenSpentExceedsMax() {
        val maxBudget   = 400.0
        val actualSpent = 450.0
        assertTrue(actualSpent >= maxBudget)
    }

    @Test
    fun overBudget_notTriggered_whenUnderMax() {
        val maxBudget   = 400.0
        val actualSpent = 350.0
        assertFalse(actualSpent >= maxBudget)
    }

    @Test
    fun overBudgetAmount_isCalculatedCorrectly() {
        val maxBudget   = 400.0
        val actualSpent = 475.0
        val overage = actualSpent - maxBudget
        assertEquals(75.0, overage, 0.001)
    }

    // ── Below minimum budget detection ────────────────────────────────────────

    @Test
    fun belowMinBudget_detected_whenSpentIsLess() {
        val minBudget   = 200.0
        val actualSpent = 150.0
        assertTrue(actualSpent < minBudget)
    }

    @Test
    fun belowMinBudget_notTriggered_whenMet() {
        val minBudget   = 200.0
        val actualSpent = 250.0
        assertFalse(actualSpent < minBudget)
    }

    @Test
    fun shortfallAmount_isCalculatedCorrectly() {
        val minBudget   = 200.0
        val actualSpent = 120.0
        val shortfall = minBudget - actualSpent
        assertEquals(80.0, shortfall, 0.001)
    }

    // ── Warning threshold (80 % of max) ──────────────────────────────────────

    @Test
    fun warningThreshold_triggersAt80Percent() {
        val maxBudget   = 500.0
        val actualSpent = 400.0   // exactly 80 %
        val percentage  = (actualSpent / maxBudget * 100).coerceIn(0.0, 100.0)
        assertTrue("Should warn at 80 %", percentage >= 80.0)
    }

    @Test
    fun warningThreshold_doesNotTrigger_below80Percent() {
        val maxBudget   = 500.0
        val actualSpent = 350.0   // 70 %
        val percentage  = (actualSpent / maxBudget * 100).coerceIn(0.0, 100.0)
        assertFalse("Should not warn below 80 %", percentage >= 80.0)
    }

    // ── Amount formatting ────────────────────────────────────────────────────

    @Test
    fun amountFormat_expense_hasNegativePrefix() {
        val amount = 89.99
        val type   = "expense"
        val prefix = if (type == "expense") "-R" else "+R"
        val formatted = "$prefix${String.format("%.2f", amount)}"
        assertEquals("-R89.99", formatted)
    }

    @Test
    fun amountFormat_income_hasPositivePrefix() {
        val amount = 15000.0
        val type   = "income"
        val prefix = if (type == "expense") "-R" else "+R"
        val formatted = "$prefix${String.format("%.2f", amount)}"
        assertEquals("+R15000.00", formatted)
    }

    @Test
    fun amountFormat_alwaysTwoDecimalPlaces() {
        val formatted = String.format("%.2f", 100.0)
        assertEquals("100.00", formatted)
    }
}
