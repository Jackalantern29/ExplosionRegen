package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.ProfileSettings.ERProfileExplosionSettings;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.events.ExplosionTriggerEvent;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;
import org.bukkit.material.Chest;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ExplosionMap implements Listener {
	private final Map<Location, BlockData> blockMap = new HashMap<>();
	private final LinkedList<Explosion> explosions = new LinkedList<>();

	public ExplosionMap() {
		BukkitRunnable runnable = new BukkitRunnable() {
			public void run() {
				for (Explosion explosion : new ArrayList<>(getExplosions())) {
					ExplosionSettings settings = explosion.getExplosionSettings();
					ExplosionPhase phase;
					if (!explosion.getBlocks().isEmpty()) {
						int random = new Random().nextInt(explosion.getBlocks().size());
						phase = ExplosionPhase.BLOCK_REGENERATING;
						if (ExplosionRegen.getSettings().getAllowPlayerSettings())
							Bukkit.getOnlinePlayers().forEach(player -> {
								if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
									ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
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
									if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
										ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.BLOCK_REGENERATING).playSound(explosion.getBlocks().get(random).getLocation(), player);
									else
										settings.getSoundSettings().getSound(ExplosionPhase.BLOCK_REGENERATING).playSound(explosion.getBlocks().get(random).getLocation(), player);
								});
							else
								settings.getSoundSettings().getSound(phase).playSound(explosion.getBlocks().get(random).getLocation());
						}
						phase = ExplosionPhase.ON_BLOCK_REGEN;
						if (explosion.getRegenTick() > 0) {
							explosion.setRegenTick(explosion.getRegenTick() - 1);
						} else {
							if (explosion.getExplosionSettings().isInstantRegen()) {
								for (BlockData block : explosion.getBlocks()) {
									explosion.regenerate(block);
								}
							} else {
								for (BlockData block : explosion.getQueueBlocks()) {
									if (block.getRegenDelay() > 0)
										block.setRegenDelay(block.getRegenDelay() - 1);
									else {
										if (ExplosionRegen.getSettings().getAllowPlayerSettings())
											Bukkit.getOnlinePlayers().forEach(player -> {
												if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
													ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
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
													if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
														ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.ON_BLOCK_REGEN).playSound(block.getLocation(), player);
													else
														settings.getSoundSettings().getSound(ExplosionPhase.ON_BLOCK_REGEN).playSound(block.getLocation(), player);
												});
											else
												settings.getSoundSettings().getSound(phase).playSound(block.getLocation());
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
								if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
									ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
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
									if (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
										ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN).playSound(explosion.getLocation(), player);
									else
										settings.getSoundSettings().getSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN).playSound(explosion.getLocation(), player);
								});
							else
								settings.getSoundSettings().getSound(phase).playSound(explosion.getLocation());
						}
						removeExplosion(explosion);
					}
				}
			}
		};
		runnable.runTaskTimer(ExplosionRegen.getInstance(), 0, 1);
	}


//	public ERExplosion addExplosion(ExplosionSettings settings, Location location) {
//		return addExplosion(settings, location, null);
//	}
	private List<Block> calculateAdjacentBlocks(Block block) {
		List<Block> list = new ArrayList<>();
		if(UpdateType.isPostUpdate(UpdateType.COLOR_UPDATE)) {
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
	public Explosion addExplosion(ExplosionSettings settings, Location location, List<Block> blockList, double blockDamage) {
		Explosion explosion = new Explosion(settings, location);
		if(blockList != null && !blockList.isEmpty()) {


			Set<Block> addLater = new HashSet<>();
			for(Block block : new ArrayList<>(blockList)) {
				if(block.getType() != Material.TNT) {
					if(MaterialUtil.equalsMaterial(block.getType(), "NETHER_PORTAL")) {
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
						} else {
							if(block.getState().getData() instanceof Bed) {
								Bed bed = (Bed) block.getState().getData();
								addLater.add(block);
								blockList.remove(block);
								Block bed2 = null;
								switch(bed.getFacing()) {
									case NORTH:
										if(bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.SOUTH);
										else
											bed2 = block.getRelative(BlockFace.NORTH);
										break;
									case SOUTH:
										if(bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.NORTH);
										else
											bed2 = block.getRelative(BlockFace.SOUTH);
										break;
									case EAST:
										if(bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.WEST);
										else
											bed2 = block.getRelative(BlockFace.EAST);
										break;
									case WEST:
										if(bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.EAST);
										else
											bed2 = block.getRelative(BlockFace.WEST);
										break;
								}
								if (bed2 != null && bed2.getState().getData() instanceof Bed) {
									addLater.add(bed2);
									blockList.remove(bed2);
								}
							}
						}
					}
				}
			}
			blockList.addAll(0, addLater);

			if(settings.getAllowRegen()) {
				for(Block block : new ArrayList<>(blockList)) {
					BlockData blockData = new BlockData(block.getState(), 0, settings.getBlockSettings().get(block.getType()).getDurability());
					BlockSettingsData bs = settings.getBlockSettings().get(blockData.getMaterial());

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
						blockData.setContents(inventory.getContents());
						inventory.clear();
					} else if(state instanceof Sign) {
						Sign sign = (Sign)state;
						blockData.setContents(sign.getLines());
					}
					if(blockMap.containsKey(block.getLocation())) {
						BlockData b = blockMap.get(block.getLocation());
						blockData.setDurability(b.getDurability() - blockDamage);
						blockMap.remove(block.getLocation());
					} else {
						blockData.setDurability(blockData.getDurability() - blockDamage);
					}
					if(blockData.getDurability() <= 0.0d) {
						if(!bs.doPreventDamage()) {
							if(bs.doRegen()) {
								explosion.addBlock(blockData);

								if(block.getState().getData() instanceof Bed) {
									block.setType(Material.AIR, false);
								}

							} else {
								Random r = new Random();
								int random = r.nextInt(99);
								if(random <= bs.getDropChance() - 1)
									block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(bs.getResult()));
							}
						} else {
							blockData.getBlock().setType(bs.getResult());
							blockList.remove(block);
						}
					} else {
						blockList.remove(block);
						blockMap.put(block.getLocation(), blockData);
					}
				}
			}
			if(!explosion.getBlocks().isEmpty()) {
				ExplosionPhase phase = ExplosionPhase.ON_EXPLODE;
				int random = new Random().nextInt(explosion.getBlocks().size());
				if(ExplosionRegen.getSettings().getAllowPlayerSettings())
					Bukkit.getOnlinePlayers().forEach(player -> {
						if(ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings)) {
							ERProfileExplosionSettings pSettings = ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(explosion.getExplosionSettings());
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
							if(ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().contains(settings))
								ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(settings).getSound(phase).playSound(location, player);
							else
								settings.getSoundSettings().getSound(phase).playSound(explosion.getLocation(), player);
						});
					else
						settings.getSoundSettings().getSound(phase).playSound(explosion.getLocation());
				}
			}
			explosions.add(explosion);
		}
		return explosion;
	}
	
	public void removeExplosion(Explosion explosion) {
		explosions.remove(explosion);
	}
	
	public LinkedList<Explosion> getExplosions() {
		return explosions;
	}
	
	public void createExplosion(Location location, ExplosionSettings settings, boolean force, float power, boolean setFire) {
		if(force || settings.getAllowExplosion()) {
			Listener listener = new Listener() {
				@EventHandler
				public void t(ExplosionTriggerEvent event) {
					event.setSettings(settings);
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
