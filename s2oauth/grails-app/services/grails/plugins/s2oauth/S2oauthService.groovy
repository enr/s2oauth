package grails.plugins.s2oauth

import grails.transaction.Transactional

import grails.plugins.s2oauth.S2oauthProviderService
import grails.plugins.s2oauth.OauthVersion

import org.scribe.model.Token
import org.scribe.model.Verifier
import grails.web.servlet.mvc.GrailsParameterMap
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GormUserDetailsService
import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.authority.SimpleGrantedAuthority

@Transactional
class S2oauthService {

    Map<String, S2oauthProviderService> providers = [:]

    def securityConfig = SpringSecurityUtils.securityConfig

    def grailsApplication

    private S2oauthProviderService s2oauthProviderService(String providerId) {
        S2oauthProviderService providerService = providers[providerId]
        if (providerService == null) {
            log.error "s2oauthProviderService: no provider ${providerId}"
            throw new S2oauthException("No provider ${providerId}")
        }
        return providerService
    }

    String sessionKeyForRequestToken(String providerName) {
        return "s2oauth:request-t:${providerName}"
    }

    String sessionKeyForAccessToken(String providerName) {
        return "s2oauth:access-t:${providerName}"
    }

    Verifier extractVerifier(String providerId, GrailsParameterMap params) {
        S2oauthProviderService providerService = s2oauthProviderService(providerId)
        String verifierKey = OauthVersion.TWO == providerService.supportedOauthVersion() ? 'code' : 'oauth_verifier'
        if (!params[verifierKey]) {
            log.error("Cannot authenticate with oauth: Could not find oauth verifier in ${params}.")
            return null
        }
        String verification = params[verifierKey]
        log.warn "verifier found=${verification}"
        return new Verifier(verification)
    }

    Token getRequestToken(String providerId) {
        S2oauthProviderService providerService = s2oauthProviderService(providerId)
        return providerService.getRequestToken()
    }

    def getAuthorizationUrl(String providerId, Token requestToken) {
        log.warn "authenticate providers= ${providers}"
        S2oauthProviderService providerService = s2oauthProviderService(providerId)
        // ExternalIdentity externalIdentity = providerService.authenticate()
        // log.warn "user identity= ${externalIdentity.profileId}"
        return providerService.getAuthorizationUrl(requestToken)
    }

    def getAccessToken(String providerId, Token requestToken, Verifier verifier) {
        log.warn "getAccessToken requestToken=${requestToken} verifier=${verifier}"
        S2oauthProviderService providerService = s2oauthProviderService(providerId)
        return providerService.getAccessToken(requestToken, verifier)
    }

    def registerProvider(S2oauthProviderService providerService) {
        String providerId = providerService.providerId()
        println "Register ${providerId}"
        println log.name
        // TODO if already present: throw error!
        // TODO: invalid provider names (used in url mappings): account
        providers[providerId] = providerService
        def baseURL = grailsApplication.config.getProperty("grails.serverURL") ?: "http://localhost:${System.getProperty('server.port', '8080')}"
        def callbackUrl = "${baseURL}/oauth/${providerId}/callback"
        println "callback ${callbackUrl}"
        def apikey = System.getenv("${providerId.toUpperCase()}_API_KEY") ?: grailsApplication.config.getProperty("s2oauth.providers.${providerId}.api_key")
        def apisecret = System.getenv("${providerId.toUpperCase()}_API_SECRET") ?: grailsApplication.config.getProperty("s2oauth.providers.${providerId}.api_secret")
        println "apikey ${apikey} - apisecret ${apisecret}"
        S2oauthProviderConfiguration providerConfig = new S2oauthProviderConfiguration(
            apiKey : apikey,
            apiSecret : apisecret,
            callbackUrl : callbackUrl,
            debug : true
        )
        providerService.init(providerConfig)
    }

    S2oauthToken createAuthToken(String providerId, Token scribeToken) {
        log.warn "createAuthToken scribe token ${scribeToken}"
        S2oauthProviderService providerService = s2oauthProviderService(providerId)
        S2oauthToken oAuthToken = providerService.createAuthToken(scribeToken)
        log.warn "createAuthToken oAuthToken from provider service= ${oAuthToken}"
        // def OAuthID = lookupOAuthIdClass()
        // def oAuthID = OAuthID.findByProviderAndAccessToken(oAuthToken.providerName, oAuthToken.socialId)
        // if (oAuthID && oAuthID.user) {
        //  updateOAuthToken(oAuthToken, oAuthID.user)
        // }
        return oAuthToken
    }

