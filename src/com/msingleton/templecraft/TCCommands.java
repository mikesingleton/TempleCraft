package com.msingleton.templecraft;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;

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
        
        TCPermissionHandler.sendResponse(p);
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
    		for(Temple temple : TempleManager.templeSet){
    			if(!temple.isRunning && temple.trySetup()){
    				temple.playerJoin(p);
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
    		
    		if(temple == null || !p.getWorld().getName().contains("EditWorld_")){
    			TempleManager.tellPlayer(p, "You are not editing a temple.");
    			return true;
    		}
    		
			if(TCPermissionHandler.hasPermission(p, "templecraft.savetemple")){
				if(p.getWorld().getName().contains("EditWorld_")){
					temple.saveTemple(p.getWorld(), p);
				}
			}
            return true;
        }
    	
    	if (cmd.equals("reload") && TCPermissionHandler.hasPermission(p, "templecraft.reload"))
        {
    		TempleManager.tellPlayer(p, "Clearing TempleWorld...");
    		for(Player tempp : TempleManager.world.getPlayers()){
    			TemplePlayer temptp = TempleManager.templePlayerMap.get(tempp);
    			if(temptp.currentTemple != null){
    				TempleManager.tellPlayer(p, "TempleWorld is currently in use.");
    				p.sendMessage("Please wait or use \"/tc forceend <templename>\" to end the temples.");
    				return true;
    			}
    		}
    		TempleManager.reloadTemples();
    		TempleManager.tellPlayer(p, "Done :)");
            return true;
        }
    	
        if ((cmd.equals("playerlist") || cmd.equals("plist")) && TCPermissionHandler.hasPermission(p, "templecraft.playerlist"))
        {
        	if(TempleManager.playerSet.contains(p))
        		tp.currentTemple.playerList(p,true);
        	else
        		TempleManager.playerList(p);
            return true;
        }
        
        if ((cmd.equals("templelist") || cmd.equals("tlist")) && TCPermissionHandler.hasPermission(p, "templecraft.templelist"))
        {
        	p.sendMessage(ChatColor.GREEN+"Temple List:");
        	for(Temple temple : TempleManager.templeSet)
        		if(temple != null)
        			p.sendMessage(temple.templeName);
            return true;
        }
        
        if ((cmd.equals("ready") || cmd.equals("notready"))  && TCPermissionHandler.hasPermission(p, "templecraft.ready"))
        {
        	Temple temple = tp.currentTemple;
        	if(temple != null && temple.playerSet.contains(p))
        		temple.notReadyList(p);
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
        
        return false;
    }
    
    private boolean advancedCommands(Player p, String[] args){    	
    	//Room commands
    	String cmd = args[0].toLowerCase();
    	String arg = args[1].toLowerCase();	
    	
    	TemplePlayer tp = TempleManager.templePlayerMap.get(p);
    	
        if (cmd.equals("new") && TCPermissionHandler.hasPermission(p, "templecraft.newtemple"))
        {        	
    		TCUtils.newTemple(p, arg);
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
        
        //Temple commands
        String templename = args[1].toLowerCase();
        Temple temple = TCUtils.getTempleByName(templename);
        
        if(temple == null){
        	TempleManager.tellPlayer(p, "There is no temple with name " + templename);
        	return true;
        }
        
        if ((cmd.equals("join") || cmd.equals("j")) && TCPermissionHandler.hasPermission(p, "templecraft.join"))
        {
            temple.playerJoin(p);
            return true;
        }
        
        // tc forcestart <templeName>
        if (cmd.equals("forcestart") && TCPermissionHandler.hasPermission(p, "templecraft.forcestart"))
        {
            temple.forceStart(p);
            return true;
        }        
        
        if (cmd.equals("forceend") && TCPermissionHandler.hasPermission(p, "templecraft.forceend"))
        {
            temple.forceEnd(p);
            return true;
        }
        
    	return false;
    }
}