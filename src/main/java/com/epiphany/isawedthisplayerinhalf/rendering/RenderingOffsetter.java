package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.helpers.ReflectionHelper;
import com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering.ModifiedPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
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
    public static final HashMap<UUID, RenderingOffsets> renderingOffsetsMap = new HashMap<>();

    private static final Field skinMapField;
    private static final Field playerRendererField;

    static {
        skinMapField = ReflectionHelper.getFieldOrNull(EntityRendererManager.class, "skinMap", "field_178636_l");
        ReflectionHelper.makeAccessible(skinMapField);
        playerRendererField = ReflectionHelper.getFieldOrNull(EntityRendererManager.class, "playerRenderer", "field_178637_m");
        ReflectionHelper.makeAccessible(playerRendererField);

        if (skinMapField == null)
            throw new NullPointerException("Unable to find field 'skinMapField' under names 'skinMap' and 'field_178636_l'");
        if (playerRendererField == null)
            throw new NullPointerException("Unable to find field 'playerRendererField' under names 'playerRenderer' and 'field_178637_m'");
    }



    /**
     * Replaces the two player renderers in EntityRendererManager with modified variants that render the split player models.
     */
    public static void replacePlayerRenderers() {
        EntityRendererManager entityRendererManager = Minecraft.getInstance().getRenderManager();
        Map<String, PlayerRenderer> skinMap = (Map<String, PlayerRenderer>) ReflectionHelper.getFieldOrDefault(skinMapField, entityRendererManager, null);

        if (skinMap != null) {
            PlayerRenderer newDefaultRenderer = new ModifiedPlayerRenderer(entityRendererManager, false);

            skinMap.replace("default", newDefaultRenderer);
            skinMap.replace("slim", new ModifiedPlayerRenderer(entityRendererManager, true));
            ReflectionHelper.setField(playerRendererField, entityRendererManager, newDefaultRenderer);

        } else
            throw new NullPointerException("Unable to acquire value of field 'skinMap' from EntityRendererManager");
    }


    /**
     * Calculates and assigns rendering offset information to a player.
     *
     * @param playerUUID The UUID of the player to set the offsets for.
     * @param offsets The physical offsets of the player's body.
     */
    public static void setOffsets(UUID playerUUID, Vec3d offsets) {
        final double physicalToModelCoordinates = 17.0660750427;

        float xOffset, yOffset, zOffset;
        float yawOffset;

        if (!offsets.equals(Vec3d.ZERO)) {
            xOffset = (float) (offsets.x * physicalToModelCoordinates);
            yOffset = (float) (offsets.y * -physicalToModelCoordinates);
            zOffset = (float) (offsets.z * physicalToModelCoordinates);
            // Angle needs to be multiplied by two for whatever reason.
            yawOffset = (float) (Math.atan2(-offsets.z, offsets.x) * 2);

        } else {
            xOffset = 0;
            yOffset = 0;
            zOffset = 0;
            yawOffset = 0;
        }

        renderingOffsetsMap.put(playerUUID, new RenderingOffsets(xOffset, yOffset, zOffset, yawOffset));
    }
}
