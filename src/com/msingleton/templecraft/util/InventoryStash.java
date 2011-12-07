package com.msingleton.templecraft.util;

import org.bukkit.GameMode;
import org.bukkit.inventory.ItemStack;

/**
 * NOTE: I DID NOT WRITE THIS CLASS (notice the author below)
 * @author tommytony
 */
public class InventoryStash {
	private ItemStack[] contents;
	private ItemStack helmet;
	private ItemStack chest;
	private ItemStack legs;
	private ItemStack feet;
	private double health;
	private int foodLevel;
	private int experience;
	private GameMode gameMode;

	public InventoryStash(ItemStack[] contents) {
		this.setContents(contents);
	}
	
	public InventoryStash(ItemStack[] contents, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack feet, double health, int foodLevel, int experience, GameMode gameMode) {
		this.setContents(contents);
		this.setHelmet(helmet);
		this.setChest(chest);
		this.setLegs(legs);
		this.setFeet(feet);
		this.setHealth(health);
		this.setFoodLevel(foodLevel);
		this.setExperience(experience);
		this.setGameMode(gameMode);
	}

	public void setContents(ItemStack[] contents) {
		this.contents = contents;
	}

	public ItemStack[] getContents() {
		return contents;
	}

	public void setHelmet(ItemStack helmet) {
		this.helmet = helmet;
	}

	public ItemStack getHelmet() {
		return helmet;
	}

	public void setChest(ItemStack chest) {
		this.chest = chest;
	}

	public ItemStack getChest() {
		return chest;
	}

	public void setLegs(ItemStack legs) {
		this.legs = legs;
	}

	public ItemStack getLegs() {
		return legs;
	}

	public void setFeet(ItemStack feet) {
		this.feet = feet;
	}

	public ItemStack getFeet() {
		return feet;
	}
	
	public void setHealth(double health) {
		this.health = health;
	}

	public double getHealth() {
		return health;
	}
	
	public void setFoodLevel(int foodLevel) {
		this.foodLevel = foodLevel;
	}

	public int getFoodLevel() {
		return foodLevel;
	}
	
	public void setExperience(int experience) {
		this.experience = experience;
	}

	public int getExperience() {
		return experience;
	}
	
	public void setGameMode(GameMode gameMode) {
		this.gameMode = gameMode;
	}

	public GameMode getGameMode() {
		return gameMode;
	}
}
