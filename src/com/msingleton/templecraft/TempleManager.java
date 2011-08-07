package com.msingleton.templecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;


import com.ryanspeets.bukkit.flatlands.TempleWorldGenerator;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class TempleManager
{
	protected static Map<Player,Location> locationMap = new HashMap<Player,Location>();
	
    // Convenience variables.
    protected static TempleCraft plugin        = null;
    public static WorldEditPlugin worldEdit = null;
    protected static Server   server           = null;
    public static World    world               = null;
    protected static boolean isEnabled         = true;
    protected static boolean checkUpdates;
    
    // Configuration
    protected static Configuration config = null;
    
	// Maps for storing player classes, class items and armor.
    public static List<String> classes          = new ArrayList<String>();
    //public static Map<Player,String> classMap       = new HashMap<Player,String>();
    protected static Map<String,String> classItemMap   = new HashMap<String,String>();
    protected static Map<String,String> classArmorMap  = new HashMap<String,String>();
    protected static Map<String,String> classEnabledItemsMap  = new HashMap<String,String>();
    
    
    // 
    public static Map<String,World> templeEditMap  = new HashMap<String,World>();
    
    //public static Map<String,Temple> templeMap  = new HashMap<String,Temple>();
    public static Map<Player,TemplePlayer> templePlayerMap  = new HashMap<Player,TemplePlayer>();
    protected static Set<Temple> templeSet         = new HashSet<Temple>();
    protected static Set<Temple> customTempleSet   = new HashSet<Temple>();
    protected static Set<Player> playerSet         = new HashSet<Player>();
    public static Set<Integer> blockSet            = new HashSet<Integer>();
    public static Set<Material> breakable          = new HashSet<Material>();    
    
    //Flatland Configs
    public static int[] landLevels = {0,60,64,65,128};
    public static byte[] landMats = {7,1,3,2,0};
    
    public static int repairDelay;
    public static int maxEditWorlds;
    public static int maxTemplesPerPerson;
    
    /**
     * Initializes the TempleManager.
     */
    public static void init(TempleCraft instance)
    {
        // If instance == null, simply update location variables.
        if (instance != null)
        {
            // General variables.
            plugin                 = instance;
            config 		           = TCUtils.getConfig("config");
            server                 = plugin.getServer();
            world                  = TCUtils.getTempleWorld();
            repairDelay            = TCUtils.getInt("settings.repairdelay", 5);
            maxEditWorlds          = TCUtils.getInt("settings.maxeditworlds", 4);
            maxTemplesPerPerson    = TCUtils.getInt("settings.maxtemplesperperson", 1);
        	classes                = TCUtils.getClasses();
        	classItemMap           = TCUtils.getClassItems(config, "classes.","items");
        	classArmorMap          = TCUtils.getClassItems(config, "classes.","armor");
        	classEnabledItemsMap   = TCUtils.getClassItems(config, "classes.","enabled");
        	worldEdit              = TempleCraft.getWorldEdit();
        	loadSets();
        	loadEditWorlds();
	    	loadCustomTemples();
	    	loadTemplePlayers();
        }
        // Convenience variables.
        checkUpdates            = TCUtils.getBoolean("settings.updatenotification", true);
    }

    private static void loadSets() {
		//breakable
    	breakable.add(Material.TNT);
    	breakable.add(Material.COBBLESTONE);
    	//Blocks
    	int[] blockArray = {0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,29,33,35,41,42,43,44,45,46,47,48,49,52,54,56,57,58,60,61,62,67,73,74,79,80,81,82,84,85,86,87,88,89,90,91,92};
    	for(int i : blockArray)
    		blockSet.add(i);
	}

	/* ///////////////////////////////////////////////////////////////////// //
	
    LOAD/SAVE METHODS

	// ///////////////////////////////////////////////////////////////////// */
    
	public static void loadCustomTemples() {
		clearWorld(world);
		
		File folder = new File("plugins/TempleCraft/SavedTemples/");
    	if(!folder.exists())
    		folder.mkdir();
    	for(String fileName : folder.list()){
    		if(fileName.contains("/"))
    			continue;
    		if(fileName.contains(".tmp")){
    			String templeName = fileName.replace(".tmp", "");
    			Temple temple;
    			temple = TCUtils.getTempleByName(templeName);
    			if(temple == null)
    				temple = new Temple(templeName);
    			else
    				temple.endTemple();
    			temple.loadTemple(world);
    		}
    	}	
	}

	private static void loadTemplePlayers() {
		for(Player p : server.getOnlinePlayers()){
			TempleManager.templePlayerMap.put(p, new TemplePlayer(p));
		}
	}
    
	private static void loadEditWorlds(){
		if(maxEditWorlds > 10)
			maxEditWorlds = 10;
		for(int i = 0; i < maxEditWorlds; i++){
			server.createWorld("EditWorld_"+i, Environment.NORMAL, new TempleWorldGenerator());
			System.out.println("EditWorld \"EditWorld_"+i+"\" Loaded!");
		}
	}
	
    /* ///////////////////////////////////////////////////////////////////// //
	
    PLAYER METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
    public static void playerList(Player p){
		if (playerSet.isEmpty())
		{
		    tellPlayer(p, "There is no one in the Temple right now.");
		    return;
		}
		
		StringBuffer list = new StringBuffer();
		final String SEPARATOR = ", ";
		for (Player player : playerSet)
		{
		    list.append(player.getName());
		    list.append(SEPARATOR);
		}
		
		tellPlayer(p, "Survivors: " + list.substring(0, list.length() - 2));
	}
    
    public static void forceEnd(Player p) {
		for(Temple temple : templeSet)
			temple.forceEnd(p);
	}
    
    public static void clearWorld(World world) {
    	for(Chunk chunk : world.getLoadedChunks()){
			world.regenerateChunk(chunk.getX(), chunk.getZ());
    	}
    	for(Temple temple : templeSet){
    		temple.p1 = null;
    		temple.p2 = null;
    	}
	}
    
    /**
	* Attempts to remove a player from the Temple session.
	* The player is teleported back to his previous location, and
	* is removed from all the sets and maps.
	*/
    
    public static void playerLeave(Player p)
	{
    	TemplePlayer tp = templePlayerMap.get(p);
    	Temple temple = tp.currentTemple;
    	
    	if(temple == null)
    		return;
    	
    	temple.spectatorSet.remove(p);
		tp.currentClass = null;
		playerSet.remove(p);
		
		if(temple.playerSet.remove(p)){			
			if(TCUtils.hasPlayerInventory(p.getName()))
				TCUtils.restorePlayerInventory(p);
		
			tp.saveData();
			tp.displayStats();
			
			if (temple.isRunning && playerSet.isEmpty())
				temple.endTemple();
		}
		
		if(temple.readySet.remove(p))
			if (!temple.readySet.isEmpty() && temple.readySet.equals(playerSet))
				temple.startTemple();
		
		p.teleport(locationMap.get(p));
		locationMap.remove(p);
		tp.currentTemple = null;
	}
    
	/* ///////////////////////////////////////////////////////////////////// //
	
    	CLASS METHODS
	
	// ///////////////////////////////////////////////////////////////////// */
	
	/**
	* Assigns a class to the player.
	*/
	public static void assignClass(Player p, String className)
	{
		if(!TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.keepPlayerInventory(p);
		p.setHealth(20);
		templePlayerMap.get(p).currentClass = className;
		giveClassItems(p);
	}
	
	/**
	* Grant a player their class-specific items.
	*/
	public static void giveClassItems(Player p)
	{        
		TemplePlayer tp = templePlayerMap.get(p);
		String className  = tp.currentClass;
		String classItems = tp.classItemMap.get(className);
		String classArmor = tp.classArmorMap.get(className);
		TCUtils.giveItems(p, classItems);
		TCUtils.equipArmor(p, classArmor);
	}
    
    /* ///////////////////////////////////////////////////////////////////// //
	
    MISC METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	/**
	* Sends a message to a player.
	*/
	public static void tellPlayer(Player p, String msg)
	{
	if (p == null)
	    return;
	
	p.sendMessage(ChatColor.GREEN + "[TC] " + ChatColor.WHITE + msg);
	}
	
	/**
	* Sends a message to all players in the Temple.
	*/
	public static void tellAll(String msg)
	{
	Player[] players = server.getOnlinePlayers();
	for(Player p: players)
		tellPlayer((Player)p, msg);
	    
	}
}