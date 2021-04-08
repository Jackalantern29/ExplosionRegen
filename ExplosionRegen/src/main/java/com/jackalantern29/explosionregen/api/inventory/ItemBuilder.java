package com.jackalantern29.explosionregen.api.inventory;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemBuilder {
    private ItemStack item;
    private ItemMeta meta;
    private List<String> lore;

    public ItemBuilder(ItemStack item) {
        this.item = new ItemStack(item);
        this.meta = item.getItemMeta();
        this.lore = (meta != null && meta.hasLore()) ? meta.getLore() : new ArrayList<>();
    }

    public ItemBuilder(Material material) {
        this(new ItemStack(material));
    }

    public ItemBuilder setDisplayName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder setLore(String[] lore) {
        meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder setLine(int index, String line) {
        if(lore.size() > index)
            lore.set(index, line);
        else
            lore.add(index, line);
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder setGlow(boolean glow) {
        if(glow) {
            if(!meta.hasEnchants())
                meta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1, false);
            if(!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS))
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            if(meta.hasEnchants())
                for(Enchantment enchant : meta.getEnchants().keySet())
                    meta.removeEnchant(enchant);
            if(meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS))
                meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
