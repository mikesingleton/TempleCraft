package com.msingleton.templecraft;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import com.msingleton.templecraft.games.Adventure;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.games.Zombies;

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
    		if(game != null && game.deadSet.contains(p)){
    			p.setFireTicks(0);
    			return;
    		}
 
    		if(game instanceof Zombies){
    			// Increases the damage zombies do over time for zombies mode
    			event.setDamage((int) (event.getDamage()*((Zombies)game).getDamageMultiplyer()));
    			((Zombies)game).hurtPlayer(p);
    		}
    	}
    	
        if (event instanceof EntityDamageByEntityEvent) {
	        EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
	        
	        Entity entity = sub.getDamager();
	        Entity entity2 = sub.getEntity();

	        int id = entity2.getEntityId();
	        
	        if(entity instanceof Projectile){
	        	entity = ((Projectile) entity).getShooter();
	        }
	        
	        //No Friendly Fire in Zombies
	        if(entity instanceof Player && entity2 instanceof Player){
	        	Player p = (Player)event.getEntity();
	        	Game game = TempleManager.templePlayerMap.get(p).currentGame;
		        if(game instanceof Zombies){
	    			event.setCancelled(true);
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
    	
        // If player, call player death and such.
        if (event.getEntity() instanceof Player)
        {        
            Player p = (Player) event.getEntity();
            TemplePlayer tp = TempleManager.templePlayerMap.get(p);
            
            if (!TempleManager.playerSet.contains(p))
                return;
            
            event.getDrops().clear();
            Game game = tp.currentGame;
            game.playerDeath(p);
        }
        // If monster, remove from monster set
        else if (event.getEntity() instanceof LivingEntity)
        {
            LivingEntity e = (LivingEntity) event.getEntity();
     
            Game game = TCUtils.getGame(e);
            
            if(game == null)
        		return;
            
            if (!game.monsterSet.contains(e))
                return;
          
            if(game instanceof Adventure){
	            Entity lastDamager = game.lastDamager.remove(e.getEntityId());
	            TCUtils.sendDeathMessage(e, lastDamager);
	           	
	           	if(lastDamager instanceof Player){
		           	TempleManager.templePlayerMap.get(lastDamager).roundMobsKilled++;
	           		if(game.mobGoldMap != null && game.mobGoldMap.containsKey(e.getEntityId())){
		           		for(Player p : game.playerSet){
		           			int gold = game.mobGoldMap.get(e.getEntityId())/game.playerSet.size();
		           			TempleManager.templePlayerMap.get(p).roundGold += gold;
		           			if(TempleCraft.method != null)
		           				TempleCraft.method.getAccount(p.getName()).add(gold);
		           		}
		           	}
	           	}
            }

            event.getDrops().clear();
            game.monsterSet.remove(e);
            // Starts a new round if all the round's zombies are killed for zombies mode
            if(game instanceof Zombies)
            	if(game.monsterSet.isEmpty())
            		((Zombies)game).nextRound();
        }
    }
}