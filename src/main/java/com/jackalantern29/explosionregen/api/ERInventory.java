package com.jackalantern29.explosionregen.api;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.enums.ERSettingsDamageCategory;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.enums.ParticleType;
import de.themoep.inventorygui.*;
import de.themoep.inventorygui.GuiElement.Action;
import de.themoep.inventorygui.GuiPageElement.PageAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.xenondevs.particle.ParticleEffect;

import java.util.*;

public class ERInventory {
	private static final List<ERInventory> inventories = new ArrayList<>();
	private final UUID uuid;
	
	private final String[] setupRows = {
			"qwertiyu0",
			"ppppppppd",
			"ppppppppz",
			"ppppppppn",
			"ppppppppm",
			"ppppppppv",
	};
	private InventoryGui explosionMenu = null;
	private final InventoryGui optionMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Option", new String[] {"0 wer"});
	private final InventoryGui vanillaParticleMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Particle", setupRows);
	private final InventoryGui presetParticleMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Particle", setupRows);
	private final InventoryGui soundMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Sound", setupRows);
	private final InventoryGui settingsMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Settings §6§l✯", new String[] {"0 qwert y", "   u i   "});
	
	private ExplosionPhase selectedPhase = ExplosionPhase.BLOCK_REGENERATING;
	private ExplosionSettings selectedSettings = null;
	
