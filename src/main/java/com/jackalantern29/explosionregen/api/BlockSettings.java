package com.jackalantern29.explosionregen.api;

import java.util.HashMap;
import java.util.Map;

import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import org.bukkit.Bukkit;

public class BlockSettings {
	private static final Map<String, BlockSettings> MAP = new HashMap<>();
	private final String name;
	private final Map<String, BlockSettingsData> settings = new HashMap<>();
	
	private BlockSettings(String name, BlockSettingsData... settings) {
		this.name = name;
		for(BlockSettingsData setting : settings)
			this.settings.put(setting.getRegenData().toString(), setting);
	}
	
	public String getName() {
		return name;
	}
	
	public void add(BlockSettingsData settings) {
		String string = settings.getRegenData() != null ? settings.getRegenData().toString() : "";
		this.settings.putIfAbsent(string, settings);
	}
	public BlockSettingsData get(RegenBlockData regenData) {
		if(this.settings.containsKey(regenData.toString()))
			return this.settings.get(regenData.toString());
		else {
			BlockSettingsData to = new BlockSettingsData(regenData);
			BlockSettingsData from = this.settings.get("");
			if(MaterialUtil.isIndestructible(regenData.getMaterial()))
				to.setPreventDamage(true);
			else
				to.setPreventDamage(from.doPreventDamage());
			to.setDropChance(from.getDropChance());
			to.setDurability(from.getDurability());
			to.setMaxRegenHeight(from.getMaxRegenHeight());
			to.setRegen(from.doRegen());
			to.setRegenDelay(from.getRegenDelay());
			to.setReplace(from.doReplace());
			to.setReplaceWith(from.getReplaceWith());
			to.setSaveItems(from.doSaveItems());
			this.settings.put(regenData.toString(), to);
			return to;
		}
	}
	public static BlockSettings registerBlockSettings(String name, BlockSettingsData...settings ) {
		if(!MAP.containsKey(name)) {
			BlockSettings setting = new BlockSettings(name, settings);
			MAP.put(name, setting);
			return setting;
		} else
			return MAP.get(name);
	}
	
	public static BlockSettings getSettings(String name) {
		return MAP.get(name);
	}
	public static void removeSettings(String name) {
		if(getSettings(name) != null) {
			MAP.remove(name);
			Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Removed Block Settings '" + name + "'.");
		}
	}
}
