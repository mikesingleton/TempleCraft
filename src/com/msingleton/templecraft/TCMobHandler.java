package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;

import com.msingleton.templecraft.games.Game;

public class TCMobHandler {
	
	public static void SpawnMobs(Game game, Location loc, CreatureType mob) {
		//for (int i = 0; i < playerSet.size(); i++)
	    //{
			LivingEntity e = game.world.spawnCreature(loc,mob);
			
			if(e == null)
				return;
	        
	        Random r = new Random();
	        if(TempleCraft.method != null && (TempleManager.mobGoldMin + TempleManager.mobGoldRan) != 0 && r.nextInt(3) == 0){
	        	game.mobGoldMap.put(e.getEntityId(), r.nextInt(TempleManager.mobGoldRan)+TempleManager.mobGoldMin);
	        }
	        
	        if(!(e instanceof Creature))
	        	return;
	        
	        // Grab a random target.
	        Creature c = (Creature) e;
	        c.setTarget(TCUtils.getClosestPlayer(game, e));
	    //}
	}
	
	public static CreatureType getRandomCreature() {
		int dZombies, dSkeletons, dSpiders, dCreepers, dWolves;
		dZombies = 5;
		dSkeletons = dZombies + 5;
		dSpiders = dSkeletons + 5;
		dCreepers = dSpiders + 5;
		dWolves = dCreepers + 5;
		
		CreatureType mob;
		
		int ran = new Random().nextInt(dWolves);
		if      (ran < dZombies)   mob = CreatureType.ZOMBIE;
	    else if (ran < dSkeletons) mob = CreatureType.SKELETON;
	    else if (ran < dSpiders)   mob = CreatureType.SPIDER;
	    else if (ran < dCreepers)  mob = CreatureType.CREEPER;
	    else if (ran < dWolves)    mob = CreatureType.WOLF;
	    else return null;
		
		return mob;
	}
}
