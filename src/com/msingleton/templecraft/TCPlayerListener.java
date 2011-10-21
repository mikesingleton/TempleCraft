package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.msingleton.templecraft.games.*;
import com.nijikokun.register.payment.Method.MethodAccount;

public class TCPlayerListener  extends PlayerListener{

	public TCPlayerListener(TempleCraft templeCraft) {		
	}
	
	 public void onPlayerInteract(PlayerInteractEvent event){    
		 
		 if(!TempleManager.isEnabled)
				return;
		 
        Player p = event.getPlayer();        
        TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        Action a = event.getAction();
        Game game = tp.currentGame;
			
        // Special Use of TNT for Zombies Mode
        if(game != null && (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK))){
        	if(game instanceof Zombies){
	        	if(p.getInventory().getItemInHand().getTypeId() == 46){
	        		if(a.equals(Action.RIGHT_CLICK_BLOCK))
	        			event.setCancelled(true);
		        	ItemStack item = p.getInventory().getItemInHand();
		        	if(item.getAmount() <= 1)
		        		p.getInventory().remove(item);
		        	else
		        		item.setAmount(item.getAmount()-1);
		        	((Zombies)tp.currentGame).throwTNT(p);
		        	event.setCancelled(true);
	        	}
        	}
        }
        
        // All methods bellow have clicked blocks
        if(!event.hasBlock())
        	return;
        
        Block b = event.getClickedBlock();
        
        // Break wool instantly if playing Spleef
        if(game != null && a.equals(Action.LEFT_CLICK_BLOCK))
        	if(game instanceof Spleef)
        		if(b.getTypeId() == 35){
        			((Spleef)game).brokenBlockMap.put(b.getLocation(), b.getTypeId()+":"+b.getData());
        			b.setTypeId(0);
        		}
    	
        // Signs
        if (b.getState() instanceof Sign){   
	        // Cast the block to a sign to get the text on it.
	        Sign sign = (Sign) event.getClickedBlock().getState();
	        if(game == null)
	        	handleSignClicked(p, sign);
	        else if(tp.currentGame instanceof Zombies)
	        	((Zombies)game).handleSignClicked(p, sign);
		 }
        
        if (!TempleManager.playerSet.contains(p))
            return;
    	
        if(game == null)
        	return;
        
