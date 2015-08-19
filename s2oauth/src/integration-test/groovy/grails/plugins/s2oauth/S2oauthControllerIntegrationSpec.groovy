package grails.plugins.s2oauth

import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*
import org.springframework.beans.factory.annotation.*

/**
 * Integration test for S2oauthController.
 */
@Integration
@Rollback
class S2oauthControllerIntegrationSpec extends Specification {

    /*
 	@Autowired
    S2oauthController controller

    // def setup() {
    //     controller = new S2oauthController()
    // }

    def "onSuccess throws exception if provider is missing"() {
        given:
            controller.params.provider = ''
        when:
            controller.onSuccess()
        then:
            thrown S2oauthException
    }
    */

}
