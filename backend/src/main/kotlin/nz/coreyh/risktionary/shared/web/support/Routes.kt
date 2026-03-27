package nz.coreyh.risktionary.shared.web.support

object Routes {
    object V1 {
        const val BASE = "/v1"

        object Auth {
            const val BASE = "${V1.BASE}/auth"
            const val LOGOUT = "$BASE/logout"
        }

        object OAuth {
            const val BASE = "${V1.BASE}/oauth"
            const val CALLBACK = "$BASE/{registrationId}/callback"
        }

        object User {
            const val BASE = "${V1.BASE}/user"
            const val ME = "$BASE/me"
        }
    }
}
