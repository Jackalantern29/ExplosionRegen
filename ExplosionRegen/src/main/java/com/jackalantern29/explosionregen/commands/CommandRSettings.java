package com.jackalantern29.explosionregen.commands;

import java.util.ArrayList;
import java.util.List;

import com.jackalantern29.erspecialeffects.InventoryMenu;
import com.jackalantern29.explosionregen.api.*;
import com.jackalantern29.explosionregen.api.inventory.ItemBuilder;
import com.jackalantern29.explosionregen.api.inventory.SettingsMenu;
import com.jackalantern29.explosionregen.api.inventory.SlotElement;
import com.jackalantern29.flatx.api.FlatBlockData;
import com.jackalantern29.flatx.bukkit.BukkitAdapter;
import com.jackalantern29.flatx.bukkit.FlatBukkit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.enums.WeatherType;
import com.jackalantern29.explosionregen.api.enums.ExplosionCondition;

public class CommandRSettings implements TabExecutor {

	private static SettingsMenu menu = new SettingsMenu("Select Explosion §l[Server]", 5);

	public static Inventory inventoryMenu;
	static {
		menu.clear();
		menu.setUpdate("menu", () -> {
			for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
				ItemStack item = new ItemBuilder(settings.getDisplayItem()).setDisplayName(settings.getDisplayName()).build();
				menu.addItem(new SlotElement(item, data -> {
					data.getWhoClicked().openInventory(settings.getSettingsMenu().getInventory());
					return true;
				}));
			}
		});
		inventoryMenu = menu.getInventory();
	}
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("explosionregen.command.rsettings")) {
			sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
			return true;
		}
		if(args.length == 0) {
			Player player = (Player)sender;
			if(Bukkit.getPluginManager().getPlugin("ERSpecialEffects") != null) {
				if(ExplosionRegen.getSettings().getAllowProfileSettings())
					InventoryMenu.sendMenu(player, ProfileSettings.get(player));
				else {
					if(!sender.hasPermission("explosionregen.command.rsettings.server")) {
						sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
						return true;
					}
					if(Bukkit.getPluginManager().getPlugin("ERSpecialEffects") != null) {
						InventoryMenu.sendMenu(player, null);
					}
				}
			}
		} else {
			if(args[0].equalsIgnoreCase("server")) {
				if(!sender.hasPermission("explosionregen.command.rsettings.server")) {
					sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
					return true;
				}
				Player player = (Player)sender;
				player.openInventory(menu.getInventory());
//				InventorySettings.get(player.getUniqueId()).openSettings(player, true);
			} else if(args[0].equalsIgnoreCase("create")) {
				if(!sender.hasPermission("explosionregen.command.rsettings.server")) {
					sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
					return true;
				}
				if(args.length == 1) {
					sender.sendMessage("§cUsage: /rsettings create <explosion|block> <name>");
					return true;
				} else {
					String type = args[1];
					String name = args[2];
					if(type.equalsIgnoreCase("explosion")) {
						BlockSettings blockSettings = args.length >= 4 ? BlockSettings.getSettings(args[3]) : BlockSettings.getSettings("default");
						ExplosionSettings settings = ExplosionSettings.registerSettings(name, blockSettings);
						settings.saveAsFile();
						sender.sendMessage("Registered Explosion Settings '" + settings.getName() + "' using '" + settings.getBlockSettings().getName() + "' block settings.");
					} else if(type.equalsIgnoreCase("block")) {
						BlockSettings settings = BlockSettings.createSettings(name);
						settings.saveAsFile();
						sender.sendMessage("[ExplosionRegen] Created Block Settings '" + name + "'.");
					}
				}
			} else if(args[0].equalsIgnoreCase("edit")) {
				if(!sender.hasPermission("explosionregen.command.rsettings.edit")) {
					sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
					return true;
				}
				if(args.length == 1) {
					
				} else {
					ExplosionSettings settings = ExplosionSettings.getSettings(args[1]);
					String option = args.length >= 3 ? args[2] : null;
					String action = args.length >= 4 ? args[3] : null;
					if(settings == null) {
						sender.sendMessage("§c'" + args[1] + "' settings does not exist. ");
						return true;
					}
					if(option == null) {
						sender.sendMessage("§cOption not valid.");
						return true;
					}
					if(action == null) {
						sender.sendMessage("§cAction not valid.");
						return true;
					}
					if(option.equalsIgnoreCase("condition")) {
						ExplosionCondition condition = args.length >= 5 ? ExplosionCondition.valueOf(args[4].toUpperCase()) : null;
						if(condition == null) {
							sender.sendMessage("§cCondition not valid.");
							return true;
						}
						if(action.equalsIgnoreCase("add") || action.equalsIgnoreCase("set")) {
							String valueArg = args.length >= 6 ? args[5] : null;
							if(valueArg == null) {
								sender.sendMessage("§cValue not valid.");
								return true;
							}
							ExplosionSettingsOverride conditions = settings.getConditions();
							Object value = null;
							switch(condition) {
							case CUSTOM_NAME:
								value = valueArg;
								break;
							case BLOCK:
								value = Material.valueOf(valueArg.toUpperCase());
								break;
							case ENTITY:
								value = EntityType.valueOf(valueArg.toUpperCase());
								break;
							case IS_CHARGED:
								value = Boolean.valueOf(valueArg);
								break;
							case WEATHER:
								value = org.bukkit.WeatherType.valueOf(valueArg);
								break;
							case WORLD:
								value = Bukkit.getWorld(valueArg);
								break;
							case MAXX:
							case MINX:
							case MAXY:
							case MINY:
							case MAXZ:
							case MINZ:
								value = Double.valueOf(valueArg);
								break;
							}
							conditions.setCondition(condition, value);
							settings.addOrSetCondition(conditions);
							sender.sendMessage("Added New Condition.");
						} else if(action.equalsIgnoreCase("remove")) {
							settings.removeCondition(condition);
							return true;
						}
					} else if(option.equalsIgnoreCase("override")) {
						String overrideName = args.length >= 5 ? args[4] : null;
						if(action.equalsIgnoreCase("set")) {
							ExplosionSettings overrideWith = args.length >= 6 ? ExplosionSettings.getSettings(args[5]) : null;
							ExplosionCondition condition = args.length >= 7 ? ExplosionCondition.valueOf(args[6].toUpperCase()) : null;
							String valueArg = args.length >= 8 ? args[7] : null;
							if(args.length == 4) {
								sender.sendMessage("§cName not valid.");
								return true;
							}
							if(overrideWith == null) {
								sender.sendMessage("§cSettings not valid.");
								return true;
							}
							if(condition == null) {
								sender.sendMessage("§cCondition not valid.");
								return true;
							}
							if(valueArg == null) {
								sender.sendMessage("§cValue not valid.");
								return true;
							}
							ExplosionSettingsOverride override = null;
							for(ExplosionSettingsOverride ovr : settings.getOverrides()) {
								if(ovr.getName().equalsIgnoreCase(overrideName))
									override = ovr;
							}
							if(override == null)
								override = new ExplosionSettingsOverride(overrideName, overrideWith);
							Object value = null;
							switch(condition) {
							case CUSTOM_NAME:
								value = valueArg;
								break;
							case BLOCK:
								value = Material.valueOf(valueArg.toUpperCase());
								break;
							case ENTITY:
								value = EntityType.valueOf(valueArg.toUpperCase());
								break;
							case IS_CHARGED:
								value = Boolean.valueOf(valueArg);
								break;
							case WEATHER:
								value = org.bukkit.WeatherType.valueOf(valueArg);
								break;
							case WORLD:
								value = Bukkit.getWorld(valueArg);
								break;
							case MAXX:
							case MINX:
							case MAXY:
							case MINY:
							case MAXZ:
							case MINZ:
								value = Double.valueOf(valueArg);
								break;
							}
							override.setCondition(condition, value);
							settings.addOrSetOverride(override);
							sender.sendMessage("Override Condition Set.");
						} else if(action.equalsIgnoreCase("remove")) {
							settings.removeOverride(overrideName);
							return true;
						}
					} else if(option.equalsIgnoreCase("block")) {
						if(args.length < 5) {
							sender.sendMessage("§c/rsettings edit " + settings.getName().toLowerCase() + " " + option.toLowerCase() + " " + action.toLowerCase());
							return true;
						}

						FlatBlockData flatData;
						try {
							flatData = FlatBukkit.createBlockData(args[4].toLowerCase());
						} catch(IllegalArgumentException e) {
							sender.sendMessage("§cInvalid block.");
							return true;
						}
						BlockSettingsData data = new BlockSettingsData(flatData);
						if(action.equalsIgnoreCase("add")) {
							if(!settings.getBlockSettings().contains(flatData.getAsString())) {
								settings.getBlockSettings().add(data);
								sender.sendMessage("§aAdded '" + flatData.getAsString() + "'.");
							} else {
								sender.sendMessage("§a'" + flatData.getAsString() + "' already exists.");
							}
							return true;
						} else if(action.equalsIgnoreCase("remove")) {
							if(settings.getBlockSettings().contains(flatData.getAsString())) {
								settings.getBlockSettings().remove(flatData.getAsString());
								sender.sendMessage("§aRemoved '" + flatData.getAsString() + "'.");
							} else {
								sender.sendMessage("§a'" + flatData.getAsString() + "' does not exist.");
							}
						}
					}
				}
			} else if(args[0].equalsIgnoreCase("reload")) {
				if(!sender.hasPermission("explosionregen.command.rsettings.reload")) {
					sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
					return true;
				}
				sender.sendMessage("§7[§cExplosionRegen§7] §aReloading...");
				ExplosionRegen.getSettings().reload();
				sender.sendMessage("§7[§cExplosionRegen§7] §aDone.");
			}
		}
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		String permission = "explosionregen.command.rsettings";
		List<String> list = new ArrayList<>();
		if(cmd.getName().equalsIgnoreCase("rsettings")) {
			if(sender.hasPermission(permission)) {
				if(args.length == 1) {
					if(sender.hasPermission(permission + ".server"))
						list.add("server");
					if(sender.hasPermission(permission + ".create"))
						list.add("create");
					if(sender.hasPermission(permission + ".edit"))
						list.add("edit");
					if(sender.hasPermission(permission + ".reload"))
						list.add("reload");
					return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>(list.size()));
				} else {
					if(args[0].equalsIgnoreCase("create") && sender.hasPermission(permission + ".create")) {
						if(args.length == 2) {
							list.add("explosion");
							list.add("block");
						} else if(args.length == 3) {
							list.add("<name>");
						} else if(args.length == 4) {
							BlockSettings.getBlockSettings().forEach(blockSettings -> list.add(blockSettings.getName()));
						}
					}
					if(args[0].equalsIgnoreCase("edit") && sender.hasPermission(permission + ".edit")) {
						if(args.length == 2) {
							for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings())
								list.add(settings.getName());
							return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>(list.size()));
						} else if(args.length == 3) {
							list.add("override");
							list.add("condition");
							list.add("block");
							return StringUtil.copyPartialMatches(args[2], list, new ArrayList<>(list.size()));
						} else if(args.length == 4) {
							if(args[2].equalsIgnoreCase("condition") || args[2].equalsIgnoreCase("override")) {
								list.add("set");
								list.add("remove");
							} else if(args[2].equalsIgnoreCase("block")) {
								list.add("add");
								list.add("remove");
							}
							return StringUtil.copyPartialMatches(args[3], list, new ArrayList<>(list.size()));
						} else if(args.length == 5) {
							if(args[2].equalsIgnoreCase("override")) {
								if(args[4].length() == 0)
									list.add("<override> <settings> <condition> <value>");
								else
									list.add(args[4] + " <settings> <condition> <value>");
							} else if(args[2].equalsIgnoreCase("condition")) {
								for(ExplosionCondition condition : ExplosionCondition.values())
									list.add(condition.name().toLowerCase());
							} else if(args[2].equalsIgnoreCase("block")) {
								if(args[3].equalsIgnoreCase("add")) {
									if(sender instanceof Player) {
										list.add((((Player)sender).getTargetBlock(null, 10).getBlockData().getAsString()));
									}
								} else if(args[3].equalsIgnoreCase("remove")) {
									ExplosionSettings settings = ExplosionSettings.getSettings(args[1]);
									for(BlockSettingsData data : settings.getBlockSettings().getBlockDatas()) {
										if(data.getFlatData() != null)
											list.add(data.getFlatData().getAsString());
									}
								}
							}
							return list;
						} else if(args.length == 6) {
							if(args[2].equalsIgnoreCase("override")) {
								for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings())
									list.add(settings.getName());
							} else if(args[2].equalsIgnoreCase("condition")) {
								switch(ExplosionCondition.valueOf(args[4].toUpperCase())) {
								case CUSTOM_NAME:
									if(args[5].length() == 0)
										list.add("<custom name>");
									else
										list.add(args[5]);
									return list;
								case ENTITY:
									for(EntityType type : EntityType.values())
										list.add(type.name().toLowerCase());
									return StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
								case BLOCK:
									for(Material type : Material.values())
										if(type.isBlock())
											list.add(type.name().toLowerCase());
									return StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
								case IS_CHARGED:
									list.add("true");
									list.add("false");
									return StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
								case WEATHER:
									for(WeatherType type : WeatherType.values())
										list.add(type.name().toLowerCase());
									return StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
								case WORLD:
									for(World world : Bukkit.getWorlds())
										list.add(world.getName().toLowerCase());
									return StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
								case MAXX:
								case MINX:
								case MAXY:
								case MINY:
								case MAXZ:
								case MINZ:
									break;
								}
								return list;
							}
							return StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
						} else if(args.length == 7) {
							if(args[2].equalsIgnoreCase("override")) {
								for(ExplosionCondition condition : ExplosionCondition.values())
									list.add(condition.name().toLowerCase());
							}
							return StringUtil.copyPartialMatches(args[6], list, new ArrayList<>(list.size()));
						} else if(args.length == 8) {
							if(args[2].equalsIgnoreCase("override")) {
								switch(ExplosionCondition.valueOf(args[6].toUpperCase())) {
								case CUSTOM_NAME:
									if(args[7].length() == 0)
										list.add("<custom name>");
									else
										list.add(args[7]);
									return list;
								case ENTITY:
									for(EntityType type : EntityType.values())
										list.add(type.name().toLowerCase());
									return StringUtil.copyPartialMatches(args[7], list, new ArrayList<>(list.size()));
								case BLOCK:
									for(Material type : Material.values())
										if(type.isBlock())
											list.add(type.name().toLowerCase());
									return StringUtil.copyPartialMatches(args[7], list, new ArrayList<>(list.size()));
								case IS_CHARGED:
									list.add("true");
									list.add("false");
									return StringUtil.copyPartialMatches(args[7], list, new ArrayList<>(list.size()));
								case WEATHER:
									for(WeatherType type : WeatherType.values())
										list.add(type.name().toLowerCase());
									return StringUtil.copyPartialMatches(args[7], list, new ArrayList<>(list.size()));
								case WORLD:
									for(World world : Bukkit.getWorlds())
										list.add(world.getName().toLowerCase());
									return StringUtil.copyPartialMatches(args[7], list, new ArrayList<>(list.size()));
								case MAXX:
								case MINX:
								case MAXY:
								case MINY:
								case MAXZ:
								case MINZ:
									break;
								}
							}
							return list;
						}
					}
				}
			}
		}
		return list;
	}

}
