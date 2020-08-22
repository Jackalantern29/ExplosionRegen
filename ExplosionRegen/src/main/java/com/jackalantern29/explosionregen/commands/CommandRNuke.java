package com.jackalantern29.explosionregen.commands;

import com.jackalantern29.explosionregen.ExplosionRegen;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;

import java.util.List;

public class CommandRNuke implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!sender.hasPermission("explosionregen.command.rnuke")) {
            sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
            return true;
        }
        if(!(sender instanceof Player)) {
            sender.sendMessage("§7[§cExplosionRegen§7] Only players can use this command.");
            return true;
        }
        Player player = (Player)sender;
        Location location = player.getLocation().add(-6,0, -6);
        location.setY(128);
        for (int i = 0; i < 4; i++) {
            for(int i1 = 0; i1 < 4; i1++) {
                Location clone = location.clone();
                int x = i*3;
                int z = i1*3;
                clone.add(x, 0, z);
                TNTPrimed tnt = (TNTPrimed) clone.getWorld().spawnEntity(clone, EntityType.PRIMED_TNT);
                tnt.setFuseTicks(20*5);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
