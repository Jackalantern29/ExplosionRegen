package com.jackalantern29.explosionregen.commands;

import java.util.ArrayList;
import java.util.List;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.flatx.bukkit.BukkitAdapter;
import com.jackalantern29.flatx.bukkit.FlatBukkit;
import org.apache.commons.lang.BooleanUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.ExplosionSettings;

public class CommandRExplode implements TabExecutor {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		String permission = "explosionregen.command.rexplode";
		List<String> list = new ArrayList<>();
		if(cmd.getName().equalsIgnoreCase("rexplode")) {
			if(sender.hasPermission(permission)) {
				if(args.length == 1) {
					list.add("at");
					if(sender instanceof Player) {
						list.add("atlook");
						list.add("" + ((Player)sender).getLocation().getBlockX());
					}
					return StringUtil.copyPartialMatches(args[0], list, new ArrayList<>(list.size()));
				} else {
					if(args[0].equalsIgnoreCase("at")) {
						if(args.length == 2) {
							Bukkit.getOnlinePlayers().forEach(player -> list.add(player.getName()));
							return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>(list.size()));
						} else if(args.length == 3) {
							ExplosionSettings.getRegisteredSettings().forEach(settings -> list.add(settings.getName()));
							return StringUtil.copyPartialMatches(args[2], list, new ArrayList<>(list.size()));
						} else if(args.length == 4) {
							if(args[3].length() == 0)
								list.add("[power]");
							else {
								try {
									list.add("" + Float.parseFloat(args[3]));
								} catch(NumberFormatException e) {
									list.add("§cInvalid Number Format");
								}
							}
							return StringUtil.copyPartialMatches(args[3], list, new ArrayList<>(list.size()));
						} else if(args.length == 5) {
							list.add("true");
							list.add("false");
							if(args[4].length() == 0) {
								Object fire = BooleanUtils.toBooleanObject(args[4]);
								if(fire == null) {
									ArrayList<String> list1 = StringUtil.copyPartialMatches(args[4], list, new ArrayList<>(list.size()));
									if(list1.isEmpty()) {
										list1.add("§cInvalid Boolean Format");
										return list1;
									}
								}
							}
							return StringUtil.copyPartialMatches(args[4], list, new ArrayList<>(list.size()));
						}
					} else if(args[0].equalsIgnoreCase("atlook")) {
						if(sender instanceof Player) {
							if(args.length == 2) {
								ExplosionSettings.getRegisteredSettings().forEach(settings -> list.add(settings.getName()));
								return StringUtil.copyPartialMatches(args[1], list, new ArrayList<>(list.size()));
							} else if(args.length == 3) {
								if(args[2].length() == 0)
									list.add("[power]");
								else {
									try {
										list.add("" + Float.parseFloat(args[2]));
									} catch(NumberFormatException e) {
										list.add("§cInvalid Number Format");
									}
								}
								return StringUtil.copyPartialMatches(args[2], list, new ArrayList<>(list.size()));
							} else if(args.length == 4) {
								list.add("true");
								list.add("false");
								if(args[3].length() == 0) {
									Object fire = BooleanUtils.toBooleanObject(args[3]);
									if(fire == null) {
										ArrayList<String> list1 = StringUtil.copyPartialMatches(args[3], list, new ArrayList<>(list.size()));
										if(list1.isEmpty()) {
											list1.add("§cInvalid Boolean Format");
											return list1;
										}
									}
								}
								return StringUtil.copyPartialMatches(args[3], list, new ArrayList<>(list.size()));
							}
						}
					} else {
						if(args.length == 2) {
						} else if(args.length == 4) {
							ExplosionSettings.getRegisteredSettings().forEach(settings -> list.add(settings.getName()));
							return StringUtil.copyPartialMatches(args[3], list, new ArrayList<>(list.size()));
						} else if(args.length == 5) {
							if(args[4].length() == 0)
								list.add("[power]");
							else {
								try {
									list.add("" + Float.parseFloat(args[4]));
								} catch(NumberFormatException e) {
									list.add("§cInvalid Number Format");
								}
							}
							return StringUtil.copyPartialMatches(args[4], list, new ArrayList<>(list.size()));
						} else if(args.length == 6) {
							list.add("true");
							list.add("false");
							if(args[5].length() == 0) {
								Object fire = BooleanUtils.toBooleanObject(args[5]);
								if(fire == null) {
									ArrayList<String> list1 = StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
									if(list1.isEmpty()) {
										list1.add("§cInvalid Boolean Format");
										return list1;
									}
								}
							}
							return StringUtil.copyPartialMatches(args[5], list, new ArrayList<>(list.size()));
						}
					}
				}
			}
		}
		return list;
	}

	// /rexplode <location|onlinePlayer> <settings> [power] [setFire]
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!sender.hasPermission("explosionregen.command.rexplode")) {
			sender.sendMessage(ExplosionRegen.getSettings().getNoPermCmdChat());
			return true;
		}
		Location location = null;
		ExplosionSettings settings = ExplosionSettings.getSettings("default");
		float power = 4f;
		boolean setFire = false;
		if(sender instanceof Player)
			location = ((Player)sender).getLocation();
		if(args.length == 0) {
			if(location == null) {
				sender.sendMessage("/rexplode <location|player> [settings]");
				return true;
			}
		} else {
			int totalArgs = args[0].equalsIgnoreCase("at") ? 3 : args[0].equalsIgnoreCase("atlook") ? 2 : 4;
			String settingsArg = args.length >= totalArgs ? args[totalArgs-1] : null;
			String powerArg = args.length >= totalArgs+1 ? args[totalArgs] : null;
			String fireArg = args.length >= totalArgs+2 ? args[totalArgs+1] : null;
			if(args[0].equalsIgnoreCase("at")) {
				if(args.length == 1) {
					sender.sendMessage("§cError: Missing value <player>");
					return true;
				} else {
					Player target = Bukkit.getPlayerExact(args[1]);
					if(target == null) {
						sender.sendMessage("§cError: Could not find player '" + args[1] + "'.");
						return true;
					}
					location = target.getLocation();
				}
			} else if(args[0].equalsIgnoreCase("atlook")) {
				if(sender instanceof Player) {
					Player player = (Player)sender;
					location = BukkitAdapter.asBukkitLocation(FlatBukkit.getPlayer(player.getUniqueId()).getTargetBlock(null, 100).getLocation());
				} else {
					sender.sendMessage("[ExplosionRegen] This command is only available for player use.");
					return true;
				}
			} else {
				int[] a = new int[] {1, 2, 3};
				World world = Bukkit.getWorld(args[0]);
				if(world == null) {
					if(sender instanceof Player) {
						world = ((Player)sender).getWorld();
						a = new int[] {0, 1, 2};
					}
					else {
						sender.sendMessage("§cError: World '" + args[0] + "' does not exist.");
						return true;
					}
				}
				double x;
				double y;
				double z;
				try {
					x = Double.parseDouble(args[a[0]]);
				} catch(NumberFormatException e) {
					sender.sendMessage("§cError: '" + args[a[0]] + "' is not an accepted value.");
					return true;
				}
				try {
					y = Double.parseDouble(args[a[1]]);
				} catch(NumberFormatException e) {
					sender.sendMessage("§cError: '" + args[a[1]] + "' is not an accepted value.");
					return true;
				}
				try {
					z = Double.parseDouble(args[a[2]]);
				} catch(NumberFormatException e) {
					sender.sendMessage("§cError: '" + args[a[2]] + "' is not an accepted value.");
					return true;
				}
				location = new Location(world, x, y, z);
			}
			if(settingsArg != null) {
				for(ExplosionSettings s : ExplosionSettings.getRegisteredSettings())
					if(s.getName().equalsIgnoreCase(settingsArg))
						settings = s;
			}
			if(powerArg != null)
				power = Float.parseFloat(powerArg);
			if(fireArg != null)
				setFire = Boolean.getBoolean(fireArg);
		}
		Explosion.createExplosion(location, settings, false, power, setFire);
		return true;
	}

}
