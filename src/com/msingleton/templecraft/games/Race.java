package com.msingleton.templecraft.games;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.util.Pair;
import com.msingleton.templecraft.util.Translation;

public class Race extends Game{
	
	public Race(String name, Temple temple, World world) {
		super(name, temple, world);
		world.setPVP(false);
		rejoinCost = 0;
	}
	
	public void startGame(){
		standings = getStandings(temple,"Race");
		super.startGame();
	}

	public void endGame(){
		saveStandings(temple,"Race");
		super.endGame();
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
        	tellPlayer(p, Translation.tr("game.finishTime", ""+totalTime));
        	standings.add(new Pair<String,Double>(p.getDisplayName(),totalTime));
        	if(isPersonalHighScore(p, totalTime))
        		TempleManager.tellPlayer(p, Translation.tr("race.personalHighscore"));
        	if(isHighScore(p, totalTime))
        		TempleManager.tellPlayer(p, Translation.tr("race.overallHighscore"));

        	sortStandings();
        	p.sendMessage(c1+"----"+c2+temple.templeName+": Race HighScores"+c1+"----");
        	for(int i = 0; i<standings.size();i++){
        		if(i<displayAmount){
        			StringBuilder line = new StringBuilder();
        			line.append(c1 + "" + (i+1) + ". " + c2 + standings.get(i).a);
        			int startLeng = line.length();
        			while(line.length()<50-startLeng)
        				line.append(" ");
        			line.append(standings.get(i).b + "" + c1 + " "+Translation.tr("race.seconds"));
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
	}
}
