package com.thinksoftlab.atmlocator.status

import com.thinksoftlab.atmlocator.i18n.StringKey

enum class StatusIconKey {
    Operational,
    OutOfCash,
    OutOfService,
    Maintenance,
    TemporarilyUnavailable,
    Unknown,
}

data class StatusPresentation(
    val iconKey: StatusIconKey,
    val labelKey: StringKey,
)

/**
 * Maps a public status code to an icon key and a label key.
 * Unrecognized and null codes stay [StatusIconKey.Unknown]. They do not become operational.
 */
fun presentStatus(code: String?): StatusPresentation = when (code?.trim()?.lowercase()) {
    "operational" -> StatusPresentation(StatusIconKey.Operational, StringKey.StatusOperational)
    "out_of_cash" -> StatusPresentation(StatusIconKey.OutOfCash, StringKey.StatusOutOfCash)
    "out_of_service" -> StatusPresentation(StatusIconKey.OutOfService, StringKey.StatusOutOfService)
    "maintenance" -> StatusPresentation(StatusIconKey.Maintenance, StringKey.StatusMaintenance)
    "temporarily_unavailable" -> StatusPresentation(
        StatusIconKey.TemporarilyUnavailable,
        StringKey.StatusTemporarilyUnavailable,
    )
    else -> StatusPresentation(StatusIconKey.Unknown, StringKey.StatusUnknown)
}
