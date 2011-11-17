package com.msingleton.templecraft.scoreboards;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import com.msingleton.templecraft.TCUtils;

public class ScoreBoard {
	public String id;
	protected Location p1;
	protected Location p2;
	protected String templeName;
	protected String gameMode;
	protected String type;
	protected List<String> headers;
	protected List<Sign> signs;
	protected static File configFile = TCUtils.getConfig("scoreboards");

	public ScoreBoard(String id, Location p1, Location p2, String templeName, String gameMode){
		System.out.println("New Scoreboard Created: "+id+", ("+p1.getX()+","+p1.getY()+","+p1.getZ()+"),"+templeName+","+gameMode);
		this.id         = id;
		this.p1         = p1;
		this.p2         = p2;
		this.templeName = templeName;
		this.gameMode   = gameMode;
		this.type       = "";
		
		headers = getHeaders(gameMode);
		signs   = getSigns(p1, gameMode);
		nudgeP2();
	}
	
	protected void updateScores(List<String> scores){}
	
	private void nudgeP2() {
		if(p1.getBlockX() != p2.getBlockX() && p1.getBlockZ() != p2.getBlockZ())
			return;
		Sign sign = signs.get(0);
		Material type = sign.getType();
		byte data = sign.getRawData();
       	if(type == Material.WALL_SIGN)
           	if(data == 2)
        		p2.setZ(p2.getZ()+1);
           	else if(data == 3)
           		p2.setZ(p2.getZ()-1);
           	else if(data == 4)
           		p2.setX(p2.getX()-1);
           	else if(data == 5)
           		p2.setX(p2.getX()+1);
		if(type == Material.SIGN_POST && p1.getBlockY() == p2.getBlockY())
			p2.setY(p2.getY()-1);
	}

	protected void save() {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		ConfigurationSection boards = config.getConfigurationSection("ScoreBoards");
		if(!boards.isConfigurationSection(id+""))
			boards.createSection(id+"");
		ConfigurationSection board = boards.getConfigurationSection(id+"");
		ConfigurationSection loc1 = board.createSection("p1");
		loc1.set("world", p1.getWorld().getName());
		loc1.set("x", p1.getX());
		loc1.set("y", p1.getY());
		loc1.set("z", p1.getZ());
		ConfigurationSection loc2 = board.createSection("p2");
		loc2.set("world", p2.getWorld().getName());
		loc2.set("x", p2.getX());
		loc2.set("y", p2.getY());
		loc2.set("z", p2.getZ());
		board.set("temple", templeName);
		board.set("gamemode", gameMode);
		board.set("type", type);
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private List<String> getHeaders(String gameMode) {
		List<String> result = new ArrayList<String>();
		if(gameMode.equals("race")){
			result.add("Player");
			result.add("Time");
		} else if(gameMode.equals("adventure")){
			result.add("Player");
			result.add("Kills");
			result.add("Gold Collected");
			result.add("Deaths");
			result.add("Time");
		}
		return result;
	}

	private List<Sign> getSigns(Location loc, String gameMode) {
		List<Sign> result = new ArrayList<Sign>();
		Block b = loc.getBlock();
		World world = b.getWorld();
		int id = b.getTypeId();
		byte data = b.getData();
		int x = loc.getBlockX();
    	int y = loc.getBlockY();
    	int z = loc.getBlockZ();
    	if(b.getState() instanceof Sign){
    		Sign sign = (Sign) b.getState();
    		if(sign.getLine(0).equals(""))
    			b.setTypeId(0);
    	} else {
    		deleteScoreBoardConfig(this.id);
    		return new ArrayList<Sign>();
    	}
		for (String s : headers){
			Block nb = world.getBlockAt(x, y, z);
			Sign newSign;
			if(!(nb.getState() instanceof Sign)){
				nb.setTypeId(0);
            	nb.setTypeIdAndData(id, data, false);
			}
			newSign = (Sign) nb.getState();
			if(!newSign.getLine(0).equals(s))
	           	newSign.setLine(0, s);
			result.add(newSign);
            Material type = b.getType();
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
		ArrayList<Sign> signs = new ArrayList<Sign>();
		signs.addAll(result);
		// Add all the signs bellow the headers
		for(Sign sign : signs)
			result.addAll(signsBelow(sign.getBlock()));
		if(p2.equals(p1))
			p2 = new Location(p1.getWorld(), x, y, z);
		return result;
	}

	private List<Sign> signsBelow(Block b) {
		List<Sign> tempSet = new ArrayList<Sign>();
		if(!inRegion(b.getLocation()))
			return tempSet;
		if(b.getRelative(0,-1,0).getState() instanceof Sign){
			Sign newSign = (Sign)b.getRelative(0, -1, 0).getState();
			//System.out.println(b.getRelative(0, -1, 0).getLocation());
			tempSet.add(newSign);
			tempSet.addAll(signsBelow(b.getRelative(0,-1,0)));
		}
		return tempSet;
	}

	public static boolean isBetween(int a, int b, int c) {
	    return b >= a ? c >= a && c <= b : c >= b && c <= a;
	}
	
	public boolean inRegion(Location loc) {
		int x1 = loc.getBlockX();
		int y1 = loc.getBlockY();
		int z1 = loc.getBlockZ();
		int x2 = p1.getBlockX();
		int y2 = p1.getBlockY();
		int z2 = p1.getBlockZ();
		int x3 = p2.getBlockX();
		int y3 = p2.getBlockY();
		int z3 = p2.getBlockZ();
	
		return isBetween(x3,x2,x1) && isBetween(y3,y2,y1) && isBetween(z3,z2,z1);
	}

	public boolean expandBoard() {
		for(Sign sign : signs)
			if(sign.getY() == p2.getBlockY() && sign.getBlock().getRelative(0,-1,0).getTypeId() != 0)
				return false;
		//System.out.println("Expand ScoreBoard "+id);
		Set<Sign> tempSet = new HashSet<Sign>();
		for(Sign sign : signs){
			Block b = sign.getBlock();
			Block rb = b.getRelative(0,-1,0);
			if(rb.getTypeId() == 0){
				rb.setTypeIdAndData(sign.getTypeId(), sign.getRawData(), false);
				tempSet.add((Sign)rb.getState());
			}
		}
		signs.addAll(tempSet);
		p2.setY(p2.getY()-1);
		return true;
	}

	public boolean contractBoard() {
		if(p1.getBlockY() == p2.getBlockY())
			return false;
		//System.out.println("Contract ScoreBoard "+id);
		Set<Sign> tempSet = new HashSet<Sign>();
		for(Sign sign : signs){
			Block b = sign.getBlock();
			if(b.getRelative(0,-1,0).getTypeId() != sign.getTypeId())
				tempSet.add(sign);
		}
		for(Sign sign : tempSet){
			signs.remove(sign);
			sign.getBlock().setTypeId(0);
		}
		p2.setY(p2.getY()+1);
		return true;
	}
	
	private void deleteScoreBoardConfig(String id) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		if(!config.isConfigurationSection("ScoreBoards")){
			config.createSection("ScoreBoards");
			saveConfig(config);
			return;
		}
		ConfigurationSection scoreBoards = config.getConfigurationSection("ScoreBoards");
		scoreBoards.set(id, null);
		saveConfig(config);
	}
	
	private void saveConfig(YamlConfiguration config) {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
