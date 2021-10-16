package com.epiphany.isawedthisplayerinhalf.rendering;

import com.epiphany.isawedthisplayerinhalf.helpers.MathConstants;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A modified version of BipedModel for customizing how players' armor is rendered, or whatever else it might be used it for.
 */
@OnlyIn(Dist.CLIENT)
public class ModifiedBipedModel<T extends LivingEntity> extends BipedModel<T> {
    private final float[] initialValues = new float[4];
    private float xOffset, yOffset, zOffset;
    private float offsetAngle;
    private boolean shouldRotate;

    /**
     * Creates a new ModifiedBipedModel.
     *
     * @param modelSize The size of the model to be created.
     */
    public ModifiedBipedModel(float modelSize) {
        super(modelSize);

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

            bipedLeftArm.rotationPointX += pointX;
            bipedLeftArm.rotationPointY += this.yOffset;
            bipedLeftArm.rotationPointZ += pointZ;

            bipedRightArm.rotationPointX += pointX;
            bipedRightArm.rotationPointY += this.yOffset;
            bipedRightArm.rotationPointZ += pointZ;
        }
    }



    /**
     * Sets the offsets for this model.
     *
     * @param xOffset The offset in the x-axis.
     * @param yOffset The offset in the y-axis.
     * @param zOffset The offset in the z-axis.
     * @param offsetAngle The angle along the XZ plane that intersects the point (xOffset, zOffset).
     * @param shouldRotate Whether the model should be rotated in the first place.
     */
    public void setOffsets(float xOffset, float yOffset, float zOffset, float offsetAngle, boolean shouldRotate) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.offsetAngle = offsetAngle;
        this.shouldRotate = shouldRotate;
    }

    /**
     * Resets certain parts of the biped model.
     */
    void reset() {
        bipedHead.rotationPointX = this.initialValues[0];
        bipedHead.rotationPointZ = this.initialValues[1];
        bipedBody.rotationPointX = this.initialValues[2];
        bipedBody.rotationPointZ = this.initialValues[3];
    }
}
