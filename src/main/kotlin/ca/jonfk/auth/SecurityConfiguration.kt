package ca.jonfk.auth

import ca.jonfk.auth.hydra.HydraService
import ca.jonfk.auth.hydra.response.AcceptLoginRequest
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
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
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
        if (principal is User) {
            val challenge = request?.getParameter("challenge")!!

            val redirect = hydraService.acceptLoginRequest(challenge, AcceptLoginRequest(
                    acr = null,
                    force_subject_identifier = null,
                    remember = null,
                    remember_for = null,
                    subject = principal.username)).body!!.redirect_to
            response?.sendRedirect(redirect)
        }
    }

}

