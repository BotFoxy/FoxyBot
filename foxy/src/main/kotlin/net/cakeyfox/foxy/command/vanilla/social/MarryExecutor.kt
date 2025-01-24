package net.cakeyfox.foxy.command.vanilla.social

import net.cakeyfox.common.FoxyEmotes
import net.cakeyfox.foxy.command.FoxyInteractionContext
import net.cakeyfox.foxy.command.structure.FoxyCommandExecutor
import net.cakeyfox.foxy.utils.pretty
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.time.ZoneId
import java.time.ZonedDateTime

class MarryExecutor : FoxyCommandExecutor() {
    override suspend fun execute(context: FoxyInteractionContext) {
        val user = context.getOption<User>("user")!!

        if (user.id == context.event.user.id) {
            context.reply(true) {
                content = pretty(
                    FoxyEmotes.FoxyCry,
                    context.locale["marry.cantMarryYourself"]
                )
            }

            return
        }

        if (user.isBot) {
            context.reply(true) {
                content = pretty(
                    FoxyEmotes.FoxyCry,
                    context.locale["marry.cantMarryBot", user.asMention]
                )
            }

            return
        }

        if (user.id == context.foxy.selfUser.id) {
            context.reply(true) {
                content = pretty(
                    FoxyEmotes.FoxyCry,
                    context.locale["marry.cantMarryMe"]
                )
            }

            return
        }

        val userData = context.db.utils.user.getDiscordUser(user.id)

        if (userData.marryStatus.marriedWith != null) {
            context.reply(true) {
                content = pretty(
                    FoxyEmotes.FoxyCry,
                    context.locale["marry.userAlreadyMarried"]
                )
            }

            return
        }

        if (context.getAuthorData().marryStatus.marriedWith != null) {
            context.reply {
                content = pretty(
                    FoxyEmotes.FoxyCry,
                    context.locale["marry.youAlreadyMarried"]
                )
            }

            return
        }
        val marriedDate = ZonedDateTime.now(ZoneId.systemDefault()).toInstant()

        context.reply {
            content = pretty(
                FoxyEmotes.Ring,
                context.locale["marry.proposal", user.asMention, context.user.asMention]
            )

            actionRow(
                context.foxy.interactionManager.createButtonForUser(
                    user,
                    ButtonStyle.SUCCESS,
                    FoxyEmotes.FoxyCake,
                    context.locale["marry.acceptButton"],
                ) {
                    context.db.utils.user.updateUser(
                        context.event.user.id,
                        mapOf(
                            "marryStatus.marriedWith" to user.id,
                            "marryStatus.marriedDate" to marriedDate
                        )
                    )

                    context.db.utils.user.updateUser(
                        user.id,
                        mapOf(
                            "marryStatus.marriedWith" to context.event.user.id,
                            "marryStatus.marriedDate" to marriedDate
                        )
                    )

                    it.edit {
                        content = pretty(
                            FoxyEmotes.Ring,
                            context.locale["marry.accepted", user.asMention]
                        )

                        actionRow(
                            context.foxy.interactionManager.createButtonForUser(
                                user,
                                ButtonStyle.SUCCESS,
                                FoxyEmotes.FoxyCake,
                                context.locale["marry.acceptedButton"]
                            ) { }.asDisabled()
                        )
                    }
                }
            )
        }
    }
}