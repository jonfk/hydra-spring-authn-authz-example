package ca.jonfk.auth

import ca.jonfk.auth.hydra.HydraService
import ca.jonfk.auth.hydra.response.AcceptLoginRequest
import ca.jonfk.auth.hydra.response.RejectRequest
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@Configuration
@EnableWebSecurity
class SecurityConfiguration(val hydraService: HydraService) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth!!.inMemoryAuthentication()
                .withUser("user1").password(passwordEncoder().encode("user1Pass")).roles("USER")
                .and()
                .withUser("user2").password(passwordEncoder().encode("user2Pass")).roles("USER")
                .and()
                .withUser("admin").password(passwordEncoder().encode("adminPass")).roles("ADMIN")
    }

    public override fun configure(http: HttpSecurity) {
        http
                .csrf().requireCsrfProtectionMatcher(EndpointRequest.to("login", "consent"))
                .and()
                .formLogin()
                .loginPage("/login")
                .successHandler(hydraAuthenticationSuccessHandler())
                .failureHandler(AuthenticationFailureHandlerImpl())
                .permitAll()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun hydraAuthenticationSuccessHandler(): AuthenticationSuccessHandler {
        return HydraAuthenticationSuccessHandler(hydraService)
    }
}

class HydraAuthenticationSuccessHandler(private val hydraService: HydraService) : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(request: HttpServletRequest?, response: HttpServletResponse?, authentication: Authentication) {
        val principal = authentication.principal
        val challenge = request?.getParameter("challenge")!!

        // Seems like the user authenticated! Let's tell hydra...
        if (principal is User) {

            val redirect = hydraService.acceptLoginRequest(challenge, AcceptLoginRequest(
                    // Sets which "level" (e.g. 2-factor authentication) of authentication the user has. The value is really arbitrary
                    // and optional. In the context of OpenID Connect, a value of 0 indicates the lowest authorization level.
                    acr = null,
                    force_subject_identifier = null,

                    // This tells hydra to remember the browser and automatically authenticate the user in future requests. This will
                    // set the "skip" parameter in the other route to true on subsequent requests!
                    remember = null,
                    // When the session expires, in seconds. Set this to 0 so it will never expire.
                    remember_for = null,
                    // Subject is an alias for user ID. A subject can be a random string, a UUID, an email address, ....
                    subject = principal.username)).body!!.redirect_to

            // All we need to do now is to redirect the user back to hydra!
            response?.sendRedirect(redirect)
        }
        // You could also deny the login request which tells hydra that no one authenticated!
//        val hydraResponse = hydraService.rejectLoginRequest(challenge = challenge, rejectRequest = RejectRequest(error = "invalid_request", error_description = "The user did something stupid", error_debug =  null, error_hint = null))
//        response?.sendRedirect("redirect:${hydraResponse.body!!.redirect_to}")
    }

}

class AuthenticationFailureHandlerImpl : AuthenticationFailureHandler {
    private val LOGIN_FAILURE_URL = "/login?error"

    override fun onAuthenticationFailure(request: HttpServletRequest?, response: HttpServletResponse?, exception: AuthenticationException?) {
        // Logic could be added to limit number of retries and fail the login request
        val challenge = request?.getParameter("challenge")!!
        response?.sendRedirect("$LOGIN_FAILURE_URL&login_challenge=$challenge")
    }

}
