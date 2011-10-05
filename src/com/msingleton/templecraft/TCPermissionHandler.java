package com.msingleton.templecraft;

import org.bukkit.entity.Player;

public class TCPermissionHandler {
	public static String[] allPermissions = {"newgame","join","leave","ready","templelist","gamelist","playerlist","forcestart","forceend","checkupdates","newtemple","edittemple","deletetemple","savetemple","worldtotemple","addplayer","removeplayer"};
	public static String[] descriptions = {
		"/tc newgame <temple> <mode> - Create a new game.",
		"/tc join <game> - Join Game <game>.",
        "/tc leave          - Leave current Temple.",
        "/tc ready         - List of players who aren't ready.",
        "/tc tlist            - Lists Temples.",
        "/tc glist            - Lists Games.",
        "/tc plist            - Lists players in Games.",
        "/tc forcestart <temple> - Manually start a Game.",
        "/tc forceend <temple>   - Manually end a Game.",
        "/tc checkupdates         - Checks for updates.",
		"/tc new <temple>         - Creates a new Temple.",
        "/tc edit <temple>     - Edit an existing temple.",
        "/tc delete <temple>  - Delete an existing temple.",
        "/tc save               - Save the current temple.",
        "/tc worldtotemple <temple> - Save the current World as a temple.",
        "/tc add <player>     - Allows a player to edit your temple.",
        "/tc remove <player> - Disallows a player to edit your temple."
	};
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

	public static void sendResponse(Player p) {
		for(int i = 0; i < allPermissions.length; i++)
			if(hasPermission(p, allPermissions[i]))
				p.sendMessage(descriptions[i]);
	}
}
