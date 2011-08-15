package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TemplePlayer{
	protected Set<Object> tempSet = new HashSet<Object>();
	private Player player;
	protected int roundMobsKilled, roundPlayersKilled, roundGold, roundDeaths, ownedTemples;
    protected String name;
    protected Location currentCheckpoint;
    protected Temple currentTemple;
	
	public TemplePlayer(){
	}
	
	public TemplePlayer(Player p){
		player       = p;
		name         = p.getName();
		ownedTemples = 0;
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
	
	/*protected void getRefresher() {
		TempleManager.server.getScheduler().scheduleSyncDelayedTask(TempleManager.plugin,
	            new Runnable()
	            {
	                public void run()
	                {
	                    tempSet.clear();
	                }
	            }, 10000);
	}*/
}
