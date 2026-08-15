package io.github.iamsorry.neatfreak.domain

import okhttp3.HttpUrl
import java.util.Locale

sealed interface RedirectDestinationPolicy {
    data class SameDomain(
        val domain: String,
    ) : RedirectDestinationPolicy

    data object PublicHttps : RedirectDestinationPolicy
}

data class RedirectRule(
    val destinationPolicy: RedirectDestinationPolicy,
    val stopWhen: (HttpUrl) -> Boolean = { false },
)

sealed interface QueryPolicy {
    data object DropAll : QueryPolicy

    data class KeepOnly(
        val names: Set<String>,
    ) : QueryPolicy

    data class RemoveMatching(
        val exactNames: Set<String>,
        val prefixes: Set<String>,
    ) : QueryPolicy
}

internal enum class Canonicalization {
    NONE,
    YOUTUBE,
    AMAZON,
}

internal data class SourcePlan(
    val platform: PlatformKind,
    val redirectRule: RedirectRule? = null,
)

internal data class CleanupRule(
    val queryPolicy: QueryPolicy,
    val removeFragment: Boolean,
    val canonicalization: Canonicalization = Canonicalization.NONE,
)

internal object SourceRuleBook {
    private val instagramContentTypes = setOf("p", "reel", "reels", "tv")
    private val instagramUsername = Regex("^[A-Za-z0-9._]{1,30}$")
    private val spotifyContentTypes = setOf("track", "album", "playlist", "artist", "show", "episode")

    fun planFor(url: HttpUrl): SourcePlan {
        val host = url.host.lowercase(Locale.ROOT)
        val firstPathSegment = url.pathSegments.firstOrNull()?.lowercase(Locale.ROOT).orEmpty()

        return when {
            hostBelongsTo(host, "threads.com") && url.encodedPath.startsWith("/share/") ->
                SourcePlan(
                    platform = PlatformKind.THREADS,
                    // Unauthenticated requests may redirect a revealed post to an error page.
                    redirectRule = sameDomainRedirect("threads.com", ::isThreadsPost),
                )

            host == "l.facebook.com" && url.encodedPath == "/l.php" ->
                SourcePlan(
                    platform = PlatformKind.FACEBOOK_EXTERNAL,
                    redirectRule = RedirectRule(RedirectDestinationPolicy.PublicHttps),
                )

            hostBelongsTo(host, "facebook.com") && url.encodedPath.startsWith("/share/") ->
                SourcePlan(
                    platform = PlatformKind.FACEBOOK,
                    redirectRule = sameDomainRedirect("facebook.com"),
                )

            hostBelongsTo(host, "instagram.com") &&
                (firstPathSegment in instagramContentTypes || isInstagramProfile(url)) ->
                SourcePlan(PlatformKind.INSTAGRAM)

            host == "lnkd.in" && firstPathSegment == "p" ->
                SourcePlan(
                    platform = PlatformKind.LINKEDIN,
                    redirectRule = sameDomainRedirect("linkedin.com", ::isLinkedInPost),
                )

            isLinkedInPost(url) -> SourcePlan(PlatformKind.LINKEDIN)

            host == "open.spotify.com" && firstPathSegment in spotifyContentTypes ->
                SourcePlan(PlatformKind.SPOTIFY)

            host == "store.steampowered.com" && firstPathSegment == "app" ->
                SourcePlan(PlatformKind.STEAM)

            host == "youtu.be" && firstPathSegment.isNotEmpty() ->
                SourcePlan(PlatformKind.YOUTUBE)

            hostBelongsTo(host, "youtube.com") &&
                url.encodedPath == "/watch" &&
                !url.queryParameter("v").isNullOrEmpty() ->
                SourcePlan(PlatformKind.YOUTUBE)

            isAmazonHost(host) && amazonProductCode(url) != null ->
                SourcePlan(PlatformKind.AMAZON)

            else -> SourcePlan(PlatformKind.GENERIC)
        }
    }

    private fun sameDomainRedirect(
        domain: String,
        stopWhen: (HttpUrl) -> Boolean = { false },
    ) = RedirectRule(
        destinationPolicy = RedirectDestinationPolicy.SameDomain(domain),
        stopWhen = stopWhen,
    )

    private fun isThreadsPost(url: HttpUrl): Boolean {
        if (!hostBelongsTo(url.host, "threads.com")) return false

        val segments = url.pathSegments
        return segments.size >= 3 &&
            segments[0].startsWith("@") &&
            segments[0].length > 1 &&
            segments[1] == "post" &&
            segments[2].isNotEmpty()
    }

    private fun isInstagramProfile(url: HttpUrl): Boolean {
        val nonEmptySegments = url.pathSegments.filter(String::isNotEmpty)
        return nonEmptySegments.size == 1 && instagramUsername.matches(nonEmptySegments.single())
    }

    private fun isLinkedInPost(url: HttpUrl): Boolean =
        hostBelongsTo(url.host, "linkedin.com") &&
            url.pathSegments.firstOrNull().equals("posts", ignoreCase = true)

    private fun isAmazonHost(host: String): Boolean =
        hostBelongsTo(host, "amazon.com") || hostBelongsTo(host, "amazon.co.jp")

    private fun hostBelongsTo(host: String, domain: String): Boolean =
        host == domain || host.endsWith(".$domain")
}

internal object CleanupRuleBook {
    private val trackerPolicy = QueryPolicy.RemoveMatching(
        exactNames = setOf("fbclid"),
        prefixes = setOf("utm_"),
    )
    private val dropAll = CleanupRule(
        queryPolicy = QueryPolicy.DropAll,
        removeFragment = true,
    )
    private val generic = CleanupRule(
        queryPolicy = trackerPolicy,
        removeFragment = false,
    )

    fun ruleFor(platform: PlatformKind): CleanupRule = when (platform) {
        PlatformKind.THREADS,
        PlatformKind.FACEBOOK,
        PlatformKind.INSTAGRAM,
        PlatformKind.LINKEDIN,
        PlatformKind.SPOTIFY,
        PlatformKind.STEAM,
        -> dropAll

        PlatformKind.YOUTUBE -> CleanupRule(
            queryPolicy = QueryPolicy.KeepOnly(names = setOf("v", "t")),
            removeFragment = true,
            canonicalization = Canonicalization.YOUTUBE,
        )

        PlatformKind.AMAZON -> CleanupRule(
            queryPolicy = QueryPolicy.DropAll,
            removeFragment = true,
            canonicalization = Canonicalization.AMAZON,
        )

        PlatformKind.FACEBOOK_EXTERNAL,
        PlatformKind.GENERIC,
        -> generic
    }
}

internal fun amazonProductCode(url: HttpUrl): String? = url.pathSegments
    .zipWithNext()
    .firstOrNull { (segment, productCode) ->
        segment.equals("dp", ignoreCase = true) && productCode.isNotEmpty()
    }
    ?.second
