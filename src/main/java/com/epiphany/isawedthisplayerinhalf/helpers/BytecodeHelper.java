package com.epiphany.isawedthisplayerinhalf.helpers;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;

/**
 * Helper class for calling functions and reading values that can't be done in pure bytecode because of obfuscation or other reasons.
 */
public class BytecodeHelper {
    private static final Field closestEntityField;

    static {
        closestEntityField = ReflectionHelper.getFieldOrNull(LookAtGoal.class, "closestEntity", "field_75334_a");
        ReflectionHelper.makeAccessible(closestEntityField);

        if (closestEntityField == null)
            throw new NullPointerException("Unable to find field 'closestEntityField' under names 'closestEntity' and 'field_75334_a'");
    }



    /**
     * Gets the x-coordinate of a vector.
     *
     * @param vector The vector to get the x-coordinate of.
     * @return The x-coordinate of the given vector.
     */
    public static double getVectorX(Vec3d vector) {
        return vector.x;
    }

    /**
     * Gets the y-coordinate of a vector.
     *
     * @param vector The vector to get the y-coordinate of.
     * @return The y-coordinate of the given vector.
     */
    public static double getVectorY(Vec3d vector) {
        return vector.y;
    }

    /**
     * Gets the z-coordinate of a vector.
     *
     * @param vector The vector to get the z-coordinate of.
     * @return The z-coordinate of the given vector.
     */
    public static double getVectorZ(Vec3d vector) {
        return vector.z;
    }

    /**
     * Gets the owner of a fishing bobber.
     *
     * @param fishingBobberEntity The fishing bobber to get the angler of.
     * @return The angler.
     */
    public static PlayerEntity getAngler(FishingBobberEntity fishingBobberEntity) {
        return fishingBobberEntity.getAngler();
    }

    /**
     * Gets the player an interaction manager contains.
     *
     * @param playerInteractionManager The interaction manager to get the player from.
     * @return The player in the interaction manager.
     */
    public static ServerPlayerEntity getPlayerFromManager(PlayerInteractionManager playerInteractionManager) {
        return playerInteractionManager.player;
    }

    /**
     * Gets the entity that the look at goal is focused on.
     *
     * @param lookAtGoal The look at goal to get the closest entity of.
     * @return The closest entity that the look at goal is focused on.
     */
    public static Entity getClosestEntity(LookAtGoal lookAtGoal) {
        return (Entity) ReflectionHelper.getFieldOrDefault(closestEntityField, lookAtGoal, null);
    }

    /**
     * Offsets the position of an axis aligned bounding box.
     *
     * @param axisAlignedBB The axis aligned bounding box to offset.
     * @param offset The offset to apply.
     * @return A copy of the axis aligned bounding box, for chaining.
     */
    public static AxisAlignedBB offsetAABB(AxisAlignedBB axisAlignedBB, Vec3d offset) {
        return axisAlignedBB.offset(offset);
    }

    /**
     * Gets the zero vector from the Vec3d class.
     *
     * @return The zero vector.
     */
    public static Vec3d getZeroVector() {
        return Vec3d.ZERO;
    }

    /**
     * Gets whether the axis aligned bounding box is in the frustum of the camera.
     *
     * @param camera The camera to get the frustum from.
     * @param axisAlignedBB The axis aligned bounding box to check.
     * @return Whether the axis aligned bounding box is in the frustum of the camera.
     */
    public static boolean isBoundingBoxInFrustum(ClippingHelperImpl camera, AxisAlignedBB axisAlignedBB) {
        return camera.isBoundingBoxInFrustum(axisAlignedBB);
    }



    /**
     * Offsets the initial position of a raycast.
     *
     * @param entity The entity whose raycast is being offset.
     * @param initialPosition The initial position of the raycast.
     *
     * @return The offset position for the raycast to use.
     */
    public static Vec3d offsetRaycast(Vec3d initialPosition, Entity entity) {
        if (entity instanceof PlayerEntity) {
            Vec3d offsets = Offsetter.getOffsets((PlayerEntity) entity);

            if (!offsets.equals(Vec3d.ZERO))
                return initialPosition.add(offsets);
        }

        return initialPosition;
    }

    public static AxisAlignedBB offsetAxisAlignedBB(AxisAlignedBB axisAlignedBB, Entity entity) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        if (!offsets.equals(Vec3d.ZERO))
            return axisAlignedBB.offset(offsets);

        return axisAlignedBB;
    }

    /**
     * Offsets a projectile based on the offset of its shooter.
     *
     * @param projectile The projectile to offset the position of.
     * @param shooter The shooter of the projectile.
     */
    public static void offsetProjectile(Entity projectile, LivingEntity shooter) {
        if (shooter instanceof PlayerEntity) {
            Vec3d offsets = Offsetter.getOffsets((PlayerEntity) shooter);

            if (!offsets.equals(Vec3d.ZERO))
                projectile.setPosition(
                        projectile.getPosX() + offsets.x,
                        projectile.getPosY() + offsets.y,
                        projectile.getPosZ() + offsets.z
                );
        }
    }

    /**
     * Gets the corrected distance squared from a player to a point.
     *
     * @param playerEntity The player to use for the first position.
     * @param x The x-position of the second position.
     * @param y The y-position of the second position.
     * @param z The z-position of the second position.
     *
     * @return The distance, squared, between the player and the point.
     */
    public static double modifiedGetDistanceSq(PlayerEntity playerEntity, double x, double y, double z) {
        Vec3d offsets = Offsetter.getOffsets(playerEntity);
        double dx = playerEntity.getPosX() + offsets.x - x;
        double dy = playerEntity.getPosY() + offsets.y - y;
        double dz = playerEntity.getPosZ() + offsets.z - z;
        return dx * dx + dy * dy + dz * dz;
    }

    public static boolean isPlayerOffset(PlayerEntity playerEntity) {
        return Offsetter.getOffsets(playerEntity).equals(Vec3d.ZERO);
    }


    /**
     * Gets whether the arm of the player should be rendered.
     * Used for the first person renderer.
     *
     * @return Whether the arm of the player should be rendered.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRenderHand() {
        return Offsetter.getOffsets(Minecraft.getInstance().player).equals(Vec3d.ZERO);
    }

    /**
     * Gets whether the game is in third-person.
     * Overrides normal behavior if the player has an offset.
     *
     * @param activeRenderInfo The active render info of the calling renderer.
     *
     * @return Whether the game is in third-person.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean modifiedIsThirdPerson(ActiveRenderInfo activeRenderInfo, boolean isInThirdPerson) {
        return isInThirdPerson || !Offsetter.getOffsets(Minecraft.getInstance().player).equals(Vec3d.ZERO);
    }
}
