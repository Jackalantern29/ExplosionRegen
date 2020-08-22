package com.jackalantern29.explosionregen.api;

import com.google.common.collect.ImmutableList;
import com.jackalantern29.explosionregen.BukkitMethods;
import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.blockdata.PistonData;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.DamageCategory;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.enums.GenerateDirection;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import com.jackalantern29.explosionregen.api.events.ExplosionBlockRegenEvent;
import net.coreprotect.CoreProtectAPI;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;

import java.util.*;
import java.util.stream.Collectors;

public class Explosion {
	private static final Map<Location, RegenBlock> BLOCK_MAP = new HashMap<>();
	private static final LinkedList<Explosion> ACTIVE_EXPLOSIONS = new LinkedList<>();

	private ExplosionSettings settings;
	private final Location location;
	private final Object source;
	private final List<RegenBlock> blocks = new ArrayList<>();
	private final List<Block> blockList;
	private RegenBlock previousBlock;
	private long regenTick;
	private double blockDamage;

	public boolean start = false;

	public Explosion(ExplosionSettings settings, Object source, Location location, List<Block> blockList) {
		this(settings, source, location, blockList, settings.getDamageAmount(DamageCategory.BLOCK));
	}
	public Explosion(ExplosionSettings settings, Object source, Location location, List<Block> blockList, double blockDamage) {
		this.settings = settings;
		this.source = source;
		this.location = location;
		this.regenTick = settings.getRegenDelay();
		this.blockList = blockList;
		this.blockDamage = blockDamage;
	}

