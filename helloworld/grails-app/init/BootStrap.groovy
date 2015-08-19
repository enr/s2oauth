import helloworld.*

class BootStrap {

    public static final String ROLE_USER_KEY = 'ROLE_USER'
    public static final String ROLE_ADMIN_KEY = 'ROLE_ADMIN'

    def init = { servletContext ->
        println log.name

        log.info 'Starting S2oauth sample app...'
        //log.info grailsApplication.config.oauth.toString()
        log.info ''

        log.info 'creating default roles...'
        def userRole = Role.findByAuthority(ROLE_USER_KEY) ?: new Role(authority: ROLE_USER_KEY).save(failOnError: true)
        def adminRole = Role.findByAuthority(ROLE_ADMIN_KEY) ?: new Role(authority: ROLE_ADMIN_KEY).save(failOnError: true)

        log.info 'creating default users...'
        def admin = new User(username:'admin', password:'admin'*2, email:'admin@yourapp.com', 'enabled':true)
        if (admin.save(flush:true)) {
            UserRole.create admin, adminRole
            log.info 'created user admin/adminadmin'
        }

        def user = new User(username:'user', password:'user'*2, email:'user@yourapp.com', 'enabled':true)
        if (user.save(flush:true)) {
            UserRole.create user, userRole
            log.info 'created user user/useruser'
        }
    }

    def destroy = {
    }
}
