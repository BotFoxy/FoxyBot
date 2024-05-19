import { Interaction, Message, User } from 'discordeno/transformers';
import { ApplicationCommandOptionTypes } from 'discordeno/types';
import { CanResolve } from './UnleashedCommandExecutor';
import { bot } from '../../FoxyLauncher';

function getOptionFromInteraction<T>(
  interaction: Interaction,
  name: string,
  shouldResolve: CanResolve,
  required: true,
): T;

function getOptionFromInteraction<T>(
  interaction: Interaction,
  name: string,
  shouldResolve: CanResolve,
  required?: false,
): T | undefined;

function getOptionFromInteraction<T>(
  interaction: Interaction,
  name: string,
  shouldResolve: CanResolve,
  required?: boolean,
): T | undefined;

function getOptionFromInteraction<T>(
  interaction: Interaction,
  name: string,
  shouldResolve: CanResolve,
  required?: boolean,
): T | undefined {
  let options = interaction.data?.options ?? [];

  if (options[0]?.type === ApplicationCommandOptionTypes.SubCommandGroup)
    options = options[0].options ?? [];

  if (options[0]?.type === ApplicationCommandOptionTypes.SubCommand)
    options = options[0].options ?? [];

  const found = options.find((option) => option.name === name) as unknown as { value: T } | undefined;

  if (!found && required)
    throw new Error(`Option ${name} is required in ${interaction.data?.name}`);

  if (!found) return undefined;

  if (shouldResolve && shouldResolve !== "full-string")
    return interaction.data?.resolved?.[shouldResolve]?.get(
      BigInt(found?.value as unknown as string),
    ) as unknown as T;

  return found?.value as T;
}

function getArgsFromMessage<T>(
  message: string,
  name: string,
  position: number,
  shouldResolve: CanResolve,
  messageContext: Message,
  required?: boolean
): T;

function getArgsFromMessage<T>(
  message: string,
  name: string,
  position: number,
  shouldResolve: CanResolve,
  messageContext: Message,
  required?: boolean
): T | undefined;

function getArgsFromMessage<T>(
  message: string,
  name: string,
  position: number,
  shouldResolve: CanResolve,
  messageContext: Message,
  required?: boolean
): T | undefined;

function getArgsFromMessage<T>(
  message: string,
  name: string,
  position: number,
  shouldResolve: CanResolve,
  messageContext: Message,
  required?: boolean
): T | undefined {
  if (shouldResolve === "users") {
    const args = message.split(' ');
    async function getUser(userId: string): Promise<User> {
      const id = userId ? userId.replace(/[^0-9]/g, '') : userId;
      if (!id) return null;
      return bot.users.get(BigInt(id)) || await bot.helpers.getUser(id);
    }
    return getUser(args[1]) as unknown as T;
  }

  if (shouldResolve === "full-string") {
    const args = message.split(' ');
    const found = args.slice(position).join(' ') as unknown as T;

    if (!found && required)
      throw new Error(`Option ${name} is required in ${message}`);

    if (!found) return undefined;

    return found;
  }

  const args = message.split(' ');
  const found = args[position] as unknown as T;

  if (!found && required)
    throw new Error(`Option ${name} is required in ${message}`);

  if (!found) return undefined;

  return found;
}

export { getOptionFromInteraction, getArgsFromMessage };