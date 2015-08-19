<!doctype html>
<html lang="en" class="no-js">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title><g:layoutTitle default="Grails"/></title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <asset:stylesheet src="application.css"/>
        <asset:javascript src="application.js"/>

        <g:layoutHead/>
    </head>
    <body>
        <div id="grailsLogo" role="banner"><a href="http://grails.org"><asset:image src="grails_logo.png" alt="Grails"/></a></div>
        <div>
            Logged with dummy? <s2oauth:ifLoggedInWith provider="dummy">yes</s2oauth:ifLoggedInWith>
            <s2oauth:ifNotLoggedInWith provider="dummy">
                no <s2oauth:connect provider="dummy">connect</s2oauth:connect>
            </s2oauth:ifNotLoggedInWith>
            Logged with facebook? <s2oauth:ifLoggedInWith provider="facebook">yes</s2oauth:ifLoggedInWith>
            <s2oauth:ifNotLoggedInWith provider="facebook">
                no <s2oauth:connect provider="facebook">connect</s2oauth:connect>
            </s2oauth:ifNotLoggedInWith>
            <br>
            <sec:ifLoggedIn>
                Logged in as <sec:username/>
                <form name="logout" action="${request.contextPath}/logout" method="POST"><input type="submit" value="Logout !"></form>
            </sec:ifLoggedIn>
            <sec:ifNotLoggedIn>
                <g:link controller='login' action='auth'>Login</g:link>
            </sec:ifNotLoggedIn>
        </div>
        <g:layoutBody/>
        <div class="footer" role="contentinfo"></div>
        <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
    </body>
</html>
