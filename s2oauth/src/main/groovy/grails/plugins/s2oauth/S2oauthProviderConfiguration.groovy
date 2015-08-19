package grails.plugins.s2oauth

class S2oauthProviderConfiguration {
	String apiKey
	String apiSecret
	String callbackUrl
	Class<?> apiClass
	String scope
	// org.scribe.model.SignatureType
	String signatureType
	boolean debug
}
