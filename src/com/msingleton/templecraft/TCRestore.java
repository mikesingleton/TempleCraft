package com.msingleton.templecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.ContainerBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.PistonBaseMaterial;

public class TCRestore {
	//Blocks
	private static Set<Integer> blockSet = new HashSet<Integer>(Arrays.asList(0,1,2,3,4,5,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,35,41,42,43,44,45,46,47,48,49,52,54,56,57,58,60,61,62,67,73,74,79,80,81,82,84,85,86,87,88,89,90,91,92));
	private static String c = ":";
	private static String s = " ";
	
	public static void saveTemple(World w, Temple temple){
		if(w == null || temple == null)
			return;
		
		w.save();
		copyDirectory(new File(w.getName()),new File("plugins/TempleCraft/SavedTemples/"+temple.templeName));
		saveSignificantBlocks(temple);
	}

	// Copies all files under srcDir to dstDir.
	// If dstDir does not exist, it will be created.
	public static void copyDirectory(File srcDir, File dstDir){
	    if (srcDir.isDirectory()) {
	        if (!dstDir.exists()) {
	            dstDir.mkdir();
	        }

	        String[] children = srcDir.list();
	        for (int i=0; i<children.length; i++) {
	            copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
	        }
	    } else {
	        // This method is implemented in Copying a File
	        copyFile(srcDir, dstDir);
	    }
	}
	
	// Copies src file to dst file.
	// If the dst file does not exist, it is created
	public static void copyFile(File src, File dst){
		try{
		    InputStream in = new FileInputStream(src);
		    OutputStream out = new FileOutputStream(dst);
	
		    // Transfer bytes from in to out
		    byte[] buf = new byte[1024];
		    int len;
		    while ((len = in.read(buf)) > 0) {
		        out.write(buf, 0, len);
		    }
		    in.close();
		    out.close();
		}catch(Exception e){
			System.out.println("Could not copy file "+src);
		}
	}
	
	public static boolean loadTemple(String worldName, Temple temple){
		File file = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName);
		
		if(!file.exists())
			return false;
		
