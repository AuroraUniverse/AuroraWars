package ru.etyosft.aurorawars.listeners;


import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etyosft.aurorawars.exceptions.TownNotFoundedException;
import ru.etyosft.aurorawars.exceptions.WarEndedException;
import ru.etyosft.aurorawars.wars.War;
import ru.etysoft.aurorauniverse.AuroraUniverse;
import ru.etysoft.aurorauniverse.data.Residents;
import ru.etysoft.aurorauniverse.events.PreTownDeleteEvent;
import ru.etysoft.aurorauniverse.world.ChunkPair;
import ru.etysoft.aurorauniverse.world.Resident;
import ru.etysoft.aurorauniverse.world.Town;
import ru.etysoft.epcore.Console;
import ru.etysoft.epcore.config.ConfigFile;

public class MainListener implements Listener {

    @EventHandler
    public void killEvent(PlayerDeathEvent e) {
        try {
            Player defenderPlayer = e.getEntity();
            if (defenderPlayer.getLastDamageCause() instanceof EntityDamageByEntityEvent) {

                EntityDamageByEntityEvent damageEvent = (EntityDamageByEntityEvent) defenderPlayer.getLastDamageCause();
                Entity attackerEntity = damageEvent.getDamager();
                if (attackerEntity instanceof Player) {
                    Player killer = (Player) attackerEntity;
                    Resident attacker = Residents.getResident(killer.getName());
                    Resident victim = Residents.getResident(defenderPlayer.getName());
                    ConfigFile configFile = AuroraWars.getConfigFile();
                    if (attacker != null && victim != null) {
                        if (attacker.hasTown() && victim.hasTown()) {
                            if (AuroraWars.hasWar(attacker.getTown())) {
                                War war = AuroraWars.getWarForTown(attacker.getTown());

                                if (war.getAttacker() == attacker.getTown() && war.getVictim() == victim.getTown()
                                        || war.getAttacker() == victim.getTown() && war.getVictim() == attacker.getTown()) {
                                    // "&7Town &c%attacker% received %points% by killing %victim%"
                                    Town finalAttacker = war.addPointsForSide(attacker.getTown(), 1);

                                    String finalAnnounce = configFile.getStringFromConfig("war.kill")
                                            .replace("%attacker%", finalAttacker.getName())
                                            .replace("%points%", "1")
                                            .replace("%victim%", defenderPlayer.getName());

                                    war.getAttacker().sendMessage(finalAnnounce);
                                    war.getVictim().sendMessage(finalAnnounce);
                                }

                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            if (ex instanceof WarEndedException) {
                Console.sendWarning("AuroraWarsR >>  WarEndedException on PlayerKilledPlayerEvent!");
            } else if (ex instanceof TownNotFoundedException) {
                Console.sendWarning("AuroraWarsR >>  ERROR! Cannot find war for town:");
                ex.printStackTrace();
            } else {
                Console.sendWarning("AuroraWarsR >> PlayerKilledPlayerEvent caused an exception:");
                ex.printStackTrace();
            }

        }

    }

    @EventHandler
    public void onTownDelete(PreTownDeleteEvent e) {
        if(AuroraWars.hasWar(e.getTown()))
        {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Resident resident = Residents.getResident(player.getName());
        ConfigFile configFile = AuroraWars.getConfigFile();
        if (resident != null) {
            if (resident.hasTown()) {
                try {
                    if (AuroraWars.hasWar(resident.getTown())) {
                        player.sendTitle(configFile.getStringFromConfig("join.title"), configFile.getStringFromConfig("join.subtitle"), 20, 360, 20);
                    }

                    if (AuroraWars.winnerWait.containsKey(resident.getTown()) && resident.getTown().getMayor() == resident) {
                        AuroraWars.winnerWait.get(resident.getTown()).open(player);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        try {
            if(event.getBlock().getType() == Material.RED_BANNER) {
                Town town = AuroraUniverse.getTownBlock(ChunkPair.fromChunk(event.getBlock().getChunk())).getTown();
                if (AuroraWars.hasWar(town)) {

                    War war = AuroraWars.getWarForTown(town);
                    war.removeFlag(event.getBlock().getLocation());
                }
            }
        } catch (Exception ignored) {

        }


    }

}
