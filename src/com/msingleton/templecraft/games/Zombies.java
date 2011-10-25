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
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.msingleton.templecraft.MobArenaClasses;
import com.msingleton.templecraft.TCMobHandler;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;

public class Zombies extends Game{
	//private static Set<Integer> transBlockSet = new HashSet<Integer>(Arrays.asList(0,8,9,10,27,28,30,31,34,37,38,39,40,50,55,59,63,64,65,66,68,69,70,71,72,75,76,77,78,83,90,93,94,96,321));
	private Set<Location> tempSpawns = new HashSet<Location>();
	private Timer gameTimer          = new Timer();
	private Map<Player,TimerTask> regainHealthMap = new HashMap<Player,TimerTask>();
	private Map<Player,TimerTask> endHurtMap      = new HashMap<Player,TimerTask>();
	private Set<TimerTask> taskSet   = new HashSet<TimerTask>();
	public Set<Player> hurtSet       = new HashSet<Player>();
	//private Set<Sign> buySignSet   = new HashSet<Sign>();
	protected int roundNum;
	
	public Zombies(String name, Temple temple, World world) {
		super(name, temple, world);
	}	
	
	public void playerJoin(Player p){	
		super.playerJoin(p);
		MobArenaClasses.clearInventory(p);
	}		
	
	public void startGame() {
		super.startGame();
		roundNum = 	0;
		nextRound();
	}

	public void endGame() {
		endTimer();
		TempleManager.tellAll("Zombies game finished in: \""+temple.templeName+"\"");
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

	/*protected void handleSign(Sign sign) {
		String[] Lines = sign.getLines();
		Block b = sign.getBlock();
		
		
		if(!Lines[0].equals("[TempleCraft]") && !Lines[0].equals("[TC]")){
			return;
		}
			
		
		if(Lines[1].toLowerCase().equals("buy")){
			try{
				int id = Integer.parseInt(Lines[2].split(" ")[0]);
				int cost = Integer.parseInt(Lines[3]);
				int amount;
				try{
					amount = Integer.parseInt(Lines[2].split(" ")[1]);
				} catch(Exception e){
					amount = 1;
				}
				sign.setLine(0, "Buy "+amount);
				sign.setLine(1, ""+Material.getMaterial(id));
				sign.setLine(2, "for");
				sign.setLine(3, cost+" gold");
			} catch(Exception e){
				System.out.println("[TempleCraft] Buy sign not set up properly");
				sign.setTypeId(0);
			}
		}
		super.handleSign(sign);
	}*/
	
	/*@Override
	public void handleSignClicked(Player p, Sign sign){
		String[] Lines = sign.getLines();
		Block b = sign.getBlock();
		
		if(Lines[0].toLowerCase().contains("buy")){
			try{
				int id = Material.getMaterial(Lines[1]).getId();
				int cost = Integer.parseInt(Lines[3].replace(" gold", ""));
				int amount;
				try{
					amount = Integer.parseInt(Lines[0].replace("Buy ", ""));
				} catch(Exception e){
					amount = 1;
				}
				if(TempleCraft.method != null){
					MethodAccount balance = TempleCraft.method.getAccount(p.getName());
					if(balance.hasEnough(cost)){
						balance.subtract(cost);
						if(id >= 298 && id <= 317)
							MobArenaClasses.equipArmor(p, ""+id);
						else
							MobArenaClasses.giveItems(true, p, id+":"+amount);
						TempleManager.tellPlayer(p, "You purchased a "+Lines[1]+" for "+cost+" gold");
					} else {
						TempleManager.tellPlayer(p, "You do not have enough gold to make this purchase.");
						TempleManager.tellPlayer(p, "Current Balance: "+balance.balance()+" gold");
					}
				}
			} catch(Exception e){
				System.out.println("[TempleCraft] Buy sign not set up properly");
				sign.setTypeId(0);
			}
		}
	}*/
	
	/* ///////////////////////////////////////////////////////////////////// //
	
		MOBSPAWN METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	public void resetTimer() {
		for(TimerTask t : taskSet)
			t.cancel();
		
		taskSet.clear();
		TimerTask task = new TimerTask() {
			public void run() {
				Game game = TCUtils.getGameByName(gameName);
				for(Player p : playerSet){
					Random r = new Random();
					Set<Location> tempSet = new HashSet<Location>(getClosestSpawnpoints(p));
					for(Location loc : tempSet){
						for(int i = 0; i<1+r.nextInt(getMobLimit()/tempSet.size());i++){
							if(monsterSet.size() > getMobLimit())
								return;
							TCMobHandler.SpawnMobs(game, loc, CreatureType.ZOMBIE);
						}
		        	}	
				}
			}
		};
		taskSet.add(task);
    	gameTimer.schedule(task, 5000);
	}
	
	public void hurtPlayer(Player p) {
		TimerTask regainHealth = new TimerTask() {
			public void run() {
	        	for(Player p : regainHealthMap.keySet())
	        		if(regainHealthMap.get(p).equals(this))
		        		if(p.getHealth() < 20)
		        			p.setHealth(p.getHealth()+1);
		        		else
		        			regainHealthMap.remove(p);
			}
		};
		
		TimerTask endHurt = new TimerTask() {
			public void run() {
				for(Player p : endHurtMap.keySet())
	        		if(endHurtMap.get(p).equals(this))
		        		hurtSet.remove(p);
			}
		};
		
		hurtSet.add(p);
		if(regainHealthMap.containsKey(p))
			regainHealthMap.remove(p).cancel();
		if(endHurtMap.containsKey(p))
			endHurtMap.remove(p).cancel();
    	gameTimer.scheduleAtFixedRate(regainHealth,3000,100);
    	gameTimer.schedule(endHurt, 500);
    	regainHealthMap.put(p, regainHealth);
    	endHurtMap.put(p, endHurt);
	}
	
	public void endTimer(){
		try{
			gameTimer.cancel();
		}catch(IllegalStateException e){
			e.printStackTrace();
		}
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
			/*
			try{
				Block b = p.getLocation().getBlock();
				//Start with the blocks near the player
				for(int i = -1; i <= 1; i++)
					for(int j = 0; j <= 2; j++)
						for(int k = -1; k <= 1; k++)
							findSpawnpointsRecursively(b.getRelative(i,j,k));
			}catch(Exception e){
				findSpawnpointsByDistance(p.getLocation());
			}*/
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
		//TNTPrimed tnt = p.getEyeLocation().getWorld().spawn(p.getEyeLocation(), TNTPrimed.class);
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

	/*private void findSpawnpointsRecursively(Block b) {	
		//if(!TCUtils.inRegion(p1, p2, startLoc))
		//	return;
		
		for(Location loc : mobSpawnpointMap.keySet())
			if(TCUtils.distance(loc, b.getLocation()) < 2)
				tempSpawns.add(b.getLocation());
		
		if(tempSpawns.size() > mobSpawnpointSet.size()/4)
			return;
		
		for(int i = -1; i <= 1; i++)
			for(int j = -1; j <= 1; j++)
				for(int k = -1; k <= 1; k++)
					if(transBlockSet.contains(b.getRelative(i,j,k).getTypeId()))
						findSpawnpointsRecursively(b.getRelative(i,j,k));
	}*/
}
