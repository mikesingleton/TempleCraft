package com.msingleton.templecraft;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;

public class Temple {
	protected File config;
    
    // Convenience variables.
    public String templeName;
    public boolean isSetup;
    protected boolean isEnabled;
    protected String owners;
    protected String editors;
    protected File ChunkGeneratorFile;
    
    // Sets and Maps for storing players and their locations.
    protected Set<Player> playerSet   = new HashSet<Player>();
    protected Set<String> ownerSet    = new HashSet<String>();
    protected Set<String> accessorSet = new HashSet<String>();
    protected Set<Player> editorSet   = new HashSet<Player>();
    
    public static int mobSpawner = 7;
    public static int diamondBlock = 57;
    public static int ironBlock = 42;
    public static int goldBlock = 41;
    public Set<Location> coordLocSet  = new HashSet<Location>();
    public static int[] coordBlocks = {mobSpawner, diamondBlock, ironBlock, goldBlock, 63, 68};
    
	protected Temple(){
	}

	protected Temple(String name){		
		config     = TCUtils.getConfig("temples");
		templeName = name;
		//Fix isSetup and Class item things
		owners     = TCUtils.getString(config,"Temples."+name+".owners", "");
		editors    = TCUtils.getString(config,"Temples."+name+".editors", "");
		isSetup    = TCUtils.getBoolean(config,"Temples."+name+".isSetup", false);
		isEnabled  = true;
		ChunkGeneratorFile = TCRestore.getChunkGenerator(this);
		loadEditors();
		TempleManager.templeSet.add(this);
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
    	LOAD/SAVE METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	public World loadTemple(String type){
		World result;
		
		String worldName;
		if(TempleManager.constantWorldNames)
			worldName = "TCTempleWorld_"+templeName;
		else
			worldName = TCUtils.getNextAvailableTempWorldName(type);
		
		if(worldName == null)
			return null;
		
		// if the world already exists and it can not be deleted (i.e. it contains players) return null
		World world = TempleManager.server.getWorld(worldName);
		if(world != null && !TCUtils.deleteTempWorld(world))
			return null;
		
		File tcffile = new File("plugins/TempleCraft/SavedTemples/"+templeName+TempleCraft.fileExtention);
		
		WorldCreator wc = new WorldCreator(worldName);
		wc.environment(Environment.NORMAL);
		// if the tcf file doesn't exist
		if(TCRestore.loadTemple(worldName, this) || !tcffile.exists()){
			if(ChunkGeneratorFile != null){
				ChunkGenerator cg = TCRestore.getChunkGenerator(ChunkGeneratorFile);
				if(cg != null)
					wc.generator(cg);
			}
    		result = TempleManager.server.createWorld(wc);
    		System.out.println("[TempleCraft] World \""+worldName+"\" Loaded!");
		} else if(type.equals("Edit") || type.equals("Convert")){
			File file = new File("plugins/TempleCraft/SavedTemples/"+templeName);
			file.mkdir();
			TCUtils.copyFromJarToDisk("Flat1.jar", file);
			ChunkGeneratorFile = new File("plugins/TempleCraft/SavedTemples/"+templeName+"/Flat1.jar");
			ChunkGenerator cg = TCRestore.getChunkGenerator(ChunkGeneratorFile);
			wc.generator(cg);
			result = TempleManager.server.createWorld(wc);
			TCRestore.loadTemple(new Location(result,0,0,0), this);
		} else {
			return null;
		}
		
		if(result == null)
			return null;
		
		if(type.equals("Edit"))
			TempleManager.templeEditMap.put(templeName, result);
		else
			result.setAutoSave(false);
		
		result.setKeepSpawnInMemory(false);
		
		coordLocSet.addAll(TCRestore.getSignificantLocs(this, result));
		return result;
	}

	protected void saveTemple(World w, Player p){		
	    TempleManager.tellPlayer(p, "Saving World...");
		TCRestore.saveTemple(w, this);
		
		isSetup = trySetup(w);
		if(TCUtils.getBoolean(config,"Temples."+templeName+".isSetup", isSetup) != isSetup){
			TCUtils.setBoolean(config,"Temples."+templeName+".isSetup", isSetup);
			if(isSetup)
				TempleManager.tellPlayer(p, templeName+" is "+ChatColor.DARK_GREEN+"now Setup");
			else
				TempleManager.tellPlayer(p, templeName+" is "+ChatColor.DARK_RED+"no longer Setup");
		} else if(!isSetup){
			TempleManager.tellPlayer(p, templeName+" is "+ChatColor.DARK_RED+"not Setup yet");
		}
		
		// ChunkGenerator
		if(ChunkGeneratorFile != null){
			File destination = new File("plugins/TempleCraft/SavedTemples/"+templeName+"/"+ChunkGeneratorFile.getName());
			if(!destination.exists()){
				File folder = new File("plugins/TempleCraft/SavedTemples/"+templeName);
				folder.mkdir();
				TCRestore.copyFile(ChunkGeneratorFile, destination);
			}
		}
		
		TempleManager.tellPlayer(p, "Temple Saved");
	}
	
	private void loadEditors() {
		for(String s : owners.split(",")){
			s = s.trim();
			ownerSet.add(s);
		}
		
		for(String s : editors.split(",")){
			s = s.trim();
			accessorSet.add(s);
		}
	}
	
	// Removes editors from temple
	public void removeEditors(){
		for(Player p: editorSet){
			TemplePlayer tp = TempleManager.templePlayerMap.get(p);
			if(tp == null)
				return;
			if(tp.currentTemple == this)
				TempleManager.playerLeave(p);
		}
	}
	
	protected boolean addOwner(String playerName) {
		if(ownerSet.contains(playerName))
			return false;
		else
			ownerSet.add(playerName);
		updateEditors();
		return true;
	}
	
	protected boolean addEditor(String playerName) {
		if(accessorSet.contains(playerName))
			return false;
		else
			accessorSet.add(playerName);
		updateEditors();
		return true;
	}
	
	protected boolean removeEditor(String playerName) {
		boolean result;
		result = (ownerSet.remove(playerName) || accessorSet.remove(playerName));
		updateEditors();
		return result;
	}
	
	private void updateEditors(){
		StringBuilder owners = new StringBuilder();
    	for(String s : ownerSet)
			if(owners.length() == 0)
				owners.append(s);
			else
				owners.append(","+s);
    	
    	StringBuilder editors = new StringBuilder();
    	for(String s : accessorSet)
			if(editors.length() == 0)
				editors.append(s);
			else
				editors.append(","+s);
    	
    	this.owners = owners.toString();
    	this.editors = editors.toString();
    	
    	
    	try {
			saveConfig();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void saveConfig() throws IOException{
		YamlConfiguration c = YamlConfiguration.loadConfiguration(config);
    	c.set("Temples."+templeName+".owners", owners);    	
    	c.set("Temples."+templeName+".editors", editors);
    	c.save(config);
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
	SETUP METHODS

	// ///////////////////////////////////////////////////////////////////// */

	
	public boolean trySetup(World w){		
		boolean foundLobbyLoc = false;
		boolean foundTempleLoc = false;

		for(Block b: getBlockSet(w,Material.WALL_SIGN.getId())){
			if(foundLobbyLoc && foundTempleLoc)
				break;
			
	        Sign sign = (Sign) b.getState();
	        if(!foundLobbyLoc)
	        	foundLobbyLoc = checkSign("lobby", sign);
	        if(!foundTempleLoc)
	        	foundTempleLoc = checkSign("spawnarea", sign);
		}
		for(Block b: getBlockSet(w,Material.SIGN_POST.getId())){     
			if(foundLobbyLoc && foundTempleLoc)
				break;
	        Sign sign = (Sign) b.getState();
	        if(!foundLobbyLoc)
	        	foundLobbyLoc = checkSign("lobby", sign);
	        if(!foundTempleLoc)
	        	foundTempleLoc = checkSign("spawnarea", sign);
		}
		for(Block b: getBlockSet(w,diamondBlock)){
			if(foundTempleLoc)
				break;
    		Block rb = b.getRelative(0, -1, 0);
    		if(rb.getTypeId() == ironBlock)
    			foundTempleLoc = true;
		}
		isSetup = foundLobbyLoc && foundTempleLoc;
		//if(!isSetup)
			//System.out.println("[TempleCraft] For "+templeName+". LobbyLoc Setup: "+foundLobbyLoc+" TempleLoc Setup: "+foundTempleLoc);
		return isSetup;
	}
	
	public Location getLobbyLoc(World w){		
		for(Block b: getBlockSet(w,Material.WALL_SIGN.getId())){			
	        Sign sign = (Sign) b.getState();
	        String[] Lines = sign.getLines();
	        if(Lines[0].equals("[TC]") || Lines[0].equals("[TempleCraft]"))
	        	if(Lines[1].toLowerCase().equals("lobby"))
	        		return b.getLocation();
		}
		for(Block b: getBlockSet(w,Material.SIGN_POST.getId())){     
	        Sign sign = (Sign) b.getState();
	        String[] Lines = sign.getLines();
	        if(Lines[0].equals("[TC]") || Lines[0].equals("[TempleCraft]"))
	        	if(Lines[1].toLowerCase().equals("lobby"))
	        		return b.getLocation();
		}
		return null;
	}
	
	private boolean checkSign(String key, Sign sign) {
		if(sign == null)
			return false;
		String[] Lines = sign.getLines();
		if(!Lines[0].equals("[TC]") && !Lines[0].equals("[TempleCraft]"))
			return false;
		
		if(Lines[1].toLowerCase().equals(key)){
			return true;
		}
		return false;
	}
	
	private Set<Block> getBlockSet(World world, int id){
	    Set<Block> result = new HashSet<Block>();

	    if(!coordLocSet.isEmpty())
	    	for(Location loc : coordLocSet){
	    		Block b = world.getBlockAt(loc);
	    		if(b.getTypeId() == id)
	    			result.add(b);
	    	}

	    return result;
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
    	MISC METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	/**
	* Sends a message to a player.
	*/
	protected void tellPlayer(Player p, String msg)
	{
	if (p == null)
	    return;
	
	p.sendMessage(ChatColor.GREEN + "[TC] " + ChatColor.WHITE + msg);
	}
	
	/**
	* Sends a message to all players in the Temple.
	*/
	protected void tellAll(String msg)
	{
		for(Player p: playerSet)
			tellPlayer((Player)p, msg);	    
	}
}
