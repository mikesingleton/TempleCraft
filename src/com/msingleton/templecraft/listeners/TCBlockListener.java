package com.msingleton.templecraft.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.msingleton.templecraft.TCPermissionHandler;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.scoreboards.ScoreBoard;
//import org.bukkit.event.block.BlockDamageEvent;


/**
 * This listener serves as a protection class. Blocks within
 * the game world cannot be destroyed, and blocks can only
 * be placed by a participant in the current arena session.
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
    	
    	if(b.getState() instanceof Sign){
    		Sign sign = (Sign) b.getState();
    		ScoreBoard sb = TempleManager.SBManager.getScoreBoardBySign(sign);
    		if(sb != null){
    			if(TCPermissionHandler.hasPermission(p, "templecraft.placesigns")){
    				TempleManager.SBManager.deleteScoreBoard(sb);
    				p.getWorld().dropItemNaturally(b.getLocation(), new ItemStack(323,1));
    				return;
    			} else {
    				TempleManager.tellPlayer(p, "You do not have permission to break TCScoreBoards.");
    				event.setCancelled(true);
    				return;
    			}
    		}
    	}
    	
    	for(ScoreBoard sb : TempleManager.SBManager.scoreBoards){
    		if(sb.inRegion(b.getLocation())){
    			TempleManager.tellPlayer(p, "This Block is Protected by ScoreBoard"+sb.id);
				event.setCancelled(true);
				return;
    		}
    	}
    	
    	if(temple == null)
    		return;
    	
    	boolean cancel = true;
    	
    	Game game = tp.currentGame;
    	
    	if(game == null)
    		return;
    	
        if (!game.isRunning && event.getPlayer().isOp())
            cancel = false;
        
        if (TempleManager.breakable.contains(b.getTypeId()))
            cancel = false;
        
        if (game.tempBlockSet.remove(b))
        	return;
        
        if(TempleManager.dropBlocks && !cancel)
        	return;
        	
    	if(!cancel)
    		b.setTypeId(0);
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
    	
    	// if player places significant block while editing, record it
    	if(TCUtils.isTCEditWorld(p.getWorld())){
    		for(int i : Temple.coordBlocks)
    			if(b.getTypeId() == i)
    				temple.coordLocSet.add(b.getLocation());
    		return;
    	}
    	
    	Game game = tp.currentGame;
    	
    	if(game == null)
    		return;
    	
    	if (!game.isRunning && event.getPlayer().isOp())
            return;
        
        if (game.isRunning && TempleManager.playerSet.contains(event.getPlayer()))
        {
            game.tempBlockSet.add(b);
            Material type = b.getType();
            
            // Make sure to add the top parts of doors.
            if (type == Material.WOODEN_DOOR || type == Material.IRON_DOOR_BLOCK)
                game.tempBlockSet.add(b.getRelative(0,1,0));
            
            return;
        }

        event.setCancelled(true);
    }
    
    public void onSignChange(SignChangeEvent event){
    	Player p = event.getPlayer();
    	TemplePlayer tp = TempleManager.templePlayerMap.get(p);
    	
    	Temple temple = tp.currentTemple;
    	
    	if(temple == null){
    		if(event.getLine(0).equals("[TCSB]") || event.getLine(0).equals("[TCS]") || event.getLine(0).equals("[TC]") || event.getLine(0).equals("[TempleCraft]")){
    			if(!TCPermissionHandler.hasPermission(p, "templecraft.placesigns")){
    				TempleManager.tellPlayer(p, "You do not have permission to place temple entrances.");
    				event.setCancelled(true);
    				return;
    			}
    		}
    		if(event.getLine(0).equals("[TCSB]")){
    			Location loc      = event.getBlock().getLocation();
    			String templeName = event.getLine(1).toLowerCase();
    			String gameMode   = event.getLine(2).toLowerCase();
    			String type       = event.getLine(3).toLowerCase();
    			TempleManager.SBManager.newScoreBoard(loc, loc, templeName, gameMode, type);
    		}
	    }
    }
}