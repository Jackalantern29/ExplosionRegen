package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.jackalantern29.explosionregen.api.enums.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.jackalantern29.explosionregen.ExplosionRegen;

import xyz.xenondevs.particle.ParticleEffect;

public class  ExplosionSettings {
	private static final Map<String, ExplosionSettings> MAP = new HashMap<>();
	
	
	private final File file;
	private final YamlConfiguration config;
	
	private final LinkedHashMap<String, Object> saveLater = new LinkedHashMap<>();
	
	private final String name;
	private BlockSettings blockSettings = BlockSettings.getSettings("default") ;
	
	private boolean enable = true;
	
	private boolean regenAllow = true;
	private List<GenerateDirection> regenDirections = new ArrayList<>(Arrays.asList(GenerateDirection.RANDOM_UP));
	private boolean regenInstant = false;
	private long regenDelay = 200;
	private long regenBlockDelay = 0;
	private int regenMaxBlockQueue = 1;
	
	private boolean damageBlockAllow = true;
	private DamageModifier damageBlockModifierType = DamageModifier.MULTIPLY;
	private int damageBlockAmount = 1;
	private boolean damageEntityAllow = true;
	private DamageModifier damageEntityModifierType = DamageModifier.MULTIPLY;
	private int damageEntityAmount = 1;

	private ParticleType particleType = ParticleType.VANILLA;
	
	private final Map<ParticleType, ParticleSettings> particleSettings = new HashMap<ParticleType, ParticleSettings>() {
		{
			put(ParticleType.VANILLA, new ParticleSettings(null,
					new ParticleData(ParticleData.getVanillaSettings(ParticleEffect.SLIME), ExplosionPhase.ON_EXPLODE),
					new ParticleData(ParticleData.getVanillaSettings(ParticleEffect.SLIME), ExplosionPhase.EXPLOSION_FINISHED_REGEN),
					new ParticleData(ParticleData.getVanillaSettings(ParticleEffect.HEART), ExplosionPhase.ON_BLOCK_REGEN),
					new ParticleData(ParticleData.getVanillaSettings(ParticleEffect.FLAME), ExplosionPhase.BLOCK_REGENERATING)));
			put(ParticleType.PRESET, null);
		}
	};

	private final SoundSettings soundSettings = new SoundSettings();
	
	private ItemStack displayItem = XMaterial.TNT.parseItem();
	private String displayName = "Default Explosions";
	
