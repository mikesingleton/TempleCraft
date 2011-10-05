package com.msingleton.templecraft.games;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.msingleton.templecraft.MobArenaClasses;
import com.msingleton.templecraft.TCMobHandler;
import com.msingleton.templecraft.TCRestore;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.nijikokun.register.payment.Method.MethodAccount;

public class Game{
	public World world;
	public Temple temple;
	public String gameName;
    public boolean isRunning     = false;
    public boolean isSetup       = false;
    public boolean isLoaded      = false;
    public boolean usingClasses  = false;
	public int rejoinCost;
    
	// Location variables for the Temple region.
	public Location lobbyLoc = null;
	public Location startLoc = null;
	
	// Map for Death Message
    public Map<Integer, Entity> lastDamager = new HashMap<Integer, Entity>();
	// Contains Mob Spawnpoint Locations
    public Set<Location> mobSpawnpointSet = new HashSet<Location>();
    
    // Contains Active Mob Spawnpoints and Creature Types
    public Map<Location,CreatureType> mobSpawnpointMap = new HashMap<Location,CreatureType>();
    public Map<Integer,Integer> mobGoldMap  = new HashMap<Integer,Integer>();
	
	public Set<Player> playerSet     = new HashSet<Player>();
    public Set<Player> readySet      = new HashSet<Player>();
    public Set<Player> deadSet       = new HashSet<Player>();
    public Set<LivingEntity> monsterSet = new HashSet<LivingEntity>();
	
    public Set<Block> coordBlockSet = new HashSet<Block>();
    public Set<Block> startBlockSet = new HashSet<Block>();
    public Set<Block> endBlockSet   = new HashSet<Block>();
    public Set<Block> lobbyBlockSet = new HashSet<Block>();
    public Set<Block> tempBlockSet  = new HashSet<Block>();
    
    public static int mobSpawner = 7;
    public static int diamondBlock = 57;
    public static int ironBlock = 42;
    public static int goldBlock = 41;
    public static int[] coordBlocks = {mobSpawner, diamondBlock, ironBlock, goldBlock, 63, 68};
    
	public Game(String name, Temple temple, World world){
		TempleManager.gameSet.add(this);
		gameName      = name;
		this.world    = world;
		this.temple   = temple;
		isSetup       = temple.isSetup;
		coordBlockSet = temple.coordBlockSet;
		rejoinCost    = TempleManager.rejoinCost;
	} 

	/**
	* Starts the current TempleCraft session.
	*/
	public void startGame()
	{		
		isRunning = true;
		convertSpawnpoints();
		for(Player p : playerSet)
			p.teleport(getPlayerSpawnLoc());
		readySet.clear();
		
		tellAll("Let the games begin!");
	}

	private void convertLobby(){
		for(Block b: getBlockSet(Material.WALL_SIGN.getId())){     
	        // Cast the block to a sign to get the text on it.
	        Sign sign = (Sign) b.getState();
	        handleSign(sign);
		}
		for(Block b: getBlockSet(Material.SIGN_POST.getId())){     
	        // Cast the block to a sign to get the text on it.
	        Sign sign = (Sign) b.getState();
	        handleSign(sign);
		}

		for(Block b: getBlockSet(goldBlock)){
    		Block rb = b.getRelative(0, -1, 0);
    		if(rb.getTypeId() == ironBlock){
    			lobbyBlockSet.add(rb);
    			b.setTypeId(0);
    			rb.setTypeId(ironBlock);
    		} else {
    			temple.coordBlockSet.remove(b);
    		}
		}
	}
	
	protected void handleSign(Sign sign) {
		String[] Lines = sign.getLines();
		Block b = sign.getBlock();
		
		
		if(!Lines[0].equals("[TempleCraft]") && !Lines[0].equals("[TC]")){
			temple.coordBlockSet.remove(b);
			return;			
		}
		
		if(Lines[1].toLowerCase().equals("lobby")){
			lobbyLoc = b.getLocation();
			b.setTypeId(0);
		} else if(Lines[1].toLowerCase().equals("classes")){
			sign.getBlock().setTypeId(0);
		} else {
			String s = Lines[1]+Lines[2]+Lines[3];
			for(String mob : TempleManager.mobs){
				if(s.toLowerCase().contains(mob.toLowerCase())){
					mobSpawnpointSet.add(b.getLocation());
		    		mobSpawnpointMap.put(b.getLocation(),CreatureType.fromName(mob));
		    		b.setTypeId(0);
				}
			}
		}
	}
	
