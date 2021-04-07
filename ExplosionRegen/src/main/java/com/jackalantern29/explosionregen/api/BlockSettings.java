package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;

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
		this.settings.put(string, settings);
	}
	public BlockSettingsData get(RegenBlockData regenData) {
		if(regenData == null)
			return this.settings.get("");
		if(this.settings.containsKey(regenData.toString())) {
			return this.settings.get(regenData.toString());
		} else {
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

	public Collection<BlockSettingsData> getBlockDatas() {
		List<BlockSettingsData> list = new ArrayList<>(settings.values());
		list.sort((o1, o2) -> {
			CompareToBuilder builder = new CompareToBuilder();
			builder.append(o1.getRegenData() != null ? o1.getRegenData().toString() : "default", o2.getRegenData() != null ? o2.getRegenData().toString() : "default");
			return builder.toComparison();
		});
		return list;
	}

	public void saveAsFile() {
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "blocks" + File.separator + name + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		for(BlockSettingsData block : getBlockDatas()) {
			String type;
			if(block.getRegenData() == null) {
				type = "default";
			} else {
				type = block.getRegenData().toString();
			}
			map.put(type + ".prevent-damage", block.doPreventDamage());
			map.put(type + ".regen", block.doRegen());
			map.put(type + ".save-items", block.doSaveItems());
			map.put(type + ".max-regen-height", block.getMaxRegenHeight());
			map.put(type + ".replace.do-replace", block.doReplace());
			map.put(type + ".replace.replace-with", block.getReplaceWith().toString());
			map.put(type + ".chance", block.getDropChance());
			map.put(type + ".durability", block.getDurability());
			map.put(type + ".regen-delay", block.getRegenDelay());
		}
		boolean doSave = false;
		for(Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			Object valueKey = config.get(key);

			if(value instanceof Float)
				value = ((Float) value).doubleValue();
			else if(value instanceof Long)
				value = ((Long) value).intValue();

			if(valueKey instanceof Float)
				valueKey = ((Float) valueKey).doubleValue();
			else if(valueKey instanceof Long)
				valueKey = ((Long) valueKey).intValue();
			if(!config.contains(key) || !valueKey.equals(value)) {
				config.set(key, value); doSave = true;
			}
		}
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(!config.contains(key)) {
				config.set(key, value); doSave = true;
			}
			map.remove(key);
		}
		if(doSave) {
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	public static Collection<BlockSettings> getBlockSettings() {
		return MAP.values();
	}
	public static void removeSettings(String name) {
		if(getSettings(name) != null) {
			MAP.remove(name);
			Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Removed Block Settings '" + name + "'.");
		}
	}
}
