

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'helloworld.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'helloworld.UserRole'
grails.plugin.springsecurity.authority.className = 'helloworld.Role'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	'/':                ['permitAll'],
	'/error':           ['permitAll'],
	'/index':           ['permitAll'],
	'/index.gsp':       ['permitAll'],
	'/shutdown':        ['permitAll'],
	'/assets/**':       ['permitAll'],
	'/**/js/**':        ['permitAll'],
	'/**/css/**':       ['permitAll'],
	'/**/images/**':    ['permitAll'],
	'/**/favicon.ico':  ['permitAll'],
	// s2oauth
  '/oauth/**':        ['permitAll']
]
