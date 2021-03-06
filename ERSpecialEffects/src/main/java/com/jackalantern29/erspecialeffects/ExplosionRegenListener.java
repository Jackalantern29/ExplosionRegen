package com.jackalantern29.erspecialeffects;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.*;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.events.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Random;

public class ExplosionRegenListener implements Listener {

	@EventHandler
	public void onLoad(ExplosionSettingsLoadEvent event) {
		ExplosionSettings settings = event.getSettings();
		SpecialEffects effects = new SpecialEffects(settings.getName());
		ExplosionSettingsPlugin plugin = settings.loadPlugin(effects, "SpecialEffects");
		if(plugin == null) {
			plugin = new ExplosionSettingsPlugin(effects, "SpecialEffects");
			settings.addPlugin(plugin);
		} else {
			for(ExplosionPhase phase : ExplosionPhase.values()) {
				ParticleSettings particle = ParticleSettings.getSettings(plugin.getString("particles." + phase.toString() + ".particle", effects.getParticleSettings(phase).getName()).toLowerCase());
				boolean canDisplay = plugin.getBoolean("particles." + phase.toString() + ".enable", effects.getParticleSettings(phase).canDisplay(phase));
				effects.setParticleSettings(phase, particle);
				Sound sound = Sound.valueOf(plugin.getString("sounds." + phase.toString() + ".sound", effects.getSoundSettings().getSound(phase).getSound().name()).toUpperCase());
				float volume = (float)plugin.getDouble("sounds." + phase.toString() + ".volume", effects.getSoundSettings().getSound(phase).getVolume());
				float pitch = (float)plugin.getDouble("sounds." + phase.toString() + ".pitch", effects.getSoundSettings().getSound(phase).getPitch());
				boolean enable = plugin.getBoolean("sounds." + phase.toString() + ".enable", effects.getSoundSettings().getSound(phase).isEnable());
				effects.getSoundSettings().setSound(phase, new SoundData(sound, volume, pitch, enable));
			}
		}
		if(ExplosionRegen.getSettings().getAllowProfileSettings())
			for(ProfileSettings profile : ProfileSettings.getProfiles()) {
				ERSpecialEffects.addPluginToProfile(profile, settings);
			}
		InventoryMenu.MAP.get(null).menu.update("menu");
	}

	@EventHandler
	public void onLoad(ProfileLoadEvent event) {
		ProfileSettings profile = event.getProfile();
		for(ExplosionSettings settings : profile.getConfigurableSettings()) {
			ERSpecialEffects.addPluginToProfile(profile, settings);
		}
	}

	@EventHandler
	public void onUnload(ProfileUnloadEvent event) {
		ERSpecialEffects.updateProfileOptions(event.getProfile());
	}

	@EventHandler
	public void onUnload(ExplosionSettingsUnloadEvent event) {
		ERSpecialEffects.updateOptions(event.getSettings());
	}
    @EventHandler
    public void onRegen(ExplosionRegenEvent event) {
        Explosion explosion = event.getExplosion();
		ExplosionPhase phase = event.getPhase();
        if(!explosion.getBlocks().isEmpty()) {
			ExplosionSettings settings = explosion.getSettings();
			for(Player player : Bukkit.getOnlinePlayers()) {
				SpecialEffects effects;
				if(ExplosionRegen.getSettings().getAllowProfileSettings()) {
					if(ProfileSettings.get(player).getPlugin(settings, "SpecialEffects") != null)
						effects = (SpecialEffects) ProfileSettings.get(player).getPlugin(settings, "SpecialEffects").toObject();
					else
						effects = (SpecialEffects) settings.getPlugin("SpecialEffects").toObject();
				} else
					effects = (SpecialEffects) settings.getPlugin("SpecialEffects").toObject();

				List<ParticleData> particles;
				if(effects.getParticleSettings(phase) == null)
					particles = ParticleSettings.getSettings(settings.getName()).getParticles();
				else
					particles = effects.getParticleSettings(phase).getParticles(phase);
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
						particle.playParticle(location, player);
					}
					if (effects.getAllowSound(phase)) {
						effects.getSoundSettings().getSound(phase).playSound(explosion.getBlocks().get(random).getLocation(), player);
					}
				}
			}
		}
    }
}
