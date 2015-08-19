package grails.plugins.s2oauth

import grails.test.mixin.*
import spock.lang.Specification
import spock.lang.*
import org.scribe.model.Token

/**
 * See the API for {@link grails.test.mixin.web.GroovyPageUnitTestMixin} for usage instructions
 */
@TestFor(S2oauthTagLib)
class S2oauthTagLibSpec extends Specification {

    def springSecurityService

    def s2oauthService

    def setup() {
        springSecurityService = [:]
        s2oauthService = [:]
    }

    def "ifLoggedInWith should print body if session is valid"() {
        given:
            def sessionKey = "oas:${provider}"
            session[sessionKey] = new Token("${provider}_token", "${provider}_secret", "${provider}_rawResponse=rawResponse")
            springSecurityService.isLoggedIn = { ->
                true
            }
            s2oauthService.sessionKeyForAccessToken = { providerName ->
                sessionKey
            }
            tagLib.springSecurityService = springSecurityService
            tagLib.s2oauthService = s2oauthService
            def template = "<s2oauth:ifLoggedInWith provider=\"${provider}\">Logged in using ${provider}</s2oauth:ifLoggedInWith>"
        and:
            def renderedContent = applyTemplate(template)
        expect:
            renderedContent == "Logged in using ${provider}"
        where:
            provider    | _
            'facebook'  | _
            'google'    | _
            'linkedin'  | _
            'twitter'   | _
    }

    def "ifLoggedInWith should print empty string if session is invalid"() {
        given:
            def sessionKey = "oas:${provider}"
            session[sessionKey] = token
            springSecurityService.isLoggedIn = { ->
                true
            }
            s2oauthService.sessionKeyForAccessToken = { providerName ->
                sessionKey
            }
            tagLib.springSecurityService = springSecurityService
            tagLib.s2oauthService = s2oauthService
            def template = "<s2oauth:ifLoggedInWith provider=\"${provider}\">Logged in using ${provider}</s2oauth:ifLoggedInWith>"
        and:
            def renderedContent = applyTemplate(template)
        expect:
            renderedContent == ""
        where:
            provider    | token
            'facebook'  | null
            'google'    | ""
            'linkedin'  | "a_token_string"
    }

    def "ifLoggedInWith should print empty string if user is not logged in"() {
        given:
            springSecurityService.isLoggedIn = { ->
                return false
            }
            tagLib.springSecurityService = springSecurityService
            def template = '<s2oauth:ifLoggedInWith provider="unknown">Logged in using unknown provider</s2oauth:ifLoggedInWith>'
        when:
            def renderedContent = applyTemplate(template)
        then:
            renderedContent == ''
    }

    def "ifNotLoggedInWith should print body if user is not logged in"() {
        given:
            def message = "Not_Logged_In"
            springSecurityService.isLoggedIn = { ->
                return false
            }
            tagLib.springSecurityService = springSecurityService
            def template = "<s2oauth:ifNotLoggedInWith provider=\"facebook\">${message}</s2oauth:ifNotLoggedInWith>"
        when:
            def renderedContent = applyTemplate(template)
        then:
            renderedContent == message
    }
}
