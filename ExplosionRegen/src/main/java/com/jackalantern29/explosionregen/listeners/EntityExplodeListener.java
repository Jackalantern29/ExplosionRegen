package com.jackalantern29.explosionregen.listeners;

import java.util.List;
import java.util.Map;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.ExplosionCondition;
import com.jackalantern29.explosionregen.api.ExplosionOverride;
import com.jackalantern29.explosionregen.api.enums.DamageCategory;
import com.jackalantern29.explosionregen.api.events.ExplosionDamageEntityEvent;
import com.jackalantern29.flatx.api.enums.FlatMaterial;
import com.jackalantern29.flatx.bukkit.event.BlockExplodeEvent;
import com.jackalantern29.flatx.bukkit.event.EntityExplodeEvent;
import com.jackalantern29.flatx.bukkit.event.ExplodeEvent;
import com.jackalantern29.flatx.bukkit.BukkitAdapter;
import me.ryanhamshire.GriefPrevention.Claim;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Painting;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EntityExplodeListener implements Listener {
	private BlockState clickedBlock = null;

	/**
	 * Called when an entity explodes
	 */
	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		if(!event.isCancelled())
			explode(event, event.getSource(), event.getLocation(), event.blockList());
	}

	/**
	 * Called when a block explodes
	 */
	@EventHandler
	public void onExplode(BlockExplodeEvent event) {
		if(!event.isCancelled())
			explode(event, event.getSource(), event.getLocation(), event.blockList());
	}

	/**
	 * Called when an entity or block explodes
	 *
	 * Sets the explosion's settings & start the explosion's regen delay
	 *
	 * @param event The explosion event (EntityExplodeEvent/BlockExplodeEvent)
	 * @param what The entity or block that exploded
	 * @param location The location the explosion occurred
	 * @param blockList List of blocks added to the damaged list
	 */
	private void explode(ExplodeEvent event, Object what, Location location, List<Block> blockList) {
		if(!ExplosionRegen.getSettings().getWorlds().contains(location.getWorld().getName()))
			return;
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
		//Start with default explosion before checking source modification.
		ExplosionSettings settings = ExplosionSettings.getSettings("default");
		if(settings.getCondition() != null) {
			if(!settings.getCondition().doMeetConditions(what))
				return;
		}
		settings = calculateOverrides(settings, what);
		if(!settings.getAllowExplosion()) {
			((Cancellable)event).setCancelled(true);
			return;
		}
		if(!settings.getCondition().doMeetConditions(what))
			return;
		Explosion explosion = new Explosion(settings, what, location, blockList);

		int powerRadius = 5;
		if(event instanceof EntityExplodeEvent) {
			EntityExplodeEvent explodeEvent = (EntityExplodeEvent)event;
			explodeEvent.setYield(0.0f);
			if(explodeEvent.getEntity() instanceof Explosive)
				powerRadius = (int) Math.ceil(((Explosive)explodeEvent.getEntity()).getYield());
		}
		else if(event instanceof BlockExplodeEvent)
			((BlockExplodeEvent) event).setYield(0.0f);
	}

	/**
	 * Find the settings that will be overridden from the source with the used settings
	 *
	 * @param settings The settings used for the source
	 * @param source The entity/block explosion
	 * @return The settings that would override the previous settings
	 */
	private ExplosionSettings calculateOverrides(ExplosionSettings settings, Object source) {
		ExplosionSettings newSettings = settings;

		if(source instanceof Entity) {
			int conditions = 0;
			for(Map.Entry<String, ExplosionOverride> override : settings.getOverrides()) {
				ExplosionCondition condition = override.getValue().getCondition();
				if(condition.doMeetConditions(source) && condition.countConditions() > conditions) {
					conditions = condition.countConditions();
					newSettings = override.getValue().getSettings();
				}
			}
		}
		if(!newSettings.getOverrides().isEmpty() && newSettings != settings)
			newSettings = calculateOverrides(settings, source);
		return newSettings;
	}
	/**
	 * Called when an explosion damages an entity
	 */
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
		if(settings.getCondition() != null) {
			if(!settings.getCondition().doMeetConditions(what))
				return;
		}
		settings = calculateOverrides(settings, what);
		if(!settings.getAllowExplosion()) {
			event.setCancelled(true);
			return;
		}
		ExplosionDamageEntityEvent e = new ExplosionDamageEntityEvent(settings, what, location);
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

	/**
	 * Saves the block clicked which will be used for when a block such as a bed explodes
	 */
	@EventHandler
	public void onClick(PlayerInteractEvent event) {
		if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			clickedBlock = event.getClickedBlock().getState();
		}
	}

	/**
	 * Allows item frames & paintings to always drop
	 */
	@EventHandler
	public void onItemFrameDestroy(HangingBreakEvent event) {
		if(event.getCause() == HangingBreakEvent.RemoveCause.EXPLOSION) {
			if(event.getEntity() instanceof ItemFrame) {
				ItemStack item;
				ItemFrame frame = (ItemFrame)event.getEntity();
				Location location = event.getEntity().getLocation().clone();
				item = frame.getItem();
				event.getEntity().remove();
				location.getWorld().dropItemNaturally(location, new ItemStack(BukkitAdapter.asBukkitMaterial(FlatMaterial.ITEM_FRAME)));
				if(item != null && item.getType() != BukkitAdapter.asBukkitMaterial(FlatMaterial.AIR))
					location.getWorld().dropItemNaturally(location, item);
			} else if(event.getEntity() instanceof Painting) {
				Location location = event.getEntity().getLocation().clone();
				event.getEntity().remove();
				location.getWorld().dropItemNaturally(location, new ItemStack(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAINTING)));
			}
		}
	}
}
