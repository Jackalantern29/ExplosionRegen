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
            case "WATCH":
            case "CLOCK":
                if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("CLOCK");
                else
                    return Material.getMaterial("WATCH");
            case "PINK_DYE":
            case "LIME_DYE":
            case "GRAY_DYE":
            case "PURPLE_DYE":
            case "LIGHT_BLUE_DYE":
                if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial(material);
                else
                    return Material.getMaterial("INK_SACK");
            case "LIGHT_GRAY_STAINED_GLASS":
            case "LIME_STAINED_GLASS":
                if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial(material);
                else
                    return Material.getMaterial("STAINED_GLASS");
            case "PISTON_EXTENSION":
            case "PISTON_HEAD":
                if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("PISTON_HEAD");
                else
                    return Material.getMaterial("PISTON_EXTENSION");
            case "STICKY_PISTON":
            case "PISTON_STICKY_BASE":
                if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("STICKY_PISTON");
                else
                    return Material.getMaterial("PISTON_STICKY_BASE");
            case "PISTON":
            case "PISTON_BASE":
                if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                    return Material.getMaterial("PISTON");
                else
                    return Material.getMaterial("PISTON_BASE");
        }
        return Material.getMaterial(material);
    }

    public static boolean equalsMaterial(Material source, String match) {
        Material material = getMaterial(match);
        if(material == null)
            return false;
        return source.equals(material);
    }

    public static boolean isIndestructible(Material material) {
        if(material.equals(getMaterial("ENDER_CHEST")) || material.equals(getMaterial("OBSIDIAN")) || material.equals(getMaterial("ENCHANTMENT_TABLE")) || material.equals(getMaterial("ANVIL")) || material.equals(getMaterial("STRUCTURE_BLOCK")) || material.equals(getMaterial("STRUCTURE_VOID")) || material.equals(getMaterial("END_PORTAL_FRAME")) || material.equals(getMaterial("END_PORTAL")) || material.equals(getMaterial("END_GATEWAY")) || material.equals(getMaterial("COMMAND_BLOCK")) || material.equals(getMaterial("CHAIN_COMMAND_BLOCK")) || material.equals(getMaterial("REPEATING_COMMAND_BLOCK")) || material.equals(getMaterial("BEDROCK")) || material.equals(getMaterial("BARRIER")))
            return true;
        else
            return false;

    }
    public static ItemStack parseItemStack(String material, int amount) {
        if(getMaterial(material) == null)
            return null;
        short data = 0;
        Material mat = getMaterial(material);
        if(amount <= 0)
            amount = 1;
        if(UpdateType.isPreUpdate(UpdateType.COLOR_UPDATE)) {
            if(material.toUpperCase().contains("STAINED_GLASS")) {
                String color = material.toUpperCase().replace("STAINED_GLASS", "");
                if (color.charAt(color.length()-1) == '_')
                    color = color.substring(0, color.length() - 1);
                if (color.equals("LIGHT_GRAY"))
                    data = 8;
            } else if(material.toUpperCase().contains("DYE")) {
                String color = material.toUpperCase().replace("DYE", "");
                if (color.charAt(color.length()-1) == '_')
                    color = color.substring(0, color.length() - 1);
                if(color.equals("PURPLE"))
                    data = 5;
                else if (color.equals("LIGHT_GRAY"))
                    data = 7;
                else if(color.equals("GRAY"))
                    data = 8;
                else if(color.equals("PINK"))
                    data = 9;
                else if(color.equals("LIME"))
                    data = 10;
                else if(color.equals("LIGHT_BLUE"))
                    data = 12;
            }
        }
        return new ItemStack(mat, amount, data);
    }

    public static ItemStack parseItemStack(String material) {
        return parseItemStack(material, 1);
    }

    public static boolean requiresGroundSupport(Material material) {
        if(material.isTransparent())
            return true;
        if(material.name().contains("PRESSURE_PLATE"))
            return true;
        if(material.name().equals("CORNFLOWER"))
            return true;
        return false;
    }


}
