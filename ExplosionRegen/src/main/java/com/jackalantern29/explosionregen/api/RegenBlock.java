package com.jackalantern29.explosionregen.api;

import com.jackalantern29.flatx.api.FlatBlockData;
import com.jackalantern29.flatx.bukkit.BukkitAdapter;
import com.jackalantern29.flatx.bukkit.FlatBukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class RegenBlock {
	private final BlockState originalBlock;
	private long regenDelay;
	private FlatBlockData newFlatData;
	private double durability;
	private RegenBlock part = null;

	public RegenBlock(Block block, long regenDelay, double durability) {
		this(block, BukkitAdapter.adapt(block).getBlockData(), regenDelay, durability);
	}

	public RegenBlock(Block originalBlock, FlatBlockData newFlatData, long regenDelay, double durability) {
		this.originalBlock = originalBlock.getState();
		this.regenDelay = regenDelay;
		this.durability = durability;
		this.newFlatData = newFlatData;
	}

	public RegenBlock(Block originalBlock, String replace, long regenDelay, double durability) {
		this.originalBlock = originalBlock.getState();
		this.regenDelay = regenDelay;
		this.durability = durability;
		FlatBlockData flatData;
		if(replace.equalsIgnoreCase("self"))
			flatData = BukkitAdapter.adapt(originalBlock).getBlockData();
		else
			flatData = FlatBukkit.createBlockData(replace);
		this.newFlatData = flatData;
	}
	
	public Block getBlock() {
		return originalBlock.getBlock();
	}
	
	public BlockState getState() {
		return originalBlock;
	}
	
	public Location getLocation() {
		return originalBlock.getLocation();
	}
	
	public long getRegenDelay() {
		return regenDelay;
	}
	
	public void setRegenDelay(long delay) {
		this.regenDelay = delay;
	}
	
	public Material getType() {
		return originalBlock.getType();
	}
	
	public FlatBlockData getNewFlatData() {
		return newFlatData;
	}
	
	public void setNewFlatData(FlatBlockData newFlatData) {
		this.newFlatData = newFlatData;
	}

	public void setBlock() {
		originalBlock.setType(BukkitAdapter.asBukkitMaterial(newFlatData.getMaterial()));
		BukkitAdapter.adapt(originalBlock).setBlockData(newFlatData);
		originalBlock.update(true);
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
