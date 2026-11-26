package com.thinksoftlab.atmlocator.store

import kotlin.js.Date

internal actual fun defaultClockMillis(): Long = Date.now().toLong()
