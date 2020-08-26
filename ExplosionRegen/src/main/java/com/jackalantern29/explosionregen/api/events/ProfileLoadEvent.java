package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.ProfileSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ProfileLoadEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final ProfileSettings profile;
    public ProfileLoadEvent(ProfileSettings profile) {
        this.profile = profile;
    }

    public Player getPlayer() {
        return profile.getPlayer();
    }

    public ProfileSettings getProfile() {
        return profile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
