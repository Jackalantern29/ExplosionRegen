package com.jackalantern29.explosionregen;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.ExplosionParticle;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.ExplosionSettingsPlugin;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.events.ExplosionBlockRegenEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionRegenFinishEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionSettingsLoadEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionTriggerEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    @EventHandler
    public void onTrigger(ExplosionTriggerEvent event) {
        Explosion explosion = event.getExplosion();
        if (!explosion.getBlocks().isEmpty()) {
            ExplosionPhase phase = event.getPhase();
            int random = new Random().nextInt(explosion.getBlocks().size());
//				if(ExplosionRegen.getSettings().getAllowProfileSettings())
//					Bukkit.getOnlinePlayers().forEach(player -> {
//						if(ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
//							ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
//							List<ParticleData> particles;
//							if(pSettings.getParticleSettings(pSettings.getParticleType()) == null)
//								particles = ParticleSettings.getSettings(pSettings.getExplosionSettings().getName() + "_vanilla").getParticles();
//							else
//								particles = pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.ON_EXPLODE);
//							for(ParticleData particle : particles) {
//								Location loc = null;
//								switch(particle.getPlayAt()) {
//									case RANDOM:
//										loc = explosion.getBlocks().get(random).getLocation();
//										break;
//									case EXPLOSION:
//										loc = explosion.getLocation();
//										break;
//									case ANYWHERE:
//									case NEXT_BLOCK:
//										loc = explosion.getBlocks().iterator().next().getLocation();
//										break;
//									case PREVIOUS_BLOCK:
//										loc = explosion.getPreviousBlock().getLocation();
//										break;
//								}
//								particle.playParticle(loc);
//							}
//						} else {
//							List<ParticleData> particles;
//							if(settings.getParticleSettings(settings.getParticleType()) == null)
//								particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
//							else
//								particles = settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.ON_EXPLODE);
//							for(ParticleData particle : particles) {
//								Location loc = null;
//								switch(particle.getPlayAt()) {
//									case RANDOM:
//										loc = explosion.getBlocks().get(random).getLocation();
//										break;
//									case EXPLOSION:
//										loc = explosion.getLocation();
//										break;
//									case ANYWHERE:
//									case NEXT_BLOCK:
//										loc = explosion.getBlocks().iterator().next().getLocation();
//										break;
//									case PREVIOUS_BLOCK:
//										loc = explosion.getPreviousBlock().getLocation();
//										break;
//								}
//								particle.playParticle(loc);
//							}
//						}
//					});
//				else {
					SpecialEffects effects = new SpecialEffects(explosion.getExplosionSettings().getName());
					List<ParticleData> particles;
					if(effects.getParticleSettings(effects.getParticleType()) == null)
						particles = ParticleSettings.getSettings(effects.getName() + "_vanilla").getParticles();
					else
						particles = effects.getParticleSettings(effects.getParticleType()).getParticles(ExplosionPhase.ON_EXPLODE);
					for(ParticleData particle : particles) {
						Location loc = null;
						switch(particle.getPlayAt()) {
							case RANDOM:
								loc = explosion.getBlocks().get(random).getLocation();
								break;
							case EXPLOSION:
								loc = explosion.getLocation();
								break;
							case ANYWHERE:
							case NEXT_BLOCK:
								loc = explosion.getBlocks().iterator().next().getLocation();
								break;
							case PREVIOUS_BLOCK:
								loc = explosion.getPreviousBlock().getLocation();
								break;
						}
						particle.playParticle(loc);
					}
			if (effects.getAllowSound(ExplosionPhase.ON_EXPLODE)) {
				effects.getSoundSettings().getSound(ExplosionPhase.ON_EXPLODE).playSound(explosion.getBlocks().get(random).getLocation());
			}
//				}
//				if(explosion.getExplosionSettings().getAllowSound(phase)) {
//					if(ExplosionRegen.getSettings().getAllowProfileSettings())
//						Bukkit.getOnlinePlayers().forEach(player -> {
//							if(ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
//								ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(phase).playSound(location, player);
//							else
//								settings.getSoundSettings().getSound(phase).playSound(explosion.getLocation(), player);
//						});
//					else
//						settings.getSoundSettings().getSound(phase).playSound(explosion.getLocation());
//				}
        }
    }

    @EventHandler
	public void onBlockRegen(ExplosionBlockRegenEvent event) {
		List<ParticleData> particles;
		Explosion explosion = event.getExplosion();
		ExplosionSettings settings = explosion.getExplosionSettings();
		SpecialEffects effects = new SpecialEffects(settings.getName());
		if(explosion.getBlocks().isEmpty())
			return;
		int random = new Random().nextInt(explosion.getBlocks().size());
		if(effects.getParticleSettings(effects.getParticleType()) == null)
			particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
		else
			particles = effects.getParticleSettings(effects.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN);
		for (ParticleData particle : particles) {
			Location location = null;
			switch (particle.getPlayAt()) {
				case RANDOM:
					location = explosion.getBlocks().get(random).getLocation();
					break;
				case EXPLOSION:
					location = explosion.getLocation();
					break;
				case ANYWHERE:
				case NEXT_BLOCK:
					if (explosion.getBlocks().iterator().hasNext())
						location = explosion.getBlocks().iterator().next().getLocation();
					break;
				case PREVIOUS_BLOCK:
					location = explosion.getPreviousBlock().getLocation();
					break;
			}
			particle.playParticle(location);
		}
		if (effects.getAllowSound(ExplosionPhase.ON_BLOCK_REGEN)) {
			effects.getSoundSettings().getSound(ExplosionPhase.ON_BLOCK_REGEN).playSound(explosion.getBlocks().get(random).getLocation());
		}
//										if (ExplosionRegen.getSettings().getAllowProfileSettings())
//											Bukkit.getOnlinePlayers().forEach(player -> {
//												if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
//													ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
//													List<ParticleData> particles;
//													if(pSettings.getParticleSettings(pSettings.getParticleType()) == null)
//														particles = ParticleSettings.getSettings(pSettings.getExplosionSettings().getName() + "_vanilla").getParticles();
//													else
//														particles = pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN);
//													for (ParticleData particle : particles) {
//														Location location = null;
//														switch (particle.getPlayAt()) {
//															case RANDOM:
//																location = explosion.getBlocks().get(random).getLocation();
//																break;
//															case EXPLOSION:
//																location = explosion.getLocation();
//																break;
//															case ANYWHERE:
//															case NEXT_BLOCK:
//																location = explosion.getBlocks().iterator().next().getLocation();
//																break;
//															case PREVIOUS_BLOCK:
//																location = explosion.getPreviousBlock().getLocation();
//																break;
//														}
//														particle.playParticle(location);
//													}
//												} else {
//													List<ParticleData> particles;
//													if(settings.getParticleSettings(settings.getParticleType()) == null)
//														particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
//													else
//														particles = settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN);
//													for (ParticleData particle : particles) {
//														Location location = null;
//														switch (particle.getPlayAt()) {
//															case RANDOM:
//																location = explosion.getBlocks().get(random).getLocation();
//																break;
//															case EXPLOSION:
//																location = explosion.getLocation();
//																break;
//															case ANYWHERE:
//															case NEXT_BLOCK:
//																location = explosion.getBlocks().iterator().next().getLocation();
//																break;
//															case PREVIOUS_BLOCK:
//																location = explosion.getPreviousBlock().getLocation();
//																break;
//														}
//														particle.playParticle(location);
//													}
//												}
//											});
//										else {
//											List<ParticleData> particles;
//											if(settings.getParticleSettings(settings.getParticleType()) == null)
//												particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
//											else
//												particles = settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN);
//											for (ParticleData particle : particles) {
//												Location location = null;
//												switch (particle.getPlayAt()) {
//													case RANDOM:
//														location = explosion.getBlocks().get(random).getLocation();
//														break;
//													case EXPLOSION:
//														location = explosion.getLocation();
//														break;
//													case ANYWHERE:
//													case NEXT_BLOCK:
//														if (explosion.getBlocks().iterator().hasNext())
//															location = explosion.getBlocks().iterator().next().getLocation();
//														break;
//													case PREVIOUS_BLOCK:
//														location = explosion.getPreviousBlock().getLocation();
//														break;
//												}
//												particle.playParticle(location);
//											}
//										}
//										if (explosion.getExplosionSettings().getAllowSound(phase)) {
//											if (ExplosionRegen.getSettings().getAllowProfileSettings())
//												Bukkit.getOnlinePlayers().forEach(player -> {
//													if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
//														ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.ON_BLOCK_REGEN).playSound(block.getLocation(), player);
//													else
//														settings.getSoundSettings().getSound(ExplosionPhase.ON_BLOCK_REGEN).playSound(block.getLocation(), player);
//												});
//											else
//												settings.getSoundSettings().getSound(phase).playSound(block.getLocation());
//										}
	}

	@EventHandler
	public void onRegenFinish(ExplosionRegenFinishEvent event) {
		List<ParticleData> particles;
		Explosion explosion = event.getExplosion();
		ExplosionSettings settings = explosion.getExplosionSettings();
		SpecialEffects effects = new SpecialEffects(settings.getName());
		if(explosion.getBlocks().isEmpty())
			return;
		int random = new Random().nextInt(explosion.getBlocks().size());
		if(effects.getParticleSettings(effects.getParticleType()) == null)
			particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
		else
			particles = effects.getParticleSettings(effects.getParticleType()).getParticles(ExplosionPhase.EXPLOSION_FINISHED_REGEN);
		for (ParticleData particle : particles) {
			Location location = null;
			switch (particle.getPlayAt()) {
				case RANDOM:
					location = explosion.getBlocks().get(random).getLocation();
					break;
				case EXPLOSION:
					location = explosion.getLocation();
					break;
				case ANYWHERE:
				case NEXT_BLOCK:
					if (explosion.getBlocks().iterator().hasNext())
						location = explosion.getBlocks().iterator().next().getLocation();
					break;
				case PREVIOUS_BLOCK:
					location = explosion.getPreviousBlock().getLocation();
					break;
			}
			particle.playParticle(location);
		}
		if (effects.getAllowSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN)) {
			effects.getSoundSettings().getSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN).playSound(explosion.getBlocks().get(random).getLocation());
		}
	}
}
