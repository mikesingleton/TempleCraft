package com.msingleton.templecraft;

import org.bukkit.entity.Player;

public class TCPermissionHandler {
	public static String[] allPermissions = {"join","leave","ready","templelist","playerlist","forcestart","forceend","save","nullclass","checkupdates","newtemple","edittemple","deletetemple","savetemple","addplayer","removeplayer","reload"};
	public static String[] descriptions = {
		"/tc join <temple> - Join Temple <temple>.",
        "/tc leave          - Leave current Temple.",
        "/tc ready         - List of players who aren't ready.",
        "/tc tlist            - List of Temples.",
        "/tc plist            - List of players in a Temple.",
        "/tc forcestart <temple> - Manually start a Temple.",
        "/tc forceend <temple>   - Manually end a Temple.",
        "/tc save                    - Save class info.",
        "/tc nullclass               - Gives the user a null class.",
        "/tc checkupdates         - Checks for updates",
		"/tc new                - Creates a new Temple.",
        "/tc edit <temple>     - Edit an existing temple.",
        "/tc delete <temple>  - Delete an existing temple.",
        "/tc save               - Save the current temple.",
        "/tc add <player>     - Allows a player to edit your temple.",
        "/tc remove <player> - Disallows a player to edit your temple.",
        "/tc reload              - Reloads TempleWorld Temples."
	};
	public static String[] editBasics = {"newtemple","edittemple","savetemple","addplayer","removeplayer","templelist","playerlist"};
	
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
