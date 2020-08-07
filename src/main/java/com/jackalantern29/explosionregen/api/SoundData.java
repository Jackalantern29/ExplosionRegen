package com.jackalantern29.explosionregen.api;

import com.cryptomorin.xseries.XSound;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SoundData {
	private XSound sound;
	private float volume;
	private float pitch;
	private boolean enable = true;
	public SoundData(XSound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	public SoundData(XSound sound, float volume, float pitch, boolean enable) {
		this(sound, volume, pitch);
		this.enable = enable;
	}
	public XSound getSound() {
		return sound;
	}

	public float getVolume() {
		return volume;
	}

	public float getPitch() {
		return pitch;
	}

	public void playSound(Location location) {
		sound.play(location, volume, pitch);
	}

	public void playSound(Location location, Player player) {
		player.playSound(location, sound.parseSound(), volume, pitch);
	}

	public void setSound(XSound sound) {
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
}
