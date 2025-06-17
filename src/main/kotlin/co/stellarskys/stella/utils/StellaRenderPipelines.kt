package co.stellarskys.stella.utils

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.VertexFormat.DrawMode
import co.stellarskys.stella.Stella
import co.stellarskys.stella.annotations.Init
import net.minecraft.client.gl.RenderPipelines
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier

object StellaRenderPipelines {
    /** Similar to [RenderPipelines.DEBUG_FILLED_BOX]  */
    val FILLED_THROUGH_WALLS: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(Stella.NAMESPACE, "pipeline/debug_filled_box_through_walls"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    )

    /** Similar to [RenderPipelines.LINES]  */
    val LINES_THROUGH_WALLS: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(Identifier.of(Stella.NAMESPACE, "pipeline/lines_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .build()
    )

    /** Similar to [RenderPipelines.DEBUG_QUADS]   */
    val QUADS_THROUGH_WALLS: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(Stella.NAMESPACE, "pipeline/debug_quads_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build()
    )

    /** Similar to [RenderPipelines.GUI_TEXTURED]  */
    val TEXTURE: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
            .withLocation(Identifier.of(Stella.NAMESPACE, "pipeline/texture"))
            .withCull(false)
            .build()
    )
    val TEXTURE_THROUGH_WALLS: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
            .withLocation(Identifier.of(Stella.NAMESPACE, "pipeline/texture_through_walls"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(false)
            .build()
    )
    val CYLINDER: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)
            .withLocation(Identifier.of(Stella.NAMESPACE, "pipeline/cylinder"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, DrawMode.TRIANGLE_STRIP)
            .withCull(false)
            .build()
    )

    @Init
    fun init() {
    } //Ensure that pipelines are pre-compiled instead of compiled on demand
}