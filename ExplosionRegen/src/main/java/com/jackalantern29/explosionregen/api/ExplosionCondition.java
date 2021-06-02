package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.api.enums.Condition;
import com.jackalantern29.explosionregen.api.enums.WeatherType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExplosionCondition {
    private final EnumMap<Condition, Object> conditionMap = new EnumMap<>(Condition.class);

    public boolean doMeetConditions(Object what) {
        int conditionsMet = 0;
        Location location = null;
        if(what instanceof Entity)
            location = ((Entity)what).getLocation();
        else if(what instanceof Block)
            location = ((Block)what).getLocation();
        for(Condition con : conditionMap.keySet()) {
            switch(con) {
                case CUSTOM_NAME:
                    if(what instanceof Entity) {
                        HashMap<String, Boolean> map = (HashMap<String, Boolean>) conditionMap.get(con);
                        for(Map.Entry<String, Boolean> entry : map.entrySet()) {
                            String name = entry.getKey();
                            String entityName = ((Entity)what).getCustomName();
                            if((name.equals(entityName)) == entry.getValue()) {
                                conditionsMet++;
                            }
                        }
                    }
                    break;
                case BLOCK:
                    if(what instanceof Block) {
                        HashMap<Material, Boolean> map = (HashMap<Material, Boolean>) conditionMap.get(con);
                        for(Map.Entry<Material, Boolean> entry : map.entrySet()) {
                            if((((Block)what).getType() == entry.getKey()) == entry.getValue()) {
                                conditionsMet++;
                            }
                        }
                    }
                    break;
                case ENTITY:
                    if(what instanceof Entity) {
                        HashMap<EntityType, Boolean> map = (HashMap<EntityType, Boolean>) conditionMap.get(con);
                        for(Map.Entry<EntityType, Boolean> entry : map.entrySet()) {
                            if((((Entity)what).getType() == entry.getKey()) == entry.getValue()) {
                                conditionsMet++;
                            }
                        }
                    }
                    break;
                case WEATHER:
                    if(location != null && location.getWorld().getEnvironment() == World.Environment.NORMAL) {
                        World world = location.getWorld();
                        HashMap<WeatherType, Boolean> map = (HashMap<WeatherType, Boolean>) conditionMap.get(con);
                        for(Map.Entry<WeatherType, Boolean> entry : map.entrySet()) {
                            if(!world.hasStorm()) {
                                if((!world.isThundering() && entry.getKey() == WeatherType.CLEAR) == entry.getValue()) {
                                    conditionsMet++;
                                }
                            } else {
                                if(!world.isThundering()) {
                                    if((entry.getKey() == WeatherType.RAIN) == entry.getValue()) {
                                        conditionsMet++;
                                    }
                                } else {
                                    if((entry.getKey() == WeatherType.THUNDER) == entry.getValue()) {
                                        conditionsMet++;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case WORLD:
                    if(location != null) {
                        HashMap<String, Boolean> map = (HashMap<String, Boolean>) conditionMap.get(con);
                        for(Map.Entry<String, Boolean> entry : map.entrySet()) {
                            if((location.getWorld().getName().equals(entry.getKey())) == entry.getValue()) {
                                conditionsMet++;
                            }
                        }
                    }
                    break;
                case IS_CHARGED:
                    if(what instanceof Creeper && (((Creeper)what).isPowered() == (boolean)conditionMap.get(con))) {
                        conditionsMet++;
                    }
                    break;
                case MINX:
                    if(location != null && (location.getX() >= (double)conditionMap.get(con))) {
                        conditionsMet++;
                    }
                    break;
                case MINY:
                    if(location != null && (location.getY() >= (double)conditionMap.get(con))) {
                        conditionsMet++;
                    }
                    break;
                case MINZ:
                    if(location != null && (location.getZ() >= (double)conditionMap.get(con))) {
                        conditionsMet++;
                    }
                    break;
                case MAXX:
                    if(location != null && (location.getX() <= (double)conditionMap.get(con))) {
                        conditionsMet++;
                    }
                    break;
                case MAXY:
                    if(location != null && (location.getY() <= (double)conditionMap.get(con))) {
                        conditionsMet++;
                    }
                    break;
                case MAXZ:
                    if(location != null && (location.getZ() <= (double)conditionMap.get(con))) {
                        conditionsMet++;
                    }
                    break;
            }
        }
        return conditionsMet == countConditions();
    }

    public int countConditions() {
        int size = 0;
        for(Object object : conditionMap.values()) {
            if(object instanceof HashMap)
                size = size + ((HashMap)object).size();
            else
                size++;
        }
        return size;
    }

    public <T> void setCondition(Condition condition, T value) {
        conditionMap.put(condition, value);
    }

    public <T> void setCondition(Condition condition, T value, boolean allow) {
        switch(condition) {
            case CUSTOM_NAME:
            case BLOCK:
            case ENTITY:
            case WEATHER:
            case WORLD:
                Map<T, Boolean> map;
                if(hasCondition(condition))
                    map = (HashMap<T, Boolean>) conditionMap.get(condition);
                else
                    map = new HashMap<>();
                map.put(value, allow);
                conditionMap.put(condition, map);
                break;
            case IS_CHARGED:
            case MINX:
            case MINY:
            case MINZ:
            case MAXX:
            case MAXY:
            case MAXZ:
                if(value == null)
                    conditionMap.put(condition, allow);
                else
                    conditionMap.put(condition, value);
                break;
        }
    }

    public void removeCondition(Condition condition) {
        conditionMap.remove(condition);
    }

    public <T> void removeCondition(Condition condition, T value) {
        switch(condition) {
            case CUSTOM_NAME:
            case BLOCK:
            case ENTITY:
            case WEATHER:
            case WORLD:
                if(value == null) {
                    conditionMap.remove(condition);
                } else {
                    HashMap<T, Boolean> map;
                    if(hasCondition(condition))
                        map = (HashMap<T, Boolean>) conditionMap.get(condition);
                    else
                        map = new HashMap<>();
                    map.remove(value);
                    conditionMap.put(condition, map);
                }
            case IS_CHARGED:
            case MINX:
            case MINY:
            case MINZ:
            case MAXX:
            case MAXY:
            case MAXZ:
                conditionMap.remove(condition);
        }
    }

    public boolean hasCondition(Condition condition) {
        return conditionMap.containsKey(condition);
    }

    public boolean hasConditionValue(Condition condition, Object key) {
        if(hasCondition(condition) && conditionMap.get(condition) instanceof HashMap) {
            return ((HashMap<?, ?>) conditionMap.get(condition)).containsKey(key);
        }
        return false;
    }

    public Set<Condition> getConditions() {
        return conditionMap.keySet();
    }

    public Object getConditionValues(Condition condition) {
        return conditionMap.get(condition);
    }

    public boolean getConditionValue(Condition condition, Object key) {
        if(hasCondition(condition) && conditionMap.get(condition) instanceof HashMap) {
            return ((HashMap<?, Boolean>)conditionMap.get(condition)).get(key);
        }
        return false;
    }
}
