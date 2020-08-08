package com.jackalantern29.explosionregen.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.jackalantern29.explosionregen.ExplosionRegen;

public class CommandRVersion implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("explosionregen.command.rversion")) {
			sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
			return true;
		}
		sender.sendMessage("§7[§cExplosionRegen§7] §fVersion " + ExplosionRegen.getInstance().getDescription().getVersion());
		return true;
	}
	
	

}
