class S2oauthUrlMappings {

    static mappings = {
        "/oauth/$provider/callback"(controller: 's2oauth', action: 'callback')
        "/oauth/$provider/authenticate"(controller: 's2oauth', action: 'authenticate')
        "/oauth/$provider/success"(controller: 's2oauth', action: 'onSuccess')
        "/oauth/$provider/failure"(controller: 's2oauth', action: 'onFailure')

        '/oauth/account/choose'(controller: 's2oauth', action: 'chooseAccount')
        '/oauth/account/link'(controller: 's2oauth', action: 'linkAccount')
        '/oauth/account/create'(controller: 's2oauth', action: 'createAccount')
    }
}
