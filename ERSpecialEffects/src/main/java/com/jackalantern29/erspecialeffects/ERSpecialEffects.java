package com.jackalantern29.erspecialeffects;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.*;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class ERSpecialEffects extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ExplosionRegenListener(), this);

        final File particleVanillaFolder = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "particles", "vanilla");
        final File particlePresetFolder = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "particles", "preset");
        if(!particleVanillaFolder.exists()) {
            particleVanillaFolder.mkdirs();
        }
        if(!particlePresetFolder.exists()) {
            particlePresetFolder.mkdirs();
        }
        for(ExplosionParticle particle : ExplosionParticle.getParticles()) {
            ParticleData.getVanillaSettings(particle, true);
        }
        if(particlePresetFolder.listFiles() != null)
            for(File f : particlePresetFolder.listFiles()) ParticleSettings.load(f);

        for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
            SpecialEffects effects = new SpecialEffects(settings.getName());
            ExplosionSettingsPlugin plugin = settings.loadPlugin(effects, "SpecialEffects");
            if(plugin == null) {
                plugin = new ExplosionSettingsPlugin(effects, "SpecialEffects");
                settings.addPlugin(plugin);
                plugin.setOption("particles.type", effects.getParticleType().name().toLowerCase());
                for(ExplosionPhase phase : ExplosionPhase.values()) {
                    plugin.setOption("particles.vanilla." + phase.toString() + ".particle", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString().toLowerCase());
                    plugin.setOption("particles.vanilla." + phase.toString() + ".enable", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
                    plugin.setOption("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name().toLowerCase());
                    plugin.setOption("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                    plugin.setOption("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                    plugin.setOption("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
                }
            } else {
                effects.setParticleType(ParticleType.valueOf(plugin.getString("particles.type", effects.getParticleType().name()).toUpperCase()));
                for(ExplosionPhase phase : ExplosionPhase.values()) {
                    ExplosionParticle particle = ExplosionParticle.getParticle(plugin.getString("particles.vanilla." + phase.toString() + ".particle", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString()).toUpperCase());
                    boolean canDisplay = plugin.getBoolean("particles.vanilla." + phase.toString() + ".enable", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
                    effects.getParticleSettings(ParticleType.VANILLA).setParticle(0, new ParticleData(ParticleData.getVanillaSettings(particle), phase, canDisplay));
                    Sound sound = Sound.valueOf(plugin.getString("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name()).toUpperCase());
                    float volume = (float)plugin.getDouble("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                    float pitch = (float)plugin.getDouble("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                    boolean enable = plugin.getBoolean("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
                    effects.getSoundSettings().setSound(phase, new SoundData(sound, volume, pitch, enable));
                }
                effects.setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(plugin.getString("particles.preset", Objects.nonNull(effects.getParticleSettings(ParticleType.PRESET)) ? effects.getParticleSettings(ParticleType.PRESET).getName() : null)));
            }
            if(ExplosionRegen.getSettings().getAllowProfileSettings()) {
                for(ProfileSettings profile : ProfileSettings.getProfiles()) {
                    addPluginToProfile(profile, settings);
                }
            }
        }
    }

    protected static void addPluginToProfile(ProfileSettings profile, ExplosionSettings settings) {
        SpecialEffects effects = new SpecialEffects(profile.getPlayer().getName() + ":" + settings.getName());
        ProfileSettingsPlugin plugin = profile.loadPlugin(settings, effects, "SpecialEffects");
        if(plugin == null) {
            plugin = new ProfileSettingsPlugin(settings, effects, "SpecialEffects");
            profile.addPlugin(plugin);
        } else {
            effects.setParticleType(ParticleType.valueOf(plugin.getString("particles.type", effects.getParticleType().name()).toUpperCase()));
            for(ExplosionPhase phase : ExplosionPhase.values()) {
                ExplosionParticle particle = ExplosionParticle.getParticle(plugin.getString("particles.vanilla." + phase.toString() + ".particle", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString()).toUpperCase());
                boolean canDisplay = plugin.getBoolean("particles.vanilla." + phase.toString() + ".enable", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
                effects.getParticleSettings(ParticleType.VANILLA).setParticle(0, new ParticleData(ParticleData.getVanillaSettings(particle), phase, canDisplay));
                Sound sound = Sound.valueOf(plugin.getString("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name()).toUpperCase());
                float volume = (float)plugin.getDouble("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                float pitch = (float)plugin.getDouble("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                boolean enable = plugin.getBoolean("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
                effects.getSoundSettings().setSound(phase, new SoundData(sound, volume, pitch, enable));
            }
            effects.setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(plugin.getString("particles.preset", Objects.nonNull(effects.getParticleSettings(ParticleType.PRESET)) ? effects.getParticleSettings(ParticleType.PRESET).getName() : null)));
        }
        plugin.setOption("particles.type", effects.getParticleType().name().toLowerCase());
        for(ExplosionPhase phase : ExplosionPhase.values()) {
            plugin.setOption("particles.vanilla." + phase.toString() + ".particle", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString().toLowerCase());
            plugin.setOption("particles.vanilla." + phase.toString() + ".enable", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
            plugin.setOption("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name().toLowerCase());
            plugin.setOption("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
            plugin.setOption("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
            plugin.setOption("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
        }
    }
    @Override
    public void onDisable() {
        for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings())
            updateOptions(settings);
        for(ProfileSettings profile : ProfileSettings.getProfiles())
            updateProfileOptions(profile);
    }

    public static void updateOptions(ExplosionSettings settings) {
        ExplosionSettingsPlugin plugin = settings.getPlugin("SpecialEffects");
        SpecialEffects effects = (SpecialEffects) plugin.toObject();
        plugin.setOption("particles.type", effects.getParticleType().name().toLowerCase());
        for(ExplosionPhase phase : ExplosionPhase.values()) {
            plugin.setOption("particles.vanilla." + phase.toString() + ".particle", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString().toLowerCase());
            plugin.setOption("particles.vanilla." + phase.toString() + ".enable", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
            plugin.setOption("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name().toLowerCase());
            plugin.setOption("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
            plugin.setOption("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
            plugin.setOption("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
        }
    }

    public static void updateProfileOptions(ProfileSettings profile) {
        for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
            ProfileSettingsPlugin plugin = profile.getPlugin(settings, "SpecialEffects");
            SpecialEffects effects = (SpecialEffects)plugin.toObject();
            plugin.setOption("particles.type", effects.getParticleType().name().toLowerCase());
            for(ExplosionPhase phase : ExplosionPhase.values()) {
                plugin.setOption("particles.vanilla." + phase.toString() + ".particle", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString().toLowerCase());
                plugin.setOption("particles.vanilla." + phase.toString() + ".enable", effects.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
                plugin.setOption("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name().toLowerCase());
                plugin.setOption("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                plugin.setOption("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                plugin.setOption("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
            }
        }
    }
}
