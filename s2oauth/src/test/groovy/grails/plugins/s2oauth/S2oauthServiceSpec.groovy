package grails.plugins.s2oauth

import grails.test.mixin.*
import spock.lang.*
import grails.persistence.Entity

@TestFor(S2oauthService)
@Mock(TestUser)
class S2oauthServiceSpec extends Specification {
    /*
    def "Should tell if username is taken"() {
        given: "a user named joe is in db"
            def joe = new TestUser(username: 'joe', password: 'secret')
            //assert !springSecurityOAuthService.usernameTaken(joe.username)
            joe.save()
            assert joe.errors.errorCount == 0
            assert joe.id != null
            assert  TestUser.get(joe.id).username == joe.username
        when: "service is asked usernameTaken joe"
        // securityConfig.userLookup.usernamePropertyName
            def securityConfig = [
                userLookup: [
                    usernamePropertyName:'username'
                ]
            ]
            S2oauthService.metaClass.lookupUserClass = { ->
                TestUser
            }
            //TestUser.metaClass.static.withNewSession = { Closure c -> c.call() }
            def s2oauthService = new S2oauthService()
            s2oauthService.securityConfig = securityConfig
            def taken = s2oauthService.existUserNamed(joe.username)
        then: "service responds true"
            taken
    }
    */
}

@Entity
class TestUser {
    String username
    String password
}
