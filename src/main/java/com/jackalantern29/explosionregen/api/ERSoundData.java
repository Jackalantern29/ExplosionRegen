package com.jackalantern29.explosionregen.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.cryptomorin.xseries.XSound;

public class ERSoundData {
	private XSound sound;
	private float volume;
	private float pitch;
	
	public ERSoundData(XSound sound, float volume, float pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
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
}
