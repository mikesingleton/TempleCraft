package com.msingleton.templecraft;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.Door;
import org.bukkit.util.config.Configuration;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;


public class Temple {
    public World world;
	
    // Convenience variables.
    public String templeName     = null;
    public Location templeLoc    = null;
    public Location lobbyLoc     = null;
    public Location spectatorLoc = null;
    protected boolean isRunning     = false;
    protected boolean isSetup       = false;
    protected boolean isEnabled     = true;
    protected boolean isProtected   = true;
    protected boolean isRestoreMode = true;
    public int specialModulo, minLevel, templeWidth, templeHeight, templeDepth, roomWidth, roomHeight, roomLength;
    public int TempleRooms, BossRooms, SpawnRooms, ItemRooms, Rooms; 
    protected boolean lightning, explosionDamage;
    
    // Location variables for the Temple region.
    public Location p1 = null;
    protected Location p2 = null;
    
    // Spawn locations list and monster distribution fields.
    protected int dZombies, dSkeletons, dSpiders, dCreepers, dWolves;
    protected int dPoweredCreepers, dPigZombies, dSlimes, dMonsters,
                         dAngryWolves, dGiants, dGhasts;
    
    // Sets and Maps for storing players, their locations, and their rewards.
    protected Set<Player> playerSet            = new HashSet<Player>();
    protected Set<Player> spectatorSet         = new HashSet<Player>();
    protected Set<Player> readySet             = new HashSet<Player>();
    protected Map<Player,String> rewardMap     = new HashMap<Player,String>();
    
    // Maps for rewards.
    protected Map<Integer,String> rewardEveryWaveMap = new HashMap<Integer,String>();
    protected Map<Integer,String> rewardAfterWaveMap = new HashMap<Integer,String>();
    
    // Maps for rewards during play.
    protected Map<Integer,String> itemEveryWaveMap = new HashMap<Integer,String>();
    protected Map<Integer,String> itemAfterWaveMap = new HashMap<Integer,String>();
    
    // Special Traits
    //protected Map<String,String> specialTraitsMap = new HashMap<String,String>();
    
    // Maps for rewards during play.
    protected List<String> enabledSpawnpoints = new ArrayList<String>();
    protected List<String> spawnpointsMobs = new ArrayList<String>();
    public Set<Location> activeSpawnpoints = new HashSet<Location>();
    public Set<Location> inactiveSpawnpoints = new HashSet<Location>();
    
    // Maps for exp
    public Map<String, Integer> expBuffer = new HashMap<String, Integer>();
    public Map<Integer, Entity> lastDamager = new HashMap<Integer, Entity>();
    
    // Entities, blocks and items on TempleCraft floor.
    protected Map<Integer,Integer> mobGoldMap = new HashMap<Integer,Integer>();
    protected Set<LivingEntity> monsterSet = new HashSet<LivingEntity>();
    protected Set<Block> blockSet          = new HashSet<Block>();
    protected Set<Block> endBlockSet       = new HashSet<Block>();
    public Set<Block> lobbyBlockSet        = new HashSet<Block>();
    public static int mobSpawner = 7;
    public static int diamondBlock = 57;
    public static int ironBlock = 42;
    public static int goldBlock = 41;
    
    public static int JoinCost = 500;
    
	public Temple(){
	}

	public Temple(String name){		
		templeName = name;
		world      = TempleManager.world;
		minLevel   = 0;
		isRunning  = false;
		
		TempleManager.templeSet.add(this);
	}

