package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.epiphany.isawedthisplayerinhalf.helpers.ReflectionHelper;
import com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering.ModifiedPlayerRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.entity.Entity;
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
        float xOffset, yOffset, zOffset;
        float yawOffset;

        if (!offsets.equals(Vec3d.ZERO)) {
            xOffset = (float) (offsets.x * 16);
            yOffset = (float) (offsets.y * -16);
            zOffset = (float) (offsets.z * 16);
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



    /**
     * Gets whether the arm of the player should be rendered.
     * Used for the first person renderer.
     *
     * @return Whether the arm of the player should be rendered.
     */
    public static boolean shouldRenderHand() {
        return Offsetter.getOffsets(Minecraft.getInstance().player).equals(Vec3d.ZERO);
    }

    /**
     * Gets whether the game is in third-person.
     * Overrides normal behavior if the player has an offset.
     *
     * @param activeRenderInfo The active render info of the calling renderer.
     *
     * @return Whether the game is in third-person.
     */
    public static boolean modifiedIsThirdPerson(ActiveRenderInfo activeRenderInfo) {
        return activeRenderInfo.isThirdPerson() || !Offsetter.getOffsets(Minecraft.getInstance().player).equals(Vec3d.ZERO);
    }

    /**
     * Gets whether the entity is within range to render.
     *
     * @param entity The entity to test.
     * @param x The x-coordinate of the camera.
     * @param y The y-coordinate of the camera.
     * @param z The z-coordinate of the camera.
     * @return Whether the entity is within range to render.
     */
    public static boolean modifiedIsInRangeToRender3d(Entity entity, double x, double y, double z) {
        Vec3d entityOffsets = Offsetter.getOffsets(entity);
        return entity.isInRangeToRender3d(x, y, z) || (!entityOffsets.equals(Vec3d.ZERO) && entity.isInRangeToRender3d(x - entityOffsets.x, y - entityOffsets.y, z - entityOffsets.z));
    }
}
