package com.msingleton.templecraft.games;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

import com.msingleton.templecraft.TCMobHandler;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.util.MobArenaClasses;
import com.nijikokun.register.payment.Method.MethodAccount;

public class Adventure extends Game{
	public Adventure(String name, Temple temple, World world) {
		super(name, temple, world);
	}
	
	public void endGame(){
		TempleManager.tellAll("Adventure game finished in: \""+temple.templeName+"\"");
		super.endGame();
	}
	
	public Location getPlayerSpawnLoc() {
		Random r = new Random();
		Location loc = null;
		for(Location l : startLocSet){
			if(!l.getWorld().equals(world))
				l.setWorld(world);
			
			if(loc == null)
				loc = l;
			else if(r.nextInt(startLocSet.size()) == 0)
				loc = l;
		}
		return loc;
	}
	
	public void playerDeath(Player p)
	{
		String msg;
		if(TempleCraft.method != null){
			String s = TempleCraft.method.format(2.0);
			String currencyName = s.substring(s.indexOf(" ") + 1);
			MethodAccount balance = TempleCraft.method.getAccount(p.getName());
			if(balance.hasEnough(rejoinCost)){
				if(TempleCraft.method != null && rejoinCost > 0){
					msg = "To continue playing will cost you "+ChatColor.GOLD+rejoinCost+" "+currencyName+".";
					TempleManager.tellPlayer(p, msg);
					msg = "Or type \"/tc leave\" and restart from the beginning!";
					TempleManager.tellPlayer(p, msg);
				} else {
					//msg = "Rejoin your friends! :O";
					//TempleManager.tellPlayer(p, msg);
				}
			} else {
				msg = "You do not have enough "+currencyName+" to rejoin.";
				TempleManager.tellPlayer(p, msg);
				msg = "Please type \"/tc leave\" to leave the temple.";
				TempleManager.tellPlayer(p, msg);
			}
		}
		p.teleport(lobbyLoc);
		super.playerDeath(p);
	}
	
	protected void handleSign(Sign sign) {
		String[] Lines = sign.getLines();
		
		if(!Lines[0].equals("[TempleCraft]") && !Lines[0].equals("[TC]") && !Lines[0].equals("[TempleCraftM]") && !Lines[0].equals("[TCM]"))
			return;
			
		if(Lines[1].toLowerCase().equals("classes")){
			if(MobArenaClasses.enabled){
				usingClasses = true;
				MobArenaClasses.generateClassSigns(sign);
			}
		}
		super.handleSign(sign);
	}
	
	public void hitEndBlock(Player p) {
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		if (playerSet.contains(p))
        {            	
        	readySet.add(p);
        	rewardSet.add(p);
        	tp.rewards = rewards;
        	int totalTime = (int)(System.currentTimeMillis()-startTime)/1000;
        	tellPlayer(p, "You finished in "+totalTime+" seconds!");
        	
        	// Update ScoreBoards
        	List<String> scores = new ArrayList<String>();
        	scores.add(p.getDisplayName());
        	scores.add(tp.roundMobsKilled + "");
        	scores.add(tp.roundGold + "");
        	scores.add(tp.roundDeaths + "");
        	scores.add(totalTime + "");
        	TempleManager.SBManager.updateScoreBoards(this, scores);
        	
        	if(readySet.equals(playerSet)){
        		endGame();
        	} else {
        		tellPlayer(p, "You are ready to leave!");
        		tp.currentCheckpoint = null;
        	}
        }
        else
        {
            tellPlayer(p, "WTF!? Get out of here!");
        }
	}
	
	public void onPlayerMove(PlayerMoveEvent event){
		super.onPlayerMove(event);		
		Player p = event.getPlayer();
		
		if(!isRunning)
			return;
		
		Set<Location> tempLocs = new HashSet<Location>();
		for(Location loc : mobSpawnpointMap.keySet())
			 if(p.getLocation().distance(loc) < mobSpawnpointMap.get(loc).b)
				tempLocs.add(loc);

		for(Location loc : tempLocs)
			TCMobHandler.SpawnMobs(this, loc, mobSpawnpointMap.remove(loc).a);
	}
}
