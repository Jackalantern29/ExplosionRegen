package com.jackalantern29.explosionregen.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.material.Chest;
import org.bukkit.scheduler.BukkitRunnable;

import com.cryptomorin.xseries.XMaterial;
import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.ERProfileSettings.ERProfileExplosionSettings;
import com.jackalantern29.explosionregen.api.enums.ERMCUpdateType;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.events.ERExplodeEvent;

public class ERExplosionMap implements Listener {
	private final Map<Location, ERBlock> blockMap = new HashMap<>();
	private final LinkedList<ERExplosion> explosions = new LinkedList<>();

	public ERExplosionMap() {
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				for (ERExplosion explosion : new ArrayList<>(getExplosions())) {
					ExplosionSettings settings = explosion.getExplosionSettings();
					ExplosionPhase phase;
					if (!explosion.getBlocks().isEmpty()) {
						int random = new Random().nextInt(explosion.getBlocks().size());
						phase = ExplosionPhase.BLOCK_REGENERATING;
						if (ExplosionRegen.getSettings().getAllowPlayerSettings())
							Bukkit.getOnlinePlayers().forEach(player -> {
								if (ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
									ERProfileExplosionSettings pSettings = ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
									for (ParticleData particle : pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.BLOCK_REGENERATING)) {
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
								} else {
									for (ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.BLOCK_REGENERATING)) {
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
							});
						else {
							for (ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.BLOCK_REGENERATING)) {
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
						if (explosion.getExplosionSettings().getAllowSound(phase)) {
							if (ExplosionRegen.getSettings().getAllowPlayerSettings())
								Bukkit.getOnlinePlayers().forEach(player -> {
									if (ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
										ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.BLOCK_REGENERATING).playSound(explosion.getBlocks().get(random).getLocation(), player);
									else
										settings.getSound(ExplosionPhase.BLOCK_REGENERATING).playSound(explosion.getBlocks().get(random).getLocation(), player);
								});
							else
								settings.getSound(phase).playSound(explosion.getBlocks().get(random).getLocation());
						}
						phase = ExplosionPhase.ON_BLOCK_REGEN;
						if (explosion.getRegenTick() > 0) {
							explosion.setRegenTick(explosion.getRegenTick() - 1);
						} else {
							if (explosion.getExplosionSettings().isInstantRegen()) {
								for (ERBlock block : explosion.getBlocks()) {
									explosion.regenerate(block);
								}
							} else {
								for (ERBlock block : explosion.getQueueBlocks()) {
									if (block.getRegenDelay() > 0)
										block.setRegenDelay(block.getRegenDelay() - 1);
									else {
										if (ExplosionRegen.getSettings().getAllowPlayerSettings())
											Bukkit.getOnlinePlayers().forEach(player -> {
												if (ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
													ERProfileExplosionSettings pSettings = ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
													for (ParticleData particle : pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN)) {
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
																location = explosion.getBlocks().iterator().next().getLocation();
																break;
															case PREVIOUS_BLOCK:
																location = explosion.getPreviousBlock().getLocation();
																break;
														}
														particle.playParticle(location);
													}
												} else {
													for (ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN)) {
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
																location = explosion.getBlocks().iterator().next().getLocation();
																break;
															case PREVIOUS_BLOCK:
																location = explosion.getPreviousBlock().getLocation();
																break;
														}
														particle.playParticle(location);
													}
												}
											});
										else {
											for (ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN)) {
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
										}
										if (explosion.getExplosionSettings().getAllowSound(phase)) {
											if (ExplosionRegen.getSettings().getAllowPlayerSettings())
												Bukkit.getOnlinePlayers().forEach(player -> {
													if (ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
														ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.ON_BLOCK_REGEN).playSound(block.getLocation(), player);
													else
														settings.getSound(ExplosionPhase.ON_BLOCK_REGEN).playSound(block.getLocation(), player);
												});
											else
												settings.getSound(phase).playSound(block.getLocation());
										}
										explosion.regenerate(block);
									}
								}
							}
						}
					} else {
						phase = ExplosionPhase.EXPLOSION_FINISHED_REGEN;
						if (ExplosionRegen.getSettings().getAllowPlayerSettings())
							Bukkit.getOnlinePlayers().forEach(player -> {
								if (ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
									ERProfileExplosionSettings pSettings = ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
									for (ParticleData particle : pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.EXPLOSION_FINISHED_REGEN)) {
										Location location = null;
										switch (particle.getPlayAt()) {
											case ANYWHERE:
											case NEXT_BLOCK:
											case RANDOM:
											case EXPLOSION:
												location = explosion.getLocation();
												break;
											case PREVIOUS_BLOCK:
												location = explosion.getPreviousBlock().getLocation();
												break;
										}
										particle.playParticle(location);
									}
								} else {
									for (ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.EXPLOSION_FINISHED_REGEN)) {
										Location location = null;
										switch (particle.getPlayAt()) {
											case ANYWHERE:
											case NEXT_BLOCK:
											case RANDOM:
											case EXPLOSION:
												location = explosion.getLocation();
												break;
											case PREVIOUS_BLOCK:
												location = explosion.getPreviousBlock().getLocation();
												break;
										}
										particle.playParticle(location);
									}
								}
							});
						else {
							for (ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.EXPLOSION_FINISHED_REGEN)) {
								Location location = null;
								switch (particle.getPlayAt()) {
									case ANYWHERE:
									case NEXT_BLOCK:
									case RANDOM:
									case EXPLOSION:
										location = explosion.getLocation();
										break;
									case PREVIOUS_BLOCK:
										location = explosion.getPreviousBlock().getLocation();
										break;
								}
								particle.playParticle(location);
							}
						}
						if (explosion.getExplosionSettings().getAllowSound(phase)) {
							if (ExplosionRegen.getSettings().getAllowPlayerSettings())
								Bukkit.getOnlinePlayers().forEach(player -> {
									if (ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
										ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN).playSound(explosion.getLocation(), player);
									else
										settings.getSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN).playSound(explosion.getLocation(), player);
								});
							else
								settings.getSound(phase).playSound(explosion.getLocation());
						}
						removeExplosion(explosion);
					}
				}
			}
		};
		runnable.runTaskTimer(ExplosionRegen.getInstance(), 0, 1);
	}


	public ERExplosion addExplosion(ExplosionSettings settings, Location location) {
		return addExplosion(settings, location, null);
	}
	private List<Block> calculateAdjacentBlocks(Block block) {
		List<Block> list = new ArrayList<>();
		if(ERMCUpdateType.isPostUpdate(ERMCUpdateType.COLOR_UPDATE)) {
			if(block.getState() instanceof Bisected) {
				Bisected bi = (Bisected)block.getState();
				if(bi.getHalf() == Half.BOTTOM)
					list.add(block.getRelative(0, 1, 0));
				else
					list.add(block.getRelative(0, -1, 0));
			}
		} else {
			if(block.getType() == Material.DOUBLE_PLANT) {
				if(block.getRelative(0, 1, 0).getType() == Material.DOUBLE_PLANT)
					list.add(block.getRelative(0, 1, 0));
				else if(block.getRelative(0, -1, 0).getType() == Material.DOUBLE_PLANT)
					list.add(block.getRelative(0, 1, 0));
			}
		}
		return list;
	}
	public ERExplosion addExplosion(ExplosionSettings settings, Location location, List<Block> blockList) {
		ERExplosion explosion = new ERExplosion(settings, location);
		if(blockList != null && !blockList.isEmpty()) {


			Set<Block> addLater = new HashSet<>();
			for(Block block : new ArrayList<>(blockList)) {
				if(block.getType() != XMaterial.TNT.parseMaterial()) {
					if(block.getType() == XMaterial.NETHER_PORTAL.parseMaterial()) {
						blockList.remove(block);
						if(block.getRelative(0, -1, 0).getType() == Material.AIR) {
							addLater.add(block);
						} else {
							addLater.add(block.getRelative(0, 1, 0));
						}
					} else if(block.getType().isTransparent()) {
						addLater.add(block);
						blockList.remove(block);
					} else {
						if(block.getRelative(0, 1, 0).getType() != Material.AIR && block.getRelative(0, 1, 0).getType().isTransparent()) {
							addLater.add(block.getRelative(0, 1, 0));
							addLater.addAll(calculateAdjacentBlocks(block.getRelative(0, 1, 0)));
							blockList.remove(block.getRelative(0, 1, 0));
							blockList.removeAll(calculateAdjacentBlocks(block.getRelative(0, 1, 0)));
						}
					}
				}
			}
			blockList.addAll(0, addLater);

			if(settings.getAllowRegen()) {
				for(Block block : new ArrayList<>(blockList)) {
					ERBlock erBlock = new ERBlock(block.getState(), settings.getBlockRegenDelay(), settings.getBlockSettings().get(XMaterial.matchXMaterial(block.getType())).getDurability());
					BlockSettingsData bs = settings.getBlockSettings().get(erBlock.getMaterial());

					BlockState state = block.getState();
					if(bs.doSaveItems() && state instanceof InventoryHolder) {
						Inventory inventory = ((InventoryHolder)state).getInventory();
						if(inventory instanceof DoubleChestInventory) {
							DoubleChestInventory dChest = (DoubleChestInventory)inventory;
							if(state.getData() instanceof Chest) {
								Chest chest = (Chest)state.getData();
								if(chest.getFacing() == BlockFace.NORTH) {
									if(block.getRelative(1, 0, 0).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								} else if(chest.getFacing() == BlockFace.SOUTH) {
									if(block.getRelative(-1, 0, 0).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								} else if(chest.getFacing() == BlockFace.EAST) {
									if(block.getRelative(0, 0, 1).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								} else if(chest.getFacing() == BlockFace.WEST) {
									if(block.getRelative(0, 0, -1).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								}
							}
						}
						erBlock.setContents(inventory.getContents());
						inventory.clear();
					} else if(state instanceof Sign) {
						Sign sign = (Sign)state;
						erBlock.setContents(sign.getLines());
					}

					if(blockMap.containsKey(block.getLocation())) {
						ERBlock b = blockMap.get(block.getLocation());
						erBlock.setDurability(b.getDurability() - 4);
						blockMap.remove(block.getLocation());
					} else {
						erBlock.setDurability(erBlock.getDurability() - 4);
					}
					if(erBlock.getDurability() <= 0.0d) {
						if(bs.doReplace()) {
							erBlock.setMaterial(bs.getReplaceWith());
						}
						if(!bs.doPreventDamage()) {
							if(bs.doRegen()) {
								explosion.addBlock(erBlock);
							} else {
								Random r = new Random();
								int random = r.nextInt(99);
								if(random <= bs.getDropChance() - 1)
									block.getLocation().getWorld().dropItemNaturally(block.getLocation(), bs.getMaterial().parseItem());
							}
						} else
							erBlock.getBlock().setType(bs.getMaterial().parseMaterial());
					} else {
						//Bukkit.getPlayer("Jack").sendMessage("" + bs.getMaterial());
						blockList.remove(block);
						blockMap.put(block.getLocation(), erBlock);
					}
				}
			}
			if(!explosion.getBlocks().isEmpty()) {
				ExplosionPhase phase = ExplosionPhase.ON_EXPLODE;
				int random = new Random().nextInt(explosion.getBlocks().size());
				if(ExplosionRegen.getSettings().getAllowPlayerSettings())
					Bukkit.getOnlinePlayers().forEach(player -> {
						if(ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
							ERProfileExplosionSettings pSettings = ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
							for(ParticleData particle : pSettings.getParticleSettings(pSettings.getParticleType()).getParticles(ExplosionPhase.ON_EXPLODE)) {
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
						} else {
							for(ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN)) {
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
						}
					});
				else {
					for(ParticleData particle : settings.getParticleSettings(settings.getParticleType()).getParticles(ExplosionPhase.ON_BLOCK_REGEN)) {
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
				}
				if(explosion.getExplosionSettings().getAllowSound(phase)) {
					if(ExplosionRegen.getSettings().getAllowPlayerSettings())
						Bukkit.getOnlinePlayers().forEach(player -> {
							if(ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
								ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(phase).playSound(location, player);
							else
								settings.getSound(phase).playSound(explosion.getLocation(), player);
						});
					else
						settings.getSound(phase).playSound(explosion.getLocation());
				}
			}
			explosions.add(explosion);
		}
		return explosion;
	}
	
	public void removeExplosion(ERExplosion explosion) {
		explosions.remove(explosion);
	}
	
	public LinkedList<ERExplosion> getExplosions() {
		return explosions;
	}
	
	public void createExplosion(Location location, ExplosionSettings settings, boolean force, float power, boolean setFire) {
		if(force || settings.getAllowExplosion()) {
			Listener listener = new Listener() {
				@EventHandler
				public void t(ERExplodeEvent event) {
					event.setSettings(settings);
				}
			};
			ExplosionRegen.getInstance().getServer().getPluginManager().registerEvents(listener, ExplosionRegen.getInstance());
			location.getWorld().createExplosion(location, power, setFire);
			ERExplodeEvent.getHandlerList().unregister(listener);
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
