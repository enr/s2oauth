package grails.plugins.s2oauth

import grails.converters.JSON

import org.scribe.model.Token
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

/**
 * This is a Spring Security authentication token for OAuth providers.
 * It will be saved in SecurityContextHolder.context.authentication when user
 * complete the authentication process.
 * It must be initialised with a
 * {@link Scribe https://github.com/fernandezpablo85/scribe-java} access token
 * from which dedicated provider.
 * Tokens can extract extra information such as the
 * principal.
 *
 * @author <a href='mailto:cazacugmihai@gmail.com'>Mihai Cazacu</a>
 */
abstract class S2oauthToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1

    protected Token accessToken
    protected Map tokenParams
    protected Object principal
    Collection<GrantedAuthority> authorities

    /**
     * Initialises the token from an access token.
     */
    S2oauthToken(Token accessToken, boolean forceJsonExtractor = false) {
        super(Collections.EMPTY_LIST)
        this.accessToken = accessToken
        this.tokenParams = extractParameters(accessToken.rawResponse,
            forceJsonExtractor ?: accessToken.rawResponse?.trim()?.startsWith('{'))
    }

    Object getPrincipal() {
        return principal
    }

    /**
     * Returns the raw response from the OAuth provider as a string.
     */
    def getCredentials() {
        return accessToken.rawResponse
    }

    /**
     * Returns the name of the OAuth provider for this token.
     */
    abstract String getProviderName()

    abstract String getSocialId()

    abstract String getScreenName()

    /**
     * Returns the parameters in the OAuth access token as a map.
     */
    protected final Map getParameters() { return Collections.unmodifiableMap(tokenParams) }

    private Map extractParameters(String data, boolean json) {
        if (json) {
          return JSON.parse(data)
        }
        return data?.split('&')?.collectEntries { it.split('=') as List }
    }
}
