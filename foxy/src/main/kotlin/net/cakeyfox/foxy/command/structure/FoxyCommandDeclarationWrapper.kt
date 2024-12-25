package net.cakeyfox.foxy.command.structure

import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

interface FoxyCommandDeclarationWrapper {
    fun create(): FoxyCommandDeclarationBuilder

    fun command(
        name: String,
        description: String,
        isPrivate: Boolean = false,
        integrationType: List<IntegrationType> = listOf(IntegrationType.GUILD_INSTALL),
        interactionContexts: List<InteractionContextType> = listOf(InteractionContextType.GUILD),
        block: FoxyCommandDeclarationBuilder.() -> Unit
    ): FoxyCommandDeclarationBuilder {
        return FoxyCommandDeclarationBuilder(
            name,
            description,
            isPrivate,
            integrationType,
            interactionContexts
        ).apply(block)
    }
}