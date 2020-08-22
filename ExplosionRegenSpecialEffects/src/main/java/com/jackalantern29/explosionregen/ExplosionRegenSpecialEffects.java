package com.jackalantern29.explosionregen;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.ExplosionParticle;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.ExplosionSettingsPlugin;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ExplosionRegenSpecialEffects extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ExplosionRegenListener(), this);
        for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
            SpecialEffects effects = new SpecialEffects(settings.getName());
            ExplosionSettingsPlugin plugin = settings.loadPlugin(effects, "SpecialEffects");
            if(plugin == null) {
                plugin = new ExplosionSettingsPlugin(effects, "SpecialEffects");
                settings.addPlugin(plugin);
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
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(Explosion explosion : Explosion.getActiveExplosions()) {
                ExplosionSettings settings = explosion.getExplosionSettings();
                SpecialEffects effects = new SpecialEffects(settings.getName());

                List<ParticleData> particles;
                if(effects.getParticleSettings(effects.getParticleType()) == null)
                    particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
                else
                    particles = effects.getParticleSettings(effects.getParticleType()).getParticles(ExplosionPhase.BLOCK_REGENERATING);
                if(!explosion.getBlocks().isEmpty()) {
                    int random = new Random().nextInt(explosion.getBlocks().size());
                    for (ParticleData particle : particles) {
                        Location location = null;
                        switch (particle.getPlayAt()) {
                            case ANYWHERE:
                            case RANDOM:
                                location = explosion.getBlocks().get(random).getLocation();
                                break;
                            case EXPLOSION:
                                location = explosion.getLocation();
                                break;
                            case NEXT_BLOCK:
                                location = explosion.getBlocks().iterator().next().getLocation();
                                break;
                            case PREVIOUS_BLOCK:
                                location = explosion.getPreviousBlock().getLocation();
                                break;
                        }
                        particle.playParticle(location);
                    }
                }
            }
        }, 0, 1);
    }

    @Override
    public void onDisable() {

    }
}
