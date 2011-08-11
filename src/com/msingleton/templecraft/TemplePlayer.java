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
	protected int totalXp, totalLevel, roundMobsKilled, roundPlayersKilled, roundXp, roundGold, roundDeaths;
	protected Map<String, Integer> classXp = new HashMap<String, Integer>();
	protected Map<String, Integer> classLevel = new HashMap<String, Integer>();
    protected Map<String,String> classItemMap   = new HashMap<String,String>();
    protected Map<String,String> classArmorMap  = new HashMap<String,String>();
    protected Set<String> ownerOfSet = new HashSet<String>();
    protected Set<String> accessToSet = new HashSet<String>();
    protected String ownerOf = "";
    protected String accessTo = "";
    public String name, currentClass;
    public Temple currentTemple;
	
	public TemplePlayer(){
	}
	
	public TemplePlayer(Player p){
		player          = p;
		name            = p.getName();
		currentClass    = null;
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
    	
    	for(String className : TempleManager.classes){
		    classXp.put(className, c.getInt("Players."+name+".classes."+className+".xp", 0));
		    classLevel.put(className, c.getInt("Players."+name+".classes."+className+".level", 1));
		    classItemMap.put(className, c.getString("Players."+name+".classes."+className+".items",TempleManager.classItemMap.get(className)));
		    classArmorMap.put(className, c.getString("Players."+name+".classes."+className+".armor",TempleManager.classArmorMap.get(className)));
    	}
    	c.save();
	}
	
	public boolean removeClass(){
		Temple temple = currentTemple;
		if(temple == null && currentClass != null){
			currentClass = null;
			if(TCUtils.hasPlayerInventory(name)){
				TCUtils.restorePlayerInventory(player);
				return true;
			} else {
				Inventory inventory = player.getInventory();
				inventory.clear(inventory.getSize() + 0);
				inventory.clear(inventory.getSize() + 1);
				inventory.clear(inventory.getSize() + 2);
				inventory.clear(inventory.getSize() + 3);
			}
		}
		return false;
	}
	
	public void addXp(String className, int xp){
		roundXp += xp;
		classXp.put(className, classXp.remove(className)+xp);
		int nextLevel = classLevel.get(className)+1;
		if(classXp.get(className) >= getLevelXp(nextLevel)){
			TempleManager.tellPlayer(player, ChatColor.AQUA+"Level Up! You are now level "+nextLevel+".");
			classLevel.put(className, classLevel.remove(className)+1);
		}
	}
	
	public void addItem(Material m){
		String item = ""+m;
		String currentItems = classItemMap.remove(currentClass);
		String newItems = handleItemString(currentItems, item);
		Configuration c = config;
		c.load();
		classItemMap.put(currentClass, newItems);
		c.setProperty("Players."+name+".classes."+currentClass+".items", newItems);
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
	
	private String handleItemString(String currentItems, String item){
		if(currentItems.contains(item)){
			if(currentItems.contains(item+":")){
				String[] tempArray = currentItems.split(":");
				String tempString = ""; 
				tempArray[1] = ""+(Integer.parseInt(tempArray[1])+1);
				for(String s : tempArray)
					tempString += s;
				return tempString;
			} else {
				return currentItems.replace(item, item+":1");
			}
		} else {
			return currentItems += ","+item;
		}
	}
	
	public void save(){
		if(currentClass != null)
			reorganizeMaps();
		saveData();
		TempleManager.tellPlayer(player, "Account Saved");
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
    	
    	for(String className : TempleManager.classes){
    		c.setProperty("Players."+name+".classes."+className+".xp", classXp.get(className));
    		c.setProperty("Players."+name+".classes."+className+".level", classLevel.get(className));
    		c.setProperty("Players."+name+".classes."+className+".items.", classItemMap.get(className));
    		c.setProperty("Players."+name+".classes."+className+".armor.", classArmorMap.get(className));
    	}
    	c.save();
	}

	private void reorganizeMaps() {
		String items = classItemMap.get(currentClass).toString().toLowerCase();
    	String armor = classArmorMap.get(currentClass).toString().toLowerCase();
    	String allItems = items + "," + armor;
    	
    	StringBuilder s = new StringBuilder();
		for(ItemStack item : player.getInventory().getContents()){
			if(item == null)
				continue;
			
			if(allItems.contains(""+item.getTypeId()) || allItems.contains(item.getType().toString().toLowerCase())){
				s.append(item.getTypeId());
				if(item.getAmount()>1)
					s.append(":" + item.getAmount());
				if(item.getData()!=null)
					s.append(":" + item.getData());
				s.append(",");
			}
		}
		if(s.length() != 0 && s.charAt(s.length()-1) == ',')
			s.deleteCharAt(s.lastIndexOf(","));
		classItemMap.put(currentClass, s.toString());
		s.setLength(0);
		
		for(ItemStack item : player.getInventory().getArmorContents()){
			if(item == null)
				continue;
			
			if(allItems.contains(""+item.getTypeId()) || allItems.contains(item.getType().toString().toLowerCase())){
				s.append(item.getTypeId());
				if(item.getAmount()>1)
					s.append(":" + item.getAmount());
				if(item.getData()!=null)
					s.append(":" + item.getData());
				s.append(",");
			}
		}
		if(s.length() != 0 && s.charAt(s.length()-1) == ',')
			s.deleteCharAt(s.lastIndexOf(","));
		classArmorMap.put(currentClass, s.toString());
		s.setLength(0);
	}

	public void displayStats(){
		//TO DO: this
		player.sendMessage("-----TempleCraft Stats-----");
		player.sendMessage(ChatColor.BLUE+"Mobs Killed: "+ChatColor.WHITE+roundMobsKilled);
		player.sendMessage(ChatColor.GREEN+"XP Gained: "+ChatColor.WHITE+roundXp);
		player.sendMessage(ChatColor.GOLD+"Gold Collected: "+ChatColor.WHITE+roundGold);
		player.sendMessage(ChatColor.DARK_RED+"Deaths: "+ChatColor.WHITE+roundDeaths);
		resetRoundStats();
	}
	
	private void resetRoundStats() {
		roundXp             = 0;
		roundGold           = 0;
		roundMobsKilled     = 0;
		roundPlayersKilled  = 0;
		roundDeaths         = 0;
	}

	public int getLevelXp(int n){
		return ((n-1)*103+47*(n*(n-1))/2);
	}
	
	public int getDeathXP() {
		int currentLevel = classLevel.get(currentClass);
		int xp = ((classXp.get(currentClass)-getLevelXp(currentLevel))/8);
		return xp;
	}
	
	public String getBuyableItems(){
		if(currentClass == null)
			return null;
		
		return TempleManager.classEnabledItemsMap.get(currentClass);
	}
}
