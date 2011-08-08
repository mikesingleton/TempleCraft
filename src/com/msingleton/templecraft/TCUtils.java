package com.msingleton.templecraft;

import java.net.URI;
import java.net.HttpURLConnection;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.config.Configuration;


import com.ryanspeets.bukkit.flatlands.TempleWorldGenerator;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Polygonal2DSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.regions.Region;
import com.tommytony.war.utils.InventoryStash;

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
    
    /* Checks if all inventory and armor slots are empty. */
    public static boolean hasEmptyInventory(Player player)
    {
		ItemStack[] inventory = player.getInventory().getContents();
		ItemStack[] armor     = player.getInventory().getArmorContents();
        
        // For inventory, check for null
        for (ItemStack stack : inventory)
            if (stack != null) return false;
        
        // For armor, check for id 0, or AIR
        for (ItemStack stack : armor)
            if (stack.getTypeId() != 0) return false;
        
        return true;
	}
    
    public static boolean hasPlayerInventory(String playerName) {
		return inventories.containsKey(playerName);
	}

	public static void keepPlayerInventory(Player player) {
		PlayerInventory inventory = player.getInventory();
		ItemStack[] contents = inventory.getContents();
		/*inventory.clear();
		inventory.clear(inventory.getSize() + 0);
		inventory.clear(inventory.getSize() + 1);
		inventory.clear(inventory.getSize() + 2);
		inventory.clear(inventory.getSize() + 3);*/
		inventories.put(player.getName(), new InventoryStash(contents, inventory.getHelmet(), inventory.getChestplate(), 
																inventory.getLeggings(), inventory.getBoots(), player.getHealth()));	
	}
    
	public static void restorePlayerInventory(Player player) {
		InventoryStash originalContents = inventories.remove(player.getName());
		PlayerInventory playerInv = player.getInventory();
		if(originalContents != null) {
			playerInvFromInventoryStash(playerInv, originalContents);
		}
		player.setHealth(originalContents.getHealth());
	}
    
	private static void playerInvFromInventoryStash(PlayerInventory playerInv,
			InventoryStash originalContents) {
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
	
    /* Gives all the items in the input string(s) to the player */
    public static void giveItems(boolean reward, Player p, String... strings)
    {
        // Variables used.
        ItemStack stack;
        int id, amount;
        
        PlayerInventory inv;
        
        if (reward)
            inv = p.getInventory();
        else
            inv = clearInventory(p);
        
        for (String s : strings)
        {
            /* Trim the list, remove possible trailing commas, split by
             * commas, and start the item loop. */
            s = s.trim();
            if (s.endsWith(","))
                s = s.substring(0, s.length()-1);
            String[] items = s.split(",");
            
            // For every item in the list
            for (String i : items)
            {
                /* Take into account possible amount, and if there is
                 * one, set the amount variable to that amount, else 1. */
                i = i.trim();
                String[] item = i.split(":");
                if (item.length >= 2 && item[1].matches("[0-9]+"))
                    amount = Integer.parseInt(item[1]);
                else
                    amount = 1;
                
                // Create ItemStack with appropriate constructor.
                if (item[0].matches("[0-9]+"))
                {
                    id = Integer.parseInt(item[0]);
                    stack = new ItemStack(id, amount);
                    if (!reward && SWORDS_TYPE.contains(stack.getType()))
                        stack.setDurability((short)-3276);
                }
                else
                {
                    stack = makeItemStack(item[0], amount);
                    if (stack == null) continue;
                    if (!reward && SWORDS_TYPE.contains(stack.getType()))
                        stack.setDurability((short)-3276);
                }
                if (item.length == 3 && item[1].matches("[0-9]+"))
                	stack.setDurability((short) Integer.parseInt(item[2]));
                
                inv.addItem(stack);
            }
        }
    }
    
    /* Used for giving items "normally". */
    public static void giveItems(Player p, String... strings)
    {
        giveItems(false, p, strings);
    }
    
    /* Places armor listed in the input string on the Player */
    public static void equipArmor(Player p, String s){
    	// Variables used.
        ItemStack stack;
        int id;
        
        PlayerInventory inv = p.getInventory();
		
        /* Trim the list, remove possible trailing commas and split by commas. */
        s = s.trim();
        if (s.endsWith(","))
            s = s.substring(0, s.length()-1);
        String[] items = s.split(",");
        
        // For every item in the list
        for (String i : items)
        {
            i = i.trim();
            
            // Create ItemStack with appropriate constructor.
            if (i.matches("[0-9]+"))
            {
                id = Integer.parseInt(i);
                stack = new ItemStack(id, 1);
            }
            else
            {
                stack = makeItemStack(i, 1);
                if (stack == null) continue;
            }
            
            // Apply the armor to the correct part of the body
            if(stack.getType() == Material.LEATHER_HELMET || stack.getType() == Material.IRON_HELMET || stack.getType() == Material.GOLD_HELMET || stack.getType() == Material.DIAMOND_HELMET){
            	inv.setHelmet(stack);
            } else if(stack.getType() == Material.LEATHER_CHESTPLATE || stack.getType() == Material.IRON_CHESTPLATE || stack.getType() == Material.GOLD_CHESTPLATE || stack.getType() == Material.DIAMOND_CHESTPLATE){
		        inv.setChestplate(stack);
            } else if(stack.getType() == Material.LEATHER_LEGGINGS || stack.getType() == Material.IRON_LEGGINGS || stack.getType() == Material.GOLD_LEGGINGS || stack.getType() == Material.DIAMOND_LEGGINGS){
		        inv.setLeggings(stack);
            } else if(stack.getType() == Material.LEATHER_BOOTS || stack.getType() == Material.IRON_BOOTS || stack.getType() == Material.GOLD_BOOTS || stack.getType() == Material.DIAMOND_BOOTS){
		        inv.setBoots(stack);
            } else {
            	System.out.println("No Armor was detected by TCUtils.getArmor");
            }
        }
    }
    
    /* Helper method for grabbing a random reward */
    public static String getRandomReward(String rewardlist)
    {
        Random ran = new Random();
        
        String[] rewards = rewardlist.split(",");
        String item = rewards[ran.nextInt(rewards.length)];
        return item.trim();
    }
    
    /* Helper method for making an ItemStack out of a string */
    private static ItemStack makeItemStack(String s, int amount)
    {
        Material mat;
        try
        {
            mat = Material.valueOf(s.toUpperCase());
            return new ItemStack(mat, amount);
        }
        catch (Exception e)
        {
            System.out.println("[TempleCraft] ERROR! Could not create item " + s + ". Check config.yml");
            return null;
        }
    }
    
    
    
    /* ///////////////////////////////////////////////////////////////////// //
    
            INITIALIZATION METHODS
    
    // ///////////////////////////////////////////////////////////////////// */
    
    /**
     * Creates a Configuration object from the config.yml file.
     */
    public static Configuration getConfig(String name)
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
        
        return new Configuration(configFile);
    }
    
    public static List<String> getDisabledCommands()
    {
        Configuration c = TempleManager.config;
        c.load();
        
        String commands = c.getString("settings.disabledcommands", "/kill");
        c.setProperty("settings.disabledcommands", commands);
        c.save();
        
        List<String> result = new LinkedList<String>();
        for (String s : commands.split(","))
            result.add(s.trim());
        
        return result;
    }
    
    /**
     * Grabs the world from the config-file, or the "default" world
     * from the list of worlds in the server object.
     */
    public static World getTempleWorld()
    {        
       	TempleManager.server.createWorld("templeworld", Environment.NORMAL, new TempleWorldGenerator());
        System.out.println("TempleWorld Created!");
        
        return TempleManager.server.getWorld("templeworld");
    }
    
    public static World getEditWorld(Player p, String s)
    {    	
    	World result;    	
    	if(TempleManager.templeEditMap.containsKey(s)){
    		result = TempleManager.templeEditMap.get(s);
    	} else {
    		result = nextAvailableEditWorld(p);
    		TempleManager.templeEditMap.put(s, result);
    	}
    	
    	return result;
    }
    
    private static World nextAvailableEditWorld(Player p) {
    	int size = TempleManager.templeEditMap.size();
    	for(World world : TempleManager.templeEditMap.values()){
    		if(world.getPlayers().isEmpty()){
    			return world;
    		}
    	}
    	
    	if(size >= TempleManager.maxEditWorlds){
    		TempleManager.tellPlayer(p, "All EditWorlds are currently in use.");
    		return null;
    	}
    	
    	World result = TempleManager.server.createWorld("EditWorld_"+size, Environment.NORMAL, new TempleWorldGenerator());
        
        return result;
	}

	/**
     * Grabs the list of classes from the config-file. If no list is
     * found, generate a set of default classes.
     */
    public static List<String> getClasses()
    {
        Configuration c = TempleManager.config;
        c.load();
        
        if (c.getKeys("classes") == null)
        {
        	/* Swords
        	 * Wood:   268
        	 * Stone:  272
        	 * Iron:   267
        	 * Gold:   283
        	 * Diamond:276
        	 * 268,272,267,283,276
        	 * 
        	 * Armor
        	 * Leather:   298,299,300,301
        	 * Chainmail: 302,303,304,305
        	 * Iron:      306,307,308,309
        	 * Gold:      314,315,316,317
        	 * Diamond:   310,311,312,313
        	 */
        	c.setProperty("classes.Knight.items",   "wood_sword");
            c.setProperty("classes.Knight.armor",   "");
            c.setProperty("classes.Knight.enabled", "298,299,300,301,306,307,308,309,314,315,316,317,"+"268,272,267,283,276,"+"320");
            c.setProperty("classes.Archer.items",   "wood_sword");
            c.setProperty("classes.Archer.armor",   "");
            c.setProperty("classes.Archer.enabled", "298,299,300,301,314,315,316,317,"+"268,272,267,"+"262");
            c.setProperty("classes.Tank.items",     "wood_sword");
            c.setProperty("classes.Tank.armor",     "");
            c.setProperty("classes.Tank.enabled",   "298,299,300,301,306,307,308,309,314,315,316,317,310,311,312,313,"+"268,272,267,283"+"320");
            c.setProperty("classes.Chef.items",     "wood_sword");
            c.setProperty("classes.Chef.armor",     "");
            c.setProperty("classes.Chef.enabled",   "298,299,300,301,314,315,316,317,306,307,308,309"+"268,272,267,283,"+"297,320,354,357,322");
            //c.setProperty("classes.Mage.items",     "wood_sword, book");
            //c.setProperty("classes.Mage.armor",     "");
            //c.setProperty("classes.Mage.enabled",   "298,299,300,301,314,315,316,317,"+"268,272,267");
            //c.setProperty("classes.Mage.specialTraits", "magic");
            
            c.save();
        }
        
        return c.getKeys("classes");
    }
    
    /**
     * Generates a map of class names and class items based on the
     * type of items ("items" or "armor") and the config-file.
     * Will explode if the classes aren't well-defined.
     */
    public static Map<String,String> getClassItems(Configuration c, String path, String type)
    {
        c.load();
        
        Map<String,String> result = new HashMap<String,String>();
        
        // Assuming well-defined classes.
        List<String> classes = TempleManager.classes;
        for (String s : classes)
        {
            result.put(s, c.getString(path + s + "." + type, null));
        }
        
        return result;
    }
    
    public static Map<String,String> getClassEnabledItems(Configuration c, String path, String type)
    {
        c.load();
        
        Map<String,String> result = new HashMap<String,String>();
        
        // Assuming well-defined classes.
        List<String> classes = TempleManager.classes;
        for (String s : classes)
        {
            result.put(s, c.getString(path + s + "." + type, null));
        }
        
        return result;
    }
    
    public static Map<String,String> getSpecialTraitsMap()
    {
    	Configuration c = TempleManager.config;
        c.load();
       
        // Set up variables and resulting map.
        List<String> classes = c.getKeys("classes");
        if (classes == null)
            return new HashMap<String,String>();
        
        Map<String,String> result = new HashMap<String,String>();
        String temp;
        
        /* Check if the keys exist in the config-file, if not, set some. */
        for (String classname : classes)
        {
	        if (c.getString("classes." + classname + ".specialTraits") == null)
	        {
	        	c.setProperty("classes." + classname + ".specialTraits", "none");
	        	c.save();
	        }
	        
	        temp = c.getString("classes." + classname + ".specialTraits");
	        if(temp != null)
		        result.put(classname, temp);
        }
        
        // And return the resulting map.
        return result;
    }
    
    /**
     * Grabs the distribution coefficients from the config-file. If
     * no coefficients are found, defaults (10) are added.
     */
    public static int getDistribution(String monster)
    {
        return getDistribution(monster, "default");
    }
    
    public static int getDistribution(String monster, String type)
    {
        Configuration c = TempleManager.config;
        c.load();
        
        if (c.getInt("waves." + type + "." + monster, -1) == -1)
        {
            int dist = 10;
            if (monster.equals("giants") || monster.equals("ghasts"))
                dist = 0;
            
            c.setProperty("waves." + type + "." + monster, dist);
            c.save();
        }
        
        return c.getInt("waves." + type + "." + monster, 0);
    }
    
    /**
     * Grabs an int from the config-file.
     */
    public static int getInt(String path, int def)
    {
        Configuration c = TempleManager.config;
        c.load();
        
        int result = c.getInt(path, def);
        c.setProperty(path, result);
        
        c.save();
        return result;
    }
    
    /**
     * Grabs a boolean from the config-file.
     */
    public static boolean getBoolean(String path, boolean def)
    {
        Configuration c = TempleManager.config;
        c.load();
        
        boolean result = c.getBoolean(path, def);
        c.setProperty(path, result);
        
        c.save();
        return result;
    }
    
    /* ///////////////////////////////////////////////////////////////////// //
    
            REGION AND SETUP METHODS
    
    // ///////////////////////////////////////////////////////////////////// */
    
    /**
     * Checks if the Location object is within the arena region.
     */
    public static boolean inRegion(Location p1, Location p2, Location loc)
    {
        if (!loc.getWorld().getName().equals(TempleManager.world.getName()))
            return false;
        
        // Return false if the location is outside of the region.
        if ((loc.getX() < p1.getX()) || (loc.getX() > p2.getX()))
            return false;
            
        if ((loc.getZ() < p1.getZ()) || (loc.getZ() > p2.getZ()))
            return false;
            
        if ((loc.getY() < p1.getY()) || (loc.getY() > p2.getY()))
            return false;
            
        return true;
    }
    
    /**
     * Maintains the invariant that p1's coordinates are of lower
     * values than their respective counter-parts of p2. Makes the
     * inRegion()-method much faster/easier.
     */
    public static void fixCoords()
    {
    	/*
        Location p1 = getCoords("p1");
        Location p2 = getCoords("p2");
        double tmp;
        
        if (p1 == null || p2 == null)
            return;
            
        if (p1.getX() > p2.getX())
        {
            tmp = p1.getX();
            p1.setX(p2.getX());
            p2.setX(tmp);
        }
        
        if (p1.getY() > p2.getY())
        {
            tmp = p1.getY();
            p1.setY(p2.getY());
            p2.setY(tmp);
        }
        
        if (p1.getZ() > p2.getZ())
        {
            tmp = p1.getZ();
            p1.setZ(p2.getZ());
            p2.setZ(tmp);
        }
        
        setCoords("p1", p1);
        setCoords("p2", p2);
        */
    }
    
    /**
     * Expands the arena region either upwards, downwards, or
     * outwards (meaning on both the X and Z axes).
     */
    public static void expandRegion(Temple temple, Location loc)
    {    	
    	if(temple.p1 == null){
        	temple.p1 = new Location(loc.getWorld(), loc.getX(), 0, loc.getZ());
        	temple.p2 = new Location(loc.getWorld(), loc.getX(), 128, loc.getZ());
        }
    	
        Location p1 = temple.p1;
        Location p2 = temple.p2;
        
        if(loc.getBlockX() > p2.getX())
			p2.setX(loc.getBlockX());
        if(loc.getBlockZ() > p2.getZ())
			p2.setZ(loc.getBlockZ());
        if(loc.getBlockX() < p1.getX())
			p1.setX(loc.getBlockX());
        if(loc.getBlockZ() < p1.getZ())
			p1.setZ(loc.getBlockZ());
    }
    
    public static String spawnList()
    {
        Configuration c = TempleManager.config;
        c.load();
        
        String result = "";
        if (c.getKeys("coords.spawnpoints") == null)
            return result;
        
        for (String s : c.getKeys("coords.spawnpoints"))
            result += s + " ";
        
        return result;
    }
    
    /**
     * Gets the player closest to the input entity. ArrayList implementation
     * means a complexity of O(n).
     */
    
    public static Player getClosestPlayer(Temple temple, Entity e)
    {
        
        // Set up the comparison variable and the result.
        double current = Double.POSITIVE_INFINITY;
        Player result = null;
        
        /* Iterate through the ArrayList, and update current and result every
         * time a squared distance smaller than current is found. */
        for (Player p : temple.playerSet)
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
    
    public static String getRandomSavedRoom(String s) {
		File dir = new File("plugins/TempleCraft/SavedRooms/");
		Random r = new Random();
		boolean go = false;
		File[] files = dir.listFiles();
		if(files == null)
			return null;
		for(File f : files)
			if(f.getName().contains(s)){
				go = true;
				if(r.nextInt(files.length) == 0)
					return f.getName();
			}
		if(go)
			return getRandomSavedRoom(s);
		else
			return null;
	}
    
    public static Temple getTemple(Entity entity){
    	for(Temple temple : TempleManager.templeSet)
        	if(inRegion(temple.p1, temple.p2, entity.getLocation()))
        		return temple;
    	
    	return null;
    }
    
    public static Temple getTempleByName(String templeName){
    	for(Temple temple : TempleManager.templeSet)
        	if(temple.templeName.equals(templeName))
        		return temple;
    	
    	return null;
    }
    
    /**
     * Verifies that all important variables are declared. Returns true
     * if, and only if, the warppoints, region, distribution coefficients,
     * classes and spawnpoints are all set up.
     */
    public static boolean verifyData()
    {
    	/*
        return ((TempleManager.dZombies     != -1)   &&
                (TempleManager.dSkeletons   != -1)   &&
                (TempleManager.dSpiders     != -1)   &&
                (TempleManager.dCreepers    != -1)   &&
                (TempleManager.classes.size() > 0)   &&
                (TempleManager.spawnpoints.size() > 0));
                */
    	return true;
    }   
    
    /**
     * Notifies the player if TempleCraft is set up and ready to be used.
     */
    public static void notifyIfSetup(Player p)
    {
        if (verifyData())
        {
            TempleManager.tellPlayer(p, "TempleCraft is set up and ready to roll!");
        }
    }
    
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
            if (urlVersion == thisVersion)
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
    
    public static void addXP(Entity entity, Map<String, Integer> expBuffer) {
    	Temple temple = TCUtils.getTemple(entity);
    	
    	for(Player p: temple.playerSet){
    		String key = p.getName() + "." + entity.getEntityId();
    		if(expBuffer.containsKey(key)){
    			TemplePlayer player = TempleManager.templePlayerMap.get(p);
    			player.addXp(player.currentClass, expBuffer.remove(key));
    		}
    	}
	}
    
    public static void sendDeathMessage(Entity entity, Entity entity2, Map<String, Integer> expBuffer){
    	Temple temple = TCUtils.getTemple(entity);
    	
    	String msg = "";
    	String killed = "";
    	String killer = "";
    	if(entity2 instanceof Player)
    		killer = ((Player)entity2).getDisplayName();
    		
    	if(entity2 instanceof Creature)
    		killer = ((Creature)entity2).getClass().getSimpleName().replace("Craft", "");
    	
    	if(entity instanceof Player)
    		killed = ((Player)entity).getDisplayName();
    	
    	if(entity instanceof Creature)
    		killed = ((Creature)entity).getClass().getSimpleName().replace("Craft", "");
    	
		for(Player p: temple.playerSet){
			String key = p.getName() + "." + entity.getEntityId();
			
			msg = killer + ChatColor.RED + " killed " + ChatColor.WHITE + killed;
    		
			if(expBuffer.containsKey(key))
        		msg += ChatColor.GREEN + " (" + expBuffer.get(key) + " XP)";
    		
			if(temple.mobGoldMap.containsKey(entity.getEntityId()) && entity2 instanceof Player)
				msg += ChatColor.GOLD + " (+" + temple.mobGoldMap.get(entity.getEntityId())/temple.playerSet.size() + " Gold)";
        	
			temple.tellPlayer(p, msg);
		}
    }
    
    

	public static void removeTemple(Temple temple) {        
        if(temple.p1 == null || temple.p2 == null)
			return;
		
        String fileName = "plugins/TempleCraft/SavedTemples/"+temple.templeName+TempleCraft.fileExtention;
        
        try {
            // Construct a File object for the file to be deleted.
            File target = new File(fileName);

            if (!target.exists()) {
              System.err.println("File " + fileName
                  + " not present to begin with!");
              return;
            }

            // Quick, now, delete it immediately:
            if (target.delete())
              System.err.println("** Deleted " + fileName + " **");
            else
              System.err.println("Failed to delete " + fileName);
          } catch (SecurityException e) {
            System.err.println("Unable to delete " + fileName + "("
                + e.getMessage() + ")");
          }
        
		TempleManager.templeSet.remove(temple);
	}
	
	public static Selection getSelection(Player player){
		Selection sel = TempleManager.worldEdit.getSelection(player);

	    if (sel == null)
	      TempleManager.tellPlayer(player, "Select a region with WorldEdit first.");
	    
		return sel;
	}
	
	public static void newTemple(Player p, String templeName) {		
		Temple temple = TCUtils.getTempleByName(templeName);
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		
		if(tp.ownerOfSet.size() >= TempleManager.maxTemplesPerPerson && !TCPermissionHandler.hasPermission(p,"templecraft.editall")){
			TempleManager.tellPlayer(p, "You already own the maximum amount of Temples allowed.");
			return;
		}
		
		if(temple != null){
			TempleManager.tellPlayer(p, "Temple \""+templeName+"\" already exists");
			return;
		}
		
		if(tp.currentTemple != null){
			TempleManager.tellPlayer(p, "Please leave the current Temple before attempting to create another.");
			return;
		}
		
		for(char c : templeName.toCharArray()){
			if(!Character.isLetterOrDigit(c)){
				TempleManager.tellPlayer(p, "You may only name a temple using letters or numbers.");
				return;
			}
		}
		
		World EditWorld = TCUtils.getEditWorld(p, templeName);
		TempleManager.clearWorld(EditWorld);
		
		if(EditWorld == null)
			return;
		
    	temple = new Temple(templeName);
    	tp.addOwnerOf(templeName);
    	tp.addAccessTo(templeName);
    	editTemple(p, temple);
	}

	public static void editTemple(Player p, Temple temple) {		
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		
		if(!tp.accessToSet.contains(temple.templeName) && !TCPermissionHandler.hasPermission(p,"templecraft.editall")){
			TempleManager.tellPlayer(p, "You do not have permission to edit this temple.");
			return;
		}
		
		if(tp.currentTemple != null && tp.currentTemple != temple){
			TempleManager.tellPlayer(p, "Please leave the current Temple before attempting to edit another.");
			return;
		}
		
		World EditWorld = TCUtils.getEditWorld(p, temple.templeName);
		TempleManager.clearWorld(EditWorld);
		
		if(EditWorld == null)
			return;
		
		temple.loadTemple(EditWorld);
		tp.currentTemple = temple;
		if(!TempleManager.locationMap.containsKey(p))
			TempleManager.locationMap.put(p, p.getLocation());
		teleportToWorld(EditWorld, p);
	}
	
	public static void addAccessTo(Player p, String name, Temple temple){
		Configuration c = TemplePlayer.config;
		c.load();
		if(c.getKeys("Players") != null){
			for(String player : c.getKeys("Players")){
				if(player.toLowerCase().contains(name)){
					String s = c.getString("Players."+player+".Temples.accessTo", temple.templeName);
					if(!s.contains(temple.templeName)){
						c.setProperty("Players."+player+".Temples.accessTo", s + "," + temple.templeName);
						TempleManager.tellPlayer(p, "Player \""+player+"\" added to temple.");
					} else {
						TempleManager.tellPlayer(p, "Player \""+player+"\" already has access to this temple.");
					}
					c.save();
					return;
				}
			}
		}
		
		TempleManager.tellPlayer(p, "Player not found.");
		c.save();
		return;
	}
	
	public static void removeAccessTo(Player p, String name, Temple temple){
		Configuration c = TemplePlayer.config;
		c.load();
		if(c.getKeys("Players") != null){
			for(String player : c.getKeys("Players")){
				if(player.toLowerCase().contains(name)){
					String s = c.getString("Players."+player+".Temples.accessTo", "");
					if(s.contains(","+temple.templeName)){
						c.setProperty("Players."+player+".Temples.accessTo", s.replace(","+temple.templeName, ""));
						TempleManager.tellPlayer(p, "Player \""+player+"\" removed from temple.");
					} else if(s.contains(temple.templeName)) {
						c.setProperty("Players."+player+".Temples.accessTo", s.replace(temple.templeName, ""));
						TempleManager.tellPlayer(p, "Player \""+player+"\" removed from temple.");
					} else {
						TempleManager.tellPlayer(p, "Player \""+player+"\" does not have access to this temple.");
					}
					c.save();
					return;
				}
			}
		}
		
		TempleManager.tellPlayer(p, "Player not found.");
		c.save();
		return;
	}
	
	public static void teleportToWorld(World world, Player p){
		int[] levels = TempleManager.landLevels;
		byte[] mats = TempleManager.landMats;
		int ground = 128;
		for(int i = levels.length-1; i>=0; i--){
			if(mats[i] == 0){
				ground = levels[i-1]+1;
				break;
			}
		}

		p.teleport(new Location(world, -1, ground, -1));
	}
}