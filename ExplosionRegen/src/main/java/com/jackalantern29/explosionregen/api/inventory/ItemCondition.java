package com.jackalantern29.explosionregen.api.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ItemCondition {
    private HashMap<Boolean, ItemBuilder> map = new HashMap<>();

    public ItemCondition(ItemBuilder ifFalse, ItemBuilder ifTrue) {
        map.put(true, ifTrue);
        map.put(false, ifFalse);
    }

    public ItemBuilder getItemBuilder(boolean condition) {
        return map.get(condition);
    }

    public ItemStack build(boolean condition) {
        return map.get(condition).build();
    }
}
