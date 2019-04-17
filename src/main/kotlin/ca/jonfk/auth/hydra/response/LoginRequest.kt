package ca.jonfk.auth.hydra.response

data class LoginRequest(
        val challenge: String?,
        val client: Client?,
        val oidc_context: OidcContext?,
        val request_url: String?,
        val requested_access_token_audience: List<String> = listOf(),
        val requested_scope: List<String> = listOf(),
        val session_id: String?,
        val skip: Boolean?,
        val subject: String?
)