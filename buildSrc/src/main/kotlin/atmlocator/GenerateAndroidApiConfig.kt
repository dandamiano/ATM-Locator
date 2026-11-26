package atmlocator

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.util.Properties

/**
 * Writes the Android ApiConfig actual. The KMP library plugin has no BuildConfig.
 * A missing local.properties entry becomes an empty string. Empty appKey means omit X-App-Key.
 */
abstract class GenerateAndroidApiConfig : DefaultTask() {
    @get:Input
    abstract val localPropertiesText: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val properties = Properties()
        val text = localPropertiesText.get()
        if (text.isNotBlank()) {
            properties.load(text.reader())
        }
        val baseUrl = properties.getProperty("ATM_API_BASE_URL")?.trim().orEmpty()
        val appKey = properties.getProperty("ATM_APP_KEY")?.trim().orEmpty()
        val packageDir = outputDirectory.get().asFile.resolve("com/thinksoftlab/atmlocator/api")
        packageDir.mkdirs()
        packageDir.resolve("ApiConfig.android.kt").writeText(
            """
            |package com.thinksoftlab.atmlocator.api
            |
            |/**
            | * Generated from ATM_API_BASE_URL and ATM_APP_KEY in local.properties.
            | * The Android KMP library plugin has no BuildConfig, so this is the Android actual.
            | * A missing key is an empty string. Empty appKey means omit X-App-Key.
            | * Do not put a host in source.
            | */
            |actual fun platformApiConfig(): ApiConfig = ApiConfig(
            |    baseUrl = ${kotlinStringLiteral(baseUrl)},
            |    appKey = ${kotlinStringLiteral(appKey)},
            |)
            |
            """.trimMargin(),
        )
    }
}

private fun kotlinStringLiteral(value: String): String = buildString {
    append('"')
    for (character in value) {
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '$' -> append("\\$")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            else -> append(character)
        }
    }
    append('"')
}
