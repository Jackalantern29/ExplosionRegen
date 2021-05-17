package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import com.jackalantern29.explosionregen.BukkitMethods;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.jackalantern29.explosionregen.ExplosionRegen;

public class ExplosionRegenSettings {
	
	private static boolean setup = false;
	private final ExplosionRegen plugin = ExplosionRegen.getInstance();
	private final File configFile = new File(plugin.getDataFolder(), "config.yml");
	private YamlConfiguration config;
	private final File explosionsFolder = new File(plugin.getDataFolder(), "explosions");
	private final File profileFolder = new File(plugin.getDataFolder(), "profiles");
	private final File blocksFolder = new File(plugin.getDataFolder(), "blocks");

	private boolean enablePlugin;
	private boolean enableProfile;
	private boolean griefPreventionAllowExplosionRegen;
	private List<String> allowWorlds = new ArrayList<>();
	public ExplosionRegenSettings() {
		if(!setup) {
			setup();
			setup = true;
		}
	}
	private void setup() {
		config = YamlConfiguration.loadConfiguration(configFile);
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

		List<String> worldList = new ArrayList<>();
		for(World world : Bukkit.getWorlds())
			worldList.add(world.getName());
		map.put("allow_worlds", worldList);

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
		griefPreventionAllowExplosionRegen = config.getBoolean("options.grief-prevention-plugin.allow-explosion-regen", true);
		allowWorlds.addAll(config.getStringList("allow_worlds"));

		for(File file : Objects.requireNonNull(blocksFolder.listFiles())) {
			BlockSettings.registerSettings(file);
		}
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + "default.yml");
		List<File> fileList = new ArrayList<>(Arrays.asList(explosionsFolder.listFiles()));
		if(!fileList.contains(file))
			fileList.add(file);
		for(File f : fileList) {
			try {
				ExplosionSettings settings = ExplosionSettings.registerSettings(f);
				settings.saveAsFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	public List<String> getWorlds() {
		return allowWorlds;
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
		allowWorlds.clear();
		setup();
	}
}
