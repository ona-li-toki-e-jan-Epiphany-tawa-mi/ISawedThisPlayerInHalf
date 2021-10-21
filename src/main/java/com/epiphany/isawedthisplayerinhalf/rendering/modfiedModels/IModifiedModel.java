package com.epiphany.isawedthisplayerinhalf.rendering.modfiedModels;

/**
 * Adds the ability to set and unset offsets onto the modified models.
 */
public interface IModifiedModel {
    /**
     * Sets the offsets for the model.
     * When offsets need to be set to (0, 0, 0) use unsetOffsets instead.
     *
     * @param xOffset The offset in the x-axis.
     * @param yOffset The offset in the y-axis.
     * @param zOffset The offset in the z-axis.
     * @param offsetAngle The angle along the XZ plane that intersects the point (xOffset, zOffset).
     */
    void setOffsets(float xOffset, float yOffset, float zOffset, float offsetAngle);

    /**
     * Unsets the offsets for the model and resets parts that do not reset automatically.
     */
    void unsetOffsets();
}
