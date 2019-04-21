package ca.jonfk.auth.hydra.response

data class ConsentRequest(
        val acr: String?,
        val challenge: String?,
        val client: Client?,
        val login_challenge: String?,
        val login_session_id: String?,
        val oidc_context: OidcContext?,
        val request_url: String?,
        val requested_access_token_audience: List<String> = listOf(),
        val requested_scope: List<String> = listOf(),
        val skip: Boolean?,
        val subject: String?
) {
}