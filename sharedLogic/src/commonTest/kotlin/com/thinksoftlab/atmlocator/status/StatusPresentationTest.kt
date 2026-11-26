package com.thinksoftlab.atmlocator.status

import com.thinksoftlab.atmlocator.i18n.AppLanguage
import com.thinksoftlab.atmlocator.i18n.StringKey
import com.thinksoftlab.atmlocator.i18n.text
import kotlin.test.Test
import kotlin.test.assertEquals

class StatusPresentationTest {
    @Test
    fun eachStatusCodeMapsToIconAndLabel() {
        val cases = listOf(
            "operational" to (StatusIconKey.Operational to StringKey.StatusOperational),
            "out_of_cash" to (StatusIconKey.OutOfCash to StringKey.StatusOutOfCash),
            "out_of_service" to (StatusIconKey.OutOfService to StringKey.StatusOutOfService),
            "maintenance" to (StatusIconKey.Maintenance to StringKey.StatusMaintenance),
            "temporarily_unavailable" to (
                StatusIconKey.TemporarilyUnavailable to StringKey.StatusTemporarilyUnavailable
                ),
            "unknown" to (StatusIconKey.Unknown to StringKey.StatusUnknown),
        )

        for ((code, expected) in cases) {
            val presentation = presentStatus(code)
            assertEquals(expected.first, presentation.iconKey, code)
            assertEquals(expected.second, presentation.labelKey, code)
            assertEquals(expectedLabel(code), text(presentation.labelKey, AppLanguage.En))
        }
    }

    @Test
    fun unrecognizedCodeStaysUnknown() {
        val presentation = presentStatus("closed")

        assertEquals(StatusIconKey.Unknown, presentation.iconKey)
        assertEquals(StringKey.StatusUnknown, presentation.labelKey)
        assertEquals("Unknown", text(presentation.labelKey, AppLanguage.En))
    }

    @Test
    fun nullCodeStaysUnknown() {
        val presentation = presentStatus(null)

        assertEquals(StatusIconKey.Unknown, presentation.iconKey)
        assertEquals(StringKey.StatusUnknown, presentation.labelKey)
    }

    private fun expectedLabel(code: String): String = when (code) {
        "operational" -> "Operational"
        "out_of_cash" -> "Out of cash"
        "out_of_service" -> "Out of service"
        "maintenance" -> "Maintenance"
        "temporarily_unavailable" -> "Temporarily unavailable"
        "unknown" -> "Unknown"
        else -> error(code)
    }
}
