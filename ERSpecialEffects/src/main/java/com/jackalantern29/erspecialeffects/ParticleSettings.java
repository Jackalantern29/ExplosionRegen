package com.jackalantern29.erspecialeffects;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.ExplosionParticle;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.particledata.DustColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class ParticleSettings {
	private static final Map<String, ParticleSettings> MAP = new HashMap<>();
	private File file;
	private final HashMap<ExplosionPhase, List<ParticleData>> particles = new HashMap<>();
	private final HashMap<ExplosionPhase, Boolean> displayParticles = new HashMap<>();

	private final String name;
	private final String author;
	public ParticleSettings(String name, String author) {
		for(ExplosionPhase phase : ExplosionPhase.values()) {
			this.displayParticles.put(phase, true);
		}
		name = name.replace(" ", "_").toLowerCase();
		this.name = name;
		if(author == null)
			author = "Server";
		this.author = author;
		this.file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "particles" + File.separator + "preset", name + ".yml");
		MAP.put(name, this);
	}
	public ParticleSettings(String name) {
		this(name, null);
	}

	public void addParticles(ExplosionPhase phase, ParticleData... particles) {
		for(ParticleData particle : particles) {
			List<ParticleData> list = this.particles.getOrDefault(phase, new ArrayList<>());
			list.add(particle);
			this.particles.put(phase, list);
		}
	}

	public void setParticle(int index, ExplosionPhase phase, ParticleData particle) {
		List<ParticleData> list = this.particles.getOrDefault(phase, new ArrayList<>());
		list.remove(index);
		list.add(index, particle);
		this.particles.put(phase, list);
	}

	public void setParticles(ExplosionPhase phase, List<ParticleData> particleData) {
		this.particles.put(phase, particleData);
	}

	public List<ParticleData> getParticles() {
		List<ParticleData> list = new ArrayList<>();
		particles.values().forEach(list::addAll);
		return list;
	}

	public List<ParticleData> getParticles(ExplosionPhase phase) {
		List<ParticleData> list = new ArrayList<>();
		if(particles.containsKey(phase))
			list.addAll(particles.get(phase));
		return list;
	}

	public void clearParticles() {
		particles.clear();
	}

	public void clearParticles(ExplosionPhase phase) {
		particles.remove(phase);
	}

	public void setCanDisplay(ExplosionPhase phase, boolean display) {
		displayParticles.put(phase, display);
	}

	public boolean canDisplay(ExplosionPhase phase) {
		return displayParticles.get(phase);
	}

	public String getName() {
		return name;
	}

	public String getAuthor() {
		return this.author;
	}

	public void playParticles(ExplosionPhase phase, Location location) {
		if(particles.containsKey(phase))
			particles.get(phase).forEach(particle -> {particle.playParticle(location);});
	}
	public void playParticles(ExplosionPhase phase, Location location, Player player) {
		if(particles.containsKey(phase))
			particles.get(phase).forEach(particle -> {particle.playParticle(location, player);});
	}
	public void saveToFile() {
		saveToFile(file);
	}
	public void saveToFile(File file) {
		YamlConfiguration config;
		config = YamlConfiguration.loadConfiguration(file);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("author", getAuthor());
		for(ExplosionPhase phase : ExplosionPhase.values()) {
			for(ParticleData particle : getParticles(phase)) {
				String section = phase + "." + particle.getParticle().toString().toLowerCase();
				map.put(section + ".amount", particle.getAmount());
				map.put(section + ".display-amount", particle.getDisplayAmount());
				map.put(section + ".offset.x", particle.getOffsetX());
				map.put(section + ".offset.y", particle.getOffsetY());
				map.put(section + ".offset.z", particle.getOffsetZ());
				map.put(section + ".speed", particle.getSpeed());
				map.put(section + ".play-at", particle.getPlayAt().toString());
			}
		}
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

	public static ParticleSettings getSettings(String name) {
		return MAP.get(name);
	}
	public static ParticleSettings load(File file) {
		if(file == null || !file.exists())
			return null;
		String name = file.getName().substring(0, file.getName().length()-4);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		if(getSettings(name) != null)
			return getSettings(name);
		List<ParticleData> particles = new ArrayList<>();
		ParticleSettings particleSettings = new ParticleSettings(name, config.getString("author"));
		for(String key : config.getKeys(false)) {

			ExplosionPhase phase;
			try {
				phase = ExplosionPhase.valueOf(key.replace("-", "_").toUpperCase());
			} catch(IllegalArgumentException e) {
				continue;
			}
			if(config.isConfigurationSection(key)) {
				for(String keyy : config.getConfigurationSection(key).getKeys(false)) {
					ExplosionParticle particle = ExplosionParticle.getParticle(keyy);
					ConfigurationSection section = config.getConfigurationSection(key + "." + keyy);
					ParticleData data = new ParticleData(particle, section.getInt("amount"), (float)section.getDouble("offset.x"), (float)section.getDouble("offset.y"), (float)section.getDouble("offset.z"), (float)section.getDouble("speed"));
					if(section.contains("play-at"))
						data.setPlayAt(ParticlePlayAt.valueOf(section.getString("play-at").toUpperCase().replace("-", "_")));
					if(section.contains("display-amount"))
						data.setDisplayAmount(section.getInt("display-amount"));
					if(section.contains("option.color")) {
						try {
							Color color = (Color) Color.class.getField(section.getString("option.color").toLowerCase()).get(null);
							data.setData(new DustColor(org.bukkit.Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue()), section.getInt("option.size", 1)));
						} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
								| SecurityException e) {
							e.printStackTrace();
						}
					}
					particles.add(data);
				}
			}
			particles.forEach(particle -> particleSettings.addParticles(phase, particle));
		}
		particleSettings.file = file;
		return particleSettings;
	}
	public static void removeSettings(String name) {
		MAP.remove(name);
	}

	public static Set<ParticleSettings> getServerSettings() {
		Set<ParticleSettings> particles = new HashSet<>();
		for(Map.Entry<String, ParticleSettings> entry : new HashMap<>(MAP).entrySet()) {
			if(entry.getValue().getAuthor().equals("Server"))
				particles.add(entry.getValue());
		}
		return particles;
	}

	public static Set<ParticleSettings> getPresetSettings() {
		Set<ParticleSettings> particles = new HashSet<>();
		for(Map.Entry<String, ParticleSettings> entry : new HashMap<>(MAP).entrySet()) {
			if(!entry.getValue().getAuthor().equals("Server") && !entry.getValue().getAuthor().equals("Vanilla"))
				particles.add(entry.getValue());
		}
		return particles;
	}

	public static Collection<ParticleSettings> getParticleSettings() {
		return MAP.values();
	}

	public static Set<ParticleSettings> getVanillaSettingss() {
		Set<ParticleSettings> particles = new HashSet<>();
		for(Map.Entry<String, ParticleSettings> entry : new HashMap<>(MAP).entrySet()) {
			if(entry.getValue().getAuthor().equals("Vanilla"))
				particles.add(entry.getValue());
		}
		return particles;
	}
}
