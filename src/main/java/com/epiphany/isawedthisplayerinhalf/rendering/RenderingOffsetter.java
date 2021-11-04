package com.epiphany.isawedthisplayerinhalf.rendering;

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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Contains various functions to offset the rendering of players.
 */
@OnlyIn(Dist.CLIENT)
public class RenderingOffsetter {
    private static final Field FIELD_skinMap;
    private static final Field FIELD_playerRenderer;

    static {
        FIELD_skinMap = ReflectionHelper.getDeclaredFieldOrNull(EntityRendererManager.class, "skinMap", "field_178636_l");
        ReflectionHelper.makeAccessible(FIELD_skinMap);
        FIELD_playerRenderer = ReflectionHelper.getDeclaredFieldOrNull(EntityRendererManager.class, "playerRenderer", "field_178637_m");
        ReflectionHelper.makeAccessible(FIELD_playerRenderer);

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
        PlayerRenderer newDefaultRenderer = new ModifiedPlayerRenderer(entityRendererManager, false);

        Map<String, PlayerRenderer> skinMap = (Map<String, PlayerRenderer>) ReflectionHelper.getValueOrDefault(FIELD_skinMap, entityRendererManager, null);
        if (skinMap == null) throw new NullPointerException("Unable to get value from 'FIELD_skinMap'");

        skinMap.replace("default", newDefaultRenderer);
        skinMap.replace("slim", new ModifiedPlayerRenderer(entityRendererManager, true));
        ReflectionHelper.setValue(FIELD_playerRenderer, entityRendererManager, newDefaultRenderer);
    }



    private static final HashMap<UUID, RenderingOffsets> renderingOffsetsMap = new HashMap<>();

    /**
     * Calculates and assigns rendering offset information to a player.
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
     * Removes the offsets of the player with the given UUID.
     *
     * @param playerUUID The UUID of the player.
     */
    public static void unsetOffsets(UUID playerUUID) {
        renderingOffsetsMap.remove(playerUUID);
    }

    public static void clearAllOffsets() {
        renderingOffsetsMap.clear();
    }
}
