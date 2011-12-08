package com.msingleton.templecraft.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.games.Arena;

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
    	if (TCUtils.isTCEditWorld(event.getEntity().getWorld())){
    		if(event.getEntity() instanceof Player){
    			Player p = (Player)event.getEntity();
    			event.setCancelled(true);
    			p.setFireTicks(0);
    			return;
    		}
    	}
    	
    	if (!TCUtils.isTCWorld(event.getEntity().getWorld()))
            return;
    	
    	if (event.getEntity() instanceof Player){
    		Player p = (Player)event.getEntity();
    		Game game = TempleManager.templePlayerMap.get(p).currentGame;
    		// If the player is dead or the game isn't running, the player can't take damage
    		if(game != null && (game.deadSet.contains(p) || !game.isRunning)){
    			p.setFireTicks(0);
    			event.setCancelled(true);
    			return;
    		}
 
    		if(game instanceof Arena){
    			// Increases the damage mobs do over time for Arena mode
    			event.setDamage((int) (event.getDamage()*((Arena)game).getDamageMultiplyer()));
    		}
    	}
    	
        if (event instanceof EntityDamageByEntityEvent) {
	        EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
	        
	        Entity entity = sub.getDamager();
	        Entity entity2 = sub.getEntity();

	        int id = entity2.getEntityId();
	        
	        if(entity instanceof Projectile)
	        	entity = ((Projectile) entity).getShooter();
	        
	        if(entity instanceof Player && entity2 instanceof Player){
	        	TemplePlayer tp1 = TempleManager.templePlayerMap.get((Player)entity);
	        	TemplePlayer tp2 = TempleManager.templePlayerMap.get((Player)entity2);
	        	// Players on the same team can't hurt each other
	        	if(tp1.team != -1 && tp1.team == tp2.team){
	        		event.setCancelled(true);
	        		return;
	        	}
	        }
	        
	        if(entity2 instanceof LivingEntity && ((LivingEntity)entity2).getHealth() > 0){
	        	Game game = TCUtils.getGame(entity);
	        	if(game == null)
	        		game = TCUtils.getGame(entity2);
	        	if(game == null)
	        		return;
	        	game.lastDamager.remove(id);
        		game.lastDamager.put(id, entity);
	        }
	    }
    }

	/**
     * Clears all player/monster drops on death.
     */
    public void onEntityDeath(EntityDeathEvent event)
    {        
    	if (!TCUtils.isTCWorld(event.getEntity().getWorld()))
            return;
    	
    	// If a living entity dies in a TempleWorld, clear drops and such
    	
        if (event.getEntity() instanceof LivingEntity)
        {
            LivingEntity e = (LivingEntity) event.getEntity();
            
            event.getDrops().clear();
            
            Game game;
            Entity lastDamager;
            if (e instanceof Player)
            {        
                Player p = (Player) e;
                
                if (!TempleManager.playerSet.contains(p))
                    return;
                
                TemplePlayer tp = TempleManager.templePlayerMap.get(p);
                game = tp.currentGame;
                
                lastDamager = game.lastDamager.remove(event.getEntity().getEntityId());
                game.playerDeath(p);
            } else {
            	// If a monster died
	            game = TCUtils.getGame(e);
	            lastDamager = game.lastDamager.remove(e.getEntityId());
            }
            
            if(game != null && lastDamager != null)
            	game.onEntityKilledByEntity(e,lastDamager);
        }
    }
}