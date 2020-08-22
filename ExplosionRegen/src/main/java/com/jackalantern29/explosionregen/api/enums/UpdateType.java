package com.jackalantern29.explosionregen.api.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;

public enum UpdateType {
	PAST_UPDATE(-1),
	BOUNTIFUL_UPDATE(8),
	COMBAT_UPDATE(9),
	FROSTBURN_UPDATE(10),
	EXPLORATION_UPDATE(11),
	COLOR_UPDATE(12),
	AQUATIC_UPDATE(13),
	PILLAGE_UPDATE(14),
	BEES_UPDATE(15),
	NETHER_UPDATE(16),
	FUTURE_UPDATE(0);
	
	private final int id;
	UpdateType(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	public static UpdateType getCurrentUpdate() {
		int version = Integer.parseInt(getNMSVersion().substring(3, getNMSVersion().length()-3));
		switch(version) {
		case 8:
			return BOUNTIFUL_UPDATE;
		case 9:
			return COMBAT_UPDATE;
		case 10:
			return FROSTBURN_UPDATE;
		case 11:
			return EXPLORATION_UPDATE;
		case 12:
			return COLOR_UPDATE;
		case 13:
			return AQUATIC_UPDATE;
		case 14:
			return PILLAGE_UPDATE;
		case 15:
			return BEES_UPDATE;
		case 16:
			return NETHER_UPDATE;
		default:
			if(version < 8)
				return PAST_UPDATE;
			else if(version > UpdateType.values()[UpdateType.values().length-2].getId());
				return FUTURE_UPDATE;
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
