package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.enums.Action;
import com.jackalantern29.flatx.api.FlatBlockData;
import com.jackalantern29.flatx.bukkit.BukkitAdapter;
import com.jackalantern29.flatx.bukkit.FlatBukkit;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class BlockSettings {
	private static final Map<String, BlockSettings> MAP = new HashMap<>();
	private final String name;
	private final Map<String, BlockSettingsData> settings = new HashMap<>();

	public BlockSettings(String name, BlockSettingsData... settings) {
		this.name = name;
		for(BlockSettingsData setting : settings)
			this.settings.put(setting.getFlatData().getAsString(), setting);
	}

	public String getName() {
		return name;
	}
	
	public void add(BlockSettingsData settings) {
		if(settings.getFlatData() == null)
			this.settings.put(null, settings);
		else
			this.settings.put(settings.getFlatData().getAsString(), settings);
	}

	public void remove(String blockData) {
		if(blockData != null && this.settings.containsKey(blockData)) {
			this.settings.remove(blockData);
		}

	}

	public boolean contains(String blockData) {
		return this.settings.containsKey(blockData);
	}

	public BlockSettingsData get(FlatBlockData flatData) {
		if(flatData == null || !this.settings.containsKey(flatData.getAsString())) {
			BlockSettingsData data = this.settings.get(null);
			if(flatData != null && MaterialUtil.isIndestructible(BukkitAdapter.asBukkitMaterial(flatData.getMaterial()))) {
				data = new BlockSettingsData(flatData);
				data.setPreventDamage(true);
			}
			return data;
		} else {
			return this.settings.get(flatData.getAsString());
		}
	}

	public Collection<BlockSettingsData> getBlockDatas() {
		List<BlockSettingsData> list = new ArrayList<>(settings.values());
		list.sort((o1, o2) -> {
			CompareToBuilder builder = new CompareToBuilder();
			builder.append(o1.getFlatData() != null ? o1.getFlatData().getAsString() : "default", o2.getFlatData() != null ? o2.getFlatData().getAsString() : "default");
			return builder.toComparison();
		});
		return list;
	}

	public void saveAsFile() {
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "blocks" + File.separator + name + ".yml");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		Set<String> keys = new HashSet<>(config.getKeys(false));
		for(BlockSettingsData block : getBlockDatas()) {
			String type;
			if(block.getFlatData() == null)
				type = "default";
			else
				type = block.getFlatData().getAsString();
			ConfigurationSection section = config.createSection(type, getDefaultMap());
			section.set("prevent-damage", block.doPreventDamage());
			section.set("action", block.getAction().name().toLowerCase());
			section.set("save-data", block.isSaveData());
			section.set("replace", block.getReplace());
			section.set("chance", block.getChance());
			section.set("durability", block.getDurability());
			section.set("regen-delay", block.getRegenDelay());
			section.set("block-update", block.isBlockUpdate());
			keys.remove(type);
		}
		for(String key : keys)
			config.set(key, null);
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		MAP.put(name, this);
	}

	public static BlockSettings loadFromFile(File file) {
		if(file == null) {
			try {
				throw new FileNotFoundException("Could not find file.");
			} catch (FileNotFoundException ignored) {
				return null;
			}
		}
		BlockSettings settings = new BlockSettings(file.getName().substring(0, file.getName().length()-4));
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

		Set<String> keys = new HashSet<>(config.getKeys(false));

		boolean save = false;
		if(!keys.contains("default")) {
			keys.add("default");
			config.createSection("default", getDefaultMap());
			save = true;
		}

		for(String key : keys) {
			ConfigurationSection section = config.getConfigurationSection(key);
			for(Map.Entry<String, Object> entry : getDefaultMap().entrySet()) {
				if(!section.isSet(entry.getKey())) {
					section.set(entry.getKey(), entry.getValue());
					save = true;
				}
			}
			FlatBlockData flatData;
			if(key.equalsIgnoreCase("default"))
				flatData = null;
			else
				flatData = FlatBukkit.createBlockData(key);
			BlockSettingsData blockData = new BlockSettingsData(flatData);
			blockData.setPreventDamage(section.getBoolean("prevent-damage"));
			blockData.setAction(Action.valueOf(section.getString("action").toUpperCase()));
			blockData.setSaveData(section.getBoolean("save-data"));
			blockData.setReplace(section.getString("replace"));
			blockData.setChance(section.getInt("chance"));
			blockData.setDurability(section.getDouble("durability"));
			blockData.setRegenDelay(section.getLong("regen-delay"));
			blockData.setBlockUpdate(section.getBoolean("block-update"));
			settings.add(blockData);
		}
		if(save) {
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		MAP.put(settings.getName(), settings);
		return settings;
	}

	private static HashMap<String, Object> getDefaultMap() {
		HashMap<String, Object> defaultMap = new HashMap<>();
		defaultMap.put("prevent-damage", false);
		defaultMap.put("action", Action.REGENERATE.name().toLowerCase());
		defaultMap.put("save-data", true);
		defaultMap.put("replace", "self");
		defaultMap.put("chance", 100);
		defaultMap.put("durability", 1.0d);
		defaultMap.put("regen-delay", 0);
		defaultMap.put("block-update", true);
		return defaultMap;
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
