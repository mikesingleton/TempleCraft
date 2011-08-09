package com.msingleton.templecraft;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * This listener acts when a player is kicked or disconnected
 * from the server. If 15 seconds pass, and the player hasn't
 * reconnected, the player is forced to leave the arena.
 */
public class TCDisconnectListener extends PlayerListener
{
    public TCDisconnectListener(TempleCraft instance)
    {
    }
    
    public void onPlayerQuit(PlayerQuitEvent event)
    {
    	handleEvent(event.getPlayer());
    }
    
    public void onPlayerKick(PlayerKickEvent event)
    {
    	handleEvent(event.getPlayer());
    }
    
    private void handleEvent(Player p) {    
    	if(!TempleManager.isEnabled)
    		return;
        if(TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.restorePlayerInventory(p);
        if(!TempleManager.templePlayerMap.containsKey(p))
        	TempleManager.templePlayerMap.put(p, new TemplePlayer(p));   
      	TempleManager.templePlayerMap.get(p).saveData();
        if (TempleManager.playerSet.contains(p))
            TempleManager.playerLeave(p);
	}

	public void onPlayerJoin(PlayerJoinEvent event)
    {		
    	final Player p = event.getPlayer();
        handleEvent(p);
        
        if (!TempleManager.checkUpdates)
            return;
        
        if (!p.isOp())
            return;
        
        TempleManager.server.getScheduler().scheduleSyncDelayedTask(TempleManager.plugin,
            new Runnable()
            {
                public void run()
                {
                    TCUtils.checkForUpdates(p, false);
                }
            }, 100);
    }
}