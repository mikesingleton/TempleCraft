package com.msingleton.templecraft;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.herocraftonline.dev.heroes.hero.Hero;
import com.msingleton.templecraft.games.Adventure;
import com.msingleton.templecraft.games.Arena;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.games.PVP;
import com.msingleton.templecraft.games.Race;
import com.msingleton.templecraft.games.Spleef;
import com.msingleton.templecraft.util.InventoryStash;
import com.msingleton.templecraft.util.Translation;

public class TCUtils
{                   
	private static HashMap<String, InventoryStash> inventories = new HashMap<String, InventoryStash>();
    public static final List<Integer>  SWORDS_ID   = new LinkedList<Integer>();
    public static final List<Material> SWORDS_TYPE = new LinkedList<Material>();
    public static final int MAX_HEALTH = 20;
    public static final int MAX_FOOD = 20;
    static
    {
        SWORDS_TYPE.add(Material.WOOD_SWORD);
        SWORDS_TYPE.add(Material.STONE_SWORD);
        SWORDS_TYPE.add(Material.GOLD_SWORD);
        SWORDS_TYPE.add(Material.IRON_SWORD);
        SWORDS_TYPE.add(Material.DIAMOND_SWORD);
    }

    
    /* ///////////////////////////////////////////////////////////////////// //
    
            INVENTORY AND REWARD METHODS
    
    // ///////////////////////////////////////////////////////////////////// */

    /* Clears the players inventory and armor slots. */
    public static PlayerInventory clearInventory(Player p)
    {
        PlayerInventory inv = p.getInventory();
        inv.clear();
        inv.setHelmet(null);
        inv.setChestplate(null);
        inv.setLeggings(null);
        inv.setBoots(null);
        return inv;
    }
    
    public static boolean hasPlayerInventory(String playerName) {
		return inventories.containsKey(playerName);
	}

