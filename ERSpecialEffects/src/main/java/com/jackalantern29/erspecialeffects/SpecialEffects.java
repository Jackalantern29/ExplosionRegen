package com.jackalantern29.erspecialeffects;

import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Sound;

import java.util.HashMap;


public class SpecialEffects {
    private String name;

	private HashMap<ExplosionPhase, ParticleSettings> particleSettings = new HashMap<>();
	private final SoundSettings soundSettings = new SoundSettings();

    public SpecialEffects(String name) {
        this.name = name;
        for(ExplosionPhase phase : ExplosionPhase.values()) {
            if(phase == ExplosionPhase.BLOCK_REGENERATING) {
                particleSettings.put(phase, ParticleSettings.getSettings("flame"));
            } else if(phase == ExplosionPhase.ON_BLOCK_REGEN) {
                particleSettings.put(phase, ParticleSettings.getSettings("heart"));
            } else {
                particleSettings.put(phase, ParticleSettings.getSettings("slime"));
            }

        }
		this.soundSettings.setSound(ExplosionPhase.ON_EXPLODE, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
		this.soundSettings.setSound(ExplosionPhase.BLOCK_REGENERATING, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
		this.soundSettings.setSound(ExplosionPhase.ON_BLOCK_REGEN, new SoundData((SoundData.getSound("STEP_GRASS") != null ? SoundData.getSound("STEP_GRASS") : SoundData.getSound("BLOCK_GRASS_STEP") != null ? SoundData.getSound("BLOCK_GRASS_STEP") : Sound.values()[0]), 1f, 1f, true));
		this.soundSettings.setSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
    }

    public String getName() {
        return name;
    }

	public ParticleSettings getParticleSettings(ExplosionPhase phase) {
        return particleSettings.get(phase);
    }

    public void setParticleSettings(ExplosionPhase phase, ParticleSettings particleSettings) {
        this.particleSettings.put(phase, particleSettings);
    }

    public boolean getAllowSound(ExplosionPhase phase) {
        return soundSettings.getSound(phase).isEnable();
    }

    public void setAllowSound(ExplosionPhase phase, boolean enable) {
        soundSettings.getSound(phase).setEnable(enable);
    }

    public SoundSettings getSoundSettings() {
        return soundSettings;
    }
}
