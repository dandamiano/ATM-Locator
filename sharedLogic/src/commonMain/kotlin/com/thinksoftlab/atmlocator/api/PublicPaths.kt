package com.thinksoftlab.atmlocator.api

/**
 * Prefix guard for the anonymous public API.
 * Nearby, search, and detail paths are not named here. OpenAPI is not in the repo.
 *
 * The guidelines name the cities fragment `/public/meta/cities`.
 * [CITIES] joins that fragment onto `/api/v1` once, so the prefix is not doubled.
 */
object PublicPaths {
    const val PREFIX: String = "/api/v1/public/"
    const val CITIES: String = "/api/v1/public/meta/cities"

    fun requirePublicPath(path: String): String {
        val normalized = normalize(path)
        require(isPublicPath(normalized)) {
            "Refusing a path outside /api/v1/public/"
        }
        return normalized
    }

    fun isPublicPath(path: String): Boolean {
        val normalized = normalize(path)
        if (containsParentSegment(normalized)) return false
        if (isForbiddenRoot(normalized)) return false
        return normalized == "/api/v1/public" || normalized.startsWith(PREFIX)
    }

    private fun normalize(path: String): String {
        val withoutQuery = path.substringBefore('?').substringBefore('#')
        val trimmed = withoutQuery.trim()
        return if (trimmed.startsWith("/")) trimmed else "/$trimmed"
    }

    private fun containsParentSegment(path: String): Boolean {
        val lower = path.lowercase()
        return lower.contains("..") || lower.contains("%2e%2e")
    }

    private fun isForbiddenRoot(path: String): Boolean {
        val forbidden = listOf(
            "/auth",
            "/bank",
            "/queue",
            "/integrations",
            "/api/v1/auth",
            "/api/v1/bank",
            "/api/v1/queue",
            "/api/v1/integrations",
        )
        return forbidden.any { root -> path == root || path.startsWith("$root/") }
    }
}
