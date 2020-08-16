package com.jackalantern29.explosionregen.api.blockdata;

import com.jackalantern29.explosionregen.BukkitMethods;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

public class RegenBlockData {
    private Material material;
    private byte data = 0;
    Object blockData;

    public RegenBlockData(Material material) {
        this.material = material;
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            blockData = BukkitMethods.createBlockData(material);
        } else {
            blockData = new MaterialData(material);
        }
        //Bukkit.getPlayer("Jack").sendMessage(toString());
    }

    public RegenBlockData(Material material, byte data) {
        this(material);
        this.data = data;
        blockData = new MaterialData(material, data);
    }
    public RegenBlockData(Block block) {
        this(block.getState().getType());
    }

    public Material getMaterial() {
        return material;
    }

    public Object getBlockData() {
        return blockData;
    }

    @Override
    public String toString() {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
            return ((BlockData) blockData).getAsString();
        else
            return blockData.toString();
    }
}
