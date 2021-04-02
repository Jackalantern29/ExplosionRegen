package com.jackalantern29.erspecialeffects.particledata;

import org.bukkit.Color;

public class DustColor {
    private Color color = Color.RED;
    private float size = 1;

    public DustColor(Color color, float size) {
        this.color = color;
        this.size = size;
    }

    public Color getColor() {
        return color;
    }

    public int getRed() {
        return color.getRed();
    }

    public int getGreen() {
        return color.getGreen();
    }

    public int getBlue() {
        return color.getBlue();
    }

    public float getSize() {
        return size;
    }
}
