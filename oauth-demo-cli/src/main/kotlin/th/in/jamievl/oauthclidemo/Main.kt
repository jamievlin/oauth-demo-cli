package th.`in`.jamievl.oauthclidemo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.credentialstorage.StorageProvider
import com.microsoft.credentialstorage.model.StoredTokenPair
import org.apache.hc.client5.http.classic.HttpClient
import org.apache.hc.client5.http.classic.methods.HttpGet
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.apache.hc.core5.http.message.BasicHeader
import java.awt.Desktop
import java.nio.charset.StandardCharsets

const val OAUTH_STOARGE_CRED_KEY = "th.in.jamievl.oauthclidemo/oauth_key"

fun authorizeOAuth(): OAuth2Token {
    val (codeVerifier, codeChallenge) = generateCodeChallengeAndVerifier()
    val rootDir = RootDirectoryContext.createWithDefaultDirectory()
    val state = rootDir.rootDirectoryName

    val authUrl = generateAuthUrl(codeChallenge, state)

    if (Desktop.isDesktopSupported()) {
        rootDir.cleanOAuthWorkingFiles()

        Desktop.getDesktop()
            ?.takeIf { it.isSupported(Desktop.Action.BROWSE) }!!
            .browse(authUrl)
    } else {
        throw RuntimeException("Cannot launch browser for OAuth2 auth!")
    }

    println("hi!")
    println("Waiting for done file...")
    rootDir.waitForDoneFile()
    val code = rootDir.readOAuthCode()

    return OAuth2Token.createFromAuthorizationCode(codeVerifier, code)!!
}

fun printUserInfo(authToken: StoredTokenPair, client: HttpClient) {
    val mapper = jacksonObjectMapper()

    val getReq = HttpGet("https://gitlab.com/api/v4/user").apply {
        addHeader(BasicHeader( "Authorization", "Bearer ${String(authToken.accessToken.value)}", true))
        addHeader("Content-Type", "application/json")
    }

    val result = client.execute(getReq) { rsp -> rsp.use {
        rsp.entity?.let {ent ->
            EntityUtils.toString(ent, StandardCharsets.UTF_8)
                ?.let { entStr -> mapper.readValue<HashMap<String, Any>>(entStr) }
        }
    }}

    val name = result?.get("name") ?: "<name unknown>"
    println("name = $name")
}

fun main(args: Array<String>) {
    val credStore = StorageProvider.getTokenPairStorage(true, StorageProvider.SecureOption.REQUIRED) ?:
        throw RuntimeException("Cannot find secure storage!")

    val token: StoredTokenPair = credStore.get(OAUTH_STOARGE_CRED_KEY) ?: run {
        authorizeOAuth().toStoredCredential().also { tokPair ->
                credStore.add(OAUTH_STOARGE_CRED_KEY, tokPair)
        }
    }

    val client = HttpClients.createDefault()
    client.use {
        printUserInfo(token, client)
    }

    token.clear()
}