        // Start Block
        if (game.lobbyLocSet.contains(b.getLocation())){
        	if(!game.usingClasses || MobArenaClasses.classMap.containsKey(p)){
	        	if(!game.isRunning){
		            game.tellPlayer(p, "You have been flagged as ready!");
		            game.playerReady(p);
		         // If a method is installed
	        	} else if(TempleCraft.method != null){	        		
	        		MethodAccount balance = TempleCraft.method.getAccount(p.getName());
	        		// if player has enough money subtract money from account
	        		if(balance.hasEnough(game.rejoinCost)){    				
	    				if(game.rejoinCost > 0){
	    					String msg = ChatColor.GOLD + "" + game.rejoinCost+" gold"+ChatColor.WHITE+" has been subtracted from your account.";
	    					game.tellPlayer(p, msg);
	    					balance.subtract(game.rejoinCost);
		            	}
	    				
	    				game.deadSet.remove(p);
		        		if(tp.currentCheckpoint != null)
							p.teleport(tp.currentCheckpoint);
						else
							p.teleport(game.getPlayerSpawnLoc());
		        		
		        		if(!game.usingClasses){
		    				if(TCUtils.hasPlayerInventory(p.getName()))
		    					TCUtils.restorePlayerInventory(p);
		    				TCUtils.keepPlayerInventory(p);
		    				p.setHealth(20);
						}
	        		} else {
	        			TempleManager.tellPlayer(p, "You do not have enough gold to rejoin.");
	        		}
	        	} else {
	        		game.deadSet.remove(p);
	        		if(tp.currentCheckpoint != null)
						p.teleport(tp.currentCheckpoint);
					else
						p.teleport(game.getPlayerSpawnLoc());
	        		
	        		if(!game.usingClasses){
	    				if(TCUtils.hasPlayerInventory(p.getName()))
	    					TCUtils.restorePlayerInventory(p);
	    				TCUtils.keepPlayerInventory(p);
	    				p.setHealth(20);
					}
	        	}
        	} else {
        		game.tellPlayer(p, "You must pick a class first!");
        	}
        // End Block
        } else if (game.endLocSet.contains(b.getLocation()) && b.getTypeId() == 41){
            if (game.playerSet.contains(p))
            {
            	game.readySet.add(p);
            	if(game.readySet.equals(game.playerSet)){
            		game.endGame();
            	} else {
            		game.tellPlayer(p, "You are ready to leave!");
            	}
            }
            else
            {
                game.tellPlayer(p, "WTF!? Get out of here!");
            }
            return;
        }
	}

	public void onPlayerMove(PlayerMoveEvent event){
		Player p = event.getPlayer();
		
		if (!TCUtils.isTCWorld(event.getPlayer().getWorld()))
            return;
		
		if(!TempleManager.isEnabled || TempleManager.templeSet.isEmpty())
			return;
		
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Temple temple = tp.currentTemple;
		
		if(temple == null)
			return;
		
		// Slows hurt players in Zombies
		if(tp.currentGame instanceof Zombies){
        	Zombies game = (Zombies) tp.currentGame;
        	if(game.hurtSet.contains(p)){
        		Vector v = p.getVelocity();
        		v.setX(p.getVelocity().getX()*(3/4));
        		v.setZ(p.getVelocity().getZ()*(3/4));
        		p.setVelocity(v);
        	}
    	}
		
		// Spawns mobs in Adventure
		if(tp.currentGame instanceof Adventure){
			Adventure game = (Adventure) tp.currentGame;
			
			if(game == null || !game.isRunning)
				return;
			
			for(Location loc : game.checkpointMap.keySet()){
				if(tp.currentCheckpoint != loc && TCUtils.distance(loc, p.getLocation()) < game.checkpointMap.get(loc)){
					tp.currentCheckpoint = loc;
				}
			}
			
			for(Location loc : game.chatMap.keySet()){
				if(tp.tempSet.contains(loc))
					continue;
				
				String[] msg = game.chatMap.get(loc);
				int range;
				String s;
				try{
					range = Integer.parseInt(msg[1]);
					s = msg[0];
				}catch(Exception e){
					range = 5;
					s = msg[0]+msg[1];
				}
				
				if(TCUtils.distance(loc, p.getLocation()) < range){
					if(msg[0].startsWith("/")){
						tp.tempSet.add(s);
						p.chat(s);
					} else {
						p.sendMessage(ChatColor.DARK_AQUA+"Message: "+ChatColor.WHITE+s);
					}
					tp.tempSet.add(loc);
				}
			}
			
			Set<Location> tempLocs = new HashSet<Location>();
			for(Location loc : game.mobSpawnpointMap.keySet()){
				if(TCUtils.distance(loc, p.getLocation()) < 20){
					tempLocs.add(loc);
				}
			}
			for(Location loc : tempLocs)
				TCMobHandler.SpawnMobs(game, loc, game.mobSpawnpointMap.remove(loc));
		}
	 }
	 
	private void handleSignClicked(Player p, Sign sign) {
		String Line1 = sign.getLine(0);
        String Line2 = sign.getLine(1);
        String Line3 = sign.getLine(2);
        //String Line4 = sign.getLine(3);
        
        if(TempleManager.playerSet.contains(p) || TCUtils.isTCEditWorld(p.getWorld()))
        	return;
        
    	if(!Line1.equals("[TempleCraft]") && !Line1.equals("[TC]"))
    		return;
    	
    	Temple temple = TCUtils.getTempleByName(Line2.toLowerCase());
    	
    	if(temple == null){
    		TempleManager.tellPlayer(p, "Temple \""+Line2+"\" does not exist");
    		return;
    	}
    	
    	if(!temple.isSetup){
			TempleManager.tellPlayer(p, "Temple \""+temple.templeName+"\" is not setup");
			return;
		}
    	
    	String mode;
    	if(Line3.equals(""))
    		mode = "adventure";
    	else
    		mode = Line3.toLowerCase();
    	
    	if(!TempleManager.modes.contains(mode)){
    		TempleManager.tellPlayer(p, "Mode \""+Line3+"\" does not exist");
    		return;
    	}
    	
    	for(Game game : TempleManager.gameSet){
    		if(!game.isRunning && game.temple.equals(temple) && game.getClass().getName().toLowerCase().equals(mode)){
    			game.playerJoin(p);
    			return;
    		}
    	}
    	
    	if(!TempleManager.gameSet.isEmpty())
    		TempleManager.tellPlayer(p, "All available games currently in progress.");
    	
    	TempleManager.tellPlayer(p, "Creating a new game...");
    	
    	String gameName = TCUtils.getUniqueGameName(temple.templeName, mode);
    	Game game = TCUtils.newGame(gameName, temple, mode);
    	if(game != null)
    		game.playerJoin(p);
    }
	
	public void onFoodLevelChange(FoodLevelChangeEvent event){
		
		if(!(event.getEntity() instanceof Player))
			return;
		
		Player p = (Player)event.getEntity();
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        Game game = tp.currentGame;
		
		if (!TempleManager.playerSet.contains(p))
            return;
		
		if(game instanceof Zombies || game instanceof Spleef)
			event.setCancelled(true);
	}
	
	/**
     * Adds liquid blocks to the blockset when players empty their buckets.
     */
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event)
    {        	
    	Player p = event.getPlayer();
    	
    	if (!TempleManager.playerSet.contains(p))
            return;
        
        if (!TempleManager.isEnabled)
        {
            event.getBlockClicked().setTypeId(0);
            event.setCancelled(true);
            return;
        }
        
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Game game = tp.currentGame;

        Block liquid = event.getBlockClicked();
        if(game.playerSet.contains(p))
        	game.tempBlockSet.add(liquid);
    }
}
