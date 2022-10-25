package ru.etyosft.aurorawars.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etysoft.aurorauniverse.data.Residents;
import ru.etysoft.aurorauniverse.world.Resident;
import ru.etysoft.epcore.Console;
import ru.etysoft.epcore.TextManager;
import ru.etysoft.epcore.config.ConfigFile;
import ru.etysoft.epcore.gui.GUITable;
import ru.etysoft.epcore.gui.Items;
import ru.etysoft.epcore.gui.Slot;
import ru.etysoft.epcore.gui.SlotRunnable;

import java.util.HashMap;

public class ConfirmationMenu {
    public static void open(Player player, SlotRunnable confirmRunnable, String info) {
        Bukkit.getScheduler().runTaskAsynchronously(AuroraWars.getInstance(), new Runnable() {
            @Override
            public void run() {
                HashMap<Integer, Slot> matrix = new HashMap<>();
                Resident resident = Residents.getResident(player.getName());
                ConfigFile configFile = AuroraWars.getConfigFile();
                if (resident != null) {
                    if (resident.hasTown()) {
                        try {

                            ItemStack greenGlass = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
                            ItemStack redGlass = new ItemStack(Material.RED_STAINED_GLASS_PANE);

                            ItemStack confirmItem = Items.createNamedItem(greenGlass, configFile.getStringFromConfig("accept-menu.confirm"));
                            ItemStack denyItem = Items.createNamedItem(redGlass, configFile.getStringFromConfig("accept-menu.deny"));

                            ItemStack infoItem = Items.createNamedItem(new ItemStack(Material.EXPERIENCE_BOTTLE, 1), TextManager.toColor(info));

                            SlotRunnable infoRunnable = new SlotRunnable() {
                                @Override
                                public void run() {
                                    super.run();
                                }
                            };

                            SlotRunnable denyRunnable = new SlotRunnable() {
                                @Override
                                public void run() {
                                    super.run();
                                    player.closeInventory();
                                }
                            };

                            Slot infoSlot = new Slot(infoRunnable, infoItem);
                            Slot confirmSlot = new Slot(confirmRunnable, confirmItem);
                            Slot denySlot = new Slot(denyRunnable, denyItem);

                            matrix.put(1, infoSlot);
                            matrix.put(8, confirmSlot);
                            matrix.put(9, denySlot);

                            Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                                @Override
                                public void run() {


                                    try {
                                        GUITable guiTable = new GUITable(configFile.getStringFromConfig("accept-menu.title"),
                                                1, matrix, AuroraWars.getInstance(), Material.GRAY_STAINED_GLASS_PANE, true);
                                        guiTable.open(player);
                                    } catch (Exception e) {
                                        Console.sendWarning("AuroraWars >> GUITable caused an exception:");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (Exception e) {
                            player.sendMessage(configFile.getPrefixedStringFromConfig("errors.unknown"));
                            Console.sendWarning("AuroraWars >> Unknown Exception:");
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
