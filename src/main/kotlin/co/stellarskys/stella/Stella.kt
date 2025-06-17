package co.stellarskys.stella

import co.stellarskys.stella.events.WorldOutlineCallback
import co.stellarskys.stella.features.BlockOverlay
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.render.VertexConsumerProvider

object Stella : ClientModInitializer {
	public val NAMESPACE = "stella"

	override fun onInitializeClient() {
		init()
		// Register outline event
		WorldOutlineCallback.register()

		// Ensure BlockOverlay gets drawn each frame
		WorldRenderEvents.AFTER_ENTITIES.register { ctx ->
			val providers = ctx.consumers() as VertexConsumerProvider.Immediate
			BlockOverlay.render(ctx.matrixStack(), providers)
		}
	}

	private fun init() {
	}
}