package grails.plugins.s2oauth

import org.scribe.model.Token
import org.scribe.model.Verifier
import org.scribe.oauth.OAuthService

public interface S2oauthProviderService {
    void init(S2oauthProviderConfiguration providerConfig);
    String providerId();
    OauthVersion supportedOauthVersion();
    OAuthService oauthClient();
    String getAuthorizationUrl(Token requestToken);
    Token getAccessToken(Token requestToken, Verifier verifier);
    S2oauthToken createAuthToken(Token accessToken);
}
