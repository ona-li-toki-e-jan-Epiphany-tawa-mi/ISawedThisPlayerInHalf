package com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering;

import com.epiphany.isawedthisplayerinhalf.helpers.MathConstants;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsets;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A modified version of BipedModel for customizing how players' armor is rendered, or whatever else it's used for.
 */
@OnlyIn(Dist.CLIENT)
public class ModifiedBipedModel<T extends LivingEntity> extends BipedModel<T> {
    /**
     * Creates a new ModifiedBipedModel.
     */
    public ModifiedBipedModel() {
        super(1.0f);
    }

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        RenderingOffsets renderingOffsets = RenderingOffsetter.getOffsetsOrNull(entity);

        if (renderingOffsets != null)
            if (renderingOffsets.shouldOffsetRender()) {
                // Creates an angle that cancels out the yaw offset put on by the renderer to make the model stay in place, and then adds in the offset angle.
                float netYawOffset = MathHelper.lerp(ageInTicks - entity.ticksExisted, entity.prevRenderYawOffset, entity.renderYawOffset)
                        * MathConstants.degreesToRadians + renderingOffsets.getYawOffset();

                // Rotates offset point into position.
                float offsetCos = MathHelper.cos(netYawOffset);
                float offsetSin = MathHelper.sin(netYawOffset);
                float pointX = renderingOffsets.getXOffset() * offsetCos - renderingOffsets.getZOffset() * offsetSin;
                float pointZ = renderingOffsets.getXOffset() * offsetSin + renderingOffsets.getZOffset() * offsetCos;

                // Applies offsets.
                this.bipedHead.rotationPointX = pointX;
                this.bipedHead.rotationPointY += renderingOffsets.getYOffset();
                this.bipedHead.rotationPointZ = pointZ;
                this.bipedHeadwear.copyModelAngles(bipedHead);

                this.bipedBody.rotationPointX = pointX;
                this.bipedBody.rotationPointY += renderingOffsets.getYOffset();
                this.bipedBody.rotationPointZ = pointZ;

                this.bipedLeftArm.rotationPointX += pointX;
                this.bipedLeftArm.rotationPointY += renderingOffsets.getYOffset();
                this.bipedLeftArm.rotationPointZ += pointZ;

                this.bipedRightArm.rotationPointX += pointX;
                this.bipedRightArm.rotationPointY += renderingOffsets.getYOffset();
                this.bipedRightArm.rotationPointZ += pointZ;

            } else {
                // Undoes offsets.
                this.bipedHead.rotationPointX = 0.0f;
                this.bipedHead.rotationPointZ = 0.0f;
                this.bipedBody.rotationPointX = 0.0f;
                this.bipedBody.rotationPointZ = 0.0f;
            }
    }
}
