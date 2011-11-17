package com.msingleton.templecraft.listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.games.Adventure;
import com.msingleton.templecraft.games.Arena;


/**
 * Prevents Creeper explosions from damaging the blocks of the
 * arena, zombies and skeletons from burning in the sun, and
 * monsters (mostly spiders) from losing their targets.
 */
public class TCMonsterListener extends EntityListener
{
    private TempleCraft plugin;
    
    public TCMonsterListener(TempleCraft instance)
    {
        plugin = instance;
    }
    
    /**
     * Handles all explosion events, also from TNT.
     */
    public void onEntityExplode(EntityExplodeEvent event)
    {
    	if(!TCUtils.isTCWorld(event.getLocation().getWorld()))
    		return;
        
    	Entity e = event.getEntity();
    	
    	Game game;
    	if(e instanceof Creature)
    		game = TCUtils.getGame(e);
    	else
    		game = TCUtils.getGameByWorld(event.getLocation().getWorld());
    	
    	if (game == null)
            return;
    	
        // Only apply to creepers in adventure mode
        if((game instanceof Adventure) && !(e instanceof LivingEntity)){
        	if(TempleManager.dropBlocks)
        		return;
        	
        	// TODO: Remove torches or levers attached to blocks exploding
        	// for(Block b : event.blockList()){
        	// }
        	
        	// Remove blocks without drops
        	for(Block b : event.blockList())
        		b.setTypeId(0);
        	event.setYield(0);
        }
    	
        /* This could be done by simply cancelling the event, but that
         * also cancels the explosion animation. This is a workaround. */
        // Don't drop any blocks from the explosion.
        event.setYield(0);
        
        // Store the blocks and their values in the map.
        final HashMap<Block,Integer> blockMap = new HashMap<Block,Integer>();
        
        for (Block b : event.blockList())
        {
            // Doors are wonky, so don't store them. Just smile and wave, and remove from set.
            if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.IRON_DOOR_BLOCK || b.getType() == Material.CAKE_BLOCK)
            {
                game.tempBlockSet.remove(b);
                continue;
            }
            
            // If a block is in the tempblockSet, make sure it drops "naturally".
            if (game.tempBlockSet.remove(b))
            {
                game.world.dropItemNaturally(b.getLocation(), new ItemStack(b.getTypeId(), 1));
                continue;
            }
            
            // If a block has extra data, store it as a fourth digit (thousand).
            int type = b.getTypeId() + (b.getData() * 1000);
            blockMap.put(b, type);
        }
        
        // Wait a couple of ticks, then rebuild the blocks.
        TempleManager.server.getScheduler().scheduleSyncDelayedTask(plugin,
            new Runnable()
            {
                public void run()
                {
                    for (Block b : blockMap.keySet())
                    {
                        int type = blockMap.get(b);
                        
                        // Modulo 1000 to get the actual type id.
                        b.getLocation().getBlock().setTypeId(type % 1000);
                        
                        /* If the type ID is greater than 1000, it means the block
                         * has extra data (stairs, levers, etc.). We subtract the
                         * block type data by dividing by 1000. Integer division
                         * always rounds by truncation. */
                        if (type > 1000)
                            b.getLocation().getBlock().setData((byte) (type / 1000));
                    }
                }
            }, TempleManager.repairDelay);
    }
    
    // Zombie/skeleton combustion from the sun.
    public void onEntityCombust(EntityCombustEvent event)
    {
    	Game game = TCUtils.getGame(event.getEntity());
    	if(game == null)
    		return;
    	
        if (game.monsterSet.contains(event.getEntity()))
            event.setCancelled(true);
    }
    
    // Monsters losing their targets.
    public void onEntityTarget(EntityTargetEvent event)
    {
    	Game game = TCUtils.getGame(event.getEntity());
    	if(game == null)
    		return;
    	
        if (!game.isRunning)
            return;
        
        if (!game.monsterSet.contains(event.getEntity()))
            return;
        
        if (event.getReason() == TargetReason.FORGOT_TARGET)
            event.setTarget(TCUtils.getClosestPlayer(game, event.getEntity()));
            
        if (event.getReason() == TargetReason.TARGET_DIED)
            event.setTarget(TCUtils.getClosestPlayer(game, event.getEntity()));
            
        if (event.getReason() == TargetReason.CLOSEST_PLAYER)
            event.setTarget(TCUtils.getClosestPlayer(game, event.getEntity()));
    }
    
    /**
     * Prevents monsters from spawning inside a temple unless
     * it's running.
     */
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {    	
    	Location loc = event.getLocation();
    	
    	// When in TCWorld, Only Spawn Custom Monsters
    	LivingEntity e = (LivingEntity) event.getEntity();
    	if(TCUtils.isTCWorld(loc.getWorld())){
    		if(event.getSpawnReason().equals(SpawnReason.CUSTOM)){
	    		Game game = TCUtils.getGameByWorld(loc.getWorld());
		    	game.monsterSet.add(e);
	    		if(game instanceof Arena)
	    			e.setHealth((int) ((Arena)game).getZombieHealth());
    		} else {
    			event.setCancelled(true);
    		}
    	}
    }
}