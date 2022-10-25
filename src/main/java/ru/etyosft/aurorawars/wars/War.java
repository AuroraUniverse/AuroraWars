package ru.etyosft.aurorawars.wars;


import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import ru.etyosft.aurorawars.AuroraWars;
import ru.etyosft.aurorawars.events.WarEndedEvent;
import ru.etyosft.aurorawars.exceptions.AlreadyInWarException;
import ru.etyosft.aurorawars.exceptions.TownNotFoundedException;
import ru.etyosft.aurorawars.exceptions.WarEndedException;
import ru.etyosft.aurorawars.gui.WinnerMenu;
import ru.etysoft.aurorauniverse.AuroraUniverse;
import ru.etysoft.aurorauniverse.chat.AuroraChat;
import ru.etysoft.aurorauniverse.utils.ColorCodes;
import ru.etysoft.aurorauniverse.world.ChunkPair;
import ru.etysoft.aurorauniverse.world.Region;
import ru.etysoft.aurorauniverse.world.Town;
import ru.etysoft.epcore.Console;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class War {

    private Town attacker;
    private Town victim;
    private boolean isEnded = false;

    private int attackerPoints;
    private int victimPoints;

    private String id;

    private Runnable warTimer;

    private ArrayList<Location> flags = new ArrayList<Location>();

    private HashMap<ChunkPair, Long> timeElapsed = new HashMap<>();

    private long startTimeMillis;

    public War(Town attacker, Town victim) throws AlreadyInWarException
    {
        if(AuroraWars.hasWar(attacker) | AuroraWars.hasWar(victim) | attacker == victim)
        {
            System.out.println(AuroraWars.hasWar(attacker) + " " + AuroraWars.hasWar(victim) + " " +(attacker == victim) + " - ?");
            throw new AlreadyInWarException();
        }
        else
        {
        attackerPoints = attacker.getResidents().size();
        victimPoints = victim.getResidents().size();
        this.victim = victim;
        this.attacker = attacker;
        victim.setForceExplosions(true);
        victim.setForcePvp(true);
        attacker.setForcePvp(true);
        attacker.setForceExplosions(true);
        AuroraWars.removeAllRequests(attacker);
        AuroraWars.removeAllRequests(victim);
        AuroraWars.registerWar(this);
        startTimeMillis = System.currentTimeMillis();

        id = attacker.getName() + victim.getName() + startTimeMillis;
        warTimer = new Runnable() {
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        for(Location location : new ArrayList<>(flags))
                        {
                            for(int i = -70; i < 70; i++)
                            {

                                try {

                                    Effect effect;

                                    int blockCount = 20;


                                    ChunkPair chunkPair = ChunkPair.fromChunk(location.getChunk());

                                    if(timeElapsed.containsKey(chunkPair)) {
                                        long time = timeElapsed.get(chunkPair);

                                        if (time > 100) {
                                            blockCount = 18;
                                        } else if (time > 80) {
                                            blockCount = 13;
                                        } else if (time > 60) {
                                            blockCount = 10;
                                        } else if (time > 50) {
                                            blockCount = 8;
                                        } else if (time > 40) {
                                            blockCount = 6;
                                        } else if (time > 30) {
                                            blockCount = 5;
                                        } else if (time > 20) {
                                            blockCount = 3;
                                        } else if (time > 10) {
                                            blockCount = 2;
                                        } else if (time > 5) {
                                            blockCount = 1;
                                        }


                                        if (Math.abs(i) < blockCount) {
                                            effect = Effect.SMOKE;
                                        } else {
                                            effect = Effect.MOBSPAWNER_FLAMES;
                                        }

                                        Location location1 = new Location(location.getWorld(), location.getX(), location.getY() + i, location.getZ());
                                        location.getWorld().playEffect(location1, effect, 0);
                                    }
                                }
                                catch (Exception e)
                                {
                                    e.printStackTrace();
                                }

                            }

                        }
                    }
                });
                for(ChunkPair chunkPair : new HashSet<>(timeElapsed.keySet()))
                {
                    long timeSec = timeElapsed.get(chunkPair);

                    if(timeSec > 0)
                    {
                        timeSec -= 1;



                        timeElapsed.remove(chunkPair);
                        timeElapsed.put(chunkPair, timeSec);
                    }
                    else
                    {

                        Region region = AuroraUniverse.getTownBlock(chunkPair);
                        Town town = region.getTown();
                        Town townOccupant;

                        if(town == attacker)
                        {
                            townOccupant = victim;
                        }
                        else
                        {
                            townOccupant = attacker;
                        }
                        Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                for (Location locationFlag : new ArrayList<>(flags))
                                {
                                    if(ChunkPair.fromChunk(locationFlag.getChunk()).equals(chunkPair)) {
                                        flags.remove(locationFlag);
                                        if (locationFlag.getWorld().getBlockAt(locationFlag).getType() == Material.RED_BANNER) {
                                            locationFlag.getWorld().getBlockAt(locationFlag).setType(Material.AIR);
                                        }
                                    }
                                }
                            }
                        });
                        if(town.getMainChunk().equals(chunkPair))
                        {

                                Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            end(true, townOccupant, town);
                                        } catch (WarEndedException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                });


                        }
                        else {
                            try {
                                region.getTown().sendMessage((AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.lose-region"))
                                        .replace("%s", (chunkPair.getX() * 16) + ", " + (chunkPair.getZ() * 16) ));
                                townOccupant.sendMessage((AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.annexed-region"))
                                        .replace("%s", (chunkPair.getX() * 16) + ", " + (chunkPair.getZ() * 16) ));


                                timeElapsed.remove(chunkPair);




                                Region.transfer(region, townOccupant);
                            } catch (ru.etysoft.aurorauniverse.exceptions.TownNotFoundedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    Bukkit.getScheduler().runTask(AuroraWars.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            updateFlags();
                        }
                    });



                }
            }
        };

        }
    }

    public boolean placeFlag(Location location)
    {
        if(!AuroraWars.getConfigFile().getBooleanFromConfig("war-type-occupation")) return false;

        if((new Location(location.getWorld(), location.getX(), location.getY() - 1, location.getZ())).getBlock().getType() == Material.AIR)
        {
            return false;
        }

        if((new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ())).getBlock().getType() != Material.AIR)
        {
            return false;
        }

        if(location.getBlock().getType() == Material.AIR || location.getBlock().getType() == Material.SNOW)
        {
            try {
                location.getWorld().getBlockAt(location).setType(Material.RED_BANNER);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            return false;
        }
        ChunkPair chunkPair = ChunkPair.fromChunk(location.getChunk());

        Region region = AuroraUniverse.getTownBlock(chunkPair);

        if(region != null)
        {
            if(region.getTown() != null)
            {
                Town town = region.getTown();

                if(town == victim || town == attacker)
                {
                    if(!flags.contains(location))
                    {
                        flags.add(location);
                    }

                    if(!timeElapsed.containsKey(chunkPair))
                    {
                        if(town.getMainChunk() == chunkPair)
                        {
                            timeElapsed.put(chunkPair, 180L);

                        }
                        else
                        {
                            timeElapsed.put(chunkPair, 60L);
                        }

                    }
                    return true;
                }
            }
        }
        return false;

    }

    public void removeFlag(Location location)
    {



        if(flags.contains(location))
        {
            flags.remove(location);
            Console.sendMessage("Removed flag");

            updateFlags();
        }
        if(location.getBlock().getType() == Material.RED_BANNER)
        {
            try {
                location.getWorld().getBlockAt(location).setType(Material.AIR);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        updateFlags();
    }

    public void updateFlags()
    {
        for(ChunkPair chunkPair : new HashSet<>(timeElapsed.keySet()))
        {
            boolean hasFlags = false;
            for(Location locationFlag : new ArrayList<>(flags))
            {
                if(locationFlag.getWorld().getBlockAt(locationFlag).getType() != Material.RED_BANNER)
                {
                    flags.remove(locationFlag);
                }
                else {


                    if (chunkPair.equals(ChunkPair.fromChunk(locationFlag.getChunk()))) {
                        hasFlags = true;
                    }

                }
            }

            if(!hasFlags)
            {
                timeElapsed.remove(chunkPair);

                Region region = AuroraUniverse.getTownBlock(chunkPair);

                region.getTown().sendMessage((AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.flag-removed-victim"))
                    .replace("%s", (chunkPair.getX() * 16) + ", " + (chunkPair.getZ() * 16) ));

                Town townVictim = victim;

                if(region.getTown() == victim)
                {
                    townVictim = attacker;
                }

                townVictim.sendMessage((AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.flag-removed-attacker"))
                        .replace("%s", (chunkPair.getX() * 16) + ", " + (chunkPair.getZ() * 16) ));


            }
        }
    }

    public Runnable getWarTimer() {
        return warTimer;
    }

    public void checkExpired()
    {
        if(System.currentTimeMillis() - startTimeMillis > ((long) AuroraWars.getConfigFile().getIntFromConfig("war.max-time-sec") * 1000) && !isEnded)
        {
            AuroraChat.sendGlobalMessage(AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.expired")
                    .replace("%attacker%", attacker.getName())
            .replace("%victim%", victim.getName()));
            try
            {
                if(victimPoints > attackerPoints)
                {
                    attackerPoints = 0;
                    end();
                }
                else if(victimPoints < attackerPoints)
                {
                    victimPoints = 0;

                    end();
                }
                else
                {
                    end(false, attacker, victim);
                    AuroraChat.sendGlobalMessage(AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.draw")
                            .replace("%attacker%", attacker.getName())
                            .replace("%victim%", victim.getName()));
                }

            }
            catch (Exception ignored) {}
        }
    }

    public int getAttackerPoints() {
        return attackerPoints;
    }

    public int getVictimPoints() {
        return victimPoints;
    }

    public Town getAttacker() {
        return attacker;
    }

    public Town getVictim() {
        return victim;
    }

    public boolean isEnded() {
        return isEnded;
    }


    public void end() throws WarEndedException {
        Town winner, looser;
        if(attackerPoints <= 0)
        {
            winner = victim;
            looser = attacker;
        }
        else
        {
            winner = attacker;
            looser = victim;
        }
        end(true, winner, looser);
    }

    public void end(boolean withPunish, Town winner, Town looser) throws WarEndedException {
        if(isEnded) throw new WarEndedException();
        isEnded = true;
        AuroraWars.unregisterWar(this);
        Bukkit.getServer().getPluginManager().callEvent(new WarEndedEvent(this));

        victim.setForceExplosions(false);
        victim.setForcePvp(false);
        attacker.setForcePvp(false);
        attacker.setForceExplosions(false);



        String declare;
        if(withPunish)
        {
            WinnerMenu winnerMenu = new WinnerMenu(winner, looser);
            AuroraWars.winnerWait.put(winner, winnerMenu);
            winnerMenu.open(winner.getMayor().getPlayer());
            declare = AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.win")
                    .replace("%winner%", winner.getName())
                    .replace("%looser%", looser.getName());

        }
        else
        {
            declare = AuroraWars.getConfigFile().getPrefixedStringFromConfig("war.win-without-punishment")
                    .replace("%winner%", winner.getName())
                    .replace("%looser%", looser.getName());
        }
        AuroraChat.sendGlobalMessage(declare);
    }



    public Town addPointsForSide(Town side, int points) throws WarEndedException, TownNotFoundedException {
        if(side == attacker)
        {
            addAttackerPoints(points);
            return attacker;
        }
        else if(side == victim)
        {
            addVictimPoints(points);
            return victim;
        }
        throw new TownNotFoundedException();
    }

    public void addAttackerPoints(int points) throws WarEndedException {

        if(isEnded) throw new WarEndedException();
        if(!AuroraWars.getConfigFile().getBooleanFromConfig("war-type-points")) return;
        attackerPoints += points;
        victimPoints -= points;

        if (victimPoints <= 0)
        {
            end();
        }
        checkExpired();
    }

    public void addVictimPoints(int points) throws WarEndedException {
        if(isEnded) throw new WarEndedException();
        if(!AuroraWars.getConfigFile().getBooleanFromConfig("war-type-points")) return;
        victimPoints += points;
        attackerPoints -= points;

        if (attackerPoints <= 0)
        {
            end();
        }
        checkExpired();
    }

    public String getId() {
        return id;
    }
}
