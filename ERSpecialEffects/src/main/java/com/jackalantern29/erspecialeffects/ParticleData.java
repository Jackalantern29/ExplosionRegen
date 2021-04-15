package com.jackalantern29.erspecialeffects;

import com.jackalantern29.explosionregen.BukkitMethods;
import com.jackalantern29.explosionregen.api.ExplosionParticle;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ParticleData implements Cloneable {
	private ExplosionParticle particle;

	private int amount = 5;
	private float offsetX = 0.1f;
	private float offsetY = 0.1f;
	private float offsetZ = 0.1f;
	private double speed = 0d;
	private Object data;
	
	private int displayAmount = 1;
	private ParticlePlayAt playAt = ParticlePlayAt.ANYWHERE;
	
	public ParticleData(ParticleData particle) {
		this(particle.getParticle(), particle.getAmount(), particle.getOffsetX(), particle.getOffsetY(), particle.getOffsetY(), particle.getOffsetZ());
	}

	public ParticleData(ExplosionParticle particle) {
		this.particle = particle;
	}

	public ParticleData(ExplosionParticle particle, int amount) {
		this(particle);
		setAmount(amount);
	}

	public ParticleData(ExplosionParticle particle, int amount, float offsetX, float offsetY, float offsetZ) {
		this(particle, amount);
		setOffsetX(offsetX);
		setOffsetY(offsetY);
		setOffsetZ(offsetZ);
	}

	public ParticleData(ExplosionParticle particle, int amount, float offsetX, float offsetY, float offsetZ, float speed) {
		this(particle, amount, offsetX, offsetY, offsetZ);
		setSpeed(speed);
	}

	public ExplosionParticle getParticle() {
		return particle;
	}

	public void setParticle(ExplosionParticle particle) {
		this.particle = particle;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public float getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(float offsetX) {
		this.offsetX = offsetX;
	}

	public float getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}

	public float getOffsetZ() {
		return offsetZ;
	}

	public void setOffsetZ(float offsetZ) {
		this.offsetZ = offsetZ;
	}

	public void setOffset(float offsetX, float offsetY, float offsetZ) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.offsetZ = offsetZ;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public ParticlePlayAt getPlayAt() {
		return playAt;
	}

	public void setPlayAt(ParticlePlayAt playAt) {
		this.playAt = playAt;
	}

	public int getDisplayAmount() {
		return displayAmount;
	}

	public void setDisplayAmount(int displayAmount) {
		this.displayAmount = displayAmount;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void playParticle(Location location) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			playParticle(location, player);
		}
	}
	public void playParticle(Location location, Player player) {
		for (int i = 0; i < displayAmount; i++) {
			BukkitMethods.spawnParticle(player, particle, location, amount, offsetX, offsetY, offsetZ, speed, data);
		}
	}
	
    @Override
    public ParticleData clone() {
        try {
            return (ParticleData) super.clone();
        } catch (CloneNotSupportedException e) {
        	throw new Error(e);
        }
    }
}
