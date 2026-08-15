package io.github.iamsorry.neatfreak.domain

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.util.Locale

internal object UrlExtractor {
    private val urlPattern = Regex("""https?://[^\s<>\"']+""", RegexOption.IGNORE_CASE)
    private val wrappedQueryPattern = Regex("""\r?\n(?=[?&#])""")
    private val trailingPunctuation = charArrayOf('.', ',', ';', '!', '。', '，', '、', '）', '】', '》')

    fun extract(input: String): HttpUrl {
        val normalized = input.replace(wrappedQueryPattern, "")
        val candidate = urlPattern.find(normalized)?.value
            ?.trimEnd(*trailingPunctuation)
            ?: throw LinkCleaningException("No HTTP or HTTPS URL was found.")

        return candidate.toHttpUrlOrNull()
            ?: throw LinkCleaningException("The URL is not valid.")
    }
}

internal data class SanitizedUrl(
    val url: HttpUrl,
    val removedParameters: List<String>,
)

internal object UrlSanitizer {
    fun sanitize(
        url: HttpUrl,
        policy: QueryPolicy,
        removeFragment: Boolean,
    ): SanitizedUrl {
        val builder = url.newBuilder()
        val removed = when (policy) {
            QueryPolicy.DropAll -> {
                val names = url.queryParameterNames.sorted()
                builder.query(null)
                names
            }
            is QueryPolicy.KeepOnly -> {
                val names = url.queryParameterNames.filterNot { it in policy.names }.sorted()
                names.forEach(builder::removeAllQueryParameters)
                names
            }
            is QueryPolicy.RemoveMatching -> {
                val names = url.queryParameterNames.filter { name ->
                    val normalized = name.lowercase(Locale.ROOT)
                    normalized in policy.exactNames || policy.prefixes.any(normalized::startsWith)
                }.sorted()
                names.forEach(builder::removeAllQueryParameters)
                names
            }
        }

        if (removeFragment) builder.fragment(null)
        return SanitizedUrl(builder.build(), removed)
    }
}

internal object UrlCanonicalizer {
    private val youtubeWatchUrl = "https://www.youtube.com/watch".toHttpUrl()

    fun canonicalize(url: HttpUrl, canonicalization: Canonicalization): HttpUrl =
        when (canonicalization) {
            Canonicalization.NONE -> url
            Canonicalization.YOUTUBE -> if (url.host == "youtu.be") {
                youtubeWatchUrl.newBuilder()
                    .addQueryParameter("v", url.pathSegments.first())
                    .apply {
                        url.queryParameter("t")?.let { addQueryParameter("t", it) }
                    }
                    .build()
            } else {
                url
            }
            Canonicalization.AMAZON -> url.newBuilder()
                .encodedPath("/")
                .addPathSegment("dp")
                .addPathSegment(checkNotNull(amazonProductCode(url)))
                .addPathSegment("")
                .build()
        }
}
