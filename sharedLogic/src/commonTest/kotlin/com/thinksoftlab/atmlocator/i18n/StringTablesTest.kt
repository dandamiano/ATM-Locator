package com.thinksoftlab.atmlocator.i18n

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StringTablesTest {
    @Test
    fun everyKeyResolvesInEnglishAndChichewa() {
        for (key in StringKey.entries) {
            val english = text(key, AppLanguage.En)
            val chichewa = text(key, AppLanguage.Ny)
            assertTrue(english.isNotBlank(), key.name)
            assertTrue(chichewa.isNotBlank(), key.name)
            if (!isChichewaReviewed(key)) {
                assertEquals(english, chichewa, key.name)
            }
        }
    }

    @Test
    fun englishHonestyCopyMatchesGuidelines() {
        assertEquals("Find a usable ATM", text(StringKey.FindUsableAtm, AppLanguage.En))
        assertEquals("Use my location", text(StringKey.UseMyLocation, AppLanguage.En))
        assertEquals("Getting location…", text(StringKey.GettingLocation, AppLanguage.En))
        assertEquals("No ATMs match", text(StringKey.NoAtmsMatch, AppLanguage.En))
        assertEquals("May be outdated", text(StringKey.MayBeOutdated, AppLanguage.En))
        assertEquals("{n} ATMs", text(StringKey.AtmCount, AppLanguage.En))
        assertEquals("4 ATMs", text(StringKey.AtmCount, AppLanguage.En, n = 4))
        assertEquals("Stale — not live", text(StringKey.StaleNotLive, AppLanguage.En))
        assertEquals("Updated unknown", text(StringKey.UpdatedUnknown, AppLanguage.En))
        assertEquals("Updated 3m ago", text(StringKey.UpdatedMinutesAgo, AppLanguage.En, n = 3))
        assertEquals("You are offline", text(StringKey.Offline, AppLanguage.En))
        assertEquals("Retry", text(StringKey.Retry, AppLanguage.En))
        assertEquals("English", text(StringKey.LanguageEnglish, AppLanguage.En))
        assertEquals("Chichewa", text(StringKey.LanguageChichewa, AppLanguage.En))
    }
}
