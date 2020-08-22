package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.BukkitMethods;
import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.ProfileSettings.ERProfileExplosionSettings;
import com.jackalantern29.explosionregen.api.blockdata.PistonData;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.events.ExplosionRegenFinishEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionTriggerEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Bisected.Half;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.material.Chest;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.List;

public class ExplosionMap implements Listener {

	public ExplosionMap() {
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				for (Explosion explosion : new ArrayList<>(Explosion.getActiveExplosions())) {
					ExplosionSettings settings = explosion.getExplosionSettings();
					ExplosionPhase phase;
					if (!explosion.getBlocks().isEmpty()) {
						int random = new Random().nextInt(explosion.getBlocks().size());
						phase = ExplosionPhase.BLOCK_REGENERATING;
//						if (ExplosionRegen.getSettings().getAllowProfileSettings())
//							Bukkit.getOnlinePlayers().forEach(player -> {
//								if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
//									ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
//									List<ParticleData> particles;
//									if(pSettings.getParticleSettings(pSettings.getParticleType()) == null)
//										particles = ParticleSettings.getSettings(pSettings.getExplosionSettings().getName() + "_vanilla").getParticles();
//									else
//										particles = pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.BLOCK_REGENERATING);
//									for (ParticleData particle : particles) {
//										Location location = null;
//										switch (particle.getPlayAt()) {
//											case ANYWHERE:
//											case RANDOM:
//												location = explosion.getBlocks().get(random).getLocation();
//												break;
//											case EXPLOSION:
//												location = explosion.getLocation();
//												break;
//											case NEXT_BLOCK:
//												location = explosion.getBlocks().iterator().next().getLocation();
//												break;
//											case PREVIOUS_BLOCK:
//												location = explosion.getPreviousBlock().getLocation();
//												break;
//										}
//										particle.playParticle(location, player);
//									}
//								} else {
//									List<ParticleData> particles;
//									if(settings.getParticleSettings(settings.getParticleType()) == null)
//										particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
//									else
//										particles = settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.BLOCK_REGENERATING);
//									for (ParticleData particle : particles) {
//										Location location = null;
//										switch (particle.getPlayAt()) {
//											case ANYWHERE:
//											case RANDOM:
//												location = explosion.getBlocks().get(random).getLocation();
//												break;
//											case EXPLOSION:
//												location = explosion.getLocation();
//												break;
//											case NEXT_BLOCK:
//												location = explosion.getBlocks().iterator().next().getLocation();
//												break;
//											case PREVIOUS_BLOCK:
//												location = explosion.getPreviousBlock().getLocation();
//												break;
//										}
//										particle.playParticle(location);
//									}
//								}
//							});
//						else {
//							List<ParticleData> particles;
//							if(settings.getParticleSettings(settings.getParticleType()) == null)
//								particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
//							else
//								particles = settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.BLOCK_REGENERATING);
//							for (ParticleData particle : particles) {
//								Location location = null;
//								switch (particle.getPlayAt()) {
//									case ANYWHERE:
//									case RANDOM:
//										location = explosion.getBlocks().get(random).getLocation();
//										break;
//									case EXPLOSION:
//										location = explosion.getLocation();
//										break;
//									case NEXT_BLOCK:
//										location = explosion.getBlocks().iterator().next().getLocation();
//										break;
//									case PREVIOUS_BLOCK:
//										location = explosion.getPreviousBlock().getLocation();
//										break;
//								}
//								particle.playParticle(location);
//							}
//						}
//						if (explosion.getExplosionSettings().getAllowSound(phase)) {
//							if (ExplosionRegen.getSettings().getAllowProfileSettings())
//								Bukkit.getOnlinePlayers().forEach(player -> {
//									if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
//										ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.BLOCK_REGENERATING).playSound(explosion.getBlocks().get(random).getLocation(), player);
//									else
//										settings.getSoundSettings().getSound(ExplosionPhase.BLOCK_REGENERATING).playSound(explosion.getBlocks().get(random).getLocation(), player);
//								});
//							else
//								settings.getSoundSettings().getSound(phase).playSound(explosion.getBlocks().get(random).getLocation());
//						}
						phase = ExplosionPhase.ON_BLOCK_REGEN;
						if (explosion.getRegenTick() > 0) {
							explosion.setRegenTick(explosion.getRegenTick() - 1);
						} else {
							if (explosion.getExplosionSettings().isInstantRegen()) {
								for (RegenBlock block : explosion.getBlocks()) {
									explosion.regenerate(block);
								}
							} else {
								for (RegenBlock block : explosion.getQueueBlocks()) {
									if (block.getRegenDelay() > 0)
										block.setRegenDelay(block.getRegenDelay() - 1);
									else {
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
										explosion.regenerate(block);
									}
								}
							}
						}
					} else {
						phase = ExplosionPhase.EXPLOSION_FINISHED_REGEN;
//						if (ExplosionRegen.getSettings().getAllowProfileSettings())
//							Bukkit.getOnlinePlayers().forEach(player -> {
//								if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
//									ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
//									List<ParticleData> particles;
//									if(pSettings.getParticleSettings(pSettings.getParticleType()) == null)
//										particles = ParticleSettings.getSettings(pSettings.getExplosionSettings().getName() + "_vanilla").getParticles();
//									else
//										particles = pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.EXPLOSION_FINISHED_REGEN);
//									for (ParticleData particle : particles) {
//										Location location = null;
//										switch (particle.getPlayAt()) {
//											case ANYWHERE:
//											case NEXT_BLOCK:
//											case RANDOM:
//											case EXPLOSION:
//												location = explosion.getLocation();
//												break;
//											case PREVIOUS_BLOCK:
//												location = explosion.getPreviousBlock().getLocation();
//												break;
//										}
//										particle.playParticle(location);
//									}
//								} else {
//									List<ParticleData> particles;
//									if(settings.getParticleSettings(settings.getParticleType()) == null)
//										particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
//									else
//										particles = settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.EXPLOSION_FINISHED_REGEN);
//									for (ParticleData particle : particles) {
//										Location location = null;
//										switch (particle.getPlayAt()) {
//											case ANYWHERE:
//											case NEXT_BLOCK:
//											case RANDOM:
//											case EXPLOSION:
//												location = explosion.getLocation();
//												break;
//											case PREVIOUS_BLOCK:
//												location = explosion.getPreviousBlock().getLocation();
//												break;
//										}
//										particle.playParticle(location);
//									}
//								}
//							});
//						else {
//							List<ParticleData> particles;
//							if(settings.getParticleSettings(settings.getParticleType()) == null)
//								particles = ParticleSettings.getSettings(settings.getName() + "_vanilla").getParticles();
//							else
//								particles = settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.EXPLOSION_FINISHED_REGEN);
//							for (ParticleData particle : particles) {
//								Location location = null;
//								switch (particle.getPlayAt()) {
//									case ANYWHERE:
//									case NEXT_BLOCK:
//									case RANDOM:
//									case EXPLOSION:
//										location = explosion.getLocation();
//										break;
//									case PREVIOUS_BLOCK:
//										location = explosion.getPreviousBlock().getLocation();
//										break;
//								}
//								particle.playParticle(location);
//							}
//						}
//						if (explosion.getExplosionSettings().getAllowSound(phase)) {
//							if (ExplosionRegen.getSettings().getAllowProfileSettings())
//								Bukkit.getOnlinePlayers().forEach(player -> {
//									if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
//										ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN).playSound(explosion.getLocation(), player);
//									else
//										settings.getSoundSettings().getSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN).playSound(explosion.getLocation(), player);
//								});
//							else
//								settings.getSoundSettings().getSound(phase).playSound(explosion.getLocation());
//						}
						ExplosionRegenFinishEvent e = new ExplosionRegenFinishEvent(explosion);
						Bukkit.getPluginManager().callEvent(e);
						explosion.remove();
					}
				}
			}
		};
		runnable.runTaskTimer(ExplosionRegen.getInstance(), 0, 1);
	}
	
	public void createExplosion(Location location, ExplosionSettings settings, boolean force, float power, boolean setFire) {
		if(force || settings.getAllowExplosion()) {
			Listener listener = new Listener() {
				@EventHandler(ignoreCancelled = true)
				public void t(ExplosionTriggerEvent event) {
					event.getExplosion().setSettings(settings);
				}
			};
			ExplosionRegen.getInstance().getServer().getPluginManager().registerEvents(listener, ExplosionRegen.getInstance());
			location.getWorld().createExplosion(location, power, setFire);
			ExplosionTriggerEvent.getHandlerList().unregister(listener);
		}
	}
	
//	public List<ExplosionSettings> getSupportedEntities() {
//		List<ExplosionSettings> list = new ArrayList<>();
//		for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
//			if(settings.getExplosionType() == ExplosionType.ENTITY) {
//				list.add(settings);
//			}
//		}
//		return list;
//	}
//	public List<ExplosionSettings> getSupportedBlocks() {
//		List<ExplosionSettings> list = new ArrayList<>();
//		for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
//			if(settings.getExplosionType() == ExplosionType.BLOCK) {
//				list.add(settings);
//			}
//		}
//		return list;
//	}
//	public List<ExplosionSettings> getSupportedCustoms() {
//		List<ExplosionSettings> list = new ArrayList<>();
//		for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
//			if(settings.getExplosionType() == ExplosionType.CUSTOM) {
//				list.add(settings);
//			}
//		}
//		return list;
//	}	
}
