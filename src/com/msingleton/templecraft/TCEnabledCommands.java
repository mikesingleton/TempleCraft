package com.msingleton.templecraft;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Handles the disabled commands.
 */
public class TCEnabledCommands extends PlayerListener
{
    private TempleCraft plugin;
    
    public TCEnabledCommands(TempleCraft instance)
    {
        plugin = instance;
    }
    
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event)
    {
        Player p = event.getPlayer();
        TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        
        if (!TempleManager.playerSet.contains(p))
            return;
        
        String msg = event.getMessage();
        String[] args = msg.split(" ");
        
        if(!tp.tempSet.isEmpty() && tp.tempSet.contains(msg)){
        	tp.tempSet.remove(msg);
        	return;
        }
        
        if (plugin.ENABLED_COMMANDS.contains(msg.trim()) || 
        	plugin.ENABLED_COMMANDS.contains(args[0]))
            return;
        
        event.setCancelled(true);
        TempleManager.tellPlayer(p, "You can't use that command in the temple!");
    }
}