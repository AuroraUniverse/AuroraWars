package ru.etyosft.aurorawars.gui;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etysoft.aurorauniverse.chat.AuroraChat;
import ru.etysoft.aurorauniverse.data.Residents;
import ru.etysoft.aurorauniverse.world.Region;
import ru.etysoft.aurorauniverse.world.Resident;
import ru.etysoft.aurorauniverse.world.Town;
import ru.etysoft.epcore.Console;
import ru.etysoft.epcore.config.ConfigFile;
import ru.etysoft.epcore.gui.GUITable;
import ru.etysoft.epcore.gui.Items;
import ru.etysoft.epcore.gui.Slot;
import ru.etysoft.epcore.gui.SlotRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WinnerMenu {

    private Town winner;
    private Town looser;

    public WinnerMenu(Town winner, Town looser) {
        this.winner = winner;
        this.looser = looser;
    }

    public Town getLooser() {
        return looser;
    }

    public Town getWinner() {
        return winner;
    }

    public void open(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(AuroraWars.getInstance(), new Runnable() {
            @Override
            public void run() {
                HashMap<Integer, Slot> matrix = new HashMap<>();
                Resident resident = Residents.getResident(player.getName());
                ConfigFile configFile = AuroraWars.getConfigFile();
                if (resident != null) {
                    if (resident.hasTown()) {
                        try {

                            ItemStack emerald = new ItemStack(Material.EMERALD);
                            ItemStack horseArmor = new ItemStack(Material.LEATHER_HORSE_ARMOR);
                            ItemStack flintAndSteel = new ItemStack(Material.FLINT_AND_STEEL);

                            ItemStack stealItem = Items.createNamedItem(emerald, configFile.getStringFromConfig("war.steal"), configFile.getStringFromConfig("war.steal-lore").replace("%balance%",
                                    String.valueOf(looser.getBank().getBalance())));
                            ItemStack captureItem = Items.createNamedItem(horseArmor, configFile.getStringFromConfig("war.capture"), configFile.getStringFromConfig("war.capture-lore"));
                            ItemStack deleteItem = Items.createNamedItem(flintAndSteel, configFile.getStringFromConfig("war.delete"), configFile.getStringFromConfig("war.delete-lore"));


                            SlotRunnable stealRunnable = new SlotRunnable() {
                                @Override
                                public void run() {
                                    super.run();
                                    AuroraWars.winnerWait.remove(winner);
                                    getGUITable().setClosable(true);
                                    player.closeInventory();
                                    double balance = looser.getBank().getBalance();

                                    looser.getBank().setBalance(0);
                                    winner.getBank().deposit( balance);
                                    AuroraChat.sendGlobalMessage(configFile.getStringFromConfig("win-actions.steal")
                                            .replace("%winner%", winner.getName())
                                            .replace("%looser%", looser.getName()));
                                }
                            };

                            SlotRunnable captureRunnable = new SlotRunnable() {
                                @Override
                                public void run() {
                                    super.run();
                                    AuroraWars.winnerWait.remove(winner);
                                    getGUITable().setClosable(true);
                                    player.closeInventory();
                                    java.util.List<Region> tbs = null;
                                    try {
                                        tbs = new ArrayList<>(looser.getTownChunks().values());


                                    List<Region> tList = new ArrayList<>(tbs);

                                    for (Region tb : tList) {
                                        try {
                                            if (tb != looser.getTownChunks().get(looser.getMainChunk())) {
                                                Region.transfer(tb, winner);

                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }

                                   AuroraChat.sendGlobalMessage(configFile.getStringFromConfig("win-actions.capture")
                                            .replace("%winner%", winner.getName())
                                            .replace("%looser%", looser.getName()));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Bukkit.getConsoleSender().sendMessage("Error! Please report this to Discord https://discord.gg/Etd4XXH");
                                    }
                                }
                            };

                            SlotRunnable deleteRunnable = new SlotRunnable() {
                                @Override
                                public void run() {
                                    super.run();
                                    AuroraWars.winnerWait.remove(winner);
                                    getGUITable().setClosable(true);
                                    player.closeInventory();
                                    looser.delete();

                                    AuroraChat.sendGlobalMessage(configFile.getStringFromConfig("win-actions.delete")
                                            .replace("%winner%", winner.getName())
                                            .replace("%looser%", looser.getName()));
                                }
                            };

                            Slot stealSlot = new Slot(stealRunnable, stealItem);
                            Slot captureSlot = new Slot(captureRunnable, captureItem);
                            Slot deleteSlot = new Slot(deleteRunnable, deleteItem);

                            int slotid = 1;
                            if (configFile.getBooleanFromConfig("war.steal-enabled")) {
                                matrix.put(slotid, stealSlot);
                                slotid++;
                            }

                            if (configFile.getBooleanFromConfig("war.capture-enabled")) {
                                matrix.put(slotid, captureSlot);
                                slotid++;
                            }

                            if (configFile.getBooleanFromConfig("war.delete-enabled")) {
                                matrix.put(slotid, deleteSlot);
                            }


                            Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        GUITable guiTable = new GUITable(configFile.getStringFromConfig("war.win-title"),
                                                1, matrix, AuroraWars.getInstance(), Material.GRAY_STAINED_GLASS_PANE, false);
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
