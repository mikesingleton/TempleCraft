package com.msingleton.templecraft;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Handles the disabled commands.
 */
public class TCDisabledCommands extends PlayerListener
{
    private TempleCraft plugin;
    
    public TCDisabledCommands(TempleCraft instance)
    {
        plugin = instance;
    }
    
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        Player p = event.getPlayer();
        
        if (!TempleManager.playerSet.contains(p))
            return;
        
        String[] args = event.getMessage().split(" ");
        
        if (!plugin.DISABLED_COMMANDS.contains(event.getMessage().substring(1).trim()) &&
            !plugin.DISABLED_COMMANDS.contains(args[0]))
            return;
        
        event.setCancelled(true);
        TempleManager.tellPlayer(p, "You can't use that command in the arena!");
    }
}