	public void handleSignClicked(Player p, Sign sign){
		
	}

	private void convertSpawnpoints() {
		for(Block b: getBlockSet(Material.WALL_SIGN.getId())){     
	        // Cast the block to a sign to get the text on it.
	        Sign sign = (Sign) b.getState();
	        handleSign(sign);
		}
		for(Block b: getBlockSet(Material.SIGN_POST.getId())){     
	        // Cast the block to a sign to get the text on it.
	        Sign sign = (Sign) b.getState();
	        handleSign(sign);
		}
		for(Block b: getBlockSet(mobSpawner)){
			mobSpawnpointSet.add(b.getLocation());
    		mobSpawnpointMap.put(b.getLocation(),TCMobHandler.getRandomCreature());
    		b.setTypeId(0);
		}
	    for(Block b: getBlockSet(diamondBlock)){
    		Block rb = b.getRelative(0, -1, 0);
    		if(rb.getTypeId() == ironBlock){
    			startBlockSet.add(rb);
    			b.setTypeId(0);
    			rb.setTypeId(0);
    		} else if(rb.getTypeId() == goldBlock){
    			endBlockSet.add(rb);
    			b.setTypeId(0);
    			rb.setTypeId(goldBlock);
    		} else {
    			temple.coordBlockSet.remove(b);
    		}
		}
	}

	private Set<Block> getBlockSet(int id){
	    Set<Block> result = new HashSet<Block>();

	    if(!coordBlockSet.isEmpty())
	    	for(Block b : coordBlockSet)
	    		if(b.getTypeId() == id)
	    			result.add(b);

	    return result;
	}
	
	/**
	* Ends the current TempleCraft session.
	* Clears the Temple floor, gives all the players their rewards,
	* and stops the spawning of monsters.
	*/
	public void endGame()
	{
		isRunning = false;
		readySet.clear();
		playerSet.clear();
		removePlayers();
		TempleManager.gameSet.remove(this);
		TCUtils.deleteTempWorld(world);
	}

