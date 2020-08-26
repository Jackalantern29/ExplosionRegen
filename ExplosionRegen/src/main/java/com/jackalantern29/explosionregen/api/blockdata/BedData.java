package com.jackalantern29.explosionregen.api.blockdata;

import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;

public class BedData extends DirectionalData {
    public BedData(Material material) {
        super(material);
    }

    public BedData(Material material, byte data) {
        super(material, data);
    }

    public BedData(Block block) {
        super(block);
    }

    public BedPart getPart() {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof Bed) {
                Bed bed = (Bed)getBlockData();
                switch(bed.getPart()) {
                    case HEAD:
                        return BedPart.HEAD;
                    case FOOT:
                        return BedPart.FOOT;
                }
            }
        } else {
            if(getBlockData() instanceof org.bukkit.material.Bed) {
                org.bukkit.material.Bed bed = (org.bukkit.material.Bed)getBlockData();
                if(bed.isHeadOfBed())
                    return BedPart.HEAD;
                else
                    return BedPart.FOOT;
            }
        }
        return null;
    }

    public void setPart(BedPart part) {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof Bed) {
                Bed bed = (Bed)getBlockData();
                switch(part) {
                    case HEAD:
                        bed.setPart(Bed.Part.HEAD);
                        break;
                    case FOOT:
                        bed.setPart(Bed.Part.FOOT);
                        break;
                }
            }
        } else {
            if(getBlockData() instanceof org.bukkit.material.Bed) {
                org.bukkit.material.Bed bed = (org.bukkit.material.Bed)getBlockData();
                switch(part) {
                    case HEAD:
                        bed.setHeadOfBed(true);
                        break;
                    case FOOT:
                        bed.setHeadOfBed(false);
                        break;
                }
            }
        }
    }

    public enum BedPart {
        HEAD,
        FOOT;
    }
}
