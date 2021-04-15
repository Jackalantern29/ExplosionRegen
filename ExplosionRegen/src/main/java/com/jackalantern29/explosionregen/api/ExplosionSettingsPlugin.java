package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.api.inventory.Menu;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExplosionSettingsPlugin {
    private final String name;
    private final Object plugin;
    private final Map<String, Object> map = new HashMap<>();
    private Menu mainMenu;

    public ExplosionSettingsPlugin(Object plugin) {
        this(plugin, plugin.getClass().getSimpleName());
    }

    public ExplosionSettingsPlugin(Object plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void setOption(String option, Object object) {
        map.put(option, object);
    }

    public Object get(String option) {
        return map.get(option);
    }

    public Object get(String option, Object defaultOption) {
        return map.getOrDefault(option, defaultOption);
    }

    public String getString(String option) {
        return (String) map.get(option);
    }

    public String getString(String option, Object defaultOption) {
        return (String) map.getOrDefault(option, defaultOption);
    }

    public int getInt(String option) {
        return (int) map.get(option);
    }

    public int getInt(String option, Object defaultOption) {
        return (int) map.getOrDefault(option, defaultOption);
    }

    public boolean getBoolean(String option) {
        return (boolean) map.get(option);
    }

    public boolean getBoolean(String option, Object defaultOption) {
        return (boolean) map.getOrDefault(option, defaultOption);
    }

    public double getDouble(String option) {
        return (double) map.get(option);
    }

    public double getDouble(String option, Object defaultOption) {
        return (double) map.getOrDefault(option, defaultOption);
    }

    public Set<String> getKeys() {
        return map.keySet();
    }

    public Collection<Object> getValues() {
        return map.values();
    }

    public Set<Map.Entry<String, Object>> getEntries() {
        return map.entrySet();
    }

    public Object toObject() {
        return plugin;
    }

    public String getName() {
        return name;
    }

    public Menu getMainMenu() {
        return mainMenu;
    }

    public void setMainMenu(Menu mainMenu) {
        this.mainMenu = mainMenu;
    }
}
