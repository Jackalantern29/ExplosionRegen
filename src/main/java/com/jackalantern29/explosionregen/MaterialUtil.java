package com.jackalantern29.explosionregen;

import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialUtil {
    public static Material getMaterial(String material) {
        material = material.toUpperCase();
        switch (material) {
            case "PORTAL":
            case "NETHER_PORTAL":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("NETHER_PORTAL");
                else
                    return Material.getMaterial("PORTAL");
            case "ENCHANTMENT_TABLE":
            case "ENCHANTING_TABLE":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("ENCHANTING_TABLE");
                else
                    return Material.getMaterial("ENCHANTMENT_TABLE");
            case "END_PORTAL_FRAME":
            case "ENDER_PORTAL_FRAME":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("END_PORTAL_FRAME");
                else
                    return Material.getMaterial("ENDER_PORTAL_FRAME");
            case "END_PORTAL":
            case "ENDER_PORTAL":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("END_PORTAL");
                else
                    return Material.getMaterial("ENDER_PORTAL");
            case "COMMAND_BLOCK":
            case "COMMAND":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("COMMAND_BLOCK");
                else
                    return Material.getMaterial("COMMAND");
            case "CHAIN_COMMAND_BLOCK":
            case "COMMAND_CHAIN":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("CHAIN_COMMAND_BLOCK");
                else
                    return Material.getMaterial("COMMAND_CHAIN");
            case "REPEATING_COMMAND_BLOCK":
            case "COMMAND_REPEATING":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("REPEATING_COMMAND_BLOCK");
                else
                    return Material.getMaterial("COMMAND_REPEATING");
        }
        return Material.getMaterial(material);
    }

    public static boolean equalsMaterial(Material source, String match) {
        Material material = getMaterial(match);
        if(material == null)
            return false;
        return source.equals(material);
    }

    public static ItemStack parseItemStack(String material, int amount) {
        if(getMaterial(material) == null)
            return null;
        Material mat = getMaterial(material);
        if(amount <= 0)
            amount = 1;
        return new ItemStack(mat, amount);
    }

    public static ItemStack parseItemStack(String material) {
        return parseItemStack(material, 1);
    }


}
