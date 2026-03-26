package nz.coreyh.risktionary.auth.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.jose.jws.MacAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import java.time.Duration
import javax.crypto.spec.SecretKeySpec
import kotlin.time.toKotlinDuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class JwtConfiguration(
    private val jwtProperties: JwtProperties,
) {
    @Bean
    fun jwtDecoder(): JwtDecoder {
        val secretKey = SecretKeySpec(jwtProperties.secret.toByteArray(), "HmacSHA256")
        return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build()
    }

    @Bean
    fun jwtEncoder(): JwtEncoder {
        val secretKey = SecretKeySpec(jwtProperties.secret.toByteArray(), "HmacSHA256")
        return NimbusJwtEncoder.withSecretKey(secretKey).algorithm(MacAlgorithm.HS256).build()
    }
}

@ConfigurationProperties("jwt")
class JwtProperties(
    val secret: String,
    accessLifetime: Duration, // spring only supports java duration afaik
) {
    val accessLifetime = accessLifetime.toKotlinDuration()
}
