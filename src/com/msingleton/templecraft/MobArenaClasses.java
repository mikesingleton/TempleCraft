package com.msingleton.templecraft;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.config.Configuration;

import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;

public class MobArenaClasses extends PlayerListener{

	protected static Configuration config             = null;
	protected static boolean enabled                  = false;
	protected static List<String> classes             = new LinkedList<String>();;
	protected static Map<Player,String> classMap      = new HashMap<Player,String>();
	protected static Map<String,String> classItemMap  = new HashMap<String,String>();
	protected static Map<String,String> classArmorMap = new HashMap<String,String>();
	public static final List<Material> SWORDS_TYPE    = new LinkedList<Material>();
    static
    {
        SWORDS_TYPE.add(Material.WOOD_SWORD);
        SWORDS_TYPE.add(Material.STONE_SWORD);
        SWORDS_TYPE.add(Material.GOLD_SWORD);
        SWORDS_TYPE.add(Material.IRON_SWORD);
        SWORDS_TYPE.add(Material.DIAMOND_SWORD);
    }
	public MobArenaClasses(TempleCraft templeCraft) {
		enabled = TCUtils.getBoolean("settings.enableclasses", false);
		if(enabled){
			config  = TCUtils.getConfig("classes");
			classes = getClasses();
			classItemMap = getClassItems(config, "classes.","items");
			classArmorMap = getClassItems(config, "classes.","armor");
		}
	}

	 public void onPlayerInteract(PlayerInteractEvent event){    
		 
		 if(!TempleManager.isEnabled)
				return;
		 
        Player p = event.getPlayer();
        Action a = event.getAction();
			
        // Signs
        if (event.hasBlock() && event.getClickedBlock().getState() instanceof Sign){        
	        // Cast the block to a sign to get the text on it.
	        Sign sign = (Sign) event.getClickedBlock().getState();
	        handleSign(p, a, sign);
		 }
	 }
	 
	 private void handleSign(Player p, Action a, Sign sign) {
			// Check if the first line of the sign is a class name.
	        String Line2 = sign.getLine(1);
	        if(classes.contains(Line2)){
	        	if (a == Action.RIGHT_CLICK_BLOCK)
	            {
	                TempleManager.tellPlayer(p, "Punch the sign to select a class.");
	                return;
	            }
	        	
	        	TemplePlayer tp = TempleManager.templePlayerMap.get(p);
	        	
	        	// Set the player's class.
	    		Temple temple = tp.currentTemple;
	        	if(temple != null){
	        		assignClass(p, Line2);
		        	TempleManager.tellPlayer(p, "You have chosen " + Line2 + " as your class!");
	        	}
				return;
	        }
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
		giveClassItems(p, className);
		classMap.put(p, className);
	}
	
	/**
	* Grant a player their class-specific items.
	*/
	public static void giveClassItems(Player p, String className)
	{
		String classItems = classItemMap.get(className);
		String classArmor = classArmorMap.get(className);
		giveItems(p, classItems);
		equipArmor(p, classArmor);
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
        
        for (String s : classes)
        {
            result.put(s, c.getString(path + s + "." + type, null));
        }
        
        return result;
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
            	System.out.println("No Armor was detected by getArmor");
            }
        }
    }
    
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
    
    /**
     * Grabs the list of classes from the config-file. If no list is
     * found, generate a set of default classes.
     */
    public static List<String> getClasses()
    {
        Configuration c = config;
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
        	c.setProperty("classes.Archer.items", "wood_sword, bow, arrow:128, grilled_pork");
            c.setProperty("classes.Archer.armor", "298,299,300,301");
            c.setProperty("classes.Knight.items", "diamond_sword, grilled_pork");
            c.setProperty("classes.Knight.armor", "306,307,308,309");
            c.setProperty("classes.Tank.items",   "iron_sword, grilled_pork:2");
            c.setProperty("classes.Tank.armor",   "310,311,312,313");
            c.setProperty("classes.Chef.items",   "stone_sword, bread:6, grilled_pork:4, mushroom_soup, cake:3, cookie:12");
            c.setProperty("classes.Chef.armor",   "314,315,316,317");
            
            c.save();
        }
        
        return c.getKeys("classes");
    }
    
    protected static void generateClassSigns(Sign sign) {
		Block b = sign.getBlock();
		Location loc = b.getLocation();
		int x = loc.getBlockX();
    	int y = loc.getBlockY();
    	int z = loc.getBlockZ();
		for (String s : classes){
            TempleManager.world.getBlockAt(x, y, z).setTypeIdAndData(b.getTypeId(), b.getData(), false);
            Sign classSign = (Sign) TempleManager.world.getBlockAt(x, y, z).getState();
           	classSign.setLine(0, "");
            classSign.setLine(1, s);
            classSign.setLine(2, "");
            classSign.setLine(3, "");
            Material type = b.getType();
            byte data = b.getData();
           	if(type == Material.WALL_SIGN){
	           	if(data == 2)
	        		x--;
	           	else if(data == 3)
	        		x++;
	           	else if(data == 4)
	        		z++;
	           	else if(data == 5)
	        		z--;
           	}
			if(type == Material.SIGN_POST){
				if(data < 4)
	        		x++;
				else if(data < 8)
	        		z++;
				else if(data < 12)
	        		x--;
				else if(data <= 15)
	        		z--;
			}
        }
	}
}
