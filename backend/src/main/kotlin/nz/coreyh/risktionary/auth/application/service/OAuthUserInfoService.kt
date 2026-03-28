package nz.coreyh.risktionary.auth.application.service

import nz.coreyh.risktionary.auth.domain.model.OAuthProvider
import nz.coreyh.risktionary.auth.domain.model.OAuthUserInfo
import nz.coreyh.risktionary.auth.infrastructure.extractor.OAuthUserInfoExtractor
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

@Component
class OAuthUserInfoService(
    mappers: List<OAuthUserInfoExtractor>,
) {
    private val mappers = mappers.associateBy { it.provider }

    fun extract(
        user: OAuth2User,
        registrationId: String,
    ): OAuthUserInfo {
        val mapper =
            OAuthProvider.from(registrationId)?.let {
                mappers[it]
            } ?: throw IllegalArgumentException("Unsupported provider: $registrationId")

        return mapper.extract(user)
    }
}
