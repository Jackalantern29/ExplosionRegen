package com.jackalantern29.explosionregen;

import com.jackalantern29.explosionregen.api.ExplosionParticle;
import com.jackalantern29.explosionregen.api.enums.UpdateType;
import com.jackalantern29.explosionregen.api.particledata.DustColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class BukkitMethods {
    private static final String MINECRAFT_PACKAGE;
    private static final String CRAFTBUKKIT_PACKAGE;

    private static final MethodHandle PLAY_PARTICLE;

    private static final MethodHandle ENUM_PARTICLE_VALUE_OF;
    private static final MethodHandle PARTICLE_VALUE_OF;
    private static final MethodHandle GET_HANDLE;
    private static final MethodHandle PLAYER_CONNECTION;
    private static final MethodHandle SEND_PACKET;

    private static final MethodHandle CREATE_BLOCK_DATA_STRING;
    private static final MethodHandle CREATE_BLOCK_DATA_MATERIAL;
    private static final MethodHandle CREATE_BLOCK_DATA_MATERIAL_STRING;
    private static final MethodHandle GET_BLOCK_DATA;
    private static final MethodHandle SET_BLOCK_DATA;

    private static final Enum[] PARTICLES;
    static {
        MINECRAFT_PACKAGE = "net.minecraft.server." + UpdateType.getNMSVersion();
        CRAFTBUKKIT_PACKAGE = "org.bukkit.craftbukkit." + UpdateType.getNMSVersion();
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

        MethodHandle playParticle = null;
        MethodHandle enumParticleValueOf = null;
        MethodHandle particleValueOf = null;
        MethodHandle getHandle = null;
        MethodHandle playerConnection = null;
        MethodHandle sendPacket = null;
        Enum[] particles = null;
        try {
            if(UpdateType.isPostUpdate(UpdateType.COMBAT_UPDATE) && getClass("org.bukkit.Particle") != null) {
                playParticle = publicLookup.findVirtual(Player.class, "spawnParticle", MethodType.methodType(void.class, Particle.class, Location.class, int.class, double.class, double.class, double.class, double.class, Object.class));
                particles = (Enum[]) getClass("org.bukkit.Particle").getEnumConstants();
                particleValueOf = publicLookup.findStatic(getClass("org.bukkit.Particle"), "valueOf", MethodType.methodType(getClass("org.bukkit.Particle"), String.class));
            } else {
                Class<?> packetClazz = Class.forName(MINECRAFT_PACKAGE + ".PacketPlayOutWorldParticles");
                Class<?> enumPartClazz = Class.forName(MINECRAFT_PACKAGE + ".EnumParticle");


                playParticle = publicLookup.findConstructor(packetClazz, MethodType.methodType(void.class, enumPartClazz, boolean.class, float.class, float.class, float.class, float.class, float.class, float.class, float.class, int.class, int[].class));
                particles = (Enum[]) enumPartClazz.getEnumConstants();
                enumParticleValueOf = publicLookup.findStatic(enumPartClazz, "valueOf", MethodType.methodType(enumPartClazz, String.class));

            }
            Class<?> entityPlayer = Class.forName(MINECRAFT_PACKAGE + ".EntityPlayer");
            Class<?> connection = Class.forName(MINECRAFT_PACKAGE + ".PlayerConnection");
            Class<?> craftPlayer = Class.forName(CRAFTBUKKIT_PACKAGE + ".entity.CraftPlayer");
            Class<?> packet = Class.forName(MINECRAFT_PACKAGE + ".Packet");
            getHandle = publicLookup.findVirtual(craftPlayer, "getHandle", MethodType.methodType(entityPlayer));
            playerConnection = publicLookup.findGetter(entityPlayer, "playerConnection", connection);
            sendPacket = publicLookup.findVirtual(connection, "sendPacket", MethodType.methodType(void.class, packet));

        } catch (NoSuchMethodException | NoClassDefFoundError | ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        MethodHandle createBlockDataString = null;
        MethodHandle createBlockDataMaterial = null;
        MethodHandle createBlockDataMaterialString = null;
        MethodHandle getBlockData = null;
        MethodHandle setBlockData = null;
        if(getClass("org.bukkit.block.data.BlockData") != null && UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE)) {
            try {
                createBlockDataString = publicLookup.findStatic(Bukkit.class,"createBlockData", MethodType.methodType(BlockData.class, String.class));
                createBlockDataMaterial = publicLookup.findStatic(Bukkit.class, "createBlockData", MethodType.methodType(BlockData.class, Material.class));
                createBlockDataMaterialString = publicLookup.findStatic(Bukkit.class, "createBlockData", MethodType.methodType(BlockData.class, Material.class, String.class));
                getBlockData = publicLookup.findVirtual(BlockState.class, "getBlockData", MethodType.methodType(BlockData.class));
                setBlockData = publicLookup.findVirtual(BlockState.class, "setBlockData", MethodType.methodType(void.class, BlockData.class));
            } catch (NoSuchMethodException | IllegalAccessException | NoClassDefFoundError e) {
                e.printStackTrace();
            }
        }
        PLAY_PARTICLE = playParticle;
        ENUM_PARTICLE_VALUE_OF = enumParticleValueOf;
        PARTICLE_VALUE_OF = particleValueOf;
        GET_HANDLE = getHandle;
        PLAYER_CONNECTION = playerConnection;
        SEND_PACKET = sendPacket;
        CREATE_BLOCK_DATA_STRING = createBlockDataString;
        CREATE_BLOCK_DATA_MATERIAL = createBlockDataMaterial;
        CREATE_BLOCK_DATA_MATERIAL_STRING = createBlockDataMaterialString;
        GET_BLOCK_DATA = getBlockData;
        SET_BLOCK_DATA = setBlockData;
        PARTICLES = particles;
    }

    private static Class<?> getClass(String className) {
        try {
            return Class.forName(className);
        } catch(Exception ignored) {
            return null;
        }
    }
    public static void spawnParticle(Player player, ExplosionParticle particle, Location location, int amount, double offsetX, double offsetY, double offsetZ, double extra, Object data) {
        if(UpdateType.isPostUpdate(UpdateType.COMBAT_UPDATE)) {
            try {
                Object particleData = null;
                if(data instanceof DustColor) {
                    DustColor color = (DustColor)data;
                    if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE))
                        particleData = new Particle.DustOptions(color.getColor(), color.getSize());
                    else {
                        offsetX = color.getRed();
                        offsetY = color.getGreen();
                        offsetZ = color.getBlue();
                    }
                }
                PLAY_PARTICLE.invoke(player, PARTICLE_VALUE_OF.invoke(particle.toString().toUpperCase()), location, amount, offsetX, offsetY, offsetZ, extra, particleData);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            try {
                if(data instanceof DustColor) {
                    DustColor color = (DustColor)data;
                    offsetX = color.getRed();
                    offsetY = color.getGreen();
                    offsetZ = color.getBlue();
                }
                Object enumParticle = ENUM_PARTICLE_VALUE_OF.invoke(particle.toString().toUpperCase());
                Object particlePacket = PLAY_PARTICLE.invoke(enumParticle, false, (float)location.getX(), (float)location.getY(), (float)location.getZ(), (float)offsetX, (float)offsetY, (float)offsetZ, (float)extra, amount);
                Object handle = GET_HANDLE.invoke(player);
                Object connection = PLAYER_CONNECTION.invoke(handle);
                SEND_PACKET.invoke(connection, particlePacket);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
    public static Enum[] getParticles() {
        return PARTICLES;
    }

    public static BlockData createBlockData(String data) {
        try {
            return (BlockData) CREATE_BLOCK_DATA_STRING.invoke(data);
        } catch (Throwable throwable) {
            return null;
        }
    }
    public static BlockData createBlockData(Material material) {
        try {
            return (BlockData) CREATE_BLOCK_DATA_MATERIAL.invoke(material);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static BlockData createBlockData(Material material, String data) {
        try {
            return (BlockData) CREATE_BLOCK_DATA_MATERIAL_STRING.invoke(material, data);
        } catch (Throwable throwable) {
            return null;
        }
    }

    public static BlockData getBlockData(BlockState block) {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE) && block != null) {
            try {
                return (BlockData) GET_BLOCK_DATA.invoke(block);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        } else
            return null;
    }

    public static void setBlockData(BlockState block, BlockData data) {
        if(UpdateType.isPostUpdate(UpdateType.AQUATIC_UPDATE) && block != null) {
            try {
                SET_BLOCK_DATA.invoke(block, data);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return;
            }
        } else
            return;
    }

}
