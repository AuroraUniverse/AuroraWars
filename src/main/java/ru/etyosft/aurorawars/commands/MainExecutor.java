package ru.etyosft.aurorawars.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etyosft.aurorawars.exceptions.AlreadyInWarException;
import ru.etyosft.aurorawars.exceptions.WarEndedException;
import ru.etyosft.aurorawars.gui.MainMenu;
import ru.etyosft.aurorawars.wars.War;
import ru.etysoft.aurorauniverse.AuroraUniverse;
import ru.etysoft.aurorauniverse.chat.AuroraChat;
import ru.etysoft.aurorauniverse.data.Residents;
import ru.etysoft.aurorauniverse.data.Towns;
import ru.etysoft.aurorauniverse.exceptions.TownNotFoundedException;
import ru.etysoft.aurorauniverse.utils.ColorCodes;
import ru.etysoft.aurorauniverse.world.ChunkPair;
import ru.etysoft.aurorauniverse.world.Region;
import ru.etysoft.aurorauniverse.world.Resident;
import ru.etysoft.aurorauniverse.world.Town;

public class MainExecutor implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender.hasPermission("aurorawars.use"))
        {
            if(args.length == 0)
            {
                if(sender instanceof Player)
                {
                    MainMenu.open((Player) sender, 1);
                }
                else
                {
                    sender.sendMessage("You cannot do it from console!");
                }
            }
            else
            {
                try {
                    String commandString = args[0].toLowerCase();
                    if (commandString.equals("placeflag")) {
                        if(!AuroraWars.getConfigFile().getBooleanFromConfig("war-type-occupation"))
                        {
                            sender.sendMessage((AuroraWars.getConfigFile().getPrefixedStringFromConfig("errors.type-unsupported")));
                        }
                        else
                        {
                            Player player = (Player) sender;

                            Resident resident = Residents.getResident(player);

                            Town town = AuroraUniverse.getTownBlock(ChunkPair.fromChunk(player.getLocation().getChunk()))
                                    .getTown();

                            War war = AuroraWars.getWarForTown(town);

                            if(war.getAttacker() == resident.getTown() || war.getVictim() == resident.getTown())
                            {



                            if(resident.getTown() != town) {

                                if (resident.getTown().withdrawBank(AuroraWars.getInstance().getConfig().getDouble("flag-price"))) {
                                    if (AuroraWars.getWarForTown(town).placeFlag(player.getLocation())) {


                                        resident.getTown().sendMessage((AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.flag-placed-attacker"))
                                                .replace("%s", ((int) player.getLocation().getX()) + ", " + ((int) player.getLocation().getZ())));

                                        town.sendMessage((AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.flag-placed-victim"))
                                                .replace("%s", ((int) player.getLocation().getX()) + ", " + ((int) player.getLocation().getZ())));

                                    } else {
                                        sender.sendMessage(AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.cant-place-flag"));
                                    }
                                } else {
                                    sender.sendMessage(AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.flag-price-error"));
                                }
                            }
                            }

                        }
                    }
                    else if (commandString.equals("info")) {
                        sender.sendMessage("Running AuroraWarsReloaded v" + AuroraWars.getInstance().getDescription().getVersion());
                    } else if (commandString.equals("declare")) {
                        if (sender.hasPermission("aurorawars.admin")) {
                            if (args.length == 3) {
                                String townNameFrom = args[1];
                                String townNameTo = args[2];
                                try {
                                    War war = new War(Towns.getTown(townNameFrom),
                                            Towns.getTown(townNameTo));
                                } catch (AlreadyInWarException e) {
                                    sender.sendMessage("Already in war!");
                                }
                            }
                        }
                    } else if (commandString.equals("end")) {
                        if (sender.hasPermission("aurorawars.admin")) {
                            if (args.length == 2) {
                                String town = args[1];

                                try {
                                    War war = AuroraWars.getWarForTown(Towns.getTown(town));
                                    war.end(false, war.getAttacker(), war.getVictim());
                                } catch (WarEndedException e) {
                                    sender.sendMessage("Already ended!");
                                }
                            }
                        }
                    } else if (commandString.equals("fend")) {
                        if (sender.hasPermission("aurorawars.admin")) {
                            if (args.length == 2) {
                                String town = args[1];

                                try {
                                    War war = AuroraWars.getWarForTown(Towns.getTown(town));
                                    war.end();
                                } catch (WarEndedException e) {
                                    sender.sendMessage("Already ended!");
                                }
                            }
                        }
                    }
                }catch (TownNotFoundedException t)
                {
                    sender.sendMessage("Town not founded!");
                }

            }



        }
        else
        {
            sender.sendMessage(AuroraWars.getConfigFile().getPrefixedStringFromConfig("errors.no-perms"));
        }
        return true;
    }
}
