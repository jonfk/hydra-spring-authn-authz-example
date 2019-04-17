package ca.jonfk.auth

import ca.jonfk.auth.hydra.HydraService
import ca.jonfk.auth.hydra.response.AcceptConsentRequest
import ca.jonfk.auth.hydra.response.AcceptLoginRequest
import ca.jonfk.auth.hydra.response.RejectRequest
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView


@Controller
class LoginAndConsentController(val hydraService: HydraService) {

    @RequestMapping("/login")
    fun login(@RequestParam("error") error: String?,
              @RequestParam("login_challenge") challenge: String?): ModelAndView {
        val loginModel = ModelAndView("login.html")
        if (error == null) {
            loginModel.addObject("loginError", true)
        }

        if (challenge != null) {
            val loginRequest = hydraService.getLoginRequest(challenge).body!!
            if (loginRequest.skip!!) {
                val redirectUrl = hydraService.acceptLoginRequest(challenge, AcceptLoginRequest(acr = null,
                        force_subject_identifier = null,
                        remember = null,
                        remember_for = null,
                        subject = loginRequest.subject)).body!!.redirect_to
                return ModelAndView("redirect:$redirectUrl")
            }
            loginModel.addObject("challenge", challenge)
        } else {
            loginModel.addObject("loginError", true)
        }

        return loginModel
    }

    @RequestMapping("/consent")
    fun consent(@RequestParam("consent_challenge") challenge: String?): ModelAndView {
        val consentModel = ModelAndView("consent.html")
        if (challenge != null) {
            val consentRequest = hydraService.getConsentRequest(challenge).body!!
            if (consentRequest.skip!!) {
                val redirectUri = hydraService.acceptConsentRequest(challenge,
                        AcceptConsentRequest(grant_access_token_audience = consentRequest.requested_access_token_audience,
                                grant_scope = consentRequest.requested_scope,
                                remember_for = null,
                                remember = null,
                                session = null)).body!!.redirect_to
                return ModelAndView("redirect:$redirectUri")
            }
            consentModel.addObject("model", ConsentModel(challenge = challenge,
                    requestedScope = consentRequest.requested_scope,
                    clientId = consentRequest.client?.client_id,
                    clientName = consentRequest.client?.client_name))
        } else {
            consentModel.addObject("error", true)
        }
        return consentModel
    }

    @PostMapping("/consent")
    fun authorizeConsent(@RequestParam("challenge") challenge: String, @RequestParam("submit") submit: ConsentSubmit): ModelAndView {
        when (submit) {
            ConsentSubmit.Authorize -> {
                val consentRequest = hydraService.getConsentRequest(challenge).body!!

                val redirectUri = hydraService.acceptConsentRequest(challenge, AcceptConsentRequest(grant_scope = consentRequest.requested_scope,
                        grant_access_token_audience = consentRequest.requested_access_token_audience,
                        remember = null,
                        remember_for = null,
                        session = null)).body!!.redirect_to

                return ModelAndView("redirect:$redirectUri")
            }
            ConsentSubmit.Deny -> {
                val redirectUri = hydraService.rejectConsentRequest(challenge, RejectRequest(error = "access_denied",
                        error_description = "The resource owner denied the request")).body!!.redirect_to
                return ModelAndView("redirect:$redirectUri")
            }
        }
    }
}

data class ConsentModel(val challenge: String,
                        val requestedScope: List<String>,
                        val clientId: String?,
                        val clientName: String?)

enum class ConsentSubmit {
    Authorize,
    Deny
}
