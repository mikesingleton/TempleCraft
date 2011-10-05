package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.sk89q.worldedit.data.Chunk;


public class Temple {
	protected Configuration config = null;
    
    // Convenience variables.
    public String templeName     = null;
    public boolean isSetup       = false;
    protected boolean isEnabled  = true; 
    protected String owners  = "";
    protected String editors = "";
    
    // Sets and Maps for storing players and their locations.
    protected Set<Player> playerSet     = new HashSet<Player>();
    protected Set<String> ownerSet    = new HashSet<String>();
    protected Set<String> accessorSet = new HashSet<String>();
    protected Set<Player> editorSet   = new HashSet<Player>();
    
    public static int mobSpawner = 7;
    public static int diamondBlock = 57;
    public static int ironBlock = 42;
    public static int goldBlock = 41;
    public Set<Block> coordBlockSet  = new HashSet<Block>();
    public static int[] coordBlocks = {mobSpawner, diamondBlock, ironBlock, goldBlock, 63, 68};
    
	protected Temple(){
	}

	protected Temple(String name){		
		config     = TCUtils.getConfig("temples");
		templeName = name;
		owners     = TCUtils.getString(config,"Temples."+name+".owners", "");
		editors    = TCUtils.getString(config,"Temples."+name+".editors", "");
		isSetup    = TCUtils.getBoolean(config,"Temples."+name+".isSetup", false);
		loadEditors();
		TempleManager.templeSet.add(this);
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
    	LOAD/SAVE METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	public World loadTemple(String type){
		World result;
		
		String worldName = TCUtils.getNextAvailableTempWorldName(type);
		
		// Checks to make sure the world that will be created does not already exist
		World world = TempleManager.server.getWorld(worldName);
		if(world != null)
			TCUtils.deleteTempWorld(world);
		
		if(TCRestore.loadTemple(worldName, this)){
    		result = TempleManager.server.createWorld(worldName, Environment.NORMAL);
    		System.out.println("[TempleCraft] World \""+worldName+"\" Loaded!");
		} else if(type.equals("Edit")){
			result = TempleManager.server.createWorld(worldName, Environment.NORMAL, new TempleWorldGenerator());
			TCRestore.loadTemple(new Location(result,0,0,0), this);
		} else {
			return null;
		}
		
		if(result != null && type.equals("Edit"))
			TempleManager.templeEditMap.put(templeName, result);
		
		result.setAutoSave(false);
		result.setKeepSpawnInMemory(false);
		
		coordBlockSet = TCRestore.getSignificantBlocks(this, result);
		return result;
	}

	protected void saveTemple(World w, Player p){		
	    TempleManager.tellPlayer(p, "Saving World...");
		TCRestore.saveTemple(w, this);
		
		isSetup = trySetup();
		if(TCUtils.getBoolean(config,"Temples."+templeName+".isSetup", isSetup) != isSetup){
			TCUtils.setBoolean(config,"Temples."+templeName+".isSetup", isSetup);
			if(isSetup)
				TempleManager.tellPlayer(p, templeName+" is "+ChatColor.DARK_GREEN+"now Setup");
			else
				TempleManager.tellPlayer(p, templeName+" is "+ChatColor.DARK_RED+"no longer Setup");
		} else if(!isSetup){
			TempleManager.tellPlayer(p, templeName+" is "+ChatColor.DARK_RED+"not Setup yet");
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
    	
    	saveConfig();
	}

	protected void saveConfig(){
		Configuration c = config;
    	c.load();
    	c.setProperty("Temples."+templeName+".owners", owners);    	
    	c.setProperty("Temples."+templeName+".editors", editors);
    	c.save();
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
	SETUP METHODS

	// ///////////////////////////////////////////////////////////////////// */

	
	public boolean trySetup(){		
		boolean foundLobbyLoc = false;
		boolean foundTempleLoc = false;

		for(Block b: getBlockSet(Material.WALL_SIGN.getId())){
			if(foundLobbyLoc && foundTempleLoc)
				break;
	        Sign sign = (Sign) b.getState();
	        if(!foundLobbyLoc)
	        	foundLobbyLoc = checkSign("lobby", sign);
	        if(!foundTempleLoc)
	        	foundTempleLoc = checkSign("spawnarea", sign);
		}
		for(Block b: getBlockSet(Material.SIGN_POST.getId())){     
			if(foundLobbyLoc && foundTempleLoc)
				break;
	        Sign sign = (Sign) b.getState();
	        if(!foundLobbyLoc)
	        	foundLobbyLoc = checkSign("lobby", sign);
	        if(!foundTempleLoc)
	        	foundTempleLoc = checkSign("spawnarea", sign);
		}
		for(Block b: getBlockSet(diamondBlock)){
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
	
	private boolean checkSign(String key, Sign sign) {
		String[] Lines = sign.getLines();
		if(!Lines[0].equals("[TC]") && !Lines[0].equals("[TempleCraft]"))
			return false;
		
		if(Lines[1].toLowerCase().equals(key)){
			return true;
		}
		return false;
	}
	
	private Set<Block> getBlockSet(int id){
	    Set<Block> result = new HashSet<Block>();

	    if(!coordBlockSet.isEmpty())
	    	for(Block b : coordBlockSet)
	    		if(b.getTypeId() == id)
	    			result.add(b);

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
