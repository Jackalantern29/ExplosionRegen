package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.flatx.api.FlatBlockData;
import com.jackalantern29.flatx.api.enums.FlatMaterial;
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
			if(block.getFlatData() == null) {
				type = "default";
			} else {
				type = block.getFlatData().getAsString();
			}
			map.put(type + ".prevent-damage", block.doPreventDamage());
			map.put(type + ".regen", block.doRegen());
			map.put(type + ".save-items", block.doSaveItems());
			map.put(type + ".max-regen-height", block.getMaxRegenHeight());
			map.put(type + ".replace.do-replace", block.doReplace());
			map.put(type + ".replace.replace-with", block.getReplaceWith().getAsString());
			map.put(type + ".chance", block.getDropChance());
			map.put(type + ".durability", block.getDurability());
			map.put(type + ".regen-delay", block.getRegenDelay());
			map.put(type + ".block-update", block.isBlockUpdate());
		}
		boolean doSave = false;
		for(Map.Entry<String, Object> entry : new HashSet<>(map.entrySet())) {
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
			map.remove(key);
		}
		for(String key : config.getKeys(false)) {
			if(!key.equals("default") && !settings.containsKey(key)) {
				config.set(key, null); doSave = true;
			}
		}
		if(doSave) {
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static BlockSettings createSettings(String name) {
		if(getSettings(name) != null)
			return getSettings(name);
		BlockSettings settings;
		Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Created Block Settings '" + name + "'.");

		if(!MAP.containsKey(name)) {
			settings = new BlockSettings(name);
			MAP.put(name, settings);
		} else
			return MAP.get(name);

		BlockSettingsData bd = new BlockSettingsData(null);
		settings.add(bd);

		return settings;
	}

	public static BlockSettings registerSettings(File file) {
		BlockSettings settings = null;
		if(file.getName().endsWith(".yml")) {
			YamlConfiguration bc = YamlConfiguration.loadConfiguration(file);
			LinkedHashMap<String, Object> saveMap = new LinkedHashMap<>();

			String name = file.getName().substring(0, file.getName().length()-4);
			if(!MAP.containsKey(name)) {
				settings = new BlockSettings(name);
				MAP.put(name, settings);
			} else
				return MAP.get(name);
			Set<String> keys = new HashSet<>(bc.getKeys(false));
			if(!keys.contains("default"))
				keys.add("default");
			for(String key : bc.getKeys(false)) {
				saveMap.put(key + ".prevent-damage", false);
				saveMap.put(key + ".regen", true);
				saveMap.put(key + ".save-items", true);
				saveMap.put(key + ".replace.do-replace", false);
				saveMap.put(key + ".replace.replace-with", BukkitAdapter.asBukkitMaterial(FlatMaterial.AIR).name().toLowerCase());
				saveMap.put(key + ".chance", 30);
				saveMap.put(key + ".durability", 1.0d);
				saveMap.put(key + ".regen-delay", 0);
				saveMap.put(key + ".block-update", true);
				boolean saveBC = false;
				for(String k : new ArrayList<>(saveMap.keySet())) {
					if(!bc.contains(k)) {
						bc.set(k, saveMap.get(k));
						saveBC = true;
					}
					saveMap.remove(k);
				}
				if(saveBC)
					try {
						bc.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				FlatBlockData flatData;
				if(key.equalsIgnoreCase("default"))
					flatData = null;
				else {
					flatData = FlatBukkit.createBlockData(key);
				}
				ConfigurationSection section = bc.getConfigurationSection(key);
				FlatBlockData replaceData;
				{
					String mat = section.getString("replace.replace-with");
					replaceData = FlatBukkit.createBlockData(mat);
				}
				BlockSettingsData bd = new BlockSettingsData(flatData);
				bd.setPreventDamage(section.getBoolean("prevent-damage"));
				bd.setRegen(section.getBoolean("regen"));
				bd.setSaveItems(section.getBoolean("save-items"));
				bd.setMaxRegenHeight(section.getInt("max-regen-height"));
				bd.setReplace(section.getBoolean("replace.do-replace"));
				bd.setReplaceWith(replaceData);
				bd.setDropChance(section.getInt("chance"));
				bd.setDurability(section.getDouble("durability"));
				bd.setRegenDelay(section.getLong("regen-delay"));
				bd.setBlockUpdate(section.getBoolean("block-update"));
				settings.add(bd);
			}
		}
		return settings;
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
