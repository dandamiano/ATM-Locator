package com.thinksoftlab.atmlocator.filter

import com.thinksoftlab.atmlocator.freshness.Freshness
import com.thinksoftlab.atmlocator.freshness.WaitSnapshot
import com.thinksoftlab.atmlocator.model.AtmListItem
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AtmFiltersTest {
    @Test
    fun maxWaitDropsOnlyLiveRowsAboveCap() {
        val filters = AtmFilters(maxWaitMinutes = 10)
        assertFalse(filters.allows(item(Freshness.Fresh, 15)))
        assertFalse(filters.allows(item(Freshness.Aging, 11)))
        assertTrue(filters.allows(item(Freshness.Fresh, 10)))
        assertTrue(filters.allows(item(Freshness.Stale, 99)))
        assertTrue(filters.allows(item(Freshness.Fresh, waitMinutes = null)))
        assertTrue(filters.allows(item(null, 99)))
    }

    @Test
    fun defaultStatusesIncludeStaleAndUnknown() {
        val filters = AtmFilters()
        assertTrue(filters.allows(item(Freshness.Stale, 5, status = "unknown")))
        assertTrue(filters.allows(item(Freshness.Fresh, 5, status = "operational")))
        assertTrue(filters.allows(item(Freshness.Aging, 5, status = "not_a_real_code")))
    }

    private fun item(
        freshness: Freshness?,
        waitMinutes: Int?,
        status: String? = "operational",
    ): AtmListItem = AtmListItem(
        id = "x",
        bankName = "NBS",
        atmName = "Test",
        statusCode = status,
        freshness = freshness,
        wait = waitMinutes?.let { WaitSnapshot(it) } ?: WaitSnapshot(null),
    )
}
