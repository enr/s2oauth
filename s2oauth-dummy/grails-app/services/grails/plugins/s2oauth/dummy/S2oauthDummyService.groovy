package grails.plugins.s2oauth.dummy

import grails.transaction.Transactional
import grails.plugins.s2oauth.S2oauthProviderService
import grails.plugins.s2oauth.S2oauthProviderConfiguration
import grails.plugins.s2oauth.S2oauthToken
import grails.plugins.s2oauth.OauthVersion
import org.scribe.model.Token
import org.scribe.model.Verifier
import org.scribe.oauth.OAuthService

@Transactional
class S2oauthDummyService implements S2oauthProviderService {

    private static final String DUMMY_KEY = 'AQBWn_P-thcErJFU4HIugHIBIoATT29UjyqexwdBHEQtn3iQsAn53lqcz1zn63OdT6kz1qTcw2l2SPJGIaVKfRth2nhKfddoZUFPeDhNRpfKBlrQB_659-D959gLLp1nqRRhZIHx7wRKKZtuwVGWT11i6JfDarYs6vDoUJwdbmLDc01VlRyMiSwlNz-ixcKGilv6swbHjIB1qpusWv5RXtwVjJsuOSFgqy5Y_NJ0tPcukTCAreXO15_mkcuBMeSlAv_cScN06OD0YD89Q2Ta6_sDSVM_hfjnkImI-XieRr5jmmSvEb4PE2UNAX4A59vumZc'
    private static final String DUMMY_RAW_RESPONSE = 'access_token=166942940015970|2.2ltzWXYNDjCtg5ZDVVJJeg__.3600.1295816400-548517159|RsXNdKrpxg8L6QNLWcs2TVTmcaE&expires=5108'

    void init(S2oauthProviderConfiguration providerConfig) {
    }

    OAuthService oauthClient() {
        return null
    }

    OauthVersion supportedOauthVersion() {
        return OauthVersion.TWO
    }

    Token getRequestToken() {
        return new Token(DUMMY_KEY, '')
    }

    String getAuthorizationUrl(Token requestToken) {
        return 'http://localhost:8080/oauth/dummy/callback?code=dummycode'
    }

    Token getAccessToken(Token requestToken, Verifier verifier) {
        return new Token(DUMMY_KEY, '')
    }

    S2oauthToken createAuthToken(Token accessToken) {
        Token token = new Token(DUMMY_KEY, '', DUMMY_RAW_RESPONSE)
        return new DummyS2oauthToken(token, 'user.id')
    }

    String providerId() {
        return "dummy"
    }
}
