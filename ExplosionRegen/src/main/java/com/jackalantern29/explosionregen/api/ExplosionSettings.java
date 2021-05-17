package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.blockdata.RegenBlockData;
import com.jackalantern29.explosionregen.api.enums.*;
import com.jackalantern29.explosionregen.api.events.ExplosionSettingsLoadEvent;
import com.jackalantern29.explosionregen.api.events.ExplosionSettingsUnloadEvent;
import com.jackalantern29.explosionregen.api.inventory.*;
import com.jackalantern29.explosionregen.commands.CommandRSettings;
import com.jackalantern29.flatx.api.enums.FlatMaterial;
import com.jackalantern29.flatx.bukkit.BukkitAdapter;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class  ExplosionSettings {
	private static final Map<String, ExplosionSettings> MAP = new HashMap<>();
	
	private final String name;

	private BlockSettings blockSettings;
	private boolean enable = true;
	private boolean regenAllow = true;
	private GenerateDirection regenDirection = GenerateDirection.RANDOM_UP;
	private boolean regenInstant = false;
	private long regenDelay = 200;
	private int regenMaxBlockQueue = 1;
	private boolean regenForceBlock = false;
	
	private boolean damageBlockAllow = true;
	private DamageModifier damageBlockModifier = DamageModifier.MULTIPLY;
	private double damageBlockAmount = 1.0d;
	private boolean damageEntityAllow = true;
	private DamageModifier damageEntityModifier = DamageModifier.MULTIPLY;
	private double damageEntityAmount = 1.0d;

	private final HashMap<String, ExplosionSettingsPlugin> plugins = new HashMap<>();

	private ItemStack displayItem;
	private String displayName;

	private final Map<String, ExplosionSettingsOverride> overrides = new HashMap<>();
	private final ExplosionSettingsOverride conditions;

	private SettingsMenu menu;

	private boolean saveChanges = false;

	private ExplosionSettings(String name, BlockSettings blockSettings) {
		this.name = name;
		this.blockSettings = blockSettings;
		this.displayName = name;
		this.displayItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.TNT)).setDisplayName(name).build();
		this.conditions = new ExplosionSettingsOverride(name + "-conditions", this);
		setupInventory();
		Bukkit.getPluginManager().callEvent(new ExplosionSettingsLoadEvent(this));
		MAP.put(name, this);
	}

	private void setupInventory() {
		this.menu = new SettingsMenu(getDisplayName(), 54);
		PageMenu bsMenu = new PageMenu(getDisplayName() + " §l[Block Settings]", 54);
		PageMenu pluginMenu = new PageMenu(getDisplayName() + " §l[Plugins]", 18);
		PageMenu conditionMenu = new PageMenu(getDisplayName() + " §l[Conditions]", 18);
		PageMenu overrideMenu = new PageMenu(getDisplayName() + " §l[Overrides]", 18);
		ItemStack closeItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BARRIER)).setDisplayName("§c§lClose Menu").build();

		// Sets the main menu
		menu.setUpdate("menu", () -> {

			ItemStack blockSettingsItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.STONE)).setDisplayName("§fBlock Settings").setLine(0, "§7Selected: §n" + getBlockSettings().getName()).build();
			ItemStack enableItem = new ItemCondition(
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.APPLE)).setDisplayName("§fAllow Explosion: §cFalse"),
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.GOLDEN_APPLE)).setDisplayName("§fAllow Explosion: §aTrue")
					).build(getAllowExplosion());
			ItemStack displayNameItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fDisplay Name: '" + getDisplayName() + "'").build();
			ItemStack allowRegenItem = new ItemCondition(
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.POTION)).setDisplayName("§fAllow Regen: §cFalse"),
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.POTION)).setDisplayName("§fAllow Regen: §aTrue")
			).build(getAllowRegen());
			ItemStack regenDirectionItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.COMPASS)).setDisplayName("§fDirection: §6" + WordUtils.capitalize(getRegenerateDirection().name().toLowerCase().replace("_", " "))).build();
			ItemStack regenInstantItem = new ItemCondition(
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.GHAST_TEAR)).setDisplayName("§fInstant Regen: §cFalse"),
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.GHAST_TEAR)).setDisplayName("§fInstant Regen: §aTrue")
			).build(isInstantRegen());
			ItemStack regenDelayItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.REDSTONE)).setDisplayName("§fRegen Delay: §6" + getRegenDelay()).build();
			ItemStack regenMaxBlockItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.CHEST)).setDisplayName("§fMax Block Queue: §6" + getMaxBlockRegenQueue()).build();
			ItemStack regenForceItem = new ItemCondition(
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.ENDER_PEARL)).setDisplayName("§fForce Block Regen: §cFalse"),
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.ENDER_EYE)).setDisplayName("§fForce Block Regen: §aTrue")
			).build(getRegenForceBlock());
			ItemStack damageInfoItem = new ItemBuilder(MaterialUtil.getMaterial("GUNPOWDER")).setDisplayName("§f- Damage -")
					.setLine(0, "§fBlock:")
					.setLine(1, "  §fAllow:    " + (getAllowDamage(DamageCategory.BLOCK) ? "§aTrue" : "§cFalse"))
					.setLine(2, "  §fModifier: §6" + WordUtils.capitalize(getDamageModifier(DamageCategory.BLOCK).name().toLowerCase()))
					.setLine(3, "  §fAmount:   §6" + getDamageAmount(DamageCategory.BLOCK))
					.setLine(4, "§fEntity:")
					.setLine(5, "  §fAllow:    " + (getAllowDamage(DamageCategory.ENTITY) ? "§aTrue" : "§cFalse"))
					.setLine(6, "  §fModifier: §6" + WordUtils.capitalize(getDamageModifier(DamageCategory.ENTITY).name().toLowerCase()))
					.setLine(7, "  §fAmount:   §6" + getDamageAmount(DamageCategory.ENTITY))
					.build();
			ItemStack damageBlockInfoItem = new ItemBuilder(MaterialUtil.getMaterial("GRASS_BLOCK")).setDisplayName("§f - Damage [Block] -")
					.setLine(0, "§fBlock:")
					.setLine(1, "  §fAllow:    " + (getAllowDamage(DamageCategory.BLOCK) ? "§aTrue" : "§cFalse"))
					.setLine(2, "  §fModifier: §6" + WordUtils.capitalize(getDamageModifier(DamageCategory.BLOCK).name().toLowerCase()))
					.setLine(3, "  §fAmount:   §6" + getDamageAmount(DamageCategory.BLOCK))
					.build();
			ItemStack damageEntityInfoItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.ARMOR_STAND)).setDisplayName("§f - Damage [Entity] -")
					.setLine(0, "§fEntity:")
					.setLine(1, "  §fAllow:    " + (getAllowDamage(DamageCategory.ENTITY) ? "§aTrue" : "§cFalse"))
					.setLine(2, "  §fModifier: §6" + WordUtils.capitalize(getDamageModifier(DamageCategory.ENTITY).name().toLowerCase()))
					.setLine(3, "  §fAmount:   §6" + getDamageAmount(DamageCategory.ENTITY))
					.build();
			ItemStack damageBlockAllowItem = new ItemCondition(
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.STICK)).setDisplayName("§fAllow Block Damage: §cFalse"),
					new ItemBuilder(MaterialUtil.getMaterial("WOODEN_SHOVEL")).setDisplayName("§fAllow Block Damage: §aTrue")
			).build(getAllowDamage(DamageCategory.BLOCK));
			ItemStack damageEntityAllowItem = new ItemCondition(
					new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.STICK)).setDisplayName("§fAllow Block Damage: §cFalse"),
					new ItemBuilder(MaterialUtil.getMaterial("WOODEN_SWORD")).setDisplayName("§fAllow Entity Damage: §aTrue")
			).build(getAllowDamage(DamageCategory.ENTITY));
			ItemStack damageBlockModifierItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.ENCHANTED_BOOK)).setDisplayName("§fBlock Damage Modifier: §6" + WordUtils.capitalize(getDamageModifier(DamageCategory.BLOCK).name().toLowerCase())).build();
			ItemStack damageEntityModifierItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.ENCHANTED_BOOK)).setDisplayName("§fEntity Damage Modifier: §6" + WordUtils.capitalize(getDamageModifier(DamageCategory.ENTITY).name().toLowerCase())).build();
			ItemStack damageBlockAmountItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.FEATHER)).setDisplayName("§fBlock Damage Amount: §6" + getDamageAmount(DamageCategory.BLOCK)).build();
			ItemStack damageEntityAmountItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.FEATHER)).setDisplayName("§fEntity Damage Amount: §6" + getDamageAmount(DamageCategory.ENTITY)).build();
			ItemStack pluginsItem = new ItemBuilder(MaterialUtil.getMaterial("FILLED_MAP")).setDisplayName("§fPlugins §7[§6" + plugins.size() + "§7]").build();

			menu.setItem(0, new SlotElement(blockSettingsItem, data -> {
				bsMenu.sendInventory(data.getWhoClicked(), true);
				return true;
			}));
			menu.setItem(2, new SlotElement(enableItem, data -> {
				setAllowExplosion(!getAllowExplosion());
				menu.update("menu");
				return true;
			}));
			menu.setItem(4, new SlotElement(getDisplayItem(), data -> true));
			menu.setItem(6, new SlotElement(displayNameItem, data -> {
				data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
				InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
					setDisplayName(input);
					data.getWhoClicked().sendMessage("§cExiting Input Mode.");
					Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> menu.sendInventory(data.getWhoClicked(), true));
					return true;
				}));
				return true;
			}));
			menu.setItem(8, new SlotElement(closeItem, data -> {
				data.getWhoClicked().openInventory(CommandRSettings.inventoryMenu);
				return true;
			}));
			menu.setItem(12, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fConditions").build(), data -> {
				conditionMenu.sendInventory(data.getWhoClicked(), true);
				return true;
			}));
			menu.setItem(14, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BOOK)).setDisplayName("§fOverrides").build(), data -> {
				overrideMenu.sendInventory(data.getWhoClicked(), true);
				return true;
			}));
			menu.setItem(18, new SlotElement(allowRegenItem, data -> {
				setAllowRegen(!getAllowRegen());
				menu.update("menu");
				return true;
			}));
			menu.setItem(20, new SlotElement(regenDirectionItem, data -> {
				if(getRegenerateDirection().ordinal() == GenerateDirection.values().length-1)
					setRegenerateDirection(GenerateDirection.values()[0]);
				else
					setRegenerateDirection(GenerateDirection.values()[getRegenerateDirection().ordinal()+1]);
				menu.update("menu");
				return true;
			}));

			menu.setItem(21, new SlotElement(regenInstantItem, data -> {
				setInstantRegen(!isInstantRegen());
				menu.update("menu");
				return true;
			}));
			menu.setItem(22, new SlotElement(regenDelayItem, data -> {
				data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
				InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
					try {
						long l = Long.parseLong(input);
						setRegenDelay(l);
						data.getWhoClicked().sendMessage("§cExiting Input Mode.");
						Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> menu.sendInventory(data.getWhoClicked(), true));
						return true;
					} catch(NumberFormatException e) {
						data.getWhoClicked().sendMessage("§cInvalid number.");
						return false;
					}
				}));
				return true;
			}));
			menu.setItem(24, new SlotElement(regenMaxBlockItem, data -> {
				data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
				InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
					try {
						int value = Integer.parseInt(input);
						setMaxBlockRegenQueue(value);
						data.getWhoClicked().sendMessage("§cExiting Input Mode.");
						Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> menu.sendInventory(data.getWhoClicked(), true));
						return true;
					} catch(NumberFormatException e) {
						data.getWhoClicked().sendMessage("§cInvalid number.");
						return false;
					}
				}));
				return true;
			}));
			menu.setItem(26, new SlotElement(regenForceItem, data -> {
				setRegenForceBlock(!getRegenForceBlock());
				menu.update("menu");
				return true;
			}));
			menu.setItem(30, new SlotElement(damageInfoItem, data -> true));
			menu.setItem(37, new SlotElement(damageBlockInfoItem, data -> true));
			menu.setItem(41, new SlotElement(damageEntityInfoItem, data -> true));
			menu.setItem(45, new SlotElement(damageBlockAllowItem, data -> {
				setAllowDamage(DamageCategory.BLOCK, !getAllowDamage(DamageCategory.BLOCK));
				menu.update("menu");
				return true;
			}));
			menu.setItem(46, new SlotElement(damageBlockModifierItem, data -> {
				if(getDamageModifier(DamageCategory.BLOCK).ordinal() == DamageModifier.values().length-1)
					setDamageModifier(DamageCategory.BLOCK, DamageModifier.values()[0]);
				else
					setDamageModifier(DamageCategory.BLOCK, DamageModifier.values()[getDamageModifier(DamageCategory.BLOCK).ordinal()+1]);
				menu.update("menu");
				return true;
			}));
			menu.setItem(47, new SlotElement(damageBlockAmountItem, data -> {
				data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
				InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
					try {
						int value = Integer.parseInt(input);
						setDamageAmount(DamageCategory.BLOCK, value);
						data.getWhoClicked().sendMessage("§cExiting Input Mode.");
						Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> menu.sendInventory(data.getWhoClicked(), true));
						return true;
					} catch(NumberFormatException e) {
						data.getWhoClicked().sendMessage("§cInvalid number.");
						return false;
					}
				}));
				return true;
			}));
			menu.setItem(49, new SlotElement(damageEntityAllowItem, data -> {
				setAllowDamage(DamageCategory.ENTITY, !getAllowDamage(DamageCategory.ENTITY));
				menu.update("menu");
				return true;
			}));
			menu.setItem(50, new SlotElement(damageEntityModifierItem, data -> {
				if(getDamageModifier(DamageCategory.ENTITY).ordinal() == DamageModifier.values().length-1)
					setDamageModifier(DamageCategory.ENTITY, DamageModifier.values()[0]);
				else
					setDamageModifier(DamageCategory.ENTITY, DamageModifier.values()[getDamageModifier(DamageCategory.ENTITY).ordinal()+1]);
				menu.update("menu");
				return true;
			}));
			menu.setItem(51, new SlotElement(damageEntityAmountItem, data -> {
				data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
				InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
					try {
						int value = Integer.parseInt(input);
						setDamageAmount(DamageCategory.ENTITY, value);
						data.getWhoClicked().sendMessage("§cExiting Input Mode.");
						Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> menu.sendInventory(data.getWhoClicked(), true));
						return true;
					} catch(NumberFormatException e) {
						data.getWhoClicked().sendMessage("§cInvalid number.");
						return false;
					}
				}));
				return true;
			}));
			menu.setItem(53, new SlotElement(pluginsItem, data -> {
				pluginMenu.sendInventory(data.getWhoClicked(), true);
				return true;
			}));
		});
		SettingsMenu switchMenu = new SettingsMenu("§lSwitch Settings", 9);
		switchMenu.setUpdate("menu", () -> {
			switchMenu.setItem(8, new SlotElement(closeItem, data -> {
				bsMenu.sendInventory(data.getWhoClicked(), true);
				return true;
			}));
			for(BlockSettings settings : BlockSettings.getBlockSettings()) {
				switchMenu.addItem(new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName(settings.getName().equals(getBlockSettings().getName()) ? "§a" + settings.getName() : settings.getName()).build(), data -> {
					if(data.getItem().hasItemMeta())
						setBlockSettings(BlockSettings.getSettings(ChatColor.stripColor(data.getItem().getItemMeta().getDisplayName())));
					switchMenu.clear();
					switchMenu.update("menu");
					return true;
				}));
			}
		});
		bsMenu.setNextPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aNext Page").build());
		bsMenu.setPrevPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aBack Page").build());

		bsMenu.setUpdate("#layout", () -> {
			for(SettingsMenu page : bsMenu.getPages()) {
				page.setItem(8, new SlotElement(closeItem, data -> {
					data.getWhoClicked().openInventory(menu.getInventory());
					return true;
				}));

				page.setItem(17, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BOOK)).setDisplayName("§aSwitch Settings").setLine(0, "§fCurrent: §d" + getBlockSettings().getName()).build(), data -> {
					switchMenu.sendInventory(data.getWhoClicked());
					return true;
				}));

				page.setItem(45, new SlotElement(bsMenu.getPrevPageItem(), data -> true));
				page.setItem(53, new SlotElement(bsMenu.getNextPageItem(), data -> true));

				for (int i = 46; i < 53; i++) {
					page.setItem(i, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BLACK_STAINED_GLASS_PANE)).setDisplayName(" ").build(), data -> true));
				}
			}
		});


		bsMenu.setUpdate("menu", () -> {
			bsMenu.clear();
			bsMenu.update("#layout");
			for(BlockSettingsData blockData : getBlockSettings().getBlockDatas()) {
				SettingsMenu blockMenu = new SettingsMenu(blockData.getRegenData() == null ? "Default" : blockData.getRegenData().toString(), 18);

				blockMenu.setUpdate("menu", () -> {
					blockMenu.setItem(8, new SlotElement(closeItem, data -> {
						bsMenu.sendInventory(data.getWhoClicked(), true);
						return true;
					}));
					blockMenu.setItem(0, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fPrevent Damage: " + (blockData.doPreventDamage() ? "§aTrue" : "§cFalse")).build(), data -> {
						blockData.setPreventDamage(!blockData.doPreventDamage());
						blockMenu.update("menu");
						return true;
					}));
					blockMenu.setItem(1, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fRegenerate: " + (blockData.doRegen() ? "§aTrue" : "§cFalse")).build(), data -> {
						blockData.setRegen(!blockData.doRegen());
						blockMenu.update("menu");
						return true;
					}));
					blockMenu.setItem(3, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fCan Replace: " + (blockData.doReplace() ? "§aTrue" : "§cFalse")).build(), data -> {
						blockData.setReplace(!blockData.doReplace());
						blockMenu.update("menu");
						return true;
					}));
					blockMenu.setItem(4, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fReplace With: §6" + blockData.getReplaceWith().toString()).build(), data -> {
						data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
						InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
							try {
								Material material = Material.valueOf(input.toUpperCase());
								blockData.setReplaceWith(new RegenBlockData(material));
								blockMenu.update("menu");
								data.getWhoClicked().sendMessage("§cExiting Input Mode.");
								Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> blockMenu.sendInventory(data.getWhoClicked(), true));
								return true;
							} catch(Exception e) {
								data.getWhoClicked().sendMessage("§cInvalid material.");
								return false;
							}
						}));
						return true;
					}));
					blockMenu.setItem(6, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fBlock Update: " + (blockData.isBlockUpdate() ? "§aTrue" : "§cFalse")).build(), data -> {
						blockData.setBlockUpdate(!blockData.isBlockUpdate());
						blockMenu.update("menu");
						return true;
					}));
					blockMenu.setItem(9, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fDrop Chance: §6" + blockData.getDropChance()).build(), data -> {
						data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
						InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
							if(NumberUtils.isNumber(input)) {
								blockData.setDropChance(NumberUtils.toInt(input));
								blockMenu.update("menu");
								data.getWhoClicked().sendMessage("§cExiting Input Mode.");
								Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> blockMenu.sendInventory(data.getWhoClicked(), true));
								return true;
							} else
								data.getWhoClicked().sendMessage("§cInvalid number.");
							return false;
						}));
						return true;
					}));
					blockMenu.setItem(10, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fDurability: §6" + blockData.getDurability()).build(), data -> {
						data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
						InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
							if(NumberUtils.isDigits(input)) {
								blockData.setDurability(NumberUtils.toDouble(input));
								blockMenu.update("menu");
								data.getWhoClicked().sendMessage("§cExiting Input Mode.");
								Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> blockMenu.sendInventory(data.getWhoClicked(), true));
								return true;
							} else {
								data.getWhoClicked().sendMessage("§cInvalid number.");
								return false;
							}
						}));
						return true;
					}));
					blockMenu.setItem(11, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fDelay: §6" + blockData.getRegenDelay()).build(), data -> {
						data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
						InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
							try {
								long l = Long.parseLong(input);
								blockData.setRegenDelay(l);
								blockMenu.update("menu");
								data.getWhoClicked().sendMessage("§cExiting Input Mode.");
								Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> blockMenu.sendInventory(data.getWhoClicked(), true));
								return true;
							} catch(NumberFormatException e) {
								data.getWhoClicked().sendMessage("§cInvalid number.");
								return false;
							}
						}));
						return true;
					}));
					//TODO Add check if block can store items
					blockMenu.setItem(13, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fSave Items: " + (blockData.doSaveItems() ? "§aTrue" : "§cFalse")).build(), data -> {
						blockData.setReplace(!blockData.doReplace());
						blockMenu.update("menu");
						return true;
					}));
					if(blockData.getRegenData() != null) {
						blockMenu.setItem(17, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LAVA_BUCKET)).setDisplayName("§4§lDelete Block").build(), data -> {
							getBlockSettings().remove(blockData.getRegenData().toString());
							bsMenu.sendInventory(data.getWhoClicked(), true);
							return true;
						}));
					}
				});


				List<String> lore = new ArrayList<>();
				lore.add("§9Prevent Damage: " + (blockData.doPreventDamage() ? "§aTrue" : "§cFalse"));
				lore.add("§9Regenerate: " + (blockData.doRegen() ? "§aTrue" : "§cFalse"));
				lore.add("§9Save Items: " + (blockData.doSaveItems() ? "§aTrue" : "§cFalse"));
				lore.add("§9Replace: ");
				lore.add("§9  Can Replace: " + (blockData.doReplace() ? "§aTrue" : "§cFalse"));
				lore.add("§9  Replace With: §6" + blockData.getReplaceWith().toString());
				lore.add("§9Block Update: " + (blockData.isBlockUpdate() ? "§aTrue" : "§cFalse"));
				lore.add("§9Drop Chance: §6" + blockData.getDropChance());
				lore.add("§9Durability: §6" + blockData.getDurability());
				lore.add("§9Delay: §6" + blockData.getRegenDelay());
				ItemStack blockItem;
				if(blockData.getRegenData() == null)
					blockItem = new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§fDefault").setLore(lore).build();
				else
					blockItem = new ItemBuilder(blockData.getRegenData().getMaterial()).setDisplayName("§f" + blockData.getRegenData().toString()).setLore(lore).build();
				bsMenu.addItem(new SlotElement(blockItem, data -> {
					data.getWhoClicked().openInventory(blockMenu.getInventory());
					return true;
				}));
			}
		});
		pluginMenu.setNextPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aNext Page").build());
		pluginMenu.setPrevPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aBack Page").build());
		conditionMenu.setNextPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aNext Page").build());
		conditionMenu.setPrevPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aBack Page").build());
		overrideMenu.setNextPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aNext Page").build());
		overrideMenu.setPrevPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aBack Page").build());
		pluginMenu.setUpdate("#layout", () -> {
			for(SettingsMenu page : pluginMenu.getPages()) {
				page.setItem(7, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BLACK_STAINED_GLASS_PANE)).setDisplayName(" ").build(), data -> true));
				page.setItem(8, new SlotElement(closeItem, data -> {
					menu.sendInventory(data.getWhoClicked(), true);
					return true;
				}));
				page.setItem(16, new SlotElement(pluginMenu.getPrevPageItem(), data -> true));
				page.setItem(17, new SlotElement(pluginMenu.getNextPageItem(), data -> true));
			}
		});
		conditionMenu.setUpdate("#layout", () -> {
			for(SettingsMenu page : conditionMenu.getPages()) {
				page.setItem(7, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BLACK_STAINED_GLASS_PANE)).setDisplayName(" ").build(), data -> true));
				page.setItem(8, new SlotElement(closeItem, data -> {
					menu.sendInventory(data.getWhoClicked(), true);
					return true;
				}));
				page.setItem(16, new SlotElement(pluginMenu.getPrevPageItem(), data -> true));
				page.setItem(17, new SlotElement(pluginMenu.getNextPageItem(), data -> true));
			}
		});
		overrideMenu.setUpdate("#layout", () -> {
			for(SettingsMenu page : overrideMenu.getPages()) {
				page.setItem(7, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BLACK_STAINED_GLASS_PANE)).setDisplayName(" ").build(), data -> true));
				page.setItem(8, new SlotElement(closeItem, data -> {
					menu.sendInventory(data.getWhoClicked(), true);
					return true;
				}));
				page.setItem(16, new SlotElement(pluginMenu.getPrevPageItem(), data -> true));
				page.setItem(17, new SlotElement(pluginMenu.getNextPageItem(), data -> true));
			}
		});
		pluginMenu.setUpdate("plugins", () -> {
			pluginMenu.clear();
			pluginMenu.update("#layout");
			for(ExplosionSettingsPlugin plugin : plugins.values()) {
				pluginMenu.addItem(new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.FILLED_MAP)).setDisplayName("§a" + plugin.getName()).build(), data -> {
					plugin.getMainMenu().sendInventory(data.getWhoClicked(), true);
					return true;
				}));
			}
		});
		conditionMenu.setUpdate("conditions", () -> {
			conditionMenu.clear();
			conditionMenu.update("#layout");
			for(ExplosionCondition condition : getConditions().getConditions()) {
				conditionMenu.addItem(new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§f" + WordUtils.capitalize(condition.name().toLowerCase().replace("_", " "))).setLine(0, "§9" + WordUtils.capitalize(getConditions().getSimpleConditionValue(condition).toString().replace("_", " "))).build(), data -> {
					if(getConditions().getConditionValue(condition) instanceof Enum) {
						data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
						InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
							try {
								getConditions().setCondition(condition, Enum.valueOf(((Enum<?>) getConditions().getConditionValue(condition)).getDeclaringClass(), input.toUpperCase()));
							} catch(IllegalArgumentException e) {
								data.getWhoClicked().sendMessage("§cInvalid option.");
								return false;
							}
							data.getWhoClicked().sendMessage("§cExiting Input Mode.");
							Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> conditionMenu.sendInventory(data.getWhoClicked(), true));
							return true;
						}));
					} else if(getConditions().getConditionValue(condition) instanceof Boolean) {
						getConditions().setCondition(condition, !(boolean)getConditions().getConditionValue(condition));
						conditionMenu.update("conditions");
					} else if(getConditions().getConditionValue(condition) instanceof Integer || getConditions().getConditionValue(condition) instanceof Double) {
						data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
						InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
							try {
								if(getConditions().getConditionValue(condition) instanceof Integer)
									getConditions().setCondition(condition, Integer.parseInt(input));
								else
									getConditions().setCondition(condition, Double.parseDouble(input));
							} catch(NumberFormatException e) {
								data.getWhoClicked().sendMessage("§cInvalid number.");
								return false;
							}
							data.getWhoClicked().sendMessage("§cExiting Input Mode.");
							Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> conditionMenu.sendInventory(data.getWhoClicked(), true));
							return true;
						}));
					} else {
						data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
						InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
							data.getWhoClicked().sendMessage("§cExiting Input Mode.");
							getConditions().setCondition(condition, input);
							Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> conditionMenu.sendInventory(data.getWhoClicked(), true));
							return true;
						}));
					}
					return true;
				}));
			}
		});
		overrideMenu.setUpdate("override", () -> {
			overrideMenu.clear();
			overrideMenu.update("#layout");
			for(ExplosionSettingsOverride override : getOverrides()) {
				PageMenu oMenu = new PageMenu(override.getName(), 18);
				oMenu.setNextPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aNext Page").build());
				oMenu.setPrevPageItem(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.LIME_STAINED_GLASS_PANE)).setDisplayName("§aBack Page").build());

				overrideMenu.addItem(new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BOOK)).setDisplayName("§f" + override.getName()).build(), data -> {
					oMenu.sendInventory(data.getWhoClicked(), true);
					return true;
				}));
				oMenu.setUpdate("#layout", () -> {
					for(SettingsMenu page : oMenu.getPages()) {
						page.setItem(7, new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.BLACK_STAINED_GLASS_PANE)).setDisplayName(" ").build(), data -> true));
						page.setItem(8, new SlotElement(closeItem, data -> {
							overrideMenu.sendInventory(data.getWhoClicked(), true);
							return true;
						}));
						page.setItem(16, new SlotElement(pluginMenu.getPrevPageItem(), data -> true));
						page.setItem(17, new SlotElement(pluginMenu.getNextPageItem(), data -> true));
					}
				});
				oMenu.setUpdate("override", () -> {
					oMenu.clear();
					oMenu.update("#layout");
					for(ExplosionCondition condition : override.getConditions()) {
						oMenu.addItem(new SlotElement(new ItemBuilder(BukkitAdapter.asBukkitMaterial(FlatMaterial.PAPER)).setDisplayName("§f" + WordUtils.capitalize(condition.name().toLowerCase().replace("_", " "))).setLine(0, "§9" + WordUtils.capitalize(override.getSimpleConditionValue(condition).toString().replace("_", " "))).build(), data -> {
							if(override.getConditionValue(condition) instanceof Enum) {
								data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
								InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
									try {
										override.setCondition(condition, Enum.valueOf(((Enum<?>) override.getConditionValue(condition)).getClass(), input.toUpperCase()));								} catch(IllegalArgumentException e) {
										data.getWhoClicked().sendMessage("§cInvalid option.");
										return false;
									}
									data.getWhoClicked().sendMessage("§cExiting Input Mode.");
									Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> oMenu.sendInventory(data.getWhoClicked(), true));
									return true;
								}));
							} else if(override.getConditionValue(condition) instanceof Boolean) {
								override.setCondition(condition, !(boolean)override.getConditionValue(condition));
								oMenu.update("override");
							} else if(override.getConditionValue(condition) instanceof Integer || override.getConditionValue(condition) instanceof Double) {
								data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
								InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
									try {
										if(override.getConditionValue(condition) instanceof Integer)
											override.setCondition(condition, Integer.parseInt(input));
										else
											override.setCondition(condition, Double.parseDouble(input));
									} catch(NumberFormatException e) {
										data.getWhoClicked().sendMessage("§cInvalid number.");
										return false;
									}
									data.getWhoClicked().sendMessage("§cExiting Input Mode.");
									Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> oMenu.sendInventory(data.getWhoClicked(), true));
									return true;
								}));
							} else {
								data.getWhoClicked().sendMessage("§aEntering Input Mode. Input Value.");
								InputMode.setChatMode((Player) data.getWhoClicked(), new InputMode(input -> {
									override.setCondition(condition, input);
									data.getWhoClicked().sendMessage("§cExiting Input Mode.");
									Bukkit.getScheduler().runTask(ExplosionRegen.getInstance(), () -> oMenu.sendInventory(data.getWhoClicked(), true));
									return true;
								}));
							}
							return true;
						}));
					}
				});
			}
		});
	}

	public void saveAsFile() {
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + name + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		LinkedHashMap<String, Object> map = new LinkedHashMap<>();
		map.put("block-settings", getBlockSettings().getName().toLowerCase());
		map.put("enable", getAllowExplosion());
		map.put("display-name", getDisplayName());
		map.put("display-item", getDisplayItem().getType().name().toLowerCase());
		map.put("regen.allow", getAllowRegen());
		map.put("regen.direction", getRegenerateDirection().name().toLowerCase());
		map.put("regen.instant", isInstantRegen());
		map.put("regen.delay", getRegenDelay());
		map.put("regen.max-block-regen-queue", getMaxBlockRegenQueue());
		map.put("regen.force-block", getRegenForceBlock());
		for(DamageCategory category : DamageCategory.values()) {
			map.put("damage." + category.name().toLowerCase() + ".allow", getAllowDamage(category));
			map.put("damage." + category.name().toLowerCase() + ".modifier", getDamageModifier(category).name().toLowerCase());
			map.put("damage." + category.name().toLowerCase() + ".amount", getDamageAmount(category));
		}
		for(ExplosionSettingsOverride override : getOverrides()) {
			map.put("override." + override.getName() + ".settings", override.getExplosionSettings().getName());
		}
		boolean doSave = false;
		for(Map.Entry<String, Object> entry : new HashSet<>(map.entrySet())) {
			String key = entry.getKey();
			Object value = entry.getValue();
			Object valueKey = config.get(key);

			if(value instanceof Float)
				value = ((Float) value).doubleValue();
			else if(value instanceof Long)
				value = ((Long) value).intValue();

			if(valueKey instanceof Float)
				valueKey = ((Float) valueKey).doubleValue();
			else if(valueKey instanceof Long)
				valueKey = ((Long) valueKey).intValue();
			if(!config.contains(key) || (!valueKey.equals(value) && saveChanges)) {
				config.set(key, value); doSave = true;
			}
			map.remove(key);
		}
		for(ExplosionSettingsPlugin plugin : plugins.values()) {
			for(Map.Entry<String, Object> entry : plugin.getEntries()) {
				String key = plugin.getName().toLowerCase() + "." + entry.getKey();
				Object value = entry.getValue();
				Object valueKey = config.get(key);

				if(value instanceof Float)
					value = ((Float) value).doubleValue();
				else if(value instanceof Long)
					value = ((Long) value).intValue();

				if(valueKey instanceof Float)
					valueKey = ((Float) valueKey).doubleValue();
				else if(valueKey instanceof Long)
					valueKey = ((Long) valueKey).intValue();
				if(!config.contains(key) || !valueKey.equals(value)) {
					config.set(key, value); doSave = true;
				}
			}
		}
		for(ExplosionCondition condition : getConditions().getConditions()) {
			String key = "conditions." + condition.name().toLowerCase();
			Object value = getConditions().getSimpleConditionValue(condition);
			Object valueKey = config.get(key);

			if(value instanceof Float)
				value = ((Float) value).doubleValue();
			else if(value instanceof Long)
				value = ((Long) value).intValue();

			if(valueKey instanceof Float)
				valueKey = ((Float) valueKey).doubleValue();
			else if(valueKey instanceof Long)
				valueKey = ((Long) valueKey).intValue();
			if(!config.contains(key) || !valueKey.equals(value)) {
				config.set(key, value); doSave = true;
			}
			for(String keyy : config.getConfigurationSection("conditions").getKeys(false)) {
				if(!getConditions().hasCondition(ExplosionCondition.valueOf(keyy.toUpperCase()))) {
					config.set(key, null); doSave = true;
				}
			}
		}
		for(ExplosionSettingsOverride override : getOverrides()) {
			for(ExplosionCondition condition : override.getConditions()) {
				String key = "override." + override.getName() + ".conditions." + condition.name().toLowerCase();
				Object value = override.getSimpleConditionValue(condition);
				Object valueKey = config.get(key);

				if(value instanceof Float)
					value = ((Float) value).doubleValue();
				else if(value instanceof Long)
					value = ((Long) value).intValue();

				if(valueKey instanceof Float)
					valueKey = ((Float) valueKey).doubleValue();
				else if(valueKey instanceof Long)
					valueKey = ((Long) valueKey).intValue();
				if(!config.contains(key) || !valueKey.equals(value)) {
					config.set(key, value); doSave = true;
				}
				for(String keyy : config.getConfigurationSection("override." + override.getName() + ".conditions").getKeys(false)) {
					if(!override.hasCondition(ExplosionCondition.valueOf(keyy.toUpperCase()))) {
						config.set(key, null); doSave = true;
					}
				}
			}
		}
		if(doSave) {
			try {
				config.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public String getName() {
		return name;
	}
	public ExplosionSettingsPlugin loadPlugin(Object plugin) {
		return loadPlugin(plugin, plugin.getClass().getName());
	}
	public ExplosionSettingsPlugin loadPlugin(Object plugin, String name) {
		File file = new File(ExplosionRegen.getInstance().getDataFolder() + File.separator + "explosions" + File.separator + getName() + ".yml");
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		name = name.toLowerCase();
		if(config.isConfigurationSection(name)) {
			ConfigurationSection section = config.getConfigurationSection(name);
			ExplosionSettingsPlugin settingsPlugin = new ExplosionSettingsPlugin(plugin);
			for(Map.Entry<String, Object> map : section.getValues(true).entrySet()) {
				if (map.getValue() instanceof MemorySection)
					continue;
				settingsPlugin.setOption(map.getKey(), map.getValue());
			}
			plugins.put(settingsPlugin.getName().toLowerCase(), settingsPlugin);
			Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Loaded plugin '" + settingsPlugin.getName() + "'.");
			return settingsPlugin;
		}
		return null;
	}

	public void addPlugin(ExplosionSettingsPlugin plugin) {
		plugins.put(plugin.getName().toLowerCase(), plugin);
		Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Added plugin '" + plugin.getName() + "'.");
	}

	public ExplosionSettingsPlugin getPlugin(String plugin) {
		return plugins.get(plugin.toLowerCase());
	}

	public BlockSettings getBlockSettings() {
		return blockSettings;
	}

	public void setBlockSettings(BlockSettings blockSettings) {
		this.blockSettings = blockSettings;
		saveChanges = true;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String value) {
		displayName = value;
		saveChanges = true;
	}

	/**
	 *
	 * @return If this explosion should can trigger
	 */
	public boolean getAllowExplosion() {
		return enable;
	}

	/**
	 * Should this explosion explode
	 * @param value
	 */
	public void setAllowExplosion(boolean value) {
		enable = value;
		saveChanges = true;
	}

	public boolean getAllowRegen() {
		return regenAllow;
	}

	public void setAllowRegen(boolean value) {
		regenAllow = value;
		saveChanges = true;
	}

	public GenerateDirection getRegenerateDirection() {
		return regenDirection;
	}

	public void setRegenerateDirection(GenerateDirection direction) {
		regenDirection = direction;
		saveChanges = true;
	}
	
	public boolean isInstantRegen() {
		return regenInstant;
	}

	public void setInstantRegen(boolean value) {
		regenInstant = value;
		saveChanges = true;
	}
	
	public long getRegenDelay() {
		return regenDelay;
	}

	public void setRegenDelay(long value) {
		regenDelay = value;
		saveChanges = true;
	}
	
	public int getMaxBlockRegenQueue() {
		return regenMaxBlockQueue;
	}


	public void setMaxBlockRegenQueue(int value) {
		regenMaxBlockQueue = value;
		saveChanges = true;
	}

	public void setRegenForceBlock(boolean value) {
		this.regenForceBlock = value;
		saveChanges = true;
	}

	public boolean getRegenForceBlock() {
		return regenForceBlock;
	}
	public boolean getAllowDamage(DamageCategory category) {
		if(category == DamageCategory.BLOCK)
			return damageBlockAllow;
		else if(category == DamageCategory.ENTITY)
			return damageEntityAllow;
		return false;
	}

	public void setAllowDamage(DamageCategory category, boolean value) {
		switch(category) {
		case BLOCK:
			damageBlockAllow = value;
			break;
		case ENTITY:
			damageEntityAllow = value;
			break;
		}
		saveChanges = true;
	}
	
	public DamageModifier getDamageModifier(DamageCategory category) {
		if(category == DamageCategory.BLOCK)
			return damageBlockModifier;
		else if(category == DamageCategory.ENTITY)
			return damageEntityModifier;
		return null;
	}

	public void setDamageModifier(DamageCategory category, DamageModifier value) {
		switch(category) {
		case BLOCK:
			damageBlockModifier = value;
			break;
		case ENTITY:
			damageEntityModifier = value;
			break;
		}
		saveChanges = true;
	}

	public double getDamageAmount(DamageCategory category) {
		if(category == DamageCategory.BLOCK)
			return damageBlockAmount;
		else if(category == DamageCategory.ENTITY)
			return damageEntityAmount;
		return 0;
	}
	
	public void setDamageAmount(DamageCategory category, double value) {
		switch(category) {
		case BLOCK:
			damageBlockAmount = value;
			break;
		case ENTITY:
			damageEntityAmount = value;
			break;
		}
		saveChanges = true;
	}
	
	public ItemStack getDisplayItem() {
		return displayItem;
	}

	public void setDisplayItem(ItemStack item) {
		if(item != null && item.getType() != BukkitAdapter.asBukkitMaterial(FlatMaterial.AIR)) {
			displayItem = new ItemBuilder(item).setDisplayName(getDisplayName()).build();
		}
		saveChanges = true;
	}

	public void addOrSetOverride(ExplosionSettingsOverride override) {
		ExplosionSettingsOverride newOverride;
		if(overrides.containsKey(override.getName())) {
			newOverride = overrides.get(override.getName());
			for(ExplosionCondition condition : override.getConditions())
				newOverride.setCondition(condition, override.getConditionValue(condition));
		} else {
			newOverride = override;
			overrides.put(newOverride.getName(), newOverride);
		}
		saveChanges = true;
	}

	public void addOrSetCondition(ExplosionSettingsOverride override) {
		for(ExplosionCondition condition : override.getConditions())
			conditions.setCondition(condition, override.getConditionValue(condition));
		saveChanges = true;
	}

	public void removeOverride(String name) {
		overrides.remove(name);
		saveChanges = true;
	}
	public void removeCondition(ExplosionCondition condition) {
		conditions.removeCondition(condition);
		saveChanges = true;
	}
	
	public Collection<ExplosionSettingsOverride> getOverrides() {
		return overrides.values();
	}
	
	public ExplosionSettingsOverride getConditions() {
		return conditions;
	}

	public SettingsMenu getSettingsMenu() {
		return menu;
	}

	public static ExplosionSettings registerSettings(String name) {
		return registerSettings(name, BlockSettings.getBlockSettings().iterator().next());
	}
	public static ExplosionSettings registerSettings(String name, BlockSettings blockSettings) {
		if(getSettings(name) != null)
			return getSettings(name);
		ExplosionSettings settings = new ExplosionSettings(name, blockSettings);
		Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Registered Explosion Settings for '" + name + "' using '" + settings.getBlockSettings().getName() + "' block settings.");
		return settings;
	}

	public static ExplosionSettings registerSettings(File file) throws IOException {
		ExplosionSettings settings;
		String name = file.getName().substring(0, file.getName().length()-4);
		if(file.exists()) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
			String blockSettings = config.getString("block-settings", "default");
			settings = registerSettings(name, BlockSettings.getSettings(blockSettings));
			settings.setAllowExplosion(config.getBoolean("enable", settings.getAllowExplosion()));
			settings.setDisplayName(config.getString("display-name", settings.getDisplayName()));
			settings.setDisplayItem(new ItemStack(Material.valueOf(config.getString("display-item", settings.getDisplayItem().getType().name()).toUpperCase())));
			settings.setAllowRegen(config.getBoolean("regen.allow", settings.getAllowRegen()));
			settings.setRegenerateDirection(GenerateDirection.valueOf(config.getString("regen.direction", settings.getRegenerateDirection().name()).toUpperCase()));
			settings.setInstantRegen(config.getBoolean("regen.instant", settings.isInstantRegen()));
			settings.setRegenDelay(config.getLong("regen.delay", settings.getRegenDelay()));
			settings.setMaxBlockRegenQueue(config.getInt("regen.max-block-regen-queue", settings.getMaxBlockRegenQueue()));
			settings.setRegenForceBlock(config.getBoolean("regen.force-block", settings.getRegenForceBlock()));
			for(DamageCategory category : DamageCategory.values()) {
				settings.setAllowDamage(category, config.getBoolean("damage." + category.name().toLowerCase() + ".allow", settings.getAllowDamage(category)));
				settings.setDamageModifier(category, DamageModifier.valueOf(config.getString("damage." + category.name().toLowerCase() + ".modifier", settings.getDamageModifier(category).name()).toUpperCase()));
				settings.setDamageAmount(category, config.getDouble("damage." + category.name().toLowerCase() + ".amount", settings.getDamageAmount(category)));
			}

			if(config.isConfigurationSection("conditions")) {
				ExplosionSettingsOverride override = settings.getConditions();
				for(String key : config.getConfigurationSection("conditions").getKeys(false)) {
					ExplosionCondition condition = ExplosionCondition.valueOf(key.toUpperCase());
					Object value = null;
					switch(condition) {
						case CUSTOM_NAME:
							value = config.get("conditions." + key);
							break;
						case ENTITY:
							value = EntityType.valueOf(config.getString("conditions." + key).toUpperCase());
							break;
						case BLOCK:
							value = Material.getMaterial(config.getString("conditions." + key).toUpperCase());
							break;
						case IS_CHARGED:
							value = config.getBoolean("conditions." + key);
							break;
						case WEATHER:
							value = WeatherType.valueOf(config.getString("conditions." + key).toUpperCase());
							break;
						case WORLD:
							value = Bukkit.getWorld(config.getString("conditions." + key));
							break;
						case MINX:
						case MAXX:
						case MINY:
						case MAXY:
						case MINZ:
						case MAXZ:
							value = config.getDouble("conditions." + key);
					}
					override.setCondition(condition, value);
				}
				settings.addOrSetCondition(override);
			}
			if(config.isConfigurationSection("override")) {
				for(String key : config.getConfigurationSection("override").getKeys(false)) {
					ExplosionSettingsOverride override = new ExplosionSettingsOverride(key, ExplosionSettings.registerSettings(config.getString("override." + key + ".settings")));//addOverride(key, t);
					for(String k : config.getConfigurationSection("override." + key + ".conditions").getKeys(false)) {
						ExplosionCondition condition = ExplosionCondition.valueOf(k.toUpperCase());
						Object value = null;
						switch(condition) {
							case CUSTOM_NAME:
								value = config.get("override." + key + ".conditions." + k);
								break;
							case ENTITY:
								value = EntityType.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
								break;
							case BLOCK:
								value = Material.getMaterial(config.getString("override." + key + ".conditions." + k).toUpperCase());
								break;
							case IS_CHARGED:
								value = config.getBoolean("override." + key + ".conditions." + k);
								break;
							case WEATHER:
								value = WeatherType.valueOf(config.getString("override." + key + ".conditions." + k).toUpperCase());
								break;
							case WORLD:
								value = Bukkit.getWorld(config.getString("override." + key + ".conditions." + k));
								break;
							case MINX:
							case MAXX:
							case MINY:
							case MAXY:
							case MINZ:
							case MAXZ:
								value = config.getDouble("override." + key + ".conditions." + k);
						}
						override.setCondition(condition, value);
					}
					settings.addOrSetOverride(override);
				}
			}
			settings.saveChanges = false;
			return settings;
		} else {
			file.createNewFile();
			return registerSettings(name, BlockSettings.getSettings("default"));
		}
	}
	public static ExplosionSettings registerSettings(File file, BlockSettings blockSettings) throws IOException {
		ExplosionSettings settings = registerSettings(file);
		settings.setBlockSettings(blockSettings);
		return settings;
	}

	public static void removeSettings(String name) {
		if(getSettings(name) != null) {
			ExplosionSettingsUnloadEvent event = new ExplosionSettingsUnloadEvent(getSettings(name));
			Bukkit.getPluginManager().callEvent(event);
			BlockSettings.removeSettings(getSettings(name).getBlockSettings().getName());
			MAP.remove(name);
			Bukkit.getConsoleSender().sendMessage("[ExplosionRegen] Removed Explosion Settings '" + name + "'.");
		}
	}
	
	public static Collection<ExplosionSettings> getRegisteredSettings() {
		return MAP.values();
	}
	public static ExplosionSettings getSettings(String name) {
		return MAP.get(name.toLowerCase());
	}
}
