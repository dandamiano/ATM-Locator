package com.thinksoftlab.atmlocator.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Platform HTTP engine with JSON content negotiation.
 * Creating the client does not perform a request and does not define ATM payloads.
 * Phase 1 owns the public API client.
 */
expect fun createHttpClient(): HttpClient

internal fun <T : HttpClientEngineConfig> HttpClientConfig<T>.installPublicJson() {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
            },
        )
    }
}
