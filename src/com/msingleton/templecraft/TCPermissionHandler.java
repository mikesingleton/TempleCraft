package com.msingleton.templecraft;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TCPermissionHandler {
	public static String[] allPermissions = {"tc","newgame","join","leave","ready","templelist","gamelist","playerlist","forcestart","forceend","checkupdates","newtemple","edittemple","deletetemple","savetemple","worldtotemple","addplayer","removeplayer"};
	public static ChatColor c1 = ChatColor.DARK_AQUA;
	public static ChatColor c2 = ChatColor.WHITE;
	public static String[] descriptions = {
		c1+"/tc <pageNumber>"+c2+"       - Help Menu",
		c1+"/tc newgame <temple> <mode>"+c2+" - Create a new game.",
		c1+"/tc join <game>"+c2+" - Join Game <game>.",
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
		c1+"/tc worldtotemple <temple>"+c2+" - Save current World as a temple.",
		c1+"/tc add <player>"+c2+"     - Allows a player to edit your temple.",
		c1+"/tc remove <player>"+c2+" - Disallows a player to edit your temple."
	};
	public static int entsPerPage = 7;
	public static int totalPages = (int)Math.ceil(allPermissions.length/(double)entsPerPage);
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
		if(page<0 || page>totalPages){
			p.sendMessage("Page "+page+" not found");
			return;
		}
		for(int i = 0;i<3;i++)
			p.sendMessage("");
		p.sendMessage(c1+"-----------"+c2+" TempleCraft Help ("+page+"/"+totalPages+") "+c1+"-----------");
		int start = (page-1)*entsPerPage;
		int end = page*entsPerPage;
		for(int i = start; i < end; i++)
			if(i >= allPermissions.length)
				p.sendMessage("");
			else if(hasPermission(p, allPermissions[i]))
				p.sendMessage(descriptions[i]);
	}
}
