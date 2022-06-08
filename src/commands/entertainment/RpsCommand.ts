import Command from "../../structures/BaseCommand";
import { SlashCommandBuilder } from "@discordjs/builders";

export default class RpsCommand extends Command {
    constructor(client) {
        super(client, {
            name: "rps",
            description: "Rock, paper, scissors",
            category: "fun",
            dev: false,
            data: new SlashCommandBuilder()
                .setName("rps")
                .setDescription("[Entertainment] Rock, paper, scissors")
                .addStringOption(option => option.setName("choice").setDescription("Rock, paper or scissors").setRequired(true))
        });
    }

    async execute(interaction, t): Promise<void> {
        const string = await interaction.options.getString("choice").toLowerCase();
        const acceptedReplies = [t('commands:rps.replies.rock'), t('commands:rps.replies.paper'), t('commands:rps.replies.scissors')];

        const random = Math.floor((Math.random() * acceptedReplies.length));
        const result = acceptedReplies[random];

        if (!acceptedReplies.includes(string)) return interaction.reply(t('commands:rps.invalidChoice', { choice: acceptedReplies.join(', ') }));
        if (result === string) return interaction.reply(t('commands:rps.tie'));

        switch (string) {
            case t('commands:rps.replies.rock'): {
                if (result === t('commands:rps.replies.paper')) return interaction.reply(t('commands:rps.clientWon', { result: result }));
                return interaction.reply(t('commands:rps.won3'));
            }
            case t('commands:rps.replies.paper'): {
                if (result === t('commands:rps.replies.scissors')) return interaction.reply(t('commands:rps.clientWon', { result: result }));
                return interaction.reply(t('commands:rps.won2'));
            }
            case t('commands:rps.replies.scissors'): {
                if (result === t('commands:rps.replies.rock')) return interaction.reply(t('commands:rps.clientWon', { result: result }));
                return interaction.reply(t('commands:rps.won'));
            }
        }
    }
}