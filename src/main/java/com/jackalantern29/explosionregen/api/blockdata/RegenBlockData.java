package com.jackalantern29.explosionregen.api.blockdata;

import org.bukkit.Material;
import org.bukkit.block.Block;

public class RegenBlockData {
    private Material material;
    private byte data = 0;
    public RegenBlockData(Material material) {
        this.material = material;
    }

    public RegenBlockData(Material material, byte data) {
        this(material);
        this.data = data;
    }
    public RegenBlockData(Block block) {
        this.material = block.getType();
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public String toString() {
        return material.name();
    }
}
