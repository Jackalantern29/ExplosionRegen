package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.BukkitMethods;
import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.material.MaterialData;

public class RegenBlock {
	private final BlockState block;
	private long regenDelay;
	private RegenBlockData regenData;
	private Object[] content;
	private double durability;

	public RegenBlock(Block block, long regenDelay, double durability) {
		this(block, new RegenBlockData(block), regenDelay, durability);
	}

	public RegenBlock(Block block, RegenBlockData toBlock, long regenDelay, double durability) {
		this.block = block.getState();
		this.regenDelay = regenDelay;
		this.regenData = toBlock;
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
	
	public RegenBlockData getRegenData() {
		return regenData;
	}
	
	public void setRegenData(RegenBlockData regenData) {
		this.regenData = regenData;
	}

	public void setBlock() {
		block.setType(regenData.getMaterial());
		if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
			BukkitMethods.setBlockData(block, (BlockData) regenData.getBlockData());
		else
			block.setData((MaterialData) regenData.getBlockData());
		block.update(true);
		if(ExplosionRegen.getInstance().getCoreProtect() != null) {
			if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
				ExplosionRegen.getInstance().getCoreProtect().logPlacement("#explosionregen", block.getLocation(), block.getType(), BukkitMethods.getBlockData(block));
			else
				ExplosionRegen.getInstance().getCoreProtect().logRemoval("#explosionregen", block.getLocation(), block.getType(), block.getData().getData());
		}
	}

	public double getDurability() {
		return durability;
	}
	
	public void setDurability(double durability) {
		this.durability = durability;
	}
	
	public void setContents(Object[] contents) {
		this.content = contents;
	}
	
	public Object[] getContents() {
		return content;
	}
}