	public void start() {
		if(start)
			return;
		if(blockList != null && !blockList.isEmpty()) {
			Set<Block> addLater = new HashSet<>();
			for (Block block : new ArrayList<>(blockList)) {
				if (block.getType() != Material.TNT && block.getType() != MaterialUtil.getMaterial("PISTON_HEAD")) {
					if (MaterialUtil.equalsMaterial(block.getType(), "NETHER_PORTAL")) {
						if (block.getRelative(0, -1, 0).getType() == Material.AIR) {
							addLater.add(block);
						} else {
							addLater.add(block.getRelative(0, 1, 0));
						}
					} else if (block.getType() == MaterialUtil.getMaterial("PISTON") || block.getType() == MaterialUtil.getMaterial("STICKY_PISTON")) {
						PistonData piston = new PistonData(block);
						piston.setExtended(false);
						piston.applyData(block);
					} else if (MaterialUtil.requiresGroundSupport(block.getType())) {
						addLater.add(block);
						blockList.remove(block);
					} else {
						if (block.getRelative(0, 1, 0).getType() != Material.AIR && block.getRelative(0, 1, 0).getType().isTransparent()) {
							addLater.add(block.getRelative(0, 1, 0));
							addLater.addAll(calculateAdjacentBlocks(block.getRelative(0, 1, 0)));
							blockList.remove(block.getRelative(0, 1, 0));
							blockList.removeAll(calculateAdjacentBlocks(block.getRelative(0, 1, 0)));
						} else {
							if (block.getState().getData() instanceof org.bukkit.material.Bed) {
								org.bukkit.material.Bed bed = (org.bukkit.material.Bed) block.getState().getData();
								addLater.add(block);
								blockList.remove(block);
								Block bed2 = null;
								switch (bed.getFacing()) {
									case NORTH:
										if (bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.SOUTH);
										else
											bed2 = block.getRelative(BlockFace.NORTH);
										break;
									case SOUTH:
										if (bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.NORTH);
										else
											bed2 = block.getRelative(BlockFace.SOUTH);
										break;
									case EAST:
										if (bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.WEST);
										else
											bed2 = block.getRelative(BlockFace.EAST);
										break;
									case WEST:
										if (bed.isHeadOfBed())
											bed2 = block.getRelative(BlockFace.EAST);
										else
											bed2 = block.getRelative(BlockFace.WEST);
										break;
								}
								if (bed2 != null && bed2.getState().getData() instanceof org.bukkit.material.Bed) {
									addLater.add(bed2);
									blockList.remove(bed2);
								}
							}
						}
					}
				} else {
					blockList.remove(block);
				}
			}
			blockList.addAll(0, addLater);

			if (settings.getAllowRegen()) {
				for (Block block : new ArrayList<>(blockList)) {
					BlockSettingsData bs = settings.getBlockSettings().get(new RegenBlockData(block));
					RegenBlock regenBlock = new RegenBlock(block, bs.getReplaceWith(), bs.getRegenDelay(), bs.getDurability());
					BlockState state = block.getState();
					if (bs.doSaveItems() && state instanceof InventoryHolder) {
						Inventory inventory = ((InventoryHolder) state).getInventory();
						if (inventory instanceof DoubleChestInventory) {
							DoubleChestInventory dChest = (DoubleChestInventory) inventory;
							if (state.getData() instanceof Chest) {
								Chest chest = (Chest) state.getData();
								if (chest.getFacing() == BlockFace.NORTH) {
									if (block.getRelative(1, 0, 0).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								} else if (chest.getFacing() == BlockFace.SOUTH) {
									if (block.getRelative(-1, 0, 0).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								} else if (chest.getFacing() == BlockFace.EAST) {
									if (block.getRelative(0, 0, 1).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								} else if (chest.getFacing() == BlockFace.WEST) {
									if (block.getRelative(0, 0, -1).getType() == Material.CHEST)
										inventory = dChest.getLeftSide();
									else
										inventory = dChest.getRightSide();
								}
							}
						}
						regenBlock.setContents(inventory.getContents());
						inventory.clear();
					} else if (state instanceof Sign) {
						Sign sign = (Sign) state;
						regenBlock.setContents(sign.getLines());
					}
					if (BLOCK_MAP.containsKey(block.getLocation())) {
						RegenBlock b = BLOCK_MAP.get(block.getLocation());
						regenBlock.setDurability(b.getDurability() - blockDamage);
						BLOCK_MAP.remove(block.getLocation());
					} else {
						regenBlock.setDurability(regenBlock.getDurability() - blockDamage);
					}
					if (regenBlock.getDurability() <= 0.0d) {
						if (!bs.doPreventDamage()) {
							if (bs.doRegen()) {
								addBlock(regenBlock);
								if (block.getState().getData() instanceof org.bukkit.material.Bed) {
									block.setType(Material.AIR, false);
								}
								if (ExplosionRegen.getInstance().getCoreProtect() != null) {
									if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
										ExplosionRegen.getInstance().getCoreProtect().logRemoval("#explosionregen", block.getLocation(), block.getType(), BukkitMethods.getBlockData(block.getState()));
									else
										ExplosionRegen.getInstance().getCoreProtect().logRemoval("#explosionregen", block.getLocation(), block.getType(), block.getData());
								}
							} else {
								Random r = new Random();
								int random = r.nextInt(99);
								if (random <= bs.getDropChance() - 1)
									block.getLocation().getWorld().dropItemNaturally(block.getLocation(), new ItemStack(bs.getResult().getMaterial()));
							}
						} else {
							blockList.remove(block);
							regenBlock.setBlock();
						}
					} else {
						blockList.remove(block);
						BLOCK_MAP.put(block.getLocation(), regenBlock);
					}
				}
			}
			ACTIVE_EXPLOSIONS.add(this);
			start = true;
		}
	}

	public ExplosionSettings getSettings() {
		return settings;
	}
	public void setSettings(ExplosionSettings settings) {
		this.settings = settings;
	}
	private List<Block> calculateAdjacentBlocks(Block block) {
		List<Block> list = new ArrayList<>();
		if(UpdateType.isPostUpdate(UpdateType.COLOR_UPDATE)) {
			if(block.getState() instanceof Bisected) {
				Bisected bi = (Bisected)block.getState();
				if(bi.getHalf() == Bisected.Half.BOTTOM)
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

	public ExplosionSettings getExplosionSettings() {
		return settings;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void addBlocks(List<RegenBlock> blocks) {
		for(RegenBlock b0 : blocks) {
			boolean doadd = true;
			for(RegenBlock b1 : this.blocks) {
				if(b1.getLocation().equals(b0.getLocation()))
					doadd = false;
			}
			if(doadd)
				this.blocks.add(b0);
		}
	}
	public void addBlock(RegenBlock block) {
		blocks.add(block);
	}
	
	public List<RegenBlock> getBlocks() {
		return getBlocks(settings.getRegenerateDirections());
	}
	
	
	public List<RegenBlock> getBlocks(List<GenerateDirection> direction) {
		List<RegenBlock> list = new ArrayList<>(blocks);
		list.sort((o1, o2) -> {
			CompareToBuilder builder = new CompareToBuilder();
			for(GenerateDirection d : direction) {
				switch(d) {
				case UP:
				case RANDOM_UP:
					builder.append(o1.getLocation().getBlockY(), o2.getLocation().getBlockY());
					break;
				case DOWN:
					builder.append(o2.getLocation().getBlockY(), o1.getLocation().getBlockY());
					break;
				case EAST:
					builder.append(o1.getLocation().getBlockX(), o2.getLocation().getBlockX());
					break;
				case WEST:
					builder.append(o2.getLocation().getBlockX(), o1.getLocation().getBlockX());
					break;
				case NORTH:
					builder.append(o2.getLocation().getBlockZ(), o1.getLocation().getBlockZ());
					break;
				case SOUTH:
					builder.append(o1.getLocation().getBlockZ(), o2.getLocation().getBlockZ());
					break;
				}
			}
			return builder.toComparison();
		});
		if(direction.contains(GenerateDirection.RANDOM_UP)) {
			HashMap<Integer, List<RegenBlock>> map = new HashMap<>();
			for(RegenBlock block : list) {
				List<RegenBlock> l;
				if(!map.containsKey(block.getLocation().getBlockY()))
					l = new ArrayList<>();
				else
					l = map.get(block.getLocation().getBlockY());					
				l.add(block);
				map.put(block.getLocation().getBlockY(), l);
			}
			list = new ArrayList<>();
			List<Integer> ls = new ArrayList<>(map.keySet());
			ls.sort((o1, o2) -> new CompareToBuilder().append(o1, o2).toComparison());
			for(int i : ls) {
				Collections.shuffle(map.get(i));
				list.addAll(map.get(i));
			}
		}
		return list;
	}
	
	public void removeBlock(Location location) {
		for(RegenBlock block : new ArrayList<>(getBlocks())) {
			if(block.getLocation().equals(location))
				blocks.remove(block);
		}
	}
	
	public boolean canRegenerate() {
		return true;
	}

	
	public void regenerate(RegenBlock block) {
		if(block.getType() == MaterialUtil.getMaterial("NETHER_PORTAL")) {
			if(block.getBlock().getType() == MaterialUtil.getMaterial("NETHER_PORTAL")) {
				removeBlock(block.getLocation());
				return;
			} else if(block.getBlock().getType() == Material.AIR) {
				if(block.getBlock().getRelative(0, -1, 0).getType() == Material.AIR)
					block.getState().setType(Material.FIRE);
				else
					block.getState().setType(Material.AIR);
			}
		}
		BlockState state = block.getState();
		BlockState bState = block.getBlock().getState();
		if(settings.getRegenForceBlock()) {
			block.getBlock().breakNaturally();
		}
		state.update(true);
		state = block.getBlock().getState();
		if(state instanceof InventoryHolder) {
			Inventory inventory = ((InventoryHolder) state).getInventory();
			if(inventory instanceof DoubleChestInventory) {
				DoubleChestInventory dChest = (DoubleChestInventory)inventory;
				if(state.getData() instanceof Chest) {
					Chest chest = (Chest)state.getData();
					if(chest.getFacing() == BlockFace.NORTH) {
						if(block.getBlock().getRelative(1, 0, 0).getType() == Material.CHEST)
							inventory = dChest.getLeftSide();
						else
							inventory = dChest.getRightSide();
					} else if(chest.getFacing() == BlockFace.SOUTH) {
						if(block.getBlock().getRelative(-1, 0, 0).getType() == Material.CHEST)
							inventory = dChest.getLeftSide();
						else
							inventory = dChest.getRightSide();
					} else if(chest.getFacing() == BlockFace.EAST) {
						if(block.getBlock().getRelative(0, 0, 1).getType() == Material.CHEST)
							inventory = dChest.getLeftSide();
						else
							inventory = dChest.getRightSide();
					} else if(chest.getFacing() == BlockFace.WEST) {
						if(block.getBlock().getRelative(0, 0, -1).getType() == Material.CHEST)
							inventory = dChest.getLeftSide();
						else
							inventory = dChest.getRightSide();
					}
				}
			}
			if(block.getContents() != null)
				inventory.setContents((ItemStack[]) block.getContents());
			state.update(true);
		} else if(state instanceof Sign) {
			Sign sign = (Sign)state;
			for(int i = 0; i < 4; i++)
				sign.setLine(i, (String) block.getContents()[i]);
			state.update(true);
		}
		if(bState.getType() != Material.AIR) {
			block.getBlock().breakNaturally();
			bState.update(true);
		}
		removeBlock(block.getLocation());
		previousBlock = block;
		CoreProtectAPI coreProtect = ExplosionRegen.getInstance().getCoreProtect();
		if(coreProtect != null) {
			if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
				coreProtect.logPlacement("#explosionregen", block.getLocation(), block.getBlock().getType(), BukkitMethods.getBlockData(block.getBlock().getState()));
			else
				coreProtect.logPlacement("#explosionregen", block.getLocation(), block.getBlock().getType(), block.getBlock().getData());
		}
		ExplosionBlockRegenEvent e = new ExplosionBlockRegenEvent(this);
		Bukkit.getPluginManager().callEvent(e);
	}
	public void regenerateAll() {
		for(RegenBlock block : new ArrayList<>(blocks))
			regenerate(block);
	}

	
	public long getRegenTick() {
		return regenTick;
	}
	
	public void setRegenTick(long regenTick) {
		this.regenTick = regenTick;
	}
	
	public Set<RegenBlock> getQueueBlocks() {
		return getQueueBlocks(settings.getRegenerateDirections());
	}
	
	public Set<RegenBlock> getQueueBlocks(List<GenerateDirection> direction) {
		Set<RegenBlock> list = new HashSet<>();
		int i = 0;
		int queue = settings.getMaxBlockRegenQueue();
		if(queue <= 0)
			queue = 1;
		for(Iterator<RegenBlock> it = getBlocks(direction).iterator(); it.hasNext(); i++) {
			try {
				list.add(it.next());
			} catch(IndexOutOfBoundsException e) {
				break;
			}
			if(i == queue-1)
				break;
		}
		return list;
	}
	public RegenBlock getPreviousBlock() {
		return previousBlock;
	}

	public void remove() {
		remove(true);
	}

	public void remove(boolean regenerate) {
		if(regenerate)
			regenerateAll();
		ACTIVE_EXPLOSIONS.remove(this);
	}

	public static Collection<Explosion> getActiveExplosions() {
		return ACTIVE_EXPLOSIONS.stream().collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
	}
}
