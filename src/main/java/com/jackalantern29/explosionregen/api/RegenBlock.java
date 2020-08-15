package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

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
		block.getBlock().setType(regenData.getMaterial());
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
