package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.epiphany.isawedthisplayerinhalf.ReflectionHelper;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

/**
 * Offsets the rendering of players by swapping out their normal renderer with a modified version.
 */
@OnlyIn(Dist.CLIENT)
public class RenderingOffsetter {
    private static final HashMap<UUID, PlayerRendererWrapper> wrappedRendererMap = new HashMap<>();
    private static Field renderer;

    public static void doClientStuff() {
        renderer = ReflectionHelper.getFieldOrNull(RenderPlayerEvent.class, "renderer");
        ReflectionHelper.makeAccessible(renderer);
    }

    /**
     * Gets the renderer assigned to the given player.
     *
     * @param player The player to get the renderer of.
     *
     * @return The renderer for the given player.
     */
    public static PlayerRendererWrapper getRenderer(PlayerEntity player) {
        return wrappedRendererMap.get(player.getUniqueID());
    }



    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onPlayerPreRender(RenderPlayerEvent.Pre renderPlayerEvent) {
        PlayerEntity player = renderPlayerEvent.getPlayer();
        UUID playerUUID = player.getUniqueID();
        PlayerRendererWrapper wrappedRenderer;

        // Gets the renderer to be used instead of the normal one.
        if (!wrappedRendererMap.containsKey(playerUUID)) {
            PlayerRenderer playerRenderer = renderPlayerEvent.getRenderer();
            PlayerModel<AbstractClientPlayerEntity> playerModel = playerRenderer.getEntityModel();
            boolean useSmallArms = (boolean) ReflectionHelper.getFieldOrDefault(playerModel.getClass(), playerModel, "smallArms", false);

            wrappedRenderer = new PlayerRendererWrapper(playerRenderer.getRenderManager(), useSmallArms, Offsetter.getOffsets(player));
            wrappedRendererMap.put(playerUUID, wrappedRenderer);

        } else
            wrappedRenderer = wrappedRendererMap.get(playerUUID);

        // Swaps out renderers, renders modified player model.
        wrappedRenderer.render((AbstractClientPlayerEntity) renderPlayerEvent.getPlayer(), renderPlayerEvent.getEntity().rotationYaw, renderPlayerEvent.getPartialRenderTick(), renderPlayerEvent.getMatrixStack(), renderPlayerEvent.getBuffers(), renderPlayerEvent.getLight());
        ReflectionHelper.setField(renderer, renderPlayerEvent, wrappedRenderer.wrappedRenderer);

        renderPlayerEvent.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onPlayerPostRender(RenderPlayerEvent.Post renderPlayerEvent) {
        ReflectionHelper.setField(renderer, renderPlayerEvent, wrappedRendererMap.get(renderPlayerEvent.getPlayer().getUniqueID()));
    }
}
