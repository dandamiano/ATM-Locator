package com.thinksoftlab.atmlocator

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform