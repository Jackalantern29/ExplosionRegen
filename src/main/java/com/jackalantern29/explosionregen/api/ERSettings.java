package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XMaterial;
import com.jackalantern29.explosionregen.ExplosionRegen;

import xyz.xenondevs.particle.ParticleEffect;

public class ERSettings {
	
	private static boolean setup = false;
	private final ExplosionRegen plugin = ExplosionRegen.getInstance();
	private final File configFile = new File(plugin.getDataFolder(), "config.yml");
	private final YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
	private final File explosionsFolder = new File(plugin.getDataFolder(), "explosions");
	private final File profileFolder = new File(plugin.getDataFolder(), "profiles");
	private final File blocksFolder = new File(plugin.getDataFolder(), "blocks");
	private final File particleVanillaFolder = new File(plugin.getDataFolder() + File.separator + "particles", "vanilla");
	private final File particlePresetFolder = new File(plugin.getDataFolder() + File.separator + "particles", "preset");

	public ERSettings() {
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
		for(File files : blocksFolder.listFiles()) {
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
					XMaterial mat = key.equalsIgnoreCase("default") ? null : XMaterial.valueOf(key.toUpperCase());
					ConfigurationSection section = bc.getConfigurationSection(key);
					BlockSettingsData bd = new BlockSettingsData(mat);
					bd.setPreventDamage(section.getBoolean("prevent-damage"));
					bd.setRegen(section.getBoolean("regen"));
					bd.setSaveItems(section.getBoolean("save-items"));
					bd.setMaxRegenHeight(section.getInt("max-regen-height"));
					bd.setReplace(section.getBoolean("replace.do-replace"));
					bd.setReplaceWith(XMaterial.valueOf(section.getString("replace.replace-with").toUpperCase()));
					bd.setDropChance(section.getInt("chance"));
					bd.setDurability(section.getDouble("durability"));
					bs.add(bd);
				}
			}
		}
		for(ParticleEffect particles : ParticleEffect.NMS_EFFECTS.keySet()) {
			File file = new File(particleVanillaFolder, particles.name().toLowerCase() + ".yml");
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			if(!file.exists()) {
				try {
					file.createNewFile();
					config.set("amount", 5);
					config.set("offset.x", 1f);
					config.set("offset.y", 1f);
					config.set("offset.z", 1f);
					config.set("speed", 0f);
					config.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			ParticleData particle = ParticleData.getVanillaSettings(particles);
			particle.setAmount(config.getInt("amount"));
			particle.setOffsetX((float)config.getDouble("offset.x"));
			particle.setOffsetY((float)config.getDouble("offset.y"));
			particle.setOffsetZ((float)config.getDouble("offset.z"));
			particle.setSpeed(Float.parseFloat(config.getString("speed")));

			//ParticleSettings.registerSettings(particles.name().toLowerCase(), true, new ParticleData(particles, config.getInt("amount"), Float.parseFloat(config.getString("offsetX")), Float.parseFloat(config.getString("offsetY")), Float.parseFloat(config.getString("offsetX")), Float.parseFloat(config.getString("speed"))));
		}
		ExplosionSettings.registerSettings("default");
		if(getAllowPlayerSettings()) {
			if(!profileFolder.exists())
				profileFolder.mkdir();
			for(Player player : Bukkit.getOnlinePlayers())
				ERProfileSettings.get(player.getUniqueId());
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
	
	public ERProfileSettings getProfileSettings(UUID uuid) {
		return ERProfileSettings.get(uuid);
	}
	
	public void reload() {
		for(ERExplosion explosions : ExplosionRegen.getExplosionMap().getExplosions())
			explosions.regenerateAll();
		for(ExplosionSettings settings : new ArrayList<>(ExplosionSettings.getRegisteredSettings())) {
			settings.save();
			ExplosionSettings.removeSettings(settings.getName());
		}
		setup();
		
	}
}
