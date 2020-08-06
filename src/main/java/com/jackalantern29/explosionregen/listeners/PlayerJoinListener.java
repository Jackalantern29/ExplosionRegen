package com.jackalantern29.explosionregen.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.ERProfileSettings;

public class PlayerJoinListener implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if(player.isOp()) {
			player.sendMessage("§7[§cExplosionRegen§7] §9This is an early build, so use this with caution. Report any bugs & issues you come across & share any ideas you would like to see.");
		}
		if(ExplosionRegen.getSettings().getAllowPlayerSettings()) {
			ERProfileSettings.get(player.getUniqueId());
		}
	}

}
