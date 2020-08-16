package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.enums.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class  ExplosionSettings {
	private static final Map<String, ExplosionSettings> MAP = new HashMap<>();
	
	private final String name;

	private BlockSettings blockSettings;
	private boolean enable;
	private boolean regenAllow;
	private List<GenerateDirection> regenDirections = new ArrayList<>();
	private boolean regenInstant;
	private long regenDelay;
	private int regenMaxBlockQueue;
	
	private boolean damageBlockAllow;
	private DamageModifier damageBlockModifier;
	private double damageBlockAmount;
	private boolean damageEntityAllow;
	private DamageModifier damageEntityModifier;
	private double damageEntityAmount;

	private ParticleType particleType;
	
	private final Map<ParticleType, ParticleSettings> particleSettings = new HashMap<>();
	private final SoundSettings soundSettings = new SoundSettings();
	
	private ItemStack displayItem;
	private String displayName;

	private final Map<String, ExplosionSettingsOverride> overrides = new HashMap<>();
	private final ExplosionSettingsOverride conditions;
	private ExplosionSettings(String name, BlockSettings blockSettings) {
		this.name = name;
		this.blockSettings = blockSettings;
		this.enable = true;
		this.regenAllow = true;
		this.regenDirections.add(GenerateDirection.RANDOM_UP);
		this.regenInstant = false;
		this.regenDelay = 200;
		this.regenMaxBlockQueue = 1;
		this.damageBlockAllow = true;
		this.damageBlockModifier = DamageModifier.MULTIPLY;
		this.damageBlockAmount = 1.0d;
		this.damageEntityAllow = true;
		this.damageEntityModifier = DamageModifier.MULTIPLY;
		this.damageEntityAmount = 1.0d;
		this.particleType = ParticleType.VANILLA;
		this.particleSettings.put(ParticleType.VANILLA, new ParticleSettings(name + "_vanilla",
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("slime")), ExplosionPhase.ON_EXPLODE, false),
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("slime")), ExplosionPhase.EXPLOSION_FINISHED_REGEN, false),
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("heart")), ExplosionPhase.ON_BLOCK_REGEN, true),
				new ParticleData(ParticleData.getVanillaSettings(ExplosionParticle.getParticle("flame")), ExplosionPhase.BLOCK_REGENERATING, true)));
		this.particleSettings.put(ParticleType.PRESET, null);
		this.soundSettings.setSound(ExplosionPhase.ON_EXPLODE, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
		this.soundSettings.setSound(ExplosionPhase.BLOCK_REGENERATING, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
		this.soundSettings.setSound(ExplosionPhase.ON_BLOCK_REGEN, new SoundData((SoundData.getSound("STEP_GRASS") != null ? SoundData.getSound("STEP_GRASS") : SoundData.getSound("BLOCK_GRASS_STEP") != null ? SoundData.getSound("BLOCK_GRASS_STEP") : Sound.values()[0]), 1f, 1f, true));
		this.soundSettings.setSound(ExplosionPhase.EXPLOSION_FINISHED_REGEN, new SoundData((SoundData.getSound("GHAST_SCREAM") != null ? SoundData.getSound("GHAST_SCREAM") : SoundData.getSound("ENTITY_GHAST_SCREAM") != null ? SoundData.getSound("ENTITY_GHAST_SCREAM") : Sound.values()[0]), 1f, 1f, false));
		this.displayItem = new ItemStack(Material.TNT);
		this.displayName = name;
		this.conditions = new ExplosionSettingsOverride(name + "-conditions", this);
		MAP.put(name, this);
	}
//		if(config.isConfigurationSection("conditions")) {
//			ERExplosionSettingsOverride override = conditions;
//			for(String key : config.getConfigurationSection("conditions").getKeys(false)) {
//				ExplosionCondition condition = ExplosionCondition.valueOf(key.toUpperCase());
//				Object value = null;
//				switch(condition) {
//				case CUSTOM_NAME:
//					value = config.get("conditions." + key);
//					break;
//				case ENTITY:
//					value = EntityType.valueOf(config.getString("conditions." + key).toUpperCase());
//					break;
//				case BLOCK:
//					value = XMaterial.valueOf(config.getString("conditions." + key).toUpperCase());
//					break;
//				case IS_CHARGED:
//					value = config.getBoolean("conditions." + key);
//					break;
//				case WEATHER:
//					value = EROverrideWeatherType.valueOf(config.getString("conditions." + key).toUpperCase());
//					break;
//				case WORLD:
//					value = Bukkit.getWorld(config.getString("conditions." + key));
//					break;
//				case MINX:
//				case MAXX:
//				case MINY:
//				case MAXY:
//				case MINZ:
//				case MAXZ:
//					value = config.getDouble("conditions." + key);
//				}
//				override.setCondition(condition, value);
//			}
//			addOrSetCondition(override);
//		}
//		if(config.isConfigurationSection("override")) {
//			for(String key : config.getConfigurationSection("override").getKeys(false)) {
//				//ExplosionType t = config.contains("override." + key + ".conditions.entity") ? ExplosionType.ENTITY : config.contains("override." + key + ".conditions.block") ? ExplosionType.BLOCK : type;
//				//ExplosionType t = config.contains("override." + key + ".conditions.type") ? ExplosionType.valueOf(config.getString("override." + key + ".conditions.type").toUpperCase()) : type;
//				ERExplosionSettingsOverride override = new ERExplosionSettingsOverride(key, config.getString("override." + key + ".settings"));//addOverride(key, t);
//				for(String k : config.getConfigurationSection("override." + key + ".conditions").getKeys(false)) {
//					ExplosionCondition condition = ExplosionCondition.valueOf(k.toUpperCase());
//					Object value = null;
//					switch(condition) {
//					case CUSTOM_NAME:
//						value = config.get("override." + key + ".conditions." + k);
//						break;
//					case ENTITY:
//						value = EntityType.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
//						break;
//					case BLOCK:
//						value = XMaterial.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
//						break;
//					case IS_CHARGED:
//						value = config.getBoolean("override." + key + ".conditions." + k);
//						break;
//					case WEATHER:
//						value = EROverrideWeatherType.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
//						break;
//					case WORLD:
//						value = Bukkit.getWorld(config.getString("override." + key + ".conditions." + k));
//						break;
//					case MINX:
//					case MAXX:
//					case MINY:
//					case MAXY:
//					case MINZ:
//					case MAXZ:
//						value = config.getDouble("override." + key + ".conditions." + k);
//					}
//					override.setCondition(condition, value);
//				}
//				addOrSetOverride(override);
//			}
//		}
	public void saveAsFile() {
		File file;
		if(name.equals("default"))
			file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + "default.yml");
		else
			file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + "overrides" + File.separator + name + ".yml");
		if(file.exists())
		saveAsFile(file);
	}
	public void saveAsFile(File file) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("block-settings", getBlockSettings().getName().toLowerCase());
		map.put("enable", getAllowExplosion());
		map.put("display-name", getDisplayName());
		map.put("display-item", getDisplayItem().getType().name().toLowerCase());
		map.put("regen.allow", getAllowRegen());
		List<String> stringDirections = new ArrayList<>();
		for(GenerateDirection direction : getRegenerateDirections())
			stringDirections.add(direction.name().toLowerCase());
		map.put("regen.direction", stringDirections);
		map.put("regen.instant", isInstantRegen());
		map.put("regen.delay", getRegenDelay());
		map.put("regen.max-block-regen-queue", getMaxBlockRegenQueue());
		for(DamageCategory category : DamageCategory.values()) {
			map.put("damage." + category.name().toLowerCase() + ".allow", getAllowDamage(category));
			map.put("damage." + category.name().toLowerCase() + ".modifier", getDamageModifier(category).name().toLowerCase());
			map.put("damage." + category.name().toLowerCase() + ".amount", getDamageAmount(category));
		}
		map.put("particles.type", getParticleType().name().toLowerCase());
		for(ExplosionPhase phase : ExplosionPhase.values()) {
			map.put("particles.vanilla." + phase.toString() + ".particle", getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString().toLowerCase());
			map.put("particles.vanilla." + phase.toString() + ".enable", getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
			SoundData sound = getSoundSettings().getSound(phase);
			map.put("sounds." + phase.toString() + ".enable", sound.isEnable());
			map.put("sounds." + phase.toString() + ".sound", sound.getSound().name().toLowerCase());
			map.put("sounds." + phase.toString() + ".volume", sound.getVolume());
			map.put("sounds." + phase.toString() + ".pitch", sound.getPitch());
		}
		if(getParticleSettings(ParticleType.PRESET) != null)
			map.put("particles.preset", getParticleSettings(ParticleType.PRESET).getName());
		boolean doSave = false;
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(value instanceof Float)
				value = ((Float)value).doubleValue();
			if(value instanceof Long)
				value = ((Long)value).intValue();
			if(!config.contains(key) || !config.get(key).equals(value)) {
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
	
	public String getName() {
		return name;
	}

	public BlockSettings getBlockSettings() {
		return blockSettings;
	}

	public void setBlockSettings(BlockSettings blockSettings) {
		this.blockSettings = blockSettings;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String value) {
		displayName = value;
	}

	public ParticleType getParticleType() {
		return particleType;
	}

	public void setParticleType(ParticleType type) {
		particleType = type;
	}

	public boolean getAllowExplosion() {
		return enable;
	}

	public void setAllowExplosion(boolean value) {
		enable = value;
	}

	public boolean getAllowRegen() {
		return regenAllow;
	}

	public void setAllowRegen(boolean value) {
		regenAllow = value;
	}
	
	public List<GenerateDirection> getRegenerateDirections() {
		return regenDirections;
	}

	public void setRegenerateDirections(List<GenerateDirection> value) {
		regenDirections = value;
	}
	
	public boolean isInstantRegen() {
		return regenInstant;
	}

	public void setInstantRegen(boolean value) {
		regenInstant = value;
	}
	
	public long getRegenDelay() {
		return regenDelay;
	}

	public void setRegenDelay(long value) {
		regenDelay = value;
	}
	
	public int getMaxBlockRegenQueue() {
		return regenMaxBlockQueue;
	}

	public void setMaxBlockRegenQueue(int value) {
		regenMaxBlockQueue = value;
	}
	
	public boolean getAllowDamage(DamageCategory category) {
		if(category == DamageCategory.BLOCK)
			return damageBlockAllow;
		else if(category == DamageCategory.ENTITY)
			return damageEntityAllow;
		return false;
	}

	public void setAllowDamage(DamageCategory category, boolean value) {
		switch(category) {
		case BLOCK:
			damageBlockAllow = value;
			break;
		case ENTITY:
			damageEntityAllow = value;
			break;
		}
	}
	
	public DamageModifier getDamageModifier(DamageCategory category) {
		if(category == DamageCategory.BLOCK)
			return damageBlockModifier;
		else if(category == DamageCategory.ENTITY)
			return damageEntityModifier;
		return null;
	}

	public void setDamageModifier(DamageCategory category, DamageModifier value) {
		switch(category) {
		case BLOCK:
			damageBlockModifier = value;
			break;
		case ENTITY:
			damageEntityModifier = value;
			break;
		}
	}

	public double getDamageAmount(DamageCategory category) {
		if(category == DamageCategory.BLOCK)
			return damageBlockAmount;
		else if(category == DamageCategory.ENTITY)
			return damageEntityAmount;
		return 0;
	}
	
	public void setDamageAmount(DamageCategory category, double value) {
		switch(category) {
		case BLOCK:
			damageBlockAmount = value;
			break;
		case ENTITY:
			damageEntityAmount = value;
			break;
		}
	}
	
	public ParticleSettings getParticleSettings(ParticleType type) {
		return particleSettings.get(type);
	}

	public void setParticleSettings(ParticleType type, ParticleSettings particleSettings) {
		this.particleSettings.put(type, particleSettings);
	}

	public boolean getAllowSound(ExplosionPhase phase) {
		return soundSettings.getSound(phase).isEnable();
	}

	public void setAllowSound(ExplosionPhase phase, boolean enable) {
		soundSettings.getSound(phase).setEnable(enable);
	}

	public SoundSettings getSoundSettings() {
		return soundSettings;
	}
	
	public ItemStack getDisplayItem() {
		return displayItem;
	}
	
	public void setDisplayItem(ItemStack item) {
		if(item != null && item.getType() != Material.AIR) {
			displayItem = item;
		}
	}
	
	public void addOrSetOverride(ExplosionSettingsOverride override) {
		ExplosionSettingsOverride newOverride;
		if(overrides.containsKey(override.getName())) {
			newOverride = overrides.get(override.getName());
			for(ExplosionCondition condition : override.getConditions())
				newOverride.setCondition(condition, override.getConditionValue(condition));
		} else {
			newOverride = override;
			overrides.put(newOverride.getName(), newOverride);
		}
	}

	public void addOrSetCondition(ExplosionSettingsOverride override) {
		for(ExplosionCondition condition : override.getConditions())
			conditions.setCondition(condition, override.getConditionValue(condition));
	}

	public void removeOverride(String name) {
		overrides.remove(name);
	}
	public void removeCondition(ExplosionCondition condition) {
		conditions.removeCondition(condition);
	}
	
	public Collection<ExplosionSettingsOverride> getOverrides() {
		return overrides.values();
	}
	
	public ExplosionSettingsOverride getConditions() {
		return conditions;
	}
	
	
	public static ExplosionSettings registerSettings(String name, BlockSettings blockSettings) {
		if(getSettings(name) != null)
			return getSettings(name);
		ExplosionSettings settings = new ExplosionSettings(name, blockSettings);
		Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Registered Explosion Settings for '" + name + "' using '" + settings.getBlockSettings().getName() + "' block settings.");
		return settings;
	}
	public static ExplosionSettings registerSettings(File file) throws IOException {
		ExplosionSettings settings;
		String name = file.getName().substring(0, file.getName().length()-4);
		if(file.exists()) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			String blockSettings = config.getString("block-settings", "default");
			settings = registerSettings(name, BlockSettings.getSettings(blockSettings));
			settings.setAllowExplosion(config.getBoolean("enable", settings.getAllowExplosion()));
			settings.setDisplayName(config.getString("display-name", settings.getDisplayName()));

			settings.setDisplayItem(new ItemStack(Material.valueOf(config.getString("display-item", settings.getDisplayItem().getType().name()).toUpperCase())));
			settings.setAllowRegen(config.getBoolean("regen.allow", settings.getAllowRegen()));
			List<GenerateDirection> directions = new ArrayList<>();
			for(Object direction : config.getList("regen.directions", settings.getRegenerateDirections())) {
				directions.add(GenerateDirection.valueOf((direction).toString().toUpperCase()));
			}
			settings.setRegenerateDirections(directions);
			settings.setInstantRegen(config.getBoolean("regen.instant", settings.isInstantRegen()));
			settings.setRegenDelay(config.getLong("regen.delay", settings.getRegenDelay()));
			settings.setMaxBlockRegenQueue(config.getInt("regen.max-block-regen-queue", settings.getMaxBlockRegenQueue()));
			for(DamageCategory category : DamageCategory.values()) {
				settings.setAllowDamage(category, config.getBoolean("damage." + category.name().toLowerCase() + ".allow", settings.getAllowDamage(category)));
				settings.setDamageModifier(category, DamageModifier.valueOf(config.getString("damage." + category.name().toLowerCase() + ".modifier", settings.getDamageModifier(category).name()).toUpperCase()));
				settings.setDamageAmount(category, config.getDouble("damage." + category.name().toLowerCase() + ".amount", settings.getDamageAmount(category)));
			}
			settings.setParticleType(ParticleType.valueOf(config.getString("particles.type", settings.getParticleType().name()).toUpperCase()));
			for(ExplosionPhase phase : ExplosionPhase.values()) {
				ExplosionParticle particle = ExplosionParticle.getParticle(config.getString("particles.vanilla." + phase.toString() + ".particle", settings.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString()).toUpperCase());
				boolean canDisplay = config.getBoolean("particles.vanilla." + phase.toString() + ".enable", settings.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getCanDisplay());
				settings.getParticleSettings(ParticleType.VANILLA).setParticle(0, new ParticleData(ParticleData.getVanillaSettings(particle), phase, canDisplay));
				Sound sound = Sound.valueOf(config.getString("sounds." + phase.toString() + ".sound", settings.getSoundSettings().getSound(phase).getSound().name()).toUpperCase());
				float volume = (float)config.getDouble("sounds." + phase.toString() + ".volume", settings.getSoundSettings().getSound(phase).getVolume());
				float pitch = (float)config.getDouble("sounds." + phase.toString() + ".pitch", settings.getSoundSettings().getSound(phase).getPitch());
				boolean enable = config.getBoolean("sounds." + phase.toString() + ".enable", settings.getSoundSettings().getSound(phase).isEnable());
				settings.getSoundSettings().setSound(phase, new SoundData(sound, volume, pitch, enable));
			}
			settings.setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(config.getString("particles.preset", Objects.nonNull(settings.getParticleSettings(ParticleType.PRESET)) ? settings.getParticleSettings(ParticleType.PRESET).getName() : null)));
			return settings;
		} else {
			file.createNewFile();
			return registerSettings(name, BlockSettings.getSettings("default"));
		}
	}
	public static ExplosionSettings registerSettings(File file, BlockSettings blockSettings) throws IOException {
		ExplosionSettings settings = registerSettings(file);
		settings.setBlockSettings(blockSettings);
		return settings;
	}
	public static void removeSettings(String name) {
		if(getSettings(name) != null) {
			BlockSettings.removeSettings(getSettings(name).getBlockSettings().getName());
			for(ParticleSettings particles : new ArrayList<>(getSettings(name).particleSettings.values()))
				if(particles != null)
					ParticleSettings.removeSettings(particles.getName());
			MAP.remove(name);
			Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Removed Explosion Settings '" + name + "'.");
		}
	}
	
	public static Collection<ExplosionSettings> getRegisteredSettings() {
		return MAP.values();
	}
	public static ExplosionSettings getSettings(String name) {
		return MAP.get(name.toLowerCase());
	}
}
