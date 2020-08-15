package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.api.enums.GenerateDirection;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Chest;

import java.util.*;

public class Explosion {
	
	private final ExplosionSettings settings;
	private final Location location;
	private final List<RegenBlock> blocks = new ArrayList<>();
	private RegenBlock previousBlock;
	private long regenTick;
	
	public Explosion(ExplosionSettings settings, Location location) {
		this.settings = settings;
		this.location = location;
		this.regenTick = settings.getRegenDelay();
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
		if(block.getType() == (Material.getMaterial("PORTAL") != null ? Material.PORTAL : Material.getMaterial("NETHER_PORTAL"))) {
			if(block.getBlock().getType() == (Material.getMaterial("PORTAL") != null ? Material.PORTAL : Material.getMaterial("NETHER_PORTAL"))) {
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
		removeBlock(block.getLocation());
		previousBlock = block;
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
	
	public List<RegenBlock> getQueueBlocks() {
		return getQueueBlocks(settings.getRegenerateDirections());
	}
	
	public List<RegenBlock> getQueueBlocks(List<GenerateDirection> direction) {
		List<RegenBlock> list = new ArrayList<>();
	//	Player jack = Bukkit.getPlayer("Jack");
		int i = 0;
		for(Iterator<RegenBlock> it = getBlocks(direction).iterator(); it.hasNext(); i++) {
			try {
				list.add(getBlocks(direction).iterator().next());
			} catch(IndexOutOfBoundsException e) {
				break;
			}
			if(i == settings.getMaxBlockRegenQueue()-1)
				break;
		}
		return list;
	}
	public RegenBlock getPreviousBlock() {
		return previousBlock;
	}
}
