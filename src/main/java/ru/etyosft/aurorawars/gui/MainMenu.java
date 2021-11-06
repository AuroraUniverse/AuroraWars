package ru.etyosft.aurorawars.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etyosft.aurorawars.commands.Request;
import ru.etyosft.aurorawars.wars.War;
import ru.etysoft.aurorauniverse.chat.AuroraChat;
import ru.etysoft.aurorauniverse.data.Residents;
import ru.etysoft.aurorauniverse.data.Towns;
import ru.etysoft.aurorauniverse.world.Resident;
import ru.etysoft.aurorauniverse.world.Town;
import ru.etysoft.epcore.Console;
import ru.etysoft.epcore.TextManager;
import ru.etysoft.epcore.config.ConfigFile;
import ru.etysoft.epcore.gui.GUITable;
import ru.etysoft.epcore.gui.Items;
import ru.etysoft.epcore.gui.Slot;
import ru.etysoft.epcore.gui.SlotRunnable;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainMenu {

    public static void open(Player player, int page) {
        Bukkit.getScheduler().runTaskAsynchronously(AuroraWars.getInstance(), new Runnable() {
            @Override
            public void run() {
                HashMap<Integer, Slot> matrix = new HashMap<>();
                Resident resident = Residents.getResident(player.getName());
                ConfigFile configFile = AuroraWars.getConfigFile();
                if (resident != null) {
                    if (resident.hasTown()) {
                        try {

                            Town playerTown = resident.getTown();

                            ItemStack townItem = new ItemStack(Material.BEACON, 1);

                            String isAtWar = configFile.getStringFromConfig("main-menu.not-at-war");
                            String score = "";

                            if (AuroraWars.hasWar(playerTown)) {
                                isAtWar = configFile.getStringFromConfig("main-menu.at-war");

                                // "&7%attacker% (%attackerp%)  &fvs&7%victim%(%victimp%)"

                                War w = AuroraWars.getWarForTown(playerTown);

                                score = configFile.getStringFromConfig("war.score")
                                        .replace("%attacker%", w.getAttacker().getName())
                                        .replace("%attackerp%", String.valueOf(w.getAttackerPoints()))
                                        .replace("%victim%", w.getVictim().getName())
                                        .replace("%victimp%", String.valueOf(w.getVictimPoints()));
                            }

                            ItemStack townItemStack = Items.createNamedItem(townItem, TextManager.toColor("&f" + playerTown.getName()),
                                    isAtWar, score);

                            SlotRunnable townSlotRunnable = new SlotRunnable() {
                                @Override
                                public void run() {
                                    super.run();
                                    RequestsMenu.open(player);

                                }
                            };

                            Slot slotMyTown = new Slot(townSlotRunnable, townItemStack);


                            matrix.put(37, slotMyTown);

                            List<Town> towns = new ArrayList<Town>(Towns.getTowns());
                            int min = ((page - 1) * 36) + 1;
                            int slotNumber = 1;


                            if ((towns.size() - min) > 36) {
                                // Has next page

                                ItemStack paper = new ItemStack(Material.PAPER, 1);
                                ItemStack itemStack = Items.createNamedItem(paper, configFile.getStringFromConfig("main-menu.next-page"));

                                SlotRunnable slotRunnable = new SlotRunnable() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        open(player, page + 1);
                                    }
                                };

                                Slot slot = new Slot(slotRunnable, itemStack);

                                matrix.put(45, slot);

                            }

                            if (page > 1) {
                                // Has prev page

                                ItemStack paper = new ItemStack(Material.MAP, 1);
                                ItemStack itemStack = Items.createNamedItem(paper, configFile.getStringFromConfig("main-menu.prev-page"));

                                SlotRunnable slotRunnable = new SlotRunnable() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        open(player, page - 1);
                                    }
                                };

                                Slot slot = new Slot(slotRunnable, itemStack);

                                matrix.put(44, slot);
                            }

                            for (int i = min; i < towns.size(); i++) {
                                if (slotNumber <= 36) {
                                    Town town = towns.get(i);
                                    ItemStack mayorHead = new ItemStack(Material.STONE_BRICK_WALL);
                                    ItemStack itemStack;
                                    if (town.getMayor() != null) {
                                        if (town.getMayor().getPlayer() != null) {
                                            mayorHead = Items.getHead(town.getMayor().getPlayer());
                                        }
                                        if (!AuroraWars.hasWar(town)) {

                                            itemStack = Items.createNamedItem(mayorHead, TextManager.toColor("&f" + town.getName()),
                                                    configFile.getStringFromConfig("main-menu.residents").replace("%s", String.valueOf(town.getResidents().size())),
                                                    configFile.getStringFromConfig("main-menu.mayor").replace("%s", String.valueOf(town.getMayor().getName())));
                                        } else {
                                            String atWar = configFile.getStringFromConfig("main-menu.at-war");

                                            War w = AuroraWars.getWarForTown(town);

                                            String townScore = configFile.getStringFromConfig("war.score")
                                                    .replace("%attacker%", w.getAttacker().getName())
                                                    .replace("%attackerp%", String.valueOf(w.getAttackerPoints()))
                                                    .replace("%victim%", w.getVictim().getName())
                                                    .replace("%victimp%", String.valueOf(w.getVictimPoints()));

                                            itemStack = Items.createNamedItem(mayorHead, TextManager.toColor("&f" + town.getName()),
                                                    atWar, townScore);
                                        }
                                    } else {
                                        itemStack = Items.createNamedItem(new ItemStack(Material.RED_CONCRETE, 1), town.getName());
                                    }
                                    SlotRunnable slotRunnable = new SlotRunnable() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            if (player.hasPermission("townywars.mayor")) {
                                                SlotRunnable declatationRunnable = new SlotRunnable() {
                                                    @Override
                                                    public void run() {
                                                        super.run();
                                                        if (!AuroraWars.hasWar(playerTown) && !AuroraWars.hasWar(town)) {

                                                            Runnable onConfirm = new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    try {
                                                                        War w = new War(playerTown, town);
                                                                        String declare = configFile.getStringFromConfig("war.declare")
                                                                                .replace("%attacker%", w.getAttacker().getName())
                                                                                .replace("%victim%", w.getVictim().getName());

                                                                        AuroraChat.sendGlobalMessage(declare);

                                                                        player.closeInventory();
                                                                    } catch (Exception e) {
                                                                        player.sendMessage("AuroraWars declare error!");
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            };

                                                            boolean isSent = AuroraWars.sendRequest(new Request(playerTown, town, onConfirm, configFile.getStringFromConfig("requests.declare")
                                                                    .replace("%attacker%", playerTown.getName())));

                                                            if (isSent) {
                                                                player.sendMessage(configFile.getPrefixedStringFromConfig("requests.sent-declare")
                                                                        .replace("%victim%", town.getName()));

                                                                town.sendMessage(configFile.getPrefixedStringFromConfig("requests.declare")
                                                                        .replace("%attacker%", playerTown.getName()));
                                                            } else {
                                                                player.sendMessage(configFile.getPrefixedStringFromConfig("requests.cant-send"));
                                                            }
                                                            player.closeInventory();
                                                        }
                                                    }
                                                };

                                                ConfirmationMenu.open(player, declatationRunnable, configFile.getStringFromConfig("confirmation.declare")
                                                        .replace("%victim%", town.getName()));

                                            }
                                        }
                                    };
                                    Slot slot = new Slot(slotRunnable, itemStack);
                                    matrix.put(slotNumber, slot);
                                    slotNumber++;
                                }
                            }


                            Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        GUITable guiTable = new GUITable(configFile.getStringFromConfig("main-menu.title"),
                                                5, matrix, AuroraWars.getInstance(), Material.GRAY_STAINED_GLASS_PANE, true);
                                        guiTable.open(player);
                                    } catch (Exception e) {
                                        Console.sendWarning("AuroraWarsR >> GUITable caused an exception:");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            player.sendMessage(configFile.getPrefixedStringFromConfig("errors.unknown"));
                            Console.sendWarning("AuroraWarsR >> Unknown Exception:");
                            e.printStackTrace();
                        }

                    } else {
                        player.sendMessage(configFile.getPrefixedStringFromConfig("errors.no-town"));
                    }
                } else {
                    player.sendMessage(configFile.getPrefixedStringFromConfig("errors.no-town"));
                }
            }
        });

    }

}
