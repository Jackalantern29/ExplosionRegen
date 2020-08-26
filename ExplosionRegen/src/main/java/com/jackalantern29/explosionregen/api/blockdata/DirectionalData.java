package com.jackalantern29.explosionregen.api.blockdata;

import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class DirectionalData extends RegenBlockData {
    public DirectionalData(Material material) {
        super(material);
    }

    public DirectionalData(Material material, byte data) {
        super(material, data);
    }

    public DirectionalData(Block block) {
        super(block);
    }

    public BlockFace getFacing() {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof org.bukkit.block.data.Directional) {
                org.bukkit.block.data.Directional direction = (org.bukkit.block.data.Directional)getBlockData();
                return direction.getFacing();
            }
        } else {
            if(getBlockData() instanceof org.bukkit.material.Directional) {
                org.bukkit.material.Directional direction = (org.bukkit.material.Directional) getBlockData();
                return direction.getFacing();
            }
        }
        return null;
    }

    public void setFacing(BlockFace facing) {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof org.bukkit.block.data.Directional) {
                org.bukkit.block.data.Directional direction = (org.bukkit.block.data.Directional)getBlockData();
                direction.setFacing(facing);
            }
        } else {
            if(getBlockData() instanceof org.bukkit.material.Directional) {
                org.bukkit.material.Directional direction = (org.bukkit.material.Directional) getBlockData();
                direction.setFacingDirection(facing);
            }
        }
    }
}
