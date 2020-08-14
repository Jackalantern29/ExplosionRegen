package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class SoundSettings {
	private static final Map<Integer, SoundSettings> MAP = new HashMap<>();

	private final EnumMap<ExplosionPhase, SoundData> sounds = new EnumMap<>(ExplosionPhase.class);

	private int id = -1;

	public SoundSettings() {
		id++;
		MAP.put(id, this);
	}
	public void setSound(ExplosionPhase phase, SoundData sound) {
		this.sounds.put(phase, sound);
	}
	public SoundData getSound(ExplosionPhase phase) {
		return sounds.get(phase);
	}
	public void clearSounds() {
		sounds.clear();
	}
	public void clearSounds(ExplosionPhase phase) {
		sounds.remove(phase);
	}
	public void playSound(ExplosionPhase phase, Location location) {
		if(sounds.containsKey(phase)) {
			SoundData sound = sounds.get(phase);
			if (sound.isEnable())
				location.getWorld().playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());
		}
	}
	public void playSound(ExplosionPhase phase, Location location, Player player) {
		if (sounds.containsKey(phase)) {
			SoundData sound = sounds.get(phase);
			if (sound.isEnable())
				player.playSound(location, sound.getSound(), sound.getVolume(), sound.getPitch());
		}
	}
	public int getId() {
		return id;
	}
	public static SoundSettings getSettings(int id) {
		return MAP.getOrDefault(id, new SoundSettings());
	}
	public static void removeSettings(int id) {
		MAP.remove(id);

	}
	public static Collection<SoundSettings> getSettingsList() {
		return MAP.values();
	}
}
