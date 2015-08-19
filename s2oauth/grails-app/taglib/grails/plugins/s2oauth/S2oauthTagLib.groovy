package grails.plugins.s2oauth

import org.scribe.model.Token
import org.grails.taglib.GrailsTagException

/**
 * Main taglib s2oauth:ifLoggedInWith s2oauth:ifNotLoggedInWith.
 */
class S2oauthTagLib {

    static namespace = 's2oauth'

    def s2oauthService
    def springSecurityService

    /**
     * Creates a link to connect to the give provider.
     */
    def connect = { attrs, body ->
        String provider = attrs.provider
        if (!provider) {
            throw new GrailsTagException('No provider specified for <oauth:connect /> tag. Try <oauth:connect provider="your-provider-name" />')
        }
        Map a = attrs + [url: [controller: 's2oauth', action: 'authenticate', params: [provider: provider]]]
        out << g.link(a, body)
    }

    /**
     * Renders the body if the user is authenticated with the given provider.
     */
    def ifLoggedInWith = { attrs, body ->
        def provider = attrs.provider
        if (currentUserIsLoggedInWithProvider(provider)) {
            out << body()
        }
    }

    /**
     * Renders the body if the user is not authenticated with the given provider.
     */
    def ifNotLoggedInWith = { attrs, body ->
        def provider = attrs.provider
        if (!currentUserIsLoggedInWithProvider(provider)) {
            out << body()
        }
    }

    private boolean currentUserIsLoggedInWithProvider(String provider) {
        if (!provider || !springSecurityService.isLoggedIn()) {
            return false
        }
        def sessionKey = s2oauthService.sessionKeyForAccessToken(provider)
        return (session[sessionKey] instanceof Token)
    }
}
