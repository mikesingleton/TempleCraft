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
    
    public static List<String> getEnabledCommands()
    {
        Configuration c = TempleManager.config;
        c.load();
        
        String commands = c.getString("settings.enabledcommands", "/tc leave");
        c.setProperty("settings.enabledcommands", commands);
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
    
    public static World getEditWorld(Player p, Temple temple)
    {    	
    	World result;    	
    	if(TempleManager.templeEditMap.containsKey(temple.templeName)){
    		result = TempleManager.templeEditMap.get(temple.templeName);
    	} else {
    		result = nextAvailableEditWorld(p);
    		TempleManager.templeEditMap.put(temple.templeName, result);
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
     * Grabs a string from the config-file.
     */
	public static String getString(Configuration config, String path, String def) {
		Configuration c = config;
        c.load();
        
        String result = c.getString(path, def);
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
    /*
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
    }*/
    
    /**
     * Expands the temple region outward if necessary
     */
    public static void expandRegion(Temple temple, Location loc)
    {    	
    	if(temple.p1 == null || temple.p2 == null){
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
    
    public static Temple getTemple(Entity entity){
    	for(Temple temple : TempleManager.templeSet)
    		if(temple.monsterSet.contains(entity))
   				return temple;
    	return null;
    }
    
    public static Temple getTempleByName(String templeName){
    	for(Temple temple : TempleManager.templeSet)
        	if(temple.templeName.equals(templeName))
        		return temple;
    	return null;
    } 
    
    public static Temple getTempleByWorld(World w) {
		for(Temple temple : TempleManager.templeSet)
			if(TempleManager.templeEditMap.get(temple.templeName).equals(w))
				return temple;
		return null;
	}
    
    public static Player getPlayerByName(String playerName){
		for(Player p : TempleManager.server.getOnlinePlayers())
			if(p.getName().equals(playerName))
				return p;
		return null;
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
    
    public static void sendDeathMessage(Entity entity, Entity entity2){
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
    	
    	if(killer.equals("")){
    		String s = entity.getLastDamageCause().getCause().name().toLowerCase();
    		killer = s.substring(0,1).toUpperCase().concat(s.substring(1, s.length()));
    	}
    	
		for(Player p: temple.playerSet){
			//String key = p.getName() + "." + entity.getEntityId();
			msg = killer + ChatColor.RED + " killed " + ChatColor.WHITE + killed;
    		
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
		
		if(tp.ownedTemples >= TempleManager.maxTemplesPerPerson && !TCPermissionHandler.hasPermission(p,"templecraft.editall")){
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
		
    	temple = new Temple(templeName);
    	tp.ownedTemples++;
    	temple.addOwner(p.getName());
		
		World EditWorld = TCUtils.getEditWorld(p, temple);
		
		if(EditWorld == null)
			return;
		
    	editTemple(p, temple);
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
		
		World EditWorld = getEditWorld(p, temple);
		
		if(EditWorld == null)
			return;
		
		TempleManager.clearWorld(EditWorld);
		
		if(temple.editorSet.isEmpty()){
			EditWorld.setTime(8000);
			EditWorld.setStorm(false);
			temple.loadTemple(EditWorld);
		}
		temple.editorSet.add(p);
		tp.currentTemple = temple;
		if(!TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.keepPlayerInventory(p);
		if(!TempleManager.locationMap.containsKey(p))
			TempleManager.locationMap.put(p, p.getLocation());
		teleportToWorld(EditWorld, temple, p);
	}
	
	public static void teleportToWorld(World world, Temple temple, Player p){
		int[] levels = TempleManager.landLevels;
		byte[] mats = TempleManager.landMats;
		int ground = 128;
		for(int i = levels.length-1; i>=0; i--){
			if(mats[i] == 0){
				ground = levels[i-1]+1;
				break;
			}
		}

		if(temple.p1 != null)
			p.teleport(new Location(world, (temple.p1.getX()-1) , ground, (temple.p1.getZ())-1));
		else
			p.teleport(new Location(world, -1, ground, -1));
	}
}