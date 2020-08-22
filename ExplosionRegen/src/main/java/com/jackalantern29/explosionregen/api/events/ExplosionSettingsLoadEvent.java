package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.ExplosionSettings;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ExplosionSettingsLoadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final ExplosionSettings settings;
    public ExplosionSettingsLoadEvent(ExplosionSettings settings) {
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
