package nz.coreyh.risktionary.shared.web.support

import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.web.server.Cookie
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * A DSL builder for creating ResponseCookies.
 * Taken from https://github.com/coreyh-nz/uni-tracker.nz-new/blob/dev/backend/common/src/main/kotlin/nz/unitracker/backend/common/web/util/CookieBuilder.kt
 */
class CookieBuilder(
    private val name: String,
    private val value: String,
) {
    var lifetime: Duration? = null
    var path: String? = null
    var domain: String? = null
    var sameSite: Cookie.SameSite = Cookie.SameSite.STRICT
    var httpOnly: Boolean = true
    var secure: Boolean = true

    fun build(): ResponseCookie {
        val builder =
            ResponseCookie
                .from(name, value)
                .httpOnly(httpOnly)
                .secure(secure)
                .sameSite(sameSite.attributeValue())

        path?.let { builder.path(it) }
        domain?.let { builder.domain(it) }
        lifetime?.let { builder.maxAge(it.toJavaDuration()) }

        return builder.build()
    }
}

/**
 * Creates a ResponseCookie using the DSL builder.
 *
 * @param name The name of the cookie.
 * @param value The value of the cookie.
 * @param block The DSL block to configure the cookie.
 * @return The built ResponseCookie.
 */
fun createCookie(
    name: String,
    value: String,
    block: CookieBuilder.() -> Unit = {},
): ResponseCookie {
    val builder = CookieBuilder(name, value)
    builder.block()
    return builder.build()
}

/**
 * Creates a deletion ResponseCookie (sets value to empty and maxAge to 0).
 *
 * @param name The name of the cookie to delete.
 * @param block The DSL block to configure the deletion cookie (e.g., path, domain).
 * @return The built deletion ResponseCookie.
 */
fun createDeleteCookie(
    name: String,
    block: CookieBuilder.() -> Unit = {},
): ResponseCookie {
    val builder = CookieBuilder(name, "")
    builder.block()
    builder.lifetime = 0.seconds // force maxAge to 0 for deletion
    return builder.build()
}

/**
 * Extension function to add a cookie to an HttpServletResponse.
 */
fun HttpServletResponse.addCookie(
    name: String,
    value: String,
    block: CookieBuilder.() -> Unit = {},
): HttpServletResponse {
    val cookie = createCookie(name, value, block)
    addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    return this
}

/**
 * Extension function to add a cookie to an ResponseEntity.HeadersBuilder.
 */
fun ResponseEntity.HeadersBuilder<*>.addCookie(
    name: String,
    value: String,
    block: CookieBuilder.() -> Unit = {},
) {
    val cookie = createCookie(name, value, block)
    header(HttpHeaders.SET_COOKIE, cookie.toString())
}

/**
 * Extension function to delete a cookie via HttpServletResponse.
 */
fun HttpServletResponse.deleteCookie(
    name: String,
    block: CookieBuilder.() -> Unit = {},
): HttpServletResponse {
    val cookie = createDeleteCookie(name, block)
    addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    return this
}

/**
 * Extension function to delete a cookie via ResponseEntity.HeadersBuilder.
 */
fun ResponseEntity.HeadersBuilder<*>.deleteCookie(
    name: String,
    block: CookieBuilder.() -> Unit = {},
) {
    val cookie = createDeleteCookie(name, block)
    header(HttpHeaders.SET_COOKIE, cookie.toString())
}

/**
 * Extension function to add a cookie to ResponseEntity's headers.
 */
fun ResponseEntity.BodyBuilder.addCookie(
    name: String,
    value: String,
    block: CookieBuilder.() -> Unit = {},
): ResponseEntity.BodyBuilder {
    val cookie = createCookie(name, value, block)
    header(HttpHeaders.SET_COOKIE, cookie.toString())
    return this
}

/**
 * Extension function to delete a cookie via ResponseEntity's headers.
 */
fun ResponseEntity.BodyBuilder.deleteCookie(
    name: String,
    block: CookieBuilder.() -> Unit = {},
): ResponseEntity.BodyBuilder {
    val cookie = createDeleteCookie(name, block)
    header(HttpHeaders.SET_COOKIE, cookie.toString())
    return this
}
