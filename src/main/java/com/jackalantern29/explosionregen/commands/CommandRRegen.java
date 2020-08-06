package com.jackalantern29.explosionregen.commands;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.ERExplosion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;

public class CommandRRegen implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("explosionregen.command.rregen")) {
            sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
            return true;
        }
        int count = 0;
        for(ERExplosion explosion : ExplosionRegen.getExplosionMap().getExplosions()) {
            explosion.regenerateAll();
            count++;
        }
        if(count == 0)
          sender.sendMessage("§aNo explosions were regenerated.");
        else
            sender.sendMessage("§aRegenerated " + count + " explosion" + (count > 1 ? "s" : "") + ".");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
