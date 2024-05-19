import { MessageFlags } from "../../../utils/discord/Message";
import { logger } from "../../../utils/logger";
import UnleashedCommandExecutor from "../../structures/UnleashedCommandExecutor";

export default async function ValorantVerifyExecutor(bot, context: UnleashedCommandExecutor, endCommand, t) {
    const code = context.getOption<string>('authcode', false);
    const authCode = await bot.database.getCode(code);

    if (!authCode) {
        return context.sendReply({
            content: context.makeReply(bot.emotes.FOXY_CRY, t('commands:valorant.verify.noAuthCode')),
            flags: MessageFlags.EPHEMERAL
        })
    } else {
        const valUserInfo = await bot.database.getUser(context.author.id);
        if (valUserInfo.riotAccount.isLinked) {
            return context.sendReply({
                content: context.makeReply(bot.emotes.FOXY_CRY, t('commands:valorant.verify.alreadyLinked')),
                flags: MessageFlags.EPHEMERAL
            })
        } else {
            try {
                const userData = await bot.database.getUser(context.author.id);
                userData.riotAccount = {
                    isLinked: true,
                    puuid: authCode.puuid,
                    isPrivate: true,
                    region: null,
                }

                await userData.save();

                return context.sendReply({
                    content: context.makeReply(bot.emotes.FOXY_NICE, t('commands:valorant.verify.success')),
                    flags: MessageFlags.EPHEMERAL
                });
            } catch (err) {
                logger.error(err);
                return context.sendReply({
                    content: context.makeReply(bot.emotes.FOXY_CRY, t('commands:valorant.verify.error')),
                    flags: MessageFlags.EPHEMERAL
                })
            }
        }
    }
}