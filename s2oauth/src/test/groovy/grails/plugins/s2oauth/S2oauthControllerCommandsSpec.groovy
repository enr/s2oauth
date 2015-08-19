package grails.plugins.s2oauth

import grails.test.mixin.TestFor
import spock.lang.*

/**
 * Unit test for OAuthCreateAccountCommand.
 */
@TestFor(S2oauthController)
class S2oauthControllerCommandsSpec extends Specification {

    @Unroll
    def "Registration command objects for #loginId validating correctly"() {

        given: "a mocked command object"
        def urc = mockCommandObject(OAuthCreateAccountCommand)
        urc.s2oauthService = [existUserNamed: { u -> false }]

        and: "a set of initial values from the spock test"
        urc.username = loginId
        urc.password1 = password
        urc.password2 = passwordRepeat

        when: "the validator is invoked"
        def isValidRegistration = urc.validate()

        then: "the appropriate fields are flagged as errors"
        isValidRegistration == anticipatedValid
        urc.errors.getFieldError(fieldInError)?.code == errorCode

        where:
        loginId     | password      | passwordRepeat    | anticipatedValid  | fieldInError      | errorCode
        "glen"      | "password"    | "no-match"        | false             | "password2"       | "OAuthCreateAccountCommand.password.error.mismatch"
        "glen"      | "!QAZxsw"     | "!QAZxsw"         | false             | "password1"       | "minSize.notmet"
        "glen"      | "password"    | "password"        | false             | "password1"       | "OAuthCreateAccountCommand.password.error.strength"
        "peter"     | "!QAZxsw2"    | "!QAZxsw2"        | true              | null              | null
        "a"         | "password"    | "password"        | false             | "username"        | "minSize.notmet"

    }

}
