package com.msingleton.templecraft.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.util.Pair;

public class Race extends Game{
	public Map<Location,Integer> checkpointMap = new HashMap<Location,Integer>();
    public Map<Location,String[]> chatMap      = new HashMap<Location,String[]>();
    
	public Race(String name, Temple temple, World world) {
		super(name, temple, world);
		rejoinCost = 0;
	}
	
	public void startGame(){
		standings = getStandings(temple,"Race");
		super.startGame();
	}

	public void endGame(){
		saveStandings(temple,"Race");
		TempleManager.tellAll("Race finished in: \""+temple.templeName+"\"");
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
		p.teleport(lobbyLoc);
		super.playerDeath(p);
	}
	
	public void hitEndBlock(Player p) {
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		if (playerSet.contains(p))
        {            	
        	readySet.add(p);
        	rewardSet.add(p);
        	tp.rewards = rewards;
        	double totalTime = ((int)(System.currentTimeMillis()-startTime)/100)/10.0;
        	tellPlayer(p, "You finished the Race in "+totalTime+" seconds!");
        	standings.add(new Pair<String,Double>(p.getDisplayName(),totalTime));
        	if(isPersonalHighScore(p, totalTime))
        		TempleManager.tellPlayer(p, "You beat your old highscore!");
        	if(isHighScore(p, totalTime))
        		TempleManager.tellPlayer(p, "You beat the highscore!");

        	sortStandings();
        	p.sendMessage(c1+"----"+c2+temple.templeName+": Race HighScores"+c1+"----");
        	for(int i = 0; i<standings.size();i++){
        		if(i<displayAmount){
        			StringBuilder line = new StringBuilder();
        			line.append(c1 + "" + (i+1) + ". " + c2 + standings.get(i).a);
        			int startLeng = line.length();
        			while(line.length()<50-startLeng)
        				line.append(" ");
        			line.append(standings.get(i).b + "" + c1 + " seconds");
        			p.sendMessage(line.toString());
        		} else if(i > saveAmount)
        			standings.remove(i);
            }
        	TempleManager.playerLeave(p);
        	
        	// Update ScoreBoards
        	List<String> scores = new ArrayList<String>();
        	scores.add(p.getDisplayName());
        	scores.add(totalTime + "");
        	TempleManager.SBManager.updateScoreBoards(this, scores);
        }
        else
        {
            tellPlayer(p, "WTF!? Get out of here!");
        }
	}
}
