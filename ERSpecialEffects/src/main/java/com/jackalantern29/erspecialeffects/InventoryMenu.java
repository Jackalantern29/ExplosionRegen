package com.jackalantern29.erspecialeffects;

import com.jackalantern29.explosionregen.api.ExplosionParticle;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.ProfileSettings;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import com.jackalantern29.explosionregen.api.inventory.ItemBuilder;
import com.jackalantern29.explosionregen.api.inventory.PageMenu;
import com.jackalantern29.explosionregen.api.inventory.SettingsMenu;
import com.jackalantern29.explosionregen.api.inventory.SlotElement;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class InventoryMenu {
    private static final HashMap<ProfileSettings, InventoryMenu> MAP = new HashMap<>();
    private final SettingsMenu menu;
    private ExplosionPhase phase = ExplosionPhase.BLOCK_REGENERATING;

    private InventoryMenu(ProfileSettings profile) {

        ItemStack closeItem = new ItemBuilder(Material.BARRIER).setDisplayName("§c§lClose Menu").build();
        this.menu = new SettingsMenu("§2Select Explosion", 5);
        menu.setUpdate("menu", () -> {
            menu.clear();
            for(ExplosionSettings settings : ExplosionSettings.getRegisteredSettings()) {
                if(profile == null || profile.hasPermission(settings)) {
                    SettingsMenu typeMenu = new SettingsMenu("§2Select Option", 5);
                    PageMenu particleMenu = new PageMenu("§2Select Particle", 54);
                    PageMenu soundMenu = new PageMenu("§2Select Sound", 54);
                    List<PageMenu> pageSet = new ArrayList<>();
                    pageSet.add(particleMenu);
                    pageSet.add(soundMenu);

                    SpecialEffects effects;
                    if(profile != null) {
                        effects = (SpecialEffects)profile.getPlugin(settings, "SpecialEffects").toObject();
                    } else {
                        effects = (SpecialEffects)settings.getPlugin("SpecialEffects").toObject();
                    }
                    menu.addItem(new SlotElement(new ItemBuilder(settings.getDisplayItem()).setDisplayName(settings.getDisplayName()).build(), data -> {
                        typeMenu.sendInventory(data.getWhoClicked(), true);
                        return true;
                    }));
                    typeMenu.setItem(1, new SlotElement(new ItemBuilder(Material.NETHER_STAR).setDisplayName("§a§lParticles").build(), data -> {
                        particleMenu.sendInventory(data.getWhoClicked(), true);
                        return true;
                    }));
                    typeMenu.setItem(2, new SlotElement(new ItemBuilder(Material.NOTE_BLOCK).setDisplayName("§a§lSounds").build(), data -> {
                        soundMenu.sendInventory(data.getWhoClicked(), true);
                        return true;
                    }));
                    typeMenu.setItem(4, new SlotElement(closeItem, data -> {
                        menu.sendInventory(data.getWhoClicked(), true);
                        return true;
                    }));

                    for(PageMenu pageMenu : pageSet) {
                        pageMenu.setNextPageItem(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§aNext Page").build());
                        pageMenu.setPrevPageItem(new ItemBuilder(Material.LIME_STAINED_GLASS_PANE).setDisplayName("§aBack Page").build());

                        pageMenu.setUpdate("#layout", () -> {
                            for(SettingsMenu setMenu : pageMenu.getPages()) {
                                setMenu.setItem(0, new SlotElement(new ItemBuilder(Material.LIME_DYE).setDisplayName("§aBlock Regenerating").setGlow(phase == ExplosionPhase.BLOCK_REGENERATING).build(), data -> {
                                    phase = ExplosionPhase.BLOCK_REGENERATING;
                                    pageMenu.update("#layout");
                                    return true;
                                }));
                                setMenu.setItem(1, new SlotElement(new ItemBuilder(Material.LIGHT_BLUE_DYE).setDisplayName("§bOn Block Regen").setGlow(phase == ExplosionPhase.ON_BLOCK_REGEN).build(), data -> {
                                    phase = ExplosionPhase.ON_BLOCK_REGEN;
                                    pageMenu.update("#layout");
                                    return true;
                                }));
                                setMenu.setItem(2, new SlotElement(new ItemBuilder(Material.PURPLE_DYE).setDisplayName("§5On Explode").setGlow(phase == ExplosionPhase.ON_EXPLODE).build(), data -> {
                                    phase = ExplosionPhase.ON_EXPLODE;
                                    pageMenu.update("#layout");
                                    return true;
                                }));
                                setMenu.setItem(3, new SlotElement(new ItemBuilder(Material.PINK_DYE).setDisplayName("§dExplosion Finished Regen").setGlow(phase == ExplosionPhase.EXPLOSION_FINISHED_REGEN).build(), data -> {
                                    phase = ExplosionPhase.EXPLOSION_FINISHED_REGEN;
                                    pageMenu.update("#layout");
                                    return true;
                                }));
                                String[] toggleLore = new String[ExplosionPhase.values().length + 1];
                                if(particleMenu.hasPage(setMenu)) {
                                    for (int ii = 1; ii < toggleLore.length; ii++) {
                                        ExplosionPhase phase = ExplosionPhase.values()[ii - 1];
                                        String line = "";
                                        if (this.phase == phase)
                                            line = line + "§b";
                                        else
                                            line = line + "§7";
                                        line = line + WordUtils.capitalize(phase.toString().replace("-", " ")) + ": ";
                                        line = line + (effects.getParticleSettings(phase).canDisplay(phase) ? "§aTrue" : "§cFalse");
                                        toggleLore[ii] = line;
                                    }
                                    setMenu.setItem(4, new SlotElement(new ItemBuilder(Material.COMPASS).setDisplayName("§aToggle Particle").setLore(toggleLore).build(), data -> {
                                        effects.getParticleSettings(phase).setCanDisplay(phase, !effects.getParticleSettings(phase).canDisplay(phase));
                                        pageMenu.update("#layout");
                                        return true;
                                    }));
                                    setMenu.setItem(5, new SlotElement(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").build(), data -> true));

                                } else if(soundMenu.hasPage(setMenu)) {
                                    setMenu.setItem(4, new SlotElement(new ItemBuilder(Material.COMPASS).setDisplayName("§aToggle Sound").build(), data -> {
                                        effects.setAllowSound(phase, !effects.getAllowSound(phase));
                                        pageMenu.update("#layout");
                                        return true;
                                    }));
                                    setMenu.setItem(5, new SlotElement(new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").build(), data -> true));
                                }
                                setMenu.setItem(6, new SlotElement(pageMenu.getPrevPageItem(), data -> true));
                                setMenu.setItem(7, new SlotElement(pageMenu.getNextPageItem(), data -> true));
                                setMenu.setItem(8, new SlotElement(closeItem, data -> {
                                    typeMenu.sendInventory(data.getWhoClicked(), true);
                                    return true;
                                }));
                            }
                        });
                    }
                    particleMenu.setUpdate("particles", () -> {
                        particleMenu.clear();
                        particleMenu.update("#layout");
                        List<ParticleSettings> particleList = new ArrayList<>(ParticleSettings.getVanillaSettingss());
                        List<ParticleSettings> presetList = new ArrayList<>(ParticleSettings.getPresetSettings());
                        particleList.sort(Comparator.comparing(ParticleSettings::getName));
                        presetList.sort(Comparator.comparing(ParticleSettings::getName));

                        for(ParticleSettings particle : particleList) {
                            if(profile != null && !profile.getPlayer().hasPermission("explosionregen.command.rsettings.particles." + particle.getName().toLowerCase()))
                                continue;
                            String displayName = WordUtils.capitalize(particle.getName().toLowerCase().replace("_", " "));
                            ItemStack item = new ItemBuilder(Material.GRAY_DYE).setDisplayName("§7" + displayName).build();
                            for(ExplosionPhase phaseI : ExplosionPhase.values()) {
                                ParticleSettings phaseParticle = effects.getParticleSettings(phaseI);

                                if(particle == phaseParticle) {
                                    switch(phaseI) {
                                        case BLOCK_REGENERATING:
                                            item = new ItemBuilder(Material.LIME_DYE).setDisplayName("§a" + displayName).build();
                                            break;
                                        case ON_BLOCK_REGEN:
                                            item = new ItemBuilder(Material.LIGHT_BLUE_DYE).setDisplayName("§b" + displayName).build();
                                            break;
                                        case ON_EXPLODE:
                                            item = new ItemBuilder(Material.PURPLE_DYE).setDisplayName("§5" + displayName).build();
                                            break;
                                        case EXPLOSION_FINISHED_REGEN:
                                            item = new ItemBuilder(Material.PINK_DYE).setDisplayName("§d" + displayName).build();
                                            break;
                                    }
                                    break;
                                }
                            }
                            particleMenu.addItem(new SlotElement(item, data -> {
                                effects.setParticleSettings(phase, particle);
                                particleMenu.update("particles");
                                return true;
                            }));
                        }
                        for(ParticleSettings particle : presetList) {
                            if(profile != null && !profile.getPlayer().hasPermission("explosionregen.command.rsettings.particles." + particle.toString().toLowerCase()))
                                continue;
                            String displayName = WordUtils.capitalize(particle.getName().toLowerCase().replace("_", " "));
                            ItemStack item = new ItemBuilder(Material.BOOK).setDisplayName("§7" + displayName).setLine(0, "§7by " + particle.getAuthor()).build();
                            boolean check = true;
                            for(ExplosionPhase phaseI : ExplosionPhase.values()) {
                                ParticleSettings phaseParticle = effects.getParticleSettings(phaseI);
                                if(particle != phaseParticle) {
                                    check = false;
                                    break;
                                }

                            }
                            if(check)
                                item = new ItemBuilder(Material.WRITTEN_BOOK).setDisplayName("§6" + displayName).setLine(0, "§7by " + particle.getAuthor()).build();
                            particleMenu.addItem(new SlotElement(item, data -> {
                                for(ExplosionPhase phaseI : ExplosionPhase.values())
                                    effects.setParticleSettings(phaseI, particle);
                                particleMenu.update("particles");
                                return true;
                            }));
                        }
                    });
                    soundMenu.setUpdate("sounds", () -> {
                        soundMenu.clear();
                        soundMenu.update("phaseSelect");
                        for(Sound sound : Sound.values()) {
                            if(profile != null && !profile.getPlayer().hasPermission("explosionregen.command.rsettings.sounds." + sound.name().toLowerCase()))
                                continue;
                            String displayName = WordUtils.capitalize(sound.name().toLowerCase().replace("_", " "));
                            ItemStack item = new ItemBuilder(Material.MUSIC_DISC_STAL).setDisplayName("§7" + displayName).build();

                            switch(sound.name().split("_")[0]) {
                                case "AMBIENT":
                                    item = new ItemBuilder(Material.MUSIC_DISC_13).setDisplayName("§7" + displayName).build();
                                    break;
                                case "BLOCK":
                                    item = new ItemBuilder(Material.MUSIC_DISC_BLOCKS).setDisplayName("§7" + displayName).build();
                                    break;
                                case "ENCHANT":
                                    item = new ItemBuilder(Material.MUSIC_DISC_CHIRP).setDisplayName("§7" + displayName).build();
                                    break;
                                case "ENTITY":
                                    item = new ItemBuilder(Material.MUSIC_DISC_CAT).setDisplayName("§7" + displayName).build();
                                    break;
                                case "ITEM":
                                    item = new ItemBuilder(Material.MUSIC_DISC_FAR).setDisplayName("§7" + displayName).build();
                                    break;
                                case "EVENT":
                                    item = new ItemBuilder(Material.MUSIC_DISC_WAIT).setDisplayName("§7" + displayName).build();
                                    break;
                                case "MUSIC":
                                    item = new ItemBuilder(Material.MUSIC_DISC_STRAD).setDisplayName("§7" + displayName).build();
                                    break;
                                case "PARTICLE":
                                    item = new ItemBuilder(Material.MUSIC_DISC_MELLOHI).setDisplayName("§7" + displayName).build();
                                    break;
                                case "UI":
                                    item = new ItemBuilder(Material.MUSIC_DISC_11).setDisplayName("§7" + displayName).build();
                                    break;
                                case "WEATHER":
                                    item = new ItemBuilder(Material.MUSIC_DISC_MALL).setDisplayName("§7" + displayName).build();
                                    break;
                            }

                            for(ExplosionPhase phaseI : ExplosionPhase.values()) {
                                Sound phaseSound = effects.getSoundSettings().getSound(phaseI).getSound();
                                if(sound == phaseSound) {
                                    item = new ItemBuilder(Material.NOTE_BLOCK).setDisplayName("§b" + displayName).build();
                                    break;
                                }
                            }
                            soundMenu.addItem(new SlotElement(item, data -> {
                                SoundData soundData = effects.getSoundSettings().getSound(phase);
                                soundData.setSound(sound);
                                effects.getSoundSettings().setSound(phase, soundData);
                                soundMenu.update("sounds");
                                return true;
                            }));
                        }
                    });
                }
            }
        });
        MAP.put(profile, this);
    }

    public static void sendMenu(Player player, ProfileSettings profile) {
        if(MAP.containsKey(profile))
            MAP.get(profile).menu.sendInventory(player, true);
        else
            new InventoryMenu(profile).menu.sendInventory(player, true);

    }
}
