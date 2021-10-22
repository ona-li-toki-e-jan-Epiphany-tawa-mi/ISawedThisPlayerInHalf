package com.epiphany.isawedthisplayerinhalf.rendering;

/**
 * An object for storing the information necessary to offset the rendering of the player's body.
 */
public class RenderingOffsets {
    public float xOffset, yOffset, zOffset;
    public float yawOffset;

    public boolean shouldOffsetRender;

    /**
     * Creates a new instance of RenderingOffsets.
     *
     * @param xOffset The x-offset of the player.
     * @param yOffset The y-offset of the player.
     * @param zOffset The z-offset of the player.
     * @param yawOffset The yaw offset of the player.
     */
    public RenderingOffsets(float xOffset, float yOffset, float zOffset, float yawOffset) {
        setOffsets(xOffset, yOffset, zOffset, yawOffset);
    }

    /**
     * Sets offset values.
     *
     * @param xOffset The x-offset of the player.
     * @param yOffset The y-offset of the player.
     * @param zOffset The z-offset of the player.
     * @param yawOffset The yaw offset of the player.
     */
    public void setOffsets(float xOffset, float yOffset, float zOffset, float yawOffset) {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.yawOffset = yawOffset;

        this.shouldOffsetRender = xOffset != 0 || yOffset != 0 || zOffset != 0 || yawOffset != 0;
    }
}
