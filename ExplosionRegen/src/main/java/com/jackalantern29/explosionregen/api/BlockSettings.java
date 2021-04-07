package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
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
		if(doSave) {
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
				settings = MAP.get(name);
			for(String key : bc.getKeys(false)) {
				saveMap.put(key + ".prevent-damage", false);
				saveMap.put(key + ".regen", true);
				saveMap.put(key + ".save-items", true);
				saveMap.put(key + ".replace.do-replace", false);
				saveMap.put(key + ".replace.replace-with", Material.AIR.name().toLowerCase());
				saveMap.put(key + ".chance", 30);
				saveMap.put(key + ".durability", 1.0d);
				saveMap.put(key + ".regen-delay", 0);
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
				RegenBlockData regenData = null;
				if(key.equalsIgnoreCase("default"))
					regenData = null;
				else {
					if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
						regenData = new RegenBlockData(Bukkit.createBlockData(key));
					else {
						//Legacy Support Disabled
/*							String mat = key.contains(",") ? key.split(",", 2)[0] : key;
							byte data = key.contains(",") ? Byte.parseByte(key.split(",", 2)[1]) : 0;
							int id;
							if(NumberUtils.isNumber(mat))
								id = Integer.parseInt(mat);
							else
								id = Material.getMaterial(mat.toUpperCase()).getId();
							regenData = new RegenBlockData(Material.getMaterial(id), data);*/
					}
				}
				ConfigurationSection section = bc.getConfigurationSection(key);
				RegenBlockData replaceData = null;
				{
					String mat = section.getString("replace.replace-with");
					if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
						replaceData = new RegenBlockData(Bukkit.createBlockData(mat));
						//replaceData = new RegenBlockData(Material.valueOf(mat.toUpperCase()));
					else {
						//Legacy Support Disabled
/*							String matt = mat.contains(",") ? mat.split(",")[0] : mat;
							byte data = mat.contains(",") ? Byte.parseByte(mat.split(",")[1]) : 0;
							int id;
							if(NumberUtils.isNumber(matt))
								id = Integer.parseInt(matt);
							else
								id = Material.getMaterial(matt.toUpperCase()).getId();
							replaceData = new RegenBlockData(Material.getMaterial(id), data);*/
					}
				}
				BlockSettingsData bd = new BlockSettingsData(regenData);
				bd.setPreventDamage(section.getBoolean("prevent-damage"));
				bd.setRegen(section.getBoolean("regen"));
				bd.setSaveItems(section.getBoolean("save-items"));
				bd.setMaxRegenHeight(section.getInt("max-regen-height"));
				bd.setReplace(section.getBoolean("replace.do-replace"));
				bd.setReplaceWith(replaceData);
				bd.setDropChance(section.getInt("chance"));
				bd.setDurability(section.getDouble("durability"));
				bd.setRegenDelay(section.getLong("regen-delay"));
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
