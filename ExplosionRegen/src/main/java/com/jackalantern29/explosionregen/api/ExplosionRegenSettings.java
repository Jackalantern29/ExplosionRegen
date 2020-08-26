package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

import com.jackalantern29.explosionregen.BukkitMethods;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.jackalantern29.explosionregen.ExplosionRegen;

public class ExplosionRegenSettings {
	
	private static boolean setup = false;
	private final ExplosionRegen plugin = ExplosionRegen.getInstance();
	private final File configFile = new File(plugin.getDataFolder(), "config.yml");
	private final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	private final File explosionsFolder = new File(plugin.getDataFolder(), "explosions");
	private final File profileFolder = new File(plugin.getDataFolder(), "profiles");
	private final File blocksFolder = new File(plugin.getDataFolder(), "blocks");

	private boolean enablePlugin;
	private boolean enableProfile;
	private boolean griefPreventionAllowExplosionRegen;
	public ExplosionRegenSettings() {
		if(!setup) {
			setup();
			setup = true;
		}
	}
	private void setup() {
		if(!plugin.getDataFolder().exists())
			plugin.getDataFolder().mkdirs();
		if(!explosionsFolder.exists()) {
			explosionsFolder.mkdirs();
		}
		if(!blocksFolder.exists()) {
			blocksFolder.mkdirs();
			try {
				Files.copy(plugin.getResource("default.yml"), Paths.get(plugin.getDataFolder() + File.separator + "blocks" + File.separator + "default.yml"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!configFile.exists())
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		boolean doSave = false;
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("options.enable-plugin", true);
		map.put("options.profile-settings.enable", false);
		map.put("options.grief-prevention-plugin.allow-explosion-regen", true);
		map.put("chat.noPermCmd", "&c[ExplosionRegen] You do not have permission to use this command!");
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(!config.contains(key)) {config.set(key, value); doSave = true;}
			map.remove(key);
		}
		if(doSave) {
			try {
				config.save(configFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		enablePlugin = config.getBoolean("options.enable-plugin");
		enableProfile = config.getBoolean("options.profile-settings.enable");
		griefPreventionAllowExplosionRegen = config.getBoolean("options.giref-prevention-plugin.allow-explosion-regen", true);
		for(File files : Objects.requireNonNull(blocksFolder.listFiles())) {
			if(files.getName().endsWith(".yml")) {
				YamlConfiguration bc = YamlConfiguration.loadConfiguration(files);
				LinkedHashMap<String, Object> saveMap = new LinkedHashMap<>();
				BlockSettings bs = BlockSettings.registerBlockSettings(files.getName().substring(0, files.getName().length()-4));
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
							bc.save(files);
						} catch (IOException e) {
							e.printStackTrace();
						}
					RegenBlockData regenData;
					if(key.equalsIgnoreCase("default"))
						regenData = null;
					else {
						if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
							regenData = new RegenBlockData(Material.valueOf(key.toUpperCase()));
						else {
							String mat = key.contains(",") ? key.split(",", 2)[0] : key;
							byte data = key.contains(",") ? Byte.parseByte(key.split(",", 2)[1]) : 0;
							int id;
							if(NumberUtils.isNumber(mat))
								id = Integer.parseInt(mat);
							else
								id = Material.getMaterial(mat.toUpperCase()).getId();
							regenData = new RegenBlockData(Material.getMaterial(id), data);
						}
					}
					ConfigurationSection section = bc.getConfigurationSection(key);
					RegenBlockData replaceData;
					{
						String mat = section.getString("replace.replace-with");
						if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
							replaceData = new RegenBlockData(Material.valueOf(mat.toUpperCase()));
						else {
							String matt = mat.contains(",") ? mat.split(",")[0] : mat;
							byte data = mat.contains(",") ? Byte.parseByte(mat.split(",")[1]) : 0;
							int id;
							if(NumberUtils.isNumber(matt))
								id = Integer.parseInt(matt);
							else
								id = Material.getMaterial(matt.toUpperCase()).getId();
							replaceData = new RegenBlockData(Material.getMaterial(id), data);
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
					bs.add(bd);
				}
			}
		}
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + "default.yml");
		try {
			ExplosionSettings settings = ExplosionSettings.registerSettings(file);
			settings.saveAsFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(getAllowProfileSettings()) {
			if(!profileFolder.exists())
				profileFolder.mkdir();
		}
		try {
			Class.forName(BukkitMethods.class.getName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	public boolean doEnablePlugin() {
		return enablePlugin;
	}
	
	public boolean getAllowProfileSettings() {
		return enableProfile;
	}

	public boolean getGPAllowExplosionRegen() {
		return griefPreventionAllowExplosionRegen;
	}
	public String getNoPermCmdChat() {
		return config.getString("chat.noPermCmd").replace("&", "§");
	}
	
	public ProfileSettings getProfileSettings(UUID uuid) {
		return ProfileSettings.get(uuid);
	}
	
	public void reload() {
		for(Explosion explosions : ExplosionRegen.getActiveExplosions())
			explosions.regenerateAll();
//		for(ParticleSettings settings : new ArrayList<>(ParticleSettings.getParticleSettings())) {
//			if(settings.getName().contains("_") && !settings.getName().split("_")[1].equals("vanilla"))
//				settings.saveToFile();
//			ParticleSettings.removeSettings(settings.getName());
//		}
		for(ExplosionSettings settings : new ArrayList<>(ExplosionSettings.getRegisteredSettings())) {
			settings.saveAsFile();
			ExplosionSettings.removeSettings(settings.getName());
		}
		setup();
	}
}
