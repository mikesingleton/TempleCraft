package com.msingleton.templecraft.scoreboards;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;

public class RecentScoreBoard extends ScoreBoard{
	
	public RecentScoreBoard(String id, Location p1, Location p2, String templeName, String gameMode){
		super(id,p1,p2,templeName,gameMode);
		type = "recent";
		save();
	}
	
	public void updateScores(List<String> strings){
		for(int i = 0; i<signs.size();i++){
			if(signs.get(i).getY() == p1.getY()){
				shiftDown(signs.get(i),1);
				if(strings.size() > i)
					signs.get(i).setLine(1, strings.get(i));
			}
		}
	}

	private void shiftDown(Sign sign, int start) {
		Block b = sign.getBlock();
		if(b.getRelative(0, -1, 0).getState() instanceof Sign){
			Sign newSign = (Sign)b.getRelative(0, -1, 0).getState();
			if(inRegion(b.getRelative(0, -1, 0).getLocation())){
				shiftDown(newSign,0);
				newSign.setLine(0, sign.getLine(3));
			}
		}
		for(int i = 3;i > start;i--)
			sign.setLine(i, sign.getLine(i-1));
	}
}
