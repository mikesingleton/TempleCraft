package com.msingleton.templecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;


import com.msingleton.templecraft.games.Game;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class TempleManager
{
	public static Map<Player,Location> locationMap = new HashMap<Player,Location>();
	
    // Convenience variables.
    public static TempleCraft plugin        = null;
    public static WorldEditPlugin worldEdit = null;
    public static Server   server           = null;
    public static boolean isEnabled         = true;
    protected static boolean checkUpdates;
    protected static boolean dropBlocks;
    
    // Configuration
    protected static Configuration config = null;
    
    // A Map of which temples are being editted in which World
    public static Map<String,World> templeEditMap  = new HashMap<String,World>();
    public static Map<Player,TemplePlayer> templePlayerMap  = new HashMap<Player,TemplePlayer>();
    public static Set<Temple> templeSet          = new HashSet<Temple>();
    public static Set<Game> gameSet              = new HashSet<Game>();
    public static Set<Player> playerSet          = new HashSet<Player>();
    public static Set<Integer> breakable         = new HashSet<Integer>();    
    public static String breakableMats;
    public static String goldPerMob;
    
    final public static String[] mobs = {"Chicken","Cow","Pig","Sheep","Zombie","PigZombie","Skeleton","Creeper","Wolf","Ghast","Monster","Slime","Spider","Squid"};
    final public static Set<String> modes = new HashSet<String>(Arrays.asList("adventure","zombies","spleef"));
    //future modes: "race","ctf","koth","assult","assassin"
    
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
            
            // Configurable
            repairDelay            = TCUtils.getInt("settings.repairdelay", 5);
            maxEditWorlds          = TCUtils.getInt("settings.maxeditworlds", 2);
            maxTemplesPerPerson    = TCUtils.getInt("settings.maxtemplesperperson", 1);
            rejoinCost             = TCUtils.getInt("settings.rejoincost", 0);
            breakableMats          = TCUtils.getString(config, "settings.breakablemats", "31,37,38,39,40,46,82");
            goldPerMob             = TCUtils.getString(config, "settings.goldpermob", "50-100");
            dropBlocks             = TCUtils.getBoolean(config, "settings.dropblocks", false);
            
        	//worldEdit              = TempleCraft.getWorldEdit();
        	loadMisc();
        	loadTemplePlayers();
	    	loadCustomTemples();
        }
        // Convenience variables.
        checkUpdates            = TCUtils.getBoolean(config, "settings.updatenotification", true);
    }

	/* ///////////////////////////////////////////////////////////////////// //
	
    LOAD/SAVE METHODS

	// ///////////////////////////////////////////////////////////////////// */
    
	public static void loadCustomTemples() {
		File folder = new File("plugins/TempleCraft/SavedTemples/");
    	if(!folder.exists())
    		folder.mkdir();
    	// Get temples based on filenames
    	for(File f : folder.listFiles()){
    		if(f.isDirectory()){
    			String templeName = f.getName();
    			Temple temple;
    			temple = TCUtils.getTempleByName(templeName);
    			if(temple == null)
    				temple = new Temple(templeName);
    		} else if(f.getName().endsWith(".tcf")){
    			String templeName = f.getName().replace(".tcf", "");
    			Temple temple;
    			temple = TCUtils.getTempleByName(templeName);
    			if(temple == null)
    				temple = new Temple(templeName);
    		}
    	}	
	}

	private static void loadTemplePlayers() {
		for(Player p : server.getOnlinePlayers())
			if(!TempleManager.templePlayerMap.containsKey(p))
				TempleManager.templePlayerMap.put(p, new TemplePlayer(p));
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
		
		for(Game game : gameSet)
			game.playerList(p, false);
	}
    
    public static void notReadyList(Player p){
		if (playerSet.isEmpty())
		{
		    tellPlayer(p, "There is no one in a Temple right now.");
		    return;
		}
		
		for(Game game : gameSet)
			game.notReadyList(p);
	}
    
    public static void removeAll() {
    	// Attempts to make all players leave whatever they are doing
		for(Player p : server.getOnlinePlayers())
			playerLeave(p);
	}
    
    public static void clearWorld(World world) {
    	for(Chunk c : world.getLoadedChunks())
    		world.regenerateChunk(c.getX(), c.getZ());
    	for(Entity e : world.getEntities())
    		if(!(e instanceof Player))
    			e.remove();
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
    	Game game = tp.currentGame;
    	
    	if(temple == null && game == null)
    		return;
    	
    	p.setFireTicks(0);
		tp.currentTemple = null;
		tp.currentGame = null;
		tp.currentCheckpoint = null;
		playerSet.remove(p);
		MobArenaClasses.classMap.remove(p);
		
		if(TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.restorePlayerInventory(p);
		
		if(locationMap.containsKey(p)){
			p.teleport(locationMap.get(p));
		} else {
			String msg = "We have lost track of your origin. Please request assistance.";
			TempleManager.tellPlayer(p, msg);
		}
		locationMap.remove(p);
		
		if(temple.editorSet.remove(p))
			if(templeEditMap.containsKey(temple.templeName))
				TCUtils.deleteTempWorld(templeEditMap.remove(temple.templeName));
		else
			tp.displayStats();
		
		if(game != null){
			// Players are only removed from the readySet when they use /tc leave
			if (game.readySet.remove(p) && !game.readySet.isEmpty() && game.readySet.equals(temple.playerSet))
				if(!game.isRunning)
					game.startGame();
			
			// Players are removed from the playerSet when they use /tc leave or the temple is ending
			if(game.playerSet.remove(p))
				if(game.playerSet.isEmpty())
					game.endGame();
		}
	}

	/* ///////////////////////////////////////////////////////////////////// //
	
    MISC METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
    private static void loadMisc() {
    	String[] g = goldPerMob.split("-");
    	try{
	    	if(g[0] != null){
	    		mobGoldMin = Integer.parseInt(g[0]);
	    		if(g[1] != null)
	    			mobGoldRan = Integer.parseInt(g[1])-Integer.parseInt(g[0]);
	    	}
    	} catch(Exception e){
    		mobGoldMin = 0;
    		mobGoldRan = 0;
    	}
    	
		//breakable
    	for(String s : breakableMats.split(",")){
    		s = s.trim();
    		if(!s.isEmpty())
    			breakable.add(Integer.parseInt(s));
    	}
	}
    
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