package com.msingleton.templecraft;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;


import com.iConomy.iConomy;

/**
 * This listener acts as a type of death-listener.
 * When a player is sufficiently low on health, and the next
 * damaging blow will kill them, they are teleported to the
 * spectator area, they have their hearts replenished, and all
 * their items are stripped from them.
 * By the end of the arena session, the rewards are given.
 */
// TO-DO: Perhaps implement TeamFluff's respawn-packet-code.
public class TCDamageListener extends EntityListener
{    
	
    public TCDamageListener(TempleCraft instance)
    {
    }
            
    public void onEntityDamage(EntityDamageEvent event)
    {
    	if (event.getEntity().getWorld().getName().contains("EditWorld_")){
    		if(event.getEntity() instanceof Player){
    			Player p = (Player)event.getEntity();
    			event.setCancelled(true);
    			p.setFireTicks(0);
    			return;
    		}
    	}
    	
    	if (!event.getEntity().getWorld().equals(TempleManager.world))
            return;
    	
    	if (event.getEntity() instanceof Player){
    		Player p = (Player)event.getEntity();
    		Temple temple = TempleManager.templePlayerMap.get(p).currentTemple;
    		if(temple != null && temple.deadSet.contains(p)){
    			p.setFireTicks(0);
    			return;
    		}
    	}
    	
        if (event instanceof EntityDamageByEntityEvent) {
	        EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
	        
	        Entity entity = sub.getDamager();
	        Entity entity2 = sub.getEntity();

	        int id = entity2.getEntityId();
	        
	        if(entity2 instanceof LivingEntity && ((LivingEntity)entity2).getHealth() > 0){
	        	Temple temple = TCUtils.getTemple(entity);
	        	if(temple == null)
	        		temple = TCUtils.getTemple(entity2);
	        	if(temple == null)
	        		return;
	        	temple.lastDamager.remove(id);
        		temple.lastDamager.put(id, entity);
	        }
	    }
    }

	/**
     * Clears all player/monster drops on death.
     */
    public void onEntityDeath(EntityDeathEvent event)
    {        
    	if (!event.getEntity().getWorld().equals(TempleManager.world))
            return;
    	
        // If player, call player death and such.
        if (event.getEntity() instanceof Player)
        {        
            Player p = (Player) event.getEntity();
            TemplePlayer tp = TempleManager.templePlayerMap.get(p);
            
            if (!TempleManager.playerSet.contains(p))
                return;
            
            event.getDrops().clear();
            Temple temple = tp.currentTemple;
            temple.playerDeath(p);
        }
        // If monster, remove from monster set
        else if (event.getEntity() instanceof LivingEntity)
        {
            LivingEntity e = (LivingEntity) event.getEntity();
     
            Temple temple = TCUtils.getTemple(e);
            
            if(temple == null)
        		return;
            
            if (!temple.monsterSet.contains(e))
                return;
          
            Entity lastDamager = temple.lastDamager.remove(e.getEntityId());
            TCUtils.sendDeathMessage(e, lastDamager);
           	
           	if(lastDamager instanceof Player){
	           	TempleManager.templePlayerMap.get(lastDamager).roundMobsKilled++;
           		if(temple.mobGoldMap != null && temple.mobGoldMap.containsKey(e.getEntityId())){
	           		for(Player p : temple.playerSet){
	           			int gold = temple.mobGoldMap.get(e.getEntityId())/temple.playerSet.size();
	           			TempleManager.templePlayerMap.get(p).roundGold += gold;
	           			if(TempleCraft.iConomy != null)
	           				iConomy.getAccount(p.getName()).getHoldings().add(gold);
	           		}
	           	}
           	}

            event.getDrops().clear();
            temple.monsterSet.remove(e);
        }
    }
    
    /**
     * Prevents monsters from spawning inside the arena unless
     * it's running.
     */
    public void onCreatureSpawn(CreatureSpawnEvent event)
    {    	
    	if (!(event.getEntity() instanceof LivingEntity))
            return;
    	
    	Location loc = event.getLocation();
    	
    	if(loc.getWorld().getName().contains("EditWorld_")){
    		event.setCancelled(true);
    		return;
    	}
    	
    	LivingEntity e = (LivingEntity) event.getEntity();
    	if(loc.getWorld().equals(TempleManager.world)){
    		boolean result = true;
	    	for(Temple temple : TempleManager.templeSet)
	    		if(temple.isRunning)
					for(Location sploc : temple.mobSpawnpointSet)
						if(TCUtils.distance(loc, sploc) < 2){
    						result = false;
    						temple.monsterSet.add(e);
    						temple.mobSpawnpointMap.remove(sploc);
						}
	    	event.setCancelled(result);
    	}
    }
}