package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.ExplosionSettings;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ExplosionSettingsUnloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final ExplosionSettings settings;
    public ExplosionSettingsUnloadEvent(ExplosionSettings settings) {
        this.settings = settings;
    }

    public ExplosionSettings getSettings() {
        return settings;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
