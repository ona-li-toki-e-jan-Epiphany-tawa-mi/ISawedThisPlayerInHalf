package com.epiphany.isawedthisplayerinhalf.helpers;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * Helper class for calling functions and accessing fields that can't be done in pure bytecode thanks to obfuscation,
 *  and for moving as much code into Java as possible to increase maintainability.
 */
@SuppressWarnings("unused")
public class BytecodeHelper {
    private static final Field FIELD_entity;
    private static final Field FIELD_closestEntity;
    private static final Field FIELD_maxDistance;
    private static final Random RANDOM;

    static {
        // LookAtGoal.
        FIELD_entity = ReflectionHelper.getDeclaredFieldOrNull(LookAtGoal.class, "entity", "field_75332_b");
        ReflectionHelper.makeAccessible(FIELD_entity);
        FIELD_closestEntity = ReflectionHelper.getDeclaredFieldOrNull(LookAtGoal.class, "closestEntity", "field_75334_a");
        ReflectionHelper.makeAccessible(FIELD_closestEntity);
        FIELD_maxDistance = ReflectionHelper.getDeclaredFieldOrNull(LookAtGoal.class, "maxDistance", "field_75333_c");
        ReflectionHelper.makeAccessible(FIELD_maxDistance);

        if (FIELD_entity == null)
            throw new NullPointerException("Unable to find field 'FIELD_entity' under names 'entity' and 'field_75332_b'");
        if (FIELD_closestEntity == null)
            throw new NullPointerException("Unable to find field 'FIELD_closestEntity' under names 'closestEntity' and 'field_75334_a'");
        if (FIELD_maxDistance == null)
            throw new NullPointerException("Unable to find field 'FIELD_maxDistance' under names 'maxDistance' and 'field_75333_c'");


        RANDOM = new Random();
    }



    /**
     * Gets the x-coordinate of a vector.
     *
     * @param vector The vector to get the x-coordinate of.
     *
     * @return The x-coordinate of the given vector.
     */
    public static double getVectorX(Vec3d vector) {
        return vector.x;
    }

    /**
     * Gets the y-coordinate of a vector.
     *
     * @param vector The vector to get the y-coordinate of.
     *
     * @return The y-coordinate of the given vector.
     */
    public static double getVectorY(Vec3d vector) {
        return vector.y;
    }

    /**
     * Gets the z-coordinate of a vector.
     *
     * @param vector The vector to get the z-coordinate of.
     *
     * @return The z-coordinate of the given vector.
     */
    public static double getVectorZ(Vec3d vector) {
        return vector.z;
    }


    /**
     * Checks if a player is offset.
     *
     * @param playerEntity The player to check for offsets.
     *
     * @return Whether the player has offsets.
     */
    public static boolean isPlayerOffset(PlayerEntity playerEntity) {
        return Offsetter.getOffsets(playerEntity).equals(Vec3d.ZERO);
    }

