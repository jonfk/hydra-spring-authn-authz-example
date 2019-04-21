package ca.jonfk.auth.hydra.response

data class OidcContext(
        val acr_values: List<String> = listOf(),
        val display: String?,
        val id_token_hint_claims: Map<String, Any> = mapOf(),
        val login_hint: String?,
        val ui_locales: List<String> = listOf()
)
