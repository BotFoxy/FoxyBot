import Command from "../../structures/command/BaseCommand";
import { SlashCommandBuilder } from "@discordjs/builders";
import { MessageEmbed, MessageActionRow, MessageButton } from "discord.js";
import NekosLife from "nekos.life";

export default class SlapCommand extends Command {
    constructor(client) {
        super(client, {
            name: "slap",
            description: "Slaps someone",
            category: "actions",
            dev: false,
            data: new SlashCommandBuilder()
                .setName("slap")
                .setDescription("[Roleplay] Slaps someone")
                .addUserOption(option => option.setName("user").setRequired(true).setDescription("User you want to slap"))
        });
    }

    async execute(interaction, t): Promise<void> {
        const user = interaction.options.getUser("user");
        if (!user) return interaction.reply(t('commands:global.noUser'));

        const neko = new NekosLife();

        const slap = await neko.slap();
        const slap2 = await neko.slap();
        const row = new MessageActionRow()
            .addComponents(
                new MessageButton()
                    .setLabel(t('commands:slap.button'))
                    .setCustomId("slap")
                    .setStyle("PRIMARY")
            )

        const embed = new MessageEmbed()
            .setDescription(t('commands:slap.success', { user: user.username, author: interaction.user.username }))
            .setImage(slap.url)
        await interaction.reply({ embeds: [embed], components: [row] });

        const filter = i => i.customId === "slap" && i.user.id === user.id;
        const collector = interaction.channel.createMessageComponentCollector(filter, { time: 15000, max: 1 });

        collector.on("collect", async i => {
            if (i.customId === "slap") {
                if (await this.client.ctx.getContext(interaction, i, 2, user)) {
                    const embed2 = new MessageEmbed()
                        .setDescription(t('commands:slap.success', { user: interaction.user.username, author: user.username }))
                        .setImage(slap2.url)
                    await interaction.followUp({ embeds: [embed2] });
                    i.deferUpdate();
                    return collector.stop();
                } else {
                    i.deferUpdate();
                }
            }
        })
    }
}