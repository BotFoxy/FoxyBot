package net.cakeyfox.foxy.utils.analytics

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import net.cakeyfox.foxy.utils.analytics.utils.StatsSender
import kotlin.reflect.jvm.jvmName

class TopggStatsSender(
    private val http: HttpClient,
    private val clientId: Long,
    private val token: String
): StatsSender {
    private val logger = KotlinLogging.logger(this::class.jvmName)

    override suspend fun send(guildCount: Long): Boolean {
        val response = http.post("https://top.gg/api/bots/$clientId/stats") {
            header("Authorization", token)
            accept(ContentType.Application.Json)
            setBody(
                TextContent(Json.encodeToString(BotStats(guildCount)), ContentType.Application.Json)
            )
        }

        if (response.status != HttpStatusCode.OK) {
            logger.error { "Failed to send stats to top.gg: ${response.status}" }
            return false
        }

        logger.info { "Successfully sent stats to top.gg" }
        return true
    }

    @Serializable
    data class BotStats(
        @SerialName("server_count")
        val serverCount: Long
    )
}