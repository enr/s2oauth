package grails.plugins.s2oauth

import org.scribe.model.Token
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Verb
import org.scribe.oauth.OAuthService

import grails.transaction.Transactional

@Transactional
class OauthResourceService {

    Map<String, OAuthService> clients = [:]

    protected Response get(String providerId, String url, Token accessToken) {
        OAuthService scribeService = oauthClient(providerId)
        OAuthRequest request = new OAuthRequest(Verb.GET, url);
        scribeService.signRequest(accessToken, request);
        Response response = request.send();
        return response
    }

    def registerProvider(S2oauthProviderService providerService) {
        String providerId = providerService.providerId()
        // TODO if already present: throw error!
        // TODO: invalid provider names (used in url mappings): account
        clients[providerId] = providerService.oauthClient()
    }

    private OAuthService oauthClient(String providerId) {
        OAuthService client = clients[providerId]
        if (client == null) {
            log.error "s2oauthProviderService: no client ${providerId}"
            throw new RuntimeException("No client ${providerId}")
        }
        return client
    }
}
