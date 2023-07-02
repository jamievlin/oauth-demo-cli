package th.`in`.jamievl.oauthclidemo

import java.awt.Desktop


fun main(args: Array<String>) {
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

    println("Waiting for done file...")
    rootDir.waitForDoneFile()
    val code = rootDir.readOAuthCode()

    val token = OAuth2Token.createFromAuthorizationCode(codeVerifier, code)!!
    println("hi hello")
}
