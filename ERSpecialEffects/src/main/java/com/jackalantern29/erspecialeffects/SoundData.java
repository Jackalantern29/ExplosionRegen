package com.jackalantern29.erspecialeffects;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundData {
	private Sound sound;
	private float volume;
	private float pitch;
	private boolean enable = true;
	public SoundData(Sound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	public SoundData(Sound sound, float volume, float pitch, boolean enable) {
		this(sound, volume, pitch);
		this.enable = enable;
	}
	public Sound getSound() {
		return sound;
	}

	public float getVolume() {
		return volume;
	}

	public float getPitch() {
		return pitch;
	}

	public void playSound(Location location) {
		location.getWorld().playSound(location, sound, volume, pitch);
	}

	public void playSound(Location location, Player player) {
		player.playSound(location, sound, volume, pitch);
	}

	public void setSound(Sound sound) {
		this.sound = sound;
	}

	public void setVolume(float volume) {
		this.volume = volume;
	}

	public void setPitch(float pitch) {
		this.pitch = pitch;
	}

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	public static Sound getSound(String sound) {
		try {
			return Sound.valueOf(sound);
		} catch(IllegalArgumentException e) {
			return null;
		}
	}
}
