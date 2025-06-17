package co.stellarskys.stella.utils

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.render.*
import net.minecraft.client.texture.SpriteAtlasTexture
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.screen.PlayerScreenHandler
import net.minecraft.util.math.Vec3d

object RenderUtils {
    // Outline buffer for wireframe edges
    fun getLineBuffer(provider: VertexConsumerProvider): VertexConsumer {
        return provider.getBuffer(RenderLayer.getLines())
    }

    // Draw a single line between two points
    fun drawLine(matrices: MatrixStack, buffer: VertexConsumer, start: Vec3d, end: Vec3d) {
        val mat = matrices.peek().positionMatrix
        buffer.vertex(mat, start.x.toFloat(), start.y.toFloat(), start.z.toFloat()).color(255, 255, 255, 255).light(15728880).normal(0f, 1f, 0f)
        buffer.vertex(mat, end.x.toFloat(), end.y.toFloat(), end.z.toFloat()).color(255, 255, 255, 255).light(15728880).normal(0f, 1f, 0f)
    }


    // Render an entire cube outline using three sets of four lines
    fun renderCubeOutline(matrices: MatrixStack, buffer: VertexConsumer, min: Vec3d, max: Vec3d) {
        // Bottom edges
        drawLine(matrices, buffer, min, Vec3d(max.x, min.y, min.z))
        drawLine(matrices, buffer, Vec3d(max.x, min.y, min.z), Vec3d(max.x, min.y, max.z))
        drawLine(matrices, buffer, Vec3d(max.x, min.y, max.z), Vec3d(min.x, min.y, max.z))
        drawLine(matrices, buffer, Vec3d(min.x, min.y, max.z), min)

        // Top edges
        drawLine(matrices, buffer, Vec3d(min.x, max.y, min.z), Vec3d(max.x, max.y, min.z))
        drawLine(matrices, buffer, Vec3d(max.x, max.y, min.z), max)
        drawLine(matrices, buffer, max, Vec3d(min.x, max.y, max.z))
        drawLine(matrices, buffer, Vec3d(min.x, max.y, max.z), Vec3d(min.x, max.y, min.z))

        // Vertical edges
        drawLine(matrices, buffer, min, Vec3d(min.x, max.y, min.z))
        drawLine(matrices, buffer, Vec3d(max.x, min.y, min.z), Vec3d(max.x, max.y, min.z))
        drawLine(matrices, buffer, Vec3d(max.x, min.y, max.z), max)
        drawLine(matrices, buffer, Vec3d(min.x, min.y, max.z), Vec3d(min.x, max.y, max.z))
    }

    fun renderFace(matrices: MatrixStack, buffer: VertexConsumer, corners: List<Vec3d>, opacity: Int) {
        val mat = matrices.peek().positionMatrix
        corners.forEach { corner ->
            buffer.vertex(mat, corner.x.toFloat(), corner.y.toFloat(), corner.z.toFloat()).color(255, 255, 255, opacity).light(15728880).normal(0f, 1f, 0f)
        }
    }

    // Render an entire cube filled using `renderFace()`
    fun renderCubeFilled(matrices: MatrixStack, buffer: VertexConsumer, min: Vec3d, max: Vec3d, opacity: Int) {

        renderFace(matrices, buffer, listOf(min, Vec3d(max.x, min.y, min.z), Vec3d(max.x, min.y, max.z), Vec3d(min.x, min.y, max.z)), opacity) // Bottom
        renderFace(matrices, buffer, listOf(Vec3d(min.x, max.y, min.z), Vec3d(max.x, max.y, min.z), max, Vec3d(min.x, max.y, max.z)), opacity) // Top
        renderFace(matrices, buffer, listOf(min, Vec3d(min.x, max.y, min.z), Vec3d(min.x, max.y, max.z), Vec3d(min.x, min.y, max.z)), opacity) // Left
        renderFace(matrices, buffer, listOf(Vec3d(max.x, min.y, min.z), Vec3d(max.x, max.y, min.z), max, Vec3d(max.x, min.y, max.z)), opacity) // Right
        renderFace(matrices, buffer, listOf(min, Vec3d(max.x, min.y, min.z), Vec3d(max.x, max.y, min.z), Vec3d(min.x, max.y, min.z)), opacity) // Front
        renderFace(matrices, buffer, listOf(Vec3d(min.x, min.y, max.z), Vec3d(max.x, min.y, max.z), max, Vec3d(min.x, max.y, max.z)), opacity) // Back
    }
}
