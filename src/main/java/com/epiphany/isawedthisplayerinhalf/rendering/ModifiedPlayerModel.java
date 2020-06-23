package com.epiphany.isawedthisplayerinhalf.rendering;

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
    private final float[] initialValues = new float[4];
    private float xOffset, yOffset, zOffset;
    private float offsetAngle;
    private boolean shouldRotate;

    public ModifiedPlayerModel(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);
        initialValues[0] = bipedHead.rotationPointX;
        initialValues[1] = bipedHead.rotationPointZ;
        initialValues[2] = bipedBody.rotationPointX;
        initialValues[3] = bipedBody.rotationPointZ;
    }

    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (shouldRotate) {
            // Creates point(x, z) that cancels out the yaw offset put on by the renderer.
            float netYawOffset = MathHelper.lerp(ageInTicks - entity.ticksExisted, entity.prevRenderYawOffset, entity.renderYawOffset) * PlayerRendererWrapper.degrees2Radians + offsetAngle;
            float offsetCos = MathHelper.cos(netYawOffset);
            float offsetSin = MathHelper.sin(netYawOffset);

            float pointX = xOffset * offsetCos - zOffset * offsetSin;
            float pointZ = xOffset * offsetSin + zOffset * offsetCos;

            // Applies offsets.
            bipedHead.rotationPointX = pointX + initialValues[0];
            bipedHead.rotationPointY += yOffset;
            bipedHead.rotationPointZ = pointZ + initialValues[1];
            bipedHeadwear.copyModelAngles(bipedHead);

            bipedBody.rotationPointX = pointX + initialValues[2];
            bipedBody.rotationPointY += yOffset;
            bipedBody.rotationPointZ = pointZ + initialValues[3];
            bipedBodyWear.copyModelAngles(bipedBody);

            bipedLeftArm.rotationPointX += pointX;
            bipedLeftArm.rotationPointY += yOffset;
            bipedLeftArm.rotationPointZ += pointZ;
            bipedLeftArmwear.copyModelAngles(bipedLeftArm);

            bipedRightArm.rotationPointX += pointX;
            bipedRightArm.rotationPointY += yOffset;
            bipedRightArm.rotationPointZ += pointZ;
            bipedRightArmwear.copyModelAngles(bipedRightArm);
        }
    }



    /**
     * Sets the offsets for this model.
     */
    void setOffsets(float xOffset, float yOffset, float zOffset, float offsetAngle, boolean shouldRotate) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.offsetAngle = offsetAngle;
        this.shouldRotate = shouldRotate;
    }

    /**
     * Resets certain parts of the player model.
     */
    void reset() {
        bipedHead.rotationPointX = initialValues[0];
        bipedHead.rotationPointZ = initialValues[1];
        bipedBody.rotationPointX = initialValues[2];
        bipedBody.rotationPointZ = initialValues[3];
    }
}
