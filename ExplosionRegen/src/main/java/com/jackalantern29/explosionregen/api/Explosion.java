package com.jackalantern29.explosionregen.api;

import com.google.common.collect.ImmutableList;
import com.jackalantern29.explosionregen.BukkitMethods;
import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.DamageCategory;
import com.jackalantern29.explosionregen.api.enums.GenerateDirection;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import com.jackalantern29.explosionregen.api.events.ExplosionBlockRegenEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionBlockRegeneratingEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionRegenFinishEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionTriggerEvent;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.*;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.Comparator;
import java.util.stream.Collectors;

import static com.jackalantern29.explosionregen.api.enums.GenerateDirection.*;

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

	private static final List<Material> SUPPORT_NEED = new ArrayList<>();
	static {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(ExplosionRegen.getInstance(), () -> {
			for(Explosion explosion : new ArrayList<>(ACTIVE_EXPLOSIONS)) {
				if (!explosion.getBlocks().isEmpty()) {
					if (explosion.getRegenTick() > 0) {
						explosion.setRegenTick(explosion.getRegenTick() - 1);
					} else {
						if (explosion.getSettings().isInstantRegen()) {
							for (RegenBlock block : new ArrayList<>(explosion.getBlocks())) {
								explosion.regenerate(block);
							}
						} else {
							Set<RegenBlock> blocks = explosion.getQueueBlocks();
							for (RegenBlock block : blocks) {
								if (block.getRegenDelay() > 0)
									block.setRegenDelay(block.getRegenDelay() - 1);
								else {
									if(block.getType() == Material.SUGAR_CANE || block.getType() == Material.CACTUS || block.getType() == Material.SCAFFOLDING) {
										for(RegenBlock b : new ArrayList<>(explosion.getBlocks())) {
											if(b.getType() == block.getType())
												explosion.regenerate(b);
										}
									} else
										explosion.regenerate(block);
								}
							}
						}
					}
				} else {
					ExplosionRegenFinishEvent e = new ExplosionRegenFinishEvent(explosion);
					Bukkit.getPluginManager().callEvent(e);
					explosion.remove();
				}

				ExplosionBlockRegeneratingEvent event = new ExplosionBlockRegeneratingEvent(explosion);
				Bukkit.getPluginManager().callEvent(event);
			}
		}, 0, 1);

		SUPPORT_NEED.addAll(Tag.FLOWERS.getValues());
		SUPPORT_NEED.add(Material.GRASS);
		SUPPORT_NEED.add(Material.TORCH);
		SUPPORT_NEED.add(Material.REDSTONE_TORCH);
		SUPPORT_NEED.add(Material.REDSTONE_WIRE);
		SUPPORT_NEED.add(Material.REPEATER);
		SUPPORT_NEED.add(Material.COMPARATOR);
		SUPPORT_NEED.add(Material.TRIPWIRE);
		SUPPORT_NEED.addAll(Tag.SAPLINGS.getValues());
		SUPPORT_NEED.add(Material.BROWN_MUSHROOM);
		SUPPORT_NEED.add(Material.RED_MUSHROOM);
		SUPPORT_NEED.addAll(Tag.RAILS.getValues());
		SUPPORT_NEED.add(Material.SEA_PICKLE);
		SUPPORT_NEED.add(Material.CRIMSON_FUNGUS);
		SUPPORT_NEED.add(Material.WARPED_FUNGUS);
		SUPPORT_NEED.addAll(Tag.BUTTONS.getValues());
		SUPPORT_NEED.addAll(Tag.PRESSURE_PLATES.getValues());
		SUPPORT_NEED.add(Material.LEVER);
		SUPPORT_NEED.add(Material.LADDER);
		SUPPORT_NEED.add(Material.TRIPWIRE_HOOK);
		SUPPORT_NEED.addAll(Tag.CARPETS.getValues());
		SUPPORT_NEED.add(Material.DEAD_BRAIN_CORAL);
		SUPPORT_NEED.add(Material.DEAD_BUBBLE_CORAL);
		SUPPORT_NEED.add(Material.DEAD_FIRE_CORAL);
		SUPPORT_NEED.add(Material.DEAD_HORN_CORAL);
		SUPPORT_NEED.add(Material.DEAD_TUBE_CORAL);
		SUPPORT_NEED.add(Material.DEAD_BRAIN_CORAL_FAN);
		SUPPORT_NEED.add(Material.DEAD_BUBBLE_CORAL_FAN);
		SUPPORT_NEED.add(Material.DEAD_FIRE_CORAL_FAN);
		SUPPORT_NEED.add(Material.DEAD_HORN_CORAL_FAN);
		SUPPORT_NEED.add(Material.DEAD_TUBE_CORAL_FAN);
		SUPPORT_NEED.add(Material.DEAD_BRAIN_CORAL_WALL_FAN);
		SUPPORT_NEED.add(Material.DEAD_BUBBLE_CORAL_WALL_FAN);
		SUPPORT_NEED.add(Material.DEAD_FIRE_CORAL_WALL_FAN);
		SUPPORT_NEED.add(Material.DEAD_HORN_CORAL_WALL_FAN);
		SUPPORT_NEED.add(Material.DEAD_TUBE_CORAL_WALL_FAN);
		SUPPORT_NEED.add(Material.DEAD_BUSH);
		SUPPORT_NEED.addAll(Tag.SIGNS.getValues());
		SUPPORT_NEED.addAll(Tag.BANNERS.getValues());
		SUPPORT_NEED.add(Material.SUGAR_CANE);
		SUPPORT_NEED.add(Material.CACTUS);
		SUPPORT_NEED.add(Material.SCAFFOLDING);
	}

	/**
	 *
	 * @param settings The settings this explosion will use.
	 * @param source The source of this explosion.
	 * @param location The location of this explosion.
	 * @param blockList List of blocks that was damaged in this explosion.
	 */
	public Explosion(ExplosionSettings settings, Object source, Location location, List<Block> blockList) {
		this.settings = settings;
		this.blockDamage = settings.getDamageAmount(DamageCategory.BLOCK);
		this.source = source;
		this.location = location;
		this.regenTick = settings.getRegenDelay();
		this.blockList = blockList;

		//If the block damaged was supporting a block, add the block that needed support
/*		for(Block block : new ArrayList<>(blockList)) {
			if(!blockList.contains(block.getRelative(1, 0, 0)) && SUPPORT_NEED.contains(block.getRelative(1, 0, 0).getType()))
				blockList.add(block.getRelative(1, 0, 0));
			else if(!blockList.contains(block.getRelative(-1, 0, 0)) && SUPPORT_NEED.contains(block.getRelative(-1, 0, 0).getType()))
				blockList.add(block.getRelative(-1, 0, 0));
			else if(!blockList.contains(block.getRelative(0, 1, 0)) && SUPPORT_NEED.contains(block.getRelative(0, 1, 0).getType()))
				blockList.add(block.getRelative(0, 1, 0));
			else if(!blockList.contains(block.getRelative(0, -1, 0)) && SUPPORT_NEED.contains(block.getRelative(0, -1, 0).getType()))
				blockList.add(block.getRelative(0, -1, 0));
			else if(!blockList.contains(block.getRelative(0, 0, 1)) && SUPPORT_NEED.contains(block.getRelative(0, 0, 1).getType()))
				blockList.add(block.getRelative(0, 0, 1));
			else if(!blockList.contains(block.getRelative(0, 0, -1)) && SUPPORT_NEED.contains(block.getRelative(0, 0, -1).getType()))
				blockList.add(block.getRelative(0, 0, -1));
		}*/

		shiftBlocks(blockList);
		int powerRadius = 5;

		//Scan the nearby radius to add blocks that can be destroyed.
		//TODO update scan to accurately destroy blocks
		for (int x = powerRadius * -1; x <= powerRadius; x++)
			for (int y = powerRadius * -1; y <= powerRadius; y++)
				for (int z = powerRadius * -1; z <= powerRadius; z++) {
					Block block = location.getBlock().getRelative(x, y, z);
					if(MaterialUtil.isIndestructible(block.getType()) && !settings.getBlockSettings().get(new RegenBlockData(block.getType())).doPreventDamage()) {
						blockList.add(block);
					}
				}
		ExplosionTriggerEvent e = new ExplosionTriggerEvent(this);
		if(!settings.getAllowDamage(DamageCategory.BLOCK)) {
			e.setCancelled(true);
		}
		Bukkit.getPluginManager().callEvent(e);
		if(e.isCancelled())
			return;
		startDelay();
	}

	private void shiftBlocks(List<Block> list) {
		List<Block> shift = new ArrayList<>();
		for(Block block : new ArrayList<>(list)) {
			for(Material material : getSupportNeededMaterials()) {
				if(block.getType() == material) {
					shift.add(block);
					list.remove(block);
				}
			}
		}
		list.addAll(0, shift);
	}

	public static List<Material> getSupportNeededMaterials() {
		return SUPPORT_NEED;
	}

	private boolean hasGravityBlockNearby(BlockState block) {
		if(block.getBlock().getRelative(1, 0, 0).getState().getType().hasGravity())
			return true;
		else if(block.getBlock().getRelative(-1, 0, 0).getState().getType().hasGravity())
			return true;
		else if(block.getBlock().getRelative(0, 1, 0).getState().getType().hasGravity())
			return true;
		else if(block.getBlock().getRelative(0, -1, 0).getState().getType().hasGravity())
			return true;
		else if(block.getBlock().getRelative(0, 0, 1).getState().getType().hasGravity())
			return true;
		else return block.getBlock().getRelative(0, 0, -1).getState().getType().hasGravity();
	}

	private void damageBlock(RegenBlock regenBlock, BlockSettingsData bs, Block block) {
		BlockState state = block.getState();
		if(state instanceof Container) {
			if(bs.doSaveItems()) {
				Inventory inventory;
				if(block.getType() == Material.CHEST) {
					inventory = ((Chest)state).getBlockInventory();
				} else
					inventory = ((Container) state).getInventory();
				inventory.clear();
			} else {
				((Container) regenBlock.getState()).getSnapshotInventory().clear();
				state.update(true);
			}
		}
		if(!bs.doPreventDamage()) {
			if (BLOCK_MAP.containsKey(block.getLocation())) {
				RegenBlock b = BLOCK_MAP.get(block.getLocation());
				regenBlock.setDurability(b.getDurability() - blockDamage);
				BLOCK_MAP.remove(block.getLocation());
			} else {
				regenBlock.setDurability(regenBlock.getDurability() - blockDamage);
			}
			if (regenBlock.getDurability() <= 0.0d) {
				if (bs.doRegen()) {
					if (regenBlock.getType() != Material.TNT)
						addBlock(regenBlock);
					if ((MaterialUtil.isBedBlock(block.getState().getType()) || getSupportNeededMaterials().contains(block.getState().getType()) || block.getState().getBlockData() instanceof Bisected) || (hasGravityBlockNearby(block.getState()) || !bs.isBlockUpdate()) || (block.getState().getType().name().contains("SHULKER_BOX") || block.getState().getType() == Material.BEACON || (block.getState().getType().name().contains("_HEAD") && block.getState().getType() != Material.PISTON_HEAD) || block.getState().getType().name().contains("_SKULL"))) {
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
				BLOCK_MAP.put(block.getLocation(), regenBlock);
			}
		} else {
			blockList.remove(block);
			if(bs.doReplace())
				regenBlock.setBlock();
		}
	}
	private void startDelay() {
		if(blockList == null || blockList.isEmpty())
			return;

		List<Block> list = new ArrayList<>(blockList);
		HashMap<Integer, List<Block>> map = new HashMap<>();
		GenerateDirection direction = settings.getRegenerateDirection();
		switch(direction) {
			case UP:
				list.sort(Comparator.comparingInt((Block o) -> o.getLocation().getBlockY()));
				break;
			case DOWN:
				list.sort(Comparator.comparingInt((Block o) -> o.getLocation().getBlockY()).reversed());
				break;
			case EAST:
				list.sort(Comparator.comparingInt((Block o) -> o.getLocation().getBlockX()));
				break;
			case WEST:
				list.sort(Comparator.comparingInt((Block o) -> o.getLocation().getBlockX()).reversed());
				break;
			case SOUTH:
				list.sort(Comparator.comparingInt((Block o) -> o.getLocation().getBlockZ()));
				break;
			case NORTH:
				list.sort(Comparator.comparingInt((Block o) -> o.getLocation().getBlockZ()).reversed());
				break;
			case RANDOM_UP:
			case RANDOM_DOWN:
			case RANDOM_EAST:
			case RANDOM_WEST:
			case RANDOM_SOUTH:
			case RANDOM_NORTH: {
				list.forEach(block -> {
					List<Block> l;
					int bi;
					if(direction == RANDOM_UP || direction == RANDOM_DOWN) {
						bi = block.getLocation().getBlockY();
					} else if(direction == RANDOM_EAST || direction == RANDOM_WEST)
						bi = block.getLocation().getBlockX();
					else
						bi = block.getLocation().getBlockZ();
					map.putIfAbsent(bi, new ArrayList<>());
					l = map.get(bi);
					l.add(block);
				});
				list.clear();
				List<Integer> ls = new ArrayList<>(map.keySet());
				if(direction == RANDOM_UP)
					ls.sort(Comparator.comparingInt((Integer value) -> value));
				else
					ls.sort(Comparator.comparingInt((Integer value) -> value).reversed());
				for(int i : ls)
					/*Collections.shuffle(map.get(i));*/
					list.addAll(map.get(i));
				break;
			}
		}
		blockList.clear();
		blockList.addAll(list);

		if(settings.getAllowRegen()) {
			for (Block block : new ArrayList<>(blockList)) {
				if(hasBlock(block.getLocation()))
					continue;
				if(block.getType() == Material.PISTON_HEAD) {
					blockList.remove(block);
					continue;
				}
				if(block.getBlockData() instanceof Piston) {
					Piston piston = (Piston) block.getBlockData();
					piston.setExtended(false);
					block.setBlockData(piston, false);
				}
				BlockSettingsData bs = settings.getBlockSettings().get(new RegenBlockData(block));
				RegenBlock regenBlock = new RegenBlock(block, bs.getReplaceWith(), bs.getRegenDelay(), bs.getDurability());
				{
					Block part = null;
					if(block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
						org.bukkit.block.data.type.Chest chest = (org.bukkit.block.data.type.Chest)block.getBlockData();
						BlockFace face = chest.getFacing();
						org.bukkit.block.data.type.Chest.Type type = chest.getType();
						if((face == BlockFace.NORTH && type == org.bukkit.block.data.type.Chest.Type.LEFT) || (face == BlockFace.SOUTH && type == org.bukkit.block.data.type.Chest.Type.RIGHT)) {
							part = block.getRelative(1, 0, 0);
						} else if((face == BlockFace.NORTH && type == org.bukkit.block.data.type.Chest.Type.RIGHT) || (face == BlockFace.SOUTH && type == org.bukkit.block.data.type.Chest.Type.LEFT)) {
							part = block.getRelative(-1, 0, 0);
						} else if((face == BlockFace.EAST && type == org.bukkit.block.data.type.Chest.Type.LEFT) || (face == BlockFace.WEST && type == org.bukkit.block.data.type.Chest.Type.RIGHT)) {
							part = block.getRelative(0, 0, 1);
						} else if((face == BlockFace.EAST && type == org.bukkit.block.data.type.Chest.Type.RIGHT) || (face == BlockFace.WEST && type == org.bukkit.block.data.type.Chest.Type.LEFT)) {
							part = block.getRelative(0, 0, -1);
						}
					} else if(block.getType().name().endsWith("_BED")) {
						org.bukkit.block.data.type.Bed bed = (org.bukkit.block.data.type.Bed) block.getBlockData();
						BlockFace face = bed.getFacing();
						org.bukkit.block.data.type.Bed.Part type = bed.getPart();
						if((face == BlockFace.NORTH && type == org.bukkit.block.data.type.Bed.Part.HEAD) || (face == BlockFace.SOUTH && type == org.bukkit.block.data.type.Bed.Part.FOOT)) {
							part = block.getRelative(0, 0, 1);
						} else if((face == BlockFace.NORTH && type == org.bukkit.block.data.type.Bed.Part.FOOT) || (face == BlockFace.SOUTH && type == org.bukkit.block.data.type.Bed.Part.HEAD)) {
							part = block.getRelative(0, 0, -1);
						} else if((face == BlockFace.EAST && type == org.bukkit.block.data.type.Bed.Part.HEAD) || (face == BlockFace.WEST && type == org.bukkit.block.data.type.Bed.Part.FOOT)) {
							part = block.getRelative(-1, 0, 0);
						} else {
							part = block.getRelative(1, 0, 0);
						}
					} else if(block.getBlockData() instanceof Bisected) {
						Bisected bi = (Bisected)block.getBlockData();
						Bisected.Half type = bi.getHalf();
						if(type == Bisected.Half.BOTTOM)
							part = block.getRelative(0, 1, 0);
						else
							part = block.getRelative(0, -1, 0);
					}
					if(part != null) {
						BlockSettingsData bsPart = settings.getBlockSettings().get(new RegenBlockData(part));
						RegenBlock regenBlockPart = new RegenBlock(part, bsPart.getReplaceWith(), bsPart.getRegenDelay(), bsPart.getDurability());
						regenBlock.setPart(regenBlockPart);
						regenBlockPart.setPart(regenBlock);
						damageBlock(regenBlockPart, bsPart, part);
					}
				}
				damageBlock(regenBlock, bs, block);
			}

		}
		ACTIVE_EXPLOSIONS.add(this);
	}

	/**
	 * Get the settings that is used in this explosion
	 *
	 * @return The explosion's settings
	 */
	public ExplosionSettings getSettings() {
		return settings;
	}

	/**
	 * Sets the settings for this explosion
	 *
	 * @param settings The new settings to be used
	 */
	public void setSettings(ExplosionSettings settings) {
		this.settings = settings;
	}

	/**
	 * Gets a list of adjacent blocks that requires support from the target block
	 *
	 * @param block The block used to check nearby blocks
	 * @return List of adjacent blocks that were added
	 */
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
		}
		return list;
	}

	/**
	 * Gets the location of this explosion
	 *
	 * @return The explosion's location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Add additional blocks to the regen list
	 *
	 * @param blocks The list of blocks to add
	 */
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

	/**
	 * The block to add to the regen list
	 *
	 * @param block The block to add
	 */
	public void addBlock(RegenBlock block) {
		blocks.add(block);
	}

	/**
	 * Returns a list of blocks to be regenerated
	 *
	 * @return List of blocks to regenerate
	 */
	public List<RegenBlock> getBlocks() {
		return blocks;
	}

	/**
	 * Removes the block from the regen list
	 *
	 * @param location The location of the block to remove
	 */
	public void removeBlock(Location location) {
		for(RegenBlock block : new ArrayList<>(getBlocks())) {
			if(block.getLocation().equals(location))
				blocks.remove(block);
		}
	}

	public boolean hasBlock(Location location) {
		for(RegenBlock block : getBlocks()) {
			if (block.getLocation().equals(location))
				return true;
		}
		return false;
	}

	public RegenBlock getBlock(Location location) {
		for(RegenBlock block : getBlocks()) {
			if(block.getLocation().equals(location))
				return block;
		}
		return null;
	}

	/**
	 * Regenerates the block in this explosion
	 *
	 * @param block The block to regenerate
	 */
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

		if(settings.getBlockSettings().get(new RegenBlockData(state.getBlockData())).doReplace()) {
			state.setBlockData((BlockData) block.getRegenData().getBlockData());
		}

		BlockState bState = block.getBlock().getState();
		if(settings.getRegenForceBlock()) {
			block.getBlock().breakNaturally();
		}
		if(hasGravityBlockNearby(state) || (state.getType().hasGravity() && !state.getBlock().getRelative(0, -1, 0).getType().isSolid()) || !settings.getBlockSettings().get(block.getRegenData()).isBlockUpdate())
			state.update(true, false);
		else
			state.update(true);
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
		if(block.getPart() != null) {
			block.getPart().setPart(null);
			regenerate(block.getPart());
		}
	}

	/**
	 * Regenerates all blocks in this explosion.
	 */
	public void regenerateAll() {
		for(RegenBlock block : new ArrayList<>(blocks))
			regenerate(block);
	}

	/**
	 * Gets the remaining ticks before regen phase starts
	 *
	 * @return The remaining ticks
	 */
	public long getRegenTick() {
		return regenTick;
	}

	/**
	 * Sets the remaining ticks before regen phase starts
	 *
	 * @param regenTick The remaining ticks to be set
	 */
	public void setRegenTick(long regenTick) {
		this.regenTick = regenTick;
	}

	public RegenBlock getPreviousBlock() {
		return previousBlock;
	}
	/**
	 * Gets a list of blocks next to be regenerated
	 *
	 * @return List of blocks to be regenerated next
	 */
	public Set<RegenBlock> getQueueBlocks() {
		Set<RegenBlock> list = new HashSet<>();
		int queue = settings.getMaxBlockRegenQueue();
		if(queue <= 0)
			queue = 1;
		List<RegenBlock> blockList = getBlocks(); // All blocks in list
		List<RegenBlock> blockList2 = new ArrayList<>(getBlocks()); // All blocks that don't need support
		for(RegenBlock block : new ArrayList<>(blockList2)) {
			for(Material material : Explosion.getSupportNeededMaterials()) {
				if(block.getType() == material)
					blockList2.remove(block);
			}
		}
		if(!blockList2.isEmpty())
			blockList = blockList2;
		for (int i = 0; i < queue; i++) {
			if(blockList.size() > i) {
				list.add(blockList.get(i));
			}
		}
		return list;
	}

	/**
	 * Removes this explosion, regenerating all blocks damaged
	 */
	public void remove() {
		remove(true);
	}

	/**
	 * Removes this explosion, and rather blocks should regenerate or not
	 *
	 * @param regenerate Should the destroyed blocks regenerate
	 */
	public void remove(boolean regenerate) {
		if(regenerate)
			regenerateAll();
		ACTIVE_EXPLOSIONS.remove(this);
	}

	public Object getSource() {
		return source;
	}

	public double getBlockDamage() {
		return blockDamage;
	}

	public void setBlockDamage(double blockDamage) {
		this.blockDamage = blockDamage;
	}


	/**
	 * Gets a list of currently active explosions
	 *
	 * @return List of currently active explosions
	 */
	public static Collection<Explosion> getActiveExplosions() {
		return ACTIVE_EXPLOSIONS.stream().collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));
	}

	/**
	 * Creates an explosion
	 * @param location The location to create the explosion
	 * @param settings The settings the explosion will use
	 * @param force Force the explosion
	 * @param power The power of the explosion
	 * @param setFire Create fire from the explosion
	 */
	public static void createExplosion(Location location, ExplosionSettings settings, boolean force, float power, boolean setFire) {
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
}
