package nz.coreyh.risktionary.shared.web.support.annotation

import org.springframework.security.access.prepost.PreAuthorize

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@PreAuthorize("isAuthenticated()")
annotation class Authenticated
