package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.ReflectionHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Method;
import java.util.List;

/**
 * A semi-wrapper, semi-renderer for players. Used in lieu of PlayerRenderer for custom models.
 */
class PlayerRendererWrapper extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    /**
     * Conversion value for degrees to radians conversion.
     */
    static final float degrees2Radians = (float) (Math.PI / 180.0);

    private final PlayerRenderer wrappedRenderer;
    private final ModifiedBipedModel upperArmorModel;
    // Reflected methods grabbed from PlayerRenderer.
    private final Method setModelVisibilities;
    private final Method applyRotations;
    private final Method preRenderCallback;
    private final Method renderName;

    /**
     * Creates a new wrapped player renderer.
     *
     * @param playerOffsets A 3d vector representing the initial offsets a player has.
     */
    PlayerRendererWrapper(EntityRendererManager renderManager, boolean useSmallArms, Vec3d playerOffsets) {
        this(renderManager, useSmallArms, new ModifiedPlayerModel<>(0.0F, useSmallArms), playerOffsets);
    }

    private PlayerRendererWrapper(EntityRendererManager renderManager, boolean useSmallArms, ModifiedPlayerModel<AbstractClientPlayerEntity> playerModel, Vec3d playerOffsets) {
        super(renderManager, playerModel, 0.5F);
        wrappedRenderer = new PlayerRenderer(renderManager, useSmallArms);
        ReflectionHelper.setField(LivingRenderer.class, wrappedRenderer, "entityModel", playerModel);

        // Modifies the layer renderers of the wrapped class, and then copies it into this class.
        upperArmorModel = new ModifiedBipedModel(1.0F);
        List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> wrappedLayers = (List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>>)
                ReflectionHelper.getFieldOrDefault(LivingRenderer.class, wrappedRenderer, "layerRenderers", null);

        if (wrappedLayers != null) {
            for (int i = 0; i < wrappedLayers.size(); i++)
                if (wrappedLayers.get(i) instanceof BipedArmorLayer) {
                    wrappedLayers.remove(i);
                    wrappedLayers.add(i, new BipedArmorLayer(this, new BipedModel(0.5F), upperArmorModel));

                    break;
                }

            ReflectionHelper.setField(LivingRenderer.class, this, "layerRenderers", wrappedLayers);
        }

        // Grabs methods through reflection
        setModelVisibilities = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "setModelVisibilities", AbstractClientPlayerEntity.class);
        setModelVisibilities.setAccessible(true);
        applyRotations = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "applyRotations", AbstractClientPlayerEntity.class, MatrixStack.class, float.class, float.class, float.class);
        applyRotations.setAccessible(true);
        preRenderCallback = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "preRenderCallback", AbstractClientPlayerEntity.class, MatrixStack.class, float.class);
        preRenderCallback.setAccessible(true);
        renderName = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "renderName", AbstractClientPlayerEntity.class, String.class, MatrixStack.class, IRenderTypeBuffer.class, int.class);
        renderName.setAccessible(true);

        setOffsets(playerOffsets);
    }



    /**
     * A clone of the render method in PlayerRenderer, without the event calls.
     */
    @Override
    public void render(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        setModelVisibilities(entityIn);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    private static float pi2 = (float) Math.PI * 2;
    // Offset to get the yaw to be x-axis + offsetAngle.
    private static float piOver4 = (float) Math.PI / 4;

    /**
     * Sets the offsets for the models within this renderer that are affected by them.
     *
     * @param offsets A 3d vector containing the offsets.
     */
    public void setOffsets(Vec3d offsets) {
        float newXOffset = (float) (offsets.x * 16);
        float newYOffset = (float) (offsets.y * -16);
        float newZOffset = (float) (offsets.z * 16);
        float offsetDistance2d = (float) Math.sqrt(newXOffset * newXOffset + newZOffset * newZOffset);
        float offsetAngle = (float) -(Math.acos(newXOffset / offsetDistance2d) + piOver4);

        if (newXOffset < 0)
            offsetAngle = pi2 - offsetAngle;

        ((ModifiedPlayerModel<?>) entityModel).setOffsets(newXOffset, newYOffset, newZOffset, offsetAngle);
        upperArmorModel.setOffsets(newXOffset, newYOffset, newZOffset, offsetAngle);
    }

    /**
     * Resets certain parts of the models within this renderer.
     */
    void reset() {
        ((ModifiedPlayerModel<?>) entityModel).reset();
        upperArmorModel.reset();
    }



    @Override
    public Vec3d getRenderOffset(AbstractClientPlayerEntity entityIn, float partialTicks) {
        return wrappedRenderer.getRenderOffset(entityIn, partialTicks);
    }

    private void setModelVisibilities(AbstractClientPlayerEntity clientPlayer) {
        ReflectionHelper.invokeMethod(
                wrappedRenderer,
                setModelVisibilities,
                clientPlayer
        );
    }

    @Override
    public ResourceLocation getEntityTexture(AbstractClientPlayerEntity entity) {
        return wrappedRenderer.getEntityTexture(entity);
    }

    @Override
    protected void preRenderCallback(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStack, float partialTickTime) {
        ReflectionHelper.invokeMethod(
                wrappedRenderer,
                preRenderCallback,
                entityLiving, matrixStack, partialTickTime
        );
    }

    @Override
    protected void renderName(AbstractClientPlayerEntity entityIn, String displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        ReflectionHelper.invokeMethod(
                wrappedRenderer,
                renderName,
                entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn
        );
    }

    @Override
    protected void applyRotations(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        ReflectionHelper.invokeMethod(
                wrappedRenderer,
                applyRotations,
                entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks
        );
    }
}