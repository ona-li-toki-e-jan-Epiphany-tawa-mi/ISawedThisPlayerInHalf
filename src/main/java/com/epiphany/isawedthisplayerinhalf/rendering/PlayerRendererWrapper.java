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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * A semi-wrapper, semi-renderer for players. Used in lieu of PlayerRenderer for custom models.
 */
@OnlyIn(Dist.CLIENT)
public class PlayerRendererWrapper extends LivingRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>> {
    /**
     * Conversion value for degrees to radians conversion.
     */
    static final float degrees2Radians = (float) (Math.PI / 180.0);
    // Reflected methods grabbed from PlayerRenderer.
    private static Method setModelVisibilities;
    private static Method applyRotations;
    private static Method preRenderCallback;
    private static Method renderName;
    // Reflected fields grabbed from PlayerRenderer.
    private static Field entityModelField;
    private static Field layerRenderers;

    final PlayerRenderer wrappedRenderer;
    private final ModifiedBipedModel upperArmorModel;



    public static void doClientStuff() {
        setModelVisibilities = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "setModelVisibilities", AbstractClientPlayerEntity.class);
        ReflectionHelper.makeAccessible(setModelVisibilities);
        applyRotations = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "applyRotations", AbstractClientPlayerEntity.class, MatrixStack.class, float.class, float.class, float.class);
        ReflectionHelper.makeAccessible(applyRotations);
        preRenderCallback = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "preRenderCallback", AbstractClientPlayerEntity.class, MatrixStack.class, float.class);
        ReflectionHelper.makeAccessible(preRenderCallback);
        renderName = ReflectionHelper.getMethodOrNull(PlayerRenderer.class, "renderName", AbstractClientPlayerEntity.class, String.class, MatrixStack.class, IRenderTypeBuffer.class, int.class);
        ReflectionHelper.makeAccessible(renderName);

        entityModelField = ReflectionHelper.getFieldOrNull(LivingRenderer.class, "entityModel");
        ReflectionHelper.makeAccessible(entityModelField);
        layerRenderers = ReflectionHelper.getFieldOrNull(LivingRenderer.class, "layerRenderers");
        ReflectionHelper.makeAccessible(layerRenderers);
    }



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
        ReflectionHelper.setField(entityModelField, wrappedRenderer, playerModel);

        // Modifies the layer renderers of the wrapped class, and then copies it into this class.
        upperArmorModel = new ModifiedBipedModel(1.0F);
        List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> wrappedLayers = (List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>>)
                ReflectionHelper.getFieldOrDefault(layerRenderers, wrappedRenderer, null);

        if (wrappedLayers != null) {
            for (int i = 0; i < wrappedLayers.size(); i++)
                if (wrappedLayers.get(i) instanceof BipedArmorLayer) {
                    wrappedLayers.remove(i);
                    wrappedLayers.add(i, new BipedArmorLayer(this, new BipedModel(0.5F), upperArmorModel));

                    break;
                }

            ReflectionHelper.setField(layerRenderers, this, wrappedLayers);
        }

        setOffsets(playerOffsets);
    }



    /**
     * A clone of the render method in PlayerRenderer, without the event calls.
     */
    @Override
    public void render(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        ReflectionHelper.invokeMethod(setModelVisibilities, wrappedRenderer, entityIn);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    /**
     * Sets the offsets for the models within this renderer that are affected by them.
     *
     * @param offsets A 3d vector containing the offsets.
     */
    public void setOffsets(Vec3d offsets) {
        float xOffset;
        float yOffset;
        float zOffset;
        float offsetAngle;
        boolean shouldRotate;

        if (!offsets.equals(Vec3d.ZERO)) {
            xOffset = (float) (offsets.x * 16);
            yOffset = (float) (offsets.y * -16);
            zOffset = (float) (offsets.z * 16);
            // Angle Needs to be multiplied by two for whatever reason.
            offsetAngle = (float) (Math.atan2(-offsets.z, offsets.x) * 2);
            shouldRotate = true;

        } else {
            xOffset = 0;
            yOffset = 0;
            zOffset = 0;
            offsetAngle = 0;
            shouldRotate = false;
        }

        if (entityModel instanceof ModifiedPlayerModel<?>)
            ((ModifiedPlayerModel<?>) entityModel).setOffsets(xOffset, yOffset, zOffset, offsetAngle, shouldRotate);
        upperArmorModel.setOffsets(xOffset, yOffset, zOffset, offsetAngle, shouldRotate);
    }

    /**
     * Resets certain parts of the models within this renderer.
     */
    public void reset() {
        ((ModifiedPlayerModel<?>) entityModel).reset();
        upperArmorModel.reset();
    }



    @Override
    public Vec3d getRenderOffset(AbstractClientPlayerEntity entityIn, float partialTicks) {
        return wrappedRenderer.getRenderOffset(entityIn, partialTicks);
    }

    @Override
    public ResourceLocation getEntityTexture(AbstractClientPlayerEntity entity) {
        return wrappedRenderer.getEntityTexture(entity);
    }

    @Override
    protected void preRenderCallback(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStack, float partialTickTime) {
        ReflectionHelper.invokeMethod(
                preRenderCallback,
                wrappedRenderer,
                entityLiving, matrixStack, partialTickTime
        );
    }

    @Override
    protected void renderName(AbstractClientPlayerEntity entityIn, String displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        ReflectionHelper.invokeMethod(
                renderName,
                wrappedRenderer,
                entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn
        );
    }

    @Override
    protected void applyRotations(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        ReflectionHelper.invokeMethod(
                applyRotations,
                wrappedRenderer,
                entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks
        );
    }
}