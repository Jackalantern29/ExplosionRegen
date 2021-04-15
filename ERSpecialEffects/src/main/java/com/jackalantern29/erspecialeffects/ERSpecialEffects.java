package com.jackalantern29.erspecialeffects;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.*;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

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
            ParticleData data = new ParticleData(particle);
            File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "particles" + File.separator + "vanilla", particle.toString().toLowerCase() + ".yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            if(!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            LinkedHashMap<String, Object> map = new LinkedHashMap<>();
            map.put("amount", 5);
            map.put("offset.x", 0.1f);
            map.put("offset.y", 0.1f);
            map.put("offset.z", 0.1f);
            map.put("speed", 0f);
            boolean doSave = false;
            for(String key : new ArrayList<>(map.keySet())) {
                if(!config.contains(key)) { config.set(key, map.get(key));doSave = true;}
                map.remove(key);
            }
            if(doSave) {
                try {
                    config.save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            data.setAmount(config.getInt("amount", 5));
            data.setOffsetX((float)config.getDouble("offset.x", 0.1f));
            data.setOffsetY((float)config.getDouble("offset.y", 0.1f));
            data.setOffsetZ((float)config.getDouble("offset.z", 0.1f));
            data.setSpeed((float)config.getDouble("speed", 0.0f));
            ParticleSettings particleSettings = new ParticleSettings(particle.toString().toLowerCase(), "Vanilla");
            for (ExplosionPhase phase : ExplosionPhase.values())
                particleSettings.addParticles(phase, data);
        }
        if(particlePresetFolder.listFiles() != null)
            for(File f : particlePresetFolder.listFiles()) ParticleSettings.load(f);

        for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
            SpecialEffects effects = new SpecialEffects(settings.getName());
            ExplosionSettingsPlugin plugin = settings.loadPlugin(effects, "SpecialEffects");
            if(plugin == null) {
                plugin = new ExplosionSettingsPlugin(effects, "SpecialEffects");
                settings.addPlugin(plugin);
                for(ExplosionPhase phase : ExplosionPhase.values()) {
                    plugin.setOption("particles." + phase.toString() + ".particle", effects.getParticleSettings(phase).getName());
                    plugin.setOption("particles." + phase.toString() + ".enable", effects.getParticleSettings(phase).canDisplay(phase));
                    plugin.setOption("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name().toLowerCase());
                    plugin.setOption("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                    plugin.setOption("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                    plugin.setOption("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
                }
            } else {
                for(ExplosionPhase phase : ExplosionPhase.values()) {
                    ParticleSettings particle = ParticleSettings.getSettings(plugin.getString("particles." + phase.toString() + ".particle", effects.getParticleSettings(phase).getName()));
                    boolean canDisplay = plugin.getBoolean("particles." + phase.toString() + ".enable", effects.getParticleSettings(phase).canDisplay(phase));
                    effects.setParticleSettings(phase, particle);
                    Sound sound = Sound.valueOf(plugin.getString("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name()).toUpperCase());
                    float volume = (float)plugin.getDouble("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                    float pitch = (float)plugin.getDouble("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                    boolean enable = plugin.getBoolean("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
                    effects.getSoundSettings().setSound(phase, new SoundData(sound, volume, pitch, enable));
                }
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
            for(ExplosionPhase phase : ExplosionPhase.values()) {
                ParticleSettings particle = ParticleSettings.getSettings(plugin.getString("particles." + phase.toString() + ".particle", effects.getParticleSettings(phase).getName()));
                boolean canDisplay = plugin.getBoolean("particles." + phase.toString() + ".enable", effects.getParticleSettings(phase).canDisplay(phase));
                effects.setParticleSettings(phase, particle);
                Sound sound = Sound.valueOf(plugin.getString("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name()).toUpperCase());
                float volume = (float)plugin.getDouble("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                float pitch = (float)plugin.getDouble("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                boolean enable = plugin.getBoolean("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
                effects.getSoundSettings().setSound(phase, new SoundData(sound, volume, pitch, enable));
            }
        }
        for(ExplosionPhase phase : ExplosionPhase.values()) {
            plugin.setOption("particles." + phase.toString() + ".particle", effects.getParticleSettings(phase).getName());
            plugin.setOption("particles." + phase.toString() + ".enable", effects.getParticleSettings(phase).canDisplay(phase));
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
        for(ExplosionPhase phase : ExplosionPhase.values()) {
            plugin.setOption("particles." + phase.toString() + ".particle", effects.getParticleSettings(phase).getName());
            plugin.setOption("particles." + phase.toString() + ".enable", effects.getParticleSettings(phase).canDisplay(phase));
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
            for(ExplosionPhase phase : ExplosionPhase.values()) {
                plugin.setOption("particles." + phase.toString() + ".particle", effects.getParticleSettings(phase).getName());
                plugin.setOption("particles." + phase.toString() + ".enable", effects.getParticleSettings(phase).canDisplay(phase));
                plugin.setOption("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name().toLowerCase());
                plugin.setOption("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
                plugin.setOption("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
                plugin.setOption("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
            }
        }
    }
}