	public static void keepPlayerInventory(Player player) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		double playerHealth;
		if(TempleCraft.heroManager != null)
			playerHealth = TempleCraft.heroManager.getHero(player).getHealth();
		else
			playerHealth = player.getHealth();
		inventories.put(player.getName(), new InventoryStash(contents, inventory.getHelmet(), inventory.getChestplate(), 
																inventory.getLeggings(), inventory.getBoots(), playerHealth, player.getFoodLevel(), player.getTotalExperience(), player.getGameMode()));	
	}
    
	public static void restorePlayerInventory(Player player) {
		InventoryStash originalContents = inventories.remove(player.getName());
		PlayerInventory playerInv = player.getInventory();
		if(originalContents != null) {
			playerInvFromInventoryStash(playerInv, originalContents);
		}
		if(TempleCraft.heroManager != null){
			Hero hero = TempleCraft.heroManager.getHero(player);
			hero.setHealth(originalContents.getHealth());
			player.setHealth((int)(originalContents.getHealth()/hero.getMaxHealth()));
		} else {
			player.setHealth((int)originalContents.getHealth());
		}
		player.setFoodLevel(originalContents.getFoodLevel());
		player.setTotalExperience(originalContents.getExperience());
		player.setGameMode(originalContents.getGameMode());
	}
    
	private static void playerInvFromInventoryStash(PlayerInventory playerInv, InventoryStash originalContents) {
		playerInv.clear();
		playerInv.clear(playerInv.getSize() + 0);
		playerInv.clear(playerInv.getSize() + 1);
		playerInv.clear(playerInv.getSize() + 2);
		playerInv.clear(playerInv.getSize() + 3);	// helmet/blockHead
		for(ItemStack item : originalContents.getContents()) {
			if(item != null && item.getTypeId() != 0) {
				playerInv.addItem(item);
			}
		}
		if(originalContents.getHelmet() != null && originalContents.getHelmet().getType() != Material.AIR) {
			playerInv.setHelmet(originalContents.getHelmet());
		}
		if(originalContents.getChest() != null && originalContents.getChest().getType() != Material.AIR) {
			playerInv.setChestplate(originalContents.getChest());
		}
		if(originalContents.getLegs() != null && originalContents.getLegs().getType() != Material.AIR) {
			playerInv.setLeggings(originalContents.getLegs());
		}
		if(originalContents.getFeet() != null && originalContents.getFeet().getType() != Material.AIR) {
			playerInv.setBoots(originalContents.getFeet());
		}
	}
    
    /* ///////////////////////////////////////////////////////////////////// //
    
            INITIALIZATION METHODS (CONFIGURATION)
    
    // ///////////////////////////////////////////////////////////////////// */
    
    /**
     * Creates a Configuration object from the config.yml file.
     */
    public static File getConfig(String name){
        new File("plugins/TempleCraft").mkdir();
        File configFile = new File("plugins/TempleCraft/"+name+".yml");
        
        try{
            if(!configFile.exists())
                configFile.createNewFile();
        }catch(Exception e){
            System.out.println("[TempleCraft] ERROR: Config file could not be created.");
            return null;
        }
        
        return configFile;
    }
    
    public static List<String> getEnabledCommands(){
        YamlConfiguration c = YamlConfiguration.loadConfiguration(TempleManager.config);
        
        String commands = c.getString("settings.enabledcommands", "/tc");
        c.set("settings.enabledcommands", commands);
        try {
			c.save(TempleManager.config);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        List<String> result = new LinkedList<String>();
        for (String s : commands.split(","))
            result.add(s.trim());
        
        return result;
    }
    
    public static String getNextAvailableTempWorldName(String type) {
    	String name;
    	
    	if(type.equals("Edit")){
    		int size = TempleManager.templeEditMap.size();
    		if(size >= TempleManager.maxEditWorlds)
    			return null;
    		else
    			name = "TCEditWorld_"+size;
    	} else {
    		Set<String> worldNames = new HashSet<String>();
    		for(World w : TempleManager.server.getWorlds())
    			worldNames.add(w.getName());
    			int i = 0;
    		do{
    			name = "TC"+type+"World_"+i;
    			i++;
    		}while(worldNames.contains(name));
    	}
    	return name;
	}
    
    /**
     * Grabs an int from the config-file.
     */
    public static int getInt(File configFile, String path, int def) {
    	YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);
    	
        int result = c.getInt(path, def);
        c.set(path, result);
        
		try {
			c.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return result;
    }
    
    private static void setInt(File configFile, String path, int value) {
    	YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);
        c.set(path, value);
		try {
			c.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    /**
     * Grabs a string from the config-file.
     */
	public static String getString(File configFile, String path, String def) {
		YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);
        
        String result = c.getString(path, def);
        c.set(path, result);
        
        try {
			c.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return result;
	}
    
    /**
     * Grabs a boolean from the config-file.
     */
    public static boolean getBoolean(File configFile, String path, boolean def)
    {
    	YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);
        
        boolean result = c.getBoolean(path, def);
        c.set(path, result);
        
		try {
			c.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(result + "\t" + path);
        return result;
    }
    
    public static void setBoolean(File configFile, String path, boolean key)
    {
    	YamlConfiguration c = YamlConfiguration.loadConfiguration(configFile);
        
        c.set(path, key);
        
        try {
			c.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Creates a Configuration object from the config.yml file.
     */
    public static void cleanConfigFiles()
    {
        cleanTempleConfig();
    }
    
    private static void cleanTempleConfig() {
    	YamlConfiguration c = YamlConfiguration.loadConfiguration(getConfig("temples"));

		if(!c.getKeys(false).contains("Temples"))
			return;
		
		Set<String> list = c.getConfigurationSection("Temples").getKeys(false);
		for(String s : list){
			Temple temple = getTempleByName(s);
			if(temple == null)
				c.getConfigurationSection("Temples").set(s, null);
		}
		try {
			c.save(getConfig("temples"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* ///////////////////////////////////////////////////////////////////// //
    
			PLAYER METHODS

	// ///////////////////////////////////////////////////////////////////// */
    
    public static Player getPlayerByName(String playerName){
		for(Player p : TempleManager.server.getOnlinePlayers())
			if(p.getName().equals(playerName))
				return p;
		return null;
	}
    
    /**
     * Turns the current set of players into an array, and grabs a random
     * element out of it.
     */
    public static Player getRandomPlayer()
    {
        Random random = new Random();
        Object[] array = TempleManager.server.getOnlinePlayers();
        return (Player) array[random.nextInt(array.length)];
    }
    
    /* ///////////////////////////////////////////////////////////////////// //
    
    		TEMPLE EDIT METHODS

	// ///////////////////////////////////////////////////////////////////// */

    public static Temple getTempleByName(String templeName){
    	templeName = templeName.toLowerCase();
    	for(Temple temple : TempleManager.templeSet)
        	if(temple.templeName.equals(templeName))
        		return temple;
    	return null;
    } 
    
    public static Temple getTempleByWorld(World w) {
		for(Temple temple : TempleManager.templeSet){
			World world = TempleManager.templeEditMap.get(temple.templeName);
			if(world != null && world.equals(w))
				return temple;
		}
		return null;
	}
    
    public static boolean newTemple(Player p, String templeName, String ChunkGen, boolean edit) {		
		Temple temple = TCUtils.getTempleByName(templeName);
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		
		if(tp.ownedTemples >= TempleManager.maxTemplesPerPerson && !TCPermissionHandler.hasPermission(p,"templecraft.editall")){
			TempleManager.tellPlayer(p, Translation.tr("maxTemplesOwned"));
			return false;
		}
		
		if(temple != null){
			TempleManager.tellPlayer(p, Translation.tr("templeAE",temple.templeName));
			return false;
		}
		
		if(tp.currentTemple != null){
			TempleManager.tellPlayer(p, Translation.tr("mustLeaveTemple"));
			return false;
		}
		
		for(char c : templeName.toCharArray()){
			if(!Character.isLetterOrDigit(c)){
				TempleManager.tellPlayer(p, Translation.tr("nameFail"));
				return false;
			}
		}
		
    	temple = new Temple(templeName);
    	tp.ownedTemples++;
    	temple.addOwner(p.getName());
    	
    	if(ChunkGen != null)
	    	temple.ChunkGeneratorFile = getChunkGeneratorByName(ChunkGen);
		
		if(!edit)
			return true;

		editTemple(p, temple);
    	
    	return true;
	}
    
    private static File getChunkGeneratorByName(String ChunkGen) {
    	File cgFolder = new File("plugins/TempleCraft/ChunkGenerators");
    	File temp = null;
    	ChunkGen = ChunkGen.toLowerCase();
    	if(cgFolder.isDirectory()){
			for(File f : cgFolder.listFiles()){
				String name = f.getName().toLowerCase();
	    		if(name.replace(".jar", "").equals(ChunkGen.replace(".jar", ""))){
		    		return f;
	    		} else if(name.startsWith(ChunkGen)){
	    			temp = f;
	    		}
	    	}
    	}
		return temp;
    }

	public static void editTemple(Player p, Temple temple) {	
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		
		if(!temple.accessorSet.contains(p.getName()) && !temple.ownerSet.contains(p.getName()) && !TCPermissionHandler.hasPermission(p,"templecraft.editall")){
			TempleManager.tellPlayer(p, Translation.tr("cantEdit"));
			return;
		}
		
		if(tp.currentTemple != null && tp.currentTemple != temple){
			TempleManager.tellPlayer(p, Translation.tr("mustLeaveTemple"));
			return;
		}
		
		World EditWorld;
		if(TempleManager.templeEditMap.containsKey(temple.templeName))
			EditWorld = TempleManager.templeEditMap.get(temple.templeName);
		else
			EditWorld = temple.loadTemple("Edit");
		
		if(EditWorld == null){
			if(TempleManager.constantWorldNames)
				TempleManager.tellPlayer(p, Translation.tr("templeInUse", temple.templeName));
			else
				TempleManager.tellPlayer(p, Translation.tr("editTempleFail"));
			return;
		}
		
		// Only clears and loads Temple if no one is already editing
		if(temple.editorSet.isEmpty()){
			TempleManager.templeEditMap.put(temple.templeName,EditWorld);
			EditWorld.setTime(8000);
			EditWorld.setStorm(false);
		}
		temple.editorSet.add(p);
		tp.currentTemple = temple;
		if(!TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.keepPlayerInventory(p);
		if(!TempleManager.locationMap.containsKey(p))
			TempleManager.locationMap.put(p, p.getLocation());
		Location lobbyLoc = temple.getLobbyLoc(EditWorld);
		if(lobbyLoc != null)
			p.teleport(lobbyLoc);
		else
			p.teleport(new Location(EditWorld,-1, EditWorld.getHighestBlockYAt(-1, -1)+2, -1));
		p.setGameMode(GameMode.CREATIVE);
	}
	
	public static void convertTemple(Player p, Temple temple) {
		File tcffile = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName+TempleCraft.fileExtention);
		File file = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName);
			
		if(!tcffile.exists() && !file.exists())
			return;
		
		World ConvertWorld = temple.loadTemple("Convert");
		
		if(ConvertWorld == null)
			return;
		
		ConvertWorld.setAutoSave(false);
		ConvertWorld.setKeepSpawnInMemory(false);
		ConvertWorld.setTime(8000);
		ConvertWorld.setStorm(false);
		ConvertWorld.setSpawnLocation(-1, ConvertWorld.getHighestBlockYAt(-1, -1)+2, -1);
		
		temple.saveTemple(ConvertWorld, p);
		deleteTempWorld(ConvertWorld);
	}
	
	public static void removeTemple(Temple temple) {		
        String fileName = "plugins/TempleCraft/SavedTemples/"+temple.templeName;
        //Remove all files associated with the temple
        try {
            File folder = new File(fileName);
            deleteFolder(folder);
	    } catch (SecurityException e) {
	    	System.err.println("Unable to delete " + fileName + "("+ e.getMessage() + ")");
	    }
        
		TempleManager.templeSet.remove(temple);
	}
	
	public static void renameTemple(Temple temple, String arg) {
		String folder = "plugins/TempleCraft/SavedTemples/";
		File current = new File(folder+temple.templeName);
		File result  = new File(folder+arg);
		if(result.exists())
			return;
		current.renameTo(result);
		File configFile = TCUtils.getConfig("temples");
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		if(config.isConfigurationSection("Temples."+temple.templeName)){
			ConfigurationSection src = config.getConfigurationSection("Temples."+temple.templeName);
			ConfigurationSection dst = config.createSection("Temples."+arg);
			copyConfigurationSection(src,dst);
			config.getConfigurationSection("Temples").set(temple.templeName, null);
		}
		temple.templeName = arg;
        try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void copyConfigurationSection(ConfigurationSection src, ConfigurationSection dst){
		for(String s : src.getKeys(false)){
			if(src.isConfigurationSection(src.getCurrentPath()+s)){
				dst.createSection(s);
				copyConfigurationSection(src.getConfigurationSection(s),dst.getConfigurationSection(s));
			} else {
				dst.set(s, src.get(s));
			}
		}
	}

	public static void setTempleMaxPlayers(Temple temple, int value) {
		TCUtils.setInt(getConfig("temples"),"Temples."+temple.templeName+".maxPlayersPerGame", value);
		temple.maxPlayersPerGame = value;
	}

	public static void removePlayers(World world){
		Set<Player> tempSet = new HashSet<Player>();
		for(Player p: world.getPlayers())
			tempSet.add(p);
		World w;
		for(Player p : tempSet){
			TemplePlayer tp = TempleManager.templePlayerMap.get(p);
			if(tp == null){
				w = getNonTempWorld();
				if(w == null)
					p.kickPlayer("Could not find a non temporary world to teleport you to.");
				p.teleport(new Location(w,0,0,0));
			} else {
				TempleManager.playerLeave(p);
			}
		}
	}
	
	private static World getNonTempWorld() {
		World ntw = TempleManager.server.getWorld("world");
		if(ntw == null)
			for(World w : TempleManager.server.getWorlds())
				if(!TCUtils.isTCWorld(w))
					ntw = w;
		return ntw;
	}
	
	public static boolean deleteTempWorld(World w){
		removePlayers(w);
		if(w == null || !w.getPlayers().isEmpty())
			return false;
		w.setAutoSave(false);
		File folder = new File(w.getName());
		for(Entity e : w.getEntities())
			e.remove();
		TempleManager.server.unloadWorld(w, true);
		if(folder.exists())
			deleteFolder(folder);
		return true;
	}
	
	public static void deleteTempWorlds(){
		for(World w : TempleManager.server.getWorlds())
			if(TCUtils.isTCWorld(w))
				deleteTempWorld(w);
	}
	
	private static void deleteFolder(File folder){
		try{
			for(File f : folder.listFiles()){
				if(f.isDirectory())
					deleteFolder(f);
				else
					f.delete();
			}
			folder.delete();
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
    
    /* ///////////////////////////////////////////////////////////////////// //
    
    		GAME METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	public static void newGameCommand(Player p, String[] args){
		Temple temple;
		String gameName;
		String mode;
		
		TempleManager.tellPlayer(p, Translation.tr("newGame"));
		if(args.length < 2){
			TempleManager.tellPlayer(p, Translation.tr("incorrectArguments"));
			return;
		} else if(args.length < 3){
			temple = getTempleByName(args[1]);
			mode = "adventure";
			gameName = getUniqueGameName(args[1], mode);
		} else {
			temple = getTempleByName(args[1]);
			mode = args[2].toLowerCase();
			gameName = getUniqueGameName(args[1], mode);
		}
		
		// Check the validity of the arguements
		if(temple == null){
			TempleManager.tellPlayer(p, Translation.tr("templeDNE", args[1]));
			return;
		}
		
		if(!temple.isSetup){
			TempleManager.tellPlayer(p, Translation.tr("templeNotSetup", temple.templeName));
			return;
		}
		
		if(!TempleManager.modes.contains(mode)){
			TempleManager.tellPlayer(p, Translation.tr("modeDNE", mode));
			return;
		}
		
		if(temple.maxPlayersPerGame < 1 && temple.maxPlayersPerGame != -1){
			TempleManager.tellPlayer(p, Translation.tr("templeFull", temple.templeName));
			return;
		}
		
		if(newGame(gameName,temple,mode) != null)
			TempleManager.tellPlayer(p, Translation.tr("game.created",gameName));
		else if(TempleManager.constantWorldNames)
			TempleManager.tellPlayer(p, Translation.tr("templeInUse",temple.templeName));
		else
			TempleManager.tellPlayer(p, Translation.tr("newGameFail"));
	}

	public static Game newGame(String name, Temple temple, String mode){
		if(temple == null)
			return null;
		
		Game game;
		
		World world = temple.loadTemple("Temple");
		
		// Checks to make sure the world is not null
		if(world == null)
			return null;
		
		if(mode.equals("adventure")){
			game = new Adventure(name, temple, world);
		} else if(mode.equals("race")){
			game = new Race(name, temple, world);
		} else if(mode.equals("spleef")){
			game = new Spleef(name, temple, world);
		} else if(mode.equals("pvp")){
			game = new PVP(name, temple, world);
		} else if(mode.equals("arena")){
			game = new Arena(name, temple, world);
		} else {
			game = new Adventure(name, temple, world);
		}
		return game;
	}
	
	public static Game getGame(Entity entity){
    	for(Game game : TempleManager.gameSet)
    		if(game.monsterSet.contains(entity))
   				return game;
    	return null;
    }
	
    public static Game getGameByName(String gameName){
    	for(Game game : TempleManager.gameSet)
        	if(game.gameName.startsWith(gameName))
        		return game;
    	return null;
    }
    
    public static Game getGameByWorld(World world){
    	for(Game game : TempleManager.gameSet)
        	if(game.world.equals(world))
        		return game;
    	return null;
    } 
    
    public static String getUniqueGameName(String templeName, String mode) {
    	String gameName = "";
    	int i = 1;
    	do{
			gameName = templeName+mode.substring(0,3)+i;
			i++;
		}while(getGameByName(gameName) != null);
		return gameName;
	}
    
    /**
     * Gets the player closest to the input entity. ArrayList implementation
     * means a complexity of O(n).
     */
    
    public static Player getClosestPlayer(Game game, Entity e)
    {
        
        // Set up the comparison variable and the result.
        double current = Double.POSITIVE_INFINITY;
        Player result = null;
        
        /* Iterate through the ArrayList, and update current and result every
         * time a squared distance smaller than current is found. */
        for (Player p : game.playerSet)
        {
            double dist = distance(p.getLocation(), e.getLocation());
            if (dist < current)
            {
                current = dist;
                result = p;
            }
        }
        return result;
    }
    
    /**
     * Calculates the squared distance between locations.
     */
    public static double distance(Location loc, Location loc2)
    {
        double d4 = loc.getX() - loc2.getX();
        double d5 = loc.getY() - loc2.getY();
        double d6 = loc.getZ() - loc2.getZ();
        
        return Math.sqrt(d4*d4 + d5*d5 + d6*d6);
    }
    
    /* ///////////////////////////////////////////////////////////////////// //
    
            MISC METHODS
    
    // ///////////////////////////////////////////////////////////////////// */
    
    /**
     * Checks if there is a new update of TempleCraft and notifies the
     * player if the boolean specified is true
     */
    public static void checkForUpdates(final Player p, boolean response)
    {
        String site = "http://dev.bukkit.org/server-mods/templecraft/images/1";
        try
        {
            // Make a URL of the site address
            URI baseURL = new URI(site);
            
            // Open the connection and don't redirect.
            HttpURLConnection con = (HttpURLConnection) baseURL.toURL().openConnection();
            con.setInstanceFollowRedirects(false);
            
            String header = con.getHeaderField("Location");
            
            // If something's wrong with the connection...
            if (header == null)
            {
                TempleManager.tellPlayer(p, Translation.tr("checkForUpdatesFail"));
                return;
            }
            
            // Otherwise, grab the location header to get the real URI.
            String[] url = new URI(con.getHeaderField("Location")).toString().split("-");
            double urlVersion = Double.parseDouble(url[4].replace("v", "")+"."+url[5]);
            
            double thisVersion = Double.parseDouble(TempleManager.plugin.getDescription().getVersion());
            
            // If the current version is the same as the thread version.
            if (thisVersion >= urlVersion)
            {
                if (!response)
                    return;
                    
                TempleManager.tellPlayer(p, Translation.tr("upToDate"));
                return;
            }
            
            // Otherwise, notify the player that there is a new version.
            TempleManager.tellPlayer(p, Translation.tr("notUpToDate"));
        }
        catch (Exception e)
        {
        }
    }
    
    public static void sendDeathMessage(Game game, Entity entity, Entity entity2){
  	
    	String msg = "";
    	String killed = getDisplayName(entity);
    	String killer = getDisplayName(entity2); 	

    	if(killer.equals("")){
    		String s = entity.getLastDamageCause().getCause().name().toLowerCase();
    		killer = s.substring(0,1).toUpperCase().concat(s.substring(1, s.length()));
    	}
    	
		for(Player p: game.playerSet){
			//String key = p.getName() + "." + entity.getEntityId();
			msg = Translation.tr("killMessage", killer, killed);
    		
			if(TempleCraft.method != null){
				String s = TempleCraft.method.format(2.0);
				String currencyName = s.substring(s.indexOf(" ") + 1);
			
				if(game.mobGoldMap.containsKey(entity.getEntityId()) && entity2 instanceof Player)
					msg += ChatColor.GOLD + " (+" + game.mobGoldMap.get(entity.getEntityId())/game.playerSet.size() + " "+currencyName+")";
			}
			
			game.tellPlayer(p, msg);
		}
    }
    
    private static String getDisplayName(Entity entity) {
    	if(entity instanceof Player)
    		return ((Player)entity).getDisplayName();
    	if(entity instanceof EnderDragon)
    		return "Ender Dragon";
    	if(entity instanceof Creature){
    		String name = ((Creature)entity).getClass().getSimpleName().replace("Craft", "");
    		
    		StringBuilder formatted = new StringBuilder();
    		for(int i = 0; i < name.length(); i++){
    			if(i != 0 && Character.isUpperCase(name.charAt(i)))
    				formatted.append(" ");
    			formatted.append(name.charAt(i));
    		}
    		return formatted.toString();
    	}
    	if(entity instanceof Ghast)
    		return "Ghast";
    	if(entity instanceof MagmaCube)
    		return "Magma Cube";
    	if(entity instanceof Slime)
    		return "Slime";
		return "";
    }
    
	public static void copyFromJarToDisk(String entry, File folder){
    	try{
			JarFile jar = new JarFile(TempleManager.plugin.getPluginFile());
			InputStream is = jar.getInputStream(jar.getJarEntry(entry));
			OutputStream os = new FileOutputStream(new File(folder, entry));
			byte[] buffer = new byte[4096];
			int length;
			while (is!=null&&(length = is.read(buffer)) > 0) {
			    os.write(buffer, 0, length);
			}
			os.close();
			is.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}

	public static boolean isTCWorld(World world) {
		String name = world.getName();
		if(name.startsWith("TCTempleWorld_") || name.startsWith("TCEditWorld_") || name.startsWith("TCConvertWorld_"))
			return true;
		return false;
	}
	
	public static boolean isTCEditWorld(World world) {
		if(world.getName().startsWith("TCEditWorld_") || TempleManager.templeEditMap.containsValue(world))
			return true;
		return false;
	}

	public static void getSignificantBlocks(Player p, int radius) {
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Temple temple = tp.currentTemple;
		Location ploc = p.getLocation();
		int x1 = ploc.getBlockX()-radius;
		int x2 = ploc.getBlockX()+radius;
		int y1 = ploc.getBlockY()-radius;
		int y2 = ploc.getBlockY()+radius;
		int z1 = ploc.getBlockZ()-radius;
		int z2 = ploc.getBlockZ()+radius;
		if(y1<=0)
			y1 = 1;
		if(y2>p.getWorld().getMaxHeight())
			y2 = p.getWorld().getMaxHeight();
		
		TempleManager.tellPlayer(p, Translation.tr("findingSigBlocks","("+x1+","+y1+","+z1+")","("+x2+","+y2+","+z2+")"));
				
		Map<Integer, Integer> foundBlocks = new HashMap<Integer, Integer>();
		
		for(int i = x1;i<=x2;i++)
			for(int j = y1;j<=y2;j++){
				for(int k = z1;k<=z2;k++){
					int id = p.getWorld().getBlockTypeIdAt(i, j, k);
					if(id == Temple.goldBlock || id == Temple.diamondBlock || id == Temple.ironBlock || id == 63 || id == 68 || (id == Temple.mobSpawner && j>5)){
		        		Location loc = new Location(p.getWorld(),i,j,k);
		        		if(!temple.coordLocSet.contains(loc)){
		        			temple.coordLocSet.add(loc);
		        			if(foundBlocks.containsKey(id))
		        				foundBlocks.put(id, foundBlocks.remove(id)+1);
		        			else
		        				foundBlocks.put(id, 1);
		        		}
		        	}
				}
			}
		
		// Print Results
		for(Integer id : foundBlocks.keySet())
			TempleManager.tellPlayer(p, Translation.tr("foundSigBlocks",foundBlocks.get(id),getMaterialName(Material.getMaterial(id).name())));
		
		TempleManager.tellPlayer(p, Translation.tr("done"));
	}
	
	public static String getMaterialName(String s){
		StringBuilder sb = new StringBuilder();
		String[] words = s.split("_");
		for(int i = 0; i<words.length;i++){
			sb.append(Character.toUpperCase(words[i].charAt(0))+words[i].toLowerCase().substring(1));
			if((i+1)<words.length)
				sb.append(" ");
		}
		return sb.toString();
	}

	public static Map<String,Integer> sortMapByValues(Map<String, Integer> standings) {
		List<String> keyList = new ArrayList<String>();
	    List<Integer> valueList = new ArrayList<Integer>();
	    for(String s : standings.keySet()){
			keyList.add(s);
			valueList.add(standings.get(s));
		}
	    Set<Integer> sortedSet = new TreeSet<Integer>(valueList);
	    
	    Object[] sortedArray = sortedSet.toArray();
	    int size = sortedArray.length;

	    Map<String, Integer> result = new LinkedHashMap<String, Integer>();
	    for (int i = 0; i < size; i++)
	      result.put(keyList.get(valueList.indexOf(sortedArray[i])), (Integer) sortedArray[i]);

		return result;
	}

	public static String getWoolColor(int team) {
		switch(team){
			case 0:
				return ChatColor.WHITE+"White";
			case 1:
				return ChatColor.GOLD+"Orange";
			case 2:
				return ChatColor.LIGHT_PURPLE+"Magenta";
			case 3:
				return ChatColor.AQUA+"Light Blue";
			case 4:
				return ChatColor.YELLOW+"Yellow";
			case 5:
				return ChatColor.GREEN+"Lime";
			case 6:
				return ChatColor.LIGHT_PURPLE+"Pink";
			case 7:
				return ChatColor.DARK_GRAY+"Gray";
			case 8:
				return ChatColor.GRAY+"Light Gray";
			case 9:
				return ChatColor.DARK_AQUA+"Cyan";
			case 10:
				return ChatColor.DARK_PURPLE+"Purple";
			case 11:
				return ChatColor.BLUE+"Blue";
			case 12:
				return ChatColor.GOLD+"Brown";
			case 13:
				return ChatColor.DARK_GREEN+"Green";
			case 14:
				return ChatColor.RED+"Red";
			case 15:
				return ChatColor.BLACK+"Black";
			default:
				return "";
		}
	}

	public static void restoreHealth(Player p) {
		if(TempleCraft.heroManager != null){
			Hero hero = TempleCraft.heroManager.getHero(p);
			hero.setHealth(hero.getMaxHealth());
			hero.setMana(0);
		}
		p.setHealth(MAX_HEALTH);
	}
}