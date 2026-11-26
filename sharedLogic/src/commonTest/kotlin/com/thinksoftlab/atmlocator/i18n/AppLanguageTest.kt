package com.thinksoftlab.atmlocator.i18n

import kotlin.test.Test
import kotlin.test.assertEquals

class AppLanguageTest {
    @Test
    fun nyTagsSelectChichewa() {
        assertEquals(AppLanguage.Ny, appLanguageForTag("ny"))
        assertEquals(AppLanguage.Ny, appLanguageForTag("ny-MW"))
    }

    @Test
    fun englishTagSelectsEnglish() {
        assertEquals(AppLanguage.En, appLanguageForTag("en"))
    }
}
