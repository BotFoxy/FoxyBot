package net.cakeyfox.foxy.welcomer

import net.cakeyfox.foxy.FoxyInstance
import net.cakeyfox.foxy.welcomer.utils.WelcomerWrapper
import net.cakeyfox.foxy.welcomer.utils.WelcomerJSONParser
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent

class WelcomerManager(
    val instance: FoxyInstance
): WelcomerWrapper {
    private val welcomer = WelcomerJSONParser()

    override fun onGuildJoin(event: GuildMemberJoinEvent) {
        val guildData = instance.mongoClient.utils.guild.getGuild(event.guild.id)

        if (guildData.GuildJoinLeaveModule.isEnabled) {
            val placeholders = welcomer.getPlaceholders(event.guild, event.user)
            val rawMessage = guildData.GuildJoinLeaveModule.joinMessage ?: return

            val (content, embeds) = welcomer.parseDiscordJsonMessage(rawMessage, placeholders)

            val channel = event.guild.getTextChannelById(guildData.GuildJoinLeaveModule.joinChannel ?: "0")
                ?: return

            channel.sendMessage(content).setEmbeds(embeds).queue()
        }
    }

    override fun onGuildLeave(event: GuildMemberRemoveEvent) {
        val guildData = instance.mongoClient.utils.guild.getGuild(event.guild.id)

        if (guildData.GuildJoinLeaveModule.alertWhenUserLeaves) {
            val placeholders = welcomer.getPlaceholders(event.guild, event.user)
            val rawMessage = guildData.GuildJoinLeaveModule.leaveMessage ?: return

            val (content, embeds) = welcomer.parseDiscordJsonMessage(rawMessage, placeholders)

            val channel = event.guild.getTextChannelById(guildData.GuildJoinLeaveModule.leaveChannel ?: "0")
                ?: return

            channel.sendMessage(content).setEmbeds(embeds).queue()
        }
    }
}