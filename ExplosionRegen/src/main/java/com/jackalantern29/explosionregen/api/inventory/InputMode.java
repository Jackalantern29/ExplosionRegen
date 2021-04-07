package com.jackalantern29.explosionregen.api.inventory;

import com.jackalantern29.explosionregen.ExplosionRegen;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class InputMode {
    private static HashMap<Player, InputMode> activeChatModes = new HashMap<>();

    private ChatFunction function;

    public InputMode(ChatFunction function) {
        this.function = function;
        Bukkit.getPluginManager().registerEvents(new ChatListen(), ExplosionRegen.getInstance());
    }

    public void setFunction(ChatFunction function) {
        this.function = function;
    }

    public ChatFunction getFunction() {
        return function;
    }

    private class ChatListen implements Listener {
        @EventHandler
        public void onChat(AsyncPlayerChatEvent event) {
            for(Map.Entry<Player, InputMode> entry : activeChatModes.entrySet()) {
                Player player = entry.getKey();
                InputMode mode = entry.getValue();
                if(event.getPlayer().equals(player)) {
                    event.setCancelled(true);
                    boolean pass = mode.getFunction().function(event.getMessage());
                    if(pass)
                        activeChatModes.remove(player);
                }
            }
        }
    }

    public interface ChatFunction {
        public boolean function(String message);
    }

    public static void setChatMode(Player player, InputMode inputMode) {
        player.closeInventory();
        activeChatModes.put(player, inputMode);
    }

    public static InputMode getChatMode(Player player) {
        return activeChatModes.get(player);
    }
}
