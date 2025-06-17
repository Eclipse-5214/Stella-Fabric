package co.stellarskys.stella

import co.stellarskys.stella.events.WorldOutlineCallback
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.VertexConsumerProvider
import co.stellarskys.stella.features.BlockOverlay

object Stella : ClientModInitializer {
	override fun onInitializeClient() {
		// Register outline event
		WorldOutlineCallback.register()

		// Ensure BlockOverlay gets drawn each frame
		WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
			val providers = ctx.consumers() as VertexConsumerProvider.Immediate
			BlockOverlay.render(ctx.matrixStack(), providers)
		}
	}
}