		copyDirectory(new File("plugins/TempleCraft/SavedTemples/"+temple.templeName),new File(worldName));
		return true;
	}
	
	public static void saveSignificantBlocks(Temple temple){
		if(temple == null || temple.coordBlockSet.isEmpty())
			return;
		
		HashSet<EntityPosition> significantLocs = new HashSet<EntityPosition>();
		
		for(Block b : temple.coordBlockSet)
			significantLocs.add(new EntityPosition(b.getLocation()));
		
		try
	    {	
	    	File folder = new File("plugins/TempleCraft/SavedTemples/");
        	if(!folder.exists())
        		folder.mkdir();
        	File tmpfile = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName+"/"+"TCLocs.tmp");
	        FileOutputStream fos = new FileOutputStream(tmpfile);
	        ObjectOutputStream oos = new ObjectOutputStream(fos);
	        oos.writeObject(significantLocs);
	        oos.close();
	        File file = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName+"/"+"TCLocs"+ TempleCraft.fileExtention);
	        if(file.exists())
	        	file.delete();
	        tmpfile.renameTo(file);
	    }
	    catch (Exception e)
	    {
	        System.out.println("[TempleCraft] Couldn't create backup file. Aborting...");
	        e.printStackTrace();
	        return;
	    }
	}
	
	@SuppressWarnings("unchecked")
	public static HashSet<EntityPosition> getSignificantEPs(Temple temple){		
		String fileName = "TCLocs"+TempleCraft.fileExtention;
		
		HashSet<EntityPosition> significantEPs = new HashSet<EntityPosition>();		
        try
        {
        	File file = new File("plugins/TempleCraft/SavedTemples/"+temple.templeName+"/"+fileName);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            significantEPs = (HashSet<EntityPosition>) ois.readObject();
            ois.close();
        }
        catch (Exception e)
        {
            System.out.println("[TempleCraft] TempleCraft file not found for this temple.");
        }

        return significantEPs;
	}
	
	public static HashSet<Block> getSignificantBlocks(Temple temple, World world){					
		HashSet<Block> significantBlocks = new HashSet<Block>();
		
        for(EntityPosition ep : getSignificantEPs(temple))
			significantBlocks.add(world.getBlockAt((int)ep.getX(),(int)ep.getY(),(int)ep.getZ()));
        
        return significantBlocks;
	}
	
	
	// Old Loading Method
	@SuppressWarnings("unchecked")
	public static void loadTemple(Location startLoc, Temple temple){	
		if(startLoc == null)
			return;
		
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
            System.out.println("[TempleCraft] TempleCraft file not found for this temple.");
            return;
        }
        
        Set<EntityPosition> ppKeySet = new HashSet<EntityPosition>();
        ppKeySet.addAll(preciousPatch.keySet());
        
        Map<EntityPosition, String> blockMap = new HashMap<EntityPosition, String>();
        Map<EntityPosition, String> nonBlockMap = new HashMap<EntityPosition, String>();
        Map<EntityPosition, String> pistonMap = new HashMap<EntityPosition, String>();
        for (EntityPosition ep : ppKeySet){
        	int id = Integer.parseInt(preciousPatch.get(ep).split(c)[0]);
        	int y = (int)(startLoc.getY()+ep.getY());
        	if(id == getDefaultBlock(y)){
        		preciousPatch.remove(ep);
        	} else if(!blockSet.contains(id) && id != 29 && id!=33 && id != 34){
        		nonBlockMap.put(ep, preciousPatch.remove(ep));
        	} else if(blockSet.contains(id)){
        		blockMap.put(ep, preciousPatch.remove(ep));
        	} else {
        		pistonMap.put(ep, preciousPatch.remove(ep));
        	}
        }
        
        loadBlockMap(startLoc, blockMap, temple, true);
        loadBlockMap(startLoc, nonBlockMap, temple, true);
        loadBlockMap(startLoc, pistonMap, temple, true);
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
	
	private static void loadBlockMap(Location startLoc, Map<EntityPosition, String> blockMap, Temple temple, Boolean physics) {
		World world = startLoc.getWorld();
		for (EntityPosition ep : blockMap.keySet()){        	
        	String[] s = blockMap.get(ep).split(c);
        	
        	int id = Integer.parseInt(s[0]);
        	byte data = Byte.parseByte(s[1]);
        	
       		double x = ep.getX()+startLoc.getX();
       		double y = ep.getY()+startLoc.getBlockY();
            double z = ep.getZ()+startLoc.getZ();
            Location loc = new Location(world, x, y, z);
           	Block b = world.getBlockAt(loc);
           	b.setTypeIdAndData(id, data, physics);
           	
           	// If it's a door, add the upper half
           	if(id == 71 || id == 64){
				b.getRelative(0,1,0).setTypeIdAndData(id,(byte) (data+8), true);
			// If it's a sign, add the text
			} else if(b.getState() instanceof Sign){
				temple.coordBlockSet.add(b);
        		if(s.length > 2){
        			for(int i = 2; i<s.length;i++){
        				if((i-2)>3 || (i-2)<0 || s[i] == null)
        					continue;
        				Sign sign = (Sign) b.getState();
        				sign.setLine((i-2), s[i]);
        			}
        		}
        	// If it's a container, add it's contents
        	} else if(b.getState() instanceof ContainerBlock){
				if(s.length > 2)
					contentsFromString((ContainerBlock)b.getState(), s[2]);
        	// If it's diamond,gold or iron or bedrock, add it to coordBlockSet
        	} else if(b.getTypeId() == Temple.goldBlock || b.getTypeId() == Temple.diamondBlock || b.getTypeId() == Temple.ironBlock || b.getTypeId() == Temple.mobSpawner){
        		temple.coordBlockSet.add(b);
        	}
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