    S2oauthToken updateOAuthToken(S2oauthToken oAuthToken, user) {
        // user
        String usernamePropertyName = securityConfig.userLookup.usernamePropertyName
        String passwordPropertyName = securityConfig.userLookup.passwordPropertyName
        String enabledPropertyName = securityConfig.userLookup.enabledPropertyName
        String accountExpiredPropertyName = securityConfig.userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = securityConfig.userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = securityConfig.userLookup.passwordExpiredPropertyName
        String username = user."${usernamePropertyName}"
        String password = user."${passwordPropertyName}"
        boolean enabled = enabledPropertyName ? user."${enabledPropertyName}" : true
        boolean accountExpired = accountExpiredPropertyName ? user."${accountExpiredPropertyName}" : false
        boolean accountLocked = accountLockedPropertyName ? user."${accountLockedPropertyName}" : false
        boolean passwordExpired = passwordExpiredPropertyName ? user."${passwordExpiredPropertyName}" : false
        // authorities
        String authoritiesPropertyName = securityConfig.userLookup.authoritiesPropertyName
        String authorityPropertyName = securityConfig.authority.nameField
        Collection<?> userAuthorities = user."${authoritiesPropertyName}"
        def authorities = userAuthorities.collect { new SimpleGrantedAuthority(it."${authorityPropertyName}") }
        oAuthToken.principal = new GrailsUser(username, password, enabled, !accountExpired, !passwordExpired,
        !accountLocked, authorities ?: [GormUserDetailsService.NO_ROLE], user.id)
        oAuthToken.authorities = authorities
        oAuthToken.authenticated = true
        return oAuthToken
    }

    def userNamed(String username) {
        if (!username) {
            return null
        }
        def User = lookupUserClass()
        String usernameFieldName = securityConfig.userLookup.usernamePropertyName
        def user = User.findWhere((usernameFieldName): username)
        return user
    }

    /**
    * Returns if a user with the given username exists in database.
    */
    boolean existUserNamed(String username) {
        def user = userNamed(username)
        return (user != null)
    }

    def createUser(command, S2oauthToken oAuthToken) {
        def User = lookupUserClass()
        def user = User.newInstance()
        //User user = new User(username: command.username, password: command.password1, enabled: true)
        String usernameFieldName = securityConfig.userLookup.usernamePropertyName
        user."${usernameFieldName}" = command.username
        user.password = command.password1
        user.enabled = true
        //user.addToOAuthIDs(provider: oAuthToken.providerName, accessToken: oAuthToken.socialId, user: user)
        // updateUser(user, oAuthToken)
        if (!user.validate() || !user.save()) {
            println "createUser: no valid, no save... return NULL"
            return null
        }
        println "user ${user}"
        def UserRole = lookupUserRoleClass()
        println "user role ${UserRole}"
        def Role = lookupRoleClass()
        println "Role ${Role}"
        def roles = getRoleNames()
        println "roles ${roles}"
        for (roleName in roles) {
            def r = Role.findByAuthority(roleName)
            log.warn " > role ${r}"
            if (r != null) {
                UserRole.create user, r
            }
        }
        oAuthToken = updateOAuthToken(oAuthToken, user)
        println "returning user ${user}"
        return user
    }

    def getAskToLinkOrCreateAccountUri() {
        def askToLinkOrCreateAccountUri = '/oauth/account/choose'
        //grailsApplication.config.grails.plugin.springsecurity.oauth.registration.askToLinkOrCreateAccountUri ?: '/oauth/askToLinkOrCreateAccount'
        return askToLinkOrCreateAccountUri
    }

    def getRoleNames() {
        def roleNames = grailsApplication.config.grails.plugin.springsecurity.oauth.registration.roleNames ?: ['ROLE_USER']
        return roleNames
    }

    protected String lookupUserClassName() {
        securityConfig.userLookup.userDomainClassName
    }

    protected Class<?> lookupUserClass() {
        grailsApplication.getDomainClass(lookupUserClassName()).clazz
    }

    protected String lookupRoleClassName() {
        securityConfig.authority.className
    }

    protected Class<?> lookupRoleClass() {
        grailsApplication.getDomainClass(lookupRoleClassName()).clazz
    }

    protected String lookupUserRoleClassName() {
        securityConfig.userLookup.authorityJoinClassName
    }

    protected Class<?> lookupUserRoleClass() {
        grailsApplication.getDomainClass(lookupUserRoleClassName()).clazz
    }

    // protected String lookupOAuthIdClassName() {
    //  def domainClass = grailsApplication.config.grails.plugin.springsecurity.oauth.domainClass ?: 'OAuthID'
    //  return domainClass
    // }
    //
    // protected Class<?> lookupOAuthIdClass() {
    //  grailsApplication.getDomainClass(lookupOAuthIdClassName()).clazz
    // }
}
