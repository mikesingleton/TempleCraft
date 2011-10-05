package com.msingleton.templecraft;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Item;
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
        
        if(b == null)
        	return;
        
        // Break wool instantly if playing Spleef
        if(game != null && a.equals(Action.LEFT_CLICK_BLOCK))
        	if(game instanceof Spleef)
        		if(b.getTypeId() == 35){
        			((Spleef)game).brokenBlockMap.put(b.getLocation(), b.getTypeId()+":"+b.getData());
        			b.setTypeId(0);
        		}
        
        // if player clicks on a significant block while editing, record it
    	if(TCUtils.isTCEditWorld(p.getWorld()) && p.getItemInHand().getTypeId() == 0){
    		Temple temple = TCUtils.getTempleByWorld(p.getWorld());
	    	for(int i : Temple.coordBlocks){
	    		if(b.getTypeId() == i){
	    			if(a.equals(Action.LEFT_CLICK_BLOCK)){
	    				if(!temple.coordBlockSet.contains(b)){
	    					TempleManager.tellPlayer(p, "This block has now been logged");
	    					temple.coordBlockSet.add(b);
    					}
	    			}
	    			if(a.equals(Action.RIGHT_CLICK_BLOCK)){
	    				p.sendMessage(ChatColor.GREEN+"------TCBlock Info------");
	    				String msg = getTypeMsg(temple, b);
	    				if(msg == null){
	    					temple.coordBlockSet.remove(b);
	    					msg = "Not found; Unlogged";
	    				} else {
	    					temple.coordBlockSet.add(b);
	    				}
	    				p.sendMessage("Type: "+getTypeMsg(temple, b));
	    				p.sendMessage("Logged: "+getLoggedMsg(temple, b));
	    				p.sendMessage("Location: "+getLocationMsg(b));
	    			}
	    		}
    		}
    		return;
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
        if (game.lobbyBlockSet.contains(b)){
        	if(!game.usingClasses || MobArenaClasses.classMap.containsKey(p)){
	        	if(!game.isRunning){
		            game.tellPlayer(p, "You have been flagged as ready!");
		            game.playerReady(p);
	        	} else {
	        		MethodAccount balance = TempleCraft.method.getAccount(p.getName());
	        		if(TempleCraft.method == null || balance.hasEnough(game.rejoinCost)){
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
	    				
	    				// If a method is installed, subtract money from account
	    				if(TempleCraft.method != null && game.rejoinCost > 0){
	    					String msg = ChatColor.GOLD + "" + game.rejoinCost+" gold"+ChatColor.WHITE+" has been subtracted from your account.";
	    					game.tellPlayer(p, msg);
	    					balance.subtract(game.rejoinCost);
		            	}
	        		} else {
	        			TempleManager.tellPlayer(p, "You do not have enough gold to rejoin.");
	        		}
	        	}
        	} else {
        		game.tellPlayer(p, "You must pick a class first!");
        	}
        // End Block
        } else if (game.endBlockSet.contains(b) && b.getTypeId() == 41){
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

	private String getLoggedMsg(Temple temple, Block b) {
		 if(temple.coordBlockSet.contains(b))
			return ChatColor.GREEN+"true";
		else
			return ChatColor.RED+"false";
	}

	// Finds what the block will convert to and block is unlogged if no type is found
	private String getTypeMsg(Temple temple, Block b) {
		if(b.getType().equals(Material.WALL_SIGN) || b.getType().equals(Material.SIGN_POST)){
			Sign sign = (Sign)b.getState();
			String Line1 = sign.getLine(0);
			String Line2 = sign.getLine(1).toLowerCase();
			String Line3 = sign.getLine(2).toLowerCase();
			String Line4 = sign.getLine(3).toLowerCase();
			if(Line1.equals("[TC]") || Line1.equals("[TempleCraft]")){
    			if(Line2.equals("lobby"))
    				return "TCSign"+ChatColor.GRAY+"(LobbySpawnPoint)";
    			if(Line2.equals("classes"))
    				return "TCSign"+ChatColor.GRAY+"(ClassSignsSpawnpoint)";
    			String s = Line2+Line3+Line4;
    			for(String mob : TempleManager.mobs)
    				if(s.toLowerCase().contains(mob.toLowerCase())){
    					return "TCSign"+ChatColor.GREEN+"("+mob+"Spawnpoint)";
    				}	
			}
			if(Line1.equals("[TCM]"))
    			return "TCMSign"+ChatColor.GRAY+"(Message/Command)";
    			
    		temple.coordBlockSet.remove(b);
    		return null;
		}

		Block rb;
		if(b.getTypeId() == Temple.diamondBlock){
    		rb = b.getRelative(0, -1, 0);
    		if(rb.getTypeId() == Temple.ironBlock)
    			return "PlayerSpawnBlock"+ChatColor.AQUA+"(Diamond)";
    		rb = b.getRelative(0, -1, 0);
    		if(rb.getTypeId() == Temple.goldBlock)
    			return "EndBlock"+ChatColor.AQUA+"(Diamond)";
    		
			temple.coordBlockSet.remove(b);
			return null;
		}
		
		if(b.getTypeId() == Temple.goldBlock){
    		rb = b.getRelative(0, -1, 0);
    		if(rb.getTypeId() == Temple.ironBlock)
    			return "StartBlock"+ChatColor.GOLD+"(Gold)";
    		rb = b.getRelative(0, 1, 0);
    		if(rb.getTypeId() == Temple.diamondBlock)
    			return "EndBlock"+ChatColor.GOLD+"(Gold)";
    		temple.coordBlockSet.remove(b);
    		return null;
		}
		
		if(b.getTypeId() == Temple.ironBlock){
    		rb = b.getRelative(0, 1, 0);
    		if(rb.getTypeId() == Temple.goldBlock)
    			return "StartBlock"+ChatColor.GRAY+"(Iron)";
    		rb = b.getRelative(0, 1, 0);
    		if(rb.getTypeId() == Temple.diamondBlock)
    			return "PlayerSpawnBlock"+ChatColor.GRAY+"(Iron)";
    		return null;
		}
		
		return null;
	}
	
	private String getLocationMsg(Block b) {
		return "("+b.getX()+","+b.getY()+","+b.getZ()+")";
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
        String Line4 = sign.getLine(3);
        
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
    		if(!game.isRunning && game.temple.equals(temple) && game.getClass().toString().toLowerCase().equals(mode)){
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
            event.getBlockClicked().getFace(event.getBlockFace()).setTypeId(0);
            event.setCancelled(true);
            return;
        }
        
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Game game = tp.currentGame;

        Block liquid = event.getBlockClicked().getFace(event.getBlockFace());
        if(game.playerSet.contains(p))
        	game.tempBlockSet.add(liquid);
    }
}
