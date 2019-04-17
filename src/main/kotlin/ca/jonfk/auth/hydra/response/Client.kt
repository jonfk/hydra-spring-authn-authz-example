package ca.jonfk.auth.hydra.response

data class Client(
        val allowed_cors_origins: List<String> = listOf(),
        val audience: List<String> = listOf(),
        val client_id: String?,
        val client_name: String?,
        val client_secret: String?,
        val client_secret_expires_at: Int?,
        val client_uri: String?,
        val contacts: List<String> = listOf(),
        val grant_types: List<String> = listOf(),
        val jwks: Jwks?,
        val jwks_uri: String?,
        val logo_uri: String?,
        val owner: String?,
        val policy_uri: String?,
        val redirect_uris: List<String> = listOf(),
        val request_object_signing_alg: String?,
        val request_uris: List<String> = listOf(),
        val response_types: List<String> = listOf(),
        val scope: String?,
        val sector_identifier_uri: String?,
        val subject_type: String?,
        val token_endpoint_auth_method: String?,
        val tos_uri: String?,
        val userinfo_signed_response_alg: String?
) {
    data class Jwks(
            val keys: List<Key> = listOf()
    ) {
        data class Key(
                val alg: String?,
                val crv: String?,
                val d: String?,
                val dp: String?,
                val dq: String?,
                val e: String?,
                val k: String?,
                val kid: String?,
                val kty: String?,
                val n: String?,
                val p: String?,
                val q: String?,
                val qi: String?,
                val use: String?,
                val x: String?,
                val x5c: List<String> = listOf(),
                val y: String?
        )
    }
}
