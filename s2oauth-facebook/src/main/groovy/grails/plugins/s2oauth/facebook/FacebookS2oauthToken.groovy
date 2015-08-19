package grails.plugins.s2oauth.facebook

import org.scribe.model.Token

import grails.plugins.s2oauth.S2oauthToken

/**
 * Spring Security authentication token for Facebook users. It's a standard {@link S2oauthToken}
 * that returns the Facebook name as the principal.
 *
 * @author <a href='mailto:cazacugmihai@gmail.com'>Mihai Cazacu</a>
 * @author <a href='mailto:enrico@comiti.name'>Enrico Comiti</a>
 * @author Thierry Nicola
 */
class FacebookS2oauthToken extends S2oauthToken {

    public static final String PROVIDER_NAME = 'facebook'

    String profileId

    FacebookS2oauthToken(Token accessToken, String profileId) {
        super(accessToken)
        this.profileId = profileId
        this.principal = profileId
    }

    String getSocialId() {
        return profileId
    }

    String getScreenName() {
        return profileId
    }

    String getProviderName() {
        return PROVIDER_NAME
    }

}
