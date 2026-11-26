package com.thinksoftlab.atmlocator.store

/**
 * JS language store is in-memory for the browser target compile.
 * The public locator is not a webApp product surface.
 */
class JsLanguageRepository : LanguageRepository by MemoryLanguageRepository()
