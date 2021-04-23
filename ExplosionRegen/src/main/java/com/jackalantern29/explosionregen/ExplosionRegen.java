package com.jackalantern29.explosionregen;

import java.util.Collection;
import java.util.UUID;

import com.jackalantern29.explosionregen.api.*;
import com.jackalantern29.explosionregen.commands.*;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.jackalantern29.explosionregen.listeners.EntityExplodeListener;

public class ExplosionRegen extends JavaPlugin implements Listener {

	private static ExplosionRegen instance;
	private static ExplosionRegenSettings settings;
	public static UUID author = UUID.fromString("76763b6e-4804-4b7e-bfbd-5d87c72e7843");

	public void onEnable() {
		instance = this;
		settings = new ExplosionRegenSettings();

		if(!settings.doEnablePlugin()) {
			Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Option 'enable-plugin' in the config is set to false. Disabling plugin.");
			setEnabled(false);
			return;
		}

		EntityExplodeListener listener = new EntityExplodeListener();
		getServer().getPluginManager().registerEvents(listener, this);
		BukkitMethods.loadClass(Explosion.class);
		BukkitMethods.getClass("com.jackalantern29.explosionregen.api.ProfileSettings");
		BukkitMethods.getClass("com.jackalantern29.explosionregen.api.ExplosionParticle");
		{
			getCommand("rexplode").setExecutor(new CommandRExplode());
			CommandRSettings rsettings = new CommandRSettings();
			getCommand("rsettings").setExecutor(rsettings);
			getCommand("rsettings").setTabCompleter(rsettings);
			getCommand("rversion").setExecutor(new CommandRVersion());
			getCommand("rregen").setExecutor(new CommandRRegen());
			getCommand("rnuke").setExecutor(new CommandRNuke());
		}
	}
	
	public void onDisable() {
		for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
			settings.saveAsFile();
		}
		for(BlockSettings settings : BlockSettings.getBlockSettings()) {
			settings.saveAsFile();
		}
		for(Explosion explosions : Explosion.getActiveExplosions()) {
			explosions.regenerateAll();
		}
	}

	public static ExplosionRegen getInstance() {
		return instance;
	}

	public static ExplosionRegenSettings getSettings() {
		return settings;
	}

	public static UUID getAuthor() {
		return author;
	}

	public static Collection<Explosion> getActiveExplosions() {
		return Explosion.getActiveExplosions();
	}

	public CoreProtectAPI getCoreProtect() {
		Plugin p = getServer().getPluginManager().getPlugin("CoreProtect");
		if(p == null)
			return null;
		try {
			if(!(p instanceof CoreProtect))
				return null;
		} catch (NoClassDefFoundError e) {
			return null;
		}
		CoreProtectAPI CoreProtect = ((CoreProtect)p).getAPI();
		if(CoreProtect.isEnabled() == false)
			return null;
		return CoreProtect;
	}

	public GriefPrevention getGriefPrevention() {
		Plugin p = getServer().getPluginManager().getPlugin("GriefPrevention");
		if(p == null || !(p instanceof GriefPrevention))
			return null;
		GriefPrevention grief = GriefPrevention.instance;
		if(grief.isEnabled() == false)
			return null;
		return grief;
	}
}
