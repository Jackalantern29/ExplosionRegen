package com.jackalantern29.explosionregen.api.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

public enum UpdateType {
	BOUNTIFUL_UPDATE(1),
	COMBAT_UPDATE(2),
	FROSTBURN_UPDATE(3),
	EXPLORATION_UPDATE(4),
	COLOR_UPDATE(5),
	AQUATIC_UPDATE(6),
	PILLAGE_UPDATE(7),
	BEES_UPDATE(8),
	NETHER_UPDATE(9),
	UNKNOWN_UPDATE(99);
	
	private final int id;
	UpdateType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public static UpdateType getCurrentUpdate() {
		switch(getNMSVersion()) {
		case "v1_8_R1":
		case "v1_8_R2":
		case "v1_8_R3":
			return BOUNTIFUL_UPDATE;
		case "v1_9_R1":
		case "v1_9_R2":
			return COMBAT_UPDATE;
		case "v1_10_R1":
			return FROSTBURN_UPDATE;
		case "v1_11_R1":
			return EXPLORATION_UPDATE;
		case "v1_12_R1":
			return COLOR_UPDATE;
		case "v1_13_R1":
		case "v1_13_R2":
			return AQUATIC_UPDATE;
		case "v1_14_R1":
			return PILLAGE_UPDATE;
		case "v1_15_R1":
			return BEES_UPDATE;
		case "v1_16_R1":
			return NETHER_UPDATE;
		default:
			return UNKNOWN_UPDATE;
		}		
	}
	public static boolean isUpdate(UpdateType update) {
		return update.equals(getCurrentUpdate());
	}
	public static boolean isPostUpdate(UpdateType update) {
		return getCurrentUpdate().getId() >= update.getId();
	}
	public static boolean isPreUpdate(UpdateType update) {
		return getCurrentUpdate().getId() <= update.getId();
	}
	public static String getNMSVersion() {
		Matcher matcher = Pattern.compile("v\\d+_\\d+_R\\d+").matcher(Bukkit.getServer().getClass().getPackage().getName());
		if (matcher.find()) {
			return matcher.group();
		}
		return null;
	}
}
