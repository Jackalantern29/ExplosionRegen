package com.jackalantern29.explosionregen.api.inventory;

import org.bukkit.Material;
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
        this.lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
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
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
