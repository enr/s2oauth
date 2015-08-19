package grails.plugins.s2oauth.facebook

import grails.plugins.s2oauth.S2oauthAbstractScribeProviderService
import grails.plugins.s2oauth.S2oauthProviderConfiguration
import grails.plugins.s2oauth.S2oauthToken
import grails.plugins.s2oauth.OauthVersion
import grails.plugins.s2oauth.S2oauthException
import grails.transaction.Transactional
import grails.converters.JSON
import org.scribe.model.Token
import org.scribe.model.Verifier
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Verb
import org.scribe.oauth.OAuthService
import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.FacebookApi

@Transactional
class S2oauthFacebookService extends S2oauthAbstractScribeProviderService {

    private static String PROVIDER_ID = 'facebook'
    private static String PROFILE_URL = 'https://graph.facebook.com/me'

    private OAuthService scribeService

    def oauthResourceService

    void init(S2oauthProviderConfiguration providerConfig) {
        providerConfig.apiClass = FacebookApi.class
        scribeService = buildScribeService(providerConfig)
    }

    OAuthService oauthClient() {
        return scribeService
    }

    OauthVersion supportedOauthVersion() {
        return OauthVersion.TWO
    }

    /*
     * Oauth 2 don't use request token.
     * For a Oauth 1 provider use:
     * scribeService.getRequestToken()
     */
    Token getRequestToken() {
        return new Token('', '')
    }

    S2oauthToken createAuthToken(Token accessToken) {
        // Untyped to help unit tests
        def response = oauthResourceService.get(PROVIDER_ID, PROFILE_URL, accessToken)
        def user
        try {
            log.error "JSON response body= ${response.body}"
            user = JSON.parse(response.body)
        } catch (Exception e) {
            log.error "Error parsing response from Facebook. Response:\n${response.body}"
            throw new S2oauthException("Error parsing response from Facebook", e)
        }
        if (!user?.id) {
            log.error "No user id from Facebook. Response:\n${response.body}"
            throw new S2oauthException("No user id from Facebook")
        }
        return new FacebookS2oauthToken(accessToken, user.id)
    }

    String providerId() {
        return PROVIDER_ID
    }
}
