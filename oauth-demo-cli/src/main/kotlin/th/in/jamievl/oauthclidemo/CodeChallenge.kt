package th.`in`.jamievl.oauthclidemo

import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal object SecureRng {
    val rng = SecureRandom()
    val acceptedChars = "0123456789abcdefghijklmnopqrstuvwxyz-_.~ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    val hasher = MessageDigest.getInstance("SHA-256");
}

internal fun generateRandomString(length: Long): String = SecureRng.rng
        .ints(length, 0, SecureRng.acceptedChars.length)
        .mapToObj { SecureRng.acceptedChars[it] }
        .toList()
        .joinToString("")

internal fun generateCodeVerifier(): String = generateRandomString(SecureRng.rng.nextLong(43, 129))

internal fun generateCodeChallenge(verifierCode: String): String {
    val rawDigest = SecureRng.hasher.digest(verifierCode.toByteArray(StandardCharsets.UTF_8))
    // dropLast is for stripping the last "=" character
    return Base64.getUrlEncoder().encodeToString(rawDigest).dropLast(1)
}

fun generateCodeChallengeAndVerifier(): Pair<String, String>  {
    val verifierCode = generateCodeVerifier();
    return Pair(verifierCode, generateCodeChallenge(verifierCode))
}
