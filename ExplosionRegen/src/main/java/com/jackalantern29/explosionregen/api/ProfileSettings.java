package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.jackalantern29.explosionregen.api.events.ProfileLoadEvent;
import com.jackalantern29.explosionregen.api.events.ProfileUnloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.jackalantern29.explosionregen.ExplosionRegen;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ProfileSettings {
	private final static List<ProfileSettings> profiles = new ArrayList<>();
	private final Player player;
	private final File file;
	private final YamlConfiguration config;
	private final HashMap<ExplosionSettings, HashMap<String, ProfileSettingsPlugin>> plugins = new HashMap<>();

	static {
		Listener listener = new Listener() {
			@EventHandler
			public void join(PlayerJoinEvent event) {
				Player player = event.getPlayer();
				if(player.isOp()) {
					player.sendMessage("§7[§cExplosionRegen§7] §9This is an early build, so use this with caution. Report any bugs & issues you come across & share any ideas you would like to see.");
				}
				new ProfileSettings(player);
			}
			@EventHandler
			public void quit(PlayerQuitEvent event) {
				get(event.getPlayer()).remove();
			}

			@EventHandler
			public void disable(PluginDisableEvent event) {
				if(event.getPlugin() == ExplosionRegen.getInstance())
					for(ProfileSettings profile : getProfiles())
						profile.save();
			}
		};
		Bukkit.getPluginManager().registerEvents(listener, ExplosionRegen.getInstance());
		for(Player player : Bukkit.getOnlinePlayers()) {
			ProfileSettings profile = new ProfileSettings(player);
			profile.save();
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				for(ProfileSettings settings : ProfileSettings.getProfiles())
					settings.getConfigurableSettings();
			}
		}.runTaskTimer(ExplosionRegen.getInstance(), 0, 20);
	}
	private ProfileSettings(Player player) {
		this.player = player;
		if(ExplosionRegen.getSettings().getAllowProfileSettings()) {
			file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "profiles" + File.separator + player.getUniqueId().toString() + ".yml");
			if(!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			config = YamlConfiguration.loadConfiguration(file);
			ProfileLoadEvent event = new ProfileLoadEvent(this);
			Bukkit.getPluginManager().callEvent(event);
			save();
		} else {
			file = null;
			config = null;
		}
		profiles.add(this);
		getConfigurableSettings();
	}

	public Player getPlayer() {
		return player;
	}

	public List<ExplosionSettings> getConfigurableSettings() {
		List<ExplosionSettings> list = new ArrayList<>();
		for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
			if(player.hasPermission("explosionregen.explosions." + settings.getName().toLowerCase())) {
				list.add(settings);
			}
		}
		return list;
	}

	public boolean hasPermission(ExplosionSettings settings) {
		return getConfigurableSettings().contains(settings);
	}

	public ExplosionSettingsPlugin loadPlugin(ExplosionSettings settings, Object plugin) {
		return loadPlugin(settings, plugin, plugin.getClass().getName());
	}

	public ProfileSettingsPlugin loadPlugin(ExplosionSettings settings, Object plugin, String name) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		name = name.toLowerCase();
		if(config.isConfigurationSection(settings.getName().toLowerCase() + "." + name)) {
			ConfigurationSection section = config.getConfigurationSection(settings.getName().toLowerCase() + "." + name);
			ProfileSettingsPlugin settingsPlugin = new ProfileSettingsPlugin(settings, plugin);
			for(Map.Entry<String, Object> map : section.getValues(true).entrySet()) {
				if (map.getValue() instanceof MemorySection)
					continue;
				settingsPlugin.setOption(map.getKey(), map.getValue());
			}
			HashMap<String, ProfileSettingsPlugin> map = plugins.getOrDefault(settings, new HashMap<>());
			map.put(settingsPlugin.getName().toLowerCase(), settingsPlugin);
			plugins.put(settings, map);
			return settingsPlugin;
		}
		return null;
	}

	public void addPlugin(ProfileSettingsPlugin plugin) {
		ExplosionSettings settings = plugin.getExplosionSettings();
		HashMap<String, ProfileSettingsPlugin> map = plugins.getOrDefault(settings, new HashMap<>());
		map.put(plugin.getName().toLowerCase(), plugin);
		plugins.put(settings, map);
	}

	private void removePlugin(ExplosionSettings settings, String plugin) {
		HashMap<String, ProfileSettingsPlugin> map = plugins.getOrDefault(settings, new HashMap<>());
		map.remove(plugin);
		plugins.put(settings, map);
	}

	public ProfileSettingsPlugin getPlugin(ExplosionSettings settings, String plugin) {
		if(plugins.containsKey(settings))
			return plugins.get(settings).get(plugin.toLowerCase());
		else
			return null;
	}
	private void remove() {
		ProfileUnloadEvent e = new ProfileUnloadEvent(this);
		Bukkit.getPluginManager().callEvent(e);
		save();
		profiles.remove(this);
	}
	public static ProfileSettings get(Player player) {
		if(player == null)
			return null;
		for(ProfileSettings p : profiles) {
			if(p.getPlayer().equals(player))
				return p;
		}
		return null;
	}

	public static ProfileSettings get(UUID uuid) {
		return get(Bukkit.getPlayer(uuid));
	}

	public static List<ProfileSettings> getProfiles() {
		return profiles;
	}
	private void save() {
		boolean save = false;

		for(Map.Entry<ExplosionSettings, HashMap<String, ProfileSettingsPlugin>> pluginEntry : plugins.entrySet()) {
			ExplosionSettings settings = pluginEntry.getKey();
			for(ProfileSettingsPlugin plugin : pluginEntry.getValue().values()) {
				for(Map.Entry<String, Object> entry : plugin.getEntries()) {
					String key = settings.getName().toLowerCase() + "." + plugin.getName().toLowerCase() + "." + entry.getKey();
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
						config.set(key, value); save = true;
					}
				}
			}
		}
		if(save) {
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