    /* ///////////////////////////////////////////////////////////////////// //
	
    LOAD/SAVE METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	public void saveTemple(World w, Player p, String fileName){
		if(p1 == null || p2 == null){
			TCRestore.saveRegion(new Location(w, 0, 0, 0), new Location(w, 0, 0, 0), "SavedTemples/"+fileName);
			TempleManager.tellPlayer(p, "Temple Saved");
			return;
		}
		
	    int x1 = (int)p1.getX();
	    int z1 = (int)p1.getZ();
	    int x2 = (int)p2.getX();
	    int z2 = (int)p2.getZ();
		
		TCRestore.saveRegion(new Location(w, x1, 0, z1), new Location(w, x2, 128, z2), "SavedTemples/"+fileName);
		TempleManager.tellPlayer(p, "Temple Saved");
	}
	
	public void loadTemple(World w){
		clearTemple();
		p1 = null;
		p2 = null;
		
		Location startLoc = getFreeLocation(w);
		TCRestore.loadRegion(startLoc, "SavedTemples/"+templeName);
		
		if(!isSetup){
			if(trySetup()){
				isSetup = true;
				loadTemple(w);
			}
		}
		
		if(p1 != null && p2 != null)
			clearEntities();
	}
	
	private void clearTemple() {
		lobbyBlockSet.clear();
		endBlockSet.clear();
		if(p1 == null || p2 == null)
			return;
		for(int i = p1.getBlockX(); i <= p2.getBlockX(); i++)
			for(int j = p1.getBlockZ(); j <= p2.getBlockZ(); j++)
				TempleManager.world.regenerateChunk(i, j);
	}

	private Location getFreeLocation(World w) {
		if(!w.equals(TempleManager.world))
			return new Location(w, 0, 0, 0);
		
		int MaxX = 0;
		for(Temple temple : TempleManager.templeSet){
			if(temple.p2 != null && !temple.equals(this) && MaxX < temple.p2.getBlockX()+2)
				MaxX = temple.p2.getBlockX()+2;
			
		}
		Location loc = new Location(w, MaxX, 0, 0);
		return loc;
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
    
    Temple METHODS

	// ///////////////////////////////////////////////////////////////////// */
	
