package com.jackalantern29.explosionregen.api;


import com.jackalantern29.flatx.api.FlatBlockData;
import com.jackalantern29.flatx.api.enums.FlatMaterial;
import com.jackalantern29.flatx.bukkit.FlatBukkit;

public class BlockSettingsData {
	private final FlatBlockData flatData;

	private FlatBlockData replaceWith = FlatBukkit.createBlockData(FlatMaterial.AIR);

	private int chance = 30;
	private int maxRegenHeight = 3;
	private double durability = 1d;
	private long regenDelay = 0;
	
	private boolean preventDamage = false;
	private boolean regen = true;
	private boolean saveItems = false;
	private boolean replace = false;
	private boolean blockUpdate = true;

	public BlockSettingsData(FlatBlockData flatData) {
		this.flatData = flatData;
	}

	public FlatBlockData getFlatData() {
		return flatData;
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
	
	public FlatBlockData getReplaceWith() {
		return replaceWith;
	}

	public FlatBlockData getResult() {
		if(doReplace())
			return getReplaceWith();
		else
			return getFlatData();
	}

	public boolean isBlockUpdate() {
		return blockUpdate;
	}

	public void setBlockUpdate(boolean blockUpdate) {
		this.blockUpdate = blockUpdate;
	}

	public void setReplaceWith(FlatBlockData replaceWith) {
		this.replaceWith = replaceWith;
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
