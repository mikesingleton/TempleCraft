package com.msingleton.templecraft;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

import com.msingleton.templecraft.games.Game;

public class TCCommands implements CommandExecutor
{
    /**
     * Handles all command parsing.
     * Unrecognized commands return false, giving the sender a list of
     * valid commands (from plugin.yml).
     */
    public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args)
    {       
        // Only accept commands from players.
        if ((sender == null) || !(sender instanceof Player))
        {
            System.out.println("[TempleCraft] Only players can use these commands, silly.");
            return true;
        }
        
        // Cast the sender to a Player object.
        Player p = (Player) sender;
        
        /* If more than one argument, must be an advanced command.
         * Only allow operators to access these commands. */
        if (args.length > 1)
       		if(advancedCommands(p, args))
       			return true;
        
        // If not exactly one argument, must be an invalid command.
        if (args.length == 1)
        	if(basicCommands(p, args[0].toLowerCase()))
        		return true;
        	else
        		try{
        			TCPermissionHandler.sendResponse(p,Integer.parseInt(args[0]));
        			return true;
        		}catch(Exception e){};
        
        
        TCPermissionHandler.sendResponse(p,1);
        return true;
    }

	/**
     * Handles basic commands.
     */
	private boolean basicCommands(Player p, String cmd)
    {   
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
		
    	if((cmd.equals("join") || cmd.equals("j")) && TCPermissionHandler.hasPermission(p, "templecraft.join"))
    	{
    		for(Game game : TempleManager.gameSet){
    			if(!game.isRunning){
    				game.playerJoin(p);
    				return true;
    			}
    		}
    		TempleManager.tellPlayer(p, "All temples are currently running or disabled! Please try again later.");
    		return true;
    	}
    	
    	if ((cmd.equals("leave") || cmd.equals("l")) && TCPermissionHandler.hasPermission(p, "templecraft.leave"))
        {
        	TempleManager.playerLeave(p);
            return true;
        }
    	
    	if (cmd.equals("save"))
        {
    		Temple temple = tp.currentTemple;
    		
    		if(temple == null || !TCUtils.isTCEditWorld(p.getWorld())){
    			TempleManager.tellPlayer(p, "You are not editing a temple.");
    			return true;
    		}
    		
			if(TCPermissionHandler.hasPermission(p, "templecraft.savetemple")){
				if(TCUtils.isTCEditWorld(p.getWorld())){
					temple.saveTemple(p.getWorld(), p);
				}
			}
            return true;
        }
    	
        if ((cmd.equals("playerlist") || cmd.equals("plist")) && TCPermissionHandler.hasPermission(p, "templecraft.playerlist"))
        {
        	if(TempleManager.playerSet.contains(p))
        		tp.currentGame.playerList(p,true);
        	else
        		TempleManager.playerList(p);
            return true;
        }
        
        if ((cmd.equals("gamelist") || cmd.equals("glist")) && TCPermissionHandler.hasPermission(p, "templecraft.playerlist"))
        {
        	if(TempleManager.gameSet.isEmpty()){
        		TempleManager.tellPlayer(p,"No games are available.");
        		return true;
        	}
        	p.sendMessage(ChatColor.GREEN+"Game List:");
        	for(Game game : TempleManager.gameSet)
        		if(game != null){
        			if(game.isRunning)
        				p.sendMessage(game.gameName+": "+ChatColor.RED+"In Progress");
        			else
        				p.sendMessage(game.gameName+": "+ChatColor.GREEN+"In Lobby");
        		}
            return true;
        }
        
        if ((cmd.equals("templelist") || cmd.equals("tlist")) && TCPermissionHandler.hasPermission(p, "templecraft.templelist"))
        {
        	if(TempleManager.templeSet.isEmpty()){
        		TempleManager.tellPlayer(p,"No Temples are available.");
        		return true;
        	}
        	p.sendMessage(ChatColor.GREEN+"Temple List:");
        	ArrayList<String> list = new ArrayList<String>();
        	
        	Iterator<Temple> it = TempleManager.templeSet.iterator();
        	while(it.hasNext()){
        		Temple temple = it.next();
        		StringBuilder line = new StringBuilder();
        		if(temple != null){
        			line.append(ChatColor.WHITE+temple.templeName+": ");
        			if(temple.isSetup){
        				line.append(ChatColor.DARK_GREEN+"Setup");
        			} else {
        				line.append(ChatColor.DARK_RED+"Not Setup");
        			}
        			int startLeng = line.length();
        			while(line.length()<55-startLeng)
        				line.append(" ");
        		}
        		if(it.hasNext()){
        			temple = it.next();
        			if(temple != null){
            			line.append(ChatColor.WHITE+temple.templeName+": ");
            			if(temple.isSetup){
            				line.append(ChatColor.DARK_GREEN+"Setup");
            			} else {
            				line.append(ChatColor.DARK_RED+"Not Setup");
            			}
            		}
        		}
        		list.add(line.toString());
        	}
        	
        	for(String s : list)
        		p.sendMessage(s);
            return true;
        }
        
        if ((cmd.equals("ready") || cmd.equals("notready"))  && TCPermissionHandler.hasPermission(p, "templecraft.ready"))
        {
        	Game game = tp.currentGame;
        	if(game != null && game.playerSet.contains(p))
        		game.notReadyList(p);
        	else
        		TempleManager.notReadyList(p);
            return true;
        }
        
        if (cmd.equals("enable") && TCPermissionHandler.hasPermission(p, "templecraft.enable"))
        {            
            // Set the boolean
            TempleManager.isEnabled = Boolean.valueOf(!TempleManager.isEnabled);
            TempleManager.tellPlayer(p, "Enabled: " + TempleManager.isEnabled);
            return true;
        }
        
        if (cmd.equals("checkupdates") && TCPermissionHandler.hasPermission(p, "templecraft.checkupdates"))
        {            
            TCUtils.checkForUpdates(p, false);
            return true;
        }
        
        if (cmd.equals("converttemples") && TCPermissionHandler.hasPermission(p, "templecraft.converttemples"))
        {
        	for(Temple temple : TempleManager.templeSet){
        		TempleManager.tellPlayer(p, "Converting "+temple.templeName+"...");
        		TCUtils.convertTemple(p, temple);
        	}
			
			TempleManager.tellPlayer(p, "Temples Converted");
			return true;
        }
        
        return false;
    }
    
    private boolean advancedCommands(Player p, String[] args){    	
    	//Room commands
    	String cmd = args[0].toLowerCase();
    	String arg = args[1].toLowerCase();	
    	
    	TemplePlayer tp = TempleManager.templePlayerMap.get(p);
    	
        if (cmd.equals("new") && TCPermissionHandler.hasPermission(p, "templecraft.newtemple"))
        {
        	TempleManager.tellPlayer(p, "Attempting to create new Temple \""+args[0]+"\"...");
        	if(args.length == 2)
        		TCUtils.newTemple(p, arg, null, true);
        	else if(args.length == 3)
        		TCUtils.newTemple(p, arg, args[2].toLowerCase(), true);
    		return true;
        }
        
        if (cmd.equals("newgame") && TCPermissionHandler.hasPermission(p, "templecraft.newgame"))
        {
        	TCUtils.newGameCommand(p, args);
    		return true;
        }
        
        if (cmd.equals("delete") && TCPermissionHandler.hasPermission(p, "templecraft.deletetemple"))
        {
        	Temple temple = TCUtils.getTempleByName(arg);
    		
    		if(temple == null){
    			TempleManager.tellPlayer(p, "Temple \""+arg+"\" does not exist");
    			return true;
    		}
    		
        	TCUtils.removeTemple(temple);
        	TempleManager.tellPlayer(p, "Temple \""+arg+"\" deleted");
            return true;
        }
        
        if (cmd.equals("rename") && TCPermissionHandler.hasPermission(p, "templecraft.renametemple"))
        {
        	Temple temple;
        	String result;
        	if(args.length == 2){
        		temple = tp.currentTemple;
	    		result = arg;
	    		if(temple == null){
	    			TempleManager.tellPlayer(p, "You must be in a temple to use this command.");
	    			return true;
	    		}
        	} else if(args.length == 3){
	        	temple = TCUtils.getTempleByName(arg);
	    		result = args[2];
	    		if(temple == null){
	    			TempleManager.tellPlayer(p, "Temple \""+arg+"\" does not exist");
	    			return true;
	    		}
        	} else {
        		return true;
        	}
        	Temple newtemple = TCUtils.getTempleByName(result);
        	if(newtemple == null){
    			TCUtils.renameTemple(temple, arg);
    		} else {
    			TempleManager.tellPlayer(p, "Temple \""+result+"\" already exists");
    			return true;
    		}
        	TempleManager.tellPlayer(p, "Temple \""+temple.templeName+"\" renamed to \""+result+"\"");
            return true;
        }
        
        if (cmd.equals("setmaxplayers") && TCPermissionHandler.hasPermission(p, "templecraft.setmaxplayers"))
        {
        	Temple temple;
        	String number;
        	if(args.length == 2){
        		temple = tp.currentTemple;
	    		number = arg;
        	} else if(args.length == 3){
	        	temple = TCUtils.getTempleByName(arg);
	    		number = args[2];
        	} else {
        		return true;
        	}
        	if(temple == null){
    			TempleManager.tellPlayer(p, "Temple \""+arg+"\" does not exist");
    			return true;
    		}
        	try{
    			int value = Integer.parseInt(number);
    			TCUtils.setTempleMaxPlayers(temple, value);
            	TempleManager.tellPlayer(p, "Temple \""+temple.templeName+"\" maxPlayers set to "+value);
    		}catch(Exception e){
    			TempleManager.tellPlayer(p, "setTempleMaxPlayers got invalid variable for expected integer");
    		}
            return true;
        }
        
        if (cmd.equals("worldtotemple") && TCPermissionHandler.hasPermission(p, "templecraft.worldtotemple"))
        {
        	if(TCUtils.getTempleByName(arg) != null){
        		TempleManager.tellPlayer(p, "Temple \""+arg+"\" already exists.");
        		return true;
        	}
        	
        	if(args.length == 2)
        		TCUtils.newTemple(p, arg, null, false);
        	else if(args.length == 3)
        		TCUtils.newTemple(p, arg, args[2].toLowerCase(), false);
        	
        	Temple temple = TCUtils.getTempleByName(arg);
			TCRestore.saveTemple(p.getWorld(), temple);
			
			TempleManager.tellPlayer(p, "World Converted to Temple \""+arg+"\"");
			return true;
        }
        
        if (cmd.equals("edit") && TCPermissionHandler.hasPermission(p, "templecraft.edittemple"))
        {        	
        	Temple temple = TCUtils.getTempleByName(arg);
    		
    		if(temple == null){
    			TempleManager.tellPlayer(p, "Temple \""+arg+"\" does not exist");
    			return true;
    		}
        	
    		TempleManager.tellPlayer(p, "Preparing "+temple.templeName+"...");
    		TCUtils.editTemple(p, temple);
            return true;
        }
        
        if (cmd.equals("add") && TCPermissionHandler.hasPermission(p, "templecraft.addplayer"))
        {        	
        	Temple temple = tp.currentTemple;
    		
    		if(temple == null || !temple.editorSet.contains(p)){
    			TempleManager.tellPlayer(p, "You need to be editing a temple to use this command.");
    			return true;
    		}
        	
    		if(!temple.ownerSet.contains(p.getName()) && !TCPermissionHandler.hasPermission(p, "templecraft.editall")){
    			TempleManager.tellPlayer(p, "Only the owner of the temple can use this command.");
    			return true;
    		}
    		
    		String playerName = null;
    		for(Player player : TempleManager.server.getOnlinePlayers()){
    			if(player.getName().toLowerCase().startsWith(arg)){
    				playerName = player.getName();
    				break;
    			}
    		}
    		
    		if(playerName == null){
    			TempleManager.tellPlayer(p, "Player not found.");
    		} else {
    			if(temple.addEditor(playerName))
    				TempleManager.tellPlayer(p, "Added \""+playerName+"\" to \""+temple.templeName+"\".");
    			else
    				TempleManager.tellPlayer(p, "\""+playerName+"\" already has access to this temple.");
    		}
    		return true;
        }
        
        if (cmd.equals("remove") && TCPermissionHandler.hasPermission(p, "templecraft.removeplayer"))
        {        	
        	Temple temple = tp.currentTemple;
    		
    		if(temple == null || !temple.editorSet.contains(p)){
    			TempleManager.tellPlayer(p, "You need to be editing a temple to use this command.");
    			return true;
    		}
        	
    		if(!temple.ownerSet.contains(p.getName()) && !TCPermissionHandler.hasPermission(p, "templecraft.editall")){
    			TempleManager.tellPlayer(p, "Only the owner of the temple can use this command.");
    			return true;
    		}
    		
    		String playerName = null;
    		for(Player player : TempleManager.server.getOnlinePlayers()){
    			if(player.getName().toLowerCase().startsWith(arg)){
    				playerName = player.getName();
    				break;
    			}
    		}
    		
    		if(playerName == null){
    			TempleManager.tellPlayer(p, "Player not found.");
    		} else {
    			if(temple.removeEditor(playerName))
    				TempleManager.tellPlayer(p, "Removed \""+playerName+"\" from \""+temple.templeName+"\".");
    			else
    				TempleManager.tellPlayer(p, "\""+playerName+"\" does not have access \""+temple.templeName+"\".");
    		}
            return true;
        }
        
        if (cmd.equals("findsigblocks") && TCPermissionHandler.hasPermission(p, "templecraft.findsigblocks"))
        {
        	try{
        		int radius = Integer.parseInt(arg);
        		Temple temple = tp.currentTemple;
        		
        		if(temple == null){
        			TempleManager.tellPlayer(p, "You must be in a temple to use this command");
        			return true;
        		}
        		
        		TCUtils.getSignificantBlocks(p, radius);
        		return true;
        	} catch(Exception e){
        		TempleManager.tellPlayer(p, "Invalid argument for expected integer.");
        		return true;
        	}
        }
        
        if(!(cmd.equals("join") || cmd.equals("j") || cmd.equals("forcestart") || cmd.equals("forceend")))
        	return false;
        		
        //Game commands
        String gamename = args[1].toLowerCase();
        Game game = TCUtils.getGameByName(gamename);
        
        if(game == null){
        	TempleManager.tellPlayer(p, "There is no game with name " + gamename);
        	return true;
        }
        
        if ((cmd.equals("join") || cmd.equals("j")) && TCPermissionHandler.hasPermission(p, "templecraft.join"))
        {
            game.playerJoin(p);
            return true;
        }
        
        // tc forcestart <game>
        if (cmd.equals("forcestart") && TCPermissionHandler.hasPermission(p, "templecraft.forcestart"))
        {
            game.forceStart(p);
            return true;
        }        
        
        if (cmd.equals("forceend") && TCPermissionHandler.hasPermission(p, "templecraft.forceend"))
        {
            game.forceEnd(p);
            return true;
        }
        
    	return false;
    }
}