	/**
	* Starts the current TempleCraft session.
	*/
	public void startTemple()
	{		
		isRunning = true;
		readySet.clear();
		
		convertSpawnpoints();
		for (Player p : playerSet)
		{
		    p.teleport(templeLoc);
		    rewardMap.put(p,"");
		}
		
		tellAll("Let the slaughter begin!");
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
    		}
		}
	}
	
	private void handleSign(Sign sign) {
		String[] Lines = sign.getLines();
		if(!Lines[0].equals("[TC]"))
			return;
		
		if(Lines[1].toLowerCase().equals("lobby")){
			lobbyLoc = sign.getBlock().getLocation();
			sign.getBlock().setTypeId(0);
		}
		
		if(Lines[1].toLowerCase().equals("classes"))
			generateClassSigns(sign);
	}
	
	private void generateClassSigns(Sign sign) {
		Block b = sign.getBlock();
		Location loc = b.getLocation();
		int x = loc.getBlockX();
    	int y = loc.getBlockY();
    	int z = loc.getBlockZ();
		for (String s : TempleManager.classes){
            TempleManager.world.getBlockAt(x, y, z).setTypeIdAndData(68, b.getData(), false);
            Sign classSign = (Sign) TempleManager.world.getBlockAt(x, y, z).getState();
           	classSign.setLine(0, "");
            classSign.setLine(1, s);
            classSign.setLine(2, "");
            classSign.setLine(3, "");
           	if(b.getType() == Material.WALL_SIGN){
	           	if(b.getData() == 2)
	        		x--;
				if(b.getData() == 3)
	        		x++;
				if(b.getData() == 4)
	        		z++;
				if(b.getData() == 5)
	        		z--;
           	}
			if(b.getType() == Material.SIGN_POST){
				if(b.getData() == 8)
	        		x--;
				if(b.getData() == 0)
	        		x++;
				if(b.getData() == 4)
	        		z++;
				if(b.getData() == 12)
	        		z--;
			}
        }
	}
	
	private void convertSpawnpoints() {
		for(Block b: getBlockSet(mobSpawner)){
    		activeSpawnpoints.add(b.getLocation());
    		b.setTypeId(0);
		}
	    for(Block b: getBlockSet(diamondBlock)){
    		Block rb = b.getRelative(0, -1, 0);
    		if(rb.getTypeId() == ironBlock){
    			templeLoc = rb.getLocation();
    			b.setTypeId(0);
    			rb.setTypeId(0);
    		} else if(rb.getTypeId() == goldBlock){
    			endBlockSet.add(rb);
    			b.setTypeId(0);
    			rb.setTypeId(goldBlock);
    		}
		}
	}

	private Set<Block> getBlockSet(int id){
		if(p1 == null || p2 == null)
			return new HashSet<Block>();
			
		int x1 = (int)p1.getX();
	    int y1 = (int)p1.getY();
	    int z1 = (int)p1.getZ();
	    int x2 = (int)p2.getX();
	    int y2 = (int)p2.getY();
	    int z2 = (int)p2.getZ();
	    
	    Set<Block> result = new HashSet<Block>();
	    
	    for (int j = y1; j <= y2; j++)
	    	for (int i = x1; i <= x2; i++)
	            for (int k = z1; k <= z2; k++)
	            	if(TempleManager.world.getBlockAt(i,j,k).getTypeId() == id)
	            		result.add(TempleManager.world.getBlockAt(i,j,k));
	    
	    return result;
	}

	/**
	* Ends the current TempleCraft session.
	* Clears the Temple floor, gives all the players their rewards,
	* and stops the spawning of monsters.
	*/
	public void endTemple()
	{
		isRunning = false;
		
		TempleManager.tellAll("Temple: \""+templeName+"\" finished.");
		removePlayers();
		killMonsters();
		activeSpawnpoints.clear();
		inactiveSpawnpoints.clear();
		loadTemple(world);
		clearEntities();
	}
	
	// Removes players from temple
	public void removePlayers(){
		Set<Player> tempSet = new HashSet<Player>();
		tempSet.addAll(playerSet);
		tempSet.addAll(spectatorSet);
		
		Iterator<Player> iterator = tempSet.iterator();
		while (iterator.hasNext())
		    TempleManager.playerLeave(iterator.next());
	}
	
	/**
	* Attempts to let a player join the Temple session.
	* Players must have an empty inventory to join the Temple. Their
	* location will be stored for when they leave.
	*/
	public void playerJoin(Player p)
	{
	if (!TempleManager.isEnabled || !this.isEnabled)
	{
	    tellPlayer(p, "TempleCraft is not enabled.");
	    return;
	}
	if (!isSetup)
	{
		tellPlayer(p, "Temple \""+templeName+"\" has not been set up yet!");
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
	if (isRunning)
	{
	    tellPlayer(p, "Temple in progress. Type /ma spec to watch.");
	    return;
	}
	
	TempleManager.templePlayerMap.get(p).currentTemple = this;
	TempleManager.playerSet.add(p);
	playerSet.add(p);
	
	if (!TempleManager.locationMap.containsKey(p))
	    TempleManager.locationMap.put(p, p.getLocation());
	
	if(!TCUtils.hasPlayerInventory(p.getName()))
		TCUtils.keepPlayerInventory(p);
	
	convertLobby();
	p.teleport(lobbyLoc);
	tellPlayer(p, "You joined the Temple. Have fun!");
	}

	private boolean trySetup() {
		convertLobby();
		convertSpawnpoints();
		if(lobbyLoc == null || templeLoc == null)
			return false;
		return true;
	}

	/**
	* Adds a joined Temple player to the set of ready players.
	*/
	public void playerReady(Player p)
	{
	readySet.add(p);
	
	if (readySet.equals(playerSet) && !isRunning)
	    startTemple();
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
		Holdings balance = iConomy.getAccount(p.getName()).getHoldings();
		
		for(Player player : playerSet){
			String msg = p.getName() + " died!";
			if(player.equals(p)){
				int xpLost = tp.getDeathXP();
				tp.addXp(tp.currentClass, -xpLost);
				msg += ChatColor.DARK_RED + " (-"+xpLost+" XP)";
			}
			tellPlayer(player, msg);
		}
		
		readySet.remove(p);
		tp.roundDeaths++;
		tp.currentClass = null;
		if(balance.hasEnough(JoinCost)){
			String msg = "To continue playing will cost you "+ChatColor.GOLD+JoinCost+" gold.";
			TempleManager.tellPlayer(p, msg);
			msg = "Or type \"/tc leave\" and restart from the beginning!";
			TempleManager.tellPlayer(p, msg);
			p.setHealth(20);
			p.teleport(lobbyLoc);
		} else {
			String msg = "You do not have enough gold to rejoin.";
			TempleManager.tellPlayer(p, msg);
			tp.displayStats();
			playerSet.remove(p);
			TempleManager.playerSet.remove(p);
			tp.saveData();
			
			if(TCUtils.hasPlayerInventory(p.getName())) {
				TCUtils.restorePlayerInventory(p);
			}
		}
		if (isRunning && playerSet.isEmpty()){
			p.teleport(TempleManager.locationMap.get(p));
		    endTemple();
		}
		
		p.setFireTicks(0);
	}
	
	/**
	* Lets a player spectate the current Temple session.
	*/
	public void playerSpectate(Player p)
	{
	if (!playerSet.contains(p))
	{
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		spectatorSet.add(p);
	    p.teleport(templeLoc);
	    tellPlayer(p, "Enjoy the show!");
	}
	else
	{
	    tellPlayer(p, "Can't spectate when in the Temple!");
	}
	}
	
	/**
	* Prints the list of players currently in the Temple session.
	*/
	public void playerList(Player p)
	{
	if (playerSet.isEmpty())
	{
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
	
	tellPlayer(p, "Survivors: " + list.substring(0, list.length() - 2));
	}
	
	/**
	* Prints the list of players who aren't ready.
	*/
	public void notReadyList(Player p)
	{
	if (!playerSet.contains(p) || isRunning)
	{
	    tellPlayer(p, "You aren't in the lobby!");
	    return;
	}
	
	Set<Player> notReadySet = new HashSet<Player>(playerSet);
	notReadySet.removeAll(readySet);
	
	StringBuffer list = new StringBuffer();
	final String SEPARATOR = ", ";
	for (Player player : notReadySet)
	{
	    list.append(player.getName());
	    list.append(SEPARATOR);
	}
	
	tellPlayer(p, "Not ready: " + list.substring(0, list.length() - 2));
	}
	
	/**
	* Forcefully starts the Temple, causing all players in the
	* playerSet who aren't ready to leave, and starting the
	* Temple for everyone else.
	*/
	public void forceStart(Player p)
	{
	if (isRunning)
	{
	    tellPlayer(p, "Temple has already started.");
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
	
	tellPlayer(p, "Forced Temple start.");
	}
	
	/**
	* Forcefully ends the Temple, causing all players to leave and
	* all relevant sets and maps to be cleared.
	*/
	public void forceEnd(Player p)
	{
	if (playerSet.isEmpty() && p != null)
	{
	    tellPlayer(p, "No one is in the Temple.");
	    return;
	}
	
	// Just for good measure.
	endTemple();
	
	if(p != null)
		tellPlayer(p, "Forced Temple end.");
	}
	
	/* ///////////////////////////////////////////////////////////////////// //
	
	MOB METHODS
	
	// ///////////////////////////////////////////////////////////////////// */
	
	public void SpawnMobs(Location loc) {
		//for (int i = 0; i < playerSet.size(); i++)
	    {
			CreatureType mob = getRandomCreature();
			LivingEntity e = world.spawnCreature(loc,mob);
	        //monsterSet.add(e);
	        
	        Random r = new Random();
	        if(r.nextInt(3) == 0)
	        	mobGoldMap.put(e.getEntityId(), r.nextInt(50)+50);
	        
	        // Grab a random target.
	        Creature c = (Creature) e;
	        c.setTarget(TCUtils.getClosestPlayer(this, e));
	    }
	}
	
	private CreatureType getRandomCreature() {
		int dZombies, dSkeletons, dSpiders, dCreepers, dWolves;
		dZombies = 5;
		dSkeletons = dZombies + 5;
		dSpiders = dSkeletons + 5;
		dCreepers = dSpiders + 5;
		dWolves = dCreepers + 5;
		
		CreatureType mob;
		
		int ran = new Random().nextInt(dWolves);
		if      (ran < dZombies)   mob = CreatureType.ZOMBIE;
	    else if (ran < dSkeletons) mob = CreatureType.SKELETON;
	    else if (ran < dSpiders)   mob = CreatureType.SPIDER;
	    else if (ran < dCreepers)  mob = CreatureType.CREEPER;
	    else if (ran < dWolves)    mob = CreatureType.WOLF;
	    else return null;
		
		return mob;
	}
	
	public Set<Location> getSpawnpoints() {
		Set<Location> result = new HashSet<Location>();
		result.addAll(activeSpawnpoints);
		result.addAll(inactiveSpawnpoints);
		return result;
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
	public void clearBlocks()
	{
	// Remove all blocks, then clear the Set.
	for (Block b : blockSet)
	    b.setType(Material.AIR);
	
	blockSet.clear();
	}
	
	/**
	* Removes all items and slimes in the Temple region.
	*/
	public void clearEntities()
	{
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
	
	/* ///////////////////////////////////////////////////////////////////// //
	
	    REWARD METHODS
	
	// ///////////////////////////////////////////////////////////////////// */
	
	/**
	* Gives all the players the rewards they earned.
	*/
	public void giveRewards()
	{
	for (Player p : rewardMap.keySet())
	{
	    String r = rewardMap.get(p);
	    if (r.isEmpty()) continue;
	    
	    tellPlayer(p, "Here are all of your rewards!");
	    TCUtils.giveItems(true, p, r);
	}
	
	rewardMap.clear();
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
		Set<Player> tempSet = new HashSet<Player>();
		tempSet.addAll(playerSet);
		tempSet.addAll(spectatorSet);
		
		for(Player p: tempSet)
			tellPlayer((Player)p, msg);	    
	}
}
