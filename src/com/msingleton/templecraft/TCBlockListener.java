package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
//import org.bukkit.event.block.BlockDamageEvent;


/**
 * This listener serves as a protection class. Blocks within
 * the arena region cannot be destroyed, and blocks can only
 * be placed by a participant in the current arena session.
 * Any placed blocks will be removed by the cleanup method in
 * TempleManager when the session ends.
 */
public class TCBlockListener extends BlockListener
{    
    public TCBlockListener(TempleCraft instance)
    {
    }

    /**
     * Prevents blocks from breaking if block protection is on.
     */
    public void onBlockBreak(BlockBreakEvent event)
    {	
    	Player p = event.getPlayer();
    	TemplePlayer tp = TempleManager.templePlayerMap.get(p);
    	Block b = event.getBlock();
    	
    	Temple temple = tp.currentTemple;
    	
    	if(temple == null)
    		return;
    	
    	if(p.getWorld().getName().contains("EditWorld_")){
    		TCUtils.expandRegion(temple, b.getLocation());
    		return;
    	}
    	
        if (!temple.isSetup || !temple.isRunning && event.getPlayer().isOp())
            return;
        
        if (temple.blockSet.remove(b) || TempleManager.breakable.contains(b.getType()))
            return;
        
        if (TCUtils.inRegion(temple.p1, temple.p2, b.getLocation()))
            event.setCancelled(true);
    }
    
    /**
     * Adds player-placed blocks to a set for removal and item
     * drop purposes. If the block is placed within the arena
     * region, cancel the event if protection is on.
     */
    public void onBlockPlace(BlockPlaceEvent event)
    {
    	Player p = event.getPlayer();
    	TemplePlayer tp = TempleManager.templePlayerMap.get(p);
    	Block b = event.getBlock();
    	
    	Temple temple = tp.currentTemple;
    	
    	if(temple == null)
    		return;
    	
    	if(p.getWorld().getName().contains("EditWorld_")){
    		TCUtils.expandRegion(temple, b.getLocation());
    		return;
    	}
    	
    	if (!temple.isSetup || !temple.isRunning && event.getPlayer().isOp())
            return;
        
        if (temple.isRunning && TempleManager.playerSet.contains(event.getPlayer()))
        {
            temple.blockSet.add(b);
            Material type = b.getType();
            
            // Make sure to add the top parts of doors.
            if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK)
                temple.blockSet.add(b.getRelative(0,1,0));
            
            return;
        }

        event.setCancelled(true);
    }
}