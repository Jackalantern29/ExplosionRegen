package com.jackalantern29.explosionregen.api.inventory;

import org.bukkit.inventory.ItemStack;

public class SlotElement {
    private ItemStack item;
    private SlotFunction function;

    public SlotElement(ItemStack item) {
        this.item = item;
    }

    public SlotElement(ItemStack item, SlotFunction function) {
        this(item);
        this.function = function;
    }
    public void setItem(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }

    public SlotFunction getFunction() {
        return function;
    }

    public void setFunction(SlotFunction function) {
        this.function = function;
    }

    public interface SlotFunction {
        public boolean function(ClickData data);
    }
}
