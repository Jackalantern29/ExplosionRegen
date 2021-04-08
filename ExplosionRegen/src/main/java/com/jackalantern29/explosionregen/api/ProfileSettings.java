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
//	public class ERProfileExplosionSettings {
//		private final ExplosionSettings settings;
//
//		public ERProfileExplosionSettings(ExplosionSettings settings) {
//			this.settings = settings;
//		}
//
//		public ExplosionSettings getExplosionSettings() {
//			return settings;
//		}
//
//		private ParticleType particleType = ParticleType.VANILLA;
//
//		private final Map<ParticleType, ParticleSettings> particleSettings = new HashMap<ParticleType, ParticleSettings>() {
//			{
//				put(ParticleType.VANILLA, new ParticleSettings(null,
//						new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("slime")), ExplosionPhase.ON_EXPLODE),
//						new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("slime")), ExplosionPhase.EXPLOSION_FINISHED_REGEN),
//						new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("heart")), ExplosionPhase.ON_BLOCK_REGEN),
//						new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("flame")), ExplosionPhase.BLOCK_REGENERATING)));
//				put(ParticleType.PRESET, null);
//			}
//		};
//
//		private boolean sounds_on_explode_enable = false;
//		private SoundData sounds_on_explode_sound = new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f);
//		private boolean sounds_explosion_finished_regen_enable = false;
//		private SoundData sounds_explosion_finished_regen_sound = new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f);
//		private boolean sounds_on_block_regen_enable = true;
//		private SoundData sounds_on_block_regen_sound = new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f);
//		private boolean sounds_block_regenerating_enable = false;
//		private SoundData sounds_block_regenerating_sound = new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f);
//
//		private ERProfileExplosionSettings(ExplosionSettings settings) {
//			this.settings = settings;
//
//			for(ParticleType type : ParticleType.values()) {
//				setParticleSettings(type, getParticleSettings(type));
//			}
//			for(ExplosionPhase cat : ExplosionPhase.values()) {
//				String replace = cat.toString().toLowerCase().replace("_", "-");
//				String section = settings.getName().toLowerCase() + ".sounds." + replace;
//				saveLater(section + ".enable", getAllowSound(cat));
//				saveLater(section + ".sound", getSound(cat).getSound().name().toLowerCase());
//				saveLater(section + ".volume", getSound(cat).getVolume());
//				saveLater(section + ".pitch", getSound(cat).getPitch());
//			}
//			save(false);
//			particleType = ParticleType.valueOf(config.getString(settings.getName().toLowerCase() + ".particles.type").toUpperCase());
//			for(ParticleType pTypes : ParticleType.values()) {
//				if(pTypes == ParticleType.VANILLA) {
//					for(ExplosionPhase phase : ExplosionPhase.values()) {
//						particleSettings.get(pTypes).clearParticles();
//						ParticleData particle = new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle(config.getString(settings.getName().toLowerCase() + ".particles.vanilla" + phase.toString() + ".particle"))), phase);
//						particle.setCanDisplay(config.getBoolean(settings.getName().toLowerCase() + ".particles.vanilla" + phase.toString() + ".enable"));
//						particleSettings.get(pTypes).addParticles(particle);
//					}
//				} else if(pTypes == ParticleType.PRESET && !config.getString(settings.getName().toLowerCase() + ".particles.preset").equalsIgnoreCase("none")) {
//					particleSettings.put(pTypes, ParticleSettings.getSettings(config.getString(settings.getName().toLowerCase() + ".particles.preset")));
//				}
//			}
//
//			sounds_on_explode_enable = config.getBoolean(settings.getName().toLowerCase() + ".sounds.on-explode.enable");
//			sounds_on_explode_sound = new SoundData(Sound.valueOf(config.getString(settings.getName().toLowerCase() + ".sounds.on-explode.sound").toUpperCase()), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.on-explode.volume")), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.on-explode.pitch")));
//			sounds_explosion_finished_regen_enable = config.getBoolean(settings.getName().toLowerCase() + ".sounds.explosion-finished-regen.enable");
//			sounds_explosion_finished_regen_sound = new SoundData(Sound.valueOf(config.getString(settings.getName().toLowerCase() + ".sounds.explosion-finished-regen.sound").toUpperCase()), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.explosion-finished-regen.volume")), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.explosion-finished-regen.pitch")));
//			sounds_on_block_regen_enable = config.getBoolean(settings.getName().toLowerCase() + ".sounds.on-block-regen.enable");
//			sounds_on_block_regen_sound = new SoundData(Sound.valueOf(config.getString(settings.getName().toLowerCase() + ".sounds.on-block-regen.sound").toUpperCase()), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.on-block-regen.volume")), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.on-block-regen.pitch")));
//			sounds_block_regenerating_enable = config.getBoolean(settings.getName().toLowerCase() + ".sounds.block-regenerating.enable");
//			sounds_block_regenerating_sound = new SoundData(Sound.valueOf(config.getString(settings.getName().toLowerCase() + ".sounds.block-regenerating.sound").toUpperCase()), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.block-regenerating.volume")), Float.parseFloat(config.getString(settings.getName().toLowerCase() + ".sounds.block-regenerating.pitch")));
//		}
//		public ExplosionSettings getExplosionSettings() {
//			return settings;
//		}
//
////		public boolean getAllowParticle(ExplosionPhase phase) {
////			return particles.get(phase).getCanDisplay();
////		}
////		public void setAllowParticle(ExplosionPhase phase, boolean value) {
////			particles.get(phase).setCanDisplay(value);
////			saveLater(settings.getName().toLowerCase() + ".particles." + phase.toString() + ".enable", value);
////		}
//		public ParticleSettings getParticleSettings(ParticleType type) {
//			return particleSettings.get(type);
//		}
//		public ParticleType getParticleType() {
//			return particleType;
//		}
//		public void setParticleType(ParticleType type) {
//			particleType = type;
//			saveLater("particles.type", type.name().toLowerCase());
//		}
//		public void setParticleSettings(ParticleType type, ParticleSettings particleSettings) {
//			this.particleSettings.put(type, particleSettings);
//			if(type == ParticleType.VANILLA) {
//				for(ParticleData particle : particleSettings.getParticles()) {
//					saveLater(settings.getName().toLowerCase() + ".particles.vanilla." + particle.getPhase().toString() + ".particle", particle.getParticle().toString().toLowerCase());
//					saveLater(settings.getName().toLowerCase() + ".particles.vanilla." + particle.getPhase().toString() + ".enable", particle.getCanDisplay());
//				}
//			} else if(type == ParticleType.PRESET)
//				saveLater(settings.getName().toLowerCase() + ".particles.preset", particleSettings.getName());
//		}
//
//
//		public boolean getAllowSound(ExplosionPhase category) {
//			if(category == ExplosionPhase.ON_EXPLODE)
//				return sounds_on_explode_enable;
//			else if(category == ExplosionPhase.EXPLOSION_FINISHED_REGEN)
//				return sounds_explosion_finished_regen_enable;
//			else if(category == ExplosionPhase.ON_BLOCK_REGEN)
//				return sounds_on_block_regen_enable;
//			else if(category == ExplosionPhase.BLOCK_REGENERATING)
//				return sounds_block_regenerating_enable;
//			return false;
//		}
//		public void setAllowSound(ExplosionPhase phase, boolean value) {
//			switch(phase) {
//			case BLOCK_REGENERATING:
//				sounds_block_regenerating_enable = value;
//				saveLater(settings.getName().toLowerCase() + ".sounds.block-regenerating.enable", value);
//				break;
//			case EXPLOSION_FINISHED_REGEN:
//				sounds_explosion_finished_regen_enable = value;
//				saveLater(settings.getName().toLowerCase() + ".sounds.explosion-finished-regen.enable", value);
//				break;
//			case ON_BLOCK_REGEN:
//				sounds_on_block_regen_enable = value;
//				saveLater(settings.getName().toLowerCase() + ".sounds.on-block-regen.enable", value);
//				break;
//			case ON_EXPLODE:
//				sounds_on_explode_enable = value;
//				saveLater(settings.getName().toLowerCase() + ".sounds.on-explode.enable", value);
//				break;
//			}
//		}
//
//
//		public SoundData getSound(ExplosionPhase category) {
//			if(category == ExplosionPhase.ON_EXPLODE)
//				return sounds_on_explode_sound;
//			else if(category == ExplosionPhase.EXPLOSION_FINISHED_REGEN)
//				return sounds_explosion_finished_regen_sound;
//			else if(category == ExplosionPhase.ON_BLOCK_REGEN)
//				return sounds_on_block_regen_sound;
//			else if(category == ExplosionPhase.BLOCK_REGENERATING)
//				return sounds_block_regenerating_sound;
//			return null;
//		}
//		public void setSound(ExplosionPhase phase, SoundData value) {
//			String replace = phase.toString().toLowerCase().replace("_", "-");
//
//			saveLater(settings.getName().toLowerCase() + ".sounds." + replace + ".sound", value.getSound().name().toLowerCase());
//			saveLater(settings.getName().toLowerCase() + ".sounds." + replace + ".volume", value.getVolume());
//			saveLater(settings.getName().toLowerCase() + ".sounds." + replace + ".pitch", value.getPitch());
//			switch (phase) {
//				case BLOCK_REGENERATING:
//					sounds_block_regenerating_sound = value;
//					break;
//				case EXPLOSION_FINISHED_REGEN:
//					sounds_explosion_finished_regen_sound = value;
//					break;
//				case ON_BLOCK_REGEN:
//					sounds_on_block_regen_sound = value;
//					break;
//				case ON_EXPLODE:
//					sounds_on_explode_sound = value;
//					break;
//			}
//		}
//	}
}
