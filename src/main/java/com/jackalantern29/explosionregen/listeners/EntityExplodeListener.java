package com.jackalantern29.explosionregen.listeners;

import java.util.ArrayList;
import java.util.List;

import com.jackalantern29.explosionregen.api.enums.DamageCategory;
import com.jackalantern29.explosionregen.api.events.ExplosionDamageEntityEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.ExplosionSettingsOverride;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.events.ExplosionTriggerEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class EntityExplodeListener implements Listener {
	private BlockState clickedBlock = null;
	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if(!event.isCancelled()) {
			if(entity != null)
				explode(event, entity, event.getLocation(), event.blockList());
			else
				explode(event, event.getLocation().getBlock().getState(), event.getLocation(), event.blockList());
		}
		//explode(event, explosionType, explosionType.toString().toLowerCase(), event.getLocation(), event.getEntity(), event.blockList());
	}
	public class BlockExplodeListener implements Listener {
		@EventHandler
		public void onExplode(BlockExplodeEvent event) {
			if(!event.isCancelled()) {
				explode(event, event.getBlock().getState(), event.getBlock().getLocation(), event.blockList());
			}
		}
	}
	private ExplosionSettings calculateOverrides(ExplosionSettings settings, Object source) {
		ExplosionSettings newSettings = settings;

		if(source instanceof Entity) {
			int conditions = 0;
			for(ExplosionSettingsOverride override : new ArrayList<>(settings.getOverrides())) {
				if(override.doMeetConditions(source)) {
					if(override.countConditions() > conditions) {
						conditions = override.countConditions();
						newSettings = override.getExplosionSettings();
					}
				}
			}
		}
		return newSettings;
	}

	private void explode(Event event, Object what, Location location, List<Block> blockList) {
		ExplosionSettings settings = ExplosionSettings.getSettings("default");
		if(settings.getConditions() != null) {
			if(!settings.getConditions().doMeetConditions(what))
				return;
		}
		settings = calculateOverrides(settings, what);
		if(!settings.getOverrides().isEmpty())
			settings = calculateOverrides(settings, what);
		ExplosionTriggerEvent e = new ExplosionTriggerEvent(settings, what, location, blockList, settings.getDamageAmount(DamageCategory.BLOCK));
		if(!settings.getAllowDamage(DamageCategory.BLOCK))
			e.setCancelled(true);
		Bukkit.getPluginManager().callEvent(e);
		settings = e.getSettings();
		double blockDamage = e.getBlockDamage();
		if(e.isCancelled())
			return;
		if(event instanceof EntityExplodeEvent)
			((EntityExplodeEvent) event).setYield(0.0f);
		else if(event instanceof BlockExplodeEvent)
			((BlockExplodeEvent) event).setYield(0.0f);
		ExplosionRegen.getExplosionMap().addExplosion(settings, location, blockList, blockDamage);

	}

	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		ExplosionSettings settings = ExplosionSettings.getSettings("default");
		Object what;
		Location location;
		if(event instanceof EntityDamageByEntityEvent && event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
			what = ((EntityDamageByEntityEvent) event).getDamager();
			location = ((EntityDamageByEntityEvent) event).getDamager().getLocation();
		} else if(event instanceof EntityDamageByBlockEvent && event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) {
			what = clickedBlock;
			location = clickedBlock.getLocation();
		} else
			return;
		if(settings.getConditions() != null) {
			if(!settings.getConditions().doMeetConditions(what))
				return;
		}
		settings = calculateOverrides(settings, what);
		if(!settings.getOverrides().isEmpty())
			settings = calculateOverrides(settings, what);
		ExplosionDamageEntityEvent e = new ExplosionDamageEntityEvent(settings, what, location, settings.getDamageAmount(DamageCategory.ENTITY));
		if(!settings.getAllowDamage(DamageCategory.ENTITY))
			e.setCancelled(true);
		Bukkit.getPluginManager().callEvent(e);
		settings = e.getSettings();
		double entityDamage = e.getEntityDamage();
		if(!e.isCancelled()) {
			DamageCategory category = DamageCategory.ENTITY;
			switch(settings.getDamageModifier(category)) {
				case ADD:
					event.setDamage(event.getDamage() + entityDamage);
					break;
				case DIVIDE:
					event.setDamage(event.getDamage() / entityDamage);
					break;
				case MULTIPLY:
					event.setDamage(event.getDamage() * entityDamage);
					break;
				case SET:
					event.setDamage(entityDamage);
					break;
				case SUBTRACT:
					event.setDamage(event.getDamage() - entityDamage);
					break;
			}
		}
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			clickedBlock = event.getClickedBlock().getState();
		}
	}
}
