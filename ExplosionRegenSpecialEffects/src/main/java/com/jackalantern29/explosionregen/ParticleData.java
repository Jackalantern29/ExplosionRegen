package com.jackalantern29.explosionregen;

import com.jackalantern29.explosionregen.api.ExplosionParticle;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParticleData implements Cloneable {
	private static final Map<ExplosionParticle, ParticleData> VANILLA_PARTICLES = new HashMap<>();

	private ExplosionParticle particle;
	private ExplosionPhase phase;
	
	private int amount = 5;
	private float offsetX = 0.1f;
	private float offsetY = 0.1f;
	private float offsetZ = 0.1f;
	private double speed = 0d;
	private Object data;
	
	private boolean canDisplay = true;
	private int displayAmount = 1;
	private ParticlePlayAt playAt = ParticlePlayAt.ANYWHERE;
	
	public ParticleData(ParticleData particle, ExplosionPhase phase) {
		this(particle.getParticle(), phase, particle.getCanDisplay(), particle.getAmount(), particle.getOffsetX(), particle.getOffsetY(), particle.getOffsetY(), particle.getOffsetZ());
	}
	public ParticleData(ParticleData particle, ExplosionPhase phase, boolean canDisplay) {
		this(particle, phase);
		setCanDisplay(canDisplay);
	}
	public ParticleData(ExplosionParticle particle, ExplosionPhase phase) {
		this.particle = particle;
		this.phase = phase;
	}
	public ParticleData(ExplosionParticle particle, ExplosionPhase phase, boolean canDisplay, int amount) {
		this(particle, phase);
		setCanDisplay(canDisplay);
		setAmount(amount);
	}
	public ParticleData(ExplosionParticle particle, ExplosionPhase phase, boolean canDisplay, int amount, float offsetX, float offsetY, float offsetZ) {
		this(particle, phase, canDisplay, amount);
		setOffsetX(offsetX);
		setOffsetY(offsetY);
		setOffsetZ(offsetZ);
	}
	public ParticleData(ExplosionParticle particle, ExplosionPhase phase, boolean canDisplay, int amount, float offsetX, float offsetY, float offsetZ, float speed) {
		this(particle, phase, canDisplay, amount, offsetX, offsetY, offsetZ);
		setSpeed(speed);
	}
	public ExplosionParticle getParticle() {
		return particle;
	}
	public void setParticle(ExplosionParticle particle) {
		this.particle = particle;
	}
	public ExplosionPhase getPhase() {
		return phase;
	}
	public int getAmount() {
		return amount;
	}
	public boolean getCanDisplay() {
		return canDisplay;
	}
	public void setCanDisplay(boolean canDisplay) {
		this.canDisplay = canDisplay;
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
		if(getCanDisplay())
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
    public ParticleData clone(ExplosionPhase phase) {
    	ParticleData data = clone();
    	data.phase = phase;
    	return data;
    }
    public ParticleData clone(boolean canDisplay) {
    	ParticleData data = clone();
    	data.setCanDisplay(canDisplay);
    	return data;
    }
    public ParticleData clone(ExplosionPhase phase, boolean canDisplay) {
    	ParticleData data = clone();
    	data.phase = phase;
    	data.setCanDisplay(canDisplay);
    	return data;
    }

    public static ParticleData getVanillaSettings(ExplosionParticle particle) {
		return getVanillaSettings(particle, false);
	}

	public static ParticleData getVanillaSettings(ExplosionParticle particle, boolean load) {
		ParticleData data;
		if(!load && VANILLA_PARTICLES.containsKey(particle))
			data = VANILLA_PARTICLES.get(particle);
		else {
			data = new ParticleData(particle, null);
			File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "particles" + File.separator + "vanilla", particle.toString().toLowerCase() + ".yml");
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			if(!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			LinkedHashMap<String, Object> map = new LinkedHashMap<>();
			map.put("amount", 5);
			map.put("offset.x", 0.1f);
			map.put("offset.y", 0.1f);
			map.put("offset.z", 0.1f);
			map.put("speed", 0f);
			boolean doSave = false;
			for(String key : new ArrayList<>(map.keySet())) {
				if(!config.contains(key)) { config.set(key, map.get(key));doSave = true;}
				map.remove(key);
			}
			if(doSave) {
				try {
					config.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			data.setAmount(config.getInt("amount", 5));
			data.setOffsetX((float)config.getDouble("offset.x", 0.1f));
			data.setOffsetY((float)config.getDouble("offset.y", 0.1f));
			data.setOffsetZ((float)config.getDouble("offset.z", 0.1f));
			data.setSpeed((float)config.getDouble("speed", 0.0f));
			VANILLA_PARTICLES.put(particle, data);
		}
		return data;
	}
}
