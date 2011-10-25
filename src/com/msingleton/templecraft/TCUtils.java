package com.msingleton.templecraft;

import java.net.URI;
import java.net.HttpURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.jar.JarFile;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.CaveSpider;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.msingleton.templecraft.games.Adventure;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.games.Spleef;
import com.msingleton.templecraft.games.Zombies;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class TCUtils
{                   
	private static HashMap<String, InventoryStash> inventories = new HashMap<String, InventoryStash>();
    public static final List<Integer>  SWORDS_ID   = new LinkedList<Integer>();
    public static final List<Material> SWORDS_TYPE = new LinkedList<Material>();
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
		inventories.put(player.getName(), new InventoryStash(contents, inventory.getHelmet(), inventory.getChestplate(), 
																inventory.getLeggings(), inventory.getBoots(), player.getHealth(), player.getFoodLevel(), player.getExperience(), player.getGameMode()));	
	}
    
	public static void restorePlayerInventory(Player player) {
		InventoryStash originalContents = inventories.remove(player.getName());
		PlayerInventory playerInv = player.getInventory();
		if(originalContents != null) {
			playerInvFromInventoryStash(playerInv, originalContents);
		}
		player.setHealth(originalContents.getHealth());
		player.setFoodLevel(originalContents.getFoodLevel());
		player.setExperience(originalContents.getExperience());
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
    public static File getConfig(String name)
    {
        new File("plugins/TempleCraft").mkdir();
        File configFile = new File("plugins/TempleCraft/"+name+".yml");
        
        try
        {
            if(!configFile.exists())
            {
                configFile.createNewFile();
            }
        }
        catch(Exception e)
        {
            System.out.println("[TempleCraft] ERROR: Config file could not be created.");
            return null;
        }
        
        return configFile;
    }
    
    public static List<String> getEnabledCommands()
    {
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
    public static int getInt(String path, int def)
    {
    	YamlConfiguration c = YamlConfiguration.loadConfiguration(TempleManager.config);
    	
        int result = c.getInt(path, def);
        c.set(path, result);
        
		try {
			c.save(TempleManager.config);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return result;
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
				c.set("Temples."+s,"");
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
			TempleManager.tellPlayer(p, "You already own the maximum amount of Temples allowed.");
			return false;
		}
		
		if(temple != null){
			TempleManager.tellPlayer(p, "Temple \""+templeName+"\" already exists");
			return false;
		}
		
		if(tp.currentTemple != null){
			TempleManager.tellPlayer(p, "Please leave the current Temple before attempting to create another.");
			return false;
		}
		
		for(char c : templeName.toCharArray()){
			if(!Character.isLetterOrDigit(c)){
				TempleManager.tellPlayer(p, "You may only name a temple using letters or numbers.");
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
			TempleManager.tellPlayer(p, "You do not have permission to edit this temple.");
			return;
		}
		
		if(tp.currentTemple != null && tp.currentTemple != temple){
			TempleManager.tellPlayer(p, "Please leave the current Temple before attempting to edit another.");
			return;
		}
		
		World EditWorld;
		if(TempleManager.templeEditMap.containsKey(temple.templeName))
			EditWorld = TempleManager.templeEditMap.get(temple.templeName);
		else
			EditWorld = temple.loadTemple("Edit");
		
		if(EditWorld == null){
			if(TempleManager.constantWorldNames)
				TempleManager.tellPlayer(p, "Temple is already in use.");
			else
				TempleManager.tellPlayer(p, "Unable to edit temple at this time.");
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
	
	public static boolean deleteTempWorld(World w){
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
		
		if(args.length < 2){
			TempleManager.tellPlayer(p, "Incorrect number of arguements.");
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
			TempleManager.tellPlayer(p, "Temple \""+args[1]+"\" does not exist");
			return;
		}
		
		if(!temple.isSetup){
			TempleManager.tellPlayer(p, "Temple \""+temple.templeName+"\" is not setup");
			return;
		}
		
		if(!TempleManager.modes.contains(mode)){
			TempleManager.tellPlayer(p, "Mode \""+mode+"\" does not exist");
			return;
		}
		
		if(newGame(gameName,temple,mode) != null)
			TempleManager.tellPlayer(p, "New game \""+gameName+"\" created");
		else if(TempleManager.constantWorldNames)
			TempleManager.tellPlayer(p, "Temple is already in use.");
		else
			TempleManager.tellPlayer(p, "Unable to create new game at this time.");
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
		} else if(mode.equals("zombies")){
			game = new Zombies(name, temple, world);
		} else if(mode.equals("spleef")){
			game = new Spleef(name, temple, world);
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
        String site = "http://forums.bukkit.org/threads/30111/";
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
                TempleManager.tellPlayer(p, "Couldn't connect to the TempleCraft thread.");
                return;
            }
            
            // Otherwise, grab the location header to get the real URI.
            String[] url = new URI(con.getHeaderField("Location")).toString().split("-");
            double urlVersion = Double.parseDouble(url[3].replace("v", "")+"."+url[4]);
            
            double thisVersion = Double.parseDouble(TempleManager.plugin.getDescription().getVersion());
            
            // If the current version is the same as the thread version.
            if (thisVersion >= urlVersion)
            {
                if (!response)
                    return;
                    
                TempleManager.tellPlayer(p, "Your version of TempleCraft is up to date!");
                return;
            }
            
            // Otherwise, notify the player that there is a new version.
            TempleManager.tellPlayer(p, "There is a new version of TempleCraft available!");;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void sendDeathMessage(Entity entity, Entity entity2){
    	Game game = TCUtils.getGame(entity);
    	
    	String msg = "";
    	String killed = getDisplayName(entity);
    	String killer = getDisplayName(entity2); 	

    	if(killer.equals("")){
    		String s = entity.getLastDamageCause().getCause().name().toLowerCase();
    		killer = s.substring(0,1).toUpperCase().concat(s.substring(1, s.length()));
    	}
    	
		for(Player p: game.playerSet){
			//String key = p.getName() + "." + entity.getEntityId();
			msg = killer + ChatColor.RED + " killed " + ChatColor.WHITE + killed;
    		
			String s = TempleCraft.method.format(2.0);
			String currencyName = s.substring(s.indexOf(" ") + 1);
			
			if(game.mobGoldMap.containsKey(entity.getEntityId()) && entity2 instanceof Player)
				msg += ChatColor.GOLD + " (+" + game.mobGoldMap.get(entity.getEntityId())/game.playerSet.size() + " "+currencyName+")";
        	
			game.tellPlayer(p, msg);
		}
    }
    
    private static String getDisplayName(Entity entity) {
    	if(entity instanceof Player)
    		return ((Player)entity).getDisplayName();
    	if(entity instanceof CaveSpider)
    		return "Cave Spider";
    	if(entity instanceof PigZombie)
    		return "Pig Zombie";
    	if(entity instanceof Creature)
    		return ((Creature)entity).getClass().getSimpleName().replace("Craft", "");
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
    	}catch(IOException e){
    		e.printStackTrace();
    	}
	}
	
	public static Selection getSelection(Player player){
		Selection sel = TempleManager.worldEdit.getSelection(player);

	    if (sel == null)
	      TempleManager.tellPlayer(player, "Select a region with WorldEdit first.");
	    
		return sel;
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
}