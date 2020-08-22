package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.ExplosionSettings;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ExplosionSettingsUnloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final String settings;
    public ExplosionSettingsUnloadEvent(String settings) {
        this.settings = settings;
    }

    public String getSettingsName() {
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
