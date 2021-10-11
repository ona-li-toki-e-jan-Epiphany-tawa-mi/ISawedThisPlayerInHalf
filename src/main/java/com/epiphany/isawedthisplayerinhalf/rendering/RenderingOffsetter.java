package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.epiphany.isawedthisplayerinhalf.helpers.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

/**
 * Contains various functions to offset the rendering of players.
 */
@OnlyIn(Dist.CLIENT)
public class RenderingOffsetter {
    public static final HashMap<UUID, PlayerRendererWrapper> wrappedRendererMap = new HashMap<>();
    private static final Field rendererField;
    private static final Field smallArmsField;

    static {
        rendererField = ReflectionHelper.getFieldOrNull(RenderPlayerEvent.class, "renderer");
        ReflectionHelper.makeAccessible(rendererField);

        smallArmsField = ReflectionHelper.getFieldOrNull(PlayerModel.class, "smallArms", "field_178735_y");
        ReflectionHelper.makeAccessible(rendererField);

        if (rendererField == null)
            throw new NullPointerException("Unable to find field 'rendererField' under name 'renderer'");
        if (smallArmsField == null)
            throw new NullPointerException("Unable to find field 'smallArmsField' under names 'smallArms' and 'field_178735_y'");
    }



    /**
     * Gets whether or not the arm of the player should be rendered.
     * Used for the first person renderer.
     *
     * @return Whether or not the arm of the player should be rendered.
     */
    public static boolean shouldRenderHand() {
        return Offsetter.getOffsets(Minecraft.getInstance().player).equals(Vec3d.ZERO);
    }

    /**
     * Gets whether or not the game is in third-person.
     * Overrides normal behavior if the player has an offset.
     *
     * @param activeRenderInfo The active render info of the calling renderer.
     *
     * @return Whether or not the game is in third-person.
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



    /**
     * Intercepts normal player-rendering and replaces it with the modified form.
     */
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onPlayerPreRender(RenderPlayerEvent.Pre renderPlayerEvent) {
        if (!renderPlayerEvent.isCanceled()) {
            PlayerEntity player = renderPlayerEvent.getPlayer();

            // Only renders with a custom model if the player has an offset.
            if (!Offsetter.getOffsets(player).equals(Vec3d.ZERO)) {
                UUID playerUUID = player.getUniqueID();
                PlayerRendererWrapper wrappedRenderer;

                // Gets the renderer to be used instead of the normal one.
                if (!wrappedRendererMap.containsKey(playerUUID)) {
                    PlayerRenderer playerRenderer = renderPlayerEvent.getRenderer();
                    boolean hasSmallArms = (boolean) ReflectionHelper.getFieldOrDefault(smallArmsField, playerRenderer.getEntityModel(),
                            false);

                    wrappedRenderer = new PlayerRendererWrapper(playerRenderer, new ModifiedPlayerModel<>(0.0f, hasSmallArms),
                            Offsetter.getOffsets(player));
                    wrappedRendererMap.put(playerUUID, wrappedRenderer);

                } else
                    wrappedRenderer = wrappedRendererMap.get(playerUUID);

                // Swaps out renderers, renders modified player model.
                wrappedRenderer.render((AbstractClientPlayerEntity) player, player.rotationYaw, renderPlayerEvent.getPartialRenderTick(),
                        renderPlayerEvent.getMatrixStack(), renderPlayerEvent.getBuffers(), renderPlayerEvent.getLight());
                ReflectionHelper.setField(rendererField, renderPlayerEvent, wrappedRenderer.wrappedRenderer);

                renderPlayerEvent.setCanceled(true);
            }
        }
    }

    /**
     * Swaps out normal renderer for the modified one.
     */
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onPlayerPostRender(RenderPlayerEvent.Post renderPlayerEvent) {
        UUID playerUUID = renderPlayerEvent.getPlayer().getUniqueID();

        if (wrappedRendererMap.containsKey(playerUUID))
            ReflectionHelper.setField(rendererField, renderPlayerEvent, wrappedRendererMap.get(playerUUID).wrappedRenderer);
    }
}
