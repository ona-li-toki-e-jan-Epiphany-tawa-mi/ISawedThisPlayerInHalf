package com.epiphany.isawedthisplayerinhalf.helpers;

import com.epiphany.isawedthisplayerinhalf.Offsetter;
import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.particles.IParticleData;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/*
 * MIT License
 *
 * Copyright (c) 2021-2022 ona-li-toki-e-jan-Epiphany-tawa-mi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*/

/**
 * Helper class for calling functions and accessing fields that can't be done in pure bytecode thanks to obfuscation,
 *  and for moving as much code into Java as possible to increase maintainability.
 */
@SuppressWarnings("unused")
public class BytecodeHelper {
    // EndermanEntity.
    private static final Field FIELD_enderman;
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
    // TemptGoal.
    private static final Field FIELD_targetX;
    private static final Field FIELD_targetY;
    private static final Field FIELD_targetZ;
    private static final Field FIELD_closestPlayer;
    // PathNavigator.
    private static final Method METHOD_func_225464_a;
    // LookController.
    private static final Method METHOD_getEyePosition;

    private static final Random RANDOM = new Random();

    static {
        // EndermanEntity.
        Class<?> stareGoal = ReflectionHelper.classForNameOrNull("net.minecraft.entity.monster.EndermanEntity$StareGoal");

        if (stareGoal == null)
            throw new NullPointerException("Unable to find class 'net.minecraft.entity.monster.EndermanEntity$StareGoal'");

        FIELD_enderman = ReflectionHelper.getDeclaredFieldOrNull(stareGoal, "enderman", "field_220835_a");

        if (FIELD_enderman == null)
            throw new NullPointerException("Unable to find field 'FIELD_enderman' under names 'enderman' and 'field_220835_a'");

        // LookAtGoal.
        FIELD_entity = ReflectionHelper.getDeclaredFieldOrNull(LookAtGoal.class, "entity", "field_75332_b");
        FIELD_closestEntity = ReflectionHelper.getDeclaredFieldOrNull(LookAtGoal.class, "closestEntity", "field_75334_a");
        FIELD_maxDistance = ReflectionHelper.getDeclaredFieldOrNull(LookAtGoal.class, "maxDistance", "field_75333_c");

        if (FIELD_entity == null)
            throw new NullPointerException("Unable to find field 'FIELD_entity' under names 'entity' and 'field_75332_b'");
        if (FIELD_closestEntity == null)
            throw new NullPointerException("Unable to find field 'FIELD_closestEntity' under names 'closestEntity' and 'field_75334_a'");
        if (FIELD_maxDistance == null)
            throw new NullPointerException("Unable to find field 'FIELD_maxDistance' under names 'maxDistance' and 'field_75333_c'");

        // EntityPredicate.
        FIELD_allowInvulnerable = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "allowInvulnerable", "field_221018_c");
        FIELD_customPredicate = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "customPredicate", "field_221023_h");
        FIELD_skipAttackChecks = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "skipAttackChecks", "field_221021_f");
        FIELD_friendlyFire = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "friendlyFire", "field_221019_d");
        FIELD_distance = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "distance", "field_221017_b");
        FIELD_useVisibilityModifier = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "useVisibilityModifier", "field_221022_g");
        FIELD_requireLineOfSight = ReflectionHelper.getDeclaredFieldOrNull(EntityPredicate.class, "requireLineOfSight", "field_221020_e");

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

        // TemptGoal.
        FIELD_targetX = ReflectionHelper.getDeclaredFieldOrNull(TemptGoal.class, "targetX", "field_75283_c");
        FIELD_targetY = ReflectionHelper.getDeclaredFieldOrNull(TemptGoal.class, "targetY", "field_75280_d");
        FIELD_targetZ = ReflectionHelper.getDeclaredFieldOrNull(TemptGoal.class, "targetZ", "field_75281_e");
        FIELD_closestPlayer = ReflectionHelper.getDeclaredFieldOrNull(TemptGoal.class, "closestPlayer", "field_75289_h");

        if (FIELD_targetX == null)
            throw new NullPointerException("Unable to find field 'FIELD_targetX' under names 'targetX' and 'field_75283_c'");
        if (FIELD_targetY == null)
            throw new NullPointerException("Unable to find field 'FIELD_targetY' under names 'targetY' and 'field_75280_d'");
        if (FIELD_targetZ == null)
            throw new NullPointerException("Unable to find field 'FIELD_targetZ' under names 'targetZ' and 'field_75281_e'");
        if (FIELD_closestPlayer == null)
            throw new NullPointerException("Unable to find field 'FIELD_closestPlayer' under names 'closestPlayer' and 'field_75289_h'");

        // PathNavigator.
        METHOD_func_225464_a = ReflectionHelper.getDeclaredMethodOrNull(
                PathNavigator.class,
                "func_225464_a",
                Set.class, int.class, boolean.class, int.class);

        if (METHOD_func_225464_a == null)
            throw new NullPointerException("Unable to find field 'METHOD_func_225464_a' under name 'func_225464_a'");

        // LookController.
        METHOD_getEyePosition = ReflectionHelper.getDeclaredMethodOrNull(
                LookController.class,
                "getEyePosition", "func_220676_b",
                Entity.class);

        if (METHOD_getEyePosition == null)
            throw new NullPointerException("Unable to find field 'METHOD_getEyePosition' under names 'getEyePosition' and 'func_220676_b'");
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
     * Either returns the LivingEntity's offsets or the zero vector, randomly.
     *
     * @param livingEntity The LivingEntity to possibly return the offsets of.
     *
     * @return Either the LivingEntity's offsets or the zero vector.
     */
    public static Vec3d getOffsetsRandomly(LivingEntity livingEntity) {
        return RANDOM.nextBoolean() ? Offsetter.getOffsets(livingEntity) : Vec3d.ZERO;
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
     * Gets the minimum distance squared from an entity to an offset one.
     *
     * @param entity The first entity to get the distance from.
     * @param offsetEntity The second, offset, entity to get the distance from.
     * @param originalDistanceSq The original distance squared between the two - without accounting for offsets.
     *
     * @return The minmum distance squared between the two entities.
     */
    public static double getMinimumDistanceSq(Entity entity, Entity offsetEntity, double originalDistanceSq) {
        Vec3d offsets = Offsetter.getOffsets(offsetEntity);

        return !offsets.equals(Vec3d.ZERO) ?
                Math.min(entity.getDistanceSq(offsetEntity.getPositionVec().add(offsets)), originalDistanceSq) :
                originalDistanceSq;
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
     * Corrected for offsets, checking only the players offset position.
     *
     * @see net.minecraft.world.IEntityReader#getClosestPlayer(EntityPredicate, LivingEntity).
     *
     * @param world The world the target is in.
     * @param predicate A predicate to control which players can be targeted.
     * @param target The target entity.
     *
     * @return The player closest to the target, or null.
     */
    public static PlayerEntity modifiedGetClosestPlayerOF(World world, EntityPredicate predicate, LivingEntity target) {
        return modifiedGetClosestPlayer(world, predicate, target, BytecodeHelper::modifiedGetDistanceSq);
    }

    /**
     * Gets the player that is closest to the target entity, or null, if nothing is found.
     * Corrected for offsets, checking both the player's normal and offset positions.
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
    public static PlayerEntity modifiedGetClosestPlayerNOOF(World world, EntityPredicate predicate, LivingEntity target, double targetX, double targetY, double targetZ) {
        return modifiedGetClosestPlayer(world, predicate, target, (livingTarget, playerEntity) ->
            Math.min(playerEntity.getDistanceSq(targetX, targetY, targetZ), modifiedGetDistanceSq(playerEntity, targetX, targetY, targetZ))
        );
    }

    /**
     * Gets the player that is closest to the target entity, or null, if nothing is found.
     * Corrected for offsets, checking both the player's normal and offset positions.
     *
     * @see net.minecraft.world.IEntityReader#getClosestPlayer(EntityPredicate, LivingEntity).
     *
     * @param world The world the target is in.
     * @param predicate A predicate to control which players can be targeted.
     * @param target The target entity.
     *
     * @return The player closest to the target, or null.
     */
    public static PlayerEntity modifiedGetClosestPlayerNOOF(World world, EntityPredicate predicate, LivingEntity target) {
        return modifiedGetClosestPlayer(world, predicate, target, (livingTarget, playerEntity) ->
            Math.min(playerEntity.getDistanceSq(target), modifiedGetDistanceSq(target, playerEntity))
        );
    }

    /**
     * Gets the player that is closest to the target entity, or null, if nothing is found.
     *
     * @see net.minecraft.world.IEntityReader#getClosestPlayer(EntityPredicate, LivingEntity, double, double, double).
     *
     * @param world The world the target is in.
     * @param predicate A predicate to control which players can be targeted.
     * @param target The target entity.
     * @param distanceSqFunction A function that accepts the target and player and calculates the distance squared between them.
     *      Whether to account for those offsets will depend on application.
     *
     * @return The player closest to the target, or null.
     */
    public static PlayerEntity modifiedGetClosestPlayer(World world, EntityPredicate predicate, LivingEntity target, BiFunction<LivingEntity, PlayerEntity, Double> distanceSqFunction) {
        List<? extends PlayerEntity> players =  target.world.getPlayers();
        PlayerEntity closestPlayer = null;
        double smallestDistance = Double.MAX_VALUE;

        for (PlayerEntity playerEntity : players)
            if (modifiedCanTarget(predicate, target, playerEntity, distanceSqFunction)) {
                double distance = distanceSqFunction.apply(target, playerEntity);

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
     * @param target The entity being targeted.
     * @param distanceSqFunction A function that accepts the attacker and target (the one with offsets) and calculates the distance squared between them.
     *      Whether to account for those offsets will depend on application.
     *
     * @return Whether the target can be targeted.
     */
    public static boolean modifiedCanTarget(EntityPredicate predicate, LivingEntity attacker, PlayerEntity target, BiFunction<LivingEntity, PlayerEntity, Double> distanceSqFunction) {
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

                        if (distanceSqFunction.apply(attacker, target) > visibleDistance * visibleDistance)
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
     * Gets whether a player can be targeted with the given predicate, accounting for offsets if the target is a player.
     *
     * @param predicate A predicate to control whether the entity can be targeted.
     * @param attacker The entity attempting to target.
     * @param target The entity being targeted.
     *
     * @return Whether the target can be targeted.
     */
    public static boolean modifiedCanTargetNOOF(EntityPredicate predicate, LivingEntity attacker, LivingEntity target) {
        if (target instanceof PlayerEntity) {
            return modifiedCanTarget(predicate, attacker, (PlayerEntity) target, (livingTarget, playerEntity) ->
                    Math.min(livingTarget.getDistanceSq(livingTarget), modifiedGetDistanceSq(livingTarget, playerEntity))
            );

        } else
            return predicate.canTarget(attacker, target);
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
    public static void modifiedPlaySound(World world, @Nullable PlayerEntity player, double x, double y, double z, SoundEvent soundIn, SoundCategory category, float volume, float pitch, LivingEntity offsetEntity) {
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
     * Randomly switches the target location of an EndermanEntity.StareGoal between the target's original and offset bodies.
     *
     * @param lookController The enderman's look controller.
     * @param x The targeted x position.
     * @param eyePosition The target's eye y position.
     * @param z The targeted z position.
     * @param stareGoal The enderman's StareGoal. Must be an instance of StareGoal.
     */
    public static void applyLookAtOffsetsRandomly(LookController lookController, double x, double eyePosition, double z, Object stareGoal) {
        EndermanEntity enderman = (EndermanEntity) ReflectionHelper.getValueOrDefault(FIELD_enderman, stareGoal, null);
        if (enderman == null) throw new NullPointerException("Unable to get value from 'FIELD_enderman'");
        LivingEntity attackTarget = enderman.getAttackTarget();
        Vec3d offsets = Offsetter.getOffsets(attackTarget);

        if (offsets.equals(Vec3d.ZERO) || RANDOM.nextBoolean()) {
            lookController.setLookPosition(x, eyePosition, z);
            return;
        }

        lookController.setLookPosition(x + offsets.x, eyePosition + offsets.y, z + offsets.z);
    }

    /**
     * Duplicates a particle so that it appears at its origin and at the position that is the players' offsets plus the
     *   origin, if they have offsets.
     *
     * @param world The world to create the particle in.
     * @param particleData See {@link World#addParticle(IParticleData, double, double, double, double, double, double)}.
     * @param x See {@link World#addParticle(IParticleData, double, double, double, double, double, double)}.
     * @param y See {@link World#addParticle(IParticleData, double, double, double, double, double, double)}.
     * @param z See {@link World#addParticle(IParticleData, double, double, double, double, double, double)}.
     * @param xSpeed See {@link World#addParticle(IParticleData, double, double, double, double, double, double)}.
     * @param ySpeed See {@link World#addParticle(IParticleData, double, double, double, double, double, double)}.
     * @param zSpeed See {@link World#addParticle(IParticleData, double, double, double, double, double, double)}.
     * @param playerEntity The player with offsets.
     */
    public static void duplicateParticleOffset(World world, IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, PlayerEntity playerEntity) {
        Vec3d offsets = Offsetter.getOffsets(playerEntity);
        if (!offsets.equals(Vec3d.ZERO))
            world.addParticle(particleData, x + offsets.x, y + offsets.y, z + offsets.z, xSpeed, ySpeed, zSpeed);

        world.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    /**
     * Offsets the spawning of a particle with the given player's offsets.
     *
     * @param serverWorld The world to spawn the particle in.
     * @param type See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param posX See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param posY See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param posZ See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param particleCount See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param xOffset See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param yOffset See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param zOffset See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param speed See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     * @param playerEntity The player to get offsets from.
     * @param <T>  See {@link ServerWorld#spawnParticle(IParticleData, double, double, double, int, double, double, double, double)}.
     */
    public static <T extends IParticleData> int modifiedSpawnParticle(ServerWorld serverWorld, T type, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed, PlayerEntity playerEntity) {
        Vec3d offsets = Offsetter.getOffsets(playerEntity);
        return serverWorld.spawnParticle(type, posX + offsets.x, posY + offsets.y, posZ + offsets.z, particleCount, xOffset, yOffset, zOffset, speed);
    }

    /**
     * Offsets the target position of a TemptGoal.
     *
     * @param temptGoal The tempt goal to offset the target position of.
     */
    public static void offsetTargetPosition(TemptGoal temptGoal) {
        PlayerEntity closestPlayer = (PlayerEntity) ReflectionHelper.getValueOrDefault(FIELD_closestPlayer, temptGoal, null);
        if (closestPlayer == null) throw new NullPointerException("Unable to get value from 'TemptGoal'");

        Vec3d offsets = Offsetter.getOffsets(closestPlayer);

        ReflectionHelper.setValue(FIELD_targetX, temptGoal, offsets.x + (double) ReflectionHelper.getValueOrDefault(FIELD_targetX, temptGoal, closestPlayer.getPosX()));
        ReflectionHelper.setValue(FIELD_targetY, temptGoal, offsets.y + (double) ReflectionHelper.getValueOrDefault(FIELD_targetY, temptGoal, closestPlayer.getPosY()));
        ReflectionHelper.setValue(FIELD_targetZ, temptGoal, offsets.z + (double) ReflectionHelper.getValueOrDefault(FIELD_targetZ, temptGoal, closestPlayer.getPosZ()));
    }

    /**
     * Makes the entity of the given look controller look at an entity's offset position.
     *
     * @see LookController#setLookPositionWithEntity(Entity, float, float).
     *
     * @param lookController The entity's look controller.
     * @param offsetEntity The entity to look at the offset position of.
     * @param deltaYaw How fast to rotate the entity's yaw.
     * @param deltaPitch How fast to rotate the entity pitch.
     */
    public static void setLookPositionWithOffsetEntity(LookController lookController, Entity offsetEntity, float deltaYaw, float deltaPitch) {
        if (offsetEntity instanceof PlayerEntity) {
            Vec3d offsets = Offsetter.getOffsets(offsetEntity);

            if (!offsets.equals(Vec3d.ZERO)) {
                Double eyePosition = (Double) ReflectionHelper.invokeMethod(METHOD_getEyePosition, lookController, offsetEntity);
                if (eyePosition == null) throw new NullPointerException("Unable to invoke method in 'LookController'");

                lookController.setLookPosition(offsetEntity.getPosX() + offsets.x, eyePosition + offsets.y, offsetEntity.getPosZ() + offsets.z, deltaYaw, deltaPitch);
                return;
            }
        }

        lookController.setLookPositionWithEntity(offsetEntity, deltaYaw, deltaPitch);
    }

    /**
     * Uses entity path finding to try to navigate to an entity's offset position.
     *
     * @see net.minecraft.pathfinding.PathNavigator#tryMoveToEntityLiving(Entity, double).
     *
     * @param pathNavigator The entity's path navigator.
     * @param offsetTarget The entity to move to the offset position of.
     * @param speed How fast to move to the target.
     *
     * @return Whether the entity will try to navigate to that position (if a valid path exists.)
     */
    public static boolean tryMoveToOffsetEntityLiving(PathNavigator pathNavigator, Entity offsetTarget, double speed) {
        if (offsetTarget instanceof PlayerEntity) {
            Vec3d offsets = Offsetter.getOffsets((PlayerEntity) offsetTarget);

            if (!offsets.equals(Vec3d.ZERO)) {
                Path pathToEntity = (Path) ReflectionHelper.invokeMethod(METHOD_func_225464_a, pathNavigator,
                        ImmutableSet.of(new BlockPos(offsetTarget.getPositionVector().add(offsets))), 16, true, 1);
                return pathToEntity != null && pathNavigator.setPath(pathToEntity, speed);
            }
        }

        return pathNavigator.tryMoveToEntityLiving(offsetTarget, speed);
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
     * Redoes the range check for an enderman's StareGoal accounting for the target's offsets.
     *
     * @param originalDistanceSq The original distanced squared to the target.
     * @param stareGoal The enderman's StareGoal. Must be an instance of StareGoal.
     */
    public static double redoShouldExecuteRangeCheck(double originalDistanceSq, Object stareGoal) {
        EndermanEntity enderman = (EndermanEntity) ReflectionHelper.getValueOrDefault(FIELD_enderman, stareGoal, null);
        if (enderman == null) throw new NullPointerException("Unable to get value from 'FIELD_enderman'");
        LivingEntity attackTarget = enderman.getAttackTarget();
        Vec3d offsets = Offsetter.getOffsets(attackTarget);

        if (offsets.equals(Vec3d.ZERO))
            return originalDistanceSq;

        return Math.min(originalDistanceSq, enderman.getDistanceSq(attackTarget.getPositionVec().add(offsets)));
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
        if (!(entity instanceof PlayerEntity))
            return originalResult;

        return originalResult || livingEntity.world.rayTraceBlocks(new RayTraceContext(livingEntityPosition, offsetVector(entityPosition, entity), RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, livingEntity)).getType() == RayTraceResult.Type.MISS;
    }

    /**
     * Redoes the check in {@link LivingEntity#canEntityBeSeen(Entity)} so that mobs can see players' torsos, even if the legs are not in view.
     * TODO Shelved
     * @param livingEntity The entity that is attempting to see.
     * @param entity The entity that is possibly being seen.
     *
     * @return Whether the entity can bee seen by the other.
     */
    @Deprecated
    public static boolean modifiedCanEntityBeSeen(LivingEntity livingEntity, Entity entity) {
        return redoCanEntityBeSeen(livingEntity.canEntityBeSeen(entity), livingEntity, entity, livingEntity.getPositionVec(), entity.getPositionVec());
    }
}
