package io.github.iamsorry.neatfreak.network

import io.github.iamsorry.neatfreak.domain.LinkCleaner
import io.github.iamsorry.neatfreak.domain.LinkCleaningException
import io.github.iamsorry.neatfreak.domain.RedirectDestinationPolicy
import io.github.iamsorry.neatfreak.domain.RedirectRule
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HttpRedirectResolverTest {
    @Test
    fun threadsShareStopsAfterResolvingPostBeforeLoginRedirect() = runTest {
        val requestedUrls = mutableListOf<String>()
        val postUrl =
            "https://www.threads.com/@example_user/post/example-post-id?xmt=tracking&slof=1"
        val client = OkHttpClient.Builder()
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                requestedUrls += request.url.toString()
                val location = when (request.url.encodedPath) {
                    "/share/example-share-code/" -> postUrl
                    else -> "https://www.threads.com/?error=invalid_post"
                }
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(302)
                    .message("Found")
                    .header("Location", location)
                    .body(ByteArray(0).toResponseBody(null))
                    .build()
            })
            .build()
        val cleaner = LinkCleaner(HttpRedirectResolver(client))

        val result = cleaner.clean("https://www.threads.com/share/example-share-code/")

        assertEquals(postUrl, result.resolvedUrl)
        assertEquals(
            "https://www.threads.com/@example_user/post/example-post-id",
            result.cleanUrl,
        )
        assertEquals(1, result.redirectCount)
        assertEquals(
            listOf("https://www.threads.com/share/example-share-code/"),
            requestedUrls,
        )
    }

    @Test
    fun linkedInShortLinkStopsAfterResolvingPost() = runTest {
        val requestedUrls = mutableListOf<String>()
        val source = "https://lnkd.in/p/example-short-code"
        val postUrl =
            "https://www.linkedin.com/posts/example-user_example-post-123/?utm_source=share&rcm=test-member-token"
        val client = redirectingClient { request ->
            requestedUrls += request.url.toString()
            redirectResponse(
                request,
                if (request.url.host == "lnkd.in") postUrl else "https://www.linkedin.com/login",
            )
        }
        val cleaner = LinkCleaner(HttpRedirectResolver(client))

        val result = cleaner.clean(source)

        assertEquals(postUrl, result.resolvedUrl)
        assertEquals("https://www.linkedin.com/posts/example-user_example-post-123/", result.cleanUrl)
        assertEquals(1, result.redirectCount)
        assertEquals(listOf(source), requestedUrls)
    }

    @Test
    fun relativeRedirectIsResolvedAgainstCurrentUrl() = runTest {
        val source = "https://www.facebook.com/share/p/example/"
        val client = redirectingClient { request ->
            if (request.url.encodedPath.startsWith("/share/")) {
                redirectResponse(request, "/author/posts/123?rdid=value")
            } else {
                successResponse(request)
            }
        }

        val result = HttpRedirectResolver(client).resolve(
            source.toHttpUrl(),
            sameDomainRule("facebook.com"),
        )

        assertEquals("https://www.facebook.com/author/posts/123?rdid=value", result.url.toString())
        assertEquals(1, result.redirectCount)
    }

    @Test
    fun getIsUsedWhenHeadDoesNotRedirect() = runTest {
        val source = "https://www.facebook.com/share/p/example/"
        val requestedMethods = mutableListOf<String>()
        val client = redirectingClient { request ->
            requestedMethods += request.method
            when {
                request.method == "HEAD" -> successResponse(request, code = 405)
                request.url.encodedPath.startsWith("/share/") ->
                    redirectResponse(request, "/author/posts/123")
                else -> successResponse(request)
            }
        }

        val result = HttpRedirectResolver(client).resolve(
            source.toHttpUrl(),
            sameDomainRule("facebook.com"),
        )

        assertEquals("https://www.facebook.com/author/posts/123", result.url.toString())
        assertEquals(listOf("HEAD", "GET", "GET"), requestedMethods)
    }

    @Test
    fun samePlatformRedirectRejectsExternalHost() = runTest {
        val source = "https://www.facebook.com/share/p/example/"
        val client = redirectingClient { request ->
            redirectResponse(request, "https://example.com/untrusted")
        }

        val failure = runCatching {
            HttpRedirectResolver(client).resolve(
                source.toHttpUrl(),
                sameDomainRule("facebook.com"),
            )
        }.exceptionOrNull()

        assertTrue(failure is LinkCleaningException)
        assertEquals("The short link redirected outside its platform.", failure?.message)
    }

    @Test
    fun externalRedirectRejectsPrivateDestination() = runTest {
        val source = "https://l.facebook.com/l.php?u=opaque&h=signature"
        val client = redirectingClient { request ->
            redirectResponse(request, "https://192.168.1.10/private")
        }

        val failure = runCatching {
            HttpRedirectResolver(client).resolve(
                source.toHttpUrl(),
                RedirectRule(RedirectDestinationPolicy.PublicHttps),
            )
        }.exceptionOrNull()

        assertTrue(failure is LinkCleaningException)
        assertEquals("Local and private-network destinations are not allowed.", failure?.message)
    }

    private fun sameDomainRule(domain: String) = RedirectRule(
        RedirectDestinationPolicy.SameDomain(domain),
    )

    private fun redirectingClient(
        respond: (okhttp3.Request) -> Response,
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(Interceptor { chain -> respond(chain.request()) })
        .build()

    private fun redirectResponse(request: okhttp3.Request, location: String): Response =
        response(request, code = 302).newBuilder()
            .header("Location", location)
            .build()

    private fun successResponse(request: okhttp3.Request, code: Int = 200): Response =
        response(request, code)

    private fun response(request: okhttp3.Request, code: Int): Response = Response.Builder()
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(code)
        .message(if (code in 300..399) "Found" else "OK")
        .body(ByteArray(0).toResponseBody(null))
        .build()
}
