package com.jackalantern29.explosionregen.api.blockdata;

import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;

public class ChestData extends DirectionalData {
    public ChestData(Material material) {
        super(material);
    }

    public ChestData(Material material, byte data) {
        super(material, data);
    }

    public ChestData(Block block) {
        super(block);
    }

    public ChestType getType() {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof Chest) {
                Chest chest = (Chest)getBlockData();
                switch(chest.getType()) {
                    case SINGLE:
                        return ChestType.SINGLE;
                    case LEFT:
                        return ChestType.LEFT;
                    case RIGHT:
                        return  ChestType.RIGHT;
                }
            }
        }
        return null;
    }

    public void setType(ChestType type) {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            if(getBlockData() instanceof Chest) {
                Chest chest = (Chest)getBlockData();
                switch(type) {
                    case SINGLE:
                        chest.setType(Chest.Type.SINGLE);
                        break;
                    case LEFT:
                        chest.setType(Chest.Type.LEFT);
                        break;
                    case RIGHT:
                        chest.setType(Chest.Type.RIGHT);
                        break;
                }
            }
        }
    }

    public enum ChestType {
        SINGLE,
        LEFT,
        RIGHT;
    }
}
