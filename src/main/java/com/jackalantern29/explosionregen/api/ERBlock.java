package com.jackalantern29.explosionregen.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

import com.cryptomorin.xseries.XMaterial;

public class ERBlock {
	private final BlockState block;
	private long regenDelay;
	private XMaterial material;
	private Object[] content;
	private double durability;
	
	public ERBlock(BlockState block, long regenDelay, double durability) {
		this.block = block;
		this.regenDelay = regenDelay;
		this.material = XMaterial.matchXMaterial(block.getType());
		this.durability = durability;
	}
	public ERBlock(BlockState block, XMaterial toBlock, long regenDelay, double durability) {
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
	
	public XMaterial getType() {
		return XMaterial.matchXMaterial(block.getType());
	}
	
	public XMaterial getMaterial() {
		return material;
	}
	
	public void setMaterial(XMaterial material) {
		block.setType(material.parseMaterial());
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
