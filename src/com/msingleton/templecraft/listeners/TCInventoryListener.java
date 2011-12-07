package com.msingleton.templecraft.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventorySlotType;
import org.getspout.spoutapi.event.inventory.InventoryListener;

import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.games.Game;
import com.msingleton.templecraft.util.Translation;

public class TCInventoryListener extends InventoryListener {
	public void onInventoryClick(InventoryClickEvent event){
		Player p = event.getPlayer();
		TemplePlayer tp = TempleManager.templePlayerMap.get(p);
        Game game = tp.currentGame;
        
        if(game == null)
        	return;
		
        if (event.getSlotType().equals(InventorySlotType.HELMET) && event.getItem().getType().equals(Material.WOOL)) {
        	TempleManager.tellPlayer(p, Translation.tr("playerListener.denyHelmet"));
        	event.setCancelled(true);
        }
	 }
}
