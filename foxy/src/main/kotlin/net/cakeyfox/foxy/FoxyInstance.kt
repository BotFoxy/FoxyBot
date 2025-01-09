package net.cakeyfox.foxy

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.cakeyfox.artistry.ArtistryClient
import net.cakeyfox.common.Constants
import net.cakeyfox.foxy.command.FoxyCommandManager
import net.cakeyfox.foxy.command.component.FoxyComponentManager
import net.cakeyfox.foxy.listeners.GuildEventListener
import net.cakeyfox.foxy.listeners.InteractionEventListener
import net.cakeyfox.foxy.listeners.MajorEventListener
import net.cakeyfox.foxy.utils.ActivityUpdater
import net.cakeyfox.foxy.utils.config.FoxyConfig
import net.cakeyfox.foxy.utils.FoxyUtils
import net.cakeyfox.foxy.utils.analytics.TopggStatsSender
import net.cakeyfox.foxy.utils.database.MongoDBClient
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.sharding.ShardManager
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.net.InetAddress
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.concurrent.thread
import kotlin.reflect.jvm.jvmName

class FoxyInstance(
    val config: FoxyConfig
) {
    lateinit var shardManager: ShardManager
    lateinit var mongoClient: MongoDBClient
    lateinit var commandHandler: FoxyCommandManager
    lateinit var artistryClient: ArtistryClient
    lateinit var utils: FoxyUtils
    lateinit var currentCluster: FoxyConfig.Cluster
    lateinit var interactionManager: FoxyComponentManager
    lateinit var httpClient: HttpClient
    lateinit var selfUser: User
    private lateinit var topggStatsSender: TopggStatsSender
    private lateinit var environment: String

    suspend fun start() {
        val logger = KotlinLogging.logger(this::class.jvmName)
        val activityUpdater = ActivityUpdater(this)
        val clusterId = withContext(Dispatchers.IO) {
            InetAddress.getLocalHost().hostName
        }
        currentCluster = config.discord.clusters.find { it.id == clusterId }
            ?: throw IllegalStateException("Unknown cluster $clusterId, check your config file!")

        environment = config.environment
        mongoClient = MongoDBClient()
        commandHandler = FoxyCommandManager(this)
        utils = FoxyUtils(this)
        interactionManager = FoxyComponentManager(this)
        artistryClient = ArtistryClient(config.others.artistry.key)
        httpClient = HttpClient(CIO) {
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }

            install(ContentNegotiation) {
                json()
            }
        }

        mongoClient.start(this)
        shardManager = DefaultShardManagerBuilder.create(
            GatewayIntent.GUILD_MEMBERS,
            GatewayIntent.MESSAGE_CONTENT,
            GatewayIntent.GUILD_MESSAGES,
            GatewayIntent.GUILD_EMOJIS_AND_STICKERS,
            GatewayIntent.SCHEDULED_EVENTS
        ).addEventListeners(
            MajorEventListener(this),
            GuildEventListener(this),
            InteractionEventListener(this)
        )
            .setStatus(OnlineStatus.ONLINE)
            .setActivity(Activity.customStatus(Constants.DEFAULT_ACTIVITY(config.environment)))
            .setShardsTotal(config.discord.totalShards)
            .setShards(currentCluster.minShard, currentCluster.maxShard)
            .setMemberCachePolicy(MemberCachePolicy.ALL)
            .setChunkingFilter(ChunkingFilter.NONE)
            .disableCache(CacheFlag.entries)
            .enableCache(
                CacheFlag.EMOJI,
                CacheFlag.STICKER,
                CacheFlag.MEMBER_OVERRIDES
            )
            .setToken(config.discord.token)
            .setEnableShutdownHook(false)
            .build()

        this.commandHandler.handle()

        selfUser = shardManager.shards.first().selfUser
        topggStatsSender = TopggStatsSender(this)

        Runtime.getRuntime().addShutdownHook(thread(false) {
            try {
                logger.info { "Foxy is shutting down..." }
                shardManager.shards.forEach { shard ->
                    shard.removeEventListener(*shard.registeredListeners.toTypedArray())
                    logger.info { "Shutting down shard #${shard.shardInfo.shardId}..." }
                    shard.shutdown()
                }
                httpClient.close()
                mongoClient.close()
                activityUpdater.shutdown()

                activeJobs.forEach {
                    logger.info { "Cancelling job $it" }
                    it.cancel()
                }
            } catch (e: Exception) {
                logger.error(e) { "Error during shutdown process" }
            }
        })
    }

    private val activeJobs = ConcurrentLinkedQueue<Job>()
}