package com.jackalantern29.explosionregen;

import com.jackalantern29.explosionregen.api.ExplosionParticle;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.Map;

public class SpecialEffects {
    private String name;

    private ParticleType particleType;
	private final Map<ParticleType, ParticleSettings> particleSettings = new HashMap<>();
	private final SoundSettings soundSettings = new SoundSettings();

    public SpecialEffects(String name) {
        this.name = name;

        this.particleType = ParticleType.VANILLA;
		this.particleSettings.put(ParticleType.VANILLA, new ParticleSettings(name + "_vanilla",
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("slime")), ExplosionPhase.ON_EXPLODE, false),
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("slime")), ExplosionPhase.EXPLOSION_FINISHED_REGEN, false),
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("heart")), ExplosionPhase.ON_BLOCK_REGEN, true),
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("flame")), ExplosionPhase.BLOCK_REGENERATING, true)));
		this.particleSettings.put(ParticleType.PRESET, null);
		this.soundSettings.setSound(ExplosionPhase.ON_EXPLODE, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
		this.soundSettings.setSound(ExplosionPhase.BLOCK_REGENERATING, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
		this.soundSettings.setSound(ExplosionPhase.ON_BLOCK_REGEN, new SoundData((SoundData.getSound("STEP_GRASS") != null ? SoundData.getSound("STEP_GRASS") : SoundData.getSound("BLOCK_GRASS_STEP") != null ? SoundData.getSound("BLOCK_GRASS_STEP") : Sound.values()[0]), 1f, 1f, true));
		this.soundSettings.setSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
    }

    public String getName() {
        return name;
    }
    public ParticleType getParticleType() {
		return particleType;
	}

	public void setParticleType(ParticleType type) {
		particleType = type;
	}

    public ParticleSettings getParticleSettings(ParticleType type) {
        return particleSettings.get(type);
    }

    public void setParticleSettings(ParticleType type, ParticleSettings particleSettings) {
        this.particleSettings.put(type, particleSettings);
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
