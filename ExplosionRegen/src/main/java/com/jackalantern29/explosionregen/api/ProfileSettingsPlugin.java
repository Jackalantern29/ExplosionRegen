package com.jackalantern29.explosionregen.api;

public class ProfileSettingsPlugin extends ExplosionSettingsPlugin {
    private final ExplosionSettings settings;
    public ProfileSettingsPlugin(ExplosionSettings settings, Object plugin) {
        super(plugin);
        this.settings = settings;
    }

    public ProfileSettingsPlugin(ExplosionSettings settings, Object plugin, String name) {
        super(plugin, name);
        this.settings = settings;
    }

    public ExplosionSettings getExplosionSettings() {
        return settings;
    }
}
