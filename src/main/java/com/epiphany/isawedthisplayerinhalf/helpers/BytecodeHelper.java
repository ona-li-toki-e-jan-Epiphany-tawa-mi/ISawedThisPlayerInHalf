package com.epiphany.isawedthisplayerinhalf.helpers;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * Helper class for calling functions and accessing fields that can't be done in pure bytecode thanks to obfuscation,
 *  and for moving as much code into Java as possible to increase maintainability.
 */
@SuppressWarnings("unused")
public class BytecodeHelper {
    // LookAtGoal.
    private static final Field FIELD_entity;
    private static final Field FIELD_closestEntity;
    private static final Field FIELD_maxDistance;
    // EntityPredicate.
    private static final Field FIELD_allowInvulnerable;
    private static final Field FIELD_customPredicate;
    private static final Field FIELD_skipAttackChecks;
    private static final Field FIELD_friendlyFire;
    private static final Field FIELD_distance;
    private static final Field FIELD_useVisibilityModifier;
    private static final Field FIELD_requireLineOfSight;

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

        // EntityPredicate.
        FIELD_allowInvulnerable = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "allowInvulnerable", "field_221018_c");
        ReflectionHelper.makeAccessible(FIELD_allowInvulnerable);
        FIELD_customPredicate = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "customPredicate", "field_221023_h");
        ReflectionHelper.makeAccessible(FIELD_customPredicate);
        FIELD_skipAttackChecks = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "skipAttackChecks", "field_221021_f");
        ReflectionHelper.makeAccessible(FIELD_skipAttackChecks);
        FIELD_friendlyFire = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "friendlyFire", "field_221019_d");
        ReflectionHelper.makeAccessible(FIELD_friendlyFire);
        FIELD_distance = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "distance", "field_221017_b");
        ReflectionHelper.makeAccessible(FIELD_distance);
        FIELD_useVisibilityModifier = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "useVisibilityModifier", "field_221022_g");
        ReflectionHelper.makeAccessible(FIELD_useVisibilityModifier);
        FIELD_requireLineOfSight = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "requireLineOfSight", "field_221020_e");
        ReflectionHelper.makeAccessible(FIELD_requireLineOfSight);

        if (FIELD_allowInvulnerable == null)
            throw new NullPointerException("Unable to find field 'FIELD_allowInvulnerable' under names 'allowInvulnerable' and 'field_221018_c'");
        if (FIELD_customPredicate == null)
            throw new NullPointerException("Unable to find field 'FIELD_customPredicate' under names 'customPredicate' and 'field_221023_h'");
        if (FIELD_skipAttackChecks == null)
            throw new NullPointerException("Unable to find field 'FIELD_skipAttackChecks' under names 'skipAttackChecks' and 'field_221021_f'");
        if (FIELD_friendlyFire == null)
            throw new NullPointerException("Unable to find field 'FIELD_friendlyFire' under names 'friendlyFire' and 'field_221019_d'");
        if (FIELD_distance == null)
            throw new NullPointerException("Unable to find field 'FIELD_distance' under names 'distance' and 'field_221017_b'");
        if (FIELD_useVisibilityModifier == null)
            throw new NullPointerException("Unable to find field 'FIELD_useVisibilityModifier' under names 'useVisibilityModifier' and 'field_221022_g'");
        if (FIELD_requireLineOfSight == null)
            throw new NullPointerException("Unable to find field 'FIELD_requireLineOfSight' under names 'requireLineOfSight' and 'field_221020_e'");


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
     * Gets the player that is closest to the target entity, or null, if nothing is found.
     * Corrected for offsets.
     *
     * @see net.minecraft.world.IEntityReader#getClosestPlayer(EntityPredicate, LivingEntity, double, double, double).
     *
     * @param world The world the target is in.
     * @param predicate A predicate to control which players can be targeted.
     * @param target The target entity.
     * @param targetX The x-position of the target entity.
     * @param targetY The y-position of the target entity.
     * @param targetZ The z-position of the target entity.
     *
     * @return The player closest to the target, or null.
     */
    public static PlayerEntity modifiedGetClosestPlayer(World world, EntityPredicate predicate, LivingEntity target, double targetX, double targetY, double targetZ) {
        List<? extends PlayerEntity> players =  target.world.getPlayers();
        PlayerEntity closestPlayer = null;
        double smallestDistance = Double.MAX_VALUE;

        for (PlayerEntity playerEntity : players)
            if (modifiedCanTarget(predicate, target, playerEntity)) {
                double distance = Math.min(playerEntity.getDistanceSq(targetX, targetY, targetZ), modifiedGetDistanceSq(playerEntity, targetX, targetY, targetZ));

                if (distance < smallestDistance) {
                    closestPlayer = playerEntity;
                    smallestDistance = distance;
                }
            }

        return closestPlayer;
    }

    /**
     * Gets whether a player can be targeted with the given predicate, accounting for offsets if the target is a player.
     *
     * @param predicate A predicate to control whether the entity can be targeted.
     * @param attacker The entity attempting to target.
     * @param target The player being targeted.
     *
     * @return Whether the target can be targeted.
     */
    public static boolean modifiedCanTarget(EntityPredicate predicate, LivingEntity attacker, PlayerEntity target) {
        if (attacker == target) {
            return false;

        } else if (target.isSpectator() || !target.isAlive()
                || (!((boolean) ReflectionHelper.getValueOrDefault(FIELD_allowInvulnerable, predicate, false)) && target.isInvulnerable())) {
            return false;

        } else {
            Predicate<LivingEntity> customPredicate = (Predicate<LivingEntity>) ReflectionHelper.getValueOrDefault(FIELD_customPredicate, predicate, null);

            if (customPredicate != null && !customPredicate.test(target)) {
                return false;

            } else {
                if (attacker != null) {
                    if (!((boolean) ReflectionHelper.getValueOrDefault(FIELD_skipAttackChecks, predicate, false))
                            && (!attacker.canAttack(target) || !attacker.canAttack(target.getType())))
                        return false;

                    if (!((boolean) ReflectionHelper.getValueOrDefault(FIELD_friendlyFire, predicate, false)) && attacker.isOnSameTeam(target))
                        return false;


                    double distance = (double) ReflectionHelper.getValueOrDefault(FIELD_distance, predicate, -1.0);

                    if (distance > 0.0) {
                        double visibilityModifier = ((boolean) ReflectionHelper.getValueOrDefault(FIELD_useVisibilityModifier, predicate, true))
                                ? target.getVisibilityMultiplier(attacker) : 1.0;
                        double visibleDistance = distance * visibilityModifier;

                        if (Math.min(attacker.getDistanceSq(target.getPosX(), target.getPosY(), target.getPosZ()), modifiedGetDistance(attacker, target)) > visibleDistance * visibleDistance)
                            return false;
                    }


                    if (!((boolean) ReflectionHelper.getValueOrDefault(FIELD_requireLineOfSight, predicate, false)) && attacker instanceof MobEntity
                            && !((MobEntity) attacker).getEntitySenses().canSee(target))
                        return false;
                }

                return true;
            }
        }
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
     * Offsets a vector by subtracting the offsets of an entity.
     *
     * @param vector The vector to offset.
     * @param entity The entity to get the offsets from.
     *
     * @return The vector that is inversely offset.
     */
    public static Vec3d offsetVectorInversely(Vec3d vector, Entity entity) {
        Vec3d offsets = Offsetter.getOffsets(entity);

        return !offsets.equals(Vec3d.ZERO) ? vector.subtract(offsets) : vector;
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
     * Plays a sound, offsetting its position with the offsets of the offset entity.
     *
     * @param world The world for the sound to occur in.
     * @param player A player to exclude from those who can hear it.
     * @param x The x-position of the sound.
     * @param y The y-position of the sound.
     * @param z The z-position of the sound.
     * @param soundIn The sound to play.
     * @param category The category of the sound.
     * @param volume The volume of the sound.
     * @param pitch The pitch of the sound.
     * @param offsetEntity The entity to get the offsets from.
     */
    public static void modifiedPlaySound(World world, PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, LivingEntity offsetEntity) {
        Vec3d offsets = Offsetter.getOffsets(offsetEntity);

        if (!offsets.equals(Vec3d.ZERO)) {
            world.playSound(player, x + offsets.x, y + offsets.y, z + offsets.z, soundIn, category, volume, pitch);

        } else
            world.playSound(player, x, y, z, soundIn, category, volume, pitch);
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

    /**
     * Redoes the check in {@link LivingEntity#canEntityBeSeen(Entity)} so that mobs can see players' torsos, even if the legs are not in view.
     *
     * @param originalResult The original result of the function.
     * @param livingEntity The entity that is attempting to see.
     * @param entity The entity that is possibly being seen.
     * @param livingEntityPosition The position of the seeing entity.
     * @param entityPosition The position of the (maybe) seen entity.
     *
     * @return Whether the entity can bee seen by the other.
     */
    public static boolean redoCanEntityBeSeen(boolean originalResult, LivingEntity livingEntity, Entity entity, Vec3d livingEntityPosition, Vec3d entityPosition) {
        return originalResult || livingEntity.world.rayTraceBlocks(new RayTraceContext(livingEntityPosition, offsetVector(entityPosition, entity), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, livingEntity)).getType() == RayTraceResult.Type.MISS;
    }
}
