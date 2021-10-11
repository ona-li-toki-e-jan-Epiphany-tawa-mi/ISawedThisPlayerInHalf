package com.epiphany.isawedthisplayerinhalf.helpers;

import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;

/**
 * I can't (don't want to) account for name mappings in the bytecode itself, so I'm using these helper functions.
 *  (a.k.a letting Forge and MCP do the work.)
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
}
