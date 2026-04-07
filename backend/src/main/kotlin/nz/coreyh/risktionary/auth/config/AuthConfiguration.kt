package nz.coreyh.risktionary.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.server.Cookie
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(CookieProperties::class)
class AuthConfiguration(
    val cookie: CookieProperties,
)

@ConfigurationProperties(prefix = "app.auth.cookie")
data class CookieProperties(
    val path: String,
    val secure: Boolean,
    val sameSite: Cookie.SameSite,
    val accessTokenName: String,
    val loginSuccessRedirectUrlName: String,
)
