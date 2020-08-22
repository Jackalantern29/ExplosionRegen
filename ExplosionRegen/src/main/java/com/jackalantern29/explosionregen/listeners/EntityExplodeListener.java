package com.jackalantern29.explosionregen.listeners;

import java.util.ArrayList;
import java.util.List;

import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.DamageCategory;
import com.jackalantern29.explosionregen.api.events.ExplosionDamageEntityEvent;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
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
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

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
		if(!ExplosionRegen.getSettings().getGPAllowExplosionRegen() && ExplosionRegen.getInstance().getGriefPrevention() != null) {
			for(Claim claims : ExplosionRegen.getInstance().getGriefPrevention().dataStore.getClaims()) {
				if(claims.areExplosivesAllowed) {
					double x1; double z1; double x2; double z2;
					x1 = claims.getLesserBoundaryCorner().getX(); z1 = claims.getLesserBoundaryCorner().getZ();
					x2 = claims.getGreaterBoundaryCorner().getX(); z2 = claims.getGreaterBoundaryCorner().getZ();
					if(location.getBlockX() >= x1 && location.getBlockX() <= x2 && location.getBlockZ() >= z1 && location.getBlockZ() <= z2)
						return;
				}
			}
		}
		ExplosionSettings settings = ExplosionSettings.getSettings("default");
		if(settings.getConditions() != null) {
			if(!settings.getConditions().doMeetConditions(what))
				return;
		}
		Explosion explosion = new Explosion(settings, what, location, blockList);
		settings = calculateOverrides(settings, what);
		if(!settings.getOverrides().isEmpty())
			settings = calculateOverrides(settings, what);
		ExplosionTriggerEvent e = new ExplosionTriggerEvent(explosion);
		if(!settings.getAllowDamage(DamageCategory.BLOCK))
			e.setCancelled(true);
		Bukkit.getPluginManager().callEvent(e);
		settings = e.getExplosion().getSettings();
		double blockDamage = e.getExplosion().getSettings().getDamageAmount(DamageCategory.ENTITY);
		if(e.isCancelled())
			return;
		int powerRadius = 5;
		if(event instanceof EntityExplodeEvent) {
			EntityExplodeEvent explodeEvent = (EntityExplodeEvent)event;
			explodeEvent.setYield(0.0f);
			if(explodeEvent.getEntity() instanceof Explosive)
				powerRadius = (int) Math.ceil(((Explosive)explodeEvent.getEntity()).getYield());
		}
		else if(event instanceof BlockExplodeEvent)
			((BlockExplodeEvent) event).setYield(0.0f);
		for (int x = powerRadius * -1; x <= powerRadius; x++)
			for (int y = powerRadius * -1; y <= powerRadius; y++)
				for (int z = powerRadius * -1; z <= powerRadius; z++) {
					Block block = location.getBlock().getRelative(x, y, z);
					if(MaterialUtil.isIndestructible(block.getType()) && !settings.getBlockSettings().get(new RegenBlockData(block.getType())).doPreventDamage())
						blockList.add(block);
				}
		explosion.start();
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
		Explosion explosion = new Explosion(settings, what, location, null, 0);
		ExplosionDamageEntityEvent e = new ExplosionDamageEntityEvent(explosion);
		if(!settings.getAllowDamage(DamageCategory.ENTITY))
			e.setCancelled(true);
		Bukkit.getPluginManager().callEvent(e);
		settings = e.getExplosion().getSettings();
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

	@EventHandler
	public void onItemFrameDestroy(HangingBreakEvent event) {
		if(event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
			if(event.getEntity() instanceof ItemFrame) {
				ItemStack item;
				ItemFrame frame = (ItemFrame)event.getEntity();
				Location location = event.getEntity().getLocation().clone();
				item = frame.getItem();
				event.getEntity().remove();
				location.getWorld().dropItemNaturally(location, new ItemStack(Material.ITEM_FRAME));
				if(item != null && item.getType() != Material.AIR)
					location.getWorld().dropItemNaturally(location, item);
			} else if(event.getEntity() instanceof Painting) {
				Location location = event.getEntity().getLocation().clone();
				event.getEntity().remove();
				location.getWorld().dropItemNaturally(location, new ItemStack(Material.PAINTING));
			}
		}
	}
}
