package com.msingleton.templecraft;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TCPermissionHandler {
	public static String[] allPermissions = {"tc","newgame","join","leave","ready","templelist","gamelist","playerlist","forcestart","forceend","checkupdates","newtemple","edittemple","deletetemple","savetemple","renametemple","worldtotemple","addplayer","removeplayer","setmaxplayers","findsigblocks"};
	public static ChatColor c1 = TempleCraft.c1;
	public static ChatColor c2 = TempleCraft.c2;
	public static String[] descriptions = {
		c1+"/tc (pageNumber)"+c2+"       - Help Menu",
		c1+"/tc newgame <temple> <mode>"+c2+" - Create a new game.",
		c1+"/tc join (game)"+c2+" - Join Game <game>.",
		c1+"/tc leave"+c2+"          - Leave current Temple.",
		c1+"/tc ready"+c2+"         - List of players who aren't ready.",
		c1+"/tc tlist"+c2+"            - Lists Temples.",
		c1+"/tc glist"+c2+"            - Lists Games.",
		c1+"/tc plist"+c2+"            - Lists players in Games.",
		c1+"/tc forcestart <game>"+c2+" - Manually start a Game.",
		c1+"/tc forceend <game>"+c2+"   - Manually end a Game.",
		c1+"/tc checkupdates"+c2+"         - Checks for updates.",
		c1+"/tc new <temple>"+c2+"         - Creates a new Temple.",
		c1+"/tc edit <temple>"+c2+"     - Edit an existing temple.",
		c1+"/tc delete <temple>"+c2+"  - Delete an existing temple.",
		c1+"/tc save"+c2+"               - Save the current temple.",
		c1+"/tc rename <newName>"+c2+" - Renames the temple to <newName>.",
		c1+"/tc worldtotemple <temple>"+c2+" - Save current World as a temple.",
		c1+"/tc add <player>"+c2+"     - Allows a player to edit your temple.",
		c1+"/tc remove <player>"+c2+" - Disallows a player to edit your temple.",
		c1+"/tc setmaxplayers <integer>"+c2+" - Sets max players per game.",
		c1+"/tc findsigblocks <radius>"+c2+" - Finds significant blocks."
	};
	public static int entsPerPage = 7;
	public static String[] editBasics = {"newtemple","edittemple","savetemple","addplayer","removeplayer","templelist","gamelist","playerlist"};
	
	public static boolean hasPermission(Player p, String s){
		if(!s.contains("templecraft."))
			s = "templecraft."+s;
		if(TempleCraft.permissionHandler != null){
			for(String command : editBasics)
				if(s.contains(command))
					if(TempleCraft.permissionHandler.has(p, "templecraft.editbasics"))
						return true;
			return TempleCraft.permissionHandler.has(p, s);
		} else {
			return p.hasPermission(s);
		}
	}

	public static void sendResponse(Player p, int page) {
		List<String> personalPerms = getPerms(p);
		int totalPages = (int)Math.ceil(personalPerms.size()/(double)entsPerPage);
		if(page<0 || page>totalPages){
			p.sendMessage("Page "+page+" not found");
			return;
		}
		for(int i = 0;i<3;i++)
			p.sendMessage("");
		p.sendMessage(c1+"-----------"+c2+" TempleCraft Help ("+page+"/"+totalPages+") "+c1+"-----------");
		int start = (page-1)*entsPerPage;
		int end = page*entsPerPage;
		for(int i = start; i < end; i++){
			if(personalPerms.size() > i)
				p.sendMessage(personalPerms.get(i));
			else
				p.sendMessage("");
		}
	}

	private static List<String> getPerms(Player p) {
		List<String> result = new ArrayList<String>(); 
		for(int i = 0; i < allPermissions.length;i++)
			if(hasPermission(p,allPermissions[i]))
				result.add(descriptions[i]);
		return result;
	}
}
