package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;


/**
 * This listener prevents players from sharing class-specific
 * items (read: cheating) before the arena session starts.
 */
// TO-DO: Merge with MASignListener and MAReadyListener into MALobbyListener
public class TCLobbyListener extends PlayerListener
{    
    public TCLobbyListener(TempleCraft instance)
    {
    }

    /**
     * Players can only drop items when the arena session has started.
     */
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Player p = event.getPlayer();
        TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        
        if (tp.currentClass == null)
            return;
            
        Temple temple = TCUtils.getTemple(p);
        
        if(temple != null && temple.playerSet.contains(p) && temple.isRunning)
       		return;
        
        TempleManager.tellPlayer(p, "No sharing class items when not in play!");
        event.setCancelled(true);
    }
    
    /**
     * Adds liquid blocks to the blockset when players empty their buckets.
     */
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
    {        	
    	
    	if (!TempleManager.playerSet.contains(event.getPlayer()))
            return;
        
        if (!TempleManager.isEnabled)
        {
            event.getBlockClicked().getFace(event.getBlockFace()).setTypeId(0);
            event.setCancelled(true);
            return;
        }

        Block liquid = event.getBlockClicked().getFace(event.getBlockFace());
        for(Temple temp : TempleManager.templeSet)
        	if(temp.playerSet.contains(event.getPlayer()))
        		temp.tempBlockSet.add(liquid);
    }
    
    /**
     * Checks if the player hits an iron block or a sign, or if the player
     * is trying to use an item.
     */
    public void onPlayerInteract(PlayerInteractEvent event)
    {        
        Player p = event.getPlayer();
        TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        
        Temple temple = tp.currentTemple;
        Action a = event.getAction();
        
        // Check if player is trying to use an item.
        if (tp.currentClass != null && (temple == null || !temple.isRunning) && ((a == Action.RIGHT_CLICK_AIR) || (a == Action.RIGHT_CLICK_BLOCK)))
        {
            event.setUseItemInHand(Result.DENY);
            TempleManager.tellPlayer(p, "No using class items when not in play!");
            event.setCancelled(true);
        }
        
        if(temple == null)
        	return;
        
        // Iron block
        if (event.hasBlock() && temple.lobbyBlockSet.contains(event.getClickedBlock()))
        {
        	if(!temple.isRunning){
	            if (tp.currentClass != null)
	            {
	                temple.tellPlayer(p, "You have been flagged as ready!");
	                temple.playerReady(p);
	            }
	            else
	            {
	                temple.tellPlayer(p, "You must first pick a class!");
	            }
        	} else {
        		Holdings balance = iConomy.getAccount(p.getName()).getHoldings();
        		if(balance.hasEnough(temple.JoinCost)){
        			if (tp.currentClass != null)
    	            {
        				temple.readySet.add(p);
        				p.teleport(temple.templeLoc);
        				String msg = ChatColor.GOLD + "" + temple.JoinCost+" gold"+ChatColor.WHITE+" has been subtracted from your account.";
        				temple.tellPlayer(p, msg);
        				balance.subtract(temple.JoinCost);
    	            }
        			else
    	            {
    	                temple.tellPlayer(p, "You must first pick a class!");
    	            }
        		} else {
        			TempleManager.tellPlayer(p, "You don't have enough gold to rejoin.");
        		}
        	}
            return;
        }
        
        // Sign is handled by playerListener
    }
}