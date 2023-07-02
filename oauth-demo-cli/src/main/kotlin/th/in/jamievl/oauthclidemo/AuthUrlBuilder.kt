package th.`in`.jamievl.oauthclidemo

import org.apache.hc.core5.net.URIBuilder
import java.io.FileNotFoundException
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*

internal object AuthUrlBuilderStatic {
    val clientId: String;

    init {
        val loader = AuthUrlBuilderStatic.javaClass.classLoader
        val clientIdStream = loader.getResourceAsStream("clientid.properties")
            ?: throw FileNotFoundException("Cannot find clientid.properties in classpath!")

        with (Properties()) {
            load(clientIdStream)

            clientId = getProperty("client.id")
        }
    }
}

fun generateAuthUrl(codeChallenge: String, state: String): URI {
    return with(URIBuilder()) {
        charset = StandardCharsets.UTF_8
        scheme = "https"
        host = "gitlab.com"
        path = "/oauth/authorize"

        addParameter("client_id", AuthUrlBuilderStatic.clientId)
        addParameter("redirect_uri", "toauth://redirect")
        addParameter("scope", "read_user")
        addParameter("state", state)
        addParameter("code_challenge", codeChallenge)
        addParameter("response_type", "code")
        addParameter("code_challenge_method", "S256")

        build()
    }
}

fun generateTokenUrlWithReqCode(codeVerifier: String, requestCode: String): URI {
    return with(URIBuilder()) {
        charset = StandardCharsets.UTF_8
        scheme = "https"
        host = "gitlab.com"
        path = "/oauth/token"

        addParameter("client_id", AuthUrlBuilderStatic.clientId)
        addParameter("code", requestCode)
        addParameter("grant_type", "authorization_code")
        addParameter("redirect_uri", "toauth://redirect")
        addParameter("code_verifier", codeVerifier)
        build()
    }
}