    /**
     * Gets the offsets of an entity divided by the scalar value.
     *
     * @param entity The entity to get the offsets from.
     * @param inverseScalar The value to divide the resulting offsets with.
     *
     * @return The inversely scaled offsets of the entity.
     */
    public static Vec3d getOffsetsInverselyScaled(Entity entity, float inverseScalar) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        if (!offsets.equals(Vec3d.ZERO)) {
            inverseScalar = 1 / inverseScalar;
            return offsets.mul(inverseScalar, inverseScalar, inverseScalar);

        } else
            return offsets;
    }

    /**
     * Gets the offsets of the player that an interaction manager contains.
     *
     * @param playerInteractionManager The interaction manager to get the offsets from.
     *
     * @return The offsets of the player in the interaction manager.
     */
    public static Vec3d getOffsetsFromManager(PlayerInteractionManager playerInteractionManager) {
        return Offsetter.getOffsets(playerInteractionManager.player);
    }

    /**
     * Gets the offsets of the angler of the fishing bobber.
     *
     * @param fishingBobberEntity The fishing bobber to get the angler with which to get the offsets from.
     *
     * @return The offsets of the angler.
     */
    public static Vec3d getAnglerOffsets(FishingBobberEntity fishingBobberEntity) {
        PlayerEntity angler = fishingBobberEntity.getAngler();

        return angler != null ? Offsetter.getOffsets(fishingBobberEntity.getAngler()) : Vec3d.ZERO;
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

    /**
     * Gets the corrected distance squared from an entity to an entity with offsets.
     *
     * @param entity The entity to use for the first position.
     * @param offsetEntity The entity with offsets to use for the second position.
     *
     * @return The distance, squared, between the entity and the other one.
     */
    public static double modifiedGetDistanceSq(Entity entity, Entity offsetEntity) {
        Vec3d offsets = Offsetter.getOffsets(offsetEntity);

        return !offsets.equals(Vec3d.ZERO) ? entity.getDistanceSq(offsetEntity.getPositionVec().add(offsets)) : entity.getDistanceSq(offsetEntity);
    }

    /**
     * Gets the corrected distance from an entity to the player.
     *
     * @param entity The entity to use for the first position.
     * @param offsetEntity The entity with offsets to use for the second position.
     *
     * @return The distance between the first entity and second entity.
     */
    public static float modifiedGetDistance(Entity entity, Entity offsetEntity) {
        return (float) Math.sqrt(BytecodeHelper.modifiedGetDistanceSq(entity, offsetEntity));
    }



    /**
     * Offsets a vector with the offsets of an entity.
     *
     * @param vector The vector to offset.
     * @param entity The entity to get the offsets from.
     *
     * @return The offset vector.
     */
    public static Vec3d offsetVector(Vec3d vector, Entity entity) {
        Vec3d offsets = Offsetter.getOffsets(entity);

       return !offsets.equals(Vec3d.ZERO) ? vector.add(offsets) : vector;
    }

    /**
     * Offsets a vector using the offsets from the angler of a fishing bobber.
     *
     * @param vector The vector to offset.
     * @param fishingBobberEntity The fishing bobber to get the angler with which to get the offsets.
     *
     * @return The offset vector
     */
    public static Vec3d offsetVectorWithAngler(Vec3d vector, FishingBobberEntity fishingBobberEntity) {
        return vector.add(getAnglerOffsets(fishingBobberEntity));
    }

    /**
     * Offsets a BlockPos using an entity's offsets.
     *
     * @param blockPosition The BlockPos to offset.
     * @param entity The entity to get the offsets from.
     *
     * @return The offset BlockPos.
     */
    public static BlockPos offsetBlockPosition(BlockPos blockPosition, Entity entity) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        return !offsets.equals(Vec3d.ZERO) ? blockPosition.add(offsets.x, offsets.y, offsets.z) : blockPosition;
    }

    /**
     * Offsets an axis aligned bounding box with the offsets from an entity.
     *
     * @param axisAlignedBB The bounding box to offset.
     * @param entity The entity to get the offsets from.
     *
     * @return The offset axis aligned bounding box.
     */
    public static AxisAlignedBB offsetAxisAlignedBB(AxisAlignedBB axisAlignedBB, Entity entity) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        return !offsets.equals(Vec3d.ZERO) ? axisAlignedBB.offset(offsets) : axisAlignedBB;
    }

    /**
     * Offsets the last matrix of a matrix stack with the offsets from a player.
     *
     * @param playerEntity The player to get the offsets from.
     * @param matrixStack The matrix stack to get the matrix to offset from.
     *
     * @return The matrix stack.
     */
    @OnlyIn(Dist.CLIENT)
    public static MatrixStack offsetMatrix(AbstractClientPlayerEntity playerEntity, MatrixStack matrixStack) {
        Vec3d offsets = Offsetter.getOffsets(playerEntity);

        if (!offsets.equals(Vec3d.ZERO))
            matrixStack.translate(offsets.x, offsets.y, offsets.z);

        return matrixStack;
    }


    /**
     * Offsets a projectile based on the offset of its shooter.
     *
     * @param projectile The projectile to offset the position of.
     * @param shooter The shooter of the projectile.
     */
    public static void offsetProjectile(Entity projectile, LivingEntity shooter) {
        Vec3d offsets = Offsetter.getOffsets(shooter);

        if (!offsets.equals(Vec3d.ZERO))
            projectile.setPosition(
                    projectile.getPosX() + offsets.x,
                    projectile.getPosY() + offsets.y,
                    projectile.getPosZ() + offsets.z
            );
    }

    /**
     * Randomly switches the target location of a LookAtGoal between the closet entity's original and offset bodies.
     *
     * @param x The x-position of the targeted entity.
     * @param y The y-position of the targeted entity.
     * @param z The z-position of the targeted entity.
     * @param lookAtGoal The LookAtGoal to randomly offset.
     *
     * @return Either a zero vector or the entity's offsets.
     */
    public static Vec3d applyLookAtOffsetsRandomly(double x, double y, double z, LookAtGoal lookAtGoal) {
        Entity closestEntity = (Entity) ReflectionHelper.getValueOrDefault(FIELD_closestEntity, lookAtGoal, null);
        if (closestEntity == null) throw new NullPointerException("Unable to get value from 'LookAtGoal'");

        Vec3d offsets = Offsetter.getOffsets(closestEntity);

        if (offsets.equals(Vec3d.ZERO) || RANDOM.nextBoolean())
            return new Vec3d(x, y, z);

        return new Vec3d(x + offsets.x, y + offsets.y, z + offsets.z);
    }



    /**
     * Gets whether the arm of the player should be rendered, returning false if the player is offset.
     *
     * @return Whether the arm of the player should be rendered.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean shouldRenderHand() {
        return Offsetter.getOffsets(Minecraft.getInstance().player).equals(Vec3d.ZERO);
    }

    /**
     * Gets whether the game is in third-person, overriding normal behavior if the player has an offset.
     *
     * @param activeRenderInfo The active render info of the calling renderer.
     *
     * @return Whether the game is in third-person.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean modifiedIsThirdPerson(ActiveRenderInfo activeRenderInfo) {
        return !Offsetter.getOffsets(activeRenderInfo.getRenderViewEntity()).equals(Vec3d.ZERO);
    }

    /**
     * Gets whether the entity is within range to render.
     *
     * @param entity The entity to test.
     * @param x The x-coordinate of the camera.
     * @param y The y-coordinate of the camera.
     * @param z The z-coordinate of the camera.
     *
     * @return Whether the entity is within range to render.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean modifiedIsInRangeToRender3d(Entity entity, double x, double y, double z) {
        Vec3d entityOffsets = Offsetter.getOffsets(entity);

        return !entityOffsets.equals(Vec3d.ZERO) && entity.isInRangeToRender3d(x + entityOffsets.x, y + entityOffsets.y, z + entityOffsets.z);
    }

    /**
     * Gets whether the axis aligned bounding box, after being offset, is in the frustum of the camera.
     *
     * @param entity The entity that is being rendered.
     * @param camera The camera to get the frustum from.
     * @param axisAlignedBB The axis aligned bounding box to check.
     *
     * @return Whether the axis aligned bounding box is in the frustum of the camera.
     */
    @OnlyIn(Dist.CLIENT)
    public static boolean modifiedIsBoundingBoxInFrustum(boolean originalResult, Entity entity, ClippingHelperImpl camera, AxisAlignedBB axisAlignedBB) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        return !offsets.equals(Vec3d.ZERO) ? camera.isBoundingBoxInFrustum(axisAlignedBB.offset(offsets)) : originalResult;
    }


    /**
     * Redoes the check in {@link LookAtGoal#shouldContinueExecuting()} if the distance to the closest player is too large
     *  and they have offsets.
     *
     * @param originalDistanceSq The original distance squared between the entity of LookAtGoal and its closest entity.
     * @param lookAtGoal The LookAtGoal that is checking the distance.
     *
     * @return Either the original distance squared or the distance squared to the player's offset position.
     */
    public static double redoIsWithinMaxDistance(double originalDistanceSq, LookAtGoal lookAtGoal) {
        float maxDistanceSq = (float) ReflectionHelper.getValueOrDefault(FIELD_maxDistance, lookAtGoal, Float.NaN);
        if (Float.isNaN(maxDistanceSq)) throw new NullPointerException("Unable to get value from 'FIELD_maxDistance'");
        maxDistanceSq *= maxDistanceSq;

        if (originalDistanceSq <= (double) maxDistanceSq)
            return originalDistanceSq;


        Entity closestEntity = (Entity) ReflectionHelper.getValueOrDefault(FIELD_closestEntity, lookAtGoal, null);
        if (closestEntity == null) throw new NullPointerException("Unable to get value from 'FIELD_closestEntity'");

        Vec3d offsets = Offsetter.getOffsets(closestEntity);

        if (!offsets.equals(Vec3d.ZERO)) {
            Entity entity = (Entity) ReflectionHelper.getValueOrDefault(FIELD_entity, lookAtGoal, null);
            if (entity == null) throw new NullPointerException("Unable to get value from 'FIELD_entity'");

            return entity.getDistanceSq(closestEntity.getPositionVector().add(offsets));
        }

        return originalDistanceSq;
    }
}
