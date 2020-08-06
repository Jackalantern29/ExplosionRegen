package com.jackalantern29.explosionregen.api;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import com.cryptomorin.xseries.XMaterial;

public class BlockSettings {
	private static final Map<String, BlockSettings> MAP = new HashMap<>();
	private final String name;
	private final Map<XMaterial, BlockSettingsData> settings = new HashMap<>();
	
	private BlockSettings(String name, BlockSettingsData... settings) {
		this.name = name;
		for(BlockSettingsData setting : settings)
			this.settings.put(setting.getMaterial(), setting);
	}
	
	public String getName() {
		return name;
	}
	
	public void add(BlockSettingsData settings) {
		this.settings.putIfAbsent(settings.getMaterial(), settings);
	}
	public BlockSettingsData get(XMaterial material) {
		if(this.settings.containsKey(material))
			return this.settings.get(material);
		else {
			BlockSettingsData to = new BlockSettingsData(material);
			BlockSettingsData from = this.settings.get(null);
			to.setDropChance(from.getDropChance());
			to.setDurability(from.getDurability());
			to.setMaxRegenHeight(from.getMaxRegenHeight());
			to.setPreventDamage(from.doPreventDamage());
			to.setRegen(from.doRegen());
			to.setRegenDelay(from.getRegenDelay());
			to.setReplace(from.doReplace());
			to.setReplaceWith(from.getReplaceWith());
			to.setSaveItems(from.doSaveItems());
			this.settings.put(material, to);
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
