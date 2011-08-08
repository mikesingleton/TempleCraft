package com.msingleton.templecraft;

import java.util.HashMap;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;


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
    	Temple temple = TCUtils.getTemple(event.getEntity());
    	
        /* This could be done by simply cancelling the event, but that
         * also cancels the explosion animation. This is a workaround. */
        if (temple == null)
            return;
        
        // Only apply to creepers
        if(!(event.getEntity() instanceof LivingEntity))
        	return;
        
        // Don't drop any blocks from the explosion.
        event.setYield(0);
        
        // Store the blocks and their values in the map.
        final HashMap<Block,Integer> blockMap = new HashMap<Block,Integer>();
        
        for (Block b : event.blockList())
        {
            // Doors are wonky, so don't store them. Just smile and wave, and remove from set.
            if (b.getType() == Material.WOODEN_DOOR || b.getType() == Material.IRON_DOOR_BLOCK || b.getType() == Material.CAKE_BLOCK)
            {
                temple.blockSet.remove(b);
                continue;
            }
            
            // If a block is in the blockSet, make sure it drops "naturally" so Oddjob doesn't cry.
            if (temple.blockSet.remove(b))
            {
                temple.world.dropItemNaturally(b.getLocation(), new ItemStack(b.getTypeId(), 1));
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
        
        if(temple.expBuffer != null && !temple.expBuffer.isEmpty())
       		TCUtils.addXP(event.getEntity(), temple.expBuffer);
    }
    
    // Zombie/skeleton combustion from the sun.
    public void onEntityCombust(EntityCombustEvent event)
    {
    	Temple temple = TCUtils.getTemple(event.getEntity());
    	if(temple == null)
    		return;
    	
        if (temple.monsterSet.contains(event.getEntity()))
            event.setCancelled(true);
    }
    
    // Monsters losing their targets.
    public void onEntityTarget(EntityTargetEvent event)
    {
    	Temple temple = TCUtils.getTemple(event.getEntity());
    	if(temple == null)
    		return;
    	
        if (!temple.isRunning)
            return;
        
        if (!temple.monsterSet.contains(event.getEntity()))
            return;
        
        /*
        if (event.getReason() == TargetReason.FORGOT_TARGET)
            event.setTarget(TCPlayerListener.getClosestPlayer(event.getEntity()));
            
        if (event.getReason() == TargetReason.TARGET_DIED)
            event.setTarget(MASpawnThread.getClosestPlayer(event.getEntity()));
            
        if (event.getReason() == TargetReason.CLOSEST_PLAYER)
            event.setTarget(MASpawnThread.getClosestPlayer(event.getEntity()));
        */
    }
}