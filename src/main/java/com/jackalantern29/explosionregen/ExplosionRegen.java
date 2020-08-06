package com.jackalantern29.explosionregen;

import java.util.UUID;

import com.jackalantern29.explosionregen.commands.CommandRRegen;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.jackalantern29.explosionregen.api.ERExplosion;
import com.jackalantern29.explosionregen.api.ERExplosionMap;
import com.jackalantern29.explosionregen.api.ERSettings;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.commands.CommandRExplode;
import com.jackalantern29.explosionregen.commands.CommandRSettings;
import com.jackalantern29.explosionregen.commands.CommandRVersion;
import com.jackalantern29.explosionregen.listeners.EntityExplodeListener;
import com.jackalantern29.explosionregen.listeners.PlayerJoinListener;

public class ExplosionRegen extends JavaPlugin implements Listener {

	private static ExplosionRegen instance;
	private static ERExplosionMap explosions;
	private static ERSettings settings;
	public static UUID author = UUID.fromString("76763b6e-4804-4b7e-bfbd-5d87c72e7843");
	public void onEnable() {
		instance = this;
		explosions = new ERExplosionMap();
		settings = new ERSettings();
		EntityExplodeListener listener = new EntityExplodeListener();
		getServer().getPluginManager().registerEvents(listener, this);
		try {
			Class.forName("org.bukkit.event.block.BlockExplodeEvent");
			getServer().getPluginManager().registerEvents(listener.new BlockExplodeListener(), instance);
		} catch( ClassNotFoundException ignored) {}
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
		
		{
			getCommand("rexplode").setExecutor(new CommandRExplode());
			CommandRSettings rsettings = new CommandRSettings();
			getCommand("rsettings").setExecutor(rsettings);
			getCommand("rsettings").setTabCompleter(rsettings);
			getCommand("rversion").setExecutor(new CommandRVersion());
			getCommand("rregen").setExecutor(new CommandRRegen());
		}
	}
	
	public void onDisable() {
		for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
			settings.save();
		}
		for(ERExplosion explosions : explosions.getExplosions()) {
			explosions.regenerateAll();
		}
	}
	public static ExplosionRegen getInstance() {
		return instance;
	}
	public static ERExplosionMap getExplosionMap() {
		return explosions;
	}
	public static ERSettings getSettings() {
		return settings;
	}
	public static UUID getAuthor() {
		return author;
	}

}
