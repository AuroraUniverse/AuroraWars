package ru.etyosft.aurorawars.gui;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etyosft.aurorawars.commands.Request;
import ru.etysoft.aurorauniverse.data.Residents;
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

public class RequestsMenu {

    public static void open(Player player) {
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

                            ArrayList<Request> requests = AuroraWars.getWarRequests(playerTown);

                            ItemStack emerald = new ItemStack(Material.EMERALD, 1);

                            int i = 1;
                            for (Request request : requests) {
                                if (i <= 18) {
                                    ItemStack requestItem = Items.createNamedItem(emerald, request.getTextInfo());
                                    SlotRunnable slotRunnable = new SlotRunnable() {
                                        @Override
                                        public void run() {
                                            super.run();
                                            if (player.hasPermission("aurorawars.mayor")) {
                                                SlotRunnable acceptRunnable = new SlotRunnable() {
                                                    @Override
                                                    public void run() {
                                                        super.run();
                                                        request.getOnAccept().run();
                                                        player.closeInventory();
                                                    }
                                                };


                                                ConfirmationMenu.open(player, acceptRunnable, request.getTextInfo());
                                            } else {
                                                player.sendMessage(configFile.getPrefixedStringFromConfig("errors.no-perm"));
                                            }

                                        }
                                    };

                                    Slot slot = new Slot(slotRunnable, requestItem);
                                    matrix.put(i, slot);
                                    i++;
                                }
                            }


                            Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        GUITable guiTable = new GUITable(configFile.getStringFromConfig("requests.title"),
                                                3, matrix, AuroraWars.getInstance(), Material.ORANGE_STAINED_GLASS_PANE, true);
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
