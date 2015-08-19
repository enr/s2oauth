package grails.plugins.s2oauth

import org.scribe.model.Token
import org.scribe.model.Verifier
import grails.plugins.s2oauth.OauthVersion
import grails.plugins.s2oauth.S2oauthProviderConfiguration
import org.scribe.oauth.OAuthService
import org.scribe.builder.ServiceBuilder
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Verb

public abstract class S2oauthAbstractScribeProviderService implements S2oauthProviderService {

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return scribeService.getAuthorizationUrl(requestToken)
    }

    @Override
    public Token getAccessToken(Token requestToken, Verifier verifier) {
        log.warn "getAccessToken requestToken=${requestToken} verifier=${verifier}"
        return scribeService.getAccessToken(requestToken, verifier)
    }

    protected OAuthService buildScribeService(S2oauthProviderConfiguration providerConfig) {
        ServiceBuilder serviceBuilder = new ServiceBuilder()
            .provider(providerConfig.apiClass)
            .apiKey(providerConfig.apiKey)
            .apiSecret(providerConfig.apiSecret)
        if (providerConfig.callbackUrl) {
            serviceBuilder.callback(providerConfig.callbackUrl)
        }
        if (providerConfig.signatureType) {
            serviceBuilder.signatureType(providerConfig.signatureType)
        }
        if (providerConfig.scope) {
            serviceBuilder.scope(providerConfig.scope)
        }
        if (providerConfig.debug) {
            serviceBuilder.debug()
        }
        return serviceBuilder.build();
    }
}
