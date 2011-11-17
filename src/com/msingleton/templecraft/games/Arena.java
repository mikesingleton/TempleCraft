package com.msingleton.templecraft.games;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.msingleton.templecraft.TCMobHandler;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;

public class Arena extends Game{
	//private static Set<Integer> transBlockSet = new HashSet<Integer>(Arrays.asList(0,8,9,10,27,28,30,31,34,37,38,39,40,50,55,59,63,64,65,66,68,69,70,71,72,75,76,77,78,83,90,93,94,96,321));
	private Set<Location> tempSpawns = new HashSet<Location>();
	private Timer gameTimer          = new Timer();
	private Set<TimerTask> taskSet   = new HashSet<TimerTask>();
	protected int roundNum;
	
	public Arena(String name, Temple temple, World world) {
		super(name, temple, world);
	}			
	
	public void startGame() {
		super.startGame();
		roundNum = 	0;
		nextRound();
	}

	public void endGame() {
		endTimer();
		TempleManager.tellAll("Arena game finished in: \""+temple.templeName+"\"");
		super.endGame();
	}
	
	public void playerDeath(Player p)
	{
		super.playerDeath(p);
		TempleManager.playerLeave(p);
	}
	
	public Location getPlayerSpawnLoc() {
		Random r = new Random();
		Location loc = null;
		for(Location l : startLocSet){
			if(loc == null)
				loc = l;
			else if(r.nextInt(startLocSet.size()) == 0)
				loc = l;
		}
		return loc;
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
		MOBSPAWN METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	public void endTimer(){
		gameTimer.cancel();
	}
	
	public void resetTimer(){
		for(TimerTask task : taskSet)
			task.cancel();
	}
	
	public void nextRound() {
		roundNum++;
		for(Player p : playerSet){
			TempleManager.tellPlayer(p, "Round "+roundNum);
			if(roundNum == 1){
				p.getInventory().addItem(new ItemStack(261,1));
				p.getInventory().addItem(new ItemStack(262,32));
				p.getInventory().addItem(new ItemStack(46,4));
			}
			if(roundNum%2 == 0)
				p.getInventory().addItem(new ItemStack(262,32));
			if(roundNum%4 == 0)
				p.getInventory().addItem(new ItemStack(46,4));
		}
		resetTimer();
		TimerTask spawnMobs = new TimerTask() {
			public void run() {
				Game game = TCUtils.getGameByName(gameName);
				for(Player p : playerSet){
					Random r = new Random();
					Set<Location> tempSet = new HashSet<Location>(getClosestSpawnpoints(p));
					for(Location loc : tempSet){
						for(int i = 0; i<1+r.nextInt(getMobLimit()/tempSet.size());i++){
							if(monsterSet.size() > getMobLimit())
								return;
							TCMobHandler.SpawnMobs(game, loc, TCMobHandler.getRandomCreature());
						}
		        	}	
				}
			}
		};
		TimerTask nextRound = new TimerTask() {
			public void run() {
				nextRound();
			}
		};
		taskSet.add(spawnMobs);
		taskSet.add(nextRound);
		gameTimer.schedule(spawnMobs, 5000);
		gameTimer.schedule(nextRound, 20000);
	}
	
	private int getMobLimit() {
		return 4+2*roundNum;
	}
	
	public double getDamageMultiplyer() {
		return 1+.1*roundNum;
	}

	public double getZombieHealth() {
		return 2+.6*roundNum;
	}
	
	private Set<Location> getClosestSpawnpoints(Player p) {
		Set<Location> result = new HashSet<Location>();
		if(mobSpawnpointSet.size() <= 1){
			result.addAll(mobSpawnpointSet);
		} else {
			findSpawnpointsByDistance(p.getLocation());
		}
		result.addAll(tempSpawns);
		tempSpawns.clear();
		return result;
	}

	private void findSpawnpointsByDistance(Location loc) {
		Map<Double,Location> distanceMap = getDistanceFromSpawnpoints(loc);
		for(int i = 5;i<=40;i+=5)
			for(Double d : distanceMap.keySet())
				if(d<i && tempSpawns.size() < 4)
					tempSpawns.add(distanceMap.get(d));
	}

	private Map<Double,Location> getDistanceFromSpawnpoints(Location loc) {
		Map<Double,Location> result = new HashMap<Double,Location>();
		for(Location spawnLoc : mobSpawnpointSet){
			result.put(TCUtils.distance(loc, spawnLoc), spawnLoc);
		}
		return result;
	}

	public void throwTNT(final Player p) {
		final Item tnt = p.getWorld().dropItem(p.getLocation().add(0, p.getEyeHeight(), 0), new ItemStack(46));
        Vector direction = p.getEyeLocation().getDirection();
        tnt.setVelocity(direction);
        TimerTask task = new TimerTask() {
			public void run() {
				try{
					tnt.getWorld().createExplosion(tnt.getLocation(), 2);
        			tnt.remove();
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		};
    	new Timer().schedule(task,1000);
	}
}
