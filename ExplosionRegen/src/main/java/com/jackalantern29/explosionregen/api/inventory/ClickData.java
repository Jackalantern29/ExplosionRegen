package com.jackalantern29.explosionregen.api.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClickData {
    private InventoryClickEvent event;

    public ClickData(InventoryClickEvent event) {
        this.event = event;
    }

    public ClickType getClick() {
        return event.getClick();
    }

    public InventoryAction getAction() {
        return event.getAction();
    }

    public ItemStack getItem() {
        return event.getCurrentItem();
    }

    public HumanEntity getWhoClicked() {
        return event.getWhoClicked();
    }

}
