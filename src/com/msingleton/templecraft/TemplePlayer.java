package com.msingleton.templecraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.config.Configuration;


public class TemplePlayer{
	private Player player;
	protected static Configuration config = TCUtils.getConfig("Players");
	protected int roundMobsKilled, roundPlayersKilled, roundGold, roundDeaths;
    protected Set<String> ownerOfSet = new HashSet<String>();
    protected Set<String> accessToSet = new HashSet<String>();
    protected String ownerOf = "";
    protected String accessTo = "";
    public String name;
    public Location currentCheckpoint;
    public Temple currentTemple;
	
	public TemplePlayer(){
	}
	
	public TemplePlayer(Player p){
		player          = p;
		name            = p.getName();
    	resetRoundStats();
		loadAccount();
	}

	private void loadAccount() {	
		Configuration c = config;
    	c.load(); 
    	
    	for(String s : c.getString("Players."+name+".Temples.ownerOf","").split(","))
    		ownerOfSet.add(s);
    	
    	for(String s : c.getString("Players."+name+".Temples.accessTo","").split(","))
    		accessToSet.add(s);
    	
    	updateAccess();
    	c.save();
	}
	
	public void addOwnerOf(String templeName){
		ownerOfSet.add(templeName);
		updateAccess();
	}
	
	public void removeOwnerOf(String templeName){
		ownerOfSet.remove(templeName);
		updateAccess();
	}
	
	public void addAccessTo(String templeName){
		accessToSet.add(templeName);
		updateAccess();
	}
	
	public void removeAccessTo(String templeName){
		ownerOfSet.remove(templeName);
		accessToSet.remove(templeName);
		updateAccess();
	}
	
	public void updateAccess(){
		StringBuilder ownerOf = new StringBuilder();
    	for(String s : ownerOfSet)
    		if(TempleManager.templeSet.isEmpty()){
    			if(ownerOf.length() == 0)
    				ownerOf.append(s);
    			else
    				ownerOf.append(","+s);
    		} else {
	    		for(Temple temple : TempleManager.templeSet)
	    			if(temple.templeName.equals(s))
		    			if(ownerOf.length() == 0)
		    				ownerOf.append(s);
		    			else
		    				ownerOf.append(","+s);
    		}
    	
    	StringBuilder accessTo = new StringBuilder();
    	for(String s : accessToSet)
    		if(TempleManager.templeSet.isEmpty()){
    			if(accessTo.length() == 0)
    				accessTo.append(s);
    			else
    				accessTo.append(","+s);
    		} else {
	    		for(Temple temple : TempleManager.templeSet)
	    			if(temple.templeName.equals(s))
		    			if(accessTo.length() == 0)
		    				accessTo.append(s);
		    			else
		    				accessTo.append(","+s);
    		}
    	
    	this.ownerOf = ownerOf.toString();
    	this.accessTo = accessTo.toString();
	}
	
	public void saveData(){		
		Configuration c = config;
    	c.load();
    	c.setProperty("Players."+name+".Temples.ownerOf", ownerOf.toString());    	
    	c.setProperty("Players."+name+".Temples.accessTo", accessTo.toString());
    	c.save();
	}

	public void displayStats(){
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
}
