package com.thinksoftlab.atmlocator.time

import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RelativeTimeTest {
    private val now = 1_700_000_000_000L

    @Test
    fun nullTimestampHidesRelativeTime() {
        val presentation = presentRelativeTime(lastUpdatedAtMillis = null, nowMillis = now)

        assertFalse(presentation.showRelative)
        assertNull(presentation.amount)
        assertNull(presentation.templateKey)
        assertNull(presentation.formatted(AppLanguage.En))
    }

    @Test
    fun threeMinutesAgoUsesMinuteTemplate() {
        val presentation = presentRelativeTime(now - 3 * 60_000L, now)

        assertTrue(presentation.showRelative)
        assertEquals(3L, presentation.amount)
        assertEquals(StringKey.UpdatedMinutesAgo, presentation.templateKey)
        assertEquals("Updated 3m ago", presentation.formatted(AppLanguage.En))
    }

    @Test
    fun twoHoursAgoUsesHourTemplate() {
        val presentation = presentRelativeTime(now - 2 * 60 * 60_000L, now)

        assertTrue(presentation.showRelative)
        assertEquals(2L, presentation.amount)
        assertEquals(StringKey.UpdatedHoursAgo, presentation.templateKey)
        assertEquals("Updated 2h ago", presentation.formatted(AppLanguage.En))
    }

    @Test
    fun twoDaysAgoUsesDayTemplate() {
        val presentation = presentRelativeTime(now - 2 * 24 * 60 * 60_000L, now)

        assertEquals(2L, presentation.amount)
        assertEquals(StringKey.UpdatedDaysAgo, presentation.templateKey)
        assertEquals("Updated 2d ago", presentation.formatted(AppLanguage.En))
    }
}
