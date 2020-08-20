package com.jackalantern29.explosionregen.api.blockdata;

import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Piston;
import org.bukkit.material.PistonBaseMaterial;

public class PistonData extends DirectionalData {
    public PistonData(Material material) {
        super(material);
    }

    public PistonData(Material material, byte data) {
        super(material, data);
    }

    public PistonData(Block block) {
        super(block);
    }

    public boolean isExtended() {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof Piston) {
                Piston piston = (Piston)getBlockData();
                return piston.isExtended();
            }
        } else {
            if(getBlockData() instanceof PistonBaseMaterial) {
                PistonBaseMaterial piston = (PistonBaseMaterial)getBlockData();
                piston.isPowered();
            }
        }
        return false;
    }

    public void setExtended(boolean extended) {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof Piston) {
                Piston piston = (Piston)getBlockData();
                piston.setExtended(extended);
            }
        } else {
            if(getBlockData() instanceof PistonBaseMaterial) {
                PistonBaseMaterial piston = (PistonBaseMaterial)getBlockData();
                piston.setPowered(extended);
            }
        }
    }
}
