package com.msingleton.templecraft.games;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.msingleton.templecraft.MobArenaClasses;
import com.msingleton.templecraft.TCMobHandler;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;

public class Spleef extends Game{	
	public Map<Location,Integer> checkpointMap = new HashMap<Location,Integer>();
    public Map<Location,String[]> chatMap      = new HashMap<Location,String[]>();
    public Map<Location,String> brokenBlockMap = new HashMap<Location,String>();
    public Set<Player> aliveSet                = new HashSet<Player>();
    private Timer gameTimer                    = new Timer();
    public Player winner;
    public int roundNum = 0;
    public int roundLim = 3;
	
	public Spleef(String name, Temple temple, World world) {
		super(name, temple, world);
	}
	
	public void playerJoin(Player p){	
		super.playerJoin(p);
		MobArenaClasses.clearInventory(p);
	}
	
	public void startGame(){
		startRound();
		super.startGame();
	}
	
	public void endGame(){
		gameTimer.cancel();
		TempleManager.tellAll("Spleef game finished in: \""+temple.templeName+"\"");
		super.endGame();
	}
	
	public void startRound(){
		roundNum++;
		restorePlayingField();
		for(Block b : lobbyBlockSet)
			b.setTypeId(0);
		aliveSet.addAll(playerSet);
		tellAll("Round "+roundNum);
	}

	public void endRound(){
		isRunning = false;
		for(Player p : aliveSet)
			p.teleport(lobbyLoc);
		tellAll(winner.getDisplayName()+" won round "+roundNum);
		if(roundNum >= roundLim){
			tellAll("Good game! Ending Spleef...");
			TimerTask task = new TimerTask() {
				public void run() {
					endGame();
				}
			};
			gameTimer.schedule(task, 2000);
		} else {
			for(Block b : lobbyBlockSet)
				b.setTypeId(42);
		}
	}
	
	public Location getPlayerSpawnLoc() {
		Random r = new Random();
		Location loc = null;
		for(Block b : startBlockSet){
			if(loc == null)
				loc = b.getLocation();
			else if(r.nextInt(startBlockSet.size()) == 0)
				loc = b.getLocation();
		}
		return loc;
	}
	
	private void restorePlayingField() {
		for(Location loc : brokenBlockMap.keySet()){
			String[] s = brokenBlockMap.get(loc).split(":");
			int id;
			byte data;
			try{
				id = Integer.parseInt(s[0]);
				data = Byte.parseByte(s[1]);
			}catch(Exception e){
				id = Integer.parseInt(s[0]);
				data = 0;
			}
			world.getBlockAt(loc).setTypeIdAndData(id, data, true);
		}
	}
	
	public void playerDeath(Player p)
	{
		p.teleport(lobbyLoc);
		super.playerDeath(p);
		aliveSet.remove(p);
		if(aliveSet.size() == 1){
			winner = (Player)playerSet.toArray()[0];
			endRound();
		} else if(aliveSet.isEmpty()){
			winner = p;
			endRound();
		}
	}
	
	protected void handleSign(Sign sign) {
		String[] Lines = sign.getLines();
		Block b = sign.getBlock();
		
		
		if(!Lines[0].equals("[TempleCraft]") && !Lines[0].equals("[TC]")){
			return;
		}
			
		
		if(Lines[1].toLowerCase().equals("spawnarea")){
			int radius;
			try{
				radius = Math.abs(Integer.parseInt(Lines[3]));
			}catch(Exception e){
				radius = 5;
			}
			
			//Get a square area of blocks and then keep the ones that are a distance radius away or less
			int y = b.getY();
			for(int i=0;i<=radius;i++){
				for(int k=0;k<=radius;k++){
					int x = b.getX()+i;
					int z = b.getZ()+k;
					Location loc = new Location(world,x, y, z);
					if(TCUtils.distance(b.getLocation(), loc) <= radius)
						startBlockSet.add(world.getBlockAt(loc));
				}
			}
			
			b.setTypeId(0);	
		}
		super.handleSign(sign);
	}
}
