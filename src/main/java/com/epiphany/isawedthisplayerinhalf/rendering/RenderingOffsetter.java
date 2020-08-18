package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.epiphany.isawedthisplayerinhalf.helpers.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
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
    private static final Field renderer;
    private static final Field smallArms;

    static {
        renderer = ReflectionHelper.getFieldOrNull(RenderPlayerEvent.class, "renderer");
        ReflectionHelper.makeAccessible(renderer);

        smallArms = ReflectionHelper.getFieldOrNull(PlayerModel.class, "smallArms", "field_178735_y");
        ReflectionHelper.makeAccessible(renderer);
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
        return !Offsetter.getOffsets(Minecraft.getInstance().player).equals(Vec3d.ZERO) || activeRenderInfo.isThirdPerson();
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
                    PlayerModel<AbstractClientPlayerEntity> playerModel = playerRenderer.getEntityModel();
                    boolean useSmallArms = (boolean) ReflectionHelper.getFieldOrDefault(smallArms, playerModel, false);

                    wrappedRenderer = new PlayerRendererWrapper(playerRenderer.getRenderManager(), useSmallArms, Offsetter.getOffsets(player));
                    wrappedRendererMap.put(playerUUID, wrappedRenderer);

                } else
                    wrappedRenderer = wrappedRendererMap.get(playerUUID);

                // Swaps out renderers, renders modified player model.
                wrappedRenderer.render((AbstractClientPlayerEntity) renderPlayerEvent.getPlayer(), renderPlayerEvent.getEntity().rotationYaw, renderPlayerEvent.getPartialRenderTick(), renderPlayerEvent.getMatrixStack(), renderPlayerEvent.getBuffers(), renderPlayerEvent.getLight());
                ReflectionHelper.setField(renderer, renderPlayerEvent, wrappedRenderer.wrappedRenderer);

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
            ReflectionHelper.setField(renderer, renderPlayerEvent, wrappedRendererMap.get(playerUUID).wrappedRenderer);
    }
}
