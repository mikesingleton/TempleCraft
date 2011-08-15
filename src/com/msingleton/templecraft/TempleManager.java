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
    protected static boolean dropBlocks;
    
    // Configuration
    protected static Configuration config = null;
    
    // A Map of which temples are being editted in which World
    public static Map<String,World> templeEditMap  = new HashMap<String,World>();
    public static Map<Player,TemplePlayer> templePlayerMap  = new HashMap<Player,TemplePlayer>();
    protected static Set<Temple> templeSet         = new HashSet<Temple>();
    protected static Set<Temple> customTempleSet   = new HashSet<Temple>();
    protected static Set<Player> playerSet         = new HashSet<Player>();
    public static Set<Integer> breakable           = new HashSet<Integer>();    
    public static String breakableMats;
    public static String goldPerMob;
    
    final public static String[] mobs = {"Chicken","Cow","Pig","Sheep","Zombie","Pig_Zombie","Skeleton","Creeper","Wolf","Ghast","Monster","Slime","Spider","Squid"};
    
    // Flatland Configs
    public static int[] landLevels = {0,60,64,65,128};
    public static byte[] landMats = {7,1,3,2,0};
    
    public static int repairDelay;
    public static int maxEditWorlds;
    public static int maxTemplesPerPerson;
    public static int rejoinCost;
    public static int mobGoldMin = 0;
    public static int mobGoldRan = 0;
    
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
            
            // Configurable
            repairDelay            = TCUtils.getInt("settings.repairdelay", 5);
            maxEditWorlds          = TCUtils.getInt("settings.maxeditworlds", 2);
            maxTemplesPerPerson    = TCUtils.getInt("settings.maxtemplesperperson", 1);
            rejoinCost             = TCUtils.getInt("settings.rejoincost", 0);
            breakableMats          = TCUtils.getString(config, "settings.breakablemats", "46,82");
            goldPerMob             = TCUtils.getString(config, "settings.goldpermob", "50-100");
            dropBlocks             = TCUtils.getBoolean("settings.dropblocks", false);
            
        	worldEdit              = TempleCraft.getWorldEdit();
        	loadMisc();
        	loadTemplePlayers();
        	loadEditWorlds();
	    	loadCustomTemples();
        }
        // Convenience variables.
        checkUpdates            = TCUtils.getBoolean("settings.updatenotification", true);
    }

    private static void loadMisc() {
    	String[] g = goldPerMob.split("-");
    	if(g[0] != null){
    		mobGoldMin = Integer.parseInt(g[0]);
    		if(g[1] != null)
    			mobGoldRan = Integer.parseInt(g[1])-Integer.parseInt(g[0]);
    	}
    	
		//breakable
    	for(String s : breakableMats.split(",")){
    		s = s.trim();
    		if(!s.isEmpty())
    			breakable.add(Integer.parseInt(s));
    	}
	}

	/* ///////////////////////////////////////////////////////////////////// //
	
    LOAD/SAVE METHODS

	// ///////////////////////////////////////////////////////////////////// */
    
    public static void reloadTemples() {
    	clearWorld(world);
    	for(Temple temple : templeSet)
    		temple.clearTemple();
    	for(Temple temple : templeSet)
    		temple.loadTemple(world);
    }
    
	public static void loadCustomTemples() {
		clearWorld(world);
		
		File folder = new File("plugins/TempleCraft/SavedTemples/");
    	if(!folder.exists())
    		folder.mkdir();
    	for(String fileName : folder.list()){
    		if(fileName.contains("/"))
    			continue;
    		if(fileName.contains(TempleCraft.fileExtention)){
    			String templeName = fileName.replace(TempleCraft.fileExtention, "");
    			Temple temple;
    			temple = TCUtils.getTempleByName(templeName);
    			if(temple == null)
    				temple = new Temple(templeName);
    			temple.loadTemple(world);
    		}
    	}	
	}

	private static void loadTemplePlayers() {
		for(Player p : server.getOnlinePlayers())
			if(!TempleManager.templePlayerMap.containsKey(p))
				TempleManager.templePlayerMap.put(p, new TemplePlayer(p));
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
		    tellPlayer(p, "There is no one in a Temple right now.");
		    return;
		}
		
		for(Temple temple : templeSet)
			temple.playerList(p);
	}
    
    public static void notReadyList(Player p){
		if (playerSet.isEmpty())
		{
		    tellPlayer(p, "There is no one in a Temple right now.");
		    return;
		}
		
		for(Temple temple : templeSet)
			temple.notReadyList(p);
	}
    
    public static void removeAll() {
		for(Temple temple : templeSet)
			temple.removeAll();
	}
    
    public static void clearWorld(World world) {
    	for(Chunk c : world.getLoadedChunks())
    		world.regenerateChunk(c.getX(), c.getZ());
	}
    
    /*public static void clearTempleWorld(World world) {
    	for(Temple temple : templeSet)
    		if(temple.startLoc != null)
    			temple.clearFoundation(temple.startLoc);
	}*/
    
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
    	
		tp.currentTemple = null;
		tp.currentCheckpoint = null;
		playerSet.remove(p);
		MobArenaClasses.classMap.remove(p);
		
		if(temple.playerSet.remove(p)){			
			if(TCUtils.hasPlayerInventory(p.getName()))
				TCUtils.restorePlayerInventory(p);
		
			tp.displayStats();
			
			if (temple.isRunning && temple.playerSet.isEmpty())
				temple.endTemple();
		}
		
		if(temple.editorSet.remove(p))
			if(temple.editorSet.isEmpty())
				TempleManager.templeEditMap.remove(temple.templeName);
		
		if(temple.readySet.remove(p))
			if (temple.readySet.isEmpty() && temple.deadSet.isEmpty() && temple.readySet.equals(playerSet) && !temple.isRunning)
				temple.startTemple();
		
		if(locationMap.containsKey(p)){
			p.teleport(locationMap.get(p));
		} else {
			String msg = "We have lost track of your origin. Please request assistance.";
			TempleManager.tellPlayer(p, msg);
		}
		locationMap.remove(p);
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