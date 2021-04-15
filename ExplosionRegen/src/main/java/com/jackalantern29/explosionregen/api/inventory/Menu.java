package com.jackalantern29.explosionregen.api.inventory;

import org.bukkit.entity.HumanEntity;

public abstract class Menu {
    public abstract void addItem(SlotElement element);

    public abstract void clear();

    public abstract void sendInventory(HumanEntity player);

    public abstract void sendInventory(HumanEntity player, boolean update);

}
