package com.msingleton.templecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInventoryEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;


import com.sk89q.worldedit.EditSession;

public class TCPlayerListener  extends PlayerListener{

	public TCPlayerListener(TempleCraft templeCraft) {		
	}
	
	public void onPlayerMove(PlayerMoveEvent event)
    {
		if(!TempleManager.isEnabled || TempleManager.templeSet.isEmpty())
			return;
		
		Player p = event.getPlayer();
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Temple temple = TCUtils.getTemple(p);
		
		if(temple == null)
			return;

		if(!temple.world.equals(TempleManager.world))
			return;
		
		if(!temple.isRunning)
			return;
		
		for(Location loc : temple.activeSpawnpoints){
			if(TCUtils.distance(loc, p.getLocation()) < 20){
				temple.SpawnMobs(loc);
				temple.inactiveSpawnpoints.add(loc);
			}
		}
		temple.activeSpawnpoints.removeAll(temple.inactiveSpawnpoints);
    }

	 public void onPlayerInteract(PlayerInteractEvent event){    
		 
		 if(!TempleManager.isEnabled)
				return;
		 
        Player p = event.getPlayer();
        TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        Action a = event.getAction();
			
        // Signs
        if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign){        
	        // Cast the block to a sign to get the text on it.
	        Sign sign = (Sign) event.getClickedBlock().getState();
	        handleSign(p, a, sign);
		 }
        
        if (!TempleManager.playerSet.contains(p))
            return;
        
        Temple temple = tp.currentTemple;
    	
        if(temple == null || !temple.isRunning)
        	return;
        
        // Gold block
        if (event.hasBlock() && temple.endBlockSet.contains(event.getClickedBlock()) && event.getClickedBlock().getTypeId() == 41)
        {
            if (temple.playerSet.contains(p))
            {
            	temple.readySet.add(p);
            	if(temple.readySet.equals(temple.playerSet)){
            		temple.endTemple();
            	} else {
            		temple.tellPlayer(p, "You are ready to leave!");
            	}
            }
            else
            {
                temple.tellPlayer(p, "WTF!? Get out of here!");
            }
            return;
        }
	}
	 
	private void handleSign(Player p, Action a, Sign sign) {
		// Check if the first line of the sign is a class name.
		String Line1 = sign.getLine(0);
        String Line2 = sign.getLine(1);
        String Line3 = sign.getLine(2).toLowerCase();
        String Line4 = sign.getLine(3).toLowerCase();
        if (!TempleManager.classes.contains(Line2)){
        	if(!Line1.equals("[TempleCraft]"))
        		return;
        	Temple temple = TCUtils.getTempleByName(Line2.toLowerCase());
        	if(temple != null){
        		if(Line3.contains("level")){
        			int level = Integer.parseInt(Line3.replace("level", "").replace("+","").trim());
        			temple.minLevel = level;
        			
        			for(String className : TempleManager.classes){
        				if(hasLevel(p, className,level)){
        					temple.playerJoin(p);
        					return;
        				}
        			}
        			TempleManager.tellPlayer(p, "You don't have any classes with a high enough level to join this temple.");
        		}
        	}
        } else {
        	if (a == Action.RIGHT_CLICK_BLOCK)
            {
                TempleManager.tellPlayer(p, "Punch the sign to select a class.");
                return;
            }
        	
        	// Set the player's class.
    		Temple temple = TCUtils.getTemple(p);
        	if(temple == null  || hasLevel(p, Line2, temple.minLevel)){
        		TempleManager.assignClass(p, Line2);
	        	TempleManager.tellPlayer(p, "You have chosen " + Line2 + " as your class!");
        	} else{
				TempleManager.tellPlayer(p, "Your "+Line2+" class isn't a high enough level to join this temple.");
        	}
			return;
        }
	}

	private boolean hasLevel(Player p, String className, int level) {
		return TempleManager.templePlayerMap.get(p).classLevel.get(className) >= level;
	}	
}
