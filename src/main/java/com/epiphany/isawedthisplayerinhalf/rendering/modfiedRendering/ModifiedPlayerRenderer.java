package com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering;

import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
