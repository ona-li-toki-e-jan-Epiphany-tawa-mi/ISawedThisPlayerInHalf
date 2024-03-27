package com.epiphany.isawedthisplayerinhalf.rendering;

/*
 * MIT License
 *
 * Copyright (c) 2021 ona-li-toki-e-jan-Epiphany-tawa-mi
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
 * An object for storing the information necessary to offset the rendering of the player's body.
 */
public class RenderingOffsets {
    private float xOffset, yOffset, zOffset;
    private float yawOffset;

    private boolean shouldOffsetRender;

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

    /**
     * Gets the x-offset of the player
     *
     * @return The player's x-offset.
     */
    public float getXOffset() {
        return this.xOffset;
    }

    /**
     * Gets the y-offset of the player
     *
     * @return The player's y-offset.
     */
    public float getYOffset() {
        return this.yOffset;
    }

    /**
     * Gets the z-offset of the player
     *
     * @return The player's z-offset.
     */
    public float getZOffset() {
        return this.zOffset;
    }

    /**
     * Gets the yaw offset of the player
     *
     * @return The player's yaw offset.
     */
    public float getYawOffset() {
        return this.yawOffset;
    }

    /**
     * Gets whether it is necessary to offset a render with the player's offsets.
     *
     * @return Whether it is necessary to offset a render with the player's offsets.
     */
    public boolean shouldOffsetRender() {
        return this.shouldOffsetRender;
    }
}
