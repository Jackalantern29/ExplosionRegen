package com.jackalantern29.explosionregen.api;


import com.jackalantern29.explosionregen.api.enums.Action;
import com.jackalantern29.flatx.api.FlatBlockData;
import com.jackalantern29.flatx.bukkit.FlatBukkit;

public class BlockSettingsData {
	private final FlatBlockData flatData;

	private String replace;

	private int chance = 30;
	private double durability = 1d;
	private long regenDelay = 0;
	
	private boolean preventDamage = false;
	private Action action = Action.REGENERATE;
	private boolean saveData = false;
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
	
	public Action getAction() {
		return action;
	}
	
	public void setAction(Action action) {
		this.action = action;
	}

	public boolean isSaveData() {
		return saveData;
	}

	public void setSaveData(boolean value) {
		saveData = value;
	}
	
	public void setReplace(String replace) {
		this.replace = replace;
	}
	
	public String getReplace() {
		return replace;
	}

	public FlatBlockData getResult() {
		if(replace.equalsIgnoreCase("self"))
			return getFlatData();
		else
			return FlatBukkit.createBlockData(replace);
	}

	public boolean isBlockUpdate() {
		return blockUpdate;
	}

	public void setBlockUpdate(boolean blockUpdate) {
		this.blockUpdate = blockUpdate;
	}

	
	public int getChance() {
		return chance;
	}
	
	public void setChance(int value) {
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
