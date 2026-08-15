package io.github.iamsorry.neatfreak.domain

import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LinkCleanerTest {
    @Test
    fun instagramRemovesEntireQuery() = runTest {
        val result = cleaner().clean(
            "https://www.instagram.com/reel/example-post/?utm_source=ig_web_copy_link&igsh=test-share-token",
        )

        assertEquals(PlatformKind.INSTAGRAM, result.platform)
        assertEquals("https://www.instagram.com/reel/example-post/", result.cleanUrl)
        assertEquals(listOf("igsh", "utm_source"), result.removedParameters)
    }

    @Test
    fun instagramProfileRemovesEntireQuery() = runTest {
        val result = cleaner().clean(
            "https://www.instagram.com/example_user?igsh=test-share-token",
        )

        assertEquals(PlatformKind.INSTAGRAM, result.platform)
        assertEquals("https://www.instagram.com/example_user", result.cleanUrl)
        assertEquals(listOf("igsh"), result.removedParameters)
    }

    @Test
    fun linkedInShortLinkUsesResolvedPostAndDropsQuery() = runTest {
        val source = "https://lnkd.in/p/example-short-code"
        val resolved =
            "https://www.linkedin.com/posts/example-user_example-post-123/?utm_source=social_share_send&utm_medium=android_app&rcm=test-member-token&utm_campaign=copy_link"
        val result = cleaner(mapOf(source to resolved)).clean(source)

        assertEquals(PlatformKind.LINKEDIN, result.platform)
        assertEquals(
            "https://www.linkedin.com/posts/example-user_example-post-123/",
            result.cleanUrl,
        )
        assertEquals(listOf("rcm", "utm_campaign", "utm_medium", "utm_source"), result.removedParameters)
        assertEquals(1, result.redirectCount)
    }

    @Test
    fun directLinkedInPostDropsQueryAndFragment() = runTest {
        val result = cleaner().clean(
            "https://www.linkedin.com/posts/example_post-123/?trk=public_post#comments",
        )

        assertEquals(PlatformKind.LINKEDIN, result.platform)
        assertEquals("https://www.linkedin.com/posts/example_post-123/", result.cleanUrl)
        assertEquals(listOf("trk"), result.removedParameters)
    }

    @Test
    fun spotifyHandlesLineBreakBeforeQuery() = runTest {
        val result = cleaner().clean(
            """https://open.spotify.com/track/example-track-id
                |?si=test-share-token&utm_source=copy-link&context=spotify%3Aplaylist%3Aexample-playlist-id
            """.trimMargin(),
        )

        assertEquals(PlatformKind.SPOTIFY, result.platform)
        assertEquals("https://open.spotify.com/track/example-track-id", result.cleanUrl)
    }

    @Test
    fun steamAppRemovesEntireQuery() = runTest {
        val result = cleaner().clean(
            "https://store.steampowered.com/app/606150/Moonlighter/?curator_clanid=test-curator-id",
        )

        assertEquals(PlatformKind.STEAM, result.platform)
        assertEquals("https://store.steampowered.com/app/606150/Moonlighter/", result.cleanUrl)
        assertEquals(listOf("curator_clanid"), result.removedParameters)
    }

    @Test
    fun youtubeShortLinkBecomesCanonicalWatchUrl() = runTest {
        val result = cleaner().clean(
            "https://youtu.be/Kj5j2Vv4auk?si=test-share-token",
        )

        assertEquals(PlatformKind.YOUTUBE, result.platform)
        assertEquals("https://www.youtube.com/watch?v=Kj5j2Vv4auk", result.cleanUrl)
        assertEquals(listOf("si"), result.removedParameters)
    }

    @Test
    fun youtubeShortLinkKeepsPlaybackTime() = runTest {
        val result = cleaner().clean(
            "https://youtu.be/Kj5j2Vv4auk?si=test-share-token&t=900",
        )

        assertEquals(PlatformKind.YOUTUBE, result.platform)
        assertEquals("https://www.youtube.com/watch?v=Kj5j2Vv4auk&t=900", result.cleanUrl)
        assertEquals(listOf("si"), result.removedParameters)
    }

    @Test
    fun youtubeWatchLinkKeepsVideoIdAndRemovesOtherParameters() = runTest {
        val result = cleaner().clean(
            "https://www.youtube.com/watch?v=Kj5j2Vv4auk&si=test-share-token",
        )

        assertEquals(PlatformKind.YOUTUBE, result.platform)
        assertEquals("https://www.youtube.com/watch?v=Kj5j2Vv4auk", result.cleanUrl)
        assertEquals(listOf("si"), result.removedParameters)
    }

    @Test
    fun youtubeWatchLinkKeepsPlaybackTime() = runTest {
        val result = cleaner().clean(
            "https://youtube.com/watch?v=Kj5j2Vv4auk&si=test-share-token&t=900",
        )

        assertEquals(PlatformKind.YOUTUBE, result.platform)
        assertEquals("https://youtube.com/watch?v=Kj5j2Vv4auk&t=900", result.cleanUrl)
        assertEquals(listOf("si"), result.removedParameters)
    }

    @Test
    fun amazonUsKeepsOnlyDpProductPath() = runTest {
        val result = cleaner().clean(
            "https://www.amazon.com/-/zh_TW/Google-Nest-Wifi/dp/B08HRPDYTP/ref=sr_1_32?_encoding=UTF8&dib_tag=se&keywords=smart%2Bhome#details",
        )

        assertEquals(PlatformKind.AMAZON, result.platform)
        assertEquals("https://www.amazon.com/dp/B08HRPDYTP/", result.cleanUrl)
        assertEquals(listOf("_encoding", "dib_tag", "keywords"), result.removedParameters)
    }

    @Test
    fun amazonJapanKeepsOnlyDpProductPath() = runTest {
        val result = cleaner().clean(
            "https://www.amazon.co.jp/%E3%83%9D%E3%82%B1%E3%83%A2%E3%83%B3/Pok%C3%A9mon-fit/dp/B07FD75Q37/ref=lp_1_2?pf_rd_p=value&th=1",
        )

        assertEquals(PlatformKind.AMAZON, result.platform)
        assertEquals("https://www.amazon.co.jp/dp/B07FD75Q37/", result.cleanUrl)
        assertEquals(listOf("pf_rd_p", "th"), result.removedParameters)
    }

    @Test
    fun nonProductAmazonLinkUsesGenericCleaning() = runTest {
        val result = cleaner().clean(
            "https://www.amazon.com/gp/help/customer/display.html?nodeId=123&utm_source=test",
        )

        assertEquals(PlatformKind.GENERIC, result.platform)
        assertEquals(
            "https://www.amazon.com/gp/help/customer/display.html?nodeId=123",
            result.cleanUrl,
        )
    }

    @Test
    fun genericLinkKeepsFunctionalParametersAndFragment() = runTest {
        val result = cleaner().clean(
            "https://aquosmobile.sharp.com.tw/product/view?product_category_id=2&product_id=17&utm_medium=paid&utm_source=fb&fbclid=abc#details",
        )

        assertEquals(PlatformKind.GENERIC, result.platform)
        assertEquals(
            "https://aquosmobile.sharp.com.tw/product/view?product_category_id=2&product_id=17#details",
            result.cleanUrl,
        )
        assertEquals(listOf("fbclid", "utm_medium", "utm_source"), result.removedParameters)
    }

    @Test
    fun trackerNamesAreMatchedWithoutCaseSensitivity() = runTest {
        val result = cleaner().clean(
            "https://example.com/page?UTM_Source=Facebook&FbClId=abc&id=7",
        )

        assertEquals("https://example.com/page?id=7", result.cleanUrl)
    }

    @Test
    fun threadsShareUsesResolvedUrlAndDropsQuery() = runTest {
        val source = "https://www.threads.com/share/example-share-code/"
        val resolved = "https://www.threads.com/@example_user/post/example-post-id?xmt=tracking&slof=1"
        val result = cleaner(mapOf(source to resolved)).clean(source)

        assertEquals(PlatformKind.THREADS, result.platform)
        assertEquals(
            "https://www.threads.com/@example_user/post/example-post-id",
            result.cleanUrl,
        )
        assertEquals(1, result.redirectCount)
    }

    @Test
    fun facebookShareUsesResolvedUrlAndDropsQueryAndFragment() = runTest {
        val source = "https://www.facebook.com/share/p/example-share-code/"
        val resolved = "https://www.facebook.com/example.page/posts/example-post-id?rdid=value#"
        val result = cleaner(mapOf(source to resolved)).clean(source)

        assertEquals(PlatformKind.FACEBOOK, result.platform)
        assertEquals("https://www.facebook.com/example.page/posts/example-post-id", result.cleanUrl)
    }

    @Test
    fun facebookExternalRedirectRemovesOnlyTrackers() = runTest {
        val source = "https://l.facebook.com/l.php?u=opaque&h=signature"
        val resolved = "https://aquosmobile.sharp.com.tw/product/view?product_category_id=2&product_id=17&utm_campaign=campaign&fbclid=click"
        val result = cleaner(mapOf(source to resolved)).clean(source)

        assertEquals(PlatformKind.FACEBOOK_EXTERNAL, result.platform)
        assertEquals(
            "https://aquosmobile.sharp.com.tw/product/view?product_category_id=2&product_id=17",
            result.cleanUrl,
        )
    }

    @Test
    fun facebookExternalUsesGenericCleaningEvenForSupportedDestination() = runTest {
        val source = "https://l.facebook.com/l.php?u=opaque&h=signature"
        val resolved =
            "https://youtu.be/Kj5j2Vv4auk?si=share-token&t=90&utm_source=facebook&fbclid=click"
        val result = cleaner(mapOf(source to resolved)).clean(source)

        assertEquals(PlatformKind.FACEBOOK_EXTERNAL, result.platform)
        assertEquals(
            "https://youtu.be/Kj5j2Vv4auk?si=share-token&t=90",
            result.cleanUrl,
        )
        assertEquals(listOf("fbclid", "utm_source"), result.removedParameters)
    }

    @Test
    fun extractsUrlFromSharedText() = runTest {
        val result = cleaner().clean(
            "A useful page: https://example.com/article?id=3&utm_source=test。",
        )

        assertEquals("https://example.com/article?id=3", result.cleanUrl)
    }

    @Test
    fun rejectsInputWithoutUrl() = runTest {
        val failure = runCatching { cleaner().clean("No link here") }.exceptionOrNull()
        assertTrue(failure is LinkCleaningException)
    }

    private fun cleaner(resolutions: Map<String, String> = emptyMap()): LinkCleaner {
        val resolver = RedirectResolver { source: HttpUrl, _: RedirectRule ->
            val destination = resolutions[source.toString()]
                ?: error("No fake redirect configured for $source")
            Resolution(destination.toHttpUrl(), 1)
        }
        return LinkCleaner(resolver)
    }
}
