package ca.jonfk.auth.hydra.response

data class RejectRequest(
        val error: String?,
        val error_debug: String? = null,
        val error_description: String?,
        val error_hint: String? = null,
        val status_code: Int? = null
)