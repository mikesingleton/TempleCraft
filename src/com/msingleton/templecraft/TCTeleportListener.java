package com.msingleton.templecraft;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * This listener prevents players from warping out of the arena, if
 * they are in the arena session.
 */
// TO-DO: Fix the bug that causes the message when people get stuck in walls.
public class TCTeleportListener extends PlayerListener
{
    @SuppressWarnings("unused")
	private TempleCraft plugin;
    
    public TCTeleportListener(TempleCraft instance)
    {
        plugin = instance;
    }
    
    public void onPlayerTeleport(PlayerTeleportEvent event)
    {
        Player p = event.getPlayer();
        TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        
        if (!TempleManager.playerSet.contains(p))
            return;
        
        Temple temple = tp.currentTemple;
        
        if(temple == null)
        	return;
        
        Location to = event.getTo();
        Location from = event.getFrom();
        
        if ((!TCUtils.isTCWorld(from.getWorld()) && TCUtils.isTCWorld(to.getWorld())) || to.getWorld().equals(p.getWorld()))
        {
            return;
        }
        
        TempleManager.tellPlayer(p, "Can't leave world! To leave, type /tc leave");
        event.setCancelled(true);
    }
}