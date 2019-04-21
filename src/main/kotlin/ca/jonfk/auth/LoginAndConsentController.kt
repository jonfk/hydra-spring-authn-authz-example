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
        if (error != null) {
            loginModel.addObject("loginError", true)
        }

        // The challenge is used to fetch information about the login request from ORY Hydra.
        if (challenge != null) {
            val loginRequest = hydraService.getLoginRequest(challenge).body!!
            // If hydra was already able to authenticate the user, skip will be true and we do not need to re-authenticate
            if (loginRequest.skip!!) {
                // You can apply logic here, for example update the number of times the user logged in.
                // ...

                // Now it's time to grant the login request. You could also deny the request if something went terribly wrong
                // (e.g. your arch-enemy logging in...)
                val response = hydraService.acceptLoginRequest(challenge, AcceptLoginRequest(acr = null,
                        force_subject_identifier = null,
                        remember = null,
                        remember_for = null,
                        // All we need to do is to confirm that we indeed want to log in the user.
                        subject = loginRequest.subject)).body!!

                // All we need to do now is to redirect the user back to hydra!
                return ModelAndView("redirect:${response.redirect_to}")
            }
            // The challenge will be a hidden input field
            loginModel.addObject("challenge", challenge)
        } else {
            loginModel.addObject("loginError", true)
        }

        // If authentication can't be skipped we MUST show the login UI.
        return loginModel
    }

    @RequestMapping("/consent")
    fun consent(@RequestParam("consent_challenge") challenge: String?): ModelAndView {
        val consentModel = ModelAndView("consent.html")
        if (challenge != null) {
            // The challenge is used to fetch information about the consent request from ORY Hydra.
            val consentRequest = hydraService.getConsentRequest(challenge).body!!
            // If a user has granted this application the requested scope, hydra will tell us to not show the UI.
            if (consentRequest.skip!!) {
                // You can apply logic here, for example grant another scope, or do whatever...
                // ...

                // Now it's time to grant the consent request. You could also deny the request if something went terribly wrong
                val redirectUri = hydraService.acceptConsentRequest(challenge,
                        AcceptConsentRequest(
                                // ORY Hydra checks if requested audiences are allowed by the client, so we can simply echo this.
                                grant_access_token_audience = consentRequest.requested_access_token_audience,
                                // We can grant all scopes that have been requested - hydra already checked for us that no additional scopes
                                // are requested accidentally.
                                grant_scope = consentRequest.requested_scope,
                                remember_for = null,
                                remember = null,
                                // The session allows us to set session data for id and access tokens
                                // This data will be available when introspecting the token. Try to avoid sensitive information here,
                                // unless you limit who can introspect tokens.
                                // access_token: { foo: 'bar' },

                                // This data will be available in the ID token.
                                // id_token: { baz: 'bar' },
                                session = null)).body!!.redirect_to

                // All we need to do now is to redirect the user back to hydra!
                return ModelAndView("redirect:$redirectUri")
            }
            // If consent can't be skipped we MUST show the consent UI.
            consentModel.addObject("model", ConsentModel(
                    // The challenge will be a hidden input field
                    challenge = challenge,
                    // We have a bunch of data available from the response, check out the API docs to find what these values mean
                    // and what additional data you have available.
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
        // Let's see if the user decided to accept or reject the consent request..
        when (submit) {
            ConsentSubmit.Authorize -> {
                // Seems like the user authenticated! Let's tell hydra...
                val consentRequest = hydraService.getConsentRequest(challenge).body!!

                val redirectUri = hydraService.acceptConsentRequest(challenge, AcceptConsentRequest(
                        // We can grant all scopes that have been requested - hydra already checked for us that no additional scopes
                        // are requested accidentally.
                        grant_scope = consentRequest.requested_scope,
                        // ORY Hydra checks if requested audiences are allowed by the client, so we can simply echo this.
                        grant_access_token_audience = consentRequest.requested_access_token_audience,
                        // This tells hydra to remember this consent request and allow the same client to request the same
                        // scopes from the same user, without showing the UI, in the future.
                        remember = null,
                        // When this "remember" sesion expires, in seconds. Set this to 0 so it will never expire.
                        remember_for = null,
                        // The session allows us to set session data for id and access tokens
                        // This data will be available when introspecting the token. Try to avoid sensitive information here,
                        // unless you limit who can introspect tokens.
                        // access_token: { foo: 'bar' },

                        // This data will be available in the ID token.
                        // id_token: { baz: 'bar' },
                        session = null)).body!!.redirect_to

                // All we need to do now is to redirect the user back to hydra!
                return ModelAndView("redirect:$redirectUri")
            }
            ConsentSubmit.Deny -> {
                // Looks like the consent request was denied by the user
                val redirectUri = hydraService.rejectConsentRequest(challenge, RejectRequest(error = "access_denied",
                        error_description = "The resource owner denied the request")).body!!.redirect_to
                // All we need to do now is to redirect the browser back to hydra!
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
