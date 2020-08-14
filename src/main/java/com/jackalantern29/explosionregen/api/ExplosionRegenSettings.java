package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;

import com.jackalantern29.explosionregen.api.enums.UpdateType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.jackalantern29.explosionregen.ExplosionRegen;

public class ExplosionRegenSettings {
	
	private static boolean setup = false;
	private final ExplosionRegen plugin = ExplosionRegen.getInstance();
	private final File configFile = new File(plugin.getDataFolder(), "config.yml");
	private final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	private final File explosionsFolder = new File(plugin.getDataFolder(), "explosions");
	private final File profileFolder = new File(plugin.getDataFolder(), "profiles");
	private final File blocksFolder = new File(plugin.getDataFolder(), "blocks");
	private final File particleVanillaFolder = new File(plugin.getDataFolder() + File.separator + "particles", "vanilla");
	private final File particlePresetFolder = new File(plugin.getDataFolder() + File.separator + "particles", "preset");

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
		if(!particleVanillaFolder.exists()) {
			particleVanillaFolder.mkdirs();
		}
		if(!particlePresetFolder.exists()) {
			particlePresetFolder.mkdirs();
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
		map.put("options.enablePlugin", true);
		map.put("options.gravity.allowShift", false);
		map.put("options.gravity.limit", 5);
		map.put("options.playerSettings.enable", false);
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
		for(File files : Objects.requireNonNull(blocksFolder.listFiles())) {
			if(files.getName().endsWith(".yml")) {
				YamlConfiguration bc = YamlConfiguration.loadConfiguration(files);
				//"default.yml"
				//smiles 6 
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
					Material mat = key.equalsIgnoreCase("default") ? null : Material.valueOf(key.toUpperCase());
					ConfigurationSection section = bc.getConfigurationSection(key);
					BlockSettingsData bd = new BlockSettingsData(mat);
					bd.setPreventDamage(section.getBoolean("prevent-damage"));
					bd.setRegen(section.getBoolean("regen"));
					bd.setSaveItems(section.getBoolean("save-items"));
					bd.setMaxRegenHeight(section.getInt("max-regen-height"));
					bd.setReplace(section.getBoolean("replace.do-replace"));
					bd.setReplaceWith(Material.valueOf(section.getString("replace.replace-with").toUpperCase()));
					bd.setDropChance(section.getInt("chance"));
					bd.setDurability(section.getDouble("durability"));
					bs.add(bd);
				}
			}
		}
		if(UpdateType.isPostUpdate(UpdateType.COMBAT_UPDATE)) {
			for(Particle particle : Particle.values()) {
				ParticleData.getVanillaSettings(particle);
			}
		} else {
			try {
				Class<?> enumPart = Class.forName("net.minecraft.server." + UpdateType.getNMSVersion() + ".EnumParticle");
				for(Object particle : (Object[])MethodHandles.lookup().findStatic(enumPart, "values", MethodType.methodType(Object[].class)).invoke()) {
					Bukkit.getConsoleSender().sendMessage("§a" + particle.toString());
				}

			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (Throwable throwable) {
				throwable.printStackTrace();
			}
		}
		for(File f : particlePresetFolder.listFiles()) {
			ParticleSettings.load(f);
		}
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + "default.yml");
		try {
			ExplosionSettings settings = ExplosionSettings.registerSettings(file);
			settings.saveAsFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(getAllowPlayerSettings()) {
			if(!profileFolder.exists())
				profileFolder.mkdir();
			for(Player player : Bukkit.getOnlinePlayers())
				ProfileSettings.get(player.getUniqueId());
		}
	}
	public boolean doEnablePlugin() {
		return config.getBoolean("options.enablePlugin");
	}
	
	public boolean allowShiftingGravity() {
		return config.getBoolean("options.gravity.allowShift");
	}
	
	public int getGravityShiftLimit() {
		return config.getInt("options.gravity.limit");
	}
	
	public boolean getAllowPlayerSettings() {
		return config.getBoolean("options.playerSettings.enable");
	}
	
	public String getNoPermCmdChat() {
		return config.getString("chat.noPermCmd").replace("&", "§");
	}
	
	public ProfileSettings getProfileSettings(UUID uuid) {
		return ProfileSettings.get(uuid);
	}
	
	public void reload() {
		for(Explosion explosions : ExplosionRegen.getExplosionMap().getExplosions())
			explosions.regenerateAll();
		for(ParticleSettings settings : new ArrayList<>(ParticleSettings.getParticleSettings())) {
			settings.saveToFile();
			ParticleSettings.removeSettings(settings.getName());
		}
		for(ExplosionSettings settings : new ArrayList<>(ExplosionSettings.getRegisteredSettings())) {
			settings.saveAsFile();
			ExplosionSettings.removeSettings(settings.getName());
		}
		setup();
		
	}
}
