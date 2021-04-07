package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.enums.*;
import com.jackalantern29.explosionregen.api.events.ExplosionSettingsLoadEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionSettingsUnloadEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class  ExplosionSettings {
	private static final Map<String, ExplosionSettings> MAP = new HashMap<>();
	
	private final String name;

	private BlockSettings blockSettings;
	private boolean enable;
	private boolean regenAllow;
	private GenerateDirection regenDirection;
	private boolean regenInstant;
	private long regenDelay;
	private int regenMaxBlockQueue;
	private boolean regenForceBlock;
	
	private boolean damageBlockAllow;
	private DamageModifier damageBlockModifier;
	private double damageBlockAmount;
	private boolean damageEntityAllow;
	private DamageModifier damageEntityModifier;
	private double damageEntityAmount;

	private final HashMap<String, ExplosionSettingsPlugin> plugins = new HashMap<>();

	private String displayName;

	private final Map<String, ExplosionSettingsOverride> overrides = new HashMap<>();
	private final ExplosionSettingsOverride conditions;

	private ExplosionSettings(String name, BlockSettings blockSettings) {
		this.name = name;
		this.blockSettings = blockSettings;
		this.enable = true;
		this.regenAllow = true;
		this.regenDirection = GenerateDirection.RANDOM_UP;
		this.regenInstant = false;
		this.regenDelay = 200;
		this.regenMaxBlockQueue = 1;
		this.regenForceBlock = false;
		this.damageBlockAllow = true;
		this.damageBlockModifier = DamageModifier.MULTIPLY;
		this.damageBlockAmount = 1.0d;
		this.damageEntityAllow = true;
		this.damageEntityModifier = DamageModifier.MULTIPLY;
		this.damageEntityAmount = 1.0d;
		this.displayName = name;
		this.conditions = new ExplosionSettingsOverride(name + "-conditions", this);


		ExplosionSettingsLoadEvent event = new ExplosionSettingsLoadEvent(this);
		Bukkit.getPluginManager().callEvent(event);
		MAP.put(name, this);
	}
	public void saveAsFile() {
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + name + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("block-settings", getBlockSettings().getName().toLowerCase());
		map.put("enable", getAllowExplosion());
		map.put("display-name", getDisplayName());
		map.put("regen.allow", getAllowRegen());
		map.put("regen.direction", getRegenerateDirection().name().toLowerCase());
		map.put("regen.instant", isInstantRegen());
		map.put("regen.delay", getRegenDelay());
		map.put("regen.max-block-regen-queue", getMaxBlockRegenQueue());
		map.put("regen.force-block", getRegenForceBlock());
		for(DamageCategory category : DamageCategory.values()) {
			map.put("damage." + category.name().toLowerCase() + ".allow", getAllowDamage(category));
			map.put("damage." + category.name().toLowerCase() + ".modifier", getDamageModifier(category).name().toLowerCase());
			map.put("damage." + category.name().toLowerCase() + ".amount", getDamageAmount(category));
		}
		boolean doSave = false;
		for(ExplosionSettingsPlugin plugin : plugins.values()) {
			for(Map.Entry<String, Object> entry : plugin.getEntries()) {
				String key = plugin.getName().toLowerCase() + "." + entry.getKey();
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
			}
		}
		for(String key : new ArrayList<>(map.keySet())) {
			Object value = map.get(key);
			if(!config.contains(key)) {
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
	public ExplosionSettingsPlugin loadPlugin(Object plugin) {
		return loadPlugin(plugin, plugin.getClass().getName());
	}
	public ExplosionSettingsPlugin loadPlugin(Object plugin, String name) {
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + getName() + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		name = name.toLowerCase();
		if(config.isConfigurationSection(name)) {
			ConfigurationSection section = config.getConfigurationSection(name);
			ExplosionSettingsPlugin settingsPlugin = new ExplosionSettingsPlugin(plugin);
			for(Map.Entry<String, Object> map : section.getValues(true).entrySet()) {
				if (map.getValue() instanceof MemorySection)
					continue;
				settingsPlugin.setOption(map.getKey(), map.getValue());
			}
			plugins.put(settingsPlugin.getName().toLowerCase(), settingsPlugin);
			Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Loaded plugin '" + settingsPlugin.getName() + "'.");
			return settingsPlugin;
		}
		return null;
	}

	public void addPlugin(ExplosionSettingsPlugin plugin) {
		plugins.put(plugin.getName().toLowerCase(), plugin);
		Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Added plugin '" + plugin.getName() + "'.");
	}

	public ExplosionSettingsPlugin getPlugin(String plugin) {
		return plugins.get(plugin.toLowerCase());
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

	/**
	 *
	 * @return If this explosion should can trigger
	 */
	public boolean getAllowExplosion() {
		return enable;
	}

	/**
	 * Should this explosion explode
	 * @param value
	 */
	public void setAllowExplosion(boolean value) {
		enable = value;
	}

	public boolean getAllowRegen() {
		return regenAllow;
	}

	public void setAllowRegen(boolean value) {
		regenAllow = value;
	}

	public GenerateDirection getRegenerateDirection() {
		return regenDirection;
	}

	public void setRegenerateDirection(GenerateDirection direction) {
		regenDirection = direction;
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

	public void setRegenForceBlock(boolean value) {
		this.regenForceBlock = value;
	}

	public boolean getRegenForceBlock() {
		return regenForceBlock;
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
			settings.setAllowRegen(config.getBoolean("regen.allow", settings.getAllowRegen()));
			settings.setRegenerateDirection(GenerateDirection.valueOf(config.getString("regen.direction", settings.getRegenerateDirection().name()).toUpperCase()));
			settings.setInstantRegen(config.getBoolean("regen.instant", settings.isInstantRegen()));
			settings.setRegenDelay(config.getLong("regen.delay", settings.getRegenDelay()));
			settings.setMaxBlockRegenQueue(config.getInt("regen.max-block-regen-queue", settings.getMaxBlockRegenQueue()));
			settings.setRegenForceBlock(config.getBoolean("regen.force-block", settings.getRegenForceBlock()));
			for(DamageCategory category : DamageCategory.values()) {
				settings.setAllowDamage(category, config.getBoolean("damage." + category.name().toLowerCase() + ".allow", settings.getAllowDamage(category)));
				settings.setDamageModifier(category, DamageModifier.valueOf(config.getString("damage." + category.name().toLowerCase() + ".modifier", settings.getDamageModifier(category).name()).toUpperCase()));
				settings.setDamageAmount(category, config.getDouble("damage." + category.name().toLowerCase() + ".amount", settings.getDamageAmount(category)));
			}

			if(config.isConfigurationSection("conditions")) {
				ExplosionSettingsOverride override = settings.getConditions();
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
							value = Material.getMaterial(config.getString("conditions." + key).toUpperCase());
							break;
						case IS_CHARGED:
							value = config.getBoolean("conditions." + key);
							break;
						case WEATHER:
							value = WeatherType.valueOf(config.getString("conditions." + key).toUpperCase());
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
				settings.addOrSetCondition(override);
			}
			if(config.isConfigurationSection("override")) {
				for(String key : config.getConfigurationSection("override").getKeys(false)) {
					ExplosionSettingsOverride override = new ExplosionSettingsOverride(key, ExplosionSettings.getSettings(config.getString("override." + key + ".settings")));//addOverride(key, t);
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
								value = Material.getMaterial(config.getString("override." + key + ".conditions." + k).toUpperCase());
								break;
							case IS_CHARGED:
								value = config.getBoolean("override." + key + ".conditions." + k);
								break;
							case WEATHER:
								value = WeatherType.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
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
					settings.addOrSetOverride(override);
				}
			}

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
//			for(ParticleSettings particles : new ArrayList<>(getSettings(name).particleSettings.values()))
//				if(particles != null)
//					ParticleSettings.removeSettings(particles.getName());
			MAP.remove(name);
			ExplosionSettingsUnloadEvent event = new ExplosionSettingsUnloadEvent(name);
			Bukkit.getPluginManager().callEvent(event);
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
