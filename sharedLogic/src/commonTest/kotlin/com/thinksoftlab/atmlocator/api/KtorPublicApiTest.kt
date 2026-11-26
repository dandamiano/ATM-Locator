package com.thinksoftlab.atmlocator.api

import com.thinksoftlab.atmlocator.model.City
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class KtorPublicApiTest {
    @Test
    fun citiesSuccessParsesFlexibleJson() = runTest {
        var requestedPath: String? = null
        val client = mockClient { request ->
            requestedPath = request.url.encodedPath
            respond(
                content = """{"cities":[{"id":"ll","name":"Lilongwe","lat":-13.96,"lng":33.78}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        val api = KtorPublicApi(ApiConfig(baseUrl = "https://api.example.test"), client)
        val result = api.cities()
        val cities = assertIs<ApiResult.Ok<List<City>>>(result).value
        assertEquals(1, cities.size)
        assertEquals("Lilongwe", cities[0].name)
        assertEquals(-13.96, cities[0].latitude)
        assertEquals(PublicPaths.CITIES, requestedPath)
    }

    @Test
    fun emptyBaseUrlIsNotConfiguredAndMakesNoRequest() = runTest {
        var hits = 0
        val client = mockClient {
            hits++
            respond("should not run", status = HttpStatusCode.OK)
        }
        val api = KtorPublicApi(ApiConfig(baseUrl = ""), client)
        val result = api.cities()
        assertIs<ApiResult.Err>(result)
        assertEquals(ApiError.NotConfigured, result.error)
        assertEquals(0, hits)
    }

    @Test
    fun status429MapsRetryAfterFromHeader() = runTest {
        val client = mockClient {
            respond(
                content = "slow down",
                status = HttpStatusCode.TooManyRequests,
                headers = headersOf(KtorPublicApi.RETRY_AFTER_HEADER, "30"),
            )
        }
        val api = KtorPublicApi(ApiConfig(baseUrl = "https://api.example.test"), client)
        val result = api.cities()
        assertIs<ApiResult.Err>(result)
        val limited = assertIs<ApiError.RateLimited>(result.error)
        assertEquals(30L, limited.retryAfterSeconds)
    }

    @Test
    fun status404MapsToNotFound() = runTest {
        val client = mockClient {
            respond("missing", status = HttpStatusCode.NotFound)
        }
        val api = KtorPublicApi(ApiConfig(baseUrl = "https://api.example.test"), client)
        val result = api.cities()
        assertIs<ApiResult.Err>(result)
        assertEquals(ApiError.NotFound, result.error)
    }

    @Test
    fun appKeyHeaderSentOnlyWhenNonEmpty() = runTest {
        var sawKey: String? = "unset"
        val withKey = mockClient { request ->
            sawKey = request.headers[KtorPublicApi.APP_KEY_HEADER]
            respond("[]", status = HttpStatusCode.OK, headers = jsonHeaders())
        }
        KtorPublicApi(ApiConfig(baseUrl = "https://api.example.test", appKey = "secret"), withKey)
            .cities()
        assertEquals("secret", sawKey)

        sawKey = "unset"
        val withoutKey = mockClient { request ->
            sawKey = request.headers[KtorPublicApi.APP_KEY_HEADER]
            respond("[]", status = HttpStatusCode.OK, headers = jsonHeaders())
        }
        KtorPublicApi(ApiConfig(baseUrl = "https://api.example.test", appKey = ""), withoutKey)
            .cities()
        assertEquals(null, sawKey)
    }

    @Test
    fun nearbySearchDetailRefuseWithoutHttp() = runTest {
        var hits = 0
        val client = mockClient {
            hits++
            respond("nope", status = HttpStatusCode.OK)
        }
        val api = KtorPublicApi(ApiConfig(baseUrl = "https://api.example.test"), client)
        assertEquals(ApiError.UnsupportedUntilOpenApi, (api.nearby(-13.0, 33.0) as ApiResult.Err).error)
        assertEquals(ApiError.UnsupportedUntilOpenApi, (api.search("nbs") as ApiResult.Err).error)
        assertEquals(ApiError.UnsupportedUntilOpenApi, (api.detail("atm-1") as ApiResult.Err).error)
        assertEquals(0, hits)
    }

    @Test
    fun publicPathGuardStillRejectsNonPublic() {
        val rejected = listOf("/auth/", "/bank/", "/queue/", "/integrations/")
        for (path in rejected) {
            assertTrue(!PublicPaths.isPublicPath(path), path)
        }
    }

    private fun jsonHeaders() = headersOf(HttpHeaders.ContentType, "application/json")

    private fun mockClient(
        respondBlock: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        val engine = MockEngine { request -> respondBlock(request) }
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }
}
