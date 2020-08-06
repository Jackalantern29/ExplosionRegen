package com.jackalantern29.explosionregen.api;

import com.cryptomorin.xseries.XMaterial;

public class BlockSettingsData {	
	private final XMaterial material;
	
	private XMaterial replaceWith = XMaterial.AIR;
	
	private int chance = 30;
	private int maxRegenHeight = 3;
	private double durability = 1d;
	private long regenDelay = 0;
	
	private boolean preventDamage = false;
	private boolean regen = true;
	private boolean saveItems = false;
	private boolean replace = false;
	
	public BlockSettingsData(XMaterial material) {
		this.material = material;
	}
	
	public XMaterial getMaterial() {
		return material;
	}
	
	public boolean doPreventDamage() {
		return preventDamage;
	}
	
	public void setPreventDamage(boolean value) {
		preventDamage = value;
	}
	
	public boolean doRegen() {
		return regen;
	}
	
	public void setRegen(boolean value) {
		regen = value;
	}
	
	public boolean doSaveItems() {
		return saveItems;
	}
	
	public void setSaveItems(boolean value) {
		saveItems = value;
	}
	
	public int getMaxRegenHeight() {
		return maxRegenHeight;
	}
	
	public void setMaxRegenHeight(int value) {
		maxRegenHeight = value;
	}
	
	public boolean doReplace() {
		return replace;
	}
	
	public void setReplace(boolean value) {
		replace = value;
	}
	
	public XMaterial getReplaceWith() {
		return replaceWith;
	}
	
	public void setReplaceWith(XMaterial material) {
		replaceWith = material;
	}
	
	public int getDropChance() {
		return chance;
	}
	
	public void setDropChance(int value) {
		chance = value;
	}
	
	public double getDurability() {
		return durability;
	}
	
	public void setDurability(double value) {
		durability = value;
	}
	
	public long getRegenDelay() {
		return regenDelay;
	}
	
	public void setRegenDelay(long delay) {
		this.regenDelay = delay;
	}
}
