package com.jackalantern29.explosionregen.api;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.enums.ParticlePlayAt;

import xyz.xenondevs.particle.ParticleEffect;

public class ParticleData implements Cloneable {
	private static final Map<ParticleEffect, ParticleData> VANILLA_PARTICLES = new HashMap<>();
	
	private ParticleEffect particle;
	private ExplosionPhase phase;
	
	private int amount = 5;
	private float offsetX = 1f;
	private float offsetY = 1f;
	private float offsetZ = 1f;
	private float speed = 0f;
	private xyz.xenondevs.particle.data.ParticleData data = null;
	
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
	public ParticleData(ParticleEffect particle, ExplosionPhase phase) {
		this.particle = particle;
		this.phase = phase;
	}
	public ParticleData(ParticleEffect particle, ExplosionPhase phase, boolean canDisplay, int amount) {
		this(particle, phase);
		setCanDisplay(canDisplay);
		setAmount(amount);
	}
	public ParticleData(ParticleEffect particle, ExplosionPhase phase, boolean canDisplay, int amount, float offsetX, float offsetY, float offsetZ) {
		this(particle, phase, canDisplay, amount);
		setOffsetX(offsetX);
		setOffsetY(offsetY);
		setOffsetZ(offsetZ);
	}
	public ParticleData(ParticleEffect particle, ExplosionPhase phase, boolean canDisplay, int amount, float offsetX, float offsetY, float offsetZ, float speed) {
		this(particle, phase, canDisplay, amount, offsetX, offsetY, offsetZ);
		setSpeed(speed);
	}
	public ParticleEffect getParticle() {
		return particle;
	}
	public void setParticle(ParticleEffect particle) {
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
	public float getSpeed() {
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
	public xyz.xenondevs.particle.data.ParticleData getData() {
		return data;
	}
	public void setData(xyz.xenondevs.particle.data.ParticleData data) {
		this.data = data;
	}
	public void playParticle(Location location) {
		if(getCanDisplay()) {
			for (int i = 0; i < displayAmount; i++)
				particle.display(location, offsetX, offsetY, offsetZ, speed, amount, data, Bukkit.getOnlinePlayers());
		}
	}
	public void playParticle(Location location, Player player) {
		if(getCanDisplay())
			for (int i = 0; i < displayAmount; i++)
				particle.display(location, offsetX, offsetY, offsetZ, speed, amount, data, player);
//		if(ERMCUpdateType.isPostUpdate(ERMCUpdateType.COMBAT_UPDATE)) {
//			try {
//				player.getClass().getMethod("spawnParticle", Particle.class, Location.class, int.class, double.class, double.class, double.class, double.class, Object.class).invoke(player, particle, location, count, offsetX, offsetY, offsetZ, extra, data);
//			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
//					| NoSuchMethodException | SecurityException e) {
//				e.printStackTrace();
//			}		
//		} else {
//			try {
//				Class<?> packetClazz = Class.forName("net.minecraft.server." + ERMCUpdateType.getNMSVersion() + ".PacketPlayOutWorldParticles");
//				Class<?> enumPartClazz = Class.forName("net.minecraft.server." + ERMCUpdateType.getNMSVersion() + ".EnumParticle");
//				Constructor<?> packetConst = packetClazz.getConstructor(enumPartClazz, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class);
//				Object part = enumPartClazz.getMethod("valueOf", String.class).invoke(null, effect.toString());
//				Object packet = packetConst.newInstance(part, false, (float)location.getBlockX(), (float)location.getBlockY(), (float)location.getBlockZ(), (float)offsetX, (float)offsetY, (float)offsetZ, 0.0f, count, ((Object)new int[0]));
//				Class<?> craftPlayerClazz = Class.forName("org.bukkit.craftbukkit." + ERMCUpdateType.getNMSVersion() + ".entity.CraftPlayer");
//				Class<?> pClazz = player.getClass().asSubclass(craftPlayerClazz);
//				Object hand = pClazz.getMethod("getHandle").invoke(player);
//				Object conn = hand.getClass().getField("playerConnection").get(hand);
//				conn.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + ERMCUpdateType.getNMSVersion() + ".Packet")).invoke(conn, packet);
//			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e) {
//				e.printStackTrace();
//			}
//		}
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
	public static ParticleData getVanillaSettings(ParticleEffect particle) {
		VANILLA_PARTICLES.putIfAbsent(particle, new ParticleData(particle, null));
		return VANILLA_PARTICLES.get(particle);
	}
}
