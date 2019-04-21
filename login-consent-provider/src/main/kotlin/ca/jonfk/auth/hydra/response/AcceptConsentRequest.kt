package ca.jonfk.auth.hydra.response

data class AcceptConsentRequest(
        val grant_access_token_audience: List<String> = listOf(),
        val grant_scope: List<String> = listOf(),
        val remember: Boolean?,
        val remember_for: Int?,
        val session: Session?
) {
    data class Session(
            val access_token: Map<String, Any> = mapOf(),
            val id_token: Map<String, Any> = mapOf()
    )
}