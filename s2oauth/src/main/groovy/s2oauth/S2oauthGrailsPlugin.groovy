package s2oauth

import grails.plugins.*

import grails.plugins.s2oauth.S2oauthProviderService

class S2oauthGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.0.4 > *"
    // resources that are excluded from plugin packaging
    //def pluginExcludes = [ "grails-app/views/error.gsp"    ]

    // TODO Fill in these fields
    def title = "S2oauth" // Headline display name of the plugin
    def author = "Your name"
    def authorEmail = ""
    def description = '''\
Brief summary/description of the plugin.
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/s2oauth"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
        def s2oauthService = grailsApplication.mainContext.getBean('s2oauthService')
        def oauthResourceService = grailsApplication.mainContext.getBean('oauthResourceService')
        for (serviceClass in grailsApplication.serviceClasses) {
            if (S2oauthProviderService.class.isAssignableFrom(serviceClass.clazz)) {
              println "Found oauth provider service: ${serviceClass.clazz}"
              String beanName = "${serviceClass.logicalPropertyName}Service"
              def providerService = grailsApplication.mainContext.getBean(beanName)
              s2oauthService.registerProvider(providerService)
              oauthResourceService.registerProvider(providerService)
            }
        }
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
