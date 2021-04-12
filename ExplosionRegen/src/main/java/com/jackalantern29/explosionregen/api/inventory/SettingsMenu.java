package com.jackalantern29.explosionregen.api.inventory;

import com.jackalantern29.explosionregen.ExplosionRegen;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class SettingsMenu {
    private String title;
    private int slots;
    private SlotElement[] elements;
    private Inventory inventory;
    private ClickListen listener;

    private HashMap<String, DynamicUpdate> map = new HashMap<>();
    public SettingsMenu(String title, int slots) {
        this.title = title;
        this.slots = slots;
        elements = new SlotElement[slots];
        this.listener = new ClickListen();
        Bukkit.getPluginManager().registerEvents(listener, ExplosionRegen.getInstance());

        Inventory inventory;
        if(slots <= 5)
            inventory = Bukkit.createInventory(null, InventoryType.HOPPER, title);
        else {
            if(slots >= 6 && slots <= 9)
                inventory = Bukkit.createInventory(null, 9, title);
            else if(slots >= 10 && slots <= 18)
                inventory = Bukkit.createInventory(null, 18, title);
            else if(slots >= 19 && slots <= 27)
                inventory = Bukkit.createInventory(null, 27, title);
            else if(slots >= 28 && slots <= 36)
                inventory = Bukkit.createInventory(null, 36, title);
            else if(slots >= 37 && slots <= 45)
                inventory = Bukkit.createInventory(null, 45, title);
            else
                inventory = Bukkit.createInventory(null, 54, title);
        }
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        if(!map.isEmpty()) {
            clear();
            for(DynamicUpdate update : map.values())
                update.update();
        }
        return inventory;
    }

    public void sendInventory(HumanEntity player) {
        sendInventory(player, false);
    }

    public void sendInventory(HumanEntity player, boolean update) {
        player.openInventory(inventory);
        if(!map.isEmpty() && update) {
            inventory.clear();
            for (DynamicUpdate dyn : map.values())
                dyn.update();
        }
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
        clear();
    }

    public SlotElement getItem(int index) {
        return elements[index];
    }

    public void setItem(int index, SlotElement element) {
        elements[index] = element;
        if(inventory != null)
            inventory.setItem(index, element.getItem());
    }

    public void addItem(SlotElement element) {
        int slot = inventory.firstEmpty();
        if(slot != -1)
            setItem(slot, element);
    }

    public void removeItem(ItemStack item) {
        for (int i = 0; i < inventory.getContents().length; i++) {
            ItemStack it = inventory.getItem(i);
            if(it != null && it == item) {
                elements[i] = null;
                inventory.remove(it);
            }
        }
    }

    public void clear() {
        elements = new SlotElement[slots];
        if(inventory != null)
            inventory.clear();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void update(String id) {
        if(map.containsKey(id)) {
            map.get(id).update();
        }
    }

    public void setUpdate(String id, DynamicUpdate update) {
        map.put(id, update);
    }
    public interface DynamicUpdate {
        public void update();
    }
    private class ClickListen implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if(event.getClickedInventory() != null && event.getClickedInventory().equals(inventory) && getItem(event.getSlot()) != null) {
                SlotElement.SlotFunction function = getItem(event.getSlot()).getFunction();
                event.setCancelled(function.function(new ClickData(event)));
            }
        }
    }
}
