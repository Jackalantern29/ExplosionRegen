package com.jackalantern29.explosionregen.api;

import com.jackalantern29.flatx.api.FlatBlockData;
import com.jackalantern29.flatx.bukkit.BukkitAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class RegenBlock {
	private final BlockState block;
	private long regenDelay;
	private FlatBlockData flatData;
	private double durability;
	private RegenBlock part = null;

	public RegenBlock(Block block, long regenDelay, double durability) {
		this(block, BukkitAdapter.adapt(block).getBlockData(), regenDelay, durability);
	}

	public RegenBlock(Block block, FlatBlockData toBlock, long regenDelay, double durability) {
		this.block = block.getState();
		this.regenDelay = regenDelay;
		this.flatData = toBlock;
		this.durability = durability;
	}
	
	public Block getBlock() {
		return block.getBlock();
	}
	
	public BlockState getState() {
		return block;
	}
	
	public Location getLocation() {
		return block.getLocation();
	}
	
	public long getRegenDelay() {
		return regenDelay;
	}
	
	public void setRegenDelay(long delay) {
		this.regenDelay = delay;
	}
	
	public Material getType() {
		return block.getType();
	}
	
	public FlatBlockData getFlatData() {
		return flatData;
	}
	
	public void setFlatData(FlatBlockData flatData) {
		this.flatData = flatData;
	}

	public void setBlock() {
		block.setType(BukkitAdapter.asBukkitMaterial(flatData.getMaterial()));
		BukkitAdapter.adapt(block).setBlockData(flatData);
		block.update(true);
/*		if (ExplosionRegen.getInstance().getCoreProtect() != null) {
			if (UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
				ExplosionRegen.getInstance().getCoreProtect().logRemoval("#explosionregen", block.getLocation(), block.getType(), BukkitAdapter.asBukkitBlockData(BukkitAdapter.adapt(block).getBlockData()));
			else
				ExplosionRegen.getInstance().getCoreProtect().logRemoval("#explosionregen", block.getLocation(), block.getType(), block.getData());
		}*/
	}

	public double getDurability() {
		return durability;
	}
	
	public void setDurability(double durability) {
		this.durability = durability;
	}

	public RegenBlock getPart() {
		return part;
	}

	public void setPart(RegenBlock part) {
		this.part = part;
	}
}
