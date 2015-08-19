package grails.plugins.s2oauth

import grails.test.mixin.TestFor
import spock.lang.Specification
import org.scribe.model.Token
import grails.plugin.springsecurity.userdetails.GrailsUser

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
@TestFor(S2oauthController)
class S2oauthControllerSpec extends Specification {

    def "authenticate should throw exception if request is missing provider"() {
        given:
            def token = Stub(Token) {
                getRawResponse() >> "a=1&b=2"
            }
            params.provider = provider
            def s2oauthService = [
                sessionKeyForRequestToken: { p -> 'no-such-key-in-session' },
                getRequestToken: { p -> token },
                getAuthorizationUrl: { p, t -> 'url' },
            ]
            controller.s2oauthService = s2oauthService
        when:
            controller.authenticate()
        then:
            thrown S2oauthException
        where:
            provider      |  _
            ''            |  _
            '  '          |  _
            null          |  _
    }

    def "onSuccess should throw exception if request is missing data"() {
        given:
            params.provider = provider
            def s2oauthService = [ sessionKeyForAccessToken:{ p -> 'no-such-key-in-session' } ]
            controller.s2oauthService = s2oauthService
        when:
            controller.onSuccess()
        then:
            thrown S2oauthException
        where:
            provider      |  _
            ''            |  _
            null          |  _
            'facebook'    |  _
    }

    // now askToLinkOrCreateAccountUri is hardcoded, so this test is redundant
    def "onSuccess should throw exception if askToLinkOrCreateAccountUri is not set"() {
        given:
            S2oauthToken authToken = Mock()
            params.provider = provider
            def providerkey = "${provider}_oauth_session_key"
            session[providerkey] = "${provider}_oauth_session_key"
            def s2oauthService = [
                sessionKeyForAccessToken:{ p -> providerkey },
                createAuthToken: { p, t -> authToken },
                getAskToLinkOrCreateAccountUri: { null }
            ]
            controller.s2oauthService = s2oauthService
        when:
            controller.onSuccess()
        then:
            thrown S2oauthException
        where:
            provider      |  _
            'facebook'    |  _
    }

    def "onSuccess should redirect to askToLinkOrCreateAccountUri if the user is not logged in"() {
        given:
            S2oauthToken authToken = Mock()
            params.provider = provider
            def providerkey = "${provider}_oauth_session_key"
            session[providerkey] = "${provider}aaa"
            def s2oauthService = [
                sessionKeyForAccessToken:{ p -> providerkey },
                createAuthToken: { p, t -> authToken },
                getAskToLinkOrCreateAccountUri: { "/askToLinkOrCreateAccountUri" }
            ]
            controller.s2oauthService = s2oauthService
        and:
            controller.onSuccess()
        expect:
            response.status == responseCode
            response.redirectedUrl == "/askToLinkOrCreateAccountUri"
        where:
            provider      |  responseCode
            'facebook'    |  302
    }

    def "onSuccess should redirect to defaultTargeturl if user is logged in"() {
        given:
            def token = Stub(Token) {
                getRawResponse() >> "a=1&b=2"
            }
            S2oauthToken authToken = new TestOAuthToken(token, false)
            params.provider = provider
            def providerkey = "${provider}_oauth_session_key"
            session[providerkey] = "${provider}aaa"
            def s2oauthService = [
                sessionKeyForAccessToken:{ p -> providerkey },
                createAuthToken: { p, t -> authToken },
                getAskToLinkOrCreateAccountUri: { "/askToLinkOrCreateAccountUri" }
            ]
            controller.s2oauthService = s2oauthService
        and:
            controller.onSuccess()
        expect:
            response.status == responseCode
            response.redirectedUrl == controller.getDefaultTargetUrl().uri
        where:
            provider      |  responseCode
            'facebook'    |  302
    }

    def "chooseAccount should return view if user is not logged in"() {
        given:
            controller.springSecurityService = [ isLoggedIn: { false } ]
        when:
            def mav = controller.chooseAccount()
        then:
            mav.viewName == '/s2oauth/chooseaccount'
    }
}

/**
 * A basic implementation for oauth token for a loggedin user.
 */
class TestOAuthToken extends S2oauthToken {
    TestOAuthToken(def token, def json) {
        super(token, json)
        this.principal = new GrailsUser("username", "password", true, true, true, true, [], 1L)
    }
    String getProviderName() {
        return "provider"
    }
    String getSocialId() {
        return "socialId"
    }
    String getScreenName() {
        return "screenName"
    }
}
