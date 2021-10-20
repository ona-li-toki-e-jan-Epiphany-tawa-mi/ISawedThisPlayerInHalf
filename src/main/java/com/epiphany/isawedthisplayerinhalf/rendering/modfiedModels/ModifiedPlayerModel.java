package com.epiphany.isawedthisplayerinhalf.rendering.modfiedModels;

import com.epiphany.isawedthisplayerinhalf.helpers.MathConstants;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A modified version of PlayerModel for customizing how players are rendered.
 */
@OnlyIn(Dist.CLIENT)
public class ModifiedPlayerModel<T extends LivingEntity> extends PlayerModel<T> implements IModifiedModel {
    private final float[] initialValues = new float[4];
    private float xOffset, yOffset, zOffset;
    private float offsetAngle;
    private boolean shouldRotate;

    /**
     * Creates a new ModifiedPlayerModel.
     *
     * @param modelSize The size of the model to be created.
     * @param smallArmsIn Whether the player model has small arms.
     */
    public ModifiedPlayerModel(float modelSize, boolean smallArmsIn) {
        super(modelSize, smallArmsIn);

        this.initialValues[0] = bipedHead.rotationPointX;
        this.initialValues[1] = bipedHead.rotationPointZ;
        this.initialValues[2] = bipedBody.rotationPointX;
        this.initialValues[3] = bipedBody.rotationPointZ;
    }

    /**
     * Sets the rotation angles for the model.
     *
     * @param entity The entity the model belongs to.
     * @param limbSwing The angle at which the entity's limbs are swinging.
     * @param limbSwingAmount How far each degree of limb swing moves the limbs.
     * @param ageInTicks The age of the entity in ticks.
     * @param netHeadYaw The net yaw rotation for the entity's head.
     * @param headPitch The pitch rotation of the entity's head.
     */
    @Override
    public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        if (this.shouldRotate) {
            // Creates an angle that cancels out the yaw offset put on by the renderer and adds it to the offset to make the model stay in place.
            float netYawOffset = MathHelper.lerp(ageInTicks - entity.ticksExisted, entity.prevRenderYawOffset, entity.renderYawOffset)
                    * MathConstants.degreesToRadians + this.offsetAngle;
            float offsetCos = MathHelper.cos(netYawOffset);
            float offsetSin = MathHelper.sin(netYawOffset);

            float pointX = this.xOffset * offsetCos - this.zOffset * offsetSin;
            float pointZ = this.xOffset * offsetSin + this.zOffset * offsetCos;

            // Applies offsets.
            bipedHead.rotationPointX = pointX + this.initialValues[0];
            bipedHead.rotationPointY += this.yOffset;
            bipedHead.rotationPointZ = pointZ + this.initialValues[1];
            bipedHeadwear.copyModelAngles(bipedHead);

            bipedBody.rotationPointX = pointX + this.initialValues[2];
            bipedBody.rotationPointY += this.yOffset;
            bipedBody.rotationPointZ = pointZ + this.initialValues[3];
            bipedBodyWear.copyModelAngles(bipedBody);

            bipedLeftArm.rotationPointX += pointX;
            bipedLeftArm.rotationPointY += this.yOffset;
            bipedLeftArm.rotationPointZ += pointZ;
            bipedLeftArmwear.copyModelAngles(bipedLeftArm);

            bipedRightArm.rotationPointX += pointX;
            bipedRightArm.rotationPointY += this.yOffset;
            bipedRightArm.rotationPointZ += pointZ;
            bipedRightArmwear.copyModelAngles(bipedRightArm);
        }
    }


    @Override
    public void setOffsets(float xOffset, float yOffset, float zOffset, float offsetAngle) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.offsetAngle = offsetAngle;
        this.shouldRotate = true;
    }

    @Override
    public void unsetOffsets() {
        this.xOffset = 0;
        this.yOffset = 0;
        this.zOffset = 0;
        this.offsetAngle = 0;

        this.bipedHead.rotationPointX = this.initialValues[0];
        this.bipedHead.rotationPointZ = this.initialValues[1];
        this.bipedBody.rotationPointX = this.initialValues[2];
        this.bipedBody.rotationPointZ = this.initialValues[3];

        this.shouldRotate = false;
    }
}