	// Removes players from temple
	public void removePlayers(){
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
	private World getNonTempWorld() {
		World ntw = TempleManager.server.getWorld("world");
		if(ntw == null)
			for(World w : TempleManager.server.getWorlds())
				if(!TCUtils.isTCWorld(w))
					ntw = w;
		return ntw;
	}
	
	public Location getPlayerSpawnLoc() {
		return null;
	}

	/**
	* Attempts to let a player join the Temple session.
	* Players must have an empty inventory to join the Temple. Their
	* location will be stored for when they leave.
	*/
	public void playerJoin(Player p)
	{		
		if (!TempleManager.isEnabled)
		{
		    tellPlayer(p, "TempleCraft is not enabled.");
		    return;
		}
		if (!isSetup)
		{
			tellPlayer(p, "Temple \""+temple.templeName+"\" has not been set up yet!");
			return;
		}
		if (playerSet.contains(p))
		{
		    tellPlayer(p, "You are already playing!");
		    return;
		}
		if (TempleManager.playerSet.contains(p))
		{
		    tellPlayer(p, "You are already playing in a different Temple!");
		    return;
		}
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		if(tp.currentGame != null){
			tellPlayer(p, "Please leave the current game before joining another.");
			return;
		}
		if (isRunning)
		{
		    tellPlayer(p, "Temple \""+temple.templeName+"\" in progress.");
		    return;
		}
		
		tp.currentTemple = temple;
		tp.currentGame = this;
		tp.currentCheckpoint = null;
		TempleManager.playerSet.add(p);
		convertLobby();
		playerSet.add(p);
		
		if(world.getPlayers().isEmpty()){
			world.setTime(8000);
			world.setStorm(false);
		}
		
		if (!TempleManager.locationMap.containsKey(p))
		    TempleManager.locationMap.put(p, p.getLocation());
		
		if(!TCUtils.hasPlayerInventory(p.getName()))
			TCUtils.keepPlayerInventory(p);
		
		p.setHealth(20);
		p.setFoodLevel(20);
		p.setExperience(0);
		
		//convertLobby();
		p.teleport(lobbyLoc);
		tellPlayer(p, "You joined "+temple.templeName+". Have fun!");
	}
	
	/**
	* Adds a joined Temple player to the set of ready players.
	*/
	public void playerReady(Player p)
	{
	readySet.add(p);
	
	if (readySet.equals(playerSet) && !isRunning)
	    startGame();
	}
	
	/**
	* Prints the list of players currently in the Temple session.
	*/
	public void playerList(Player p, boolean tellIfEmpty){
		if (playerSet.isEmpty())
		{
			if(tellIfEmpty)
				tellPlayer(p, "There is no one in the Temple right now.");
		    return;
		}
		
		StringBuffer list = new StringBuffer();
		final String SEPARATOR = ", ";
		for (Player player : playerSet)
		{
		    list.append(player.getName());
		    list.append(SEPARATOR);
		}
		
		tellPlayer(p, ChatColor.GRAY + "Playing in " + gameName + ": " + ChatColor.WHITE + list.substring(0, list.length() - 2));
	}
	
	/**
	* Prints the list of players who aren't ready.
	*/
	public void notReadyList(Player p)
	{
		if (playerSet.isEmpty())
		{
		    tellPlayer(p, "No one is in " + temple.templeName + ".");
		    return;
		}
		
		Set<Player> notReadySet = new HashSet<Player>(playerSet);
		notReadySet.removeAll(readySet);
		
		if (notReadySet.isEmpty())
		{
		    tellPlayer(p, "Everyone is ready in " + temple.templeName + ".");
		    return;
		}
		
		StringBuffer list = new StringBuffer();
		final String SEPARATOR = ", ";
		for (Player player : notReadySet)
		{
		    list.append(player.getName());
		    list.append(SEPARATOR);
		}
		
		tellPlayer(p, ChatColor.GRAY + "Not ready in " + temple.templeName + ": " + ChatColor.WHITE + list.substring(0, list.length() - 2));
	}
	
	/**
	* Forcefully starts the Temple, causing all players in the
	* playerSet who aren't ready to leave, and starting the
	* Temple for everyone else.
	*/
	public void forceStart(Player p){
		if (isRunning)
		{
		    tellPlayer(p, "Game has already started.");
		    return;
		}
		if (readySet.isEmpty())
		{
		    tellPlayer(p, "Can't force start, no players are ready.");
		    return;
		}
		
		Iterator<Player> iterator = playerSet.iterator();
		while (iterator.hasNext())
		{
		    Player player = iterator.next();
		    if (!readySet.contains(player))
		    	TempleManager.playerLeave(player);
		}
		
		if(p != null)
			tellPlayer(p, "Forced Game start.");
	}
	
	/**
	* Forcefully ends the Temple, causing all players to leave and
	* all relevant sets and maps to be cleared.
	*/
	public void forceEnd(Player p){
		if (playerSet.isEmpty() && p != null){
		    tellPlayer(p, "No one is in the Temple.");
		    return;
		}
		
		endGame();
		
		if(p != null)
			tellPlayer(p, "Forced Game end.");
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
	    CLEANUP METHODS
	
	// ///////////////////////////////////////////////////////////////////// */
	
	/**
	* Kills all monsters currently on the Temple floor.
	*/
	public void killMonsters()
	{        
	// Remove all monsters, then clear the Set.
	for (LivingEntity e : monsterSet)
	{
	    if (!e.isDead())
	        e.remove();
	}
	monsterSet.clear();
	}
	
	/**
	* Removes all the blocks on the Temple floor.
	*/
	public void clearTempBlocks()
	{
	// Remove all blocks, then clear the Set.
	for (Block b : tempBlockSet)
	    b.setType(Material.AIR);
	
	tempBlockSet.clear();
	}
	
	/**
	* Removes all items and slimes in the Temple region.
	*/
	public void clearEntities(){	
    	for (Entity e : world.getEntities())
    		if(!(e instanceof Player))
    			e.remove();
	}
	
	/**
	* Removes a dead player from the Temple session.
	* The player is teleported safely back to the spectator area,
	* and their health is restored. All sets and maps are updated.
	* If this was the last player alive, the Temple session ends.
	*/
	public void playerDeath(Player p)
	{
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		
		deadSet.add(p);
		MobArenaClasses.classMap.remove(p);
		tp.tempSet.clear();
		tp.roundDeaths++;
		
		p.setHealth(20);
		p.setFireTicks(0);
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
    MISC METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	/**
	* Sends a message to a player.
	*/
	public void tellPlayer(Player p, String msg)
	{
	if (p == null)
	    return;
	
	p.sendMessage(ChatColor.GREEN + "[TC] " + ChatColor.WHITE + msg);
	}
	
	/**
	* Sends a message to all players in the Temple.
	*/
	public void tellAll(String msg)
	{
		for(Player p: playerSet)
			tellPlayer((Player)p, msg);	    
	}
}