	private final Map<String, ERExplosionSettingsOverride> overrides = new HashMap<>();
	private ERExplosionSettingsOverride conditions;
	private ExplosionSettings(String name) {
		//this.type = type;
		conditions = new ERExplosionSettingsOverride(getName() + "-conditions", this);
		MAP.put(name.toLowerCase(), this);
		this.name = name;
		if(name.equals("default"))
			file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + "default.yml");
		else
			this.file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + "overrides" + File.separator + name + ".yml");
		
		if(!file.exists()) {
			if(!file.getParentFile().exists())
				file.getParentFile().mkdir();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.config = YamlConfiguration.loadConfiguration(file);
		//saveLater("type", getExplosionType().toString().toLowerCase());
		saveLater("block-settings", blockSettings.getName());
		saveLater("enable", getAllowExplosion());
		setDisplayName(getDisplayName());
		setDisplayItem(getDisplayItem());
		setAllowRegen(getAllowRegen());
		List<GenerateDirection> list = new ArrayList<>();
		for(GenerateDirection direction : getRegenerateDirections())
			list.add(direction);
		setRegenerateDirections(list);
		setInstantRegen(isInstantRegen());
		setRegenDelay(getRegenDelay());
		setBlockRegenDelay(getBlockRegenDelay());
		setMaxBlockRegenQueue(getMaxBlockRegenQueue());
		for(ERSettingsDamageCategory cat : ERSettingsDamageCategory.values()) {
			saveLater("damage." + cat.toString().toLowerCase() + ".allow", allowDamage(cat));
			saveLater("damage." + cat.toString().toLowerCase() + ".modifier-type", getDamageModifier(cat).toString().toLowerCase());
			saveLater("damage." + cat.toString().toLowerCase() + ".amount", getDamageAmount(cat));
		}
		setParticleType(getParticleType());
		for(ParticleType pTypes : ParticleType.values()) {
			if(getParticleSettings(pTypes) != null)
				setParticleSettings(pTypes, getParticleSettings(pTypes));
			else if(pTypes == ParticleType.PRESET)
				saveLater("particles.preset", "none");
		}
		for(ExplosionPhase phase : ExplosionPhase.values()) {
			soundSettings.setSound(phase, new SoundData(XSound.BLOCK_GRASS_STEP, 1f, 1f, true));
			String replace = phase.toString().toLowerCase().replace("_", "-");
			String section = "sounds." + replace;
			saveLater(section + ".enable", getAllowSound(phase));
			saveLater(section + ".sound", getSoundSettings().getSound(phase).getSound().name().toLowerCase());
			saveLater(section + ".volume", getSoundSettings().getSound(phase).getVolume());
			saveLater(section + ".pitch", getSoundSettings().getSound(phase).getPitch());
		}
		save(false);
		
		blockSettings = BlockSettings.getSettings(config.getString("block-settings"));
		enable = config.getBoolean("enable");
		displayName = config.getString("display-name").replace("&", "§");
		displayItem = XMaterial.valueOf(config.getString("display-item").toUpperCase()).parseItem();
		
		regenAllow = config.getBoolean("regen.allow");
		LinkedList<GenerateDirection> directions = new LinkedList<>();
		for(String s : config.getStringList("regen.directions"))
			directions.add(GenerateDirection.valueOf(s.toUpperCase()));
		regenDirections = directions;
		regenInstant = config.getBoolean("regen.instant");
		regenDelay = config.getLong("regen.delay");
		regenBlockDelay = config.getLong("regen.block-delay");
		regenMaxBlockQueue = config.getInt("regen.max-block-regen-queue");

		
		damageBlockAllow = config.getBoolean("damage.block.allow");
		damageBlockModifierType = DamageModifier.valueOf(config.getString("damage.block.modifier-type").toUpperCase());
		damageBlockAmount = config.getInt("damage.block.amount");
		
		damageEntityAllow = config.getBoolean("damage.entity.allow");
		damageEntityModifierType = DamageModifier.valueOf(config.getString("damage.entity.modifier-type").toUpperCase());
		damageEntityAmount = config.getInt("damage.entity.amount");
		
		particleType = ParticleType.valueOf(config.getString("particles.type").toUpperCase());
		for(ParticleType pTypes : ParticleType.values()) {
			if(pTypes == ParticleType.VANILLA) {
				particleSettings.get(pTypes).clearParticles();
				for(ExplosionPhase phase : ExplosionPhase.values()) {
					ParticleData particle = new ParticleData(ParticleData.getVanillaSettings(ParticleEffect.valueOf(config.getString("particles.vanilla." + phase.toString() + ".particle").toUpperCase())), phase);
					particle.setCanDisplay(config.getBoolean("particles.vanilla." + phase.toString() + ".enable"));
					particleSettings.get(pTypes).addParticles(particle);
				}	
			} else if(pTypes == ParticleType.PRESET && config.contains("particles.preset") && !config.getString("particles.preset").equalsIgnoreCase("none")) {
				particleSettings.put(pTypes, ParticleSettings.getSettings(config.getString("particles.preset")));
			}
		}
		for(ExplosionPhase phase : ExplosionPhase.values()) {
			String section = "sounds." + phase.toString();
			XSound sound = XSound.valueOf(config.getString(section + ".sound").toUpperCase());
			float volume = (float) config.getDouble(section + ".volume");
			float pitch = (float) config.getDouble(section + ".pitch");
			boolean enable = config.getBoolean(section + ".enable");
			soundSettings.setSound(phase, new SoundData(sound, volume, pitch, enable));
		}

		if(config.isConfigurationSection("conditions")) {
			ERExplosionSettingsOverride override = conditions;
			for(String key : config.getConfigurationSection("conditions").getKeys(false)) {
				ExplosionCondition condition = ExplosionCondition.valueOf(key.toUpperCase());
				Object value = null;
				switch(condition) {
				case CUSTOM_NAME:
					value = config.get("conditions." + key);
					break;
				case ENTITY:
					value = EntityType.valueOf(config.getString("conditions." + key).toUpperCase());
					break;
				case BLOCK:
					value = XMaterial.valueOf(config.getString("conditions." + key).toUpperCase());
					break;
				case IS_CHARGED:
					value = config.getBoolean("conditions." + key);
					break;
				case WEATHER:
					value = EROverrideWeatherType.valueOf(config.getString("conditions." + key).toUpperCase());
					break;
				case WORLD:
					value = Bukkit.getWorld(config.getString("conditions." + key));
					break;
				case MINX:
				case MAXX:
				case MINY:
				case MAXY:
				case MINZ:
				case MAXZ:
					value = config.getDouble("conditions." + key);
				}
				override.setCondition(condition, value);
			}
			addOrSetCondition(override);
		}
		
		if(config.isConfigurationSection("override")) {
			for(String key : config.getConfigurationSection("override").getKeys(false)) {
				//ExplosionType t = config.contains("override." + key + ".conditions.entity") ? ExplosionType.ENTITY : config.contains("override." + key + ".conditions.block") ? ExplosionType.BLOCK : type;
				//ExplosionType t = config.contains("override." + key + ".conditions.type") ? ExplosionType.valueOf(config.getString("override." + key + ".conditions.type").toUpperCase()) : type;
				ERExplosionSettingsOverride override = new ERExplosionSettingsOverride(key, config.getString("override." + key + ".settings"));//addOverride(key, t);
				for(String k : config.getConfigurationSection("override." + key + ".conditions").getKeys(false)) {
					ExplosionCondition condition = ExplosionCondition.valueOf(k.toUpperCase());
					Object value = null;
					switch(condition) {
					case CUSTOM_NAME:
						value = config.get("override." + key + ".conditions." + k);
						break;
					case ENTITY:
						value = EntityType.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
						break;
					case BLOCK:
						value = XMaterial.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
						break;
					case IS_CHARGED:
						value = config.getBoolean("override." + key + ".conditions." + k);
						break;
					case WEATHER:
						value = EROverrideWeatherType.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
						break;
					case WORLD:
						value = Bukkit.getWorld(config.getString("override." + key + ".conditions." + k));
						break;
					case MINX:
					case MAXX:
					case MINY:
					case MAXY:
					case MINZ:
					case MAXZ:
						value = config.getDouble("override." + key + ".conditions." + k);
					}
					override.setCondition(condition, value);
				}
				addOrSetOverride(override);
			}
		}
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
//	public ExplosionType getExplosionType() {
//		return type;
//	}
	
	public String getName() {
		return name;
		
	}
	public BlockSettings getBlockSettings() {
		return blockSettings;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String value) {
		displayName = value;
		saveLater("display-name", value);
	}
	public ParticleType getParticleType() {
		return particleType;
	}
	public void setParticleType(ParticleType type) {
		particleType = type;
		saveLater("particles.type", type.name().toLowerCase());
	}
	public boolean getAllowExplosion() {
		return enable;
	}
	public void setAllowExplosion(boolean value) {
		enable = value;
		saveLater("enable", value);
	}

	
	public boolean getAllowRegen() {
		return regenAllow;
	}
	public void setAllowRegen(boolean value) {
		regenAllow = value;
		saveLater("regen.allow", value);
	}

	
	public List<GenerateDirection> getRegenerateDirections() {
		return regenDirections;
	}
	public void setRegenerateDirections(List<GenerateDirection> value) {
		regenDirections = value;
		List<String> list = new ArrayList<>();
		value.forEach(direction -> list.add(direction.name()));
		saveLater("regen.directions", list);
	}

	
	public boolean isInstantRegen() {
		return regenInstant;
	}
	public void setInstantRegen(boolean value) {
		regenInstant = value;
		saveLater("regen.instant", value);
	}

	
	public long getRegenDelay() {
		return regenDelay;
	}
	public void setRegenDelay(long value) {
		regenDelay = value;
		saveLater("regen.delay", value);
	}
	
	public long getBlockRegenDelay() {
		return regenBlockDelay;
	}
	public void setBlockRegenDelay(long value) {
		regenBlockDelay = value;
		saveLater("regen.block-delay", value);
	}
	
	public int getMaxBlockRegenQueue() {
		return regenMaxBlockQueue;
	}
	public void setMaxBlockRegenQueue(int value) {
		regenMaxBlockQueue = value;
		saveLater("regen.max-block-regen-queue", value);
	}

	
	public boolean allowDamage(ERSettingsDamageCategory category) {
		if(category == ERSettingsDamageCategory.BLOCK)
			return damageBlockAllow;
		else if(category == ERSettingsDamageCategory.ENTITY)
			return damageEntityAllow;
		return false;
	}
	public void setAllowDamage(ERSettingsDamageCategory category, boolean value) {
		switch(category) {
		case BLOCK:
			damageBlockAllow = value;
			saveLater("damage.block.allow", value);
			break;
		case ENTITY:
			damageEntityAllow = value;
			saveLater("damage.entity.allow", value);
			break;
		}
	}
	
	public DamageModifier getDamageModifier(ERSettingsDamageCategory category) {
		if(category == ERSettingsDamageCategory.BLOCK)
			return damageBlockModifierType;
		else if(category == ERSettingsDamageCategory.ENTITY)
			return damageEntityModifierType;
		return null;
	}
	public void setModifier(ERSettingsDamageCategory category, DamageModifier value) {
		String valueString = value.name().toLowerCase().replace("_", "-");
		switch(category) {
		case BLOCK:
			damageBlockModifierType = value;
			saveLater("damage.block.modifier-type", valueString);
			break;
		case ENTITY:
			damageEntityModifierType = value;
			saveLater("damage.entity.modifier-type", valueString);
			break;
		}
	}

	
	public int getDamageAmount(ERSettingsDamageCategory category) {
		if(category == ERSettingsDamageCategory.BLOCK)
			return damageBlockAmount;
		else if(category == ERSettingsDamageCategory.ENTITY)
			return damageEntityAmount;
		return 0;
	}
	
	public void setDamageAmount(ERSettingsDamageCategory category, int value) {
		switch(category) {
		case BLOCK:
			damageBlockAmount = value;
			saveLater("damage.block.amount", value);
			break;
		case ENTITY:
			damageEntityAmount = value;
			saveLater("damage.entity.amount", value);
			break;
		}
	}
	
	public ParticleSettings getParticleSettings(ParticleType type) {
		return particleSettings.get(type);
	}
	
	public void setParticleSettings(ParticleType type, ParticleSettings particleSettings) {
		this.particleSettings.put(type, particleSettings);
		if(type == ParticleType.VANILLA) {
			for(ParticleData particle : particleSettings.getParticles()) {
				saveLater("particles.vanilla." + particle.getPhase().toString() + ".particle", particle.getParticle().name().toLowerCase());
				saveLater("particles.vanilla." + particle.getPhase().toString() + ".enable", particle.getCanDisplay());
			}
		} else if(type == ParticleType.PRESET)
			saveLater("particles.preset", particleSettings.getName());
	}

	public boolean getAllowSound(ExplosionPhase phase) {
		return soundSettings.getSound(phase).isEnable();
	}
	public void setAllowSound(ExplosionPhase phase, boolean enable) {
		soundSettings.getSound(phase).setEnable(enable);
		saveLater("sounds." + phase.toString() + ".enable", enable);
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
			saveLater("display-item", item.getType().name().toLowerCase());			
		}
	}
	
	public void addOrSetOverride(ERExplosionSettingsOverride override) {
		ERExplosionSettingsOverride newOverride;
		if(overrides.containsKey(override.getName())) {
			newOverride = overrides.get(override.getName());
			for(ExplosionCondition condition : override.getConditions())
				newOverride.setCondition(condition, override.getConditionValue(condition));
		} else {
			newOverride = override;
			overrides.put(newOverride.getName(), newOverride);
		}
		
		for(ExplosionCondition condition : newOverride.getConditions()) {
			saveLater("override." + newOverride.getName() + ".conditions." + condition.name().toLowerCase(), newOverride.getConditionValue(condition) instanceof Enum ? ((Enum<?>)newOverride.getConditionValue(condition)).name() : newOverride.getConditionValue(condition));
			saveLater("override." + newOverride.getName() + ".settings", newOverride.getExplosionSettings().getName().toLowerCase());
		}
	}
	public void addOrSetCondition(ERExplosionSettingsOverride override) {
		for(ExplosionCondition condition : override.getConditions())
			conditions.setCondition(condition, override.getConditionValue(condition));
		
		for(ExplosionCondition condition : conditions.getConditions()) {
			saveLater("conditions." + condition.name().toLowerCase(), conditions.getConditionValue(condition) instanceof Enum ? ((Enum<?>)conditions.getConditionValue(condition)).name() : conditions.getConditionValue(condition));
		}
	}
	public void removeOverride(String name) {
		overrides.remove(name);
		saveLater("override." + name, null);
	}
	public void removeCondition(ExplosionCondition condition) {
		conditions.removeCondition(condition);
		saveLater("conditions." + condition.name().toLowerCase(), null);
	}
	
	public Collection<ERExplosionSettingsOverride> getOverrides() {
		return overrides.values();
	}
	
	public ERExplosionSettingsOverride getConditions() {
		return conditions;
	}
	
	
	public static ExplosionSettings registerSettings(String name) {
		if(getSettings(name) != null)
			return getSettings(name);
		ExplosionSettings settings = new ExplosionSettings(name);
		Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Registered Explosion Settings for '" + name + "' using '" + settings.getBlockSettings().getName() + "' block settings.");
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
