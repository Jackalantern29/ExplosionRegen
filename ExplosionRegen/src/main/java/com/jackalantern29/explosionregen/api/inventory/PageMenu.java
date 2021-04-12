package com.jackalantern29.explosionregen.api.inventory;

import com.jackalantern29.explosionregen.ExplosionRegen;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.HashMap;

public class PageMenu {
    private String title;
    private int slots;
    private HashMap<Integer, SettingsMenu> pages = new HashMap<>();
    private HashMap<String, DynamicUpdate> map = new HashMap<>();

    private ItemStack nextPageItem = null;
    private ItemStack prevPageItem = null;

    public PageMenu(String title, int slots) {
        this.title = title;
        this.slots = slots;
        Bukkit.getPluginManager().registerEvents(new ClickListen(), ExplosionRegen.getInstance());
        pages.put(0, new SettingsMenu(this.title, this.slots));
    }

    public SettingsMenu getPage(int page) {
        return pages.get(page);
    }

    public void addPage() {
        pages.put(pages.size(), new SettingsMenu(getTitle(), getSlots()));
    }

    public boolean hasPage(SettingsMenu menu) {
        return getPages().contains(menu);
    }

    public Collection<SettingsMenu> getPages() {
        return pages.values();
    }

    public void setNextPageItem(ItemStack item) {
        this.nextPageItem = item;
    }

    public ItemStack getNextPageItem() {
        return nextPageItem;
    }

    public void setPrevPageItem(ItemStack item) {
        this.prevPageItem = item;
    }

    public ItemStack getPrevPageItem() {
        return prevPageItem;
    }

    public String getTitle() {
        return title;
    }

    public int getSlots() {
        return slots;
    }

    public void clear() {
        for(SettingsMenu menu : getPages()) {
            menu.clear();
        }
    }

    public void addItem(SlotElement element) {
        int count = 0;
        for(SettingsMenu menu : getPages()) {
            int slot = menu.getInventory().firstEmpty();
            if(slot != -1) {
                menu.addItem(element);
                return;
            }
            count++;
        }
        addPage();
        update("#layout");
    }

    public void sendInventory(HumanEntity player) {
        sendInventory(player, false);
    }

    public void sendInventory(HumanEntity player, boolean update) {
        getPage(0).sendInventory(player);
        if(!map.isEmpty() && update)
            for(SettingsMenu page : getPages())
                page.clear();
            for(DynamicUpdate dyn : map.values())
                dyn.update();
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

    public class ClickListen implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent event) {
            for (int i = 0; i < getPages().size(); i++) {
                SettingsMenu menu = getPage(i);
                Inventory inventory = menu.getInventory();
                if(event.getClickedInventory() != null && event.getClickedInventory().equals(inventory)) {
                    if(event.getCurrentItem().equals(getNextPageItem())) {
                        if (i != getPages().size() - 1)
                            getPage(i+1).sendInventory(event.getWhoClicked());
                        else
                            getPage(0).sendInventory(event.getWhoClicked());
                        event.setCancelled(true);
                        return;
                    } else if(event.getCurrentItem().equals(getPrevPageItem())) {
                        if(i != 0)
                            getPage(i-1).sendInventory(event.getWhoClicked());
                        else
                            getPage(getPages().size()-1).sendInventory(event.getWhoClicked());
                        event.setCancelled(true);
                        return;
                    }
                }

            }
        }
    }
}
