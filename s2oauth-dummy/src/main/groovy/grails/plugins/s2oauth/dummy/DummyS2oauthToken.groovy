package grails.plugins.s2oauth.dummy

import org.scribe.model.Token

import grails.plugins.s2oauth.S2oauthToken

class DummyS2oauthToken extends S2oauthToken {

   public static final String PROVIDER_NAME = 'dummy'

   String profileId

   DummyS2oauthToken(Token accessToken, String profileId) {
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
