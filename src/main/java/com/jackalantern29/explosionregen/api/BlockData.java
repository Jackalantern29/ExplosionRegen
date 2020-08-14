package com.jackalantern29.explosionregen.api;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class BlockData {
	private final BlockState block;
	private long regenDelay;
	private Material material;
	private Object[] content;
	private double durability;

	public BlockData(BlockState block, long regenDelay, double durability) {
		this.block = block;
		this.regenDelay = regenDelay;
		this.material = block.getType();
		this.durability = durability;
	}
	public BlockData(BlockState block, Material toBlock, long regenDelay, double durability) {
		this(block, regenDelay, durability);
		this.material = toBlock;
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
	
	public Material getMaterial() {
		return material;
	}
	
	public void setMaterial(Material material) {
		block.setType(material);
		this.material = material;
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
