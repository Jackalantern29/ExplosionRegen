package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;

public class ProfileSettings {
	private final static List<ProfileSettings> profiles = new ArrayList<>();
	private final List<ERProfileExplosionSettings> explosions = new ArrayList<>();
	private final UUID uuid;
	private final File file;
	private final YamlConfiguration config;
	private final LinkedHashMap<String, Object> saveLater = new LinkedHashMap<>();
	
	
	public ProfileSettings(UUID uuid) {
		this.uuid = uuid;
		if(ExplosionRegen.getSettings().getAllowProfileSettings()) {
			file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "profiles" + File.separator + uuid.toString() + ".yml");
			if(!file.exists())
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			config = YamlConfiguration.loadConfiguration(file);
			for(ExplosionPhase cat : ExplosionPhase.values()) {
				switch(cat) {
				case ON_EXPLODE:
				case EXPLOSION_FINISHED_REGEN:
					saveLater("options." + cat.toString().toLowerCase() + ".allow-particles", false);
					saveLater("options." + cat.toString().toLowerCase() + ".allow-sound", false);
				case ON_BLOCK_REGEN:
					saveLater("options." + cat.toString().toLowerCase() + ".allow-particles", true);
					saveLater("options." + cat.toString().toLowerCase() + ".allow-sound", true);
				case BLOCK_REGENERATING:
					saveLater("options." + cat.toString().toLowerCase() + ".allow-particles", true);
					saveLater("options." + cat.toString().toLowerCase() + ".allow-sound", false);
				}
			}
			save(false);
			new BukkitRunnable() {
				
				@Override
				public void run() {
					for(ExplosionSettings settings : getConfigurableSettings()) {
						boolean add = true;
						for(ERProfileExplosionSettings s : explosions) {
							if(s.getExplosionSettings().getName().equals(settings.getName())) {
								add = false;
								break;
							}
						}
						if(add) {
							explosions.add(new ERProfileExplosionSettings(settings));
						}
					}
				}
			}.runTaskTimer(ExplosionRegen.getInstance(), 0, 20);
		} else {
			file = null;
			config = null;
		}
		profiles.add(this);
	}
	public UUID getUniqueId() {
		return uuid;
	}
	public ERProfileExplosionSettings getProfileExplosionSettings(ExplosionSettings settings) {
		for(ERProfileExplosionSettings s : explosions)
			if(s.getExplosionSettings().getName().equals(settings.getName()))
				return s;
		return null;
	}
	public List<ExplosionSettings> getConfigurableSettings() {
		List<ExplosionSettings> list = new ArrayList<>();
		if(Bukkit.getOfflinePlayer(uuid).isOnline()) {
			for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
				if(Bukkit.getPlayer(uuid).hasPermission("explosionregen.explosions." + settings.getName().toLowerCase())) {
					list.add(settings);
				}
			}
		}
		return list;
	}
	
	public static ProfileSettings get(UUID uuid) {
		for(ProfileSettings p : profiles) {
			if(p.getUniqueId().equals(uuid))
				return p;
		}
		
		return new ProfileSettings(uuid);
	}
	
	private void saveLater(String key, Object value) {
		saveLater.put(key, value);
	}
	public void save() {
		save(true);
	}
	private void save(boolean replaceIfDifferent) {
		if(!saveLater.isEmpty()) {
			boolean save = false;
			for(String key : new ArrayList<>(saveLater.keySet())) {
				Object value = saveLater.get(key);	
				if(!config.contains(key) || (replaceIfDifferent && !(value instanceof Float) && !(value instanceof Long) && !(config.get(key).equals(value))) || (replaceIfDifferent && (value instanceof Float || value instanceof Long) && !config.get(key).toString().equals(value.toString()))) {
					config.set(key, value);
					save = true;
				}
				saveLater.remove(key);
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
	public class ERProfileExplosionSettings {
		private final ExplosionSettings settings;

		public ERProfileExplosionSettings(ExplosionSettings settings) {
			this.settings = settings;
		}

		public ExplosionSettings getExplosionSettings() {
			return settings;
		}


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
	}
}
