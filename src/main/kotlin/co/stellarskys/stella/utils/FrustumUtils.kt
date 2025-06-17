package co.stellarskys.stella.utils

import co.stellarskys.stella.mixin.accessors.FrustumInvoker
import co.stellarskys.stella.mixin.accessors.WorldRendererAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.Frustum
import net.minecraft.util.math.Box
import org.joml.FrustumIntersection

object FrustumUtils {
    val frustum: Frustum
        get() = (MinecraftClient.getInstance().worldRenderer as WorldRendererAccessor).getFrustum()

    fun isVisible(box: Box?): Boolean {
        return frustum.isVisible(box)
    }

    fun isVisible(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double): Boolean {
        val plane: Int = (frustum as FrustumInvoker).invokeIntersectAab(minX, minY, minZ, maxX, maxY, maxZ)

        return plane == FrustumIntersection.INSIDE || plane == FrustumIntersection.INTERSECT
    }
}