package com.jackalantern29.explosionregen.api;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.MaterialUtil;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.enums.ParticleType;
import de.themoep.inventorygui.*;
import de.themoep.inventorygui.GuiElement.Action;
import de.themoep.inventorygui.GuiPageElement.PageAction;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InventorySettings {
	private static final List<InventorySettings> inventories = new ArrayList<>();
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
	private final InventoryGui optionMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Option", new String[] {"0  we"});
	private final InventoryGui vanillaParticleMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Particle", setupRows);
	private final InventoryGui presetParticleMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Particle", setupRows);
	private final InventoryGui soundMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Sound", setupRows);
	
	private ExplosionPhase selectedPhase = ExplosionPhase.BLOCK_REGENERATING;
	private ExplosionSettings selectedSettings = null;
	
	private InventorySettings(UUID uuid) {
		this.uuid = uuid;
		inventories.add(this);
	}
	@SuppressWarnings("deprecation")
	public void openSettings(Player player, boolean isServer) {
		if(explosionMenu == null) {
			String[] rows;
			if(ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().size() <= 5)
				rows = new String[] {"qqqqq"};
			else {
				int size = (ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().size() > 54 ? 6 : ProfileSettings.get(player.getUniqueId()).getConfigurableSettings().size() % 9);
				rows = new String[size];
				for (int i = 0; i < size; i++) {
					rows[i] = "qqqqqqqqq";
				}
			}
			explosionMenu = new InventoryGui(ExplosionRegen.getInstance(), "§2Select Explosion", rows);
		}
		GuiBackElement backElement = new GuiBackElement('0', MaterialUtil.parseItemStack("BARRIER"), "§c§lGo Back");
		
		explosionMenu.setTitle("§2Select Explosion" + (isServer ? " §6§l✯" : ""));
		optionMenu.setTitle("§2Select Option" + (isServer ? " §6§l✯" : ""));
		vanillaParticleMenu.setTitle("§2Select Particle" + (isServer ? " §6§l✯" : ""));
		presetParticleMenu.setTitle("§2Select Particle" + (isServer ? " §6§l✯" : ""));
		soundMenu.setTitle("§2Select Sound" + (isServer ? " §6§l✯" : ""));
		ItemStack filler = MaterialUtil.parseItemStack("LIGHT_GRAY_STAINED_GLASS_PANE");
		explosionMenu.setFiller(filler);
		optionMenu.setFiller(filler);
		vanillaParticleMenu.setFiller(filler);
		presetParticleMenu.setFiller(filler);
		soundMenu.setFiller(filler);

		explosionMenu.setCloseAction(click -> false);
		optionMenu.setCloseAction(click -> false);
		vanillaParticleMenu.setCloseAction(click -> false);
		presetParticleMenu.setCloseAction(click -> false);
		soundMenu.setCloseAction(click -> false);

		optionMenu.addElement(backElement);
		vanillaParticleMenu.addElement(backElement);
		presetParticleMenu.addElement(backElement);
		soundMenu.addElement(backElement);

		GuiElementGroup explosionGroup = new GuiElementGroup('q');
		for(ExplosionSettings settings : ProfileSettings.get(player.getUniqueId()).getConfigurableSettings()) {
			explosionGroup.addElement(new DynamicGuiElement('q', () -> new StaticGuiElement('q', settings.getDisplayItem(), click -> {selectedSettings = settings; optionMenu.show(click.getEvent().getWhoClicked()); return true;}, settings.getDisplayName())));
		}
		explosionMenu.addElement(explosionGroup);
		{
			optionMenu.addElement(new DynamicGuiElement('w', () -> {
				if(player.hasPermission("explosionregen.command.rsettings.particles"))
					return new StaticGuiElement('w', MaterialUtil.parseItemStack("NETHER_STAR"), click -> {
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
					return new StaticGuiElement('e', MaterialUtil.parseItemStack("NOTE_BLOCK"), click -> {selectedPhase = ExplosionPhase.BLOCK_REGENERATING;soundMenu.setPageNumber(0);soundMenu.show(click.getEvent().getWhoClicked()); return true;}, "§a§lSounds");
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
									(fi == 0 ? ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA).getParticles(ExplosionPhase.values()[ii-1]).get(0).getCanDisplay() : ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getAllowSound(ExplosionPhase.values()[ii-1]))) ? "§aTrue" : "§cFalse");
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
					return new StaticGuiElement('t', MaterialUtil.parseItemStack("COMPASS"), click, toggleLore);
				}));
				g.addElement(new DynamicGuiElement('i', () -> new StaticGuiElement('i', MaterialUtil.parseItemStack("CLOCK"), click -> {
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
					List<ExplosionParticle> keys = Arrays.asList(ExplosionParticle.getParticles());
					keys.sort(Comparator.comparing(ExplosionParticle::toString));
					for(ExplosionParticle particles : keys) {
						if(!player.hasPermission("explosionregen.command.rsettings.particles." + particles.toString().toLowerCase()))
							continue;
						listGroup.addElement(new DynamicGuiElement('l', () -> {
							ItemStack item = MaterialUtil.parseItemStack("GRAY_DYE");
							ChatColor color = ChatColor.GRAY;
							for(ExplosionPhase phase : ExplosionPhase.values()) {
								if((isServer ? selectedSettings.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle(): ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle()).equals(particles)) {
									switch(phase) {
									case BLOCK_REGENERATING:
										item = MaterialUtil.parseItemStack("LIME_DYE");
										color = ChatColor.GREEN;
										break;
									case ON_BLOCK_REGEN:
										item = MaterialUtil.parseItemStack("LIGHT_BLUE");
										color = ChatColor.AQUA;
										break;
									case ON_EXPLODE:
										item = MaterialUtil.parseItemStack("PURPLE_DYE");
										color = ChatColor.DARK_PURPLE;
										break;
									case EXPLOSION_FINISHED_REGEN:
										item = MaterialUtil.parseItemStack("PINK_DYE");
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
									pSettings.addParticles(ParticleData.getVanillaSettings(ExplosionParticle.getParticle(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase()))).clone(selectedPhase, display));
									selectedSettings.setParticleSettings(ParticleType.VANILLA, pSettings);
								} else {
									ParticleSettings pSettings = ProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA);
									boolean display = pSettings.getParticles(selectedPhase).get(0).getCanDisplay();
									pSettings.clearParticles(selectedPhase);
									pSettings.addParticles(ParticleData.getVanillaSettings(ExplosionParticle.getParticle(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase()))).clone(selectedPhase, display));
									ProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).setParticleSettings(ParticleType.VANILLA, pSettings);
								}
								g.draw(click.getEvent().getWhoClicked()); return true;
							}, color + StringUtils.capitaliseAllWords(particles.toString().toLowerCase().replace("_", " ")));
						}));
					}
				} else if(fi == 1) {
					for(Sound sounds : Sound.values()) {
						if(!player.hasPermission("explosionregen.command.rsettings.sounds." + sounds.name().toLowerCase()))
							continue;
						listGroup.addElement(new DynamicGuiElement('l', () -> {
							ItemStack item = new ItemStack(Material.getMaterial("MUSIC_DISC_STAL") != null ? Material.getMaterial("MUSIC_DISC_STAL") : Material.getMaterial("RECORD_9"));
							ChatColor color = ChatColor.GRAY;
							switch(sounds.name().split("_")[0]) {
							case "AMBIENT":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_13") != null ? Material.getMaterial("MUSIC_DISC_13") : Material.getMaterial("GREEN_RECORD"));
								break;
							case "BLOCK":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_BLOCKS") != null ? Material.getMaterial("MUSIC_DISC_BLOCKS") : Material.getMaterial("RECORD_3"));
								break;
							case "ENCHANT":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_CHIRP") != null ? Material.getMaterial("MUSIC_DISC_CHIRP") : Material.getMaterial("RECORD_5"));
								break;
							case "ENTITY":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_CAT") != null ? Material.getMaterial("MUSIC_DISC_CAT") : Material.getMaterial("RECORD_4"));
								break;
							case "ITEM":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_FAR") != null ? Material.getMaterial("MUSIC_DISC_FAR") : Material.getMaterial("RECORD_6"));
								break;
							case "EVENT":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_WAIT") != null ? Material.getMaterial("MUSIC_DISC_WAIT") : Material.getMaterial("RECORD_11"));
								break;
							case "MUSIC":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_STRAD") != null ? Material.getMaterial("MUSIC_DISC_STRAD") : Material.getMaterial("RECORD_10"));
								break;
							case "PARTICLE":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_MELLOHI") != null ? Material.getMaterial("MUSIC_DISC_MELLOHI") : Material.getMaterial("RECORD_8"));
								break;
							case "UI":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_11") != null ? Material.getMaterial("MUSIC_DISC_11") : Material.getMaterial("GOLD_RECORD"));
								break;
							case "WEATHER":
								item = new ItemStack(Material.getMaterial("MUSIC_DISC_MALL") != null ? Material.getMaterial("MUSIC_DISC_MALL") : Material.getMaterial("RECORD_7"));
								break;
							}	
							for(ExplosionPhase cat : ExplosionPhase.values()) {
								if((isServer ? selectedSettings.getSoundSettings().getSound(cat).getSound() : ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getSound(cat).getSound()) == sounds) {
									item = new ItemStack(Material.NOTE_BLOCK);
									color = ChatColor.AQUA;
								}
							}
							return new StaticGuiElement('l', item, click -> {
								SoundData data = selectedSettings.getSoundSettings().getSound(selectedPhase);
								data.setSound(sounds); 
								if(isServer)
									selectedSettings.getSoundSettings().setSound(selectedPhase, data);
								else
									ProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).setSound(selectedPhase, data);
								g.draw(click.getEvent().getWhoClicked()); return true;}, color + StringUtils.capitaliseAllWords(sounds.toString().toLowerCase().replace("_", " ")));
						}));
					}
				} else {
					List<ParticleSettings> keys = new ArrayList<>(ParticleSettings.getParticleSettings());
					keys.sort(Comparator.comparing(ParticleSettings::getName));
					for(ParticleSettings particles : keys) {
						if(!player.hasPermission("explosionregen.command.rsettings.particles." + particles.getName().toLowerCase()))
							continue;
						listGroup.addElement(new DynamicGuiElement('l', () -> {
							ItemStack item = new ItemStack(Material.BOOK);
							ChatColor color = ChatColor.GRAY;
							if(isServer) {
								if(selectedSettings.getParticleSettings(ParticleType.PRESET) != null && selectedSettings.getParticleSettings(ParticleType.PRESET).getName().equals(particles.getName())) {
									item = new ItemStack(Material.WRITTEN_BOOK);
									ItemMeta meta = Objects.requireNonNull(item).getItemMeta();
									meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
									item.setItemMeta(meta);
									color = ChatColor.GOLD;
								}
							} else {
								if(ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.PRESET) != null && selectedSettings.getParticleSettings(ParticleType.PRESET).getName().equals(particles.getName())) {
									item = new ItemStack(Material.WRITTEN_BOOK);
									color = ChatColor.GOLD;
									ItemMeta meta = Objects.requireNonNull(item).getItemMeta();
									meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
									item.setItemMeta(meta);
								}
							}
							return new StaticGuiElement('l', item, click -> {
								if(isServer) {
									selectedSettings.setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toLowerCase())));
								} else
									ProfileSettings.get(click.getEvent().getWhoClicked().getUniqueId()).getProfileExplosionSettings(selectedSettings).setParticleSettings(ParticleType.PRESET, ParticleSettings.getSettings(ChatColor.stripColor(click.getEvent().getCurrentItem().getItemMeta().getDisplayName().replace(" ", "_").toUpperCase())));
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
						item = MaterialUtil.parseItemStack("LIME_DYE");
						name = "Block Regenerating";
						cc = 'q';
						break;
					case ON_BLOCK_REGEN:
						item = MaterialUtil.parseItemStack("LIGHT_BLUE_DYE");
						name = "On Block Regen";
						cc = 'w';
						break;
					case ON_EXPLODE:
						item = MaterialUtil.parseItemStack("PURPLE_DYE");
						name = "On Explode";
						cc = 'e';
						break;
					case EXPLOSION_FINISHED_REGEN:
						item = MaterialUtil.parseItemStack("PINK_DYE");
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
						String lore = "§7Selected " + (fi == 0 ? "Particle" : "Sound") + " Settings: §6" + (fi == 0 ? StringUtils.capitaliseAllWords(isServer ? selectedSettings.getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString().toLowerCase() : ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getParticleSettings(ParticleType.VANILLA).getParticles(phase).get(0).getParticle().toString().toLowerCase()) : StringUtils.capitaliseAllWords(isServer ? selectedSettings.getSoundSettings().getSound(phase).getSound().toString().toLowerCase().replace("_", " ") : ProfileSettings.get(player.getUniqueId()).getProfileExplosionSettings(selectedSettings).getSound(phase).getSound().toString().toLowerCase().replace("_", " ")));
						return new StaticGuiElement(fcc, item, click -> {selectedPhase = ExplosionPhase.valueOf(fName.substring(2).replace(" ", "_").toUpperCase()); g.draw(click.getEvent().getWhoClicked()); return true;}, fName, lore);
					}));
				}
				g.addElement(new GuiPageElement('z', Material.getMaterial("STAINED_GLASS_PANE") != null ? new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5) : new ItemStack(Material.getMaterial("LIME_STAINED_GLASS_PANE")), PageAction.PREVIOUS, "§7Previous"));
				g.addElement(new GuiPageElement('v', Material.getMaterial("STAINED_GLASS_PANE") != null ? new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)5) : new ItemStack(Material.getMaterial("LIME_STAINED_GLASS_PANE")), PageAction.NEXT, "§7Next"));
			}
		}
		explosionMenu.addElement(explosionGroup);
		explosionMenu.addElement('e', Material.getMaterial("STAINED_GLASS_PANE") != null ? new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)15) : new ItemStack(Material.getMaterial("BLACK_STAINED_GLASS_PANE")), click -> true, " ");
		explosionMenu.build();
		explosionMenu.show(player);
	}
	public static InventorySettings get(UUID uuid) {
		for(InventorySettings inv : inventories)
			if(inv.uuid == uuid)
				return inv;
		return new InventorySettings(uuid);
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
