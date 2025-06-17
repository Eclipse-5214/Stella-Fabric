package co.stellarskys.stella.utils

import co.stellarskys.stella.Stella
import co.stellarskys.stella.annotations.Init
import co.stellarskys.stella.mixin.accessors.BeaconBlockEntityRendererInvoker
import co.stellarskys.stella.utils.FrustumUtils.isVisible
import co.stellarskys.stella.utils.StellaRenderLayers.getLines
import co.stellarskys.stella.utils.StellaRenderLayers.getLinesThroughWalls
import co.stellarskys.stella.utils.StellaRenderLayers.getTexture
import co.stellarskys.stella.utils.StellaRenderLayers.getTextureThroughWalls
import com.mojang.blaze3d.systems.RenderSystem
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents.AfterTranslucent
import net.fabricmc.fabric.api.event.Event
import net.minecraft.block.BlockState
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.LightmapTextureManager
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.VertexConsumerProvider.Immediate
import net.minecraft.client.render.VertexRendering
import net.minecraft.client.util.BufferAllocator
import net.minecraft.client.world.ClientWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.text.OrderedText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.ColorHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.profiler.Profilers
import org.joml.Matrix4f
import java.awt.Color
import java.util.function.Consumer
import java.util.function.Function
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object RenderHelper {
    private val TRANSLUCENT_DRAW: Identifier = Identifier.of(Stella.NAMESPACE, "translucent_draw")
    private const val MAX_OVERWORLD_BUILD_HEIGHT = 319
    private val CLIENT: MinecraftClient = MinecraftClient.getInstance()
    private val ALLOCATOR = BufferAllocator(1536)

    @Init
    fun init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.addPhaseOrdering(Event.DEFAULT_PHASE, TRANSLUCENT_DRAW)
        WorldRenderEvents.AFTER_TRANSLUCENT.register(
            TRANSLUCENT_DRAW,
            AfterTranslucent { obj: WorldRenderContext? -> RenderHelper.drawTranslucents() })
    }

    fun renderFilledWithBeaconBeam(
        context: WorldRenderContext,
        pos: BlockPos,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        renderFilled(context, pos, colorComponents, alpha, throughWalls)
        renderBeaconBeam(context, pos, colorComponents)
    }

    fun renderFilled(
        context: WorldRenderContext,
        pos: BlockPos,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        renderFilled(
            context,
            pos.getX().toDouble(),
            pos.getY().toDouble(),
            pos.getZ().toDouble(),
            (pos.getX() + 1).toDouble(),
            (pos.getY() + 1).toDouble(),
            (pos.getZ() + 1).toDouble(),
            colorComponents,
            alpha,
            throughWalls
        )
    }

    fun renderFilled(
        context: WorldRenderContext,
        pos: Vec3d,
        dimensions: Vec3d,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        renderFilled(
            context,
            pos.x,
            pos.y,
            pos.z,
            pos.x + dimensions.x,
            pos.y + dimensions.y,
            pos.z + dimensions.z,
            colorComponents,
            alpha,
            throughWalls
        )
    }

    fun renderFilled(
        context: WorldRenderContext,
        box: Box,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        renderFilled(
            context,
            box.minX,
            box.minY,
            box.minZ,
            box.maxX,
            box.maxY,
            box.maxZ,
            colorComponents,
            alpha,
            throughWalls
        )
    }

    fun renderFilled(
        context: WorldRenderContext,
        minX: Double,
        minY: Double,
        minZ: Double,
        maxX: Double,
        maxY: Double,
        maxZ: Double,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        if (throughWalls) {
            if (isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
                renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha, true)
            }
        } else {
            if (isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
                renderFilledInternal(context, minX, minY, minZ, maxX, maxY, maxZ, colorComponents, alpha, false)
            }
        }
    }

    private fun renderFilledInternal(
        context: WorldRenderContext,
        minX: Double,
        minY: Double,
        minZ: Double,
        maxX: Double,
        maxY: Double,
        maxZ: Double,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        val matrices = context.matrixStack()
        val camera = context.camera().getPos()

        matrices!!.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)

        val consumers = context.consumers()
        val buffer =
            consumers!!.getBuffer(if (throughWalls) StellaRenderLayers.FILLED_THROUGH_WALLS else StellaRenderLayers.FILLED)

        VertexRendering.drawFilledBox(
            matrices,
            buffer,
            minX,
            minY,
            minZ,
            maxX,
            maxY,
            maxZ,
            colorComponents[0],
            colorComponents[1],
            colorComponents[2],
            alpha
        )

        matrices.pop()
    }

    fun renderBeaconBeam(context: WorldRenderContext, pos: BlockPos, colorComponents: FloatArray) {
        if (isVisible(
                pos.getX().toDouble(),
                pos.getY().toDouble(),
                pos.getZ().toDouble(),
                (pos.getX() + 1).toDouble(),
                MAX_OVERWORLD_BUILD_HEIGHT.toDouble(),
                (pos.getZ() + 1).toDouble()
            )
        ) {
            val matrices = context.matrixStack()
            val camera = context.camera().getPos()

            matrices!!.push()
            matrices.translate(pos.getX() - camera.getX(), pos.getY() - camera.getY(), pos.getZ() - camera.getZ())

            val length = camera.subtract(pos.toCenterPos()).horizontalLength().toFloat()
            val scale =
                if (CLIENT.player != null && CLIENT.player!!.isUsingSpyglass()) 1.0f else max(1.0f, length / 96.0f)

            BeaconBlockEntityRendererInvoker.renderBeam(
                matrices,
                context.consumers(),
                context.tickCounter().getTickProgress(true),
                scale,
                context.world().getTime(),
                0,
                MAX_OVERWORLD_BUILD_HEIGHT,
                ColorHelper.fromFloats(1f, colorComponents[0], colorComponents[1], colorComponents[2])
            )

            matrices.pop()
        }
    }

    fun renderOutline(
        context: WorldRenderContext,
        pos: BlockPos,
        colorComponents: FloatArray,
        lineWidth: Float,
        throughWalls: Boolean
    ) {
        renderOutline(
            context,
            pos.getX().toDouble(),
            pos.getY().toDouble(),
            pos.getZ().toDouble(),
            (pos.getX() + 1).toDouble(),
            (pos.getY() + 1).toDouble(),
            (pos.getZ() + 1).toDouble(),
            colorComponents,
            1f,
            lineWidth,
            throughWalls
        )
    }

    fun renderOutline(
        context: WorldRenderContext,
        pos: Vec3d,
        dimensions: Vec3d,
        colorComponents: FloatArray,
        lineWidth: Float,
        throughWalls: Boolean
    ) {
        renderOutline(
            context,
            pos.x,
            pos.y,
            pos.z,
            pos.x + dimensions.x,
            pos.y + dimensions.y,
            pos.z + dimensions.z,
            colorComponents,
            1f,
            lineWidth,
            throughWalls
        )
    }

    fun renderOutline(
        context: WorldRenderContext,
        box: Box,
        colorComponents: FloatArray,
        lineWidth: Float,
        throughWalls: Boolean
    ) {
        renderOutline(context, box, colorComponents, 1f, lineWidth, throughWalls)
    }

    fun renderOutline(
        context: WorldRenderContext,
        box: Box,
        colorComponents: FloatArray,
        alpha: Float,
        lineWidth: Float,
        throughWalls: Boolean
    ) {
        renderOutline(
            context,
            box.minX,
            box.minY,
            box.minZ,
            box.maxX,
            box.maxY,
            box.maxZ,
            colorComponents,
            alpha,
            lineWidth,
            throughWalls
        )
    }

    fun renderOutline(
        context: WorldRenderContext,
        minX: Double,
        minY: Double,
        minZ: Double,
        maxX: Double,
        maxY: Double,
        maxZ: Double,
        colorComponents: FloatArray,
        alpha: Float,
        lineWidth: Float,
        throughWalls: Boolean
    ) {
        if (isVisible(minX, minY, minZ, maxX, maxY, maxZ)) {
            val matrices = context.matrixStack()
            val camera = context.camera().getPos()

            matrices!!.push()
            matrices.translate(-camera.getX(), -camera.getY(), -camera.getZ())

            val consumers = context.consumers() as Immediate?
            val layer: RenderLayer? =
                if (throughWalls) getLinesThroughWalls(lineWidth.toDouble()) else getLines(lineWidth.toDouble())
            val buffer = consumers!!.getBuffer(layer)

            VertexRendering.drawBox(
                matrices,
                buffer,
                minX,
                minY,
                minZ,
                maxX,
                maxY,
                maxZ,
                colorComponents[0],
                colorComponents[1],
                colorComponents[2],
                alpha
            )
            consumers.draw(layer)

            matrices.pop()
        }
    }

    /**
     * Draws lines from point to point.<br></br><br></br>
     *
     *
     * Tip: To draw lines from the center of a block, offset the X, Y and Z each by 0.5
     *
     *
     * Note: This is super messed up when drawing long lines. Tried different normals and [DrawMode.LINES] but nothing worked.
     *
     * @param context         The WorldRenderContext which supplies the matrices and tick delta
     * @param points          The points from which to draw lines between
     * @param colorComponents An array of R, G and B color components
     * @param alpha           The alpha of the lines
     * @param lineWidth       The width of the lines
     * @param throughWalls    Whether to render through walls or not
     */
    fun renderLinesFromPoints(
        context: WorldRenderContext,
        points: Array<Vec3d>,
        colorComponents: FloatArray,
        alpha: Float,
        lineWidth: Float,
        throughWalls: Boolean
    ) {
        val camera = context.camera().getPos()
        val matrices = context.matrixStack()

        matrices!!.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)

        val entry = matrices.peek()

        val consumers = context.consumers() as Immediate?
        val layer: RenderLayer? =
            if (throughWalls) getLinesThroughWalls(lineWidth.toDouble()) else getLines(lineWidth.toDouble())
        val buffer = consumers!!.getBuffer(layer)

        for (i in points.indices) {
            val nextPoint = points[if (i + 1 == points.size) i - 1 else i + 1]
            val normalVec = nextPoint.toVector3f()
                .sub(points[i].getX().toFloat(), points[i].getY().toFloat(), points[i].getZ().toFloat()).normalize()
            // If the last point, the normal is the previous point minus the current point.
            // Negate the normal to make it point forward, away from the previous point.
            if (i + 1 == points.size) {
                normalVec.negate()
            }

            buffer
                .vertex(entry, points[i].getX().toFloat(), points[i].getY().toFloat(), points[i].getZ().toFloat())
                .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
                .normal(entry, normalVec)
        }

        consumers.draw(layer)
        matrices.pop()
    }

    fun renderLineFromCursor(
        context: WorldRenderContext,
        point: Vec3d,
        colorComponents: FloatArray,
        alpha: Float,
        lineWidth: Float
    ) {
        val camera = context.camera().getPos()
        val matrices = context.matrixStack()

        matrices!!.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)

        val entry = matrices.peek()

        val consumers = context.consumers() as Immediate?
        val layer: RenderLayer? = getLinesThroughWalls(lineWidth.toDouble())
        val buffer = consumers!!.getBuffer(layer)

        // Start drawing the line from a point slightly in front of the camera
        val cameraPoint = camera.add(Vec3d.fromPolar(context.camera().getPitch(), context.camera().getYaw()))
        val normal = point.toVector3f().sub(cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat())
            .normalize()

        buffer
            .vertex(entry, cameraPoint.x.toFloat(), cameraPoint.y.toFloat(), cameraPoint.z.toFloat())
            .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
            .normal(entry, normal)

        buffer
            .vertex(entry, point.getX().toFloat(), point.getY().toFloat(), point.getZ().toFloat())
            .color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
            .normal(entry, normal)

        consumers.draw(layer)
        matrices.pop()
    }

    fun renderQuad(
        context: WorldRenderContext,
        points: Array<Vec3d?>,
        colorComponents: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        val positionMatrix = Matrix4f()
        val camera = context.camera().getPos()

        positionMatrix.translate(-camera.x.toFloat(), -camera.y.toFloat(), -camera.z.toFloat())

        val consumers = context.consumers() as Immediate?
        val layer: RenderLayer = if (throughWalls) StellaRenderLayers.QUADS_THROUGH_WALLS else StellaRenderLayers.QUADS
        val buffer = consumers!!.getBuffer(layer)

        for (i in 0..3) {
            buffer.vertex(
                positionMatrix,
                points[i]!!.getX().toFloat(),
                points[i]!!.getY().toFloat(),
                points[i]!!.getZ().toFloat()
            ).color(colorComponents[0], colorComponents[1], colorComponents[2], alpha)
        }

        consumers.draw(layer)
    }

    /**
     * Renders a texture in world space facing the player (like a name tag)
     * @param context world render context
     * @param pos world position
     * @param width rendered width
     * @param height rendered height
     * @param textureWidth amount of texture rendered width
     * @param textureHeight amount of texture rendered height
     * @param renderOffset offset once it's been placed in the world facing the player
     * @param texture reference to texture to render
     * @param shaderColor color to apply to the texture (use white if none)
     * @param throughWalls if it should render though walls
     */
    fun renderTextureInWorld(
        context: WorldRenderContext,
        pos: Vec3d,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float,
        renderOffset: Vec3d,
        texture: Identifier?,
        shaderColor: FloatArray,
        alpha: Float,
        throughWalls: Boolean
    ) {
        val positionMatrix = Matrix4f()
        val camera = context.camera()
        val cameraPos = camera.getPos()

        positionMatrix
            .translate(
                (pos.getX() - cameraPos.getX()).toFloat(),
                (pos.getY() - cameraPos.getY()).toFloat(),
                (pos.getZ() - cameraPos.getZ()).toFloat()
            )
            .rotate(camera.getRotation())

        val consumers = context.consumers() as Immediate?
        val layer: RenderLayer? = if (throughWalls) getTextureThroughWalls(texture) else getTexture(texture)
        val buffer = consumers!!.getBuffer(layer)

        val color = ColorHelper.fromFloats(alpha, shaderColor[0], shaderColor[1], shaderColor[2])

        buffer.vertex(
            positionMatrix,
            renderOffset.getX().toFloat(),
            renderOffset.getY().toFloat(),
            renderOffset.getZ().toFloat()
        ).texture(1f, 1 - textureHeight).color(color)
        buffer.vertex(
            positionMatrix,
            renderOffset.getX().toFloat(),
            renderOffset.getY().toFloat() + height,
            renderOffset.getZ().toFloat()
        ).texture(1f, 1f).color(color)
        buffer.vertex(
            positionMatrix,
            renderOffset.getX().toFloat() + width,
            renderOffset.getY().toFloat() + height,
            renderOffset.getZ().toFloat()
        ).texture(1 - textureWidth, 1f).color(color)
        buffer.vertex(
            positionMatrix,
            renderOffset.getX().toFloat() + width,
            renderOffset.getY().toFloat(),
            renderOffset.getZ().toFloat()
        ).texture(1 - textureWidth, 1 - textureHeight).color(color)

        consumers.draw(layer)
    }

    fun renderText(context: WorldRenderContext, text: Text, pos: Vec3d, throughWalls: Boolean) {
        renderText(context, text, pos, 1f, throughWalls)
    }

    fun renderText(context: WorldRenderContext, text: Text, pos: Vec3d, scale: Float, throughWalls: Boolean) {
        renderText(context, text, pos, scale, 0f, throughWalls)
    }

    fun renderText(
        context: WorldRenderContext,
        text: Text,
        pos: Vec3d,
        scale: Float,
        yOffset: Float,
        throughWalls: Boolean
    ) {
        renderText(context, text.asOrderedText(), pos, scale, yOffset, throughWalls)
    }

    /**
     * Renders text in the world space.
     *
     * @param throughWalls whether the text should be able to be seen through walls or not.
     */
    fun renderText(
        context: WorldRenderContext,
        text: OrderedText?,
        pos: Vec3d,
        scale: Float,
        yOffset: Float,
        throughWalls: Boolean
    ) {
        var scale = scale
        val positionMatrix = Matrix4f()
        val camera = context.camera()
        val cameraPos = camera.getPos()
        val textRenderer = CLIENT.textRenderer

        scale *= 0.025f

        positionMatrix
            .translate(
                (pos.getX() - cameraPos.getX()).toFloat(),
                (pos.getY() - cameraPos.getY()).toFloat(),
                (pos.getZ() - cameraPos.getZ()).toFloat()
            )
            .rotate(camera.getRotation())
            .scale(scale, -scale, scale)

        val xOffset = -textRenderer.getWidth(text) / 2f

        val consumers = VertexConsumerProvider.immediate(ALLOCATOR)

        textRenderer.draw(
            text,
            xOffset,
            yOffset,
            -0x1,
            false,
            positionMatrix,
            consumers,
            if (throughWalls) TextRenderer.TextLayerType.SEE_THROUGH else TextRenderer.TextLayerType.NORMAL,
            0,
            LightmapTextureManager.MAX_LIGHT_COORDINATE
        )
        consumers.draw()
    }

    /**
     * Renders a cylinder without the top or bottom faces.
     *
     * @param pos      The position that the cylinder will be centred around.
     * @param height   The total height of the cylinder with `pos` as the midpoint.
     * @param segments The amount of triangles used to approximate the circle.
     */
    fun renderCylinder(
        context: WorldRenderContext,
        centre: Vec3d,
        radius: Float,
        height: Float,
        segments: Int,
        color: Int
    ) {
        val matrices = context.matrixStack()
        val camera = context.camera().getPos()

        matrices!!.push()
        matrices.translate(-camera.x, -camera.y, -camera.z)

        val buffer = context.consumers()!!.getBuffer(StellaRenderLayers.CYLINDER)
        val positionMatrix = matrices.peek().getPositionMatrix()
        val halfHeight = height / 2.0f

        for (i in 0..segments) {
            val angle = Math.TAU * i / segments
            val dx = cos(angle).toFloat() * radius
            val dz = sin(angle).toFloat() * radius

            buffer.vertex(
                positionMatrix,
                centre.getX().toFloat() + dx,
                centre.getY().toFloat() + halfHeight,
                centre.getZ().toFloat() + dz
            ).color(color)
            buffer.vertex(
                positionMatrix,
                centre.getX().toFloat() + dx,
                centre.getY().toFloat() - halfHeight,
                centre.getZ().toFloat() + dz
            ).color(color)
        }

        matrices.pop()
    }

    /**
     * This is called after all [WorldRenderEvents.AFTER_TRANSLUCENT] listeners have been called so that we can draw all remaining render layers.
     */
    private fun drawTranslucents(context: WorldRenderContext) {
        val profiler = Profilers.get()

        profiler.push("skyblockerTranslucentDraw")
        val immediate = context.consumers() as Immediate?

        //Draw all render layers that haven't been drawn yet - drawing a specific layer does nothing and idk why (IF bug maybe?)
        immediate!!.draw()
        profiler.pop()
    }

    fun runOnRenderThread(runnable: Runnable) {
        if (RenderSystem.isOnRenderThread()) {
            runnable.run()
        } else {
            CLIENT.execute(runnable)
        }
    }

    /**
     * Retrieves the bounding box of a block in the world.
     *
     * @param world The client world.
     * @param pos   The position of the block.
     * @return The bounding box of the block.
     */
    fun getBlockBoundingBox(world: ClientWorld, pos: BlockPos?): Box? {
        return getBlockBoundingBox(world, world.getBlockState(pos), pos)
    }

    fun getBlockBoundingBox(world: ClientWorld?, state: BlockState, pos: BlockPos?): Box? {
        return state.getOutlineShape(world, pos).asCuboid().getBoundingBox().offset(pos)
    }

    private fun playNotificationSound() {
        if (CLIENT.player != null) {
            CLIENT.player!!.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 0.1f)
        }
    }

    fun pointIsInArea(x: Double, y: Double, x1: Double, y1: Double, x2: Double, y2: Double): Boolean {
        return x >= x1 && x <= x2 && y >= y1 && y <= y2
    }

    fun renderNineSliceColored(
        context: DrawContext,
        texture: Identifier?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        argb: Int
    ) {
        context.drawGuiTexture(
            Function { texture: Identifier? -> RenderLayer.getGuiTextured(texture) },
            texture,
            x,
            y,
            width,
            height,
            argb
        )
    }

    fun renderNineSliceColored(
        context: DrawContext,
        texture: Identifier?,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        color: Color
    ) {
        renderNineSliceColored(
            context,
            texture,
            x,
            y,
            width,
            height,
            ColorHelper.getArgb(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue())
        )
    }

    fun drawHorizontalGradient(
        context: DrawContext,
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float,
        colorStart: Int,
        colorEnd: Int
    ) {
        context.draw(Consumer { provider: VertexConsumerProvider? ->
            val vertexConsumer = provider!!.getBuffer(RenderLayer.getGui())
            val positionMatrix = context.getMatrices().peek().getPositionMatrix()
            vertexConsumer.vertex(positionMatrix, startX, startY, 0f).color(colorStart)
            vertexConsumer.vertex(positionMatrix, startX, endY, 0f).color(colorStart)
            vertexConsumer.vertex(positionMatrix, endX, endY, 0f).color(colorEnd)
            vertexConsumer.vertex(positionMatrix, endX, startY, 0f).color(colorEnd)
        })
    }
}