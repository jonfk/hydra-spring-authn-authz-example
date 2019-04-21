package ca.jonfk.auth.hydra

import ca.jonfk.auth.hydra.response.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriTemplate

@Service
class HydraService(val hydraAdminRestOperations: RestOperations) {
    /**
     * @see <a href="https://www.ory.sh/docs/hydra/sdk/api#get-an-login-request" target="_top">Get a login request</a>
     */
    fun getLoginRequest(challenge: String): ResponseEntity<LoginRequest> {
        val uri = UriComponentsBuilder.fromUriString("/oauth2/auth/requests/login/{challenge}").buildAndExpand(challenge)
        return hydraAdminRestOperations.getForEntity(uri.toUriString(), LoginRequest::class.java)
    }

    /**
     * @see <a href="https://www.ory.sh/docs/hydra/sdk/api#accept-an-login-request" target="_top">Accept a login request</a>
     */
    fun acceptLoginRequest(challenge: String, acceptLoginRequest: AcceptLoginRequest): ResponseEntity<CompletedRequest> {
        val uri = UriComponentsBuilder.fromUriString("/oauth2/auth/requests/login/{challenge}/accept").buildAndExpand(challenge)
        return hydraAdminRestOperations.exchange(uri.toUriString(), HttpMethod.PUT, HttpEntity(acceptLoginRequest), CompletedRequest::class.java)
    }

    /**
     * @see <a href="https://www.ory.sh/docs/hydra/sdk/api#reject-a-login-request" target="_top">Reject a login request</a>
     */
    fun rejectLoginRequest(challenge: String, rejectRequest: RejectRequest): ResponseEntity<CompletedRequest> {
        val uri = UriComponentsBuilder.fromUriString("/oauth2/auth/requests/login/{challenge}/reject").buildAndExpand(challenge)
        return hydraAdminRestOperations.exchange(uri.toUriString(), HttpMethod.PUT, HttpEntity(rejectRequest), CompletedRequest::class.java)
    }

    /**
     * @see <a href="https://www.ory.sh/docs/hydra/sdk/api#get-consent-request-information" target="_top">Get consent request information</a>
     */
    fun getConsentRequest(challenge: String): ResponseEntity<ConsentRequest> {
        val uri = UriComponentsBuilder.fromUriString("/oauth2/auth/requests/consent/{challenge}").buildAndExpand(challenge)
        return hydraAdminRestOperations.getForEntity(uri.toUriString(), ConsentRequest::class.java)
    }

    /**
     * @see <a href="https://www.ory.sh/docs/hydra/sdk/api#accept-an-consent-request" target="_top">Accept a consent request</a>
     */
    fun acceptConsentRequest(challenge: String, acceptConsentRequest: AcceptConsentRequest): ResponseEntity<CompletedRequest> {
        val uri = UriComponentsBuilder.fromUriString("/oauth2/auth/requests/consent/{challenge}/accept").buildAndExpand(challenge)
        return hydraAdminRestOperations.exchange(uri.toUriString(), HttpMethod.PUT, HttpEntity(acceptConsentRequest), CompletedRequest::class.java)
    }

    /**
     * @see <a href="https://www.ory.sh/docs/hydra/sdk/api#reject-an-consent-request" target="_top">Reject a consent request</a>
     */
    fun rejectConsentRequest(challenge: String, rejectRequest: RejectRequest): ResponseEntity<CompletedRequest> {
        val uri = UriComponentsBuilder.fromUriString("/oauth2/auth/requests/consent/{challenge}/reject").buildAndExpand(challenge)
        return hydraAdminRestOperations.exchange(uri.toUriString(), HttpMethod.PUT, HttpEntity(rejectRequest), CompletedRequest::class.java)
    }
}

@Configuration
class HydraConfiguration(@Value("\${hydra.baseUri}") val hydraBaseUri: String,
                         @Value("\${hydra.admin.baseUri}") val hydraAdminBaseUri: String) {

    @Bean
    fun hydraRestOperations(): RestOperations {
        val restTemplate = RestTemplate()
        val factory = DefaultUriBuilderFactory(hydraBaseUri)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY
        restTemplate.uriTemplateHandler = factory
        return restTemplate
    }

    @Bean
    fun hydraAdminRestOperations(): RestOperations {
        val restTemplate = RestTemplate()
        val factory = DefaultUriBuilderFactory(hydraAdminBaseUri)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY
        restTemplate.uriTemplateHandler = factory
        return restTemplate
    }
}
