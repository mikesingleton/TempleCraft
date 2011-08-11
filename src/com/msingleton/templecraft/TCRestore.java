package com.msingleton.templecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;

public class TCRestore {
	//Blocks
	private static int[] blockArray = {0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,35,41,42,43,44,45,46,47,48,49,52,54,56,57,58,60,61,62,67,73,74,79,80,81,82,84,85,86,87,88,89,90,91,92};
	private static Set<Integer> blockSet = new HashSet<Integer>();
	private static String c = ":";
	private static String s = " ";
	
	public static void saveTemple(Location p1, Location p2, Temple temple){	
		World world = p1.getWorld();
		
	    int x1 = (int)p1.getX();
	    int y1 = (int)p1.getY();
	    int z1 = (int)p1.getZ();
	    int x2 = (int)p2.getX();
	    int y2 = (int)p2.getY();
	    int z2 = (int)p2.getZ();
		
		// Save the precious patch
	    HashMap<EntityPosition, String> preciousPatch = new HashMap<EntityPosition, String>();
	    Location lo;
	    String id = "";
	    // Save top to bottom so it loads bottom to top	    
	    for (int j = y2; j >= y1; j--)
        {
	    	for (int i = x1; i <= x2; i++)
	    	{
	            for (int k = z1; k <= z2; k++)
	            {
	            	Block b = world.getBlockAt(i,j,k);
	            	boolean push = true;
	            	if(b.getTypeId() == getDefaultBlock(j)){
	            		continue;
	            		// Save chest and furnace and dispenser inventories
	            	} else if(b.getState() instanceof ContainerBlock){
	            		if(!contentsToString((ContainerBlock)b.getState()).replace(" ", "").isEmpty()){
	            			id = b.getTypeId()+c+b.getData() + c + contentsToString((ContainerBlock)b.getState());	
	            		} else {
	            			id = b.getTypeId()+c+b.getData();
	            		}
	            		// Don't save the top halves of doors
	            	} else if((b.getTypeId() == 71 || b.getTypeId() == 64) && b.getData() > 7) {
           				push = false;
            			// Save sign messages
	            	} else if(b.getTypeId() == 68 || b.getTypeId() == 63){
	            		Sign sign = (Sign) b.getState();
	            		id = b.getTypeId()+c+b.getData() + c + sign.getLine(0) + c + sign.getLine(1) + c + sign.getLine(2) + c + sign.getLine(3);
	            	} else {
	            		id = b.getTypeId()+c+b.getData();
	            	}
	            	if(push){
	            		lo = world.getBlockAt(i-x1,j-y1,k-z1).getLocation();
	            		preciousPatch.put(new EntityPosition(lo),id);
	            	}
	            }
	        }
	    }
	    try
	    {	
	    	File folder = new File("plugins/TempleCraft/SavedTemples/");
        	if(!folder.exists())
        		folder.mkdir();
        	File tmpfile = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName + ".tmp");
	        FileOutputStream fos = new FileOutputStream(tmpfile);
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(preciousPatch);
	        oos.close();
	        File file = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName + TempleCraft.fileExtention);
	        tmpfile.renameTo(file);
	    }
	    catch (Exception e)
	    {
	        System.out.println("Couldn't create backup file. Aborting...");
	        e.printStackTrace();
	        return;
	    }
	}
	
	private static String contentsToString(ContainerBlock cb) {
		StringBuilder result = new StringBuilder();
		for(ItemStack item : cb.getInventory().getContents()){
			if(item != null)
				result.append(item.getTypeId() + s + item.getAmount() + s + item.getDurability() + s);
			else
				result.append(s+s+s);
		}
		result.deleteCharAt(result.length()-1);
		return result.toString();
	}
	
	private static void contentsFromString(ContainerBlock cb, String string) {
		String[] items = string.split(s);
		for(int i = 0; i < items.length; i+=3){
			if(items[i] == null || items[i].isEmpty())
				continue;
			ItemStack item = new ItemStack(Integer.parseInt(items[i]));
			item.setAmount(Integer.parseInt(items[i+1]));
			item.setDurability(Short.parseShort(items[i+2]));
			cb.getInventory().setItem(i/3, item);
		}
	}

	public static int getDefaultBlock(int y) {
		int[] levels = TempleManager.landLevels;
		byte[] mats = TempleManager.landMats;
		for(int i = 0; i<levels.length; i++){
			int bottom, top;
			if(i == 0)
				bottom = 0;
			else
				bottom = levels[i-1];
			top = levels[i];
			if(y >= bottom && y <= top)
				return mats[i];
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public static void loadTemple(Location startLoc, Temple temple){
		World world = startLoc.getWorld();
		
		String fileName = temple.templeName + TempleCraft.fileExtention;
		
		HashMap<EntityPosition,String> preciousPatch;
        try
        {
        	File file = new File("plugins/TempleCraft/SavedTemples/"+fileName);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            preciousPatch = (HashMap<EntityPosition,String>) ois.readObject();
            ois.close();
        }
        catch (Exception e)
        {
            System.out.println("TempleCraft file not found for this temple.");
            return;
        }
        
        // Load blockSet if it isn't loaded already
        if(blockSet.isEmpty()){
	        for(int id : blockArray){
	    		blockSet.add(id);
	    	}
        }
        
        for (EntityPosition ep : preciousPatch.keySet()){        	
        	String[] s = preciousPatch.get(ep).split(c);
        	
        	double x = ep.getX()+startLoc.getX();
        	double y = ep.getY()+startLoc.getY();
        	double z = ep.getZ()+startLoc.getZ();
        	Location loc = new Location(world, x, y, z);
        	Block b = world.getBlockAt(loc);
        	
        	int id = Integer.parseInt(s[0]);
        	byte data = Byte.parseByte(s[1]);
        	
       		if(blockSet.contains(id)){
    			b.setTypeIdAndData(id, data, true);
    			if(b.getState() instanceof ContainerBlock){
    				if(s.length > 2)
    					contentsFromString((ContainerBlock)b.getState(), s[2]);
    			}
        	}
       		
       		addToTempleSets(temple, b);
        	TCUtils.expandRegion(temple, loc);
        }
        
        for (EntityPosition ep : preciousPatch.keySet()){        	
        	String[] s = preciousPatch.get(ep).split(c);
        	
        	double x = ep.getX()+startLoc.getX();
        	double y = ep.getY()+startLoc.getY();
        	double z = ep.getZ()+startLoc.getZ();
        	Location loc = new Location(world, x, y, z);
        	Block b = world.getBlockAt(loc);
        	
        	int id = Integer.parseInt(s[0]);
        	byte data = Byte.parseByte(s[1]);
        	
       		if(!blockSet.contains(id)){
    			b.setTypeIdAndData(id, data, true);
    			if(b.getTypeId() == 71 || b.getTypeId() == 64){
    				b.getRelative(0,1,0).setTypeIdAndData(b.getTypeId(),(byte) (b.getData()+8), true);
    			} else if(b.getTypeId() == 68 || b.getTypeId() == 63){
            		if(s.length > 2){
            			for(int i = 2; i<s.length;i++){
            				if((i-2)>3 || (i-2)<0 || s[i] == null)
            					continue;
            				Sign sign = (Sign) b.getState();
            				sign.setLine((i-2), s[i]);
            			}
            		}
            	}
        	}
       		addToTempleSets(temple, b);
        	TCUtils.expandRegion(temple, loc);
        }
	}

	private static void addToTempleSets(Temple temple, Block b) {
		if(b.getWorld().equals(TempleManager.world)){
			for(int id : Temple.coordBlocks)
				if(b.getTypeId() == id && b.getLocation().getBlockY() > 0)
					temple.coordBlockSet.add(b);
		}
	}

	public static void clearEntities(Location p1, Location p2)
	{
	World world = p1.getWorld();
		
	Chunk c1 = world.getChunkAt(p1);
	Chunk c2 = world.getChunkAt(p2);
	
	/* Yes, ugly nesting, but it's necessary. This bit
	 * removes all the entities in the Temple region without
	 * bloatfully iterating through all entities in the
	 * world. Much faster on large servers especially. */ 
	for (int i = c1.getX(); i <= c2.getX(); i++)
	    for (int j = c1.getZ(); j <= c2.getZ(); j++)
	        for (Entity e : world.getChunkAt(i,j).getEntities())
	            if ((e instanceof Item) || (e instanceof Slime))
	                e.remove();
	}
}
