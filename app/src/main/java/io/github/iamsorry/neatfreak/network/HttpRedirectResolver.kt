package io.github.iamsorry.neatfreak.network

import io.github.iamsorry.neatfreak.domain.LinkCleaningException
import io.github.iamsorry.neatfreak.domain.RedirectDestinationPolicy
import io.github.iamsorry.neatfreak.domain.RedirectRule
import io.github.iamsorry.neatfreak.domain.RedirectResolver
import io.github.iamsorry.neatfreak.domain.Resolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale
import java.util.concurrent.TimeUnit

class HttpRedirectResolver(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(false)
        .followSslRedirects(false)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .callTimeout(20, TimeUnit.SECONDS)
        .build(),
    private val maxRedirects: Int = 5,
) : RedirectResolver {

    override suspend fun resolve(url: HttpUrl, rule: RedirectRule): Resolution =
        withContext(Dispatchers.IO) {
            val headResult = resolveWithMethod(url, rule, "HEAD")
            if (headResult.redirectCount > 0) {
                headResult
            } else {
                val getResult = resolveWithMethod(url, rule, "GET")
                if (getResult.redirectCount == 0) {
                    throw LinkCleaningException("The short link did not redirect.")
                }
                getResult
            }
        }

    private fun resolveWithMethod(
        source: HttpUrl,
        rule: RedirectRule,
        method: String,
    ): Resolution {
        var current = source
        var redirectCount = 0

        while (true) {
            val requestBuilder = Request.Builder()
                .url(current)
                .header("User-Agent", "NeatFreak/0.1 (Android)")
            if (method == "HEAD") requestBuilder.head() else requestBuilder.get()

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (response.code !in REDIRECT_CODES) {
                    return Resolution(current, redirectCount)
                }

                if (redirectCount >= maxRedirects) {
                    throw LinkCleaningException("The link redirected too many times.")
                }

                val location = response.header("Location")
                    ?: throw LinkCleaningException("The redirect response did not include a destination.")
                val destination = current.resolve(location)
                    ?: throw LinkCleaningException("The redirect destination is invalid.")

                validateDestination(destination, rule.destinationPolicy)
                current = destination
                redirectCount += 1

                if (rule.stopWhen(destination)) {
                    return Resolution(destination, redirectCount)
                }
            }
        }
    }

    private fun validateDestination(
        destination: HttpUrl,
        policy: RedirectDestinationPolicy,
    ) {
        if (destination.scheme != "https") {
            throw LinkCleaningException("Only secure HTTPS redirect destinations are allowed.")
        }

        when (policy) {
            is RedirectDestinationPolicy.SameDomain -> {
                if (!hostBelongsTo(destination.host, policy.domain)) {
                    throw LinkCleaningException("The short link redirected outside its platform.")
                }
            }
            RedirectDestinationPolicy.PublicHttps -> {
                if (!isPublicHost(destination.host)) {
                    throw LinkCleaningException("Local and private-network destinations are not allowed.")
                }
            }
        }
    }

    private fun hostBelongsTo(host: String, domain: String): Boolean =
        host.equals(domain, ignoreCase = true) || host.endsWith(".$domain", ignoreCase = true)

    private fun isPublicHost(hostValue: String): Boolean {
        val host = hostValue.lowercase(Locale.ROOT)
        if (host == "localhost" || host.endsWith(".localhost") || host.endsWith(".local")) return false
        if (':' in host) return false // Block IPv6 literals, including loopback and private ranges.

        val octets = host.split('.').mapNotNull(String::toIntOrNull)
        if (octets.size != 4 || octets.any { it !in 0..255 }) return true

        val first = octets[0]
        val second = octets[1]
        return when {
            first == 0 || first == 10 || first == 127 -> false
            first == 100 && second in 64..127 -> false
            first == 169 && second == 254 -> false
            first == 172 && second in 16..31 -> false
            first == 192 && second == 168 -> false
            first == 198 && second in 18..19 -> false
            first >= 224 -> false
            else -> true
        }
    }

    private companion object {
        val REDIRECT_CODES = setOf(301, 302, 303, 307, 308)
    }
}
