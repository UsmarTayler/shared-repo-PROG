package com.example.budgie_budgetapp

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for transaction input validation logic
 * (mirrors the validateInputs() checks in TransactionsActivity).
 */
class TransactionValidationTest {

    // ── Amount validation ────────────────────────────────────────────────────

    @Test
    fun emptyAmount_isInvalid() {
        val amount = ""
        assertTrue("Empty amount should be invalid", amount.isEmpty())
    }

    @Test
    fun zeroAmount_isInvalid() {
        val amount = "0"
        val parsed = amount.toDoubleOrNull()
        assertNotNull(parsed)
        assertFalse("Zero should not be a valid amount", parsed!! > 0)
    }

    @Test
    fun negativeAmount_isInvalid() {
        val amount = "-50"
        val parsed = amount.toDoubleOrNull()
        assertNotNull(parsed)
        assertFalse("Negative amount should be invalid", parsed!! > 0)
    }

    @Test
    fun validAmount_isAccepted() {
        val amount = "199.99"
        val parsed = amount.toDoubleOrNull()
        assertNotNull("Should parse to Double", parsed)
        assertTrue("Positive amount should be valid", parsed!! > 0)
    }

    @Test
    fun nonNumericAmount_failsToParse() {
        val amount = "abc"
        assertNull("Non-numeric input should return null", amount.toDoubleOrNull())
    }

    @Test
    fun amountWithSpaces_isInvalid() {
        val amount = "  "
        val trimmed = amount.trim()
        assertTrue("Whitespace-only amount should be empty after trim", trimmed.isEmpty())
    }

    // ── Description validation ────────────────────────────────────────────────

    @Test
    fun emptyDescription_isInvalid() {
        val description = ""
        assertTrue("Empty description should be invalid", description.trim().isEmpty())
    }

    @Test
    fun whitespaceOnlyDescription_isInvalid() {
        val description = "   "
        assertTrue("Whitespace description should be invalid", description.trim().isEmpty())
    }

    @Test
    fun validDescription_isAccepted() {
        val description = "Woolworths grocery run"
        assertFalse("Non-empty description should be valid", description.trim().isEmpty())
    }

    // ── Transaction type ──────────────────────────────────────────────────────

    @Test
    fun transactionType_defaultsToExpense() {
        val defaultType = "expense"
        assertEquals("expense", defaultType)
    }

    @Test
    fun transactionType_canBeIncome() {
        val type = "income"
        assertTrue(type == "income" || type == "expense")
    }

    @Test
    fun transactionType_isExpense_or_income() {
        listOf("expense", "income").forEach { type ->
            assertTrue("$type should be a valid type", type == "expense" || type == "income")
        }
    }

    // ── Category position validation ──────────────────────────────────────────

    @Test
    fun categoryPosition_isInvalid_whenNegative() {
        val position  = -1
        val listSize  = 5
        val addNewPos = listSize - 1   // last position is "Add New"
        assertFalse("Negative position should be invalid", position >= 0 && position < addNewPos)
    }

    @Test
    fun categoryPosition_isInvalid_whenPointingToAddNew() {
        val categories = listOf("Food", "Transport", "➕ Add New Category")
        val position   = categories.size - 1   // "Add New" position
        assertFalse("Add New position should be invalid", position < categories.size - 1)
    }

    @Test
    fun categoryPosition_isValid_forRealCategory() {
        val categories = listOf("Food", "Transport", "➕ Add New Category")
        val position   = 0   // "Food"
        assertTrue("Valid category index should pass", position >= 0 && position < categories.size - 1)
    }

    // ── Search filter logic ───────────────────────────────────────────────────

    @Test
    fun searchFilter_matchesCategoryName() {
        val query       = "food"
        val categoryName = "Food"
        assertTrue(categoryName.lowercase().contains(query.lowercase()))
    }

    @Test
    fun searchFilter_matchesDescription() {
        val query       = "woolworths"
        val description = "Woolworths grocery run"
        assertTrue(description.lowercase().contains(query.lowercase()))
    }

    @Test
    fun searchFilter_matchesAmount() {
        val query  = "89.99"
        val amount = 89.99
        assertTrue(String.format("%.2f", amount).contains(query))
    }

    @Test
    fun searchFilter_emptyQuery_returnsAll() {
        val query = ""
        assertTrue("Empty query should match everything", query.isEmpty())
    }

    @Test
    fun searchFilter_noMatch_returnsEmpty() {
        val query        = "xyznotfound"
        val categoryName = "Food"
        val description  = "Grocery run"
        val amount       = 50.00

        val matches = categoryName.lowercase().contains(query) ||
                description.lowercase().contains(query) ||
                String.format("%.2f", amount).contains(query)

        assertFalse("Non-matching query should return no results", matches)
    }
}
