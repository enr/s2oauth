s2oauth
=======

**POC Spring security + OAuth plugin for Grails 3**

[![Build Status](https://api.travis-ci.org/enr/s2oauth.png?branch=master)](https://travis-ci.org/enr/s2oauth)

Main differences with the Grails 2 plugin:

- no more dependency on https://github.com/antony/grails-oauth-scribe but some code of that plugin was ported in this

- no (at the moment) domain classes and persistence in database

- simplest code as possible

Repository structure
--------------------

To speed up development there is a single "uber" repo containing:

- s2oauth: the base plugin
- s2oauth-facebook: Facebook implementation
- s2oauth-dummy: a dummy implementation, useful to test without involving a real provider
- helloworld: the actual app using the plugins

To build all projects and create the app war:

    ./gradlew war

To test all projects:

    ./gradlew test

How to create a new provider plugin
-----------------------------------

Steps used to create the Dummy plugin.

Create the plugin:

    grails create-plugin s2oauth-dummy
    cd s2oauth-dummy

Add the core plugin as dependency in `build.gradle`:

```groovy
compile project(':s2oauth')
// or "org.grails.plugins:s2oauth:0.1"
```

Create the plugin service:

    grails create-service grails.plugins.s2oauth.dummy.S2oauthDummy

Edit the service with `implements grails.plugins.s2oauth.S2oauthProviderService`
or `extends grails.plugins.s2oauth.S2oauthAbstractScribeProviderService`

Create token class in `src/main/groovy`:

```groovy
class DummyS2oauthToken extends grails.plugins.s2oauth.S2oauthToken {
    // ...
}
```

Usage
-----

How to use the plugin in your app?

In `build.gradle`:

```groovy
compile project(':s2oauth-facebook')
compile project(':s2oauth-dummy')
```

Set up the Spring security core plugin

Add S2oauth config in application.yml:

```yaml
s2oauth:
    providers:
        facebook:
          api_key: changeme_apikey
          api_secret: changeme_apisecret
```

You can set API keys as environment variable in the format `"${providerId.toUpperCase()}_API_KEY` and `${providerId.toUpperCase()}_API_SECRET`.

Use taglibs, see `helloworld/grails-app/views/layouts/main.gsp`.

License
-------

Apache 2
