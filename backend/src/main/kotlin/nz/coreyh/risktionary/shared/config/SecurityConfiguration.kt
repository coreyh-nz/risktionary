package nz.coreyh.risktionary.shared.config

import nz.coreyh.risktionary.auth.infrastructure.security.JwtAuthenticationFilter
import nz.coreyh.risktionary.auth.infrastructure.security.OAuth2FailureHandler
import nz.coreyh.risktionary.auth.infrastructure.security.OAuth2SuccessHandler
import nz.coreyh.risktionary.shared.web.support.Routes
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val oauth2FailureHandler: OAuth2FailureHandler,
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            logout { disable() }

            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }

            authorizeHttpRequests {
                authorize(anyRequest, permitAll)
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
                authenticationEntryPoint =
                    HttpStatusEntryPoint(
                        HttpStatus.UNAUTHORIZED,
                    )
            }

            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthenticationFilter)
        }
        return http.build()
    }
}
