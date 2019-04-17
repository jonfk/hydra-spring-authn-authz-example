package ca.jonfk.auth.hydra.response

data class AcceptLoginRequest(
        val acr: String?,
        val force_subject_identifier: String?,
        val remember: Boolean?,
        val remember_for: Int?,
        val subject: String?
)