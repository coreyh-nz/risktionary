package nz.coreyh.risktionary.shared.config

import nz.coreyh.risktionary.auth.infrastructure.security.JwtAuthenticationFilter
import nz.coreyh.risktionary.auth.infrastructure.security.OAuth2FailureHandler
import nz.coreyh.risktionary.auth.infrastructure.security.OAuth2SuccessHandler
import nz.coreyh.risktionary.auth.infrastructure.security.OAuthRedirectCookieFilter
import nz.coreyh.risktionary.shared.web.security.ApiAccessDeniedHandler
import nz.coreyh.risktionary.shared.web.security.ApiAuthenticationEntryPoint
import nz.coreyh.risktionary.shared.web.support.Routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val apiAuthenticationEntryPoint: ApiAuthenticationEntryPoint,
    private val apiAccessDeniedHandler: ApiAccessDeniedHandler,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oauth2FailureHandler: OAuth2FailureHandler,
    private val oAuthRedirectCookieFilter: OAuthRedirectCookieFilter,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val appProperties: AppProperties,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            logout { disable() }
            cors { }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            authorizeHttpRequests {
                authorize("${Routes.V1.OAuth.BASE}/**", permitAll)
                authorize(Routes.V1.Game.JOIN, permitAll)
                authorize(Routes.V1.Game.SOCKET, permitAll)
                authorize(anyRequest, authenticated)
            }

            oauth2Login {
                loginPage = "/" // disable the login page
                authenticationSuccessHandler = oAuth2SuccessHandler
                authenticationFailureHandler = oauth2FailureHandler
                authorizationEndpoint {
                    baseUri = Routes.V1.OAuth.BASE
                }
                redirectionEndpoint {
                    baseUri = Routes.V1.OAuth.CALLBACK
                }
            }

            exceptionHandling {
                authenticationEntryPoint = apiAuthenticationEntryPoint
                accessDeniedHandler = apiAccessDeniedHandler
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
            addFilterBefore<OAuth2AuthorizationRequestRedirectFilter>(oAuthRedirectCookieFilter)
        }
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config =
            CorsConfiguration().apply {
                allowedOrigins = listOf(appProperties.frontendUrl)
                allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
                allowedHeaders = listOf("*")
                allowCredentials = true // required for cookies
                maxAge = 3600
            }

        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", config)
        }
    }
}
