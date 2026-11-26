package com.thinksoftlab.atmlocator.store

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

internal actual fun defaultClockMillis(): Long =
    (NSDate().timeIntervalSince1970 * 1000.0).toLong()
