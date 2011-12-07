package com.msingleton.templecraft.games;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.msingleton.templecraft.TCUtils;
import com.msingleton.templecraft.Temple;
import com.msingleton.templecraft.TempleManager;
import com.msingleton.templecraft.TemplePlayer;
import com.msingleton.templecraft.util.MobArenaClasses;
import com.msingleton.templecraft.util.Translation;

public class PVP extends Game{
    public Set<Player> aliveSet = new HashSet<Player>();
    public Player winner;
    public int[] scores = new int[16];
    public int scoreToWin;
	
	public PVP(String name, Temple temple, World world) {
		super(name, temple, world);
		world.setPVP(true);
		scoreToWin = 10;
	}
	
	public void playerJoin(Player p){	
		super.playerJoin(p);
		MobArenaClasses.clearInventory(p);
	}
	
	public void startGame(){
		super.startGame();
	}
	
	public void endGame(String winner){
		tellAll(Translation.tr("pvp.win",winner));
		super.endGame();
	}
	
	public void convertLobby(){
		super.convertLobby();
		for(int i : lobbyLocMap.values())
			scores[i] = 0;
	}
	
	public void playerDeath(Player p)
	{
		p.teleport(lobbyLoc);
		super.playerDeath(p);
		aliveSet.remove(p);
		if(aliveSet.size() == 1){
			winner = (Player)aliveSet.toArray()[0];
			endGame();
		} else if(aliveSet.isEmpty()){
			winner = p;
			endGame();
		}
	}
	
	public void onEntityKilledByEntity(LivingEntity killed, Entity killer) {
		super.onEntityKilledByEntity(killed, killer);
		if(killer instanceof Player && killed instanceof Player){
			TCUtils.sendDeathMessage(this, killed, killer);
			
			TemplePlayer tp = TempleManager.templePlayerMap.get((Player)killer);
			if(tp.team != -1){
				scores[tp.team]++;
				if(scores[tp.team] == scoreToWin)
					endGame(TCUtils.getWoolColor(tp.team));
				else
					tellAll(TCUtils.getWoolColor(tp.team)+": "+ChatColor.WHITE+scores[tp.team]+"/"+scoreToWin);
			} else {
				if(tp.roundPlayersKilled == scoreToWin)
					endGame(ChatColor.DARK_RED+((Player)killer).getDisplayName());
				else
					tellAll(ChatColor.DARK_RED+((Player)killer).getDisplayName()+": "+ChatColor.WHITE+tp.roundPlayersKilled+"/"+scoreToWin);
			}
		}
	}
}
