package com.bencvt.minecraft.buildregion;

import libshapedraw.primitive.Color;
import libshapedraw.primitive.ReadonlyColor;

/**
 * Encapsulate a non-null BuildMode value along with associated Colors that are
 * automatically animated every time the BuildMode changes.
 * 
 * @author bencvt
 */
public class BuildModeValue implements ReadonlyBuildModeValue {
    public static final long ANIM_DURATION = 500;

    private BuildMode value;
    private final Color colorVisible;
    private final Color colorHidden;

    public BuildModeValue(BuildMode value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;
        this.colorVisible = value.colorVisible.copy();
        this.colorHidden = value.colorHidden.copy();
    }

    @Override
    public ReadonlyColor getColorVisible() {
        return colorVisible;
    }

    @Override
    public ReadonlyColor getColorHidden() {
        return colorHidden;
    }

    @Override
    public BuildMode getValue() {
        return value;
    }

    public void setValueNoAnimation(BuildMode value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;

        colorVisible.animateStop().set(value.colorVisible);
        colorHidden.animateStop().set(value.colorHidden);
    }

    public void setValue(BuildMode value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;

        colorVisible.animateStart(value.colorVisible, ANIM_DURATION);
        colorVisible.animateStart(value.colorHidden, ANIM_DURATION);
    }
}
