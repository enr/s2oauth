package grails.plugins.s2oauth

/**
 * Main exception rethrow in case of errors.
 */
class S2oauthException extends RuntimeException {

    S2oauthException(String message) {
        super(message)
    }

    S2oauthException(String message, Throwable t) {
        super(message)
    }

}
