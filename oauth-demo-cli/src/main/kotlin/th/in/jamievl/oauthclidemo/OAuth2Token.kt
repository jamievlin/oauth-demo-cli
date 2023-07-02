package th.`in`.jamievl.oauthclidemo

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.microsoft.credentialstorage.model.StoredTokenPair
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.apache.hc.core5.http.io.entity.EntityUtils
import java.nio.charset.StandardCharsets

import java.util.*


@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class OAuth2Token(
    @JsonProperty("access_token") val accessToken: String,
    @JsonProperty("token_type") val tokenType: String,
    @JsonProperty("expires_in") val expiresIn: Long?,
    @JsonProperty("created_at") val createdAt: Long?,
    @JsonProperty("refresh_token") val refreshToken: String?,
    @JsonProperty("scope") val scope: String?
) {
    companion object {
        private val mapper = jacksonObjectMapper()

        fun createFromAuthorizationCode(codeVerifier: String, requestCode: String): OAuth2Token? {
            val postReq = HttpPost(generateTokenUrlWithReqCode(codeVerifier, requestCode))

            return HttpClients.createDefault()?.use {client ->
                client.execute(postReq) { rsp -> rsp.use excInt@{
                    val entity = it.entity
                    val entityByteArray = entity?.let { ent -> EntityUtils.toString(ent, StandardCharsets.UTF_8) }

                    if (it.code == 400) {
                        return@excInt null
                    }

                    entityByteArray?.let { byteArr -> mapper.readValue<OAuth2Token>(byteArr) }
                }}
            }
        }
    }

    fun toStoredCredential() = StoredTokenPair(accessToken.toCharArray(), refreshToken!!.toCharArray())
}
