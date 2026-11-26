package com.thinksoftlab.atmlocator.freshness

import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FreshnessRulesTest {
    @Test
    fun freshWithWaitMinutesShowsSolidMinutesAndPulse() {
        val presentation = presentFreshness(Freshness.Fresh, WaitSnapshot(waitMinutes = 4))

        assertTrue(presentation.showMinutes)
        assertEquals("~4 min", presentation.minutesText)
        assertEquals(FreshnessStyle.SolidGold, presentation.style)
        assertEquals(StringKey.Fresh, presentation.labelKey)
        assertEquals("Fresh", text(presentation.labelKey, AppLanguage.En))
        assertTrue(presentation.showLivePulse)
    }

    @Test
    fun agingWithWaitMinutesShowsOutlineMinutesWithoutPulse() {
        val presentation = presentFreshness(Freshness.Aging, WaitSnapshot(waitMinutes = 12))

        assertTrue(presentation.showMinutes)
        assertEquals("~12 min", presentation.minutesText)
        assertEquals(FreshnessStyle.OutlineGold, presentation.style)
        assertEquals(StringKey.Aging, presentation.labelKey)
        assertEquals("Aging", text(presentation.labelKey, AppLanguage.En))
        assertFalse(presentation.showLivePulse)
    }

    @Test
    fun staleHidesMinutesAndPulse() {
        val presentation = presentFreshness(Freshness.Stale, WaitSnapshot(waitMinutes = 3))

        assertFalse(presentation.showMinutes)
        assertNull(presentation.minutesText)
        assertEquals(FreshnessStyle.Muted, presentation.style)
        assertEquals(StringKey.StaleNotLive, presentation.labelKey)
        assertEquals("Stale — not live", text(presentation.labelKey, AppLanguage.En))
        assertFalse(presentation.showLivePulse)
    }

    @Test
    fun nullFreshnessHidesMinutesEvenWhenWaitIsPresent() {
        val presentation = presentFreshness(freshness = null, wait = WaitSnapshot(waitMinutes = 3))

        assertFalse(presentation.showMinutes)
        assertNull(presentation.minutesText)
        assertEquals(FreshnessStyle.Muted, presentation.style)
        assertEquals(StringKey.StaleNotLive, presentation.labelKey)
        assertFalse(presentation.showLivePulse)
    }

    @Test
    fun freshWithNullWaitMinutesDoesNotInventZero() {
        val presentation = presentFreshness(Freshness.Fresh, WaitSnapshot(waitMinutes = null))

        assertFalse(presentation.showMinutes)
        assertNull(presentation.minutesText)
        assertEquals(FreshnessStyle.SolidGold, presentation.style)
        assertEquals(StringKey.Fresh, presentation.labelKey)
        assertTrue(presentation.showLivePulse)
    }

    @Test
    fun missingWaitObjectHidesMinutes() {
        val presentation = presentFreshness(Freshness.Aging, wait = null)

        assertFalse(presentation.showMinutes)
        assertNull(presentation.minutesText)
        assertEquals(StringKey.Aging, presentation.labelKey)
        assertFalse(presentation.showLivePulse)
    }
}
