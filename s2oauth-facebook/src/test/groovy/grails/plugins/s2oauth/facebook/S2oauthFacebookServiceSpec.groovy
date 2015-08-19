package grails.plugins.s2oauth.facebook

import grails.test.mixin.TestFor
import spock.lang.*
import org.scribe.model.Token
import grails.plugins.s2oauth.S2oauthException
import grails.plugins.s2oauth.OauthVersion

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(S2oauthFacebookService)
class S2oauthFacebookServiceSpec extends Specification {

    //S2oauthFacebookService service

    def oauthResourceService

    def setup() {
        //service = new S2oauthFacebookService()
        oauthResourceService = [:]
    }
    def "should return the correct OAuth version"() {
        given:
            OauthVersion version
        when:
            version = service.supportedOauthVersion()
        then:
            version == OauthVersion.TWO
    }

    def "should throw S2oauthException for unexpected response"() {
        given:
            def exception = null
            def oauthAccessToken = new Token('token', 'secret', 'rawResponse=rawResponse')
            def response = [body: responseBody]
            oauthResourceService.get = { providerId, accessToken, url ->
                return response
            }
            service.oauthResourceService = oauthResourceService
        and:
            try {
                def token = service.createAuthToken(oauthAccessToken)
            } catch (Throwable throwable) {
                exception = throwable
            }
        expect:
            exception instanceof S2oauthException
        where:
            responseBody      |  _
            ''                |  _
            null              |  _
            '{}'              |  _
            '{"test"="test"}' |  _
    }

    def "should return the correct OAuth token"() {
        given:
            def responseBody = '''{"id":"123123123",
"name":"My Name","first_name":"My","last_name":"Name","link":"http:\\/\\/www.facebook.com\\/my.name","username":"my.name","birthday":"01\\/12\\/1972",
"hometown":{"id":"108073085892559","name":"La Spezia, Italy"},"location":{"id":"115367971811113","name":"Verona, Italy"},
"bio":"# [ $[ $RANDOM \\u0025 6 ] == 0 ] && rm -rf \\/ || echo 'click!'",
"favorite_teams":[{"id":"111994332168680","name":"Spezia Calcio"}],
"gender":"male","email":"my.name\\u0040gmail.com","timezone":1,"locale":"en_US","verified":true,"updated_time":"2012-08-16T12:33:51+0000"}'''
            def oauthAccessToken = new Token('token', 'secret', 'rawResponse=rawResponse')
            def response = [body:responseBody]
            oauthResourceService.get = { providerId, accessToken, url ->
                return response
            }
            service.oauthResourceService = oauthResourceService
        when:
            def token = service.createAuthToken( oauthAccessToken )
        then:
            token.principal == '123123123'
            token.socialId == '123123123'
            token.providerName == 'facebook'
    }

}
