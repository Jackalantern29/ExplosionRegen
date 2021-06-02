package com.jackalantern29.explosionregen.api;

public class ExplosionOverride {
    private ExplosionCondition condition;
    private ExplosionSettings settings;

    public ExplosionOverride(ExplosionCondition condition, ExplosionSettings settings) {
        this.condition = condition;
        this.settings = settings;
    }

    public void setCondition(ExplosionCondition condition) {
        this.condition = condition;
    }

    public ExplosionCondition getCondition() {
        return condition;
    }

    public void setSettings(ExplosionSettings settings) {
        this.settings = settings;
    }

    public ExplosionSettings getSettings() {
        return settings;
    }
}
