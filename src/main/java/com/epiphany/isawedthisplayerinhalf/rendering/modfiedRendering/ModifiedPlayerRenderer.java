package com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A modified version of PlayerRenderer that swaps out the player model and armor layer renderer for modified copies.
 */
@OnlyIn(Dist.CLIENT)
public class ModifiedPlayerRenderer extends PlayerRenderer {
    /**
     * Creates a new ModifiedPlayerRenderer with the modified models.
     *
     * @param renderManager The EntityRendererManager.
     * @param useSmallArms Whether the player has small arms.
     */
    public ModifiedPlayerRenderer(EntityRendererManager renderManager, boolean useSmallArms) {
        super(renderManager, useSmallArms);

        this.entityModel = new ModifiedPlayerModel<>(useSmallArms);

        // Swaps out the armor layer renderer.
        ModifiedBipedModel upperArmorModel = new ModifiedBipedModel<>();
        boolean success = false;

        for (int i = 0; i < layerRenderers.size(); i++)
            if (layerRenderers.get(i) instanceof BipedArmorLayer) {
                layerRenderers.remove(i);
                layerRenderers.add(i, new BipedArmorLayer<>(this, new BipedModel<>(0.5F), upperArmorModel));

                success = true;
                break;
            }

        if (!success)
            throw new NullPointerException("Failed to swap out BipedArmorLayer");
    }
}