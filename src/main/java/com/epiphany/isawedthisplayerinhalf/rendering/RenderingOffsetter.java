package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.epiphany.isawedthisplayerinhalf.helpers.ReflectionHelper;
import com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering.ModifiedPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
 * MIT License
 *
 * Copyright (c) 2020-2022 ona-li-toki-e-jan-Epiphany-tawa-mi
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
 * Contains various functions to offset the rendering of players.
 */
@OnlyIn(Dist.CLIENT)
public class RenderingOffsetter {
    // EntityRendererManager.
    private static final Field FIELD_skinMap;
    private static final Field FIELD_playerRenderer;

    static {
        FIELD_skinMap = ReflectionHelper.getDeclaredFieldOrNull(EntityRendererManager.class, "skinMap", "field_178636_l");
        FIELD_playerRenderer = ReflectionHelper.getDeclaredFieldOrNull(EntityRendererManager.class, "playerRenderer", "field_178637_m");

        if (FIELD_skinMap == null)
            throw new NullPointerException("Unable to find field 'FIELD_skinMap' under names 'skinMap' and 'field_178636_l'");
        if (FIELD_playerRenderer == null)
            throw new NullPointerException("Unable to find field 'FIELD_playerRenderer' under names 'playerRenderer' and 'field_178637_m'");
    }

    /**
     * Replaces the two player renderers in EntityRendererManager with modified variants that render the split player models.
     */
    public static void replacePlayerRenderers() {
        EntityRendererManager entityRendererManager = Minecraft.getInstance().getRenderManager();

        Map<String, PlayerRenderer> skinMap = (Map<String, PlayerRenderer>) ReflectionHelper.getValueOrDefault(FIELD_skinMap, entityRendererManager, null);
        if (skinMap == null) throw new NullPointerException("Unable to get value from 'FIELD_skinMap'");

        PlayerRenderer newDefaultRenderer = new ModifiedPlayerRenderer(entityRendererManager, false);
        skinMap.replace("default", newDefaultRenderer);
        skinMap.replace("slim", new ModifiedPlayerRenderer(entityRendererManager, true));
        ReflectionHelper.setValue(FIELD_playerRenderer, entityRendererManager, newDefaultRenderer);
    }



    private static final ConcurrentHashMap<UUID, RenderingOffsets> renderingOffsetsMap = new ConcurrentHashMap<>();

    /**
     * Returns the rendering offsets an entity has, or null, if it has none.
     *
     * @param entity The entity to get the offsets of.
     *
     * @return The entity's offsets.
     */
    public static RenderingOffsets getOffsetsOrNull(Entity entity) {
        return entity instanceof PlayerEntity ? renderingOffsetsMap.get(entity.getUniqueID()) : null;
    }

    /**
     * Calculates and assigns rendering offset information to a player.
     *
     * Do not run this method directly; use {@link com.epiphany.isawedthisplayerinhalf.Offsetter#setOffsets(PlayerEntity, Vec3d)} instead.
     *
     * @param playerEntity The player to set the offsets for.
     * @param offsets The physical offsets of the player's body.
     */
    public static void setOffsets(PlayerEntity playerEntity, Vec3d offsets) {
        final double PHYSICAL_TO_RENDERING_COORDINATES = 17.0660750427;

        float xOffset, yOffset, zOffset;
        float yawOffset;

        if (!offsets.equals(Vec3d.ZERO)) {
            xOffset = (float) (offsets.x * PHYSICAL_TO_RENDERING_COORDINATES);
            yOffset = (float) (offsets.y * -PHYSICAL_TO_RENDERING_COORDINATES);
            zOffset = (float) (offsets.z * PHYSICAL_TO_RENDERING_COORDINATES);
            // Angle needs to be multiplied by two for whatever reason.
            yawOffset = (float) (Math.atan2(-offsets.z, offsets.x) * 2);

        } else {
            xOffset = 0;
            yOffset = 0;
            zOffset = 0;
            yawOffset = 0;
        }

        renderingOffsetsMap.put(playerEntity.getUniqueID(), new RenderingOffsets(xOffset, yOffset, zOffset, yawOffset));
    }

    /**
     * Removes the offsets of the player with the given UUID.
     *
     * Do not run this method directly; use {@link com.epiphany.isawedthisplayerinhalf.Offsetter#unsetOffsets(PlayerEntity)} instead.
     *
     * @param playerUUID The UUID of the player.
     */
    public static void unsetOffsets(UUID playerUUID) {
        renderingOffsetsMap.remove(playerUUID);
    }

    /**
     * Clears the entire map of player offsets.
     *
     * Do not run this method directly; use {@link Offsetter#clearAllOffsets()} instead.
     */
    public static void clearAllOffsets() {
        renderingOffsetsMap.clear();
    }
}
