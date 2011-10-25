package com.msingleton.templecraft.games;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import com.msingleton.templecraft.MobArenaClasses;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.nijikokun.register.payment.Method.MethodAccount;

public class Adventure extends Game{	
	public Map<Location,Integer> checkpointMap = new HashMap<Location,Integer>();
    public Map<Location,String[]> chatMap      = new HashMap<Location,String[]>();
	
	public Adventure(String name, Temple temple, World world) {
		super(name, temple, world);
	}
	
	public void endGame(){
		TempleManager.tellAll("Adventure game finished in: \""+temple.templeName+"\"");
		super.endGame();
	}
	
	public Location getPlayerSpawnLoc() {
		Random r = new Random();
		Location loc = null;
		for(Location l : startLocSet){
			if(!l.getWorld().equals(world))
				l.setWorld(world);
			
			if(loc == null)
				loc = l;
			else if(r.nextInt(startLocSet.size()) == 0)
				loc = l;
		}
		return loc;
	}
	
	public void playerDeath(Player p)
	{
		String msg;
		if(TempleCraft.method != null){
			String s = TempleCraft.method.format(2.0);
			String currencyName = s.substring(s.indexOf(" ") + 1);
			MethodAccount balance = TempleCraft.method.getAccount(p.getName());
			if(balance.hasEnough(rejoinCost)){
				if(TempleCraft.method != null && rejoinCost > 0){
					msg = "To continue playing will cost you "+ChatColor.GOLD+rejoinCost+" "+currencyName+".";
					TempleManager.tellPlayer(p, msg);
					msg = "Or type \"/tc leave\" and restart from the beginning!";
					TempleManager.tellPlayer(p, msg);
				} else {
					//msg = "Rejoin your friends! :O";
					//TempleManager.tellPlayer(p, msg);
				}
			} else {
				msg = "You do not have enough "+currencyName+" to rejoin.";
				TempleManager.tellPlayer(p, msg);
				msg = "Please type \"/tc leave\" to leave the temple.";
				TempleManager.tellPlayer(p, msg);
			}
		}
		p.teleport(lobbyLoc);
		super.playerDeath(p);
	}
	
	protected void handleSign(Sign sign) {
		String[] Lines = sign.getLines();
		Block b = sign.getBlock();
		
		
		if(!Lines[0].equals("[TempleCraft]") && !Lines[0].equals("[TC]")){
			if(Lines[0].equals("[TempleCraftM]") || Lines[0].equals("[TCM]")){
				String[] newLines = {Lines[1]+Lines[2],Lines[3]};
				chatMap.put(b.getLocation(), newLines);
				b.setTypeId(0);
			}
			return;
		}
			
		if(Lines[1].toLowerCase().equals("classes")){
			if(MobArenaClasses.enabled){
				usingClasses = true;
				MobArenaClasses.generateClassSigns(sign);
			}
		} else if(Lines[1].toLowerCase().equals("checkpoint")){
			try{
				checkpointMap.put(sign.getBlock().getLocation(), Integer.parseInt(Lines[3]));
			} catch(Exception e){
				checkpointMap.put(sign.getBlock().getLocation(), 5);
			}
			b.setTypeId(0);
		}
		super.handleSign(sign);
	}
}
