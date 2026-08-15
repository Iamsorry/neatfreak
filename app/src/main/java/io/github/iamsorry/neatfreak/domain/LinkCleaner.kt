package io.github.iamsorry.neatfreak.domain

import okhttp3.HttpUrl

enum class PlatformKind {
    THREADS,
    FACEBOOK,
    FACEBOOK_EXTERNAL,
    INSTAGRAM,
    LINKEDIN,
    SPOTIFY,
    STEAM,
    YOUTUBE,
    AMAZON,
    GENERIC,
}

data class Resolution(
    val url: HttpUrl,
    val redirectCount: Int,
)

fun interface RedirectResolver {
    suspend fun resolve(url: HttpUrl, rule: RedirectRule): Resolution
}

data class CleanResult(
    val sourceUrl: String,
    val resolvedUrl: String,
    val cleanUrl: String,
    val platform: PlatformKind,
    val removedParameters: List<String>,
    val redirectCount: Int,
)

class LinkCleaningException(message: String) : IllegalArgumentException(message)

class LinkCleaner(
    private val redirectResolver: RedirectResolver,
) {
    suspend fun clean(input: String): CleanResult {
        val source = UrlExtractor.extract(input)
        val sourcePlan = SourceRuleBook.planFor(source)
        val resolution = sourcePlan.redirectRule?.let { rule ->
            redirectResolver.resolve(source, rule)
        } ?: Resolution(source, 0)
        val cleanupRule = CleanupRuleBook.ruleFor(sourcePlan.platform)
        val sanitized = UrlSanitizer.sanitize(
            url = resolution.url,
            policy = cleanupRule.queryPolicy,
            removeFragment = cleanupRule.removeFragment,
        )
        val canonicalUrl = UrlCanonicalizer.canonicalize(
            url = sanitized.url,
            canonicalization = cleanupRule.canonicalization,
        )

        return CleanResult(
            sourceUrl = source.toString(),
            resolvedUrl = resolution.url.toString(),
            cleanUrl = canonicalUrl.toString(),
            platform = sourcePlan.platform,
            removedParameters = sanitized.removedParameters,
            redirectCount = resolution.redirectCount,
        )
    }
}
