package com.thinksoftlab.atmlocator.api

import com.thinksoftlab.atmlocator.filter.AtmFilters
import com.thinksoftlab.atmlocator.model.AtmListItem
import com.thinksoftlab.atmlocator.model.City
import com.thinksoftlab.atmlocator.model.parseCitiesJson
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess

/**
 * Ktor [PublicApi]. Cities is live against [PublicPaths.CITIES].
 *
 * Nearby, search, and detail always return [ApiError.UnsupportedUntilOpenApi] and
 * perform no HTTP. OpenAPI has not named those paths or query/DTO fields in-repo;
 * do not inject guessed `/nearby` strings. When the spec lands, extend this class
 * with confirmed paths under [PublicPaths.requirePublicPath] only.
 *
 * [nearby] accepts latitude and longitude only as location fields (plus filters).
 */
/** Builds the production [PublicApi] without exposing [HttpClient] to UI modules. */
fun createKtorPublicApi(config: ApiConfig = platformApiConfig()): PublicApi =
    KtorPublicApi(config, createHttpClient())

class KtorPublicApi(
    private val config: ApiConfig,
    private val client: HttpClient,
) : PublicApi {

    override suspend fun cities(): ApiResult<List<City>> {
        return getText(PublicPaths.CITIES) { body -> parseCitiesJson(body) }
    }

    @Suppress("UNUSED_PARAMETER")
    override suspend fun nearby(
        latitude: Double,
        longitude: Double,
        filters: AtmFilters,
    ): ApiResult<List<AtmListItem>> = ApiResult.Err(ApiError.UnsupportedUntilOpenApi)

    @Suppress("UNUSED_PARAMETER")
    override suspend fun search(
        text: String,
        filters: AtmFilters,
    ): ApiResult<List<AtmListItem>> = ApiResult.Err(ApiError.UnsupportedUntilOpenApi)

    @Suppress("UNUSED_PARAMETER")
    override suspend fun detail(atmId: String): ApiResult<AtmListItem> =
        ApiResult.Err(ApiError.UnsupportedUntilOpenApi)

    private suspend fun <T> getText(
        path: String,
        parse: (String) -> T,
    ): ApiResult<T> {
        if (config.baseUrl.isBlank()) {
            return ApiResult.Err(ApiError.NotConfigured)
        }
        val safePath = PublicPaths.requirePublicPath(path)
        val origin = config.baseUrl.trimEnd('/')
        val absoluteUrl = origin + safePath
        return try {
            val response = client.request {
                method = HttpMethod.Get
                url(absoluteUrl)
                if (config.sendsAppKey) {
                    header(APP_KEY_HEADER, config.appKey)
                }
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                ApiResult.Ok(parse(body))
            } else {
                val retryAfter = response.headers[RETRY_AFTER_HEADER]
                ApiResult.Err(
                    mapHttpFailure(
                        statusCode = response.status.value,
                        retryAfterHeader = retryAfter,
                        bodyMessage = body.takeIf { it.isNotBlank() }?.take(200),
                    ),
                )
            }
        } catch (_: Throwable) {
            ApiResult.Err(mapConnectivityFailure())
        }
    }

    companion object {
        const val APP_KEY_HEADER: String = "X-App-Key"
        const val RETRY_AFTER_HEADER: String = "Retry-After"
    }
}
