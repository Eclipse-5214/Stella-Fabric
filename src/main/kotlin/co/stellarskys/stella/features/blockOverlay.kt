package co.stellarskys.stella.features

import co.stellarskys.stella.utils.RenderUtils
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockRenderView

object BlockOverlay {
    private var queuedPos: BlockPos? = null
    private var queuedState: BlockState? = null
    private var queuedWorld: BlockRenderView? = null

    fun queue(pos: BlockPos, state: BlockState, world: BlockRenderView) {
        println("Queued block at $pos with state $state") // Debug info
        queuedPos = pos
        queuedState = state
        queuedWorld = world
    }

    fun render(matrices: MatrixStack?, providers: VertexConsumerProvider.Immediate) {
        val safeMatrices = matrices ?: return // Ensure it's never null
        val pos = queuedPos ?: return
        val state = queuedState ?: return
        val world = queuedWorld ?: return

        val client = MinecraftClient.getInstance()
        val cameraPos = client.gameRenderer.camera.pos
        val offset = Vec3d(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z)

        val shape = state.getOutlineShape(world, pos)
        val lineBuffer = RenderUtils.getLineBuffer(providers) // Single buffer for both outlines & faces

        for (box in shape.boundingBoxes) {
            val expansion = 0.005
            val min = Vec3d(box.minX - expansion, box.minY - expansion, box.minZ - expansion).add(offset)
            val max = Vec3d(box.maxX + expansion, box.maxY + expansion, box.maxZ + expansion).add(offset)

            println(min)
            println(max)

            // 🖊️ **Render Outlines Using the Same Buffer**
            RenderUtils.renderCubeOutline(matrices, lineBuffer, min, max)

            RenderUtils.renderCubeFilled( matrices, lineBuffer, min, max, 255)

        }

    }
}
