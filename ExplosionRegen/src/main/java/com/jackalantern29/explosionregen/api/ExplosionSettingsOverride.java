package com.jackalantern29.explosionregen.api;

import java.util.EnumMap;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;

import com.jackalantern29.explosionregen.api.enums.WeatherType;
import com.jackalantern29.explosionregen.api.enums.ExplosionCondition;
import org.bukkit.entity.EntityType;

public class ExplosionSettingsOverride {

	private final String name;
	//private Object source;
	private final EnumMap<ExplosionCondition, Object> conditions = new EnumMap<>(ExplosionCondition.class);
	private ExplosionSettings settings;
	public ExplosionSettingsOverride(String name, ExplosionSettings settings) {
		this.name = name.toLowerCase();
		this.settings = settings;
	}
	
	
	public boolean doMeetConditions(Object what) {
		int conditionsMet = 0;

		for(ExplosionCondition con : conditions.keySet()) {
			Location location = null;
			if(what instanceof Entity)
				location = ((Entity)what).getLocation();
			else if(what instanceof Block)
				location = ((Block)what).getLocation();
			switch(con) {
			case CUSTOM_NAME:
				if(what instanceof Entity)
					if(((Entity)what).getCustomName().equals(conditions.get(con)))
						conditionsMet++;
				break;
			case ENTITY:
				if(what instanceof Entity)
					if(((Entity)what).getType() == conditions.get(con))
						conditionsMet++;
				break;
			case BLOCK:
				if(what instanceof Block)
					if(((Block)what).getType() == conditions.get(con))
						conditionsMet++;
				break;
			case IS_CHARGED:
				if(what instanceof Entity)
					if((what) instanceof Creeper) {
						if(((Creeper)what).isPowered() == (boolean)conditions.get(con))
							conditionsMet++;
				}
				break;
			case WEATHER:
				if(location != null) {	
					World world = location.getWorld();
					if(!world.hasStorm() && !world.isThundering() && ((conditions.get(con)) == WeatherType.CLEAR))
						conditionsMet++;
					else if(world.hasStorm() && !world.isThundering() && ((conditions.get(con)) == WeatherType.RAIN))
						conditionsMet++;
					else if(world.hasStorm() && world.isThundering() && ((conditions.get(con)) == WeatherType.THUNDER))
						conditionsMet++;
				}
				break;
			case WORLD:
				if(location != null)
					if(location.getWorld().equals(conditions.get(con)))
						conditionsMet++;
				break;
			case MINX:
				if(location != null)
					if(location.getX() >= (double)conditions.get(con))
						conditionsMet++;
				break;
			case MAXX:
				if(location != null)
					if(location.getX() <= (double)conditions.get(con))
						conditionsMet++;
				break;
			case MINY:
				if(location != null)
					if(location.getY() >= (double)conditions.get(con))
						conditionsMet++;
				break;
			case MAXY:
				if(location != null)
					if(location.getY() <= (double)conditions.get(con))
						conditionsMet++;
				break;
			case MINZ:
				if(location != null)
					if(location.getZ() >= (double)conditions.get(con))
						conditionsMet++;
				break;
			case MAXZ:
				if(location != null)
					if(location.getZ() <= (double)conditions.get(con))
						conditionsMet++;
				break;
			}
		}
		return conditionsMet == countConditions();
	}
	
	public int countConditions() {
		return conditions.size();
	}
	
	public void setCondition(ExplosionCondition condition, Object value) {
		conditions.put(condition, value);
	}
	public void removeCondition(ExplosionCondition condition) {
		conditions.remove(condition);
	}
	public boolean hasCondition(ExplosionCondition condition) {
		return conditions.containsKey(condition);
	}
	public Set<ExplosionCondition> getConditions() {
		return conditions.keySet();
	}
	public Object getConditionValue(ExplosionCondition condition) {
		return conditions.get(condition);
	}

	public Object getSimpleConditionValue(ExplosionCondition condition) {
		Object value = getConditionValue(condition);
		if(value instanceof EntityType)
			return ((EntityType)value).name().toLowerCase();
		else if(value instanceof Material)
			return ((Material)value).name().toLowerCase();
		else if(value instanceof WeatherType)
			return ((WeatherType)value).name().toLowerCase();
		else if(value instanceof World)
			return ((World)value).getName();
		else
			return value;
	}
	public String getName() {
		return name;
	}
	public ExplosionSettings getExplosionSettings() {
		return settings;
	}
	public void setExplosionSettings(ExplosionSettings settings) {
		this.settings = settings;
	}
	
}
