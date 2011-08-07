package com.msingleton.templecraft;

public class LevelFormula {
	public static void main(String[] args){
		int xp = 0;
		for(int i = 1; i<=100; i++){
			xp += 103+47*i;
			System.out.println("Level "+i+":"+(i*103+47*(i*(i+1))/2));
		}
	}
}
