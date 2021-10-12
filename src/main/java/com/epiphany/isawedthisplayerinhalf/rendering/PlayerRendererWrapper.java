package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.helpers.ReflectionHelper;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
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
    static final float degreesToRadians = (float) (Math.PI / 180.0);
    // Reflected methods grabbed from PlayerRenderer.
    private static final Method setModelVisibilitiesMethod;
    private static final Method applyRotationsMethod;
    private static final Method preRenderCallbackMethod;
    private static final Method renderNameMethod;
    // Reflected fields grabbed from PlayerRenderer.
    private static final Field entityModelField;
    private static final Field layerRenderersField;

    final PlayerRenderer wrappedRenderer;
    private final ModifiedBipedModel upperArmorModel;



    static {
        setModelVisibilitiesMethod = ReflectionHelper.getMethodOrNull(
                PlayerRenderer.class, "setModelVisibilities", "func_177137_d",
                AbstractClientPlayerEntity.class
        );
        ReflectionHelper.makeAccessible(setModelVisibilitiesMethod);
        applyRotationsMethod = ReflectionHelper.getMethodOrNull(
                PlayerRenderer.class, "applyRotations", "func_225621_a_",
                AbstractClientPlayerEntity.class, MatrixStack.class, float.class, float.class, float.class
        );
        ReflectionHelper.makeAccessible(applyRotationsMethod);
        preRenderCallbackMethod = ReflectionHelper.getMethodOrNull(
                PlayerRenderer.class, "preRenderCallback", "func_225620_a_",
                AbstractClientPlayerEntity.class, MatrixStack.class, float.class
        );
        ReflectionHelper.makeAccessible(preRenderCallbackMethod);
        renderNameMethod = ReflectionHelper.getMethodOrNull(
                PlayerRenderer.class, "renderName", "func_225629_a_",
                AbstractClientPlayerEntity.class, String.class, MatrixStack.class, IRenderTypeBuffer.class, int.class
        );
        ReflectionHelper.makeAccessible(renderNameMethod);

        entityModelField = ReflectionHelper.getFieldOrNull(LivingRenderer.class, "entityModel", "field_77045_g");
        ReflectionHelper.makeAccessible(entityModelField);
        layerRenderersField = ReflectionHelper.getFieldOrNull(LivingRenderer.class, "layerRenderers",
                "field_177097_h");
        ReflectionHelper.makeAccessible(layerRenderersField);

        if (setModelVisibilitiesMethod == null)
            throw new NullPointerException("Unable to find method 'setModelVisibilitiesMethod' under names 'setModelVisibilities' and 'func_177137_d'");
        if (applyRotationsMethod == null)
            throw new NullPointerException("Unable to find method 'applyRotationsMethod' under names 'applyRotations' and 'func_225621_a_'");
        if (preRenderCallbackMethod == null)
            throw new NullPointerException("Unable to find method 'preRenderCallbackMethod' under names 'preRenderCallback' and 'func_225620_a_'");
        if (renderNameMethod == null)
            throw new NullPointerException("Unable to find method 'renderNameMethod' under names 'renderName' and 'func_225629_a_'");

        if (entityModelField == null)
            throw new NullPointerException("Unable to find field 'entityModelField' under names 'entityModel' and 'field_77045_g'");
        if (layerRenderersField == null)
            throw new NullPointerException("Unable to find field 'layerRenderersField' under names 'layerRenderers' and 'field_177097_h'");
    }



    /**
     * Creates a new wrapped player renderer.
     *
     * @param rendererToBeWrapped The player's current original renderer.
     * @param playerModel A modified version of the player's model.
     * @param playerOffsets A 3d vector representing the initial offsets a player has.
     */
    PlayerRendererWrapper(PlayerRenderer rendererToBeWrapped, ModifiedPlayerModel<AbstractClientPlayerEntity> playerModel, Vec3d playerOffsets) {
        super(rendererToBeWrapped.getRenderManager(), playerModel, 0.5F);
        this.wrappedRenderer = rendererToBeWrapped;
        ReflectionHelper.setField(entityModelField, this.wrappedRenderer, playerModel);


        // Modifies the layer renderers of the wrapped class, and then copies it into this class.
        this.upperArmorModel = new ModifiedBipedModel<>(1.0F);
        List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>> wrappedLayers =
                (List<LayerRenderer<AbstractClientPlayerEntity, PlayerModel<AbstractClientPlayerEntity>>>)
                        ReflectionHelper.getFieldOrDefault(layerRenderersField, this.wrappedRenderer, null);

        if (wrappedLayers != null) {
            for (int i = 0; i < wrappedLayers.size(); i++)
                if (wrappedLayers.get(i) instanceof BipedArmorLayer) {
                    wrappedLayers.remove(i);
                    wrappedLayers.add(i, new BipedArmorLayer<>(this, new BipedModel<>(0.5F), this.upperArmorModel));

                    break;
                }

            ReflectionHelper.setField(layerRenderersField, this, wrappedLayers);

        } else
            throw new NullPointerException("Failed to get layer renderers from wrapper PlayerRenderer");


        setOffsets(playerOffsets);
    }



    /**
     * A clone of the render method in PlayerRenderer, without the event calls.
     */
    @Override
    public void render(AbstractClientPlayerEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        ReflectionHelper.invokeMethod(setModelVisibilitiesMethod, this.wrappedRenderer, entityIn);
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
    }

    /**
     * Sets the offsets for the models within this renderer that are affected by them.
     *
     * @param offsets A 3d vector containing the offsets.
     */
    public void setOffsets(Vec3d offsets) {
        float xOffset, yOffset, zOffset;
        float offsetAngle;
        boolean shouldRotate;

        if (!offsets.equals(Vec3d.ZERO)) {
            xOffset = (float) (offsets.x * 16);
            yOffset = (float) (offsets.y * -16);
            zOffset = (float) (offsets.z * 16);
            // Angle needs to be multiplied by two for whatever reason.
            offsetAngle = (float) (Math.atan2(-offsets.z, offsets.x) * 2);
            shouldRotate = true;

        } else {
            xOffset = 0;
            yOffset = 0;
            zOffset = 0;
            offsetAngle = 0;
            shouldRotate = false;
        }


        ((ModifiedPlayerModel<?>) this.entityModel).setOffsets(xOffset, yOffset, zOffset, offsetAngle, shouldRotate);
        this.upperArmorModel.setOffsets(xOffset, yOffset, zOffset, offsetAngle, shouldRotate);
    }

    /**
     * Resets certain parts of the models within this renderer.
     */
    public void reset() {
        ((ModifiedPlayerModel<?>) entityModel).reset();
        this.upperArmorModel.reset();
    }



    //////////////////////////////////////////////////////////////////////////////////
    // A bunch of functions that just pass along arguments to the wrapped renderer. //
    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public Vec3d getRenderOffset(AbstractClientPlayerEntity entityIn, float partialTicks) {
        return this.wrappedRenderer.getRenderOffset(entityIn, partialTicks);
    }

    @Override
    public ResourceLocation getEntityTexture(AbstractClientPlayerEntity entity) {
        return this.wrappedRenderer.getEntityTexture(entity);
    }

    @Override
    protected void preRenderCallback(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStack, float partialTickTime) {
        ReflectionHelper.invokeMethod(
                preRenderCallbackMethod,
                this.wrappedRenderer,
                entityLiving, matrixStack, partialTickTime
        );
    }

    @Override
    protected void renderName(AbstractClientPlayerEntity entityIn, String displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        ReflectionHelper.invokeMethod(
                renderNameMethod,
                this.wrappedRenderer,
                entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn
        );
    }

    @Override
    protected void applyRotations(AbstractClientPlayerEntity entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        ReflectionHelper.invokeMethod(
                applyRotationsMethod,
                this.wrappedRenderer,
                entityLiving, matrixStackIn, ageInTicks, rotationYaw, partialTicks
        );
    }
}