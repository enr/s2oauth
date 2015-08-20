package grails.plugins.s2oauth

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.servlet.ModelAndView
import org.scribe.model.Token
import org.scribe.model.Verifier
import org.apache.commons.lang.StringUtils

class S2oauthController {

public static final String SPRING_SECURITY_OAUTH_TOKEN = 'springSecurityOAuthToken'

    def s2oauthService
    def springSecurityService
    def authenticationManager
    def rememberMeServices

    def authenticate() {
        String providerName = params.provider
        if (StringUtils.isBlank(providerName)) {
            throw new S2oauthException("Error no provider")
        }
        log.warn "authenticate ${providerName}"
        Token requestToken = s2oauthService.getRequestToken(providerName)
        log.warn "requestToken from s2oauthservice=${requestToken}"
        session[s2oauthService.sessionKeyForRequestToken(providerName)] = requestToken
        String url = s2oauthService.getAuthorizationUrl(providerName, requestToken)
        log.warn "redirect url from s2oauthservice=${url}"
        // if url null or invalid...
        //RedirectHolder.setUri(params.redirectUrl)
        return redirect(url: url)
    }

    def callback() {
        String providerName = params.provider
        log.warn "callback ${providerName}"
        Verifier verifier = s2oauthService.extractVerifier(providerName, params)
        if (!verifier) {
            redirect(uri: "http://localhost:8080/oauth/${providerName}/failure")
            return
        }
        //Token requestToken = (Token) session[sessionKeyForRequestToken(providerName)]
        Token requestToken = new Token(params?.code, "")
        log.warn "in callback, requestToken found = ${requestToken}"
        // Token requestToken = provider.oauthVersion == SupportedOauthVersion.TWO ?
        //     new Token(params?.code, "") :
        //     (Token) session[oauthService.findSessionKeyForRequestToken(providerName)]
        if (!requestToken) {
            throw new S2oauthException("Error retrieving request token provider=${providerName}")
        }
        Token accessToken
        try {
            accessToken = s2oauthService.getAccessToken(providerName, requestToken, verifier)
        } catch (Exception e) {
            log.error("Cannot authenticate with oauth")
            log.error(e)
            return redirect(uri: "http://localhost:8080/oauth/${providerName}/failure")
        }
        session[s2oauthService.sessionKeyForAccessToken(providerName)] = accessToken
        session.removeAttribute(s2oauthService.sessionKeyForRequestToken(providerName))
        return redirect(uri: "http://localhost:8080/oauth/${providerName}/success")
    }

    def onSuccess() {
        String providerName = params.provider
        log.warn "onsuccess ${providerName}"
        // Validate the 'provider' URL. Any errors here are either misconfiguration
        // or web crawlers (or malicious users).
        if (!providerName) {
            log.warn "The Spring Security OAuth callback URL must include the 'provider' URL parameter"
            throw new S2oauthException("The Spring Security OAuth callback URL must include the 'provider' URL parameter")
        }
        def sessionKey = s2oauthService.sessionKeyForAccessToken(providerName)
        if (!session[sessionKey]) {
            log.warn "No OAuth token in the session for provider '${providerName}'"
            throw new S2oauthException("Authentication error for provider '${providerName}'")
        }
        // Create the relevant authentication token and attempt to log in.
        log.warn " >>>>>> ${providerName} s2oauthService.createAuthToken"
        S2oauthToken oAuthToken = s2oauthService.createAuthToken(providerName, session[sessionKey])
        log.warn " <<<<<< ${providerName} s2oauthService.createAuthToken"
        if (oAuthToken.principal instanceof GrailsUser) {
            authenticateAndRedirect(oAuthToken, getDefaultTargetUrl())
        } else {
            // This OAuth account hasn't been registered against an internal
            // account yet. Give the oAuthID the opportunity to create a new
            // internal account or link to an existing one.
            session[SPRING_SECURITY_OAUTH_TOKEN] = oAuthToken
            def redirectUrl = s2oauthService.getAskToLinkOrCreateAccountUri()
            if (!redirectUrl) {
                log.warn "grails.plugin.springsecurity.oauth.registration.askToLinkOrCreateAccountUri configuration option must be set"
                throw new S2oauthException('Internal error')
            }
            log.debug "Redirecting to askToLinkOrCreateAccountUri: ${redirectUrl}"
            redirect(redirectUrl instanceof Map ? redirectUrl : [uri: redirectUrl])
        }
    }

    def onFailure(String provider) {
        String providerName = params.provider
        log.warn "onfail ${providerName}"
        flash.default = "Error authenticating with ${providerName}"
        log.warn "Error authenticating with external provider ${providerName}"
        render 'onfailure'
    }

