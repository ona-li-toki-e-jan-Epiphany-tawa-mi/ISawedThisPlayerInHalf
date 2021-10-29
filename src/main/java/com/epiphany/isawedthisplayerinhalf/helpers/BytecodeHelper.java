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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.Random;

/**
 * TODO Redo this docstring once done with refactor.
 * TODO Prune methods in this when refactor is done.
 * Helper class for calling functions and reading values that can't be done in pure bytecode because of obfuscation or other reasons.
 */
@SuppressWarnings("unused")
public class BytecodeHelper {
    private static final Field closestEntityField;
    private static final Random random;

    static {
        closestEntityField = ReflectionHelper.getFieldOrNull(LookAtGoal.class, "closestEntity", "field_75334_a");
        ReflectionHelper.makeAccessible(closestEntityField);

        if (closestEntityField == null)
            throw new NullPointerException("Unable to find field 'closestEntityField' under names 'closestEntity' and 'field_75334_a'");

        random = new Random();
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
     * Gets the owner of a fishing bobber.
     *
     * @param fishingBobberEntity The fishing bobber to get the angler of.
     *
     * @return The angler.
     */
    public static PlayerEntity getAngler(FishingBobberEntity fishingBobberEntity) {
        return fishingBobberEntity.getAngler();
    }

    /**
     * Gets the player an interaction manager contains.
     *
     * @param playerInteractionManager The interaction manager to get the player from.
     *
     * @return The player in the interaction manager.
     */
    public static ServerPlayerEntity getPlayerFromManager(PlayerInteractionManager playerInteractionManager) {
        return playerInteractionManager.player;
    }

    /**
     * Gets the entity that the look at goal is focused on.
     *
     * @param lookAtGoal The look at goal to get the closest entity of.
     *
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
     *
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
     * Offsets a vector with the offsets of an entity.
     *
     * @param vec3d The vector to offset.
     * @param entity The entity to get the offsets from.
     *
     * @return The offset vector.
     */
    public static Vec3d offsetVector(Vec3d vec3d, Entity entity) {
        Vec3d offsets = Offsetter.getOffsets(entity);

       return !offsets.equals(Vec3d.ZERO) ? vec3d.add(offsets) : vec3d;
    }

    public static AxisAlignedBB offsetAxisAlignedBB(AxisAlignedBB axisAlignedBB, Entity entity) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        return !offsets.equals(Vec3d.ZERO) ? axisAlignedBB.offset(offsets) : axisAlignedBB;
    }

    /**
     * Offsets a projectile based on the offset of its shooter.
     *
     * @param projectile The projectile to offset the position of.
     * @param shooter The shooter of the projectile.
     */
    public static void offsetProjectile(Entity projectile, LivingEntity shooter) {
        Vec3d offsets = Offsetter.getOffsets((PlayerEntity) shooter);

        if (!offsets.equals(Vec3d.ZERO))
            projectile.setPosition(
                    projectile.getPosX() + offsets.x,
                    projectile.getPosY() + offsets.y,
                    projectile.getPosZ() + offsets.z
            );
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
     * TODO Prune parameter types.
     * Gets the corrected distance squared from an entity to the player.
     *
     * @param entity The entity to use for the first position.
     * @param player The player to use for the second position.
     *
     * @return The distance, squared, between the entity and the player.
     */
    public static double modifiedGetDistanceSq(Entity entity, PlayerEntity player) {
        Vec3d offsets = Offsetter.getOffsets(player);

        return !offsets.equals(Vec3d.ZERO) ? entity.getDistanceSq(player.getPositionVec().add(offsets)) : entity.getDistanceSq(player);
    }

    /**
     * Gets The offsets of the player than an interaction manager contains.
     *
     * @param playerInteractionManager The interaction manager to get the offsets from.
     *
     * @return The offsets of the player in the interaction manager.
     */
    public static Vec3d getOffsetsFromManager(PlayerInteractionManager playerInteractionManager) {
        return Offsetter.getOffsets(playerInteractionManager.player);
    }

    public static Vec3d applyLookAtOffsetsRandomly(double x, double y, double z, LookAtGoal lookAtGoal) {
        Vec3d offsets = Offsetter.getOffsets(getClosestEntity(lookAtGoal));

        if (offsets.equals(Vec3d.ZERO) || random.nextBoolean())
            return new Vec3d(x, y, z);

        return new Vec3d(x + offsets.x, y + offsets.y, z + offsets.z);
    }

    public static Vec3d getAnglerOffsets(FishingBobberEntity fishingBobberEntity) {
        PlayerEntity angler = fishingBobberEntity.getAngler();

        return angler != null ? Offsetter.getOffsets(fishingBobberEntity.getAngler()) : Vec3d.ZERO;
    }

    public static Vec3d offsetVectorWithAngler(Vec3d vec3d, FishingBobberEntity fishingBobberEntity) {
        return vec3d.add(getAnglerOffsets(fishingBobberEntity));
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
     * Gets the corrected distance from an entity to the player.
     *
     * @param entity1 The entity to use for the first position.
     * @param entity2 The entity to use for the second position.
     *
     * @return The distance between the first entity and second entity.
     */
    public static float modifiedGetDistance(Entity entity1, Entity entity2) {
        return entity2 instanceof PlayerEntity ? (float) Math.sqrt(BytecodeHelper.modifiedGetDistanceSq(entity1, (PlayerEntity) entity2)) :
                entity1.getDistance(entity2);
    }

    public static Vec3d getOffsetsInverselyScaled(Entity entity, float inverseScalar) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        if (!offsets.equals(Vec3d.ZERO)) {
            inverseScalar = 1 / inverseScalar;
            return offsets.mul(inverseScalar, inverseScalar, inverseScalar);

        } else
            return offsets;
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
}
