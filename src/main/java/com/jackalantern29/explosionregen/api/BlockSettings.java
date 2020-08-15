package com.jackalantern29.explosionregen.api;

import java.util.HashMap;
import java.util.Map;

import com.jackalantern29.explosionregen.MaterialUtil;
import org.bukkit.Bukkit;

import org.bukkit.Material;

public class BlockSettings {
	private static final Map<String, BlockSettings> MAP = new HashMap<>();
	private final String name;
	private final Map<Material, BlockSettingsData> settings = new HashMap<>();
	
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
	public BlockSettingsData get(Material material) {
		if(this.settings.containsKey(material))
			return this.settings.get(material);
		else {
			BlockSettingsData to = new BlockSettingsData(material);
			BlockSettingsData from = this.settings.get(null);
			if(material.equals(MaterialUtil.getMaterial("ENDER_CHEST")) || material.equals(MaterialUtil.getMaterial("OBSIDIAN")) || material.equals(MaterialUtil.getMaterial("ENCHANTMENT_TABLE")) || material.equals(MaterialUtil.getMaterial("ANVIL")) || material.equals(MaterialUtil.getMaterial("STRUCTURE_BLOCK")) || material.equals(MaterialUtil.getMaterial("END_PORTAL_FRAME")) || material.equals(MaterialUtil.getMaterial("END_PORTAL")) || material.equals(MaterialUtil.getMaterial("END_GATEWAY")) || material.equals(MaterialUtil.getMaterial("COMMAND_BLOCK")) || material.equals(MaterialUtil.getMaterial("CHAIN_COMMAND_BLOCK")) || material.equals(MaterialUtil.getMaterial("REPEATING_COMMAND_BLOCK")) || material.equals(MaterialUtil.getMaterial("BEDROCK")) || material.equals(MaterialUtil.getMaterial("BARRIER")))
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
