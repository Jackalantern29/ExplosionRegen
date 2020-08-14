package com.jackalantern29.explosionregen.api;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import net.minecraft.server.v1_8_R1.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.enums.ParticlePlayAt;

public class ParticleData implements Cloneable {
	private static final Map<Particle, ParticleData> VANILLA_PARTICLES = new HashMap<>();

	private static MethodHandle PLAY_PARTICLE = null;
	private static MethodHandle ENUM_PARTICLE = null;
	private static MethodHandle GET_HANDLE = null;
	private static MethodHandle PLAYER_CONNECTION = null;
	private static MethodHandle SEND_PACKET = null;

	static {
		MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		MethodHandle playParticle = null;
		MethodHandle enumParticle = null;
		MethodHandle getHandle = null;
		MethodHandle playerConnection = null;
		MethodHandle sendPacket = null;
		try {
			if(UpdateType.isPostUpdate(UpdateType.COMBAT_UPDATE)) {
				playParticle = publicLookup.findVirtual(Player.class, "spawnParticle", MethodType.methodType(void.class, Particle.class, Location.class, int.class, double.class, double.class, double.class, double.class, Object.class));
			} else {
				Class<?> packetClazz = Class.forName("net.minecraft.server." + UpdateType.getNMSVersion() + ".PacketPlayOutWorldParticles");
				Class<?> enumPartClazz = Class.forName("net.minecraft.server." + UpdateType.getNMSVersion() + ".EnumParticle");
				playParticle = publicLookup.findConstructor(packetClazz, MethodType.methodType(void.class, enumPartClazz, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class));
				enumParticle = publicLookup.findStatic(enumPartClazz, "valueOf", MethodType.methodType(enumPartClazz, String.class));
			}
			Class<?> entityPlayer = Class.forName("net.minecraft.server." + UpdateType.getNMSVersion() + ".EntityPlayer");
			Class<?> connection = Class.forName("net.minecraft.server." + UpdateType.getNMSVersion() + ".PlayerConnection");
			Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + UpdateType.getNMSVersion() + ".entity.CraftPlayer");
			Class<?> packet = Class.forName("net.minecraft.server." + UpdateType.getNMSVersion() + ".Packet");
			getHandle = publicLookup.findVirtual(craftPlayer, "getHandle", MethodType.methodType(entityPlayer));
			playerConnection = publicLookup.findGetter(entityPlayer, "playerConnection", connection);
			sendPacket = publicLookup.findVirtual(connection, "sendPacket", MethodType.methodType(void.class, packet));

		} catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}
		PLAY_PARTICLE = playParticle;
	}
	private Particle particle;
	private ExplosionPhase phase;
	
	private int amount = 5;
	private float offsetX = 1f;
	private float offsetY = 1f;
	private float offsetZ = 1f;
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
	public ParticleData(Particle particle, ExplosionPhase phase) {
		this.particle = particle;
		this.phase = phase;
	}
	public ParticleData(Particle particle, ExplosionPhase phase, boolean canDisplay, int amount) {
		this(particle, phase);
		setCanDisplay(canDisplay);
		setAmount(amount);
	}
	public ParticleData(Particle particle, ExplosionPhase phase, boolean canDisplay, int amount, float offsetX, float offsetY, float offsetZ) {
		this(particle, phase, canDisplay, amount);
		setOffsetX(offsetX);
		setOffsetY(offsetY);
		setOffsetZ(offsetZ);
	}
	public ParticleData(Particle particle, ExplosionPhase phase, boolean canDisplay, int amount, float offsetX, float offsetY, float offsetZ, float speed) {
		this(particle, phase, canDisplay, amount, offsetX, offsetY, offsetZ);
		setSpeed(speed);
	}
	public Particle getParticle() {
		return particle;
	}
	public void setParticle(Particle particle) {
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
		if(PLAY_PARTICLE == null)
			return;
		if(getCanDisplay())
			for (int i = 0; i < displayAmount; i++) {
				if(UpdateType.isPostUpdate(UpdateType.COMBAT_UPDATE)) {
					try {
						PLAY_PARTICLE.invoke(player, particle, location, amount, offsetX, offsetY, offsetZ, speed, data);
					} catch (Throwable throwable) {
						throwable.printStackTrace();
					}
				} else {
					try {
						Object particlePacket = PLAY_PARTICLE.invoke(ENUM_PARTICLE.invoke(particle.name()), false, (float)location.getX(), (float)location.getY(), (float)location.getZ(), offsetX, offsetY, offsetZ, speed, amount);
						Object handle = GET_HANDLE.invoke(player);
						Object connection = PLAYER_CONNECTION.invoke(handle);
						SEND_PACKET.invoke(connection, particlePacket);
					} catch (Throwable throwable) {
						throwable.printStackTrace();
					}
				}
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
	public static ParticleData getVanillaSettings(Particle particle) {
		ParticleData data;
		if(VANILLA_PARTICLES.containsKey(particle))
			data = VANILLA_PARTICLES.get(particle);
		else {
			data = new ParticleData(particle, null);
			File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "particles" + File.separator + "vanilla", particle.name().toLowerCase() + ".yml");
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
			map.put("offset.x", 1f);
			map.put("offset.y", 1f);
			map.put("offset.z", 1f);
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
			data.setOffsetX((float)config.getDouble("offset.x", 1.0f));
			data.setOffsetY((float)config.getDouble("offset.y", 1.0f));
			data.setOffsetZ((float)config.getDouble("offset.z", 1.0f));
			data.setSpeed((float)config.getDouble("speed", 0.0f));
			VANILLA_PARTICLES.put(particle, data);
		}
		return data;
	}
}