    def chooseAccount() {
        log.warn "now chooseaccount"
        if (springSecurityService.isLoggedIn()) {
            def currentUser = springSecurityService.getCurrentUser()
            S2oauthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
            if (!oAuthToken) {
                log.warn "askToLinkOrCreateAccount: OAuthToken not found in session"
                throw new S2oauthException('Authentication error')
            }
            //currentUser.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: currentUser)
            if (currentUser.validate() && currentUser.save()) {
                oAuthToken = s2oauthService.updateOAuthToken(oAuthToken, currentUser)
                authenticateAndRedirect(oAuthToken, getDefaultTargetUrl())
                return
            }
        }
        return new ModelAndView("/s2oauth/chooseaccount", [rememberMeParameter:getRememberMeParameterName()])
    }

    def linkAccount(OAuthLinkAccountCommand command) {
        log.warn "linkaccount ${command}"
        S2oauthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
        if (!oAuthToken) {
            log.warn "linkAccount: OAuthToken not found in session"
            throw new S2oauthException('Authentication error')
        }
        if (request.post) {
            if (!authenticationIsValid(command.username, command.password)) {
                log.info "Authentication error for use ${command.username}"
                command.errors.rejectValue("username", "OAuthLinkAccountCommand.authentication.error")
                render view: 'chooseaccount', model: [linkAccountCommand: command]
                return
            }
            def commandValid = command.validate()
            def user = s2oauthService.userNamed(command.username)
            boolean userExists = (user != null)
            if (!userExists) {
                command.errors.rejectValue("username", "OAuthLinkAccountCommand.username.not.exists")
            }
            boolean linked = commandValid && userExists
            if (linked) {
                oAuthToken = s2oauthService.updateOAuthToken(oAuthToken, user)
                authenticateAndRedirect(oAuthToken, getDefaultTargetUrl())
                return
            }
        }
        render view: 'chooseaccount', model: [linkAccountCommand: command]
    }

    def createAccount(OAuthCreateAccountCommand command) {
        log.warn "createaccount ${command}"
        S2oauthToken oAuthToken = session[SPRING_SECURITY_OAUTH_TOKEN]
        if (!oAuthToken) {
            log.warn "createAccount: OAuthToken not found in session"
            throw new S2oauthException('Authentication error')
        }
        if (request.post) {
            if (!springSecurityService.loggedIn) {
                def config = SpringSecurityUtils.securityConfig
                def commandValid = command.validate()
                boolean created = (commandValid && (s2oauthService.createUser(command, oAuthToken) != null))
                if (created) {
                    authenticateAndRedirect(oAuthToken, getDefaultTargetUrl())
                    return
                }
            }
        }
        render view: 'chooseaccount', model: [createAccountCommand: command]
    }

    private String getRememberMeParameterName() {
        def conf = SpringSecurityUtils.securityConfig
        return conf.rememberMe.parameter
    }

    private boolean authenticationIsValid(String username, String password) {
        boolean valid = true
        try {
            authenticationManager.authenticate new UsernamePasswordAuthenticationToken(username, password)
        } catch (AuthenticationException e) {
            valid = false
        }
        return valid
    }

    protected Map getDefaultTargetUrl() {
        def config = SpringSecurityUtils.securityConfig
        def savedRequest = SpringSecurityUtils.getSavedRequest(session)
        def defaultUrlOnNull = '/'
        if (savedRequest && !config.successHandler.alwaysUseDefault) {
            return [url: (savedRequest.redirectUrl ?: defaultUrlOnNull)]
        }
        return [uri: (config.successHandler.defaultTargetUrl ?: defaultUrlOnNull)]
    }

    protected void authenticateAndRedirect(S2oauthToken oAuthToken, redirectUrl) {
        session.removeAttribute SPRING_SECURITY_OAUTH_TOKEN
        SecurityContextHolder.context.authentication = oAuthToken
        String rememberMeParameterName = getRememberMeParameterName()
        if ((oAuthToken) && rememberMeParameterName && params[rememberMeParameterName]) {
            rememberMeServices.loginSuccess(request, response, SecurityContextHolder.context.authentication)
        }
        redirect(redirectUrl instanceof Map ? redirectUrl : [uri: redirectUrl])
    }

}

class OAuthCreateAccountCommand {

    def s2oauthService

    String username
    String password1
    String password2

    static constraints = {
        username blank: false, minSize: 3, validator: { String username, command ->
            if (command.s2oauthService.existUserNamed(username)) {
                 return 'OAuthCreateAccountCommand.username.error.unique'
            }
        }
        password1 blank: false, minSize: 8, maxSize: 64, validator: { password1, command ->
            if (command.username && command.username == password1) {
                return 'OAuthCreateAccountCommand.password.error.username'
            }
            if (password1 && password1.length() >= 8 && password1.length() <= 64 &&
                    (!password1.matches('^.*\\p{Alpha}.*$') ||
                     !password1.matches('^.*\\p{Digit}.*$') ||
                     !password1.matches('^.*[!@#$%^&].*$'))) {
                return 'OAuthCreateAccountCommand.password.error.strength'
            }
        }
        password2 nullable: true, blank: true, validator: { password2, command ->
            if (command.password1 != password2) {
                return 'OAuthCreateAccountCommand.password.error.mismatch'
            }
        }
    }
}

class OAuthLinkAccountCommand {

    String username
    String password

    static constraints = {
        username blank: false
        password blank: false
    }
}
