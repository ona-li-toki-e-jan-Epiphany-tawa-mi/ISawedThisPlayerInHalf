package com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering;

import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsets;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Common methods used in the modified models.
 */
public class ModifiedModelCommon {
    /**
     * Calculates and interpolates the angle required to cancel out the player's offset and rotate the upper body into position.
     *
     * @param ageInTicks The age in ticks of the entity supplied by setRotationAngles().
     * @param entity The entity (player) to calculate the angles for.
     * @param renderingOffsets The rendering offsets that the player has.
     *
     * @return The angle required to cancel out the player's offset and rotate the upper body into position.
     */
    static float calculateNetYawOffset(float ageInTicks, LivingEntity entity, RenderingOffsets renderingOffsets) {
        final float DEGREES_TO_RADIANS = (float) (Math.PI / 180.0);

        return MathHelper.lerp(ageInTicks - entity.ticksExisted, entity.prevRenderYawOffset, entity.renderYawOffset) * DEGREES_TO_RADIANS
                + renderingOffsets.getYawOffset();
    }

    /**
     * Calculates the offset position on the x-axis of the upper body.
     *
     * @param renderingOffsets The rendering offsets that the player has.
     * @param offsetSin The sin of the player's net yaw offset.
     * @param offsetCos The cos of the player's net yaw offset.
     *
     * @return The offset position on the x-axis of the upper body.
     */
    static float calculatePointX(RenderingOffsets renderingOffsets, float offsetSin, float offsetCos) {
        return renderingOffsets.getXOffset() * offsetCos - renderingOffsets.getZOffset() * offsetSin;
    }

    /**
     * Calculates the offset position on the z-axis of the upper body.
     *
     * @param renderingOffsets The rendering offsets that the player has.
     * @param offsetSin The sin of the player's net yaw offset.
     * @param offsetCos The cos of the player's net yaw offset.
     *
     * @return The offset position on the z-axis of the upper body.
     */
    static float calculatePointZ(RenderingOffsets renderingOffsets, float offsetSin, float offsetCos) {
        return renderingOffsets.getXOffset() * offsetSin + renderingOffsets.getZOffset() * offsetCos;
    }
}
