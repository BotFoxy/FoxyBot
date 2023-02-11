import { createCommand } from "../../structures/commands/createCommand";
import { createEmbed } from "../../utils/discord/Embed";
import * as os from 'os';
import { bot } from "../../index";

const StatusCommand = createCommand({
name: "status",
    description: "[🛠] Mostra o status do bot",
    descriptionLocalizations: {
        "en-US": "[🛠] Shows the bot status"
    },
    category: "util",
    options: [],
    execute: async (ctx, endCommand, t) => {
        const embed = createEmbed({
            title: '🦊 | Foxy Status',
            fields: [
                { name: `💻 | ${t('commands:status.model')}`, value: `\`\`\`${os.cpus().map(c => c.model)[0]}\`\`\`` },
                { name: `✨ | ${t('commands:status.ram')}`, value: `\`\`\`${(process.memoryUsage().heapUsed / 1024 / 1024).toFixed(2)} MB\`\`\``, inline: true },
                { name: `🛠 | ${t('commands:status.cpuUsage')}`, value: `\`\`\`${(process.cpuUsage().system / 1024 / 1024).toFixed(2)}%\`\`\``, inline: true },
                { name: `💁‍♀️ | ${t('commands:status.node')}`, value: `\`\`\`${process.version}\`\`\``, inline: true },
                { name: `🖥 | ${t('commands:status.arch')}`, value: `\`\`\`${process.arch}\`\`\``, inline: true },
                { name: `⛏ | ${t('commands:status.platform')}`, value: `\`\`\`${process.platform}\`\`\``, inline: true }
            ]
        });

        ctx.foxyReply({ embeds: [embed] });
        endCommand();
    }
})

export default StatusCommand;