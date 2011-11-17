package com.msingleton.templecraft.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.GameMode;
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

import com.msingleton.templecraft.TCPermissionHandler;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleCraft;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.games.*;
import com.msingleton.templecraft.scoreboards.ScoreBoard;

public class TCPlayerListener  extends PlayerListener{

	public TCPlayerListener(TempleCraft templeCraft) {		
	}
	
	 public void onPlayerInteract(PlayerInteractEvent event){    
		 
		 if(!TempleManager.isEnabled)
				return;
		 
        Player p = event.getPlayer();
        Action a = event.getAction();
        
        // Expands and contracts ScoreBoards
        if(p.isSneaking() && TCPermissionHandler.hasPermission(p, "templecraft.placesigns")){
        	// Expand
        	if(a.equals(Action.RIGHT_CLICK_BLOCK)){
        		Block b = event.getClickedBlock();
		        if(b.getState() instanceof Sign){
		    		Sign sign = (Sign) b.getState();
		    		ScoreBoard sb = TempleManager.SBManager.getScoreBoardBySign(sign);
		    		if(sb != null)
		    			sb.expandBoard();
		    	}
        	}
        	// Contract
        	if(a.equals(Action.LEFT_CLICK_BLOCK)){
        		Block b = event.getClickedBlock();
		        if(b.getState() instanceof Sign){
		    		Sign sign = (Sign) b.getState();
		    		ScoreBoard sb = TempleManager.SBManager.getScoreBoardBySign(sign);
		    		if(sb != null)
		    			sb.contractBoard();
		    	}
        	}
        	event.setCancelled(true);
        }
        
        if(p.getGameMode().equals(GameMode.CREATIVE))
        	return;
        
        TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        Game game = tp.currentGame;
			
        // Special Use of TNT for Arena Mode
        if(game != null && (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK))){
        	if(game instanceof Arena){
	        	if(p.getInventory().getItemInHand().getTypeId() == 46){
	        		if(a.equals(Action.RIGHT_CLICK_BLOCK))
	        			event.setCancelled(true);
		        	ItemStack item = p.getInventory().getItemInHand();
		        	if(item.getAmount() <= 1)
		        		p.getInventory().remove(item);
		        	else
		        		item.setAmount(item.getAmount()-1);
		        	((Arena)tp.currentGame).throwTNT(p);
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
		 }
        
        if (!TempleManager.playerSet.contains(p))
            return;
    	
        if(game == null)
        	return;
        
        // Start Block
        if (b.getTypeId() == 42 && game.lobbyLocSet.contains(b.getLocation())){
        	game.hitStartBlock(p);
        // End Block
        } else if (b.getTypeId() == 42 && game.rewardLocMap.containsKey(b.getLocation())){
            game.hitRewardBlock(p,game.rewardLocMap.remove(b.getLocation()));
            return;
        } else if (b.getTypeId() == 41 && game.endLocSet.contains(b.getLocation())){
            game.hitEndBlock(p);
            return;
        }
	}

	public void onPlayerMove(PlayerMoveEvent event){
		Player p = event.getPlayer();
		
		if(p.getGameMode().equals(GameMode.CREATIVE))
        	return;
		
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		Temple temple = tp.currentTemple;
		
		// For Entering Temple
		if(temple == null){
			if(event.getTo().distance(event.getFrom())>0.05){
				Set<Sign> signs = new HashSet<Sign>();
				Block b = p.getLocation().getBlock();
				for(int i = -3; i<=3;i++)
					for(int j = -3; j<0;j++)
						for(int k = -3; k<=3;k++){
							Block sign = b.getRelative(i,j,k);
							if(sign.getState() instanceof Sign)
								signs.add((Sign)sign.getState());
						}
				if(signs.isEmpty()){
					tp.sensedSign = null;
					tp.canAutoTele = true;
					tp.stopEnterTimer();
					return;
				}
				if(tp.canAutoTele){
					if(tp.sensedSign == null){
						for(Sign sign : signs){
							if(sign.getLine(0).equals("[TCS]") || sign.getLine(0).equals("[TempleCraftS]") || sign.getLine(3).equals("sensor")){
								tp.sensedSign = sign;
								TempleManager.tellPlayer(p, "You found the enterance to a temple!! Stand still to Enter!");
								tp.startEnterTimer(p);
							}
						}
					} else {
						tp.resetEnterTimer(p);
					}
				}
			}
			return;
		}
		
		if (!TCUtils.isTCWorld(event.getPlayer().getWorld()))
            return;
		
		if(!TempleManager.isEnabled || TempleManager.templeSet.isEmpty())
			return;
		
		Game game = tp.currentGame;
			
		if(game == null)
			return;
		
		game.onPlayerMove(event);
	}

	public static void handleSignClicked(Player p, Sign sign) {
		String Line1 = sign.getLine(0);
        String Line2 = sign.getLine(1);
        String Line3 = sign.getLine(2);
        //String Line4 = sign.getLine(3);
        
        if(TempleManager.playerSet.contains(p) || TCUtils.isTCEditWorld(p.getWorld()))
        	return;
        
    	if(!Line1.equals("[TempleCraft]") && !Line1.equals("[TC]") && !Line1.equals("[TempleCraftS]") && !Line1.equals("[TCS]"))
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
    	
    	if(temple.maxPlayersPerGame < 1 && temple.maxPlayersPerGame != -1){
			TempleManager.tellPlayer(p, "Temple \""+temple.templeName+"\" is unjoinable");
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
    		if(!game.isRunning && game.maxPlayers != game.playerSet.size() && game.gameName.contains(temple.templeName) && game.gameName.contains(mode.substring(0,3))){
    			game.playerJoin(p);
    			return;
    		}
    	}
    	
    	if(!TempleManager.gameSet.isEmpty())
    		TempleManager.tellPlayer(p, "All available games currently full or in progress.");
    	
    	TempleManager.tellPlayer(p, "Creating new game...");
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
		
		if(game instanceof Arena || game instanceof Spleef)
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
