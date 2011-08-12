package com.ryanspeets.bukkit.flatlands;

import java.util.Random;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import com.msingleton.templecraft.TempleManager;

public class TempleWorldGenerator extends ChunkGenerator
{
  public byte[] generate(World arg0, Random arg1, int arg2, int arg3)
  {
	int[] levels = TempleManager.landLevels;
	byte[] mats = TempleManager.landMats;
    byte[] result = new byte[32768];
    int level = 0;
    for (int y = 0; y < 128; y++){
	   for (int x = 0; x < 16; x++) {
	      for (int z = 0; z < 16; z++) {
        	byte out;
        	
        	if(level+1 >= levels.length){
        		out = 0;
        	} else {
        		if(y > levels[level])
        			level++;
        		
        		out = mats[level];
        	}
        	
	        result[((x * 16 + z) * 128 + y)] = out;
        }
      }
    }
    return result;
  }

  public boolean canSpawn(World world, int x, int z)
  {
    return true;
  }
}