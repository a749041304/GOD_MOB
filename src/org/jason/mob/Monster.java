package org.jason.mob;

import java.util.List;

public class Monster {
	private String name;
	private int range;
	private int height;
	private String health;
	private String damage;
	private String exp;
	private String money;
	private List <String> drops;
	private List <String> skills;
	
	public Monster() {
	}
	
	public Monster (String name, int range, int height, String health, String damage, String exp, String money, List <String> drops, List <String> skills) {
		this.name = name;
		this.range = range;
		this.height = height;
		this.health = health;
		this.damage = damage;
		this.exp = exp;
		this.money = money;
		this.drops = drops;
		this.skills = skills;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRange() {
		return range;
	}
	public void setRange(int range) {
		this.range = range;
	}
	public String getHealth() {
		return health;
	}
	public void setHealth(String health) {
		this.health = health;
	}
	public String getDamage() {
		return damage;
	}
	public void setDamage(String damage) {
		this.damage = damage;
	}
	public String getExp() {
		return exp;
	}
	public void setExp(String exp) {
		this.exp = exp;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	public List<String> getDrops() {
		return drops;
	}
	public void setDrops(List<String> drops) {
		this.drops = drops;
	}
	public List<String> getSkills() {
		return skills;
	}
	public void setSkills(List<String> skills) {
		this.skills = skills;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
}