	private ERInventory(UUID uuid) {
		this.uuid = uuid;
		inventories.add(this);
	}
	@SuppressWarnings("deprecation")
	public void openSettings(Player player, boolean isServer) {
		if(explosionMenu == null) {
			String[] rows;
			if(ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().size() <= 5)
				rows = new String[] {"qqqqq"};
			else {
				int size = (ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().size() > 54 ? 6 : ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings().size() % 9);
				rows = new String[size];
				for (int i = 0; i < size; i++) {
					rows[i] = "qqqqqqqqq";
				}
			}
			explosionMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Explosion", rows);
		}
		//InventoryGui menu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Explosion" + (isServer == true ? " §6§l✯" : ""), guiSetup);
		GuiBackElement backElement = new GuiBackElement('0', XMaterial.BARRIER.parseItem(), "§c§lGo Back");
		
		explosionMenu.setTitle("§2Select Explosion" + (isServer ? " §6§l✯" : ""));
		optionMenu.setTitle("§2Select Option" + (isServer ? " §6§l✯" : ""));
		vanillaParticleMenu.setTitle("§2Select Particle" + (isServer ? " §6§l✯" : ""));
		presetParticleMenu.setTitle("§2Select Particle" + (isServer ? " §6§l✯" : ""));
		soundMenu.setTitle("§2Select Sound" + (isServer ? " §6§l✯" : ""));
		
		ItemStack filler = XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE.parseItem();
		explosionMenu.setFiller(filler);
		optionMenu.setFiller(filler);
		vanillaParticleMenu.setFiller(filler);
		presetParticleMenu.setFiller(filler);
		soundMenu.setFiller(filler);
		settingsMenu.setFiller(filler);
		
		explosionMenu.setCloseAction(click -> false);
		optionMenu.setCloseAction(click -> false);
		vanillaParticleMenu.setCloseAction(click -> false);
		presetParticleMenu.setCloseAction(click -> false);
		soundMenu.setCloseAction(click -> false);
		settingsMenu.setCloseAction(click -> false);
		
		optionMenu.addElement(backElement);
		vanillaParticleMenu.addElement(backElement);
		presetParticleMenu.addElement(backElement);
		soundMenu.addElement(backElement);
		settingsMenu.addElement(backElement);
		
		GuiElementGroup explosionGroup = new GuiElementGroup('q');
		for(ExplosionSettings settings : ERProfileSettings.get(player.getUniqueId()).getConfigurableSettings()) {
			explosionGroup.addElement(new DynamicGuiElement('q', () -> new StaticGuiElement('q', settings.getDisplayItem(), click -> {selectedSettings = settings; optionMenu.show(click.getEvent().getWhoClicked()); return true;}, settings.getDisplayName())));
		}
		explosionMenu.addElement(explosionGroup);
		{
			optionMenu.addElement(new DynamicGuiElement('w', () -> {
				if(player.hasPermission("explosionregen.command.rsettings.particles"))
					return new StaticGuiElement('w', XMaterial.NETHER_STAR.parseItem(), click -> {
						selectedPhase = ExplosionPhase.BLOCK_REGENERATING;
						if(selectedSettings.getParticleType() == ParticleType.VANILLA) {
							vanillaParticleMenu.setPageNumber(0);
							vanillaParticleMenu.show(click.getEvent().getWhoClicked()); 							
						} else if(selectedSettings.getParticleType() == ParticleType.PRESET) {
							presetParticleMenu.setPageNumber(0);
							presetParticleMenu.show(click.getEvent().getWhoClicked());							
						}
						return true;
					}, "§a§lParticles");
				else
					return optionMenu.getFiller();
			}));
			optionMenu.addElement(new DynamicGuiElement('e', () -> {
				if(player.hasPermission("explosionregen.command.rsettings.sounds"))
					return new StaticGuiElement('e', XMaterial.NOTE_BLOCK.parseItem(), click -> {selectedPhase = ExplosionPhase.BLOCK_REGENERATING;soundMenu.setPageNumber(0);soundMenu.show(click.getEvent().getWhoClicked()); return true;}, "§a§lSounds");
				else
					return optionMenu.getFiller();
			}));
			optionMenu.addElement(new DynamicGuiElement('r', () -> {
				if(isServer && player.hasPermission("explosionregen.command.rsettings.settings"))
					return new StaticGuiElement('r', XMaterial.PAPER.parseItem(), click -> {selectedPhase = ExplosionPhase.BLOCK_REGENERATING;settingsMenu.show(click.getEvent().getWhoClicked()); return true;}, "§a§lSettings");
				else
					return optionMenu.getFiller();
			}));
			
			for(int i = 0; i < 3; i++) {
				InventoryGui g = i == 0 ? vanillaParticleMenu : i == 1 ? soundMenu : presetParticleMenu;
				final int fi = i;
				g.addElement(new DynamicGuiElement('t', () -> {
					String[] toggleLore = new String[5];
					toggleLore[0] = "§aToggle " + (fi == 0 ? "Particle" : "Sound");
					for (int ii = 1; ii < toggleLore.length; ii++) {
						toggleLore[ii] = (selectedPhase == ExplosionPhase.values()[ii-1] ? "§b" : "§7") + 
								StringUtils.capitaliseAllWords(ExplosionPhase.values()[ii-1].toString().replace("-", " ")) + 
								": " + 
								((isServer ? (fi == 0 ? selectedSettings.getParticleSettings(ParticleType.VANILLA).getParticles(ExplosionPhase.values()[ii-1]).get(0).getCanDisplay() : selectedSettings.getAllowSound(ExplosionPhase.values()[ii-1])) :
									(fi == 0 ? ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA).getParticles(ExplosionPhase.values()[ii-1]).get(0).getCanDisplay() : ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getAllowSound(ExplosionPhase.values()[ii-1]))) ? "§aTrue" : "§cFalse");
					}
					Action click;
					if(fi == 0) {
						click = click1 -> {
							ParticleSettings pSettings = selectedSettings.getParticleSettings(ParticleType.VANILLA);
							pSettings.getParticles(selectedPhase).get(0).setCanDisplay(!selectedSettings.getParticleSettings(ParticleType.VANILLA).getParticles(selectedPhase).get(0).getCanDisplay());
							selectedSettings.setParticleSettings(ParticleType.VANILLA, pSettings);
							g.draw(click1.getEvent().getWhoClicked());
							return true;
						};
					} else if(fi == 1) {
						click = click12 -> {
							selectedSettings.setAllowSound(selectedPhase, !selectedSettings.getAllowSound(selectedPhase));
							g.draw(click12.getEvent().getWhoClicked());
							return true;
						};
					} else {
						click = click13 -> {
							//settings.setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase())));
							g.draw(click13.getEvent().getWhoClicked());
							return true;
						};
					}
					return new StaticGuiElement('t', XMaterial.COMPASS.parseItem(), click, toggleLore);
				}));
				g.addElement(new DynamicGuiElement('i', () -> new StaticGuiElement('i', XMaterial.CLOCK.parseItem(), click -> {
					ParticleType type = selectedSettings.getParticleType();
					selectedSettings.setParticleType(type == ParticleType.VANILLA ? ParticleType.PRESET : ParticleType.VANILLA );
					if(type == ParticleType.VANILLA)
						presetParticleMenu.show(player);
					else
						vanillaParticleMenu.show(player);
					return true;
				}, "§aSwitch Particle Type", "§7Current Type: §6" + StringUtils.capitalise(selectedSettings.getParticleType().name().toLowerCase()))));
				GuiElementGroup listGroup = new GuiElementGroup('p');
				if(fi == 0) {
					List<ParticleEffect> keys = new ArrayList<>(ParticleEffect.NMS_EFFECTS.keySet());
					keys.sort(Comparator.comparing(ParticleEffect::name));
					for(ParticleEffect particles : keys) {
						if(!player.hasPermission("explosionregen.command.rsettings.particles." + particles.name().toLowerCase()))
							continue;
						listGroup.addElement(new DynamicGuiElement('l', () -> {
							ItemStack item = XMaterial.GRAY_DYE.parseItem();
							ChatColor color = ChatColor.GRAY;
							for(ExplosionPhase phase : ExplosionPhase.values()) {
								if((isServer ? selectedSettings.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle(): ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle()).equals(particles)) {
									switch(phase) {
									case BLOCK_REGENERATING:
										item = XMaterial.LIME_DYE.parseItem();
										color = ChatColor.GREEN;
										break;
									case ON_BLOCK_REGEN:
										item = XMaterial.LIGHT_BLUE_DYE.parseItem();
										color = ChatColor.AQUA;
										break;
									case ON_EXPLODE:
										item = XMaterial.PURPLE_DYE.parseItem();
										color = ChatColor.DARK_PURPLE;
										break;
									case EXPLOSION_FINISHED_REGEN:
										item = XMaterial.PINK_DYE.parseItem();
										color = ChatColor.LIGHT_PURPLE;
										break;
									}
								}
							}
							return new StaticGuiElement('l', item, click -> {
								if(isServer) {
									ParticleSettings pSettings = selectedSettings.getParticleSettings(ParticleType.VANILLA);
									boolean display = pSettings.getParticles(selectedPhase).get(0).getCanDisplay();
									pSettings.clearParticles(selectedPhase);
									pSettings.addParticles(ParticleData.getVanillaSettings(ParticleEffect.valueOf(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase()))).clone(selectedPhase, display));
									selectedSettings.setParticleSettings(ParticleType.VANILLA, pSettings);
								} else {
									ParticleSettings pSettings = ERProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA);
									boolean display = pSettings.getParticles(selectedPhase).get(0).getCanDisplay();
									pSettings.clearParticles(selectedPhase);
									pSettings.addParticles(ParticleData.getVanillaSettings(ParticleEffect.valueOf(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase()))).clone(selectedPhase, display));									
									ERProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).setParticleSettings(ParticleType.VANILLA, pSettings);
								}
								g.draw(click.getEvent().getWhoClicked()); return true;
							}, color + StringUtils.capitaliseAllWords(particles.toString().toLowerCase().replace("_", " ")));
						}));
					}
				} else if(fi == 1) {
					for(XSound sounds : XSound.VALUES) {
						if(!sounds.isSupported() || !player.hasPermission("explosionregen.command.rsettings.sounds." + sounds.name().toLowerCase()))
							continue;
						listGroup.addElement(new DynamicGuiElement('l', () -> {
							ItemStack item = XMaterial.MUSIC_DISC_STAL.parseItem();
							ChatColor color = ChatColor.GRAY;
							switch(sounds.name().split("_")[0]) {
							case "AMBIENT":
								item = XMaterial.MUSIC_DISC_13.parseItem();
								break;
							case "BLOCK":
								item = XMaterial.MUSIC_DISC_BLOCKS.parseItem();
								break;
							case "ENCHANT":
								item = XMaterial.MUSIC_DISC_CHIRP.parseItem();
								break;
							case "ENTITY":
								item = XMaterial.MUSIC_DISC_CAT.parseItem();
								break;
							case "ITEM":
								item = XMaterial.MUSIC_DISC_FAR.parseItem();
								break;
							case "EVENT":
								item = XMaterial.MUSIC_DISC_WAIT.parseItem();
								break;
							case "MUSIC":
								item = XMaterial.MUSIC_DISC_STRAD.parseItem();
								break;
							case "PARTICLE":
								item = XMaterial.MUSIC_DISC_MELLOHI.parseItem();
								break;
							case "UI":
								item = XMaterial.MUSIC_DISC_STAL.parseItem();
								break;
							case "WEATHER":
								item = XMaterial.MUSIC_DISC_MALL.parseItem();
								break;
							}	
							for(ExplosionPhase cat : ExplosionPhase.values()) {
								if((isServer ? selectedSettings.getSoundSettings().getSound(cat).getSound() : ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getSound(cat).getSound()) == sounds) {
									item = XMaterial.NOTE_BLOCK.parseItem();
									color = ChatColor.AQUA;
								}
							}
							return new StaticGuiElement('l', item, click -> {
								SoundData data = selectedSettings.getSoundSettings().getSound(selectedPhase);
								data.setSound(sounds); 
								if(isServer)
									selectedSettings.getSoundSettings().setSound(selectedPhase, data);
								else
									ERProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).setSound(selectedPhase, data);
								g.draw(click.getEvent().getWhoClicked()); return true;}, color + StringUtils.capitaliseAllWords(sounds.toString().toLowerCase().replace("_", " ")));
						}));
					}
				} else {
					List<ParticleSettings> keys = new ArrayList<>();
					for(ParticleSettings particle : ParticleSettings.getSettingsList())
							if(particle.getName() != null)
								keys.add(particle);
					keys.sort(Comparator.comparing(ParticleSettings::getName));
					for(ParticleSettings particles : keys) {
						if(!player.hasPermission("explosionregen.command.rsettings.particles." + particles.getName().toLowerCase()))
							continue;
						listGroup.addElement(new DynamicGuiElement('l', () -> {
							ItemStack item = XMaterial.BOOK.parseItem();
							ChatColor color = ChatColor.GRAY;
							if(isServer) {
								if(selectedSettings.getParticleSettings(ParticleType.PRESET).getName() != null && selectedSettings.getParticleSettings(ParticleType.PRESET).getName().equals(particles.getName())) {
									item = XMaterial.WRITTEN_BOOK.parseItem();
									ItemMeta meta = Objects.requireNonNull(item).getItemMeta();
									meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
									item.setItemMeta(meta);
									color = ChatColor.GOLD;
								}
							} else {
								if(ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.PRESET) != null && selectedSettings.getParticleSettings(ParticleType.PRESET).getName().equals(particles.getName())) {
									item = XMaterial.WRITTEN_BOOK.parseItem();
									color = ChatColor.GOLD;
									ItemMeta meta = Objects.requireNonNull(item).getItemMeta();
									meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
									item.setItemMeta(meta);
								}
							}
							return new StaticGuiElement('l', item, click -> {
								if(isServer)
									selectedSettings.setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase())));
								else
									ERProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase())));
								g.draw(click.getEvent().getWhoClicked()); return true;
							}, color + StringUtils.capitaliseAllWords(particles.getName().toLowerCase().replace("_", " ")), "§7by " + particles.getAuthor());
						}));
					}
				}
				g.addElement(listGroup);
				for(ExplosionPhase phase : ExplosionPhase.values()) {
					ItemStack item;
					String name;
					char cc;
					switch(phase) {
					case BLOCK_REGENERATING:
						item = XMaterial.LIME_DYE.parseItem();
						name = "Block Regenerating";
						cc = 'q';
						break;
					case ON_BLOCK_REGEN:
						item = XMaterial.LIGHT_BLUE_DYE.parseItem();
						name = "On Block Regen";
						cc = 'w';
						break;
					case ON_EXPLODE:
						item = XMaterial.PURPLE_DYE.parseItem();
						name = "On Explode";
						cc = 'e';
						break;
					case EXPLOSION_FINISHED_REGEN:
						item = XMaterial.PINK_DYE.parseItem();
						name = "Explosion Finished Regen";
						cc = 'r';
						break;
					default:
						cc = 0;
						item = null;
						name = "";
					}
					final char fcc = cc;
					g.addElement(new DynamicGuiElement(cc, () -> {
						String fName;
						if(selectedPhase == phase) {
							addFakeEnchant(Objects.requireNonNull(item));
							fName = "§b" + name;
							
						} else {
							removeFakeEnchant(Objects.requireNonNull(item));
							fName = "§7" + name;
						}
						String lore = "§7Selected " + (fi == 0 ? "Particle" : "Sound") + " Settings: §6" + (fi == 0 ? StringUtils.capitaliseAllWords(isServer ? selectedSettings.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().name().toLowerCase() : ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().name().toLowerCase()) : StringUtils.capitaliseAllWords(isServer ? selectedSettings.getSoundSettings().getSound(phase).getSound().toString().toLowerCase().replace("_", " ") : ERProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getSound(phase).getSound().toString().toLowerCase().replace("_", " ")));
						return new StaticGuiElement(fcc, item, click -> {selectedPhase = ExplosionPhase.valueOf(fName.substring(2).replace(" ", "_").toUpperCase()); g.draw(click.getEvent().getWhoClicked()); return true;}, fName, lore);
					}));
				}
				g.addElement(new GuiPageElement('z', XMaterial.LIME_STAINED_GLASS_PANE.parseItem(), PageAction.PREVIOUS, "§7Previous"));
				g.addElement(new GuiPageElement('v', XMaterial.LIME_STAINED_GLASS_PANE.parseItem(), PageAction.NEXT, "§7Next"));
			}	
			settingsMenu.addElement(new DynamicGuiElement('q', () -> new StaticGuiElement('q', XMaterial.RED_DYE.parseItem(), click -> true,
					"§aRegen",
					"§7Allow: §6" + StringUtils.capitalize("" + selectedSettings.getAllowRegen()),
					"§7Instant: §6" + StringUtils.capitalize("" + selectedSettings.isInstantRegen()),
					"§7Delay: §6" + StringUtils.capitalize("" + selectedSettings.getRegenDelay()),
					"§7Block Delay: §6" + StringUtils.capitalize("" + selectedSettings.getBlockRegenDelay()),
					"§7Max Block Regen Queue: §6" + StringUtils.capitalize("" + selectedSettings.getMaxBlockRegenQueue()),
					"§7Directions: §6" + StringUtils.capitalize("" + selectedSettings.getRegenerateDirections()))));
			settingsMenu.addElement(new StaticGuiElement('w', XMaterial.ANVIL.parseItem(), click -> true, "§aConditions", "§c§lNot Yet Implemented."));
			settingsMenu.addElement(new DynamicGuiElement('e', () -> new StaticGuiElement('e', XMaterial.COMPASS.parseItem(), click -> {selectedSettings.setAllowExplosion(!selectedSettings.getAllowExplosion()); settingsMenu.draw(); return true;}, "§aAllow Explosion: " + (selectedSettings.getAllowExplosion() ? "§aTrue" : "§cFalse"))));
			settingsMenu.addElement(new DynamicGuiElement('r', () -> new StaticGuiElement('r', XMaterial.CHEST.parseItem(), click -> true, "§aConfigure Block Settings", "§7Selected Settings: §6" + StringUtils.capitaliseAllWords(selectedSettings.getBlockSettings().getName()))));
			settingsMenu.addElement(new DynamicGuiElement('t', () -> {
				List<String> lore = new ArrayList<>();
				lore.add("§aConfigure Damage Settings");
				for(ERSettingsDamageCategory types : ERSettingsDamageCategory.values()) {
					lore.add("§7" + StringUtils.capitalise(types.name().toLowerCase()));
					lore.add("  §7Allow: §6" + StringUtils.capitalise(selectedSettings.allowDamage(types) + ""));
					lore.add("  §7Power: §6" + selectedSettings.getDamageAmount(types));
					lore.add("  §7Modifier Type: §6" + StringUtils.capitalise(selectedSettings.getDamageModifier(types).name().toLowerCase()));
					lore.add("  §7Amount: §6" + selectedSettings.getDamageAmount(types));
				}
				return new StaticGuiElement('t', XMaterial.FIRE_CHARGE.parseItem(), click -> true, lore.toArray(new String[0]));
			}));
			settingsMenu.addElement(new StaticGuiElement('y', XMaterial.MAP.parseItem(), click -> true, "§aCreate New Override", "§c§lNot Yet Implemented."));
			settingsMenu.addElement(new DynamicGuiElement('u', () -> new StaticGuiElement('u', XMaterial.PAPER.parseItem(), click -> true, "§aChange Display Name", "§7Current Name: §6" + StringUtils.capitaliseAllWords(selectedSettings.getDisplayName()))));
			settingsMenu.addElement(new DynamicGuiElement('i', () -> new StaticGuiElement('i', selectedSettings.getDisplayItem(), click -> {selectedSettings.setDisplayItem(click.getEvent().getCurrentItem());settingsMenu.draw(); return true;}, "§aChange Display Item", "§7Replace this item to change the Display Item.")));
		}
		explosionMenu.addElement(explosionGroup);
		explosionMenu.addElement('e', XMaterial.BLACK_STAINED_GLASS_PANE.parseItem(), click -> true, " ");
		explosionMenu.build();
		explosionMenu.show(player);
	}
	public static ERInventory get(UUID uuid) {
		for(ERInventory inv : inventories)
			if(inv.uuid == uuid)
				return inv;
		return new ERInventory(uuid);
	}
	private void addFakeEnchant(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.addEnchant(Enchantment.PROTECTION_EXPLOSIONS, 1, true);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
	}
	private void removeFakeEnchant(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		meta.removeEnchant(Enchantment.PROTECTION_EXPLOSIONS);
		meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
	}
}
