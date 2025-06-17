package co.stellarskys.stella.utils

import it.unimi.dsi.fastutil.doubles.Double2ObjectMap
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderLayer.MultiPhase
import net.minecraft.client.render.RenderLayer.MultiPhaseParameters
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.RenderPhase.LineWidth
import net.minecraft.util.Identifier
import net.minecraft.util.TriState
import java.util.*
import java.util.function.DoubleFunction
import java.util.function.Function


object StellaRenderLayers {
    private val LINES_LAYERS: Double2ObjectMap<MultiPhase?> = Double2ObjectOpenHashMap<MultiPhase?>()
    private val LINES_THROUGH_WALLS_LAYERS: Double2ObjectMap<MultiPhase?> = Double2ObjectOpenHashMap<MultiPhase?>()
    private val TEXTURE_LAYERS: Object2ObjectMap<Identifier?, MultiPhase?> =
        Object2ObjectOpenHashMap<Identifier?, MultiPhase?>()
    private val TEXTURE_THROUGH_WALLS_LAYERS: Object2ObjectMap<Identifier?, MultiPhase?> =
        Object2ObjectOpenHashMap<Identifier?, MultiPhase?>()

    val FILLED: MultiPhase = RenderLayer.of(
        "filled",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        RenderPipelines.DEBUG_FILLED_BOX,
        MultiPhaseParameters.builder()
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false)
    )

    val FILLED_THROUGH_WALLS: MultiPhase = RenderLayer.of(
        "filled_through_walls",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        StellaRenderPipelines.FILLED_THROUGH_WALLS,
        MultiPhaseParameters.builder()
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false)
    )

    private val LINES = DoubleFunction { lineWidth: Double ->
        RenderLayer.of(
            "lines",
            RenderLayer.DEFAULT_BUFFER_SIZE,
            false,
            false,
            RenderPipelines.LINES,
            MultiPhaseParameters.builder()
                .lineWidth(LineWidth(OptionalDouble.of(lineWidth)))
                .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                .build(false)
        )
    }

    private val LINES_THROUGH_WALLS = DoubleFunction { lineWidth: Double ->
        RenderLayer.of(
            "lines_through_walls",
            RenderLayer.DEFAULT_BUFFER_SIZE,
            false,
            false,
            StellaRenderPipelines.LINES_THROUGH_WALLS,
            MultiPhaseParameters.builder()
                .lineWidth(LineWidth(OptionalDouble.of(lineWidth)))
                .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                .build(false)
        )
    }

    val QUADS: MultiPhase = RenderLayer.of(
        "quad",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        RenderPipelines.DEBUG_QUADS,
        MultiPhaseParameters.builder()
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false)
    )

    val QUADS_THROUGH_WALLS: MultiPhase = RenderLayer.of(
        "quad_through_walls",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        StellaRenderPipelines.QUADS_THROUGH_WALLS,
        MultiPhaseParameters.builder()
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false)
    )

    private val TEXTURE = Function { texture: Identifier? ->
        RenderLayer.of(
            "texture",
            RenderLayer.DEFAULT_BUFFER_SIZE,
            false,
            true,
            StellaRenderPipelines.TEXTURE,
            MultiPhaseParameters.builder()
                .texture(RenderPhase.Texture(texture, TriState.FALSE, false))
                .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                .build(false)
        )
    }

    private val TEXTURE_THROUGH_WALLS = Function { texture: Identifier? ->
        RenderLayer.of(
            "texture_through_walls",
            RenderLayer.DEFAULT_BUFFER_SIZE,
            false,
            true,
            StellaRenderPipelines.TEXTURE_THROUGH_WALLS,
            MultiPhaseParameters.builder()
                .texture(RenderPhase.Texture(texture, TriState.FALSE, false))
                .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
                .build(false)
        )
    }
    val CYLINDER: MultiPhase = RenderLayer.of(
        "cylinder",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        StellaRenderPipelines.CYLINDER,
        MultiPhaseParameters.builder()
            .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
            .build(false)
    )

    fun getLines(lineWidth: Double): MultiPhase? {
        return LINES_LAYERS.computeIfAbsent(lineWidth, LINES)
    }

    fun getLinesThroughWalls(lineWidth: Double): MultiPhase? {
        return LINES_THROUGH_WALLS_LAYERS.computeIfAbsent(lineWidth, LINES_THROUGH_WALLS)
    }

    fun getTexture(texture: Identifier?): MultiPhase? {
        return TEXTURE_LAYERS.computeIfAbsent(texture, TEXTURE)
    }

    fun getTextureThroughWalls(texture: Identifier?): MultiPhase? {
        return TEXTURE_THROUGH_WALLS_LAYERS.computeIfAbsent(texture, TEXTURE_THROUGH_WALLS)
    }
}