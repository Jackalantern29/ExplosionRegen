package com.jackalantern29.explosionregen.api;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.enums.ParticlePlayAt;

import xyz.xenondevs.particle.ParticleEffect;
import xyz.xenondevs.particle.data.color.RegularColor;

public class ParticleSettings {
	private static final Map<String, ParticleSettings> MAP = new HashMap<>();

	private final Map<ExplosionPhase, List<ParticleData>> particles = new HashMap<>();
	private final String name;
	private final String author;
	public ParticleSettings(String name, String author, ParticleData... particles) {
		this.author = author;
		addParticles(particles);
		File file;
		YamlConfiguration config;
		if(name != null) {
			name = name.replace(" ", "_");
			file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "particles" + File.separator + "preset", name + ".yml");
			config = YamlConfiguration.loadConfiguration(file);
			this.name = name;
			if(!file.exists()) {
				try {
					file.createNewFile();
					config.set("author", author);
					for(ParticleData particle : getParticles()) {
						ConfigurationSection section = config.createSection(particle.getPhase() + "." + particle.getParticle().name().toLowerCase());
						section.set("amount", particle.getAmount());
						section.set("display-amount", particle.getDisplayAmount());
						section.set("offset.x", particle.getOffsetX());
						section.set("offset.y", particle.getOffsetY());
						section.set("offset.z", particle.getOffsetZ());
						section.set("speed", particle.getSpeed());
						section.set("play-at", particle.getPlayAt().toString());
					}
					config.save(file);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
			for(ParticleData particle : getParticles()) {
				ConfigurationSection section = config.getConfigurationSection(particle.getPhase() + "." + particle.getParticle().name().toLowerCase());
				if(section == null) {
					section = config.createSection(particle.getPhase() + "." + particle.getParticle().name().toLowerCase());
					section.set("amount", particle.getAmount());
					section.set("display-amount", particle.getDisplayAmount());
					section.set("offset.x", particle.getOffsetX());
					section.set("offset.y", particle.getOffsetY());
					section.set("offset.z", particle.getOffsetZ());
					section.set("speed", particle.getSpeed());		
					section.set("play-at", particle.getPlayAt().toString());
					try {
						config.save(file);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					particle.setAmount(section.getInt("amount"));
					particle.setOffset((float)section.getDouble("offset.x"), (float)section.getDouble("offset.y"), (float)section.getDouble("offset.z"));
					particle.setSpeed((float)section.getDouble("speed"));
					if(section.contains("play-at"))
						particle.setPlayAt(ParticlePlayAt.valueOf(section.getString("play-at").toUpperCase().replace("-", "_")));
					if(section.contains("display-amount"))
						particle.setDisplayAmount(section.getInt("display-amount"));
				}
			}
			for(String key : config.getKeys(false)) {
				ExplosionPhase phase = ExplosionPhase.valueOf(key.replace("-", "_").toUpperCase());
				if(config.isConfigurationSection(key)) {
					for(String keyy : config.getConfigurationSection(key).getKeys(false)) {
						ParticleEffect particle = ParticleEffect.valueOf(keyy.toUpperCase());
						ConfigurationSection section = config.getConfigurationSection(key + "." + keyy);
						ParticleData data = new ParticleData(particle, phase, true, section.getInt("amount"), (float)section.getDouble("offset.x"), (float)section.getDouble("offset.y"), (float)section.getDouble("offset.z"), (float)section.getDouble("speed"));
						if(section.contains("play-at"))
							data.setPlayAt(ParticlePlayAt.valueOf(section.getString("play-at").toUpperCase().replace("-", "")));
						if(section.contains("display-amount"))
							data.setDisplayAmount(section.getInt("display-amount"));
						if(section.contains("option.color"))
							try {
								data.setData(new RegularColor((Color) Color.class.getField(section.getString("option.color").toLowerCase()).get(null)));
							} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException
									| SecurityException e) {
								e.printStackTrace();
							}
						addParticles(data);
					}
				}
			}
		} else {
			this.name = "null";
		}
		MAP.put(name, this);
	}
	public ParticleSettings(String name, ParticleData... particles) {
		this(name, null, particles);
	}
	public void addParticles(ParticleData... particles) {
		for(ParticleData particle : particles) {
			List<ParticleData> list = this.particles.getOrDefault(particle.getPhase(), new ArrayList<>());
			list.add(particle);
			this.particles.put(particle.getPhase(), list);	
		}
	}
	public void setParticle(int index, ParticleData particle) {
		List<ParticleData> list = this.particles.getOrDefault(particle.getPhase(), new ArrayList<>());
		list.remove(index);
		list.add(index, particle);
		this.particles.put(particle.getPhase(), list);
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
	public String getName() {
		return name;
	}
	public String getAuthor() {
		if(this.author == null)
			return "Server";
		else
			return this.author;
	}
	public void playParticles(ExplosionPhase phase, Location location) {
		if(particles.containsKey(phase))
			particles.get(phase).forEach(particle -> {if(particle.getPhase().equals(phase) && particle.getCanDisplay()) particle.playParticle(location);});
	}
	public void playParticles(ExplosionPhase phase, Location location, Player player) {
		if(particles.containsKey(phase))
			particles.get(phase).forEach(particle -> {if(particle.getPhase().equals(phase) && particle.getCanDisplay()) particle.playParticle(location, player);});
	}
	public static ParticleSettings getSettings(String name) {
		return MAP.getOrDefault(name, new ParticleSettings(name));
	}
	public static void removeSettings(String name) {
		MAP.remove(name);

	}
	public static Collection<ParticleSettings> getSettingsList() {
		return MAP.values();
	}
}
