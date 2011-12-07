package com.msingleton.templecraft.games;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.util.MobArenaClasses;

public class Spleef extends Game{
    public Map<Location,String> brokenBlockMap = new HashMap<Location,String>();
    public Set<Player> aliveSet                = new HashSet<Player>();
    private Timer gameTimer                    = new Timer();
    public Player winner;
    public int roundNum = 0;
    public int roundLim = 3;
	
	public Spleef(String name, Temple temple, World world) {
		super(name, temple, world);
		world.setPVP(false);
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
		super.endGame();
	}
	
	public void startRound(){
		isRunning = true;
		roundNum++;
		restorePlayingField();
		for(Location loc : lobbyLocMap.keySet())
			loc.getBlock().setTypeId(0);
		deadSet.clear();
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
			for(Location loc : lobbyLocMap.keySet())
				loc.getBlock().setTypeId(42);
		}
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
			winner = (Player)aliveSet.toArray()[0];
			endRound();
		} else if(aliveSet.isEmpty()){
			winner = p;
			endRound();
		}
	}
}
