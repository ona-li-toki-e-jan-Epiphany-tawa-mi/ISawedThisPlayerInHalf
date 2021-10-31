package com.epiphany.isawedthisplayerinhalf.rendering.modfiedRendering;

import com.epiphany.isawedthisplayerinhalf.helpers.MathConstants;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsets;
import com.epiphany.isawedthisplayerinhalf.rendering.RenderingOffsetter;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A modified version of PlayerModel for customizing how players are rendered.
 */
@OnlyIn(Dist.CLIENT)
public class ModifiedPlayerModel<T extends LivingEntity> extends PlayerModel<T> {
    /**
     * Creates a new ModifiedPlayerModel.
     *
     * @param smallArmsIn Whether the player model has small arms.
     */
    public ModifiedPlayerModel(boolean smallArmsIn) {
        super(0.0f, smallArmsIn);
    }

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        RenderingOffsets renderingOffsets = RenderingOffsetter.renderingOffsetsMap.get(entity.getUniqueID());

        if (renderingOffsets != null)
            if (renderingOffsets.shouldOffsetRender) {
                // Creates an angle that cancels out the yaw offset put on by the renderer and adds it to the offset to make the model stay in place.
                float netYawOffset = MathHelper.lerp(ageInTicks - entity.ticksExisted, entity.prevRenderYawOffset, entity.renderYawOffset)
                        * MathConstants.degreesToRadians + renderingOffsets.yawOffset;

                // Rotates offset point into position.
                float offsetCos = MathHelper.cos(netYawOffset);
                float offsetSin = MathHelper.sin(netYawOffset);
                float pointX = renderingOffsets.xOffset * offsetCos - renderingOffsets.zOffset * offsetSin;
                float pointZ = renderingOffsets.xOffset * offsetSin + renderingOffsets.zOffset * offsetCos;

                // Applies offsets.
                this.bipedHead.rotationPointX = pointX;
                this.bipedHead.rotationPointY += renderingOffsets.yOffset;
                this.bipedHead.rotationPointZ = pointZ;
                this.bipedHeadwear.copyModelAngles(this.bipedHead);

                this.bipedBody.rotationPointX = pointX;
                this.bipedBody.rotationPointY += renderingOffsets.yOffset;
                this.bipedBody.rotationPointZ = pointZ;
                this.bipedBodyWear.copyModelAngles(this.bipedBody);

                this.bipedLeftArm.rotationPointX += pointX;
                this.bipedLeftArm.rotationPointY += renderingOffsets.yOffset;
                this.bipedLeftArm.rotationPointZ += pointZ;
                this.bipedLeftArmwear.copyModelAngles(this.bipedLeftArm);

                this.bipedRightArm.rotationPointX += pointX;
                this.bipedRightArm.rotationPointY += renderingOffsets.yOffset;
                this.bipedRightArm.rotationPointZ += pointZ;
                this.bipedRightArmwear.copyModelAngles(this.bipedRightArm);

            } else {
                // ???
                this.bipedHead.rotationPointX = 0.0f;
                this.bipedHead.rotationPointZ = 0.0f;
                this.bipedBody.rotationPointX = 0.0f;
                this.bipedBody.rotationPointZ = 0.0f;
            }
    }
}
