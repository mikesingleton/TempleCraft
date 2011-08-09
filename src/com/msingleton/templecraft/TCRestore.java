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
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;

public class TCRestore {
	private static String c = ":";	
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
	    String id;
	    // Save top to bottom so it loads bottom to top	    
	    for (int j = y2; j >= y1; j--)
        {
	    	for (int i = x1; i <= x2; i++)
	    	{
	            for (int k = z1; k <= z2; k++)
	            {
	            	Block b = world.getBlockAt(i,j,k);
	            	if(isDefaultBlock(b))
	            		continue;
	            	else if(b.getTypeId() == 68 || b.getTypeId() == 63){
	            		Sign sign = (Sign) b.getState();
	            		id = b.getTypeId()+c+b.getData() + c + sign.getLine(0) + c + sign.getLine(1) + c + sign.getLine(2) + c + sign.getLine(3);
	            	} else {
	            		id = b.getTypeId()+c+b.getData();
	            	}
	                lo = world.getBlockAt(i-x1,j-y1,k-z1).getLocation();
	                preciousPatch.put(new EntityPosition(lo),id);
	            }
	        }
	    }
	    try
	    {	
	    	File folder = new File("plugins/TempleCraft/SavedTemples/");
        	if(!folder.exists())
        		folder.mkdir();
        	File file = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName + TempleCraft.fileExtention);
	        FileOutputStream fos = new FileOutputStream(file);
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(preciousPatch);
	        oos.close();
	    }
	    catch (Exception e)
	    {
	        System.out.println("Couldn't create backup file. Aborting...");
	        e.printStackTrace();
	        return;
	    }
	}
	
	private static boolean isDefaultBlock(Block b) {
		int[] levels = TempleManager.landLevels;
		byte[] mats = TempleManager.landMats;
		for(int i = 0; i<levels.length; i++){
			int bottom, top;
			if(i == 0)
				bottom = 0;
			else
				bottom = levels[i-1];
			top = levels[i];
			if(b.getLocation().getBlockY() > bottom && b.getLocation().getBlockY() <= top && b.getTypeId() == mats[i])
				return true;
		}
		return false;
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
            System.out.println("Couldn't find backup file...");
            return;
        }
        
        // Loads Blocks first so redstone and torches have something to sit on
        
        for (EntityPosition ep : preciousPatch.keySet())
        {
        	double x = ep.getX()+startLoc.getX();
        	double y = ep.getY()+startLoc.getY();
        	double z = ep.getZ()+startLoc.getZ();
        	Location loc = new Location(world, x, y, z);
        	String[] s = preciousPatch.get(ep).split(c);
        	Block b = world.getBlockAt(loc);
        	if(TempleManager.blockSet.contains(Integer.parseInt(s[0]))){
        		b.setTypeIdAndData(Integer.parseInt(s[0]), Byte.parseByte(s[1]), true);
        		addToTempleSets(temple, b);
        		TCUtils.expandRegion(temple, loc);
        	}
        }
        
        // Loads everything else that is not a Block
        
        for (EntityPosition ep : preciousPatch.keySet())
        {
        	double x = ep.getX()+startLoc.getX();
        	double y = ep.getY()+startLoc.getY();
        	double z = ep.getZ()+startLoc.getZ();
        	Location loc = new Location(TempleManager.world, x, y, z);
        	String[] s = preciousPatch.get(ep).split(c);
        	Block b = world.getBlockAt(loc);
        	if(!TempleManager.blockSet.contains(Integer.parseInt(s[0]))){
        		b.setTypeIdAndData(Integer.parseInt(s[0]), Byte.parseByte(s[1]), true);
        		addToTempleSets(temple, b);
        		TCUtils.expandRegion(temple, loc);
	        	if(b.getTypeId() == 68 || b.getTypeId() == 63){
	        		if(s.length > 2){
	        			for(int i = 2; i<s.length;i++){
	        				Sign sign = (Sign) b.getState();
	        				sign.setLine((i-2), s[i]);
	        			}
	        		}
	        	}
        	}
        }
	}

	private static void addToTempleSets(Temple temple, Block b) {
		if(b.getWorld().equals(TempleManager.world)){
			temple.blockSet.add(b);
			for(int id : Temple.coordBlocks)
				if(b.getTypeId() == id)
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
