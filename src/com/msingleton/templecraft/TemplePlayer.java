package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import com.msingleton.templecraft.games.Game;

public class TemplePlayer{
	public Set<Object> tempSet = new HashSet<Object>();
	private Player player;
	protected int roundMobsKilled, roundPlayersKilled, roundGold;
	public int roundDeaths;
	protected Sign sensedSign;
	protected boolean canAutoTele;
	protected int ownedTemples;
    protected String name;
    protected Timer playerTimer = new Timer();
    protected TimerTask enterTempleTask;
    protected TimerTask counter;
    public Location currentCheckpoint;
    public Temple currentTemple;
    public Game currentGame;
	
	public TemplePlayer(){
	}
	
	public TemplePlayer(Player p){
		player       = p;
		name         = p.getName();
		ownedTemples = 0;
		canAutoTele  = false;
    	resetRoundStats();
    	//getRefresher();
    	getOwnedTemples();
	}

	private void getOwnedTemples() {
		for(Temple temple : TempleManager.templeSet)
			if(temple.owners.contains(name.toLowerCase()))
				ownedTemples++;
	}

	protected void displayStats(){
		//TO DO: this
		player.sendMessage("-----TempleCraft Stats-----");
		player.sendMessage(ChatColor.BLUE+"Mobs Killed: "+ChatColor.WHITE+roundMobsKilled);
		player.sendMessage(ChatColor.GOLD+"Gold Collected: "+ChatColor.WHITE+roundGold);
		player.sendMessage(ChatColor.DARK_RED+"Deaths: "+ChatColor.WHITE+roundDeaths);
		resetRoundStats();
	}
	
	private void resetRoundStats() {
		roundGold           = 0;
		roundMobsKilled     = 0;
		roundPlayersKilled  = 0;
		roundDeaths         = 0;
	}
	
	public void startEnterTimer(final Player p) {
		final TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		
		counter = new TimerTask() {
            public void run()
            {
            	int timeRemaining = (int)(Math.ceil(enterTempleTask.scheduledExecutionTime()-System.currentTimeMillis())/1000.0);
            	if(timeRemaining <= 3 && timeRemaining > 0){
            		TempleManager.tellPlayer(p, "Entering Temple in "+timeRemaining+"...");
            	} else if(timeRemaining <= 0){
            		cancel();
            	}
            }
		};
		
		enterTempleTask= new TimerTask() {
	        public void run()
	        {
	        	TCPlayerListener.handleSignClicked(p,tp.sensedSign);
	    		tp.sensedSign = null;
	    		tp.canAutoTele = false;
	    		stopEnterTimer();
	        }
		};
		
		playerTimer.scheduleAtFixedRate(counter, 0, 1000);
		playerTimer.schedule(enterTempleTask, 5000);
	}

	public void stopEnterTimer() {
		if(counter != null)
			counter.cancel();
		if(enterTempleTask != null)
			enterTempleTask.cancel();
	}
	
	public void resetEnterTimer(Player p) {
		stopEnterTimer();
		startEnterTimer(p);
	}
}
