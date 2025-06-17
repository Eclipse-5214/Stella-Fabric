package co.stellarskys.stella.events

import co.stellarskys.stella.features.BlockOverlay
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.MinecraftClient

object WorldOutlineCallback {
    fun register() {
        WorldRenderEvents.BLOCK_OUTLINE.register { _, context ->
            val client = MinecraftClient.getInstance()
            val world = client.world ?: return@register true

            val pos = context.blockPos()
            val state = context.blockState()

            // Send block data to `BlockOverlay`
            BlockOverlay.queue(pos, state, world)

            // Cancel vanilla outline rendering
            false
        }
    }
}
