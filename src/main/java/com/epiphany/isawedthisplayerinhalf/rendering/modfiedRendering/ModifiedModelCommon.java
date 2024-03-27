package com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering;

import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsets;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/*
 * MIT License
 *
 * Copyright (c) 2021 ona-li-toki-e-jan-Epiphany-tawa-mi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